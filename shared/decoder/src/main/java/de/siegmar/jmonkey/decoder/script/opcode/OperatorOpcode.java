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
import de.siegmar.jmonkey.decoder.script.operator.AssignmentOperator;
import de.siegmar.jmonkey.decoder.script.operator.UnaryOperator;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

// TODO rename
public class OperatorOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(
        0x46, 0xC6,
        0x17, 0x57, 0x97, 0xD7,
        0x1A, 0x3A, 0x5A, 0x9A, 0xBA, 0xDA,
        0x1B, 0x9B, 0x5B, 0xDB);

    public OperatorOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter var = resolvePointer(bb);

        if ((opcode & 0x7F) == 0x46) {
            final UnaryOperator operator = (opcode & 128) != 0
                ? UnaryOperator.DECREMENT : UnaryOperator.INCREMENT;
            opcodeDelegate.mutateVariable(var, operator);
        } else {
            final AssignmentOperator operator = switch (opcode & 0x7F) {
                case 0x0A, 0x1A, 0x2C -> AssignmentOperator.ASSIGN;
                case 0x1B -> AssignmentOperator.MULTIPLY_ASSIGN;
                case 0x3A, 0x6A -> AssignmentOperator.SUBTRACT_ASSIGN;
                case 0x2A, 0x5A -> AssignmentOperator.ADD_ASSIGN;
                case 0x5B -> AssignmentOperator.DIVIDE_ASSIGN;
                default -> throw new IllegalStateException("opcode not found: %X".formatted(opcode));
            };

            final OpParameter operand = resolveParameter16(bb, opcode, 0);

            opcodeDelegate.mutateVariable(var, operator, operand);
        }
    }

}
