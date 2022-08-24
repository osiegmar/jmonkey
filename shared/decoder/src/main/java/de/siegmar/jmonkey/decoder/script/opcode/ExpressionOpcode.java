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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.decoder.script.Calculation;
import de.siegmar.jmonkey.decoder.script.ExpressionResult;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.operator.ArithmeticOperator;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class ExpressionOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0xAC);

    public ExpressionOpcode() {
        super(OPCODES);
    }

    // opcode result subIpcode... $FF
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter result = resolvePointer(bb);
        final Deque<Object> stack = new ArrayDeque<>();

        int subOpcode;
        while ((subOpcode = bb.readU8()) != 0xFF) {
            switch (subOpcode & 0x1F) {
                case 0x1 -> stack.push(resolveParameter16(bb, subOpcode, 0));
                case 0x2, 0x3, 0x4, 0x5 -> stack.push(
                    new Calculation(stack.pop(), stack.pop(), mapOperator(subOpcode)));
                case 0x6 -> stack.push(new ExpressionResult(opcodeDelegate.callOpcode()));
                default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
            }
        }

        opcodeDelegate.exprMode(result, stack.pop());
    }

    private ArithmeticOperator mapOperator(final int subOpcode) {
        return switch (subOpcode - 0x2) {
            case 0 -> ArithmeticOperator.PLUS;
            case 1 -> ArithmeticOperator.MINUS;
            case 2 -> ArithmeticOperator.MULTIPLY;
            case 3 -> ArithmeticOperator.DIVIDE;
            default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
        };
    }

}
