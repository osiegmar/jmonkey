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
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ObjectImageMeta;

public final class ChunkOIDecoder {

    private ChunkOIDecoder() {
    }

    public static ObjectImageMeta decodeImageMeta(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "OI");

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int objectId = bb.readU16();
        return new ObjectImageMeta(objectId);
    }

    public static Optional<LayeredImage> decodeImage(final BasicChunk chunk, final ChunkOC chunkOC,
                                                     final ColorPalette palette) {

        Assert.assertEqual(chunk.header().name(), "OI");

        if (chunk.header().payloadLength() == 2) {
            return Optional.empty();
        }

        final ByteString imageChunk = chunk.data().slice(2);

        final LayeredImage image = palette != null
            ? AbstractImageDecoder.decodeVGA(imageChunk, palette, chunkOC.width(), chunkOC.height())
            : AbstractImageDecoder.decodeEGA(imageChunk, ColorPalette.EGA, chunkOC.width(), chunkOC.height());

        return Optional.of(image);
    }

}
