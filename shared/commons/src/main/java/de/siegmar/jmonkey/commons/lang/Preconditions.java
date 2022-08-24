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

package de.siegmar.jmonkey.commons.lang;

public final class Preconditions {

    private Preconditions() {
    }

    public static void checkArgument(final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(final boolean expression, final String msg) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(msg));
        }
    }

    public static void checkArgument(final boolean expression, final String msg, final Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(msg.formatted(args)));
        }
    }

}
