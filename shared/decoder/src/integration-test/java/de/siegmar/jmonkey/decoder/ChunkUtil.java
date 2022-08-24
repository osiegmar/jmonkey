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

package de.siegmar.jmonkey.decoder;

import java.util.Optional;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ChunkOCDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.ChunkHDDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkPADecoder;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;

public final class ChunkUtil {

    private ChunkUtil() {
    }

    public static Optional<ColorPalette> readPAOfRoom(final LecFile lecFile, final TreeIndex<LecChunk> roNode) {
        Preconditions.checkArgument(roNode.chunk().type() == DataChunkType.RO);

        return roNode
            .findFirstBy(n -> n.chunk().type() == DataChunkType.PA)
            .map(n -> ChunkPADecoder.readPalette(lecFile.readChunk(n.chunk())));
    }

    public static Optional<ChunkHD> readHDOfRoom(final LecFile lecFile, final TreeIndex<LecChunk> roNode) {
        Preconditions.checkArgument(roNode.chunk().type() == DataChunkType.RO);

        return roNode
            .findFirstBy(n -> n.chunk().type() == DataChunkType.HD)
            .map(n -> ChunkHDDecoder.decode(lecFile.readChunk(n.chunk())));
    }

    public static Optional<LayeredImage> readBMOfRoom(final LecFile lecFile, final TreeIndex<LecChunk> roNode) {
        Preconditions.checkArgument(roNode.chunk().type() == DataChunkType.RO);

        final Optional<TreeIndex<LecChunk>> bmNode = roNode
            .findFirstBy(n -> n.chunk().type() == DataChunkType.BM);

        if (bmNode.isEmpty()) {
            return Optional.empty();
        }

        final ChunkHD chunkHD = roNode
            .findFirstBy(n -> n.chunk().type() == DataChunkType.HD)
            .map(n -> ChunkHDDecoder.decode(lecFile.readChunk(n.chunk())))
            .orElseThrow();

        return ChunkBMDecoder.decode(lecFile.readChunk(bmNode.get().chunk()), chunkHD,
            readPAOfRoom(lecFile, roNode).orElse(null));
    }

    public static Optional<ChunkOC> readOCOfRoom(final LecFile lecFile, final TreeIndex<LecChunk> roNode,
                                                 final int objectId) {
        Preconditions.checkArgument(roNode.chunk().type() == DataChunkType.RO);

        return roNode.deepStream()
            .filter(c -> c.chunk().type() == DataChunkType.OC)
            .map(c -> ChunkOCDecoder.decode(lecFile.readChunk(c.chunk())))
            .filter(oc -> oc.objectId() == objectId)
            .filter(oc -> oc.height() > 0)
            .findFirst();
    }

}
