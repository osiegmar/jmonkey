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

package de.siegmar.jmonkey.commons.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class XorInputStreamTest {

    @Test
    void illegalPattern() {
        assertThrows(IllegalArgumentException.class, () ->
            new XorInputStream(new ByteArrayInputStream(new byte[]{}), (byte) -1));
    }

    @Test
    void getPattern() {
        final XorInputStream in = new XorInputStream(new ByteArrayInputStream(new byte[]{}), (byte) 1);
        assertEquals(1, in.getPattern());
    }

    @Test
    void read() throws IOException {
        final byte[] data = {0, 1, 2, 3};
        final XorInputStream in = new XorInputStream(new ByteArrayInputStream(data), (byte) 1);

        assertEquals(1, in.read());
        assertEquals(0, in.read());
        assertEquals(3, in.read());
        assertEquals(2, in.read());
        assertEquals(-1, in.read());
    }

    @Test
    void readArray() throws IOException {
        final byte[] data = {0, 1, 2, 3};
        final XorInputStream in = new XorInputStream(new ByteArrayInputStream(data), (byte) 1);

        assertEquals(1, in.getPattern());

        final byte[] expected = {1, 0, 3, 2};
        assertArrayEquals(expected, in.readAllBytes());
    }

}
