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

import java.util.List;

import de.siegmar.jmonkey.commons.misc.ImageAdapter;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.commons.misc.RasterImage;

public class LayeredImage {

    private final RasterImage image;
    private final List<MaskLayer> masks;

    public LayeredImage(final RasterImage image, final List<MaskLayer> masks) {
        this.image = image;
        this.masks = List.copyOf(masks);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public List<MaskLayer> getMasks() {
        return masks;
    }

    public <T> void writeTo(final ImageAdapter<T> imageAdapter) {
        image.writeTo(imageAdapter);
    }

    public <T> void writeTo(final ImageAdapter<T> imageAdapter, final MaskLayer maskLayer) {
        image.writeTo(imageAdapter, maskLayer);
    }

}
