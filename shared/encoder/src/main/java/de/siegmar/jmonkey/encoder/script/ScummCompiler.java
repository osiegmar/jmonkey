/*
 * JMonkey - Java based development kit for "The Secret of Monkey Island"
 * Copyright (C) 2022  Oliver Siegmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.siegmar.jmonkey.encoder.script;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.ByteStringBuilder;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.decoder.script.ScummVars;
import de.siegmar.jmonkey.decoder.script.VarType;
import de.siegmar.jmonkey.encoder.script.parser.statement.AssignmentExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BinaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BooleanLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.CallExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.EvalExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Expression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.GotoStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.Identifier;
import de.siegmar.jmonkey.encoder.script.parser.statement.IncDecExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.LabeledStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.MemberExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;
import de.siegmar.jmonkey.encoder.script.parser.statement.Statement;
import de.siegmar.jmonkey.encoder.script.parser.statement.StringLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.UnaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.UnlessStatement;

// TODO Oh boy, this needs refactoring!
@SuppressWarnings({
    "checkstyle:ClassFanOutComplexity",
    "checkstyle:MethodCount",
    "checkstyle:NestedIfDepth",
    "checkstyle:JavaNCSS",
    "checkstyle:CyclomaticComplexity",
    "checkstyle:ExecutableStatementCount",
    "PMD.AvoidDuplicateLiterals"
})
public class ScummCompiler {

    private final ByteStringBuilder bsb = new ByteStringBuilder();
    private final Map<String, Integer> labelOffsets = new HashMap<>();
    private final Map<Integer, String> relativeGotoOffsets = new HashMap<>();

    public ByteString compile(final Program program) {
        for (final Statement statement : program.getBody()) {
            final Statement stmt;
            if (statement instanceof LabeledStatement ls) {
                labelOffsets.put(ls.getLabel().getName(), bsb.size());
                stmt = ((LabeledStatement) statement).getStatement();
            } else {
                stmt = statement;
            }
            expressionStatement(stmt);
        }
        final ByteString bs = bsb.build();

        if (!relativeGotoOffsets.isEmpty()) {
            final byte[] bytes = bs.dumpCopy();
            for (final Map.Entry<Integer, String> entry : relativeGotoOffsets.entrySet()) {
                final int offset = entry.getKey();
                final String label = entry.getValue();
                final int target = labelOffsets.get(label) - offset - 2;

                bytes[offset] = (byte) (target & 0xFF);
                bytes[offset + 1] = (byte) ((target >>> 8) & 0xFF);
            }
            return ByteString.wrap(bytes);
        }

        return bs;
    }

    public ByteString compileExpression(final ExpressionStatement statement) {
        expression(statement);
        return bsb.build();
    }

    private void expressionStatement(final Statement statement) {
        if (statement instanceof ExpressionStatement es) {
            expression(es);
        } else if (statement instanceof GotoStatement gs) {
            gotoStmt(gs);
        } else if (statement instanceof UnlessStatement us) {
            unlessStmt(us);
        } else {
            throw new IllegalStateException("Statement not supported: " + statement);
        }
    }

    private void unlessStmt(final UnlessStatement us) {
        int opcode = 0x28;

        Expression exp = us.getExpression();

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        if (exp instanceof UnaryExpression ua) {
            if (!"!".equals(ua.getOperator())) {
                throw new IllegalStateException("Invalid operator: " + ua.getOperator());
            }
            exp = ua.getExpression();
        } else if (exp instanceof BinaryExpression be) {
            if (be.getLeft() instanceof CallExpression ce) {
                if (ce.getCallee().equals(Identifier.of("getState"))) {
                    unlessStateGoto(us);
                }
            } else {
                unlessBinaryStmt(us);
            }
            return;
        } else if (exp instanceof CallExpression ce) {
            if (ce.getCallee().equals(Identifier.of("classOfIs"))) {
                classOfIsGoto(us);
                return;
            } else {
                throw new IllegalStateException("Invalid CallExpression: " + ce);
            }

        } else {
            opcode |= 0x80;
        }

        final String labelName = us.getConsequence().getLabel().getName();

        appendRef(exp, 0, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        relativeGotoOffsets.put(bsb.size(), labelName);
        bsb.appendU16(0);
    }

    private void classOfIsGoto(final UnlessStatement us) {
        final CallExpression ce = (CallExpression) us.getExpression();

        final List<Expression> arguments = ce.getArguments();

        int opcode = 0x1D;

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append16(arguments.get(0), 0, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        writeList16((CallExpression) arguments.get(1));

        relativeGotoOffsets.put(bsb.size(), us.getConsequence().getLabel().getName());
        bsb.appendU16(0);
    }

    private void unlessStateGoto(final UnlessStatement us) {
        final BinaryExpression be = (BinaryExpression) us.getExpression();

        int opcode;
        if ("==".equals(be.getOperator())) {
            opcode = 0x0F;
        } else if ("!=".equals(be.getOperator())) {
            opcode = 0x2F;
        } else {
            throw new IllegalStateException("Invalid operator: " + be.getOperator());
        }

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) be.getLeft()).getArguments().get(0), 0, paramBuilder);
        opcode |= append8(be.getRight(), 1, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        relativeGotoOffsets.put(bsb.size(), us.getConsequence().getLabel().getName());
        bsb.appendU16(0);
    }

    private void unlessBinaryStmt(final UnlessStatement us) {
        final BinaryExpression be = (BinaryExpression) us.getExpression();

        int opcode = switch (be.getOperator()) {
            case ">=" -> 0x38;
            case "<" -> 0x78;
            case "<=" -> 0x04;
            case ">" -> 0x44;
            case "==" -> 0x48;
            case "!=" -> 0x08;
            default -> throw new IllegalStateException("Invalid operator: " + be.getOperator());
        };

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        appendRef(be.getLeft(), 0, paramBuilder);

        opcode |= append16(be.getRight(), 0, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        relativeGotoOffsets.put(bsb.size(), us.getConsequence().getLabel().getName());
        bsb.appendU16(0);
    }

    private void gotoStmt(final GotoStatement gs) {
        final String label = gs.getLabel().getName();
        bsb.appendU8(0x18);
        relativeGotoOffsets.put(bsb.size(), label);
        bsb.appendU16(0);
    }

    private void expression(final ExpressionStatement es) {
        final Expression expression = es.getExpression();
        if (expression instanceof CallExpression ce) {
            callExpression(ce);
        } else if (expression instanceof AssignmentExpression ae) {
            assignExpression(ae);
        } else if (expression instanceof IncDecExpression ide) {
            incDecExpression(ide);
        } else {
            throw new IllegalStateException("Expression not supported: " + es);
        }
    }

    private void callExpression(final CallExpression exp) {
        if (exp.getCallee() instanceof MemberExpression callee) {
            final Identifier object = (Identifier) callee.getObject();
            final Identifier property = (Identifier) callee.getProperty();
            if ("system".equals(object.getName())) {
                if ("quit".equals(property.getName())) {
                    systemQuit();
                }
            } else if ("resource".equals(object.getName())) {
                switch (property.getName()) {
                    case "clearHeap" -> resourceClearHeap();
                    case "loadScript" -> resourceLoadScript(exp.getArguments());
                    case "loadSound" -> resourceLoadSound(exp.getArguments());
                    case "loadCostume" -> resourceLoadCostume(exp.getArguments());
                    case "loadRoom" -> resourceLoadRoom(exp.getArguments());
                    case "nukeCostume" -> resourceNukeCostume(exp.getArguments());
                    case "nukeRoom" -> resourceNukeRoom(exp.getArguments());
                    case "lockScript" -> resourceLockScript(exp.getArguments());
                    case "lockSound" -> resourceLockSound(exp.getArguments());
                    case "lockCostume" -> resourceLockCostume(exp.getArguments());
                    case "lockRoom" -> resourceLockRoom(exp.getArguments());
                    case "unlockScript" -> resourceUnlockScript(exp.getArguments());
                    case "unlockSound" -> resourceUnlockSound(exp.getArguments());
                    case "unlockCostume" -> resourceUnlockCostume(exp.getArguments());
                    case "unlockRoom" -> resourceUnlockRoom(exp.getArguments());
                    case "loadCharset" -> resourceLoadCharset(exp.getArguments());
                    default -> throw new IllegalStateException("Unknown property: " + property.getName());
                }
            }
        } else if (exp.getCallee() instanceof Identifier callee) {
            switch (callee.getName()) {
                case "stopLocalGlobalScript" -> stopLocalGlobalScript();
                case "stopObjectCode" -> stopObjectCode();
                case "cutscene" -> cutScene(exp.getArguments());
                case "beginOverride" -> beginOverride();
                case "endOverride" -> endOverride();
                case "delay" -> delay(exp.getArguments());
                case "startSound" -> startSound(exp.getArguments());
                case "breakHere" -> breakHere();
                case "waitForActor" -> waitForActor(exp.getArguments());
                case "waitForMessage" -> waitForMessage();
                case "waitForCamera" -> waitForCamera();
                case "waitForSentence" -> waitForSentence();
                case "endCutscene" -> endCutscene();
                case "stopSound" -> stopSound(exp.getArguments());
                case "createBoxMatrix" -> createBoxMatrix();
                case "setBoxFlags" -> setBoxFlags(exp.getArguments());
                case "actorFollowCamera" -> actorFollowCamera(exp.getArguments());
                case "setCameraAt" -> setCameraAt(exp.getArguments());
                case "panCameraTo" -> panCameraTo(exp.getArguments());
                case "debug" -> debug(exp.getArguments());
                case "freezeScripts" -> freezeScripts(exp.getArguments());
                case "cursor" -> cursor(exp.getArguments());
                case "userput" -> userput(exp.getArguments());
                case "initCharset" -> initCharset(exp.getArguments());
                case "loadRoom" -> loadRoom(exp.getArguments());
                case "stopScript" -> stopScript(exp.getArguments());
                case "pickupObject" -> pickupObject(exp.getArguments());
                case "delayVariable" -> delayVariable(exp.getArguments());
                case "oldRoomEffectSet" -> oldRoomEffectSet(exp.getArguments());
                case "roomScroll" -> roomScroll(exp.getArguments());
                case "roomColor" -> roomColor(exp.getArguments());
                case "setScreen" -> setScreen(exp.getArguments());
                case "setPalColor" -> setPalColor(exp.getArguments());
                case "shake" -> shake(exp.getArguments());
                case "setClass" -> setClass(exp.getArguments());
                case "pseudoRoom" -> pseudoRoom(exp.getArguments());
                case "putCodeInString" -> putCodeInString(exp.getArguments());
                case "copyString" -> copyString(exp.getArguments());
                case "setStringChar" -> setStringChar(exp.getArguments());
                case "createString" -> createString(exp.getArguments());
                case "animateCostume" -> animateCostume(exp.getArguments());
                case "faceActor" -> faceActor(exp.getArguments());
                case "setState" -> setState(exp.getArguments());
                case "putActorInRoom" -> putActorInRoom(exp.getArguments());
                case "setOwnerOf" -> setOwnerOf(exp.getArguments());
                case "walkActorToObject" -> walkActorToObject(exp.getArguments());
                case "putActorAtObject" -> putActorAtObject(exp.getArguments());
                case "chainScript" -> chainScript(exp.getArguments());
                case "setObjectName" -> setObjectName(exp.getArguments());
                case "putActor" -> putActor(exp.getArguments());
                case "drawObject" -> drawObject(exp.getArguments());
                case "drawBox" -> drawBox(exp.getArguments());
                case "walkActorToActor" -> walkActorToActor(exp.getArguments());
                case "lights" -> lights(exp.getArguments());
                case "walkActorTo" -> walkActorTo(exp.getArguments());
                case "startObject" -> startObject(exp.getArguments());
                case "loadRoomWithEgo" -> loadRoomWithEgo(exp.getArguments());
                case "setVarRange" -> setVarRange(exp.getArguments());
                case "saveVerbs" -> saveVerbs(exp.getArguments());
                case "restoreVerbs" -> restoreVerbs(exp.getArguments());
                case "startScript" -> startScript(exp.getArguments());
                case "actorOps" -> actorOps(exp.getArguments());
                case "verbOps" -> verbOps(exp.getArguments());
                case "print" -> print(exp.getArguments(), false);
                case "printEgo" -> print(exp.getArguments(), true);
                case "doSentence" -> doSentence(exp.getArguments());
                case "stopSentence" -> stopSentence();
                default -> throw new IllegalStateException("Unknown callee: " + callee.getName());
            }
        } else {
            throw new IllegalStateException("Invalid type: " + exp);
        }
    }

    private void stopSentence() {
        bsb.appendU8(0x19);
        bsb.appendU8(0xFE);
        // FIXME see DoSentenceOpcode
    }

    private void doSentence(final List<Expression> arguments) {
        int opcode = 0x19;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append8(arguments.get(0), 0, paramBuilder);
        opcode |= append16(arguments.get(1), 1, paramBuilder);
        opcode |= append16(arguments.get(2), 2, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void print(final List<Expression> arguments, final boolean ego) {
        final int argumentStart;
        if (ego) {
            final int opcode = 0xD8;
            bsb.appendU8(opcode);
            argumentStart = 0;
        } else {
            int opcode = 0x14;

            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            opcode |= append8(arguments.get(0), 0, paramBuilder);

            bsb.appendU8(opcode);
            bsb.append(paramBuilder.build());
            argumentStart = 1;
        }

        boolean endedWithText = false;

        for (int i = argumentStart; i < arguments.size(); i++) {
            final AssignmentExpression argument = (AssignmentExpression) arguments.get(i);
            if (!"=".equals(argument.getOperator())) {
                throw new IllegalStateException("Invalid operator: " + argument.getOperator());
            }
            final String name = ((Identifier) argument.getLeft()).getName();
            final Expression rightExpression = argument.getRight();

            if ("pos".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                final CallExpression callExpression = (CallExpression) argument.getRight();
                if (!Identifier.of("listOf").equals(callExpression.getCallee())) {
                    throw new IllegalStateException("Invalid callee: " + callExpression.getCallee());
                }
                final List<Expression> arguments1 = callExpression.getArguments();
                if (arguments1.size() != 2) {
                    throw new IllegalStateException("Wrong number of arguments (has to be 2): " + arguments1);
                }

                int subOpcode = 0x00;
                subOpcode |= appendParameter16(arguments1, 0, namedArgBuilder);
                subOpcode |= appendParameter16(arguments1, 1, namedArgBuilder);

                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
//                bsb.appendU8(0xFF);
            } else if ("color".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x01;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
//                bsb.appendU8(0xFF);
            } else if ("clipped".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x02;
                subOpcode |= append16(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
//                bsb.appendU8(0xFF);
            } else if ("center".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x04);
                }
//                bsb.appendU8(0xFF);
            } else if ("left".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x06);
                }
//                bsb.appendU8(0xFF);
            } else if ("overhead".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x07);
                }
            } else if ("text".equals(name)) {
                final int subOpcode = 0x0F;
                bsb.appendU8(subOpcode);
                bsb.append(ScummStringEncoder.encode(((StringLiteralExpression) rightExpression).getValue(), true));
                endedWithText = true;
            } else {
                throw new IllegalStateException("Unknown argument: " + argument);
            }
        }

        if (!endedWithText) {
            bsb.appendU8(0xFF);
        }

        // TODO check that text was the last argument !
    }

    private void verbOps(final List<Expression> arguments) {
        int opcode = 0x7A;

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append8(arguments.get(0), 0, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        for (int i = 1; i < arguments.size(); i++) {
            final AssignmentExpression argument = (AssignmentExpression) arguments.get(i);
            if (!"=".equals(argument.getOperator())) {
                throw new IllegalStateException("Invalid operator: " + argument.getOperator());
            }
            final String name = ((Identifier) argument.getLeft()).getName();
            final Expression rightExpression = argument.getRight();

            if ("text".equals(name)) {
                final int subOpcode = 0x02;
                bsb.appendU8(subOpcode);
                bsb.append(ScummStringEncoder.encode(((StringLiteralExpression) rightExpression).getValue(), false));
            } else if ("color".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x03;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("hiColor".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x04;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("xy".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                final CallExpression callExpression = (CallExpression) argument.getRight();
                if (!Identifier.of("listOf").equals(callExpression.getCallee())) {
                    throw new IllegalStateException("Invalid callee: " + callExpression.getCallee());
                }
                final List<Expression> arguments1 = callExpression.getArguments();
                if (arguments1.size() != 2) {
                    throw new IllegalStateException("Wrong number of arguments (has to be 2): " + arguments1);
                }

                int subOpcode = 0x05;
                subOpcode |= appendParameter16(arguments1, 0, namedArgBuilder);
                subOpcode |= appendParameter16(arguments1, 1, namedArgBuilder);

                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("on".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x06);
                }
            } else if ("off".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x07);
                }
            } else if ("create".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x09);
                }
            } else if ("dimColor".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x10;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("key".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x12;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("center".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x13);
                }
            } else if ("toString".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x14;
                subOpcode |= append16(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else {
                throw new IllegalStateException("Unknown argument: " + argument);
            }
        }

        bsb.appendU8(0xFF);
    }

    private void actorOps(final List<Expression> arguments) {
        int opcode = 0x13;

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append8(arguments.get(0), 0, paramBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        for (int i = 1; i < arguments.size(); i++) {
            final AssignmentExpression argument = (AssignmentExpression) arguments.get(i);
            if (!"=".equals(argument.getOperator())) {
                throw new IllegalStateException("Invalid operator: " + argument.getOperator());
            }
            final String name = ((Identifier) argument.getLeft()).getName();
            final Expression rightExpression = argument.getRight();

            if ("init".equals(name)) {
                final BooleanLiteralExpression exp = (BooleanLiteralExpression) rightExpression;
                if (exp.getValue()) {
                    bsb.appendU8(0x0A);
                }
            } else if ("costume".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x01;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("walkSpeed".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                final CallExpression callExpression = (CallExpression) argument.getRight();
                if (!Identifier.of("listOf").equals(callExpression.getCallee())) {
                    throw new IllegalStateException("Invalid callee: " + callExpression.getCallee());
                }
                final List<Expression> arguments1 = callExpression.getArguments();
                if (arguments1.size() != 2) {
                    throw new IllegalStateException("Wrong number of arguments (has to be 2): " + arguments1);
                }

                int subOpcode = 0x04;
                subOpcode |= appendParameter8(arguments1, 0, namedArgBuilder);
                subOpcode |= appendParameter8(arguments1, 1, namedArgBuilder);

                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("sound".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x05;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("walkAnimNr".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x06;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("talkAnimNr".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                final CallExpression callExpression = (CallExpression) argument.getRight();
                if (!Identifier.of("listOf").equals(callExpression.getCallee())) {
                    throw new IllegalStateException("Invalid callee: " + callExpression.getCallee());
                }
                final List<Expression> arguments1 = callExpression.getArguments();
                if (arguments1.size() != 2) {
                    throw new IllegalStateException("Wrong number of arguments (has to be 2): " + arguments1);
                }

                int subOpcode = 0x07;
                subOpcode |= appendParameter8(arguments1, 0, namedArgBuilder);
                subOpcode |= appendParameter8(arguments1, 1, namedArgBuilder);

                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("standAnimNr".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x08;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("elevation".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x0B;
                subOpcode |= append16(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("width".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x12;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("scale".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x13;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("palette".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                final CallExpression callExpression = (CallExpression) argument.getRight();
                if (!Identifier.of("listOf").equals(callExpression.getCallee())) {
                    throw new IllegalStateException("Invalid callee: " + callExpression.getCallee());
                }
                final List<Expression> arguments1 = callExpression.getArguments();
                if (arguments1.size() != 2) {
                    throw new IllegalStateException("Wrong number of arguments (has to be 2): " + arguments1);
                }

                int subOpcode = 0x0D;
                subOpcode |= appendParameter8(arguments1, 0, namedArgBuilder);
                subOpcode |= appendParameter8(arguments1, 1, namedArgBuilder);

                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("talkColor".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x0E;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else if ("name".equals(name)) {
                final int subOpcode = 0x0F;
                bsb.appendU8(subOpcode);
                bsb.append(
                    ScummStringEncoder.encode(((StringLiteralExpression) argument.getRight()).getValue(), false));
            } else if ("initAnimNr".equals(name)) {
                final ByteStringBuilder namedArgBuilder = new ByteStringBuilder();
                int subOpcode = 0x10;
                subOpcode |= append8(argument.getRight(), 0, namedArgBuilder);
                bsb.appendU8(subOpcode);
                bsb.append(namedArgBuilder.build());
            } else {
                throw new IllegalStateException("Unknown argument: " + argument);
            }
        }

        bsb.appendU8(0xFF);
    }

    private void startScript(final List<Expression> arguments) {
        int opcode = 0x0A;

        for (int i = 2; i < arguments.size(); i++) {
            final AssignmentExpression expression = (AssignmentExpression) arguments.get(i);
            final String name = ((Identifier) expression.getLeft()).getName();
            final boolean value = ((BooleanLiteralExpression) expression.getRight()).getValue();

            if (value) {
                if ("freezeResistant".equals(name)) {
                    opcode |= 0x20;
                } else if ("recursive".equals(name)) {
                    opcode |= 0x40;
                } else {
                    throw new IllegalStateException("Unknown argument: " + name);
                }
            }
        }

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(arguments.get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        bsb.append(parameterBuilder.build());
        writeList16((CallExpression) arguments.get(1));
    }

    private void assignExpression(final AssignmentExpression ae) {
        if (ae.getRight() instanceof NumericLiteralExpression) {
            assignNle(ae);
        } else if (ae.getRight() instanceof UnaryExpression) {
            assignNle(ae);
        } else if (ae.getRight() instanceof MemberExpression) {
            assignNle(ae);
        } else if (ae.getRight() instanceof Identifier) {
            assignNle(ae);
        } else if (ae.getRight() instanceof BinaryExpression) {
            assignBinaryExpression(ae);
        } else if (ae.getRight() instanceof AssignmentExpression) {
            assignBinaryExpression(ae);
        } else if (ae.getRight() instanceof EvalExpression) {
            assignBinaryExpression(ae);
        } else if (ae.getRight() instanceof CallExpression ce) {
            switch (((Identifier) ce.getCallee()).getName()) {
                case "getStringChar" -> getStringChar(ae);
                case "getRandomNr" -> getRandomNumber(ae);
                case "findInventory" -> findInventory(ae);
                case "findObject" -> findObject(ae);
                case "getDist" -> getDist(ae);
                case "getVerbEntryPoint" -> getVerbEntryPoint(ae);
                case "actorFromPos" -> actorFromPos(ae);
                case "getActorCostume" -> getActorCostume(ae);
                case "getActorElevation" -> getActorElevation(ae);
                case "getActorFacing" -> getActorFacing(ae);
                case "getActorMoving" -> getActorMoving(ae);
                case "getActorRoom" -> getActorRoom(ae);
                case "getActorWalkBox" -> getActorWalkBox(ae);
                case "getActorWidth" -> getActorWidth(ae);
                case "getActorX" -> getActorX(ae);
                case "getActorY" -> getActorY(ae);
                case "getInventoryCount" -> getInventoryCount(ae);
                case "getObjectOwner" -> getObjectOwner(ae);
                case "isScriptRunning" -> isScriptRunning(ae);
                case "isSoundRunning" -> isSoundRunning(ae);
                default -> throw new IllegalStateException("Not supported: " + ce);
            }
        } else {
            throw new IllegalStateException("Assignment not supported " + ae);
        }
    }

    private void assignBinaryExpression(final AssignmentExpression ae) {
        final int opcode = 0xAC;

        bsb.appendU8(opcode);
        appendRef(ae.getLeft(), 0, bsb);

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();

        appendExpression(ae.getRight(), paramBuilder);

        bsb.append(paramBuilder.build());
        bsb.appendU8(0xFF);
    }

    private void appendExpression(final Expression exp, final ByteStringBuilder paramBuilder) {
        if (exp instanceof BinaryExpression bexp) {
            appendExp(bexp.getLeft(), paramBuilder);
            appendExp(bexp.getRight(), paramBuilder);

            final int opOpcode = switch (bexp.getOperator()) {
                case "+" -> 2;
                case "-" -> 3;
                case "*" -> 4;
                case "/" -> 5;
                default -> throw new IllegalStateException("Invalid operator: " + bexp.getOperator());
            };
            paramBuilder.appendU8(opOpcode);
        } else if (exp instanceof EvalExpression ev) {
            appendExp(ev, paramBuilder);
        } else {
            throw new IllegalStateException("Invalid expression type: " + exp);
        }
    }

    private void appendExp(final Expression exp, final ByteStringBuilder paramBuilder) {
        if (exp instanceof BinaryExpression be) {
            appendExpression(be, paramBuilder);
        } else if (exp instanceof EvalExpression ee) {
            paramBuilder.appendU8(6);
            paramBuilder.append(new ScummCompiler().compileExpression(ExpressionStatement.of(ee.getExpression())));
        } else {
            appendNumber(paramBuilder, exp);
        }
    }

    private void appendNumber(final ByteStringBuilder paramBuilder, final Expression left) {
        int opcode = 1;
        final ByteStringBuilder innerBsb = new ByteStringBuilder();
        opcode |= append16(left, 0, innerBsb);
        paramBuilder.appendU8(opcode);
        paramBuilder.append(innerBsb.build());
    }

    private void assignNle(final AssignmentExpression ae) {
        final String operator = ae.getOperator();
        final Expression left = ae.getLeft();
        final Expression right = ae.getRight();

        if ("=".equals(operator)) {
            int opcode = 0x1A;
            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            append8(left, 0, paramBuilder);
            opcode |= append16(right, 0, paramBuilder);
            bsb.appendU8(opcode);
            bsb.append(paramBuilder.build());
//            bsb.appendU16(parseNumber(right));
        } else if ("+=".equals(operator)) {
            int opcode = 0x5A;

            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            opcode |= append16(right, 0, paramBuilder);

            bsb.appendU8(opcode);
            append16(left, 0, bsb);
            bsb.append(paramBuilder.build());
        } else if ("-=".equals(operator)) {
            int opcode = 0x3A;

            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            opcode |= append16(right, 0, paramBuilder);

            bsb.appendU8(opcode);
            append16(left, 0, bsb);
            bsb.append(paramBuilder.build());
        } else if ("*=".equals(operator)) {
            int opcode = 0x1B;

            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            opcode |= append16(right, 0, paramBuilder);

            bsb.appendU8(opcode);
            append16(left, 0, bsb);
            bsb.append(paramBuilder.build());
        } else if ("/=".equals(operator)) {
            int opcode = 0x5B;

            final ByteStringBuilder paramBuilder = new ByteStringBuilder();
            opcode |= append16(right, 0, paramBuilder);

            bsb.appendU8(opcode);
            append16(left, 0, bsb);
            bsb.append(paramBuilder.build());
        } else {
            throw new IllegalStateException("Unknown operator: " + operator);
        }
    }

    private void incDecExpression(final IncDecExpression exp) {
        final String operator = exp.getOperator();
        final Expression identifier = exp.getIdentifier();

        final int opcode = switch (operator) {
            case "++" -> 0x46;
            case "--" -> 0x46 | 0x80;
            default -> throw new IllegalStateException("Unknown operator: " + operator);
        };

        bsb.appendU8(opcode);
        append8(identifier, 0, bsb);
    }

    private void isSoundRunning(final AssignmentExpression arguments) {
        int opcode = 0x7C;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void isScriptRunning(final AssignmentExpression arguments) {
        int opcode = 0x68;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getObjectOwner(final AssignmentExpression arguments) {
        int opcode = 0x10;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getInventoryCount(final AssignmentExpression arguments) {
        int opcode = 0x31;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorY(final AssignmentExpression arguments) {
        int opcode = 0x23;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorX(final AssignmentExpression arguments) {
        int opcode = 0x43;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorWidth(final AssignmentExpression arguments) {
        int opcode = 0x6C;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorWalkBox(final AssignmentExpression arguments) {
        int opcode = 0x7B;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorRoom(final AssignmentExpression arguments) {
        int opcode = 0x03;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorMoving(final AssignmentExpression arguments) {
        int opcode = 0x56;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorFacing(final AssignmentExpression arguments) {
        int opcode = 0x63;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorElevation(final AssignmentExpression arguments) {
        int opcode = 0x06;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getActorCostume(final AssignmentExpression arguments) {
        int opcode = 0x71;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void actorFromPos(final AssignmentExpression arguments) {
        int opcode = 0x15;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getVerbEntryPoint(final AssignmentExpression arguments) {
        int opcode = 0x0B;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getDist(final AssignmentExpression arguments) {
        int opcode = 0x34;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        opcode |= append16(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void findObject(final AssignmentExpression arguments) {
        int opcode = 0x35;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void findInventory(final AssignmentExpression arguments) {
        int opcode = 0x3D;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void getRandomNumber(final AssignmentExpression arguments) {
        int opcode = 0x16;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        opcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);

        bsb.appendU8(opcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void saveVerbs(final List<Expression> arguments) {
        bsb.appendU8(0xAB);
        int subOpcode = 1;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        subOpcode |= appendParameter8(arguments, 0, parameterBuilder);
        subOpcode |= appendParameter8(arguments, 1, parameterBuilder);
        subOpcode |= appendParameter8(arguments, 2, parameterBuilder);

        bsb.appendU8(subOpcode);
        bsb.append(parameterBuilder.build());
    }

    private void restoreVerbs(final List<Expression> arguments) {
        bsb.appendU8(0xAB);
        int subOpcode = 2;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        subOpcode |= appendParameter8(arguments, 0, parameterBuilder);
        subOpcode |= appendParameter8(arguments, 1, parameterBuilder);
        subOpcode |= appendParameter8(arguments, 2, parameterBuilder);

        bsb.appendU8(subOpcode);
        bsb.append(parameterBuilder.build());
    }

    private void setVarRange(final List<Expression> arguments) {
        int opcode = 0x26;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        append16(arguments.get(0), 0, paramBuilder);

        final CallExpression callExpression = (CallExpression) arguments.get(2);
        final Identifier identifier = (Identifier) callExpression.getCallee();
        Assert.assertEqual(identifier, Identifier.of("listOf"));

        boolean isU16Required = false;
        for (final Expression argument : callExpression.getArguments()) {
            final NumericLiteralExpression nle = (NumericLiteralExpression) argument;
            final int number = nle.getNumber();
            if (number > 255) {
                opcode |= 0x80;
                isU16Required = true;
                break;
            }
        }

        final ByteStringBuilder listParamBuilder = new ByteStringBuilder();
        for (final Expression argument : callExpression.getArguments()) {
            final NumericLiteralExpression nle = (NumericLiteralExpression) argument;
            if (isU16Required) {
                listParamBuilder.appendU16(nle.getNumber());
            } else {
                listParamBuilder.appendU8(nle.getNumber());
            }
        }

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        bsb.appendU8(parseNumber(arguments.get(1)));
        bsb.append(listParamBuilder.build());
    }

    private void loadRoomWithEgo(final List<Expression> arguments) {
        int opcode = 0x24;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        opcode |= appendParameter8(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        bsb.appendU16(parseNumber(arguments.get(2)));
        bsb.appendU16(parseNumber(arguments.get(3)));
    }

    private void startObject(final List<Expression> arguments) {
        int opcode = 0x37;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        opcode |= appendParameter8(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        writeList16((CallExpression) arguments.get(2));
    }

    private void walkActorTo(final List<Expression> arguments) {
        int opcode = 0x1E;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        opcode |= appendParameter16(arguments, 2, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void lights(final List<Expression> arguments) {
        int opcode = 0x70;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append8(arguments.get(0), 0, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        bsb.appendU8(((NumericLiteralExpression) arguments.get(1)).getNumber());
        bsb.appendU8(((NumericLiteralExpression) arguments.get(2)).getNumber());
    }

    private void walkActorToActor(final List<Expression> arguments) {
        int opcode = 0x0D;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter8(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        bsb.appendU8(((NumericLiteralExpression) arguments.get(2)).getNumber());
    }

    private void drawBox(final List<Expression> arguments) {
        int opcode = 0x3F;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= append16(arguments.get(0), 0, paramBuilder);
        opcode |= append16(arguments.get(1), 1, paramBuilder);

        int auxopcode = 0x05;
        final ByteStringBuilder auxParamBuilder = new ByteStringBuilder();
        auxopcode |= append16(arguments.get(2), 0, auxParamBuilder);
        auxopcode |= append16(arguments.get(3), 1, auxParamBuilder);
        auxopcode |= append8(arguments.get(4), 2, auxParamBuilder);

        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());

        bsb.appendU8(auxopcode);
        bsb.append(auxParamBuilder.build());
    }

    private void drawObject(final List<Expression> arguments) {
        int opcode = 0x05;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        opcode |= appendParameter16(arguments, 2, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void putActor(final List<Expression> arguments) {
        int opcode = 0x01;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        opcode |= appendParameter16(arguments, 2, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void setObjectName(final List<Expression> arguments) {
        int opcode = 0x54;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        bsb.append(ScummStringEncoder.encode(((StringLiteralExpression) arguments.get(1)).getValue(), false));
    }

    private void chainScript(final List<Expression> arguments) {
        int opcode = 0x42;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
        writeList16((CallExpression) arguments.get(1));
    }

    private void putActorAtObject(final List<Expression> arguments) {
        int opcode = 0x0E;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void walkActorToObject(final List<Expression> arguments) {
        int opcode = 0x36;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void setOwnerOf(final List<Expression> arguments) {
        int opcode = 0x29;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        opcode |= appendParameter8(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void putActorInRoom(final List<Expression> arguments) {
        appendParameters8(arguments, 0x2D);
    }

    private void setState(final List<Expression> arguments) {
        int opcode = 0x07;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter16(arguments, 0, paramBuilder);
        opcode |= appendParameter8(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void faceActor(final List<Expression> arguments) {
        int opcode = 0x09;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        opcode |= appendParameter8(arguments, 0, paramBuilder);
        opcode |= appendParameter16(arguments, 1, paramBuilder);
        bsb.appendU8(opcode);
        bsb.append(paramBuilder.build());
    }

    private void animateCostume(final List<Expression> arguments) {
        appendParameters8(arguments, 0x11);
    }

    private void putCodeInString(final List<Expression> arguments) {
        bsb.appendU8(0x27);
        int subOpcode = 1;
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        subOpcode |= appendParameter8(arguments, 0, paramBuilder);

        bsb.appendU8(subOpcode);
        bsb.append(paramBuilder.build());

        final StringLiteralExpression str = (StringLiteralExpression) arguments.get(1);
        bsb.append(ScummStringEncoder.encode(str.getValue(), false));
    }

    private void getStringChar(final AssignmentExpression arguments) {
        bsb.appendU8(0x27);
        int subOpcode = 0x04;

        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        subOpcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(0), 0, parameterBuilder);
        subOpcode |= append8(((CallExpression) arguments.getRight()).getArguments().get(1), 1, parameterBuilder);

        bsb.appendU8(subOpcode);
        append16(arguments.getLeft(), 0, bsb);
        bsb.append(parameterBuilder.build());
    }

    private void copyString(final List<Expression> arguments) {
        bsb.appendU8(0x27);
        final int subOpcode = 2;
        appendParameters8(arguments, subOpcode);
    }

    private void setStringChar(final List<Expression> arguments) {
        bsb.appendU8(0x27);
        final int subOpcode = 3;
        appendParameters8(arguments, subOpcode);
    }

    private void createString(final List<Expression> arguments) {
        bsb.appendU8(0x27);
        final int subOpcode = 5;
        appendParameters8(arguments, subOpcode);
    }

    private void pseudoRoom(final List<Expression> arguments) {
        bsb.appendU8(0xCC);
        final int roomId = ((NumericLiteralExpression) arguments.get(0)).getNumber();
        bsb.appendU8(roomId);

        final CallExpression callExpression = (CallExpression) arguments.get(1);
        final Identifier identifier = (Identifier) callExpression.getCallee();
        Assert.assertEqual(identifier, Identifier.of("listOf"));
        for (final Expression argument : callExpression.getArguments()) {
            final NumericLiteralExpression nle = (NumericLiteralExpression) argument;
            bsb.appendU8(0x80 | nle.getNumber());
        }

        bsb.appendU8(0);
    }

    private void setClass(final List<Expression> arguments) {
        final int subOpcode = 0x5D;
        final ByteStringBuilder parameterBuilder = new ByteStringBuilder();
        final int param1Modifier = appendParameter16(arguments, 0, parameterBuilder);
        bsb.appendU8(subOpcode | param1Modifier);
        bsb.append(parameterBuilder.build());
        writeList16((CallExpression) arguments.get(1));
    }

    private void roomScroll(final List<Expression> arguments) {
        bsb.appendU8(0x33);
        appendParameters16(arguments, 1);
    }

    private void roomColor(final List<Expression> arguments) {
        bsb.appendU8(0x33);
        appendParameters16(arguments, 2);
    }

    private void setScreen(final List<Expression> arguments) {
        bsb.appendU8(0x33);
        appendParameters16(arguments, 3);
    }

    private void setPalColor(final List<Expression> arguments) {
        bsb.appendU8(0x33);
        appendParameters16(arguments, 4);
    }

    private void shake(final List<Expression> arguments) {
        bsb.appendU8(0x33);
        if (arguments.get(0).equals(BooleanLiteralExpression.of(true))) {
            bsb.appendU8(0x05);
        } else if (arguments.get(0).equals(BooleanLiteralExpression.of(false))) {
            bsb.appendU8(0x06);
        } else {
            throw new IllegalStateException("Illegal type: " + arguments.get(0));
        }
    }

    private void oldRoomEffectSet(final List<Expression> arguments) {
        bsb.appendU8(0x5C);
        bsb.appendU8(0x03);
        bsb.appendU16(parseNumber(arguments.get(0)));
    }

    private int parseNumber(final Expression expression) {
        if (expression instanceof NumericLiteralExpression nle) {
            return nle.getNumber();
        } else if (expression instanceof UnaryExpression ue) {
            if (!"-".equals(ue.getOperator())) {
                throw new IllegalStateException("Wrong operator: " + ue.getOperator());
            }
            if (ue.getExpression() instanceof NumericLiteralExpression nle) {
                return -nle.getNumber();
            }
            throw new IllegalStateException("Wrong expression: " + ue.getExpression());
        } else {
            throw new IllegalStateException("Wrong type: " + expression);
        }
    }

    private void delayVariable(final List<Expression> arguments) {
        bsb.appendU8(0x2B);

        final ByteStringBuilder paramBuilder = new ByteStringBuilder();
        append8(arguments.get(0), 0, paramBuilder);

        bsb.append(paramBuilder.build());
    }

    private void pickupObject(final List<Expression> arguments) {
        appendParameters16(arguments, 0x50);
    }

    private void stopScript(final List<Expression> arguments) {
        appendParameters8(arguments, 0x62);
    }

    private void loadRoom(final List<Expression> arguments) {
        appendParameters8(arguments, 0x72);
    }

    private void cursor(final List<Expression> arguments) {
        bsb.appendU8(0x2C);
        final Identifier arg = (Identifier) arguments.get(0);
        if (Identifier.of("ON").equals(arg)) {
            bsb.appendU8(0x01);
        } else if (Identifier.of("OFF").equals(arg)) {
            bsb.appendU8(0x02);
        } else if (Identifier.of("SOFT_ON").equals(arg)) {
            bsb.appendU8(0x05);
        } else if (Identifier.of("SOFT_OFF").equals(arg)) {
            bsb.appendU8(0x06);
        } else {
            throw new IllegalStateException();
        }
    }

    private void userput(final List<Expression> arguments) {
        bsb.appendU8(0x2C);
        final Identifier arg = (Identifier) arguments.get(0);
        if (Identifier.of("ON").equals(arg)) {
            bsb.appendU8(0x03);
        } else if (Identifier.of("OFF").equals(arg)) {
            bsb.appendU8(0x04);
        } else if (Identifier.of("SOFT_ON").equals(arg)) {
            bsb.appendU8(0x07);
        } else if (Identifier.of("SOFT_OFF").equals(arg)) {
            bsb.appendU8(0x08);
        } else {
            throw new IllegalStateException();
        }
    }

    private void initCharset(final List<Expression> arguments) {
        bsb.appendU8(0x2C);
        appendParameters8(arguments, 0x0D);
    }

    private void freezeScripts(final List<Expression> arguments) {
        appendParameters8(arguments, 0x60);
    }

    private void debug(final List<Expression> arguments) {
        appendParameters16(arguments, 0x6B);
    }

    private void panCameraTo(final List<Expression> arguments) {
        appendParameters16(arguments, 0x12);
    }

    private void setCameraAt(final List<Expression> arguments) {
        appendParameters16(arguments, 0x32);
    }

    private void actorFollowCamera(final List<Expression> arguments) {
        appendParameters8(arguments, 0x52);
    }

    private void resourceLoadScript(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 1);
    }

    private void resourceLoadSound(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 2);
    }

    private void resourceLoadCostume(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 3);
    }

    private void resourceLoadRoom(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 4);
    }

    private void resourceNukeCostume(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 7);
    }

    private void resourceNukeRoom(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 8);
    }

    private void resourceLockScript(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 9);
    }

    private void resourceLockSound(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 10);
    }

    private void resourceLockCostume(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 11);
    }

    private void resourceLockRoom(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 12);
    }

    private void resourceUnlockScript(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 13);
    }

    private void resourceUnlockSound(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 14);
    }

    private void resourceUnlockCostume(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 15);
    }

    private void resourceUnlockRoom(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 16);
    }

    private void resourceLoadCharset(final List<Expression> arguments) {
        bsb.appendU8(0x0C);
        appendParameters8(arguments, 18);
    }

    private void resourceClearHeap() {
        bsb.appendU8(0x0C);
        bsb.appendU8(0x11);
    }

    private void createBoxMatrix() {
        bsb.appendU8(0x30);
        bsb.appendU8(0x04);
    }

    private void setBoxFlags(final List<Expression> arguments) {
        bsb.appendU8(0x30);
        final int subOpcode = 1;

        appendParameters8(arguments, subOpcode);
    }

    private void stopSound(final List<Expression> arguments) {
        appendParameters8(arguments, 0x3C);
    }

    private void stopLocalGlobalScript() {
        bsb.appendU8(0xA0);
    }

    private void stopObjectCode() {
        bsb.appendU8(0x00);
    }

    private void cutScene(final List<Expression> arguments) {
        bsb.appendU8(0x40);
        writeList16((CallExpression) arguments.get(0));
    }

    // FIXME refactor (appendParameter & ScummStringEncoder)
    protected void writeList16(final CallExpression listOfExpression) {
        if (listOfExpression.getArguments() != null) {
            for (final Expression argument : listOfExpression.getArguments()) {
                final ByteStringBuilder innerBsb = new ByteStringBuilder();
                int mode = 1;
                mode |= append16(argument, 0, innerBsb);
                bsb.appendU8(mode);
                bsb.append(innerBsb.build());
            }
        }
        bsb.appendU8(0xFF);
    }

    private int append16(final Expression argument, final int j, final ByteStringBuilder bsb1) {
        if (argument instanceof NumericLiteralExpression || argument instanceof UnaryExpression) {
            bsb1.appendU16(parseNumber(argument));
            return 0;
        }

        return appendRef(argument, j, bsb1);
    }

    private int appendRef(final Expression argument, final int j, final ByteStringBuilder bsb1) {
        if (argument instanceof MemberExpression me) {
            final Identifier property = (Identifier) me.getObject();
            final VarType varType = VarType.valueOf(property.getName().toUpperCase(Locale.ROOT));
            if (me.getProperty() instanceof NumericLiteralExpression nle) {
                bsb1.appendU16(varType.to(nle.getNumber()));
            } else if (me.getProperty() instanceof BinaryExpression be) {
                if (!"+".equals(be.getOperator())) {
                    throw new IllegalStateException("Wrong operator: " + be.getOperator());
                }

                bsb1.appendU16(0x2000 | varType.to(((NumericLiteralExpression) be.getLeft()).getNumber()));
                final Expression right = be.getRight();

                if (right instanceof NumericLiteralExpression) {
                    bsb1.appendU16(parseVariable(right));
                } else if (right instanceof MemberExpression me2) {
                    final VarType vt =
                        VarType.valueOf(((Identifier) me2.getObject()).getName().toUpperCase(Locale.ROOT));
                    bsb1.appendU16(0x2000 | vt.to((((NumericLiteralExpression) me2.getProperty())).getNumber()));
                }
            } else {
                throw new IllegalStateException("Unknown type: " + me.getProperty());
            }
            return 0x80 >> j;
        } else if (argument instanceof Identifier i) {
            final int scummVar = ScummVars.resolve(i.getName())
                .orElseThrow();

            bsb1.appendU16(VarType.VAR.to(scummVar));
            return 0x80 >> j;
        } else {
            throw new IllegalStateException("Illegal type: " + argument);
        }
    }

    private int parseVariable(final Expression exp) {
        if (exp instanceof NumericLiteralExpression nle) {
            return nle.getNumber();
        } else if (exp instanceof MemberExpression me) {
            final Identifier property = (Identifier) me.getObject();
            final VarType varType = VarType.valueOf(property.getName().toUpperCase(Locale.ROOT));
            if (me.getProperty() instanceof NumericLiteralExpression nle) {
                return varType.to(nle.getNumber());
            }
        }

        throw new IllegalStateException("Unsupported exp: " + exp);
    }

    private int append8(final Expression argument, final int j, final ByteStringBuilder bsb1) {
        if (argument instanceof NumericLiteralExpression nle) {
            bsb1.appendU8(nle.getNumber());
            return 0;
        }

        return appendRef(argument, j, bsb1);
    }

    private void beginOverride() {
        overrideOpcode(1);
    }

    private void endOverride() {
        overrideOpcode(0);
    }

    private void overrideOpcode(final int mode) {
        bsb.appendU8(0x58);
        bsb.appendU8(mode);
    }

    private void delay(final List<Expression> arguments) {
        final int number = ((NumericLiteralExpression) arguments.get(0)).getNumber();
        bsb.appendU8(0x2E);
        bsb.appendU8(0xFF & number);
        bsb.appendU8(0xFF & number >> 8);
        bsb.appendU8(0xFF & number >> 16);
    }

    private void startSound(final List<Expression> arguments) {
        appendParameters8(arguments, 0x1C);
    }

    private void breakHere() {
        bsb.appendU8(0x80);
    }

    private void waitForActor(final List<Expression> arguments) {
        bsb.appendU8(0xAE);
        appendParameters8(arguments, 0x01);
    }

    private void appendParameters8(final List<Expression> arguments, final int initialSubOpcode) {
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();

        int subOpcode = initialSubOpcode;
        for (int j = 0; j < arguments.size(); j++) {
            subOpcode |= appendParameter8(arguments, j, paramBuilder);
        }

        bsb.appendU8(subOpcode);
        bsb.append(paramBuilder.build());
    }

    private int appendParameter8(final List<Expression> arguments, final int j, final ByteStringBuilder paramBuilder) {
        return append8(arguments.get(j), j, paramBuilder);
    }

    private void appendParameters16(final List<Expression> arguments, final int initialSubOpcode) {
        final ByteStringBuilder paramBuilder = new ByteStringBuilder();

        int subOpcode = initialSubOpcode;
        for (int j = 0; j < arguments.size(); j++) {
            subOpcode |= appendParameter16(arguments, j, paramBuilder);
        }

        bsb.appendU8(subOpcode);
        bsb.append(paramBuilder.build());
    }

    private int appendParameter16(final List<Expression> arguments, final int j, final ByteStringBuilder paramBuilder) {
        return append16(arguments.get(j), j, paramBuilder);
    }

    private void waitForMessage() {
        bsb.appendU8(0xAE);
        bsb.appendU8(0x02);
    }

    private void waitForCamera() {
        bsb.appendU8(0xAE);
        bsb.appendU8(0x03);
    }

    private void waitForSentence() {
        bsb.appendU8(0xAE);
        bsb.appendU8(0x04);
    }

    private void endCutscene() {
        bsb.appendU8(0xC0);
    }

    private void systemQuit() {
        bsb.appendU8(0x98);
        bsb.appendU8(0x03);
    }

}
