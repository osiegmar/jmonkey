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

package de.siegmar.jmonkey.decoder.script;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import de.siegmar.jmonkey.decoder.script.operator.AssignmentOperator;
import de.siegmar.jmonkey.decoder.script.operator.ComparisonOperator;
import de.siegmar.jmonkey.decoder.script.operator.UnaryOperator;
import de.siegmar.jmonkey.decoder.script.parameter.CompoundParameter;
import de.siegmar.jmonkey.decoder.script.parameter.ConstantParameter;
import de.siegmar.jmonkey.decoder.script.parameter.NamedVariable;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;
import de.siegmar.jmonkey.decoder.script.parameter.StringParameter;

@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:MethodCount"})
public class ScummVMPrintOpcodeDelegate implements OpcodeDelegate {

    private Script mainScript;
    private Writer writer;
    private Writer tmpWriter;
    private final HexFormat hexFormat = HexFormat.of().withUpperCase();

    public ScummVMPrintOpcodeDelegate(final Writer writer) {
        this.writer = writer;
    }

    @Override
    public void setScript(final Script script) {
        this.mainScript = script;
    }

    @Override
    public Script getScript() {
        return mainScript;
    }

    @Override
    public void stopLocalGlobalScript() {
        stopObjectCode();
    }

    @Override
    public void stopObjectCode() {
        printM("stopObjectCode");
    }

    @Override
    public void actorFollowCamera(final OpParameter actor) {
        printM("actorFollowCamera", actor);
    }

    @Override
    public void actorFromPos(final OpParameter result, final OpParameter x, final OpParameter y) {
        assign(result, m("actorFromPos", x, y));
    }

    private void assign(final OpParameter result, final OpParameter value) {
        print("%s = %s", result, value);
    }

    @Override
    public void setClass(final OpParameter obj, final List<OpParameter> classes) {
        printM("setClass", obj, classes);
    }

    @Override
    public void animateCostume(final OpParameter costume, final OpParameter animation) {
        printM("animateCostume", costume, animation);
    }

    @Override
    public void chainScript(final OpParameter script, final List<OpParameter> args) {
        printM("chainScript", script, args);
    }

    @Override
    public void initCharset(final OpParameter charset) {
        printM("InitCharset", charset);
    }

    @Override
    public void cutscene(final List<OpParameter> args) {
        printM("cutscene", args);
    }

    @Override
    public void debug(final OpParameter param) {
        printM("debug", param);
    }

    @Override
    public void drawBox(final OpParameter left, final OpParameter top,
                        final OpParameter right, final OpParameter bottom,
                        final OpParameter color) {
        printM("drawBox", left, top, right, bottom, color);
    }

    @Override
    public void drawObject(final OpParameter object, final OpParameter xpos, final OpParameter ypos) {
        printM("drawObject", object, xpos, ypos);
    }

    @Override
    public void endCutscene() {
        printM("endCutscene");
    }

    @Override
    public void faceActor(final OpParameter actor, final OpParameter object) {
        printM("faceActor", actor, object);
    }

    @Override
    public void findInventory(final OpParameter result, final OpParameter owner, final OpParameter index) {
        assign(result, m("findInventory", owner, index));
    }

    @Override
    public void findObject(final OpParameter result, final OpParameter x, final OpParameter y) {
        assign(result, m("findObject", x, y));
    }

    @Override
    public void freezeScripts(final OpParameter flag) {
        printM("freezeScripts", flag);
    }

    @Override
    public void getActorCostume(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorCostume", actor));
    }

    @Override
    public void getActorElevation(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorElevation", actor));
    }

    @Override
    public void getActorFacing(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorFacing", actor));
    }

    @Override
    public void getActorMoving(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorMoving", actor));
    }

    @Override
    public void getActorRoom(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorRoom", actor));
    }

    @Override
    public void getActorWalkBox(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorWalkBox", actor));
    }

    @Override
    public void getActorWidth(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorWidth", actor));
    }

    @Override
    public void getActorX(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorX", actor));
    }

    @Override
    public void getActorY(final OpParameter result, final OpParameter actor) {
        assign(result, m("getActorY", actor));
    }

    @Override
    public void getDist(final OpParameter result, final OpParameter o1, final OpParameter o2) {
        assign(result, m("getDist", o1, o2));
    }

    @Override
    public void getInventoryCount(final OpParameter result, final OpParameter actor) {
        assign(result, m("getInventoryCount", actor));
    }

    @Override
    public void getObjectOwner(final OpParameter result, final OpParameter object) {
        assign(result, m("getObjectOwner", object));
    }

    @Override
    public void getRandomNr(final OpParameter result, final OpParameter bound) {
        assign(result, m("getRandomNr", bound));
    }

    @Override
    public void getVerbEntryPoint(final OpParameter result, final OpParameter object, final OpParameter verb) {
        assign(result, m("getVerbEntryPoint", object, verb));
    }

    @Override
    public void isScriptRunning(final OpParameter result, final OpParameter script) {
        assign(result, m("isScriptRunning", script));
    }

    @Override
    public void isSoundRunning(final OpParameter result, final OpParameter sound) {
        assign(result, m("isSoundRunning", sound));
    }

    @Override
    public void lights(final OpParameter arg1, final int arg2, final int arg3) {
        printM("lights", arg1, arg2, arg3);
    }

    @Override
    public void loadRoom(final OpParameter room) {
        printM("loadRoom", room);
    }

    @Override
    public void loadRoomWithEgo(final OpParameter object, final OpParameter room, final int x, final int y) {
        printM("loadRoomWithEgo", object, room, x, y);
    }

    @Override
    public void createBoxMatrix() {
        printM("createBoxMatrix");
    }

    @Override
    public void setBoxFlags(final OpParameter box, final OpParameter val) {
        printM("setBoxFlags", box, val);
    }

    @Override
    public void oldRoomEffectSet(final int x) {
        printM("oldRoomEffect-set", x);
    }

    @Override
    public void walkActorTo(final OpParameter actor, final OpParameter x, final OpParameter y) {
        printM("walkActorTo", actor, x, y);
    }

    @Override
    public void walkActorToObject(final OpParameter actor, final OpParameter object) {
        printM("walkActorToObject", actor, object);
    }

    @Override
    public void walkActorToActor(final OpParameter walker, final OpParameter walkee, final int distance) {
        printM("walkActorToActor", walker, walkee, distance);
    }

    @Override
    public void waitForActor(final OpParameter actor) {
        printM("WaitForActor", actor);
    }

    @Override
    public void waitForMessage() {
        printM("WaitForMessage");
    }

    @Override
    public void waitForCamera() {
        printM("WaitForCamera");
    }

    @Override
    public void waitForSentence() {
        printM("WaitForSentence");
    }

    @Override
    public void quit() {
        printM("systemOps", 3);
    }

    @Override
    public void stopSound(final OpParameter sound) {
        printM("stopSound", sound);
    }

    @Override
    public void startScript(final OpParameter script, final List<OpParameter> args,
                            final boolean freezeResistant, final boolean recursive) {
        final StringBuilder sb = new StringBuilder(64)
            .append("startScript(")
            .append(script).append(',').append('[').append(args.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")))
            .append(']');

        if (freezeResistant) {
            sb.append(",F");
        }
        if (recursive) {
            sb.append(",R");
        }
        sb.append(')');

        print(sb.toString());
    }

    @Override
    public void stopScript(final OpParameter script) {
        printM("stopScript", script);
    }

    @Override
    public void startSound(final OpParameter sound) {
        printM("startSound", sound);
    }

    @Override
    public void startObject(final OpParameter obj, final OpParameter script, final List<OpParameter> args) {
        printM("startObject", obj, script, args);
    }

    @Override
    public void setState(final OpParameter object, final OpParameter state) {
        printM("setState", object, state);
    }

    @Override
    public void setOwnerOf(final OpParameter object, final OpParameter owner) {
        printM("setOwnerOf", object, owner);
    }

    @Override
    public void setObjectName(final OpParameter obj, final ScummString name) {
        printM("setObjectName", obj, decodeScummString(name));
    }

    @Override
    public void setCameraAt(final OpParameter x) {
        printM("setCameraAt", x);
    }

    @Override
    public void putActor(final OpParameter actor, final OpParameter x, final OpParameter y) {
        printM("putActor", actor, x, y);
    }

    @Override
    public void putActorInRoom(final OpParameter actor, final OpParameter room) {
        printM("putActorInRoom", actor, room);
    }

    @Override
    public void putActorAtObject(final OpParameter actor, final OpParameter object) {
        printM("putActorAtObject", actor, object);
    }

    @Override
    public void pickupObject(final OpParameter object) {
        printM("pickupObject", object);
    }

    @Override
    public void panCameraTo(final OpParameter x) {
        printM("panCameraTo", x);
    }

    @Override
    public void beginOverride() {
        printM("beginOverride");
    }

    @Override
    public void endOverride() {
        printM("endOverride");
    }

    @Override
    public void startExpression() {
        tmpWriter = writer;
        writer = new StringWriter();
    }

    @SuppressWarnings("PMD.NullAssignment")
    @Override
    public String endExpression() {
        final String res = writer.toString();
        writer = tmpWriter;
        tmpWriter = null;
        return res;
    }

    @Override
    public void saveVerbs(final OpParameter start, final OpParameter end, final OpParameter mode) {
        printM("saveVerbs", start, end, mode);
    }

    @Override
    public void restoreVerbs(final OpParameter start, final OpParameter end, final OpParameter mode) {
        printM("restoreVerbs", start, end, mode);
    }

    @Override
    public void setVarRange(final OpParameter result, final int number, final List<Integer> varlist) {
        printM("setVarRange", result, number, varlist);
    }

    @Override
    public void doSentence(final OpParameter verb, final OpParameter obj1, final OpParameter obj2) {
        printM("doSentence", verb, obj1, obj2);
    }

    @Override
    public void doSentenceStop() {
        print("doSentence(STOP)");
    }

    @Override
    public void roomColor(final OpParameter color, final OpParameter palIndex) {
        printM("RoomColor", color, palIndex);
    }

    @Override
    public void shake(final boolean on) {
        printM(on ? "ShakeOn" : "ShakeOff");
    }

    @Override
    public void roomScroll(final OpParameter minX, final OpParameter maxX) {
        printM("RoomScroll", minX, maxX);
    }

    @Override
    public void setScreen(final OpParameter width, final OpParameter height) {
        printM("SetScreen", width, height);
    }

    @Override
    public void setPalColor(final OpParameter color, final OpParameter palIndex) {
        printM("SetPalColor", color, palIndex);
    }

    @Override
    public void pseudoRoom(final int room, final List<Integer> aliases) {
        final Object[] args = new Object[aliases.size() + 1];
        args[0] = room;
        for (int i = 0; i < aliases.size(); i++) {
            args[i + 1] = aliases.get(i);
        }

        printM("PseudoRoom", args);
    }

    @Override
    public ResourceManager resource() {
        return new PrintResourceManager();
    }

    @Override
    public void createString(final OpParameter stringId, final OpParameter size) {
        printM("CreateString", stringId, size);
    }

    @Override
    public void getStringChar(final OpParameter result, final OpParameter stringId, final OpParameter index) {
        assign(result, m("GetStringChar", stringId, index));
    }

    @Override
    public void setStringChar(final OpParameter stringId, final OpParameter index, final OpParameter ch) {
        printM("SetStringChar", stringId, index, ch);
    }

    @Override
    public void copyString(final OpParameter destId, final OpParameter srcId) {
        printM("CopyString", destId, srcId);
    }

    @Override
    public void putCodeInString(final OpParameter stringId, final ScummString str) {
        printM("PutCodeInString", stringId, decodeScummString(str));
    }

    @Override
    public void mutateVariable(final OpParameter var, final UnaryOperator operator) {
        print("%s%s", var, operator);
    }

    @Override
    public void mutateVariable(final OpParameter var, final AssignmentOperator operator, final OpParameter operand) {
        print("%s %s %s", var, operator, operand);
    }

    @Override
    public ActorOps actorOps(final OpParameter actor) {
        return new ActorOpsImp(actor);
    }

    @Override
    public VerbOps verbOps(final OpParameter verb) {
        return new VerbOpsImpl(verb);
    }

    @Override
    public PrintOps printEgo() {
        return new PrintOpsImpl();
    }

    @Override
    public PrintOps print(final OpParameter ego) {
        return new PrintOpsImpl(ego);
    }

    private void print(final String text, final Object... args) {
        try {
            writer.write(text.formatted(args));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exprMode(final OpParameter result, final Object pop) {
        final StringBuilder sb = new StringBuilder();
        sb.append(operandToStr(pop));
        print("Exprmode %s = %s", result, sb);
    }

    @Override
    public void cursor(final InputMode mode) {
        final String name = switch (mode) {
            case ON -> "CursorShow";
            case OFF -> "CursorHide";
            case SOFT_ON -> "CursorSoftOn";
            case SOFT_OFF -> "CursorSoftOff";
        };
        printM(name);
    }

    @Override
    public void userput(final InputMode mode) {
        final String name = switch (mode) {
            case ON -> "UserputOn";
            case OFF -> "UserputOff";
            case SOFT_ON -> "UserputSoftOn";
            case SOFT_OFF -> "UserputSoftOff";
        };
        printM(name);
    }

    @Override
    public void breakHere() {
        printM("breakHere");
    }

    @Override
    public void delay(final int delay) {
        printM("delay", delay);
    }

    @Override
    public void delayVariable(final OpParameter var) {
        printM("delayVariable", var);
    }

    @Override
    public Object callOpcode() {
        return mainScript.callOpcode();
    }

    @Override
    public void beforeOpcode(final int pos, final int opcode) {
        print("[%04X] (%02X) ".formatted(pos, opcode));
    }

    @Override
    public void afterOpcode() {
        print(";\n");
    }

    @Override
    public void endScript() {
        print("END\n");
    }

    @Override
    public void gotoOffset(final int offset) {
        print("goto %04X", offset);
    }

    @Override
    public void stateConditionJump(final OpParameter var, final ComparisonOperator op, final OpParameter cmp,
                                   final int offset) {
        print("unless (getState(%s) %s %s) goto %04X", var, op, cmp, offset);
    }

    @Override
    public void conditionalJump(final OpParameter var, final ComparisonOperator operator, final OpParameter val,
                                final int offset) {
        print("unless (%s %s %s) goto %04X", var, operator, val, offset);
    }

    @Override
    public void conditionalJump(final OpParameter var, final ComparisonOperator operator, final int offset) {
        if (operator == ComparisonOperator.EQUAL) {
            print("unless (!%s) goto %04X", var, offset);
        } else {
            print("unless (%s) goto %04X", var, offset);
        }
    }

    @Override
    public void classOfIsConditionJump(final OpParameter value, final List<OpParameter> args, final int offset) {
        print("unless (classOfIs(%s,%s)) goto %04X", value, args, offset);
    }

    private static Object operandToStr(final Object o) {
        if (o instanceof Calculation c) {
            return "(%s %s %s)".formatted(
                operandToStr(c.operand2()),
                c.operator(),
                operandToStr(c.operand1()));
        }
        if (o instanceof ExpressionResult r) {
            return "<" + r.result() + ">";
        }
        if (o instanceof ConstantParameter
            || o instanceof CompoundParameter
            || o instanceof NamedVariable) {
            return o.toString();
        }

        throw new IllegalStateException();
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private String decodeScummString(final ScummString ss) {
        final List<String> parts = new ArrayList<>();

        for (final ScummString.StringPart part : ss.getParts()) {
            if (part instanceof ScummString.NewLinePart) {
                parts.add(sprintM("newline"));
            } else if (part instanceof ScummString.NewLinePart2) {
                parts.add(sprintM("newline"));
            } else if (part instanceof ScummString.VerbNewLinePart) {
                parts.add(sprintM("verbNewline"));
            } else if (part instanceof ScummString.KeepTextPart) {
                parts.add(sprintM("keepText"));
            } else if (part instanceof ScummString.SleepPart) {
                parts.add(sprintM("wait"));
            } else if (part instanceof ScummString.IntPart p) {
                parts.add(sprintM("getInt", p.getParam()));
            } else if (part instanceof ScummString.VerbPart p) {
                parts.add(sprintM("getVerb", p.getParam()));
            } else if (part instanceof ScummString.NamePart p) {
                parts.add(sprintM("getName", p.getParam()));
            } else if (part instanceof ScummString.GetStringPart p) {
                parts.add(sprintM("getString", p.getParam()));
            } else if (part instanceof ScummString.TextPart p) {
                parts.add('"' + escapeStr(p.getRawString()) + '"');
            } else {
                throw new IllegalStateException("Unknown: " + part.getClass());
            }
        }

        return parts.stream()
            .map(Object::toString)
            .collect(Collectors.joining(" + "));
    }

    private String escapeStr(final byte[] text) {
        final StringBuilder sb = new StringBuilder();

        for (final byte b : text) {
            final char ch = (char) b;

            if (ch < 128 && ch > 31) {
                if (ch == '\\' || ch == '"') {
                    sb.append('\\');
                }
                sb.append(ch);
            } else {
                sb.append("\\x").append(hexFormat.toHexDigits((byte) ch));
            }
        }

        return sb.toString();
    }

    private StringParameter m(final String name, final Object... arg) {
        return new MethodBuilder(name).arg(arg).build();
    }

    public void printM(final String name, final Object... arg) {
        try {
            writer.append(sprintM(name, arg));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String sprintM(final String name, final Object... arg) {
        return new MethodBuilder(name).arg(arg).toString();
    }

    private static class MethodBuilder {

        private final String name;
        private final List<String> args = new ArrayList<>();
        private String argSeparator = ",";

        MethodBuilder(final String name) {
            this.name = name;
        }

        public void setArgSeparator(final String argSeparator) {
            this.argSeparator = argSeparator;
        }

        public MethodBuilder arg(final Object... arg) {
            for (final Object o : arg) {
                if (o instanceof List<?> l) {
                    final String collect = l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(argSeparator));
                    args.add("[%s]".formatted(collect));
                } else {
                    args.add(o.toString());
                }
            }
            return this;
        }

        public StringParameter build() {
            return new StringParameter(toString());
        }

        @Override
        public String toString() {
            final String argsStr = args.stream()
                .map(Object::toString)
                .collect(Collectors.joining(argSeparator));
            return "%s(%s)".formatted(name, argsStr);
        }

    }

    private class PrintResourceManager implements ResourceManager {

        PrintResourceManager() {
            print("Resource");
        }

        @Override
        public void clearHeap() {
            printM(".clearHeap");
        }

        @Override
        public void loadScript(final OpParameter resId) {
            printM(".loadScript", resId);
        }

        @Override
        public void loadSound(final OpParameter resId) {
            printM(".loadSound", resId);
        }

        @Override
        public void loadCostume(final OpParameter resId) {
            printM(".loadCostume", resId);
        }

        @Override
        public void loadRoom(final OpParameter resId) {
            printM(".loadRoom", resId);
        }

        @Override
        public void lockScript(final OpParameter resId) {
            printM(".lockScript", resId);
        }

        @Override
        public void lockSound(final OpParameter resId) {
            printM(".lockSound", resId);
        }

        @Override
        public void lockCostume(final OpParameter resId) {
            printM(".lockCostume", resId);
        }

        @Override
        public void lockRoom(final OpParameter resId) {
            printM(".lockRoom", resId);
        }

        @Override
        public void unlockScript(final OpParameter resId) {
            printM(".unlockScript", resId);
        }

        @Override
        public void unlockSound(final OpParameter resId) {
            printM(".unlockSound", resId);
        }

        @Override
        public void unlockCostume(final OpParameter resId) {
            printM(".unlockCostume", resId);
        }

        @Override
        public void unlockRoom(final OpParameter resId) {
            printM(".unlockRoom", resId);
        }

        @Override
        public void loadCharset(final OpParameter resId) {
            printM(".loadCharset", resId);
        }

        @Override
        public void nukeRoom(final OpParameter resId) {
            printM(".nukeRoom", resId);
        }

        @Override
        public void nukeCostume(final OpParameter resId) {
            printM(".nukeCostume", resId);
        }

    }

    private class ActorOpsImp implements ActorOps {

        private final OpParameter actor;
        private List<String> args = new ArrayList<>();

        ActorOpsImp(final OpParameter actor) {
            this.actor = actor;
        }

        @Override
        public void costume(final OpParameter costume) {
            args.add(sprintM("Costume", costume));
        }

        @Override
        public void walkSpeed(final OpParameter speedX, final OpParameter speedY) {
            args.add(sprintM("WalkSpeed", speedX, speedY));
        }

        @Override
        public void sound(final OpParameter sound) {
            args.add(sprintM("Sound", sound));
        }

        @Override
        public void walkAnimNr(final OpParameter anim) {
            args.add(sprintM("WalkAnimNr", anim));
        }

        @Override
        public void talkAnimNr(final OpParameter startFrame, final OpParameter stopFrame) {
            args.add(sprintM("TalkAnimNr", startFrame, stopFrame));
        }

        @Override
        public void standAnimNr(final OpParameter anim) {
            args.add(sprintM("StandAnimNr", anim));
        }

        @Override
        public void init() {
            args.add(sprintM("Init"));
        }

        @Override
        public void elevation(final OpParameter elevation) {
            args.add(sprintM("Elevation", elevation));
        }

        @Override
        public void width(final OpParameter width) {
            args.add(sprintM("Width", width));
        }

        @Override
        public void scale(final OpParameter scale) {
            args.add(sprintM("Scale", scale));
        }

        @Override
        public void palette(final OpParameter idx, final OpParameter val) {
            args.add(sprintM("Palette", idx, val));
        }

        @Override
        public void talkColor(final OpParameter color) {
            args.add(sprintM("TalkColor", color));
        }

        @Override
        public void name(final ScummString name) {
            args.add(sprintM("Name", decodeScummString(name)));
        }

        @Override
        public void initAnimNr(final OpParameter anim) {
            args.add(sprintM("InitAnimNr", anim));
        }

        @Override
        public void end() {
            printM("ActorOps", actor, args);
        }

    }

    private class VerbOpsImpl implements VerbOps {

        private final OpParameter verb;
        private final List<String> args = new ArrayList<>();

        VerbOpsImpl(final OpParameter verb) {
            this.verb = verb;
        }

        @Override
        public void text(final ScummString text) {
            args.add(sprintM("Text", decodeScummString(text)));
        }

        @Override
        public void color(final OpParameter color) {
            args.add(sprintM("Color", color));
        }

        @Override
        public void hiColor(final OpParameter color) {
            args.add(sprintM("HiColor", color));
        }

        @Override
        public void setXy(final OpParameter x, final OpParameter y) {
            args.add(sprintM("SetXY", x, y));
        }

        @Override
        public void on() {
            args.add(sprintM("On"));
        }

        @Override
        public void off() {
            args.add(sprintM("Off"));
        }

        @Override
        public void create() {
            args.add(sprintM("New"));
        }

        @Override
        public void dimColor(final OpParameter color) {
            args.add(sprintM("DimColor", color));
        }

        @Override
        public void key(final OpParameter key) {
            args.add(sprintM("Key", key));
        }

        @Override
        public void center() {
            args.add(sprintM("Center"));
        }

        @Override
        public void setToString(final OpParameter str) {
            args.add(sprintM("SetToString", str));
        }

        @Override
        public void end() {
            printM("VerbOps", verb, args);
        }

    }

    private class PrintOpsImpl implements PrintOps {

        private final OpParameter ego;
        private List<String> args = new ArrayList<>();

        PrintOpsImpl() {
            ego = null;
        }

        PrintOpsImpl(final OpParameter ego) {
            this.ego = ego;
        }

        @Override
        public void pos(final OpParameter x, final OpParameter y) {
            args.add(sprintM("Pos", x, y));
        }

        @Override
        public void color(final OpParameter color) {
            args.add(sprintM("Color", color));
        }

        @Override
        public void clipped(final OpParameter clipped) {
            args.add(sprintM("Clipped", clipped));
        }

        @Override
        public void center() {
            args.add(sprintM("Center"));
        }

        @Override
        public void left() {
            args.add(sprintM("Left"));
        }

        @Override
        public void overhead() {
            args.add(sprintM("Overhead"));
        }

        @Override
        public void text(final ScummString text) {
            args.add(sprintM("Text", decodeScummString(text)));
        }

        @Override
        public void end() {
            if (ego != null) {
                printM("print", ego, args);
            } else {
                printM("printEgo", args);
            }
        }

    }

}
