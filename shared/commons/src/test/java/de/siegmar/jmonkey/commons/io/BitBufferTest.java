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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

class BitBufferTest {

    @Test
    void singleByteBE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b00010001}, ByteOrder.BIG_ENDIAN);
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    @Test
    void singleByteLE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b00010001}, ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    @Test
    void multiByteBE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b00010001, (byte) 0b11100010}, ByteOrder.BIG_ENDIAN);
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));

        assertEquals(1, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));

        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    @Test
    void multiByteLE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b00010001, (byte) 0b11100010}, ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));

        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(0, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(1, bb.readBits(1));
        assertEquals(1, bb.readBits(1));

        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    @Test
    void multiBitBE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b000_100_01, (byte) 0b1_110_001_0}, ByteOrder.BIG_ENDIAN);
        assertEquals(0, bb.readBits(3));
        assertEquals(4, bb.readBits(3));
        assertEquals(3, bb.readBits(3));
        assertEquals(6, bb.readBits(3));
        assertEquals(1, bb.readBits(3));
        assertEquals(0, bb.readBits(1));
        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    @Test
    void multiBitLE() {
        final BitBuffer bb = getBitBuffer(new byte[]{0b00_010_001, (byte) 0b1_110_001_0}, ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bb.readBits(3));
        assertEquals(2, bb.readBits(3));
        assertEquals(0, bb.readBits(3));
        assertEquals(1, bb.readBits(3));
        assertEquals(6, bb.readBits(3));
        assertEquals(1, bb.readBits(1));
        assertThrows(BufferUnderflowException.class, () -> bb.readBits(1));
    }

    private BitBuffer getBitBuffer(final byte[] array, final ByteOrder byteOrder) {
        return new BitBuffer(ByteBuffer.wrap(array), byteOrder);
    }

}
