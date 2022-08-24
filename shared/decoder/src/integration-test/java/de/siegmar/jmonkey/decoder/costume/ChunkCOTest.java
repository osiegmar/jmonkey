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

package de.siegmar.jmonkey.decoder.costume;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.ChunkUtil;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkCOTest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.CO)
    void readCO(final LecFile lecFile, final TreeIndex<LecChunk> coNode) {
        final TreeIndex<LecChunk> roNode = coNode.getParent()
            .findFirstBy(n -> n.chunk().type() == DataChunkType.RO)
            .orElseThrow();

        final ColorPalette colorPalette = ChunkUtil.readPAOfRoom(lecFile, roNode)
            .orElse(ColorPalette.EGA);

        final Costume costume = ChunkCODecoder.decode(lecFile.readChunk(coNode.chunk()), colorPalette);
        final int animations = costume.header().animOffsets().size();
        for (int anim = 0; anim < animations; anim++) {
            StandaloneCostumeAnimation.collectCostumeAnimation(costume, anim);
        }
    }

}
