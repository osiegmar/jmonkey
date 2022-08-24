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

package de.siegmar.jmonkey.font;

public enum FontChunkType {

    CHARSET("c\u0003");

    private final String name;

    FontChunkType(final String name) {
        this.name = name;
    }

    public static FontChunkType of(final String name) {
        if (CHARSET.getName().equals(name)) {
            return CHARSET;
        }
        throw new IllegalStateException("Unknown name: " + name);
    }

    public String getName() {
        return name;
    }

}
