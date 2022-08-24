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

public class MaskLayer {

    private final int width;
    private final int height;
    private final byte[][] pixels;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    public MaskLayer(final int width, final int height, final byte[][] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public <T> void writeTo(final ImageAdapter<T> imageAdapter, final int setColor, final int unsetColor) {
        // FIXME hack (?)
        imageAdapter.init(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageAdapter.setRGB(x, y, pixels[y][x] == 1 ? setColor : unsetColor);
            }
        }
    }

    public boolean isSet(final int x, final int y) {
        return pixels[y][x] == 1;
    }

}
