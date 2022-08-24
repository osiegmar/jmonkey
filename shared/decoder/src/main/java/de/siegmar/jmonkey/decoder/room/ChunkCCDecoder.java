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

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkCCDecoder {

    private ChunkCCDecoder() {
    }

    public static ChunkCC decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "CC");
        Assert.assertEqual(chunk.header().payloadLength(), 64);

        final var bb = chunk.ebbLE();

        final var list = new ArrayList<ColorCycle>();
        while (bb.hasRemaining()) {
            final int freq = bb.bigEndian().readU16();
            final int start = bb.readU8();
            final int end = bb.readU8();

            // TODO why 0x0aaa ???
            if (freq != 0 && freq != 0x0aaa && start < end) {
                // TODO why 16384 ???
                final int delay = 16384 / freq;

                list.add(new ColorCycle(delay, start, end));
            }
        }

        return new ChunkCC(list);
    }

}
