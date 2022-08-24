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

package de.siegmar.jmonkey.commons.io;

import java.nio.charset.StandardCharsets;

public class BasicChunkHeader {

    public static final int HEADER_SIZE = 6;

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final int length;
    private final String type;

    public BasicChunkHeader(final int length, final String type) {
        this.length = length;
        this.type = type;
    }

    public static BasicChunkHeader ofData(final String type, final ByteString data) {
        return new BasicChunkHeader(HEADER_SIZE + data.size(), type);
    }

    public int payloadLength() {
        return length - HEADER_SIZE;
    }

    // TODO
    public int length() {
        return length;
    }

    // TODO RENAME to type
    public String name() {
        return type;
    }

    public ByteString toByteString() {
        return new ByteStringBuilder()
            .appendU32(length)
            .append(type.getBytes(StandardCharsets.US_ASCII))
            .build();
    }

}
