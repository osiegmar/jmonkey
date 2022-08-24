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

import org.junit.jupiter.api.Test;

class ScummStringEncoderTest {

    @Test
    void simple() {
        assertThat(ScummStringEncoder.encode("foo", false).dumpCopy())
            .isEqualTo(new byte[]{'f', 'o', 'o', 0x00});
    }

    @Test
    void newline() {
        assertThat(ScummStringEncoder.encode("a{newline}b", false).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x02, 0x01, 'b', 0x00});
    }

    @Test
    void keepText() {
        assertThat(ScummStringEncoder.encode("a{keepText}b", false).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x01, 0x02, 'b', 0x00});
    }

    @Test
    void sleep() {
        assertThat(ScummStringEncoder.encode("a{sleep}b", false).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x01, 0x03, 'b', 0x00});
    }

    @Test
    void verbNewline() {
        assertThat(ScummStringEncoder.encode("a{verbNewline}b", false).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x02, 0x08, 'b', 0x00});
    }

    @Test
    void getInt() {
        assertThat(ScummStringEncoder.encode("a{int(Var[308])}b", false).dumpCopy())
            .isEqualTo(new byte[]{'a', -0x01, 0x04, 308 & 0xFF, 308 >> 8, 'b', 0x00});
    }

}
