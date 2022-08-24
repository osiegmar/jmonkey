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

import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.decoder.script.VarType;

public class ParameterBuilder {

    private final VarType varType;
    private final List<VariablePointer> pointers = new ArrayList<>();

    public ParameterBuilder(final VarType varType) {
        this.varType = varType;
    }

    public void appendPointer(final int address) {
        pointers.add(new VariablePointer(null, address));
    }

    public void appendPointer(final VarType type, final int address) {
        pointers.add(new VariablePointer(type, address));
    }

    // Var[100]
    // Var[100 + 5]
    // Var[100 + Bit[5]]
    public OpParameter build() {
        return new CompoundParameter(varType, pointers);
    }

}
