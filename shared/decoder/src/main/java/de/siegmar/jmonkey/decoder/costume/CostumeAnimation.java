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

package de.siegmar.jmonkey.decoder.costume;

import java.util.List;

import de.siegmar.jmonkey.commons.misc.RasterImage;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

public record CostumeAnimation(List<RasterImage> frames) {

    public CostumeAnimation {
        frames = List.copyOf(frames);
    }

    public List<RasterImage> drawAnimation() {
        return frames().stream().map(this::drawFrame).toList();
    }

    private RasterImage drawFrame(final RasterImage animationFrame) {
        final WritableRasterImage wimg = new WritableRasterImage(animationFrame.getWidth(), animationFrame.getHeight());
        for (int y = 0; y < animationFrame.getHeight(); y++) {
            for (int x = 0; x < animationFrame.getWidth(); x++) {
                final int color = animationFrame.getColor(x, y);
                if (color != 0) {
                    wimg.draw(x, y, color);
                }
            }
        }
        return wimg.rasterImage();
    }

}
