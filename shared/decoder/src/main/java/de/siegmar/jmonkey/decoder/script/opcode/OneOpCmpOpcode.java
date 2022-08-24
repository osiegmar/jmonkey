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

public class OneOpCmpOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x28, 0xA8);

    public OneOpCmpOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter var = resolvePointer(bb);
        final int target = bb.readS16();
        final int offset = bb.position() + target;

        final ComparisonOperator nop = switch (opcode) {
            case 0x28 -> ComparisonOperator.EQUAL;
            case 0xA8 -> ComparisonOperator.NOT_EQUAL;
            default -> throw new IllegalStateException("opcode not found: %X".formatted(opcode));
        };

        opcodeDelegate.conditionalJump(var, nop, offset);
    }

}
