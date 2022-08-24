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

package de.siegmar.jmonkey.index;

public enum IndexChunkType {

    ROOM_NAMES("RN"),
    ROOM_DIR("0R"),
    SCRIPT_DIR("0S"),
    SOUND_DIR("0N"),
    COSTUME_DIR("0C"),
    GLOBAL_OBJECTS("0O");

    private final String name;

    IndexChunkType(final String name) {
        this.name = name;
    }

    public static IndexChunkType of(final String name) {
        for (final IndexChunkType value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new IllegalStateException("Unknown name: " + name);
    }

    public String getName() {
        return name;
    }

}
