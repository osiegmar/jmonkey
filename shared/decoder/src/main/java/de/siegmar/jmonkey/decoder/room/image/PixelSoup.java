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

package de.siegmar.jmonkey.decoder.room.image;

import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.RasterImage;

public class PixelSoup {

    private final List<Integer> pixels = new ArrayList<>(256);
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    public void draw(final int x, final int y, final int color) {
        Assert.assertThat(x <= 0xFF);
        Assert.assertThat(y <= 0xFF);
        Assert.assertThat(color <= 0xFF);

        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);

        pixels.add(toPixel(x, y, color));
    }

    public RasterImage toRasterImage() {
        final int height = maxY - minY;
        final int width = maxX - minX;
        final int[][] pixelArray = new int[height][width];
        for (final int pixel : pixels) {
            final int x = pixel >> 16;
            final int y = pixel >> 8 & 0x00FF;
            final int color = pixel & 0x0000FF;
            pixelArray[y][x] = color;
        }
        return new RasterImage(width, height, pixelArray);
    }

    private static int toPixel(final int x, final int y, final int color) {
        return x << 16 | y << 8 | color;
    }

}
