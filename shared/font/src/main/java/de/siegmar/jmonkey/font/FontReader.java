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

import static java.lang.System.Logger.Level.DEBUG;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

/**
 * Decoder for font files (90X.LFL).
 *
 * @see <a href="https://osiegmar.github.io/jmonkey/file-formats/font-files.html">Font file format specification</a>
 */
public final class FontReader {

    private static final System.Logger LOG = System.getLogger(FontReader.class.getName());

    private static final int GLYPH_HEADER_SIZE = 4;
    private static final int COLORMAP_LENGTH = 15;

    private final ByteString data;

    private FontReader(final ByteString data) {
        this.data = data;
    }

    public static Font decodeFont(final Path file) {
        final ByteString bs = ByteString.readFrom(file);

        LOG.log(DEBUG, () -> "Read font from %s (SHA-1 hash: %s)"
            .formatted(file, bs.toMD5()));

        return new FontReader(bs).decode();
    }

    private Font decode() {
        final var bb = data.ebbLE();
        final FontChunkHeader chunkHeader = FontChunkHeader.read(bb);

        // the length is 15 byte too short (probably they forgot to include the colormap)
        Assert.assertEqual(chunkHeader.length() + COLORMAP_LENGTH, data.size());

        final byte[] colorMap = bb.readBytes(COLORMAP_LENGTH);

        final int relativeOffset = bb.position();
        final int bitsPerPixel = bb.readU8();

        Assert.assertThat(bitsPerPixel == 1 || bitsPerPixel == 2);

        final int fontHeight = bb.readU8();
        final int numChars = bb.readU16();

        final int[] offsets = new int[numChars];
        for (int i = 0; i < numChars; i++) {
            final int offset = bb.readU32();
            if (offset != 0) {
                offsets[i] = relativeOffset + offset;
            }
        }

        final Map<Integer, FontGlyph> glyphs = new HashMap<>();

        for (int asciiChar = 0; asciiChar < numChars; asciiChar++) {
            final int offset = offsets[asciiChar];

            // 0 means no glyph for that glyphData
            if (offset != 0) {
                glyphs.put(asciiChar, readGlyph(bb, fontHeight, numChars, offsets, asciiChar, offset));
            }
        }

        Assert.assertThat(!bb.hasRemaining(),
            "buffer should be fully consumed, but still has %d bytes left",
            bb.remaining());

        final int maxWidth = glyphs.values().stream()
            .mapToInt(FontGlyph::width)
            .max().orElseThrow();

        return new Font(bitsPerPixel, fontHeight, maxWidth, ByteString.wrap(colorMap), glyphs);
    }

    private FontGlyph readGlyph(final EnhancedByteBuffer bb, final int fontHeight, final int numChars,
                                final int[] offsets, final int asciiChar, final int offset) {
        bb.position(offset);
        final int glyphWidth = bb.readU8();
        final int glyphHeight = bb.readU8();
        final int xOffset = bb.readS8();
        final int yOffset = bb.readS8();

        Assert.assertThat(glyphHeight <= fontHeight);

        int nextOffset = bb.limit();
        for (int y = asciiChar + 1; y < numChars; y++) {
            if (offsets[y] != 0) {
                nextOffset = offsets[y];
                break;
            }
        }

        final int datalen = nextOffset - offset - GLYPH_HEADER_SIZE;

        final ByteString glyphData = bb.readImmutableBytes(datalen);
        return new FontGlyph(asciiChar, glyphWidth, glyphHeight, xOffset, yOffset, glyphData);
    }

}
