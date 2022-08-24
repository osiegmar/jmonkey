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

import java.io.ByteArrayOutputStream;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.decoder.header.ChunkFO;
import de.siegmar.jmonkey.decoder.header.ChunkFOItem;

public final class ChunkFOEncoder {

    private ChunkFOEncoder() {
    }

    public static BasicChunk encode(final ChunkFO chunk) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final LeWriter leWriter = new LeWriter(bos);

        leWriter.writeU8(chunk.items().size());

        for (final ChunkFOItem item : chunk.items()) {
            leWriter.writeU8(item.roomId());
            leWriter.writeU32(item.lfOffset());
        }

        return ChunkFO.wrap(ByteString.wrap(bos.toByteArray()));
    }

}
