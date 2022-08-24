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

import java.util.StringJoiner;

import de.siegmar.jmonkey.decoder.script.operator.AssignmentOperator;
import de.siegmar.jmonkey.decoder.script.operator.UnaryOperator;

public class ScummInteger {

    private int val;

    public ScummInteger(final int initialValue) {
        val = initialValue;
    }

    public int getVal() {
        return val;
    }

    public void apply(final UnaryOperator op) {
        switch (op) {
            case INCREMENT -> val++;
            case DECREMENT -> val--;
            default -> throw new IllegalStateException();
        }
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    public void apply(final AssignmentOperator op, final ScummInteger operand) {
        final int opval = operand.getVal();
        switch (op) {
            case ASSIGN -> val = opval;
            case ADD_ASSIGN -> val += opval;
            case SUBTRACT_ASSIGN -> val -= opval;
            case MULTIPLY_ASSIGN -> val *= opval;
            case DIVIDE_ASSIGN -> val /= opval;
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ScummInteger.class.getSimpleName() + "[", "]")
            .add("val=" + val)
            .toString();
    }

}
