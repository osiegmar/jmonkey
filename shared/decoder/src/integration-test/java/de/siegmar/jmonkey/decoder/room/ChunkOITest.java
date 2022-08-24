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

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.BufferedImageAdapter;
import de.siegmar.jmonkey.decoder.ChunkUtil;
import de.siegmar.jmonkey.decoder.ImageUtil;
import de.siegmar.jmonkey.decoder.room.image.ChunkOIDecoder;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkOITest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.OI)
    void readOI(final LecFile lecFile, final TreeIndex<LecChunk> oiNode) {
        final ColorPalette colorPalette = ChunkUtil.readPAOfRoom(lecFile, oiNode.getParent())
            .orElse(null);

        final ObjectImageMeta objectImageMeta =
            ChunkOIDecoder.decodeImageMeta(lecFile.readChunk(oiNode.chunk()));

        final Optional<ChunkOC> chunkOC =
            ChunkUtil.readOCOfRoom(lecFile, oiNode.getParent(), objectImageMeta.objectId());

        if (chunkOC.isPresent()) {
            final Optional<BufferedImage> image =
                ChunkOIDecoder.decodeImage(lecFile.readChunk(oiNode.chunk()), chunkOC.get(), colorPalette)
                    .map(BufferedImageAdapter::convert);

            image.ifPresent(bufferedImage -> ImageUtil.write(bufferedImage, "oi_" + oiNode.chunk().pos()));
        }
    }

}
