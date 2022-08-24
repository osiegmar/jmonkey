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
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkOCTest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.OC)
    void readOC(final LecFile lecFile, final TreeIndex<LecChunk> ocNode) {
        final ChunkOC chunkOC = ChunkOCDecoder.decode(lecFile.readChunk(ocNode.chunk()));
        assertThat(chunkOC.objectId()).isBetween(17, 996);
        assertThat(chunkOC.xPosition()).isBetween(0, 984);
        assertThat(chunkOC.yPosition()).isBetween(0, 192);
        assertThat(chunkOC.width()).isBetween(0, 640);
        assertThat(chunkOC.height()).isBetween(0, 144);
        assertThat(chunkOC.parent()).isBetween(0, 16);
        assertThat(chunkOC.walkX()).isBetween(0, 65526);
        assertThat(chunkOC.walkY()).isBetween(0, 199);
        assertThat(chunkOC.actorDirection()).isBetween(0, 3);
        assertThat(chunkOC.offsets()).isNotNull();
        assertThat(chunkOC.scriptStart()).isBetween(21, 107);
        assertThat(chunkOC.name()).isNotNull();
    }

}
