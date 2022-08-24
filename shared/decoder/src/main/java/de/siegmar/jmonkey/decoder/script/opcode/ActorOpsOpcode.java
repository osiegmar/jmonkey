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

package de.siegmar.jmonkey.decoder.script.opcode;

import java.util.Set;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

/**
 * Decoder for {@code actorOps} opcode (original: {@code actor}).
 */
public class ActorOpsOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x13, 0x53, 0x93, 0xD3);

    public ActorOpsOpcode() {
        super(OPCODES);
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter actor = resolveParameter8(bb, opcode, 0);

        final var actorOps = opcodeDelegate.actorOps(actor);

        int subOpCode;
        while ((subOpCode = bb.readU8()) != 0xFF) {
            switch (subOpCode & 0x1F) {
                case 0x01 -> actorOps.costume(resolveParameter8(bb, subOpCode, 0));
                case 0x04 -> actorOps.walkSpeed(resolveParameter8(bb, subOpCode, 0),
                    resolveParameter8(bb, subOpCode, 1));
                case 0x05 -> actorOps.sound(resolveParameter8(bb, subOpCode, 0));
                case 0x06 -> actorOps.walkAnimNr(resolveParameter8(bb, subOpCode, 0));
                case 0x07 -> actorOps.talkAnimNr(resolveParameter8(bb, subOpCode, 0),
                    resolveParameter8(bb, subOpCode, 1));
                case 0x08 -> actorOps.standAnimNr(resolveParameter8(bb, subOpCode, 0));
                case 0x0A -> actorOps.init();
                case 0x0B -> actorOps.elevation(resolveParameter16(bb, subOpCode, 0));
                case 0x0D -> actorOps.palette(resolveParameter8(bb, subOpCode, 0),
                    resolveParameter8(bb, subOpCode, 1)
                );
                case 0x0E -> actorOps.talkColor(resolveParameter8(bb, subOpCode, 0));
                case 0x0F -> actorOps.name(getString(bb));
                case 0x10 -> actorOps.initAnimNr(resolveParameter8(bb, subOpCode, 0));
                case 0x12 -> actorOps.width(resolveParameter8(bb, subOpCode, 0));
                case 0x13 -> actorOps.scale(resolveParameter8(bb, subOpCode, 0));
                default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpCode));
            }
        }

        actorOps.end();
    }

}
