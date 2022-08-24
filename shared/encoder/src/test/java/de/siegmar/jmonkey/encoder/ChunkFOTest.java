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

package de.siegmar.jmonkey.encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.decoder.header.ChunkFO;
import de.siegmar.jmonkey.decoder.header.ChunkFODecoder;
import de.siegmar.jmonkey.decoder.header.ChunkFOItem;

class ChunkFOTest {

    @Test
    void chunkFO() {
        final ChunkFO chunkFO = new ChunkFO(List.of(
            new ChunkFOItem(1, 2),
            new ChunkFOItem(10, 20)
        ));
        final BasicChunk encodedChunkFO = ChunkFOEncoder.encode(chunkFO);

        assertEquals(chunkFO, ChunkFODecoder.decode(encodedChunkFO));
    }

}
