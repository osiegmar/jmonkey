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

package de.siegmar.jmonkey.decoder;

import java.awt.image.BufferedImage;

import de.siegmar.jmonkey.commons.misc.ImageAdapter;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;

public class BufferedImageAdapter implements ImageAdapter<BufferedImage> {

    private BufferedImage img;

    public static BufferedImage convert(final LayeredImage image) {
        final BufferedImageAdapter bia = new BufferedImageAdapter();
        image.writeTo(bia);
        return bia.getImage();
    }

    @Override
    public void init(final int width, final int height) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void setRGB(final int x, final int y, final int color) {
        img.setRGB(x, y, color);
    }

    @Override
    public BufferedImage getImage() {
        return img;
    }

}
