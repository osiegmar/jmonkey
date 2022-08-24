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

package de.siegmar.jmonkey.decoder.room;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.ImageAdapter;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.decoder.BufferedImageAdapter;
import de.siegmar.jmonkey.decoder.ChunkUtil;
import de.siegmar.jmonkey.decoder.ImageUtil;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ChunkBMTest {

    @ParameterizedTest
    @LecChunkSource(DataChunkType.BM)
    void readBM(final LecFile lecFile, final TreeIndex<LecChunk> bmNode) {
        final TreeIndex<LecChunk> roNode = bmNode.getParent();

        final ChunkHD chunkHD = ChunkUtil.readHDOfRoom(lecFile, roNode)
            .orElseThrow();

        final ColorPalette colorPalette = ChunkUtil.readPAOfRoom(lecFile, roNode)
            .orElse(null);

        final Optional<LayeredImage> img =
            ChunkBMDecoder.decode(lecFile.readChunk(bmNode.chunk()), chunkHD, colorPalette);

        final int roomPos = roNode.chunk().pos();

        if (img.isPresent()) {
            // write image
            final BufferedImageAdapter image = new BufferedImageAdapter();
            img.get().writeTo(image);
            ImageUtil.write(image.getImage(), "bm_" + roomPos);

            // write layers
            final LayeredImage layeredImg = img.get();
            ImageUtil.write(toImg(layeredImg), "bmlayers_" + roomPos + "_0");
            final Iterator<MaskLayer> it = layeredImg.getMasks().iterator();
            int i = 0;
            while (it.hasNext()) {
                final BufferedImage layeredImage = maskToImg(it.next(), 0xFFFFFFFF);
                ImageUtil.write(layeredImage, "bmlayers_" + roomPos + "_" + i++);
            }

            // write blend image
            final BufferedImage blendImage = blend(img.get());
            ImageUtil.write(blendImage, "bmlayered_" + roomPos);
        }
    }

    private BufferedImage blend(final LayeredImage image) {
        final List<MaskLayer> layers = image.getMasks();
        final Iterator<MaskLayer> iterator = layers.iterator();

        final BufferedImage bi = toImg(image);
        final Graphics2D graphics = bi.createGraphics();

        final Iterator<Integer> colIt = List.of(0xF0FF0000, 0xF000FF00, 0xF00000FF).iterator();

        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                final MaskLayer l = iterator.next();
                graphics.setComposite(AlphaComposite.DstAtop);
                graphics.drawImage(maskToImg(l, colIt.next()), 0, 0, null);
            }

            graphics.dispose();
        }

        return bi;
    }

    private BufferedImage toImg(final LayeredImage image) {
        final BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        image.writeTo((ImageAdapter<BufferedImage>) bi::setRGB);
        return bi;
    }

    private BufferedImage maskToImg(final MaskLayer imageLayer, final int col) {
        final BufferedImageAdapter adapter = new BufferedImageAdapter();
        imageLayer.writeTo(adapter, col, 0xFF000000);
        return adapter.getImage();
    }

}
