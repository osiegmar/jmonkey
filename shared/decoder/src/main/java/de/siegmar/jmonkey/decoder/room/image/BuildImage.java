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

import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

final class BuildImage {

    private static final int STRIP_WIDTH = 8;
    private final ColorPalette palette;
    private final double height;
    private final WritableRasterImage writableRasterImage;
    private Codec.RenderingDirection renderingDirection;
    private int imgY;
    private int imgX;
    private int stripOffset;

    BuildImage(final ColorPalette palette, final double height, final WritableRasterImage writableRasterImage) {
        this.palette = palette;
        this.height = height;
        this.writableRasterImage = writableRasterImage;
    }

    public void setRenderingDirection(final Codec.RenderingDirection renderingDirection) {
        this.renderingDirection = renderingDirection;
    }

    public boolean draw(final int paletteIdx) {
        writableRasterImage.draw(STRIP_WIDTH * stripOffset + imgX, imgY, palette.color(paletteIdx));
        return renderingDirection == Codec.RenderingDirection.HORIZONTAL ? moveHorizontal() : moveVertical();
    }

    private boolean moveHorizontal() {
        if (++imgX == STRIP_WIDTH) {
            imgX = 0;
            if (++imgY == height) {
                imgY = 0;
                stripOffset++;
                return true;
            }
        }
        return false;
    }

    private boolean moveVertical() {
        if (++imgY == height) {
            imgY = 0;
            if (++imgX == STRIP_WIDTH) {
                imgX = 0;
                stripOffset++;
                return true;
            }
        }
        return false;
    }

}
