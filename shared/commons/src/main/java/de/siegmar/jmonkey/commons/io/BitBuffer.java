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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitBuffer {

    private static final int[] MASKS = new int[Byte.SIZE + 1];
    private static final int BITS_PER_BYTE = 8;

    private final ByteBuffer bb;
    private final ByteOrder byteOrder;
    private int curData;
    private int bitLeft;

    static {
        for (int i = 1; i <= Byte.SIZE; i++) {
            MASKS[i] = (MASKS[i - 1] << 1) + 1;
        }
    }

    public BitBuffer(final ByteBuffer bb, final ByteOrder byteOrder) {
        this.bb = bb;
        this.byteOrder = byteOrder;
    }

    private void ensureBuffer(final int len) {
        if (bitLeft < len) {
            final int fetch = bb.get() & 0xff;
            if (ByteOrder.LITTLE_ENDIAN.equals(byteOrder)) {
                curData |= fetch << bitLeft;
            } else {
                curData <<= Byte.SIZE;
                curData |= fetch;
            }
            bitLeft += Byte.SIZE;
        }
    }

    public boolean readBit() {
        return readBits(1) == 1;
    }

    public int readBits(final int len) {
        if (len > BITS_PER_BYTE) {
            throw new IllegalArgumentException("Len > 8 not supported");
        }

        ensureBuffer(len);

        final int val;
        if (ByteOrder.LITTLE_ENDIAN.equals(byteOrder)) {
            val = curData & MASKS[len];
            curData >>>= len;
        } else {
            val = (curData >> (bitLeft - len)) & MASKS[len];
        }
        bitLeft -= len;

        return val;
    }

    public boolean bitsAvailable() {
        ensureBuffer(1);
        return bitLeft > 0;
    }

}
