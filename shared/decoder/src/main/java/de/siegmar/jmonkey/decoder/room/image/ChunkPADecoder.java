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

public final class ChunkPADecoder {

    private static final int ALPHA = 0xFF_00_00_00;

    private ChunkPADecoder() {
    }

    public static ColorPalette readPalette(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "PA");

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int size = bb.readU16();

        // PC-VGA comes with 256 colors, Amiga with 32 colors
        Assert.assertThat(size == 3 * 256 || size == 3 * 32);

        final int colors = size / 3;
        final int[] palette = new int[colors];
        for (int i = 0; i < colors; i++) {
            final int r = bb.readU8();
            final int g = bb.readU8();
            final int b = bb.readU8();

            palette[i] = ALPHA | r << 16 | g << 8 | b;
        }

        return new ColorPalette(palette);
    }

}
