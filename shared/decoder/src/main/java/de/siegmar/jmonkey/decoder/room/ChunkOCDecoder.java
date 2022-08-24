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

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkOCDecoder {

    private ChunkOCDecoder() {
    }

    @SuppressWarnings("checkstyle:ExecutableStatementCount")
    public static ChunkOC decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "OC");

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int objectId = bb.readU16();

        // unknown byte - always 0
        final int unknown = bb.readU8();
        Assert.assertThat(unknown == 0);

        final int xPosition = bb.readU8() * 8;

        final int yPosAndState = bb.readU8();

        // TODO use parentState
        //final int parentState = yPosAndState & 0x80;

        final int yPosition = (yPosAndState & 0x7F) * 8;

        final int width = bb.readU8() * 8;
        final int parent = bb.readU8();
        final int walkX = bb.readU16();
        final int walkY = bb.readU16();

        final int heightAndActorDirection = bb.readU8();
        final int height = heightAndActorDirection & 0xF8;
        final int actorDirection = heightAndActorDirection & 0x07;

        final int nameOffset = bb.readU8();

        final Map<Integer, Integer> offsets = new LinkedHashMap<>();
        // offset table
        while (true) {
            final int verb = bb.readU8();
            if (verb == 0) {
                // 0-terminated
                break;
            }
            final int offset = bb.readU16();
            offsets.put(verb, offset);
        }

        Assert.assertEqual(bb.position(), nameOffset);

        final String name = bb.readNTS(Charset.forName("CP437"));

        if (offsets.isEmpty()) {
            Assert.assertEqual(bb.remaining(), 0);
        } else {
            final int firstOffset = offsets.values().stream().sorted().findFirst().get();
            Assert.assertEqual(bb.position(), firstOffset);
        }

        // need to keep the entire chunk because script addresses are relative to chunk begin
        final ByteString entireChunk = chunk.dataWithHeader();

        return new ChunkOC(objectId, xPosition, yPosition, width,
            parent, walkX, walkY, height, actorDirection,
            offsets, name, bb.position(), entireChunk);
    }

}
