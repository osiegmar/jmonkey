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

import java.util.List;

import de.siegmar.jmonkey.decoder.script.operator.AssignmentOperator;
import de.siegmar.jmonkey.decoder.script.operator.ComparisonOperator;
import de.siegmar.jmonkey.decoder.script.operator.UnaryOperator;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

@SuppressWarnings("checkstyle:MethodCount")
public interface OpcodeDelegate {

    Script getScript();

    void setScript(Script mainScript);

    void stopLocalGlobalScript();

    void stopObjectCode();

    void actorFollowCamera(OpParameter actor);

    void actorFromPos(OpParameter result, OpParameter x, OpParameter y);

    void setClass(OpParameter obj, List<OpParameter> classes);

    void animateCostume(OpParameter costume, OpParameter animation);

    void chainScript(OpParameter script, List<OpParameter> args);

    void initCharset(OpParameter charset);

    void cutscene(List<OpParameter> args);

    void debug(OpParameter param);

    void drawBox(OpParameter left, OpParameter top, OpParameter right, OpParameter bottom, OpParameter color);

    void drawObject(OpParameter object, OpParameter xpos, OpParameter ypos);

    void endCutscene();

    void faceActor(OpParameter actor, OpParameter object);

    void findInventory(OpParameter result, OpParameter owner, OpParameter index);

    void findObject(OpParameter result, OpParameter x, OpParameter y);

    void freezeScripts(OpParameter flag);

    void getActorCostume(OpParameter result, OpParameter actor);

    void getActorElevation(OpParameter result, OpParameter actor);

    void getActorFacing(OpParameter result, OpParameter actor);

    void getActorMoving(OpParameter result, OpParameter actor);

    void getActorRoom(OpParameter result, OpParameter actor);

    void getActorWalkBox(OpParameter result, OpParameter actor);

    void getActorWidth(OpParameter result, OpParameter actor);

    void getActorX(OpParameter result, OpParameter actor);

    void getActorY(OpParameter result, OpParameter actor);

    void getDist(OpParameter result, OpParameter o1, OpParameter o2);

    void getInventoryCount(OpParameter result, OpParameter actor);

    void getObjectOwner(OpParameter result, OpParameter object);

    void getRandomNr(OpParameter result, OpParameter bound);

    void getVerbEntryPoint(OpParameter result, OpParameter object, OpParameter verb);

    void isScriptRunning(OpParameter result, OpParameter script);

    void isSoundRunning(OpParameter result, OpParameter sound);

    void lights(OpParameter arg1, int arg2, int arg3);

    void loadRoom(OpParameter room);

    void loadRoomWithEgo(OpParameter object, OpParameter room, int x, int y);

    void createBoxMatrix();

    void setBoxFlags(OpParameter box, OpParameter val);

    void oldRoomEffectSet(int x);

    void walkActorTo(OpParameter actor, OpParameter x, OpParameter y);

    void walkActorToObject(OpParameter actor, OpParameter object);

    void walkActorToActor(OpParameter walker, OpParameter walkee, int distance);

    void waitForActor(OpParameter actor);

    void waitForMessage();

    void waitForCamera();

    void waitForSentence();

    void quit();

    void stopSound(OpParameter sound);

    void stopScript(OpParameter script);

    void startSound(OpParameter sound);

    void startObject(OpParameter obj, OpParameter script, List<OpParameter> args);

    void setState(OpParameter object, OpParameter state);

    void setOwnerOf(OpParameter object, OpParameter owner);

    void setObjectName(OpParameter obj, ScummString name);

    void setCameraAt(OpParameter x);

    void putActor(OpParameter actor, OpParameter x, OpParameter y);

    void putActorInRoom(OpParameter actor, OpParameter room);

    void putActorAtObject(OpParameter actor, OpParameter object);

    void pickupObject(OpParameter object);

    void panCameraTo(OpParameter x);

    void beginOverride();

    void endOverride();

    void startExpression();

    Object endExpression();

    void saveVerbs(OpParameter start, OpParameter end, OpParameter mode);

    void restoreVerbs(OpParameter start, OpParameter end, OpParameter mode);

    void setVarRange(OpParameter result, int number, List<Integer> varlist);

    void doSentence(OpParameter verb, OpParameter obj1, OpParameter obj2);

    void doSentenceStop();

    void roomColor(OpParameter color, OpParameter palIndex);

    void shake(boolean on);

    void roomScroll(OpParameter minX, OpParameter maxX);

    void setScreen(OpParameter width, OpParameter height);

    void setPalColor(OpParameter color, OpParameter palIndex);

    void pseudoRoom(int room, List<Integer> aliases);

    ResourceManager resource();

    void createString(OpParameter stringId, OpParameter size);

    void getStringChar(OpParameter result, OpParameter stringId, OpParameter index);

    void setStringChar(OpParameter stringId, OpParameter index, OpParameter ch);

    void copyString(OpParameter destId, OpParameter srcId);

    void putCodeInString(OpParameter stringId, ScummString str);

    void mutateVariable(OpParameter var, UnaryOperator operator);

    void mutateVariable(OpParameter var, AssignmentOperator operator, OpParameter operand);

    ActorOps actorOps(OpParameter actor);

    VerbOps verbOps(OpParameter verb);

    PrintOps printEgo();

    PrintOps print(OpParameter ego);

    void exprMode(OpParameter result, Object pop);

    void cursor(InputMode mode);

    void userput(InputMode mode);

    void startScript(OpParameter script, List<OpParameter> args, boolean freezeResistant, boolean recursive);

    void breakHere();

    void gotoOffset(int offset);

    void stateConditionJump(OpParameter var, ComparisonOperator op, OpParameter cmp, int offset);

    void conditionalJump(OpParameter var, ComparisonOperator operator, OpParameter val, int offset);

    void conditionalJump(OpParameter var, ComparisonOperator operator, int offset);

    void classOfIsConditionJump(OpParameter value, List<OpParameter> args, int offset);

    void delay(int delay);

    void delayVariable(OpParameter var);

    Object callOpcode();

    void beforeOpcode(int pos, int opcode);

    void afterOpcode();

    void endScript();

}
