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

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.decoder.BufferedImageAdapter;
import de.siegmar.jmonkey.decoder.ChunkUtil;
import de.siegmar.jmonkey.decoder.room.box.ChunkBX;
import de.siegmar.jmonkey.decoder.room.box.ChunkBXDecoder;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkBXTest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.BX)
    void readBX(final LecFile lecFile, final TreeIndex<LecChunk> bxNode) throws IOException {
        final ChunkBX chunkBX = ChunkBXDecoder.decode(lecFile.readChunk(bxNode.chunk()));
        assertThat(chunkBX.boxes()).hasSizeBetween(0, 61);
        assertThat(chunkBX.matrixes()).hasSizeBetween(0, 61);

        final Optional<BufferedImage> optImage =
            ChunkUtil.readBMOfRoom(lecFile, bxNode.getParent())
                .map(BufferedImageAdapter::convert);

        if (optImage.isPresent()) {
            final BufferedImage image = optImage.get();
            ChunkBXDecoder.bxToImage(chunkBX, image);
            ImageIO.write(image, "png", OutputStream.nullOutputStream());
//                ImageIO.write(image, "png", new File("/tmp/bx/%s.png".formatted(boxNode.chunk().pos())));
        }
    }

}
