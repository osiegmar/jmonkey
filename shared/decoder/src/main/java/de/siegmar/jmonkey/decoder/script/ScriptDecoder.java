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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.siegmar.jmonkey.decoder.script.opcode.ActorFollowCameraOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ActorFromPosOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ActorOpsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ActorSetClassOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.AnimateActorOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.BreakHereOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ChainScriptOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ClassOfIsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.CursorCommandOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.CutsceneOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DebugOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DelayOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DelayVariableOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DoSentenceOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DrawBoxOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.DrawObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.EndCutsceneOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ExpressionOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.FaceActorOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.FindInventoryOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.FindObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.FreezeScriptsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorCostumeOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorElevevationOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorFacingOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorMovingOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorRoomOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorWalkBoxOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorWidthOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorXOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetActorYOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetDistOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetInventoryCountOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetObjectOwnerOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetRandomNumberOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.GetVerbEntryPointOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.IfStateOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.IsScriptRunningOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.IsSoundRunningOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.JumpRelativeOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.LightsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.LoadRoomOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.LoadRoomWithEgoOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.MatrixOpOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.OldRoomEffectSetOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.OneOpCmpOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.Opcode;
import de.siegmar.jmonkey.decoder.script.opcode.OperatorOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.OverrideOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PanCameraToOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PickupObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PrintOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PseudoRoomOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PutActorAtObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PutActorInRoomOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.PutActorOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.ResourceRoutinesOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.RoomOpsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SaveRestoreVerbsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SetCameraAtOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SetObjectNameOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SetOwnerOfOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SetStateOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SetVarRangeOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StartObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StartScriptOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StartSoundOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StopObjectCodeOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StopScriptOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StopSoundOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.StringOpsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.SystemOpsOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.TwoOpCmpOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.VerbOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.WaitOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.WalkActorToActorOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.WalkActorToObjectOpcode;
import de.siegmar.jmonkey.decoder.script.opcode.WalkActorToOpcode;

@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
public class ScriptDecoder {

    private  final Map<Integer, Opcode> decoders = new HashMap<>();

    public ScriptDecoder() {
        loadOpcodes();
    }

    @SuppressWarnings({"checkstyle:JavaNCSS", "checkstyle:ExecutableStatementCount"})
    private void loadOpcodes() {
        addOpCode(new ActorFollowCameraOpcode());
        addOpCode(new ActorFromPosOpcode());
        addOpCode(new ActorOpsOpcode());
        addOpCode(new ActorSetClassOpcode());
        addOpCode(new AnimateActorOpcode());
        addOpCode(new BreakHereOpcode());
        addOpCode(new ChainScriptOpcode());
        addOpCode(new ClassOfIsOpcode());
        addOpCode(new CursorCommandOpcode());
        addOpCode(new CutsceneOpcode());
        addOpCode(new DebugOpcode());
        addOpCode(new DelayOpcode());
        addOpCode(new DelayVariableOpcode());
        addOpCode(new DoSentenceOpcode());
        addOpCode(new DrawBoxOpcode());
        addOpCode(new DrawObjectOpcode());
        addOpCode(new EndCutsceneOpcode());
        addOpCode(new ExpressionOpcode());
        addOpCode(new FaceActorOpcode());
        addOpCode(new FindInventoryOpcode());
        addOpCode(new FindObjectOpcode());
        addOpCode(new FreezeScriptsOpcode());
        addOpCode(new GetActorCostumeOpcode());
        addOpCode(new GetActorElevevationOpcode());
        addOpCode(new GetActorFacingOpcode());
        addOpCode(new GetActorMovingOpcode());
        addOpCode(new GetActorRoomOpcode());
        addOpCode(new GetActorWalkBoxOpcode());
        addOpCode(new GetActorWidthOpcode());
        addOpCode(new GetActorXOpcode());
        addOpCode(new GetActorYOpcode());
        addOpCode(new GetDistOpcode());
        addOpCode(new GetInventoryCountOpcode());
        addOpCode(new GetObjectOwnerOpcode());
        addOpCode(new GetRandomNumberOpcode());
        addOpCode(new GetVerbEntryPointOpcode());
        addOpCode(new IfStateOpcode());
        addOpCode(new IsScriptRunningOpcode());
        addOpCode(new IsSoundRunningOpcode());
        addOpCode(new JumpRelativeOpcode());
        addOpCode(new LightsOpcode());
        addOpCode(new LoadRoomOpcode());
        addOpCode(new LoadRoomWithEgoOpcode());
        addOpCode(new MatrixOpOpcode());
        addOpCode(new OldRoomEffectSetOpcode());
        addOpCode(new OneOpCmpOpcode());
        addOpCode(new OperatorOpcode());
        addOpCode(new OverrideOpcode());
        addOpCode(new PanCameraToOpcode());
        addOpCode(new PickupObjectOpcode());
        addOpCode(new PrintOpcode());
        addOpCode(new PseudoRoomOpcode());
        addOpCode(new PutActorAtObjectOpcode());
        addOpCode(new PutActorInRoomOpcode());
        addOpCode(new PutActorOpcode());
        addOpCode(new ResourceRoutinesOpcode());
        addOpCode(new RoomOpsOpcode());
        addOpCode(new SaveRestoreVerbsOpcode());
        addOpCode(new SetCameraAtOpcode());
        addOpCode(new SetObjectNameOpcode());
        addOpCode(new SetOwnerOfOpcode());
        addOpCode(new SetStateOpcode());
        addOpCode(new SetVarRangeOpcode());
        addOpCode(new StartObjectOpcode());
        addOpCode(new StartScriptOpcode());
        addOpCode(new StartSoundOpcode());
        addOpCode(new StopObjectCodeOpcode());
        addOpCode(new StopScriptOpcode());
        addOpCode(new StopSoundOpcode());
        addOpCode(new StringOpsOpcode());
        addOpCode(new SystemOpsOpcode());
        addOpCode(new TwoOpCmpOpcode());
        addOpCode(new VerbOpcode());
        addOpCode(new WaitOpcode());
        addOpCode(new WalkActorToActorOpcode());
        addOpCode(new WalkActorToObjectOpcode());
        addOpCode(new WalkActorToOpcode());
    }

    private void addOpCode(final Opcode opcode) {
        for (final Integer i : opcode.opcodes()) {
            if (decoders.containsKey(i)) {
                throw new IllegalStateException("Operator %02X already defined by %s; Can't register %s"
                    .formatted(i, decoders.get(i), opcode));
            }
            decoders.put(i, opcode);
        }
    }

    public Optional<Opcode> getOpcodeDecoder(final int opcode) {
        return Optional.ofNullable(decoders.get(opcode));
    }

}
