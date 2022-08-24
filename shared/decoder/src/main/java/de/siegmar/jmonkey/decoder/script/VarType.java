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

import de.siegmar.jmonkey.commons.lang.Assert;

public enum VarType {

    BIT("Bit"),
    LOCAL("Local"),
    VAR("Var");

    private final String str;

    VarType(final String str) {
        this.str = str;
    }

    public static VarType of(final int i) {
        if ((i & 0x8000) != 0) {
            Assert.assertThat((i & 0xFFF) < 0x800);
            return BIT;
        }

        if ((i & 0x4000) != 0) {
            Assert.assertThat((i & 0xFFF) <= 0x10);
            return LOCAL;
        }

        Assert.assertThat((i & 0xFFF) < 0x320);
        return VAR;
    }

    public int to(final int val) {
        return switch (this) {
            case BIT -> val | 0x8000;
            case LOCAL -> val | 0x4000;
            case VAR -> val;
        };
    }

    @Override
    public String toString() {
        return str;
    }

}
