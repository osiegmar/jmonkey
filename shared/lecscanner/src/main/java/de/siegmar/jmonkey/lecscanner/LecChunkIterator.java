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

package de.siegmar.jmonkey.lecscanner;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import de.siegmar.jmonkey.commons.io.BasicChunkHeader;
import de.siegmar.jmonkey.commons.io.DataChunkType;

public class LecChunkIterator implements Iterator<LecChunk>, Closeable {

    private static final Set<String> KNOWN_TYPES = Arrays.stream(DataChunkType.values())
        .map(Enum::name).collect(Collectors.toSet());

    private final LecFile lecFile;
    private boolean initialized;
    private int nextPos;
    private LecChunk nextChunk;

    public LecChunkIterator(final LecFile lecFile) {
        this.lecFile = lecFile;
    }

    @Override
    public boolean hasNext() {
        if (!initialized) {
            nextChunk = fetch();
            initialized = true;
        }
        return nextChunk != null;
    }

    private LecChunk fetch() {
        if (nextPos >= lecFile.getSize()) {
            return null;
        }

        final int startPos = nextPos;

        final BasicChunkHeader basicChunkHeader;
        final DataChunkType dataChunkType;
        basicChunkHeader = lecFile.readData(nextPos, BasicChunkHeader.HEADER_SIZE)
            .ebbLE().readChunkHeader();
        nextPos += BasicChunkHeader.HEADER_SIZE;

        if ("RO".equals(basicChunkHeader.name())) {
            // let's see if this is a ROom (container) or ROland MIDI (data) node
            final String peekChunkName = lecFile.readData(nextPos, BasicChunkHeader.HEADER_SIZE)
                .ebbLE().readAscii(4, 2);

            // A ROom container is always followed by an HD chunk
            dataChunkType = KNOWN_TYPES.contains(peekChunkName)
                ? DataChunkType.valueOf(basicChunkHeader.name())
                : DataChunkType.ROL;
        } else {
            dataChunkType = DataChunkType.valueOf(basicChunkHeader.name());
        }

        // fast-forward to the end of the current chunks
        if (dataChunkType == DataChunkType.LF) {
            // LF is a mixed node (container but starts with UINT16 room number)
            nextPos += 2;
        } else if (dataChunkType.isDataOnly()) {
            nextPos += basicChunkHeader.payloadLength();
        }

        return new LecChunk(startPos, basicChunkHeader.length(), dataChunkType);
    }

    @Override
    public LecChunk next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final LecChunk current = nextChunk;
        nextChunk = fetch();
        return current;
    }

    @Override
    public void close() throws IOException {
        lecFile.close();
    }

}
