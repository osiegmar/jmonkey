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

package de.siegmar.jmonkey.commons.misc;

public class WritableRasterImage {

    protected final int width;
    protected final int height;
    protected int[][] pixels;

    public WritableRasterImage(final int width, final int height) {
        this(width, height, new int[height][width]);
    }

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    WritableRasterImage(final int width, final int height, final int[][] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public void draw(final int x, final int y, final int col) {
        pixels[y][x] = col;
    }

    public void transfer(final RasterImage src, final int x, final int y) {
        for (int sy = 0; sy < src.getHeight(); sy++) {
            for (int sx = 0; sx < src.getWidth(); sx++) {
                final int color = src.getColor(sx, sy);
                if (color != 0) {
                    pixels[sy + y][sx + x] = color;
                }
            }
        }
    }

    public RasterImage rasterImage() {
        return new RasterImage(width, height, pixels);
    }

    // TODO refactor
    public void drawPreviousColor(final int x, final int y) {
        pixels[y][x] = pixels[y][x - 1];
    }

    public void mirror() {
        final int[][] tmp = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tmp[y][x] = pixels[y][width - 1 - x];
            }
        }
        pixels = tmp;
    }

}
