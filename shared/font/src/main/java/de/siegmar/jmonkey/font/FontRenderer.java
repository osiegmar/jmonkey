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

import java.awt.Dimension;
import java.nio.ByteOrder;

import de.siegmar.jmonkey.commons.io.BitBuffer;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.ImageAdapter;

/**
 * Renderer for fonts.
 *
 * @param <T> Image type
 * @see FontReader
 */
public class FontRenderer<T> {

    private final ImageAdapter<T> image;
    private final Font font;
    private final int startX;
    private int curX;
    private int curY;

    public FontRenderer(final ImageAdapter<T> image, final Font font, final int startX, final int startY) {
        this.image = image;
        this.font = font;
        this.startX = startX;
        curX = startX;
        curY = startY;
    }

    public static Dimension calcDimension(final Font font, final byte[] text) {
        int width = 0;
        int height = 0;
        for (final byte b : text) {
            final int c = b & 0xff;
            if (c == '@') {
                break;
            }

            final FontGlyph glyph = font.glyphs().get(c);
            width += glyph.width() + glyph.xOffset();
            height = Math.max(height, glyph.height() + glyph.yOffset());
        }

        return new Dimension(width, height);
    }

    public void write(final ColorPalette palette, final int color, final byte[] text, final int x, final int y) {
        curX = x;
        curY = y;
        for (final byte b : text) {
            final int c = b & 0xff;
            if (c == '@') {
                break;
            }
            appendChar(palette, color, c);
        }
    }

    public void appendChar(final ColorPalette palette, final int color, final int c) {
        curX += writeChar(palette, color, c);
    }

    public int writeChar(final ColorPalette palette, final int color, final int c) {
        return writeGlyph(font.glyphs().get(c), palette, color);
    }

    public int writeGlyph(final FontGlyph glyph, final ColorPalette palette, final int color) {
        final BitBuffer bitb = glyph.glyphData().ebb(ByteOrder.BIG_ENDIAN).bitstream();
        for (int y = 0; y < glyph.height(); y++) {
            for (int x = 0; x < glyph.width(); x++) {
                final int pixelValue = bitb.readBits(font.bitsPerPixel());

                // pixelValue 0 is transparent (don't draw), 1 is color, 2 is shadow
                if (pixelValue > 0) {
                    final int fontPaletteIdx = pixelValue == 1 ? color : pixelValue;
                    final int pixelColor = palette.color(font.colors().get(fontPaletteIdx - 1));

                    image.setRGB(
                        curX + x + glyph.xOffset(),
                        curY + y + glyph.yOffset(),
                        pixelColor);
                }
            }
        }
        return glyph.width() + glyph.xOffset();
    }

    public void addSpace(final int space) {
        curX += space;
    }

    public void newLine() {
        curX = startX;
        curY += font.fontHeight() + 3;
    }

}
