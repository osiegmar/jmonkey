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

package de.siegmar.jmonkey.decoder.script.parameter;

import java.util.Iterator;
import java.util.List;

import de.siegmar.jmonkey.decoder.script.VarType;

public class CompoundParameter implements OpParameter {

    private final VarType varType;
    private final List<VariablePointer> pointers;

    public CompoundParameter(final VarType varType, final List<VariablePointer> pointers) {
        this.varType = varType;
        this.pointers = List.copyOf(pointers);
    }

    public VarType getVarType() {
        return varType;
    }

    public List<VariablePointer> getPointers() {
        return pointers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(varType).append('[');

        for (final Iterator<VariablePointer> it = pointers.iterator(); it.hasNext();) {
            final VariablePointer pointer = it.next();

            if (pointer.type() != null) {
                sb.append(pointer.type()).append('[');
                sb.append(pointer.address());
                sb.append(']');
            } else {
                sb.append(pointer.address());
            }

            if (it.hasNext()) {
                sb.append(" + ");
            }
        }

        return sb.append(']').toString();
    }

}
