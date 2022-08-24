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

package de.siegmar.jmonkey.decoder.header;

import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkFODecoder {

    private ChunkFODecoder() {
    }

    public static ChunkFO decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "FO");
//        Assert.assertRange(chunk.header().length(), 7, 256);

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int lfCount = bb.readU8();
        final List<ChunkFOItem> items = new ArrayList<>(lfCount);

        for (int i = 0; i < lfCount; i++) {
            final int roomNo = bb.readU8();
            final int lfOffset = bb.readU32();
            items.add(new ChunkFOItem(roomNo, lfOffset));
        }

        Assert.assertEqual(bb.remaining(), 0);

        return new ChunkFO(items);
    }

}
