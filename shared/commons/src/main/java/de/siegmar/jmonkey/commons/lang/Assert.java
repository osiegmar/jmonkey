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

public final class Assert {

    private Assert() {
    }

    public static void assertThat(final boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    public static void assertThat(final boolean expression, final String msg, final Object... args) {
        if (!expression) {
            throw new IllegalStateException(msg.formatted(args));
        }
    }

    public static void assertEqual(final Object value, final Object expected) {
        if (!expected.equals(value)) {
            throw new IllegalStateException("value '%s' is not '%s' as expected".formatted(value, expected));
        }
    }

    public static void assertEqual(final long value, final long expected) {
        if (value != expected) {
            throw new IllegalStateException("value '%s' is not '%s' as expected".formatted(value, expected));
        }
    }

    public static void assertRange(final long value, final long min, final long max) {
        assertMin(value, min);
        assertMax(value, max);
    }

    private static void assertMin(final long value, final long min) {
        if (value < min) {
            throw new IllegalStateException("value " + value + " is smaller than minimum of " + min);
        }
    }

    private static void assertMax(final long value, final long max) {
        if (value > max) {
            throw new IllegalStateException("value " + value + " is bigger than maximum of " + max);
        }
    }

}
