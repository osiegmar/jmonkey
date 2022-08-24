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
import de.siegmar.jmonkey.decoder.script.operator.ComparisonOperator;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class TwoOpCmpOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES =
        Set.of(
            0x04, 0x44, 0x84, 0xC4,
            0x08, 0x38, 0x48, 0x78, 0x88, 0xB8, 0xC8, 0xF8);

    public TwoOpCmpOpcode() {
        super(OPCODES);
    }

    // opcode var value[p16] target[16]
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter var = resolvePointer(bb);
        final OpParameter val = resolveParameter16(bb, opcode, 0);
        final int target = bb.readS16();
        final int offset = bb.position() + target;

        final ComparisonOperator operator = switch (opcode & 0x7F) {
            case 0x38 -> ComparisonOperator.GREATER_EQUAL;
            case 0x78 -> ComparisonOperator.LESSER;
            case 0x04 -> ComparisonOperator.LESSER_EQUAL;
            case 0x44 -> ComparisonOperator.GREATER;
            case 0x48 -> ComparisonOperator.EQUAL;
            case 0x08 -> ComparisonOperator.NOT_EQUAL;
            default -> throw new IllegalStateException("opcode not found: %X".formatted(opcode));
        };

        opcodeDelegate.conditionalJump(var, operator, val, offset);
    }

}
