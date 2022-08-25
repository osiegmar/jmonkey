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

package de.siegmar.jmonkey.encoder.script;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ScummStringEncoderTest {

    @Test
    void simple() {
        final String actual = "foo";
        final String expected = "foo$00";
        assertThis(actual, expected);
    }

    @Test
    void newline() {
        final String actual = "a{newline}b";
        final String expected = "a$FF$01b$00";
        assertThis(actual, expected);
    }

    @Test
    void newline2() {
        final String actual = "a{newline2}b";
        final String expected = "a$FE$01b$00";
        assertThis(actual, expected);
    }

    @Test
    void keepText() {
        final String actual = "a{keepText}b";
        final String expected = "a$FF$02b$00";
        assertThis(actual, expected);
    }

    @Test
    void sleep() {
        final String actual = "a{sleep}b";
        final String expected = "a$FF$03b$00";
        assertThis(actual, expected);
    }

    @Test
    void verbNewline() {
        final String actual = "a{verbNewline}b";
        final String expected = "a$FE$08b$00";
        assertThis(actual, expected);
    }

    @Test
    void getInt() {
        final String actual = "a{int(Var[308])}b";
        assertThat(ScummStringEncoder.encode(actual).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x01, 0x04, 308 & 0xFF, 308 >> 8, 'b', 0x00});
    }

    void assertThis(final String actual, final String expected) {
        final String vActual = visualize(ScummStringEncoder.encode(actual).dumpCopy());
        final String vExpected = visualize(expected.getBytes(StandardCharsets.UTF_8));
        assertEquals(vExpected, vActual);
    }

    String visualize(final byte[] data) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : data) {
            if (b > 31) {
                sb.append((char) b);
            } else {
                sb.append("$%02X".formatted(b));
            }
        }
        return sb.toString();
    }

}
