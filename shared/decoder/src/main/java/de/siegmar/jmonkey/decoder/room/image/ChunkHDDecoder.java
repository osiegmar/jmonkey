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

package de.siegmar.jmonkey.decoder.room.image;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkHDDecoder {

    private ChunkHDDecoder() {
    }

    public static ChunkHD decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "HD");
        Assert.assertEqual(chunk.header().payloadLength(), 6);

        final EnhancedByteBuffer bb = chunk.ebbLE();
        final int roomWidth = bb.readU16();
        final int roomHeight = bb.readU16();
        final int numObjects = bb.readU16();

        Assert.assertEqual(bb.remaining(), 0);

        return new ChunkHD(roomWidth, roomHeight, numObjects);
    }

}
