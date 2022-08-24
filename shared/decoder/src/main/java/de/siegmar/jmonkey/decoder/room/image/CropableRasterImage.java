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

import de.siegmar.jmonkey.commons.misc.RasterImage;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

public final class CropableRasterImage extends WritableRasterImage {

    private boolean dirty;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    // TODO use flexible width / height - don't pass in
    public CropableRasterImage(final int width, final int height) {
        super(width, height);
    }

    @Override
    public void draw(final int x, final int y, final int col) {
        super.draw(x, y, col);
        dirty = true;
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    public RasterImage crop() {
        final int maxWidth = maxX - minX + 1;
        final int maxHeight = maxY - minY + 1;

        if (!dirty || minX == 0 && minY == 0 && width == maxWidth && height == maxHeight) {
            return rasterImage();
        }

        final int[][] tmp = new int[maxHeight][maxWidth];
        for (int y = 0; y < maxHeight; y++) {
            final int sourceY = minY + y;
            for (int x = 0; x < maxWidth; x++) {
                tmp[y][x] = pixels[sourceY][minX + x];
            }
        }

        return new RasterImage(maxWidth, maxHeight, tmp);
    }

}
