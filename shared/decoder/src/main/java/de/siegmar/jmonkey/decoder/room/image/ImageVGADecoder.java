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

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.RasterImage;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

public class ImageVGADecoder extends AbstractImageDecoder {

    public ImageVGADecoder(final ByteString data, final ColorPalette palette,
                           final int width, final int height) {
        super(data, palette.roomPalette(), width, height);
    }

    @Override
    protected int fetchOffset() {
        return bb.readU32();
    }

    @Override
    public RasterImage decodeImage(final List<Integer> stripOffsets) {
        final WritableRasterImage img = new WritableRasterImage(width, height);
        final BuildImage imageLayer = new BuildImage(palette, height, img);

        int strip = 0;
        for (final Integer stripOffset : stripOffsets) {
            Assert.assertEqual(bb.position(), (long) stripOffset);

            // TODO slice data for this stripe only

            final int codec = bb.readU8();
            final Codec algo = Codec.of(codec);
            imageLayer.setRenderingDirection(algo.getDirection());

            switch (algo.getMethod()) {
                case UNCOMPRESSED -> UncompressedImageDecoder.decode(imageLayer, bb);
                case EGA -> EgaImageDecoder.decode(img, bb, height, palette, strip);
                case METHOD_1 -> Method1ImageDecoder.decode(imageLayer, algo.getPaletteBitLength(), bb);
                default -> throw new IllegalStateException("Unsupported compression method: " + algo.getMethod());
            }

            strip++;
        }

        return img.rasterImage();
    }

}
