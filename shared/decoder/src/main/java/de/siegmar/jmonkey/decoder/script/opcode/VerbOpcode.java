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

@SuppressWarnings("checkstyle:CyclomaticComplexity")
public class VerbOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x7A, 0xFA);

    public VerbOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter verb = resolveParameter8(bb, opcode, 0);

        final var verbOps = opcodeDelegate.verbOps(verb);

        int subOpcode;
        while ((subOpcode = bb.readU8()) != 0xFF) {
            switch (subOpcode & 0x1F) {
                case 0x02 -> verbOps.text(getString(bb));
                case 0x03 -> verbOps.color(resolveParameter8(bb, subOpcode, 0));
                case 0x04 -> verbOps.hiColor(resolveParameter8(bb, subOpcode, 0));
                case 0x05 -> verbOps.setXy(resolveParameter16(bb, subOpcode, 0),
                    resolveParameter16(bb, subOpcode, 1));
                case 0x06 -> verbOps.on();
                case 0x07 -> verbOps.off();
                case 0x09 -> verbOps.create();
                case 0x10 -> verbOps.dimColor(resolveParameter8(bb, subOpcode, 0));
                case 0x12 -> verbOps.key(resolveParameter8(bb, subOpcode, 0));
                case 0x13 -> verbOps.center();
                case 0x14 -> verbOps.setToString(resolveParameter16(bb, subOpcode, 0));
                default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
            }
        }

        verbOps.end();
    }

}
