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

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.testhelper.PathType;
import de.siegmar.jmonkey.testhelper.PathsProvider;

class FontRendererTest {

    @ParameterizedTest
    @PathsProvider(PathType.FONT)
    void decodeFont(final Path file) {
        final Font font = FontReader.decodeFont(file);
        assertThat(font.bitsPerPixel()).isNotNegative();
        assertThat(font.fontHeight()).isNotNegative();
        assertThat(font.maxWidth()).isNotNegative();
        assertThat(font.colors()).isNotNull();
        assertThat(font.glyphs()).isNotEmpty();
    }

    @ParameterizedTest
    @PathsProvider(PathType.FONT)
    void useFont(final Path file) throws IOException {
        final Font font = FontReader.decodeFont(file);

        final BufferedImageAdapter imageAdapter = new BufferedImageAdapter();
        imageAdapter.init(640, 480);

        final FontRenderer<BufferedImage> fontRenderer = new FontRenderer<>(imageAdapter, font, 10, 10);

        final int charsPerRow = (int) Math.ceil(Math.sqrt(font.glyphs().keySet().size()));
        int charNum = 0;
        for (final Integer character : font.glyphs().keySet()) {
            fontRenderer.writeChar(ColorPalette.EGA, 15, character);
            fontRenderer.addSpace(font.maxWidth() + 1);

            if (charNum == charsPerRow) {
                charNum = 0;
                fontRenderer.newLine();
            }
            charNum++;
        }

        ImageIO.write(imageAdapter.getImage(), "png", OutputStream.nullOutputStream());
    }

}
