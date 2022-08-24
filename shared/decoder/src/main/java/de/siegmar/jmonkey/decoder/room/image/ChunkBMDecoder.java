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

import java.util.Optional;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.ColorPalette;

public final class ChunkBMDecoder {

    private ChunkBMDecoder() {
    }

    public static Optional<LayeredImage> decode(final BasicChunk chunk, final ChunkHD chunkHD,
                                                final ColorPalette palette) {
        Assert.assertEqual(chunk.header().name(), "BM");

        if (chunkHD.height() == 0 && chunk.header().payloadLength() == 2) {
            return Optional.empty();
        }

        final ByteString imageChunk = chunk.data();

        return Optional.of(palette != null
            ? AbstractImageDecoder.decodeVGA(imageChunk, palette, chunkHD.width(), chunkHD.height())
            : AbstractImageDecoder.decodeEGA(imageChunk, ColorPalette.EGA, chunkHD.width(), chunkHD.height()));
    }

}
