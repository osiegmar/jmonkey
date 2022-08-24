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

package de.siegmar.jmonkey.explorer.model;

import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.font.Font;
import de.siegmar.jmonkey.font.FontGlyph;
import de.siegmar.jmonkey.font.FontRenderer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class FontGlyphView {

    private final Font font;
    private final FontGlyph fontGlyph;

    public FontGlyphView(final Font font, final FontGlyph fontGlyph) {
        this.font = font;
        this.fontGlyph = fontGlyph;
    }

    public int getCharNo() {
        return fontGlyph.asciiChar();
    }

    public ImageView getGlyph() {
        final WritableImage writableImage = new WritableImage(
            fontGlyph.width() + Math.abs(fontGlyph.xOffset()),
            fontGlyph.height() + Math.abs(fontGlyph.yOffset()));
        final PixelWriter pixelWriter = writableImage.getPixelWriter();

        final int startX = fontGlyph.xOffset() < 0 ? Math.abs(fontGlyph.xOffset()) : 0;
        final int startY = fontGlyph.yOffset() < 0 ? Math.abs(fontGlyph.yOffset()) : 0;

        new FontRenderer<Image>(pixelWriter::setArgb, font, startX, startY)
            .writeGlyph(fontGlyph, ColorPalette.EGA, 13);

        return new ImageView(writableImage);
    }

    public char getAsciiChar() {
        return (char) fontGlyph.asciiChar();
    }

    public int getWidth() {
        return fontGlyph.width();
    }

    public int getHeight() {
        return fontGlyph.height();
    }

    public int getXOffset() {
        return fontGlyph.xOffset();
    }

    public int getYOffset() {
        return fontGlyph.yOffset();
    }

}
