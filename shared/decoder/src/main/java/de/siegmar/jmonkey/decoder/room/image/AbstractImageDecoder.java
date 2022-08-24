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

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.commons.misc.RasterImage;

public abstract class AbstractImageDecoder {

    protected final EnhancedByteBuffer bb;
    protected final ColorPalette palette;
    protected final int width;
    protected final int height;

    protected AbstractImageDecoder(final ByteString data, final ColorPalette palette,
                                   final int width, final int height) {

        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkArgument(width % 8 == 0);

        bb = data.ebbLE();
        this.palette = palette;
        this.width = width;
        this.height = height;
    }

    public static LayeredImage decodeVGA(final ByteString data, final ColorPalette palette,
                                         final int width, final int height) {

        return new ImageVGADecoder(data, palette, width, height).buildImg();
    }

    public static LayeredImage decodeEGA(final ByteString data, final ColorPalette palette,
                                         final int width, final int height) {

        return new ImageEGADecoder(data, palette, width, height).buildImg();
    }

    LayeredImage buildImg() {
        final int stripCnt = width / 8;

        final int zPlanesOffset = fetchOffset();

        final List<Integer> stripOffsets = decodeStripOffsets(stripCnt);

        final RasterImage imageLayer = decodeImage(stripOffsets);

        Assert.assertEqual(bb.position(), zPlanesOffset);

        final List<MaskLayer> zPlanes = decodeLayers(stripCnt, zPlanesOffset);

        return new LayeredImage(imageLayer, zPlanes);
    }

    private List<Integer> decodeStripOffsets(final int stripCnt) {
        final List<Integer> stripOffsets = new ArrayList<>(stripCnt);
        for (int strip = 0; strip < stripCnt; strip++) {
            stripOffsets.add(fetchOffset());
        }
        return stripOffsets;
    }

    protected abstract int fetchOffset();

    protected abstract RasterImage decodeImage(List<Integer> stripOffsets);

    protected List<MaskLayer> decodeLayers(final int stripCnt, final int zPlanesOffset) {
        if (!bb.hasRemaining()) {
            return List.of();
        }

        final List<List<Integer>> zPlanes = readZPlanes(stripCnt, zPlanesOffset);
        final List<MaskLayer> layers = new ArrayList<>(zPlanes.size());

        for (final List<Integer> zPlane : zPlanes) {
            final BuildMask maskLayer = new BuildMask();
            for (int strip = 0; strip < stripCnt; strip++) {
                addZplane(zPlane, strip, maskLayer);
            }
            layers.add(new MaskLayer(width, height, maskLayer.pixels));
        }

        return layers;
    }

    private List<List<Integer>> readZPlanes(final int stripCnt, final int zPlanesOffset) {
        final List<List<Integer>> zPlanes = new ArrayList<>();

        int offset = zPlanesOffset;
        do {
            bb.position(offset);

            final int len = bb.readU16();
            if (len == 0) {
                Assert.assertThat(!bb.hasRemaining());
                break;
            }

            final List<Integer> zPlane = new ArrayList<>(stripCnt);
            for (int strip = 0; strip < stripCnt; strip++) {
                final int relativeOffset = bb.readU16();
                zPlane.add(offset + relativeOffset);
            }
            zPlanes.add(zPlane);

            offset += len;
        } while (offset < bb.limit());

        return zPlanes;
    }

    private void addZplane(final List<Integer> zPlane,
                           final int currentStrip, final BuildMask img) {
        final int zPlaneOffset = zPlane.get(currentStrip);
        bb.position(zPlaneOffset);

        int len;

        int y = height;
        while (y > 0) {
            len = bb.readU8();

            if ((len & 0x80) != 0) {
                len &= 0x7F;

                // TODO check: there are OIs where we run out of data
//                final int mask = bb.readU8();
                final int mask = bb.hasRemaining() ? bb.readU8() : 0;

                do {
                    if (mask != 0) {
                        tintMask(height, img, currentStrip, y, mask);
                    }
                    --y;
                } while (--len > 0 && y > 0);
            } else {
                do {
                    final int mask = bb.readU8();
                    if (mask != 0) {
                        tintMask(height, img, currentStrip, y, mask);
                    }
                    --y;
                } while (--len > 0 && y > 0);
            }
        }
    }

    private static void tintMask(final int height, final BuildMask img, final int currentStrip,
                                 final int h, final int mask) {
        for (int p = 0; p < 8; p++) {
            if ((mask & (0x80 >>> p)) != 0) {
                img.draw(currentStrip * 8 + p, height - h, (byte) 1);
            }
        }
    }

    private final class BuildMask {

        private final byte[][] pixels = new byte[height][width];

        public void draw(final int x, final int y, final byte color) {
            pixels[y][x] = color;
        }

    }

}
