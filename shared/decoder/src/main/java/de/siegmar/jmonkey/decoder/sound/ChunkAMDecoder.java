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

package de.siegmar.jmonkey.decoder.sound;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

/**
 * Decode Amiga AM sound chunk.
 *
 * See also https://github.com/scummvm/scummvm/blob/v2.5.1/engines/scumm/players/player_v4a.cpp.
 */
public final class ChunkAMDecoder {

    private ChunkAMDecoder() {
    }

    public static ChunkAM decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "AM");
        Assert.assertEqual(chunk.header().payloadLength(), 4);

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int type = bb.readU8();

        // 2 unknown bytes
        bb.skip(2);

        final int songNo = bb.readU8();

        return new ChunkAM(type, songNo);
    }

}
