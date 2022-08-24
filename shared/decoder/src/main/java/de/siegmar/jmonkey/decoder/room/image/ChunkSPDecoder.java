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
import de.siegmar.jmonkey.commons.misc.ColorPalette;

public final class ChunkSPDecoder {

    private ChunkSPDecoder() {
    }

    public static ColorPalette readPalette(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "SP");

        if (chunk.header().payloadLength() == 0) {
            // SP chunk exists but is empty in Amiga version
            return ColorPalette.EMPTY;
        }

        Assert.assertEqual(chunk.header().payloadLength(), 256);

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int[] palette = new int[256];
        for (int i = 0; i < 256; i++) {
            palette[i] = bb.readU8();
        }

        Assert.assertThat(!bb.hasRemaining());

        return new ColorPalette(palette);
    }

}
