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

package de.siegmar.jmonkey.font;

import de.siegmar.jmonkey.commons.io.BasicChunkHeader;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;

public final class FontChunkHeader extends BasicChunkHeader {

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final FontChunkType type;

    private FontChunkHeader(final BasicChunkHeader basicChunkHeader, final FontChunkType type) {
        super(basicChunkHeader.length(), basicChunkHeader.name());
        this.type = type;
    }

    public static FontChunkHeader read(final EnhancedByteBuffer bb) {
        final BasicChunkHeader basicChunkHeader = bb.readChunkHeader();
        return new FontChunkHeader(basicChunkHeader, FontChunkType.of(basicChunkHeader.name()));
    }

    public FontChunkType type() {
        return type;
    }

}
