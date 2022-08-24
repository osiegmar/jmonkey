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

package de.siegmar.jmonkey.decoder.room;

import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkNLDecoder {

    private ChunkNLDecoder() {
    }

    public static ChunkNL decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "NL");

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int size = bb.readU8();

        final List<Integer> items;
        if (size == 0) {
            items = List.of();
        } else {
            items = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(bb.readU8());
            }
        }

        Assert.assertEqual(bb.remaining(), 0);

        return new ChunkNL(items);
    }

}
