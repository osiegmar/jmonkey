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

import org.junit.jupiter.api.Test;

class ByteStringTest {

    @Test
    void simple() {
        final byte[] data = {1, 2, 3};
        final ByteString bs = ByteString.wrap(data);
        assertEquals(3, bs.size());
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], bs.get(i));
        }
    }

    @Test
    void slicePos() {
        final byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        final var sliced = ByteString.wrap(data).slice(8);
        assertEquals(2, sliced.size());
        assertArrayEquals(new byte[]{8, 9}, sliced.dumpCopy());
    }

    @Test
    void slicePosLen() {
        final byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        ByteString sliced = ByteString.wrap(data).slice(2, 5);
        assertEquals(5, sliced.size());
        assertArrayEquals(new byte[]{2, 3, 4, 5, 6}, sliced.dumpCopy());

        sliced = sliced.slice(1, 2);
        assertEquals(2, sliced.size());
        assertArrayEquals(new byte[]{3, 4}, sliced.dumpCopy());
        assertEquals(3, sliced.get(0));
        assertEquals(4, sliced.get(1));
    }

    @Test
    void limit() {
        final byte[] data = {0, 1, 2};

        assertArrayEquals(new byte[]{2}, ByteString.wrap(data).slice(2, 1).dumpCopy());
        assertThrows(IndexOutOfBoundsException.class, () -> spotBugsIgnore(ByteString.wrap(data).slice(2, 2)));
    }

    private void spotBugsIgnore(final Object obj) {
        if (obj != null) {
            throw new IllegalStateException("Object is not null: " + obj);
        }
    }

}
