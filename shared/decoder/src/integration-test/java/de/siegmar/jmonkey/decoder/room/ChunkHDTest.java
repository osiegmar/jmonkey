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

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.ChunkHDDecoder;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkHDTest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.HD)
    void readHD(final LecFile lecFile, final TreeIndex<LecChunk> hdNode) {
        final ChunkHD chunkHD = ChunkHDDecoder.decode(lecFile.readChunk(hdNode.chunk()));

        if (!"DISK09.LEC".equalsIgnoreCase(lecFile.toString())) {
            // TODO analyze DISK09 PC EGA EN
            assertThat(chunkHD.width()).isBetween(1, 6371);
        } else {
            assertThat(chunkHD.width()).isBetween(1, 1280);
        }
        assertThat(chunkHD.height()).isBetween(0, 200);
        assertThat(chunkHD.noOfObjects()).isBetween(0, 63);
    }

}
