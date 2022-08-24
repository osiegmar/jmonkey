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

public class MatrixOpOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x30);

    public MatrixOpOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int subOpcode = bb.readU8();

        if ((subOpcode & 0x1F) == 0x04) {
            opcodeDelegate.createBoxMatrix();
            return;
        }

        final OpParameter box = resolveParameter8(bb, subOpcode, 0);
        final OpParameter val = resolveParameter8(bb, subOpcode, 1);

        if ((subOpcode & 0x1F) == 0x01) {
            opcodeDelegate.setBoxFlags(box, val);
        } else {
            throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
        }
    }

}
