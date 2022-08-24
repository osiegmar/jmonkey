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
import de.siegmar.jmonkey.commons.lang.Assert;

/**
 * Decoder for SA chunks. These are used for scaling actors.
 *
 * Format as <a href="https://github.com/osiegmar/monkey-island/wiki/Data-files">documented</a>:
 *
 * <pre>{@code
 * data-sa             = *4sa-scale-slot
 * sa-scale-slot       = 2( sa-scale sa-y )
 * sa-scale            = UINT16
 * sa-y                = UINT16
 * }</pre>
 */
public final class ChunkSADecoder {

    private ChunkSADecoder() {
    }

    public static ChunkSA decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "SA");

        // Either this block is empty
        if (chunk.header().payloadLength() == 0) {
            return new ChunkSA(List.of());
        }

        final var bb = chunk.ebbLE();

        // ...or it contains 4 scale slots
        Assert.assertEqual(bb.remaining(), 32);

        final var list = new ArrayList<ScaleSlot>(4);
        while (bb.hasRemaining()) {
            list.add(new ScaleSlot(bb.readU16(), bb.readU16(), bb.readU16(), bb.readU16()));
        }

        return new ChunkSA(list);
    }

}
