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
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

// TODO this is in very early stage of analysis
public final class ChunkWADecoder {

    private static final System.Logger LOG = System.getLogger(ChunkWADecoder.class.getName());

    private ChunkWADecoder() {
    }

    public static ByteString decode(final BasicChunk chunk) {
        final EnhancedByteBuffer bb = chunk.dataWithHeader().ebbLE();
        bb.skip(6);

        final int priority = bb.readU16();
        LOG.log(System.Logger.Level.DEBUG, "Priority: %d", priority);

        // PC speaker streams
        final int[] pcSpkStreams = new int[4];
        for (int i = 0; i < 4; i++) {
            pcSpkStreams[i] = bb.readU16();
            LOG.log(System.Logger.Level.DEBUG, "SpkStream[%d]: %d", i, pcSpkStreams[i]);
        }

        // PCjr / Tandy speaker streams
        final int[] pcjrSpkStreams = new int[4];
        for (int i = 0; i < 4; i++) {
            pcjrSpkStreams[i] = bb.readU16();
            LOG.log(System.Logger.Level.DEBUG, "PcJrSpkStream[%d]: %d", i, pcjrSpkStreams[i]);
        }

        // position at first stream
        Assert.assertEqual(bb.position(), pcSpkStreams[0]);

        return null;
    }

}
