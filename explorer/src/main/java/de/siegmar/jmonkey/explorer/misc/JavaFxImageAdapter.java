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

package de.siegmar.jmonkey.explorer.misc;

import de.siegmar.jmonkey.commons.misc.ImageAdapter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class JavaFxImageAdapter implements ImageAdapter<Image> {

    private WritableImage wi;
    private PixelWriter pixelWriter;

    @Override
    public void init(final int width, final int height) {
        wi = new WritableImage(width, height);
        pixelWriter = wi.getPixelWriter();
    }

    @Override
    public void setRGB(final int x, final int y, final int color) {
        pixelWriter.setArgb(x, y, color);
    }

    @Override
    public WritableImage getImage() {
        return wi;
    }
}
