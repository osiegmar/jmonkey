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

package de.siegmar.jmonkey.decoder.room.box;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

public final class ChunkBXDecoder {

    private ChunkBXDecoder() {
    }

    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static ChunkBX decode(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "BX");

        final EnhancedByteBuffer bb = chunk.ebbLE();

        final int nrOfBoxes = bb.readU8();

        final var boxes = new ArrayList<ChunkBXBox>(nrOfBoxes);
        for (int i = 0; i < nrOfBoxes; i++) {
            boxes.add(readBox(bb));
        }

        final var matrix = new ArrayList<ChunkBXMatrix>(nrOfBoxes);

        var route = new ArrayList<ChunkBXMatrixTravelRoute>();
        for (int row = 0; row < nrOfBoxes;) {
            final int start = bb.readU8();

            if (start == 0xff) {
                // TODO remove PMD.AvoidReassigningLoopVariables
                matrix.add(new ChunkBXMatrix(row++, route));
                route = new ArrayList<>();
                continue;
            }

            final int end = bb.readU8();
            final int dst = bb.readU8();
            route.add(new ChunkBXMatrixTravelRoute(start, end, dst));
        }

        Assert.assertThat(!bb.hasRemaining());
        Assert.assertThat(matrix.size() == nrOfBoxes,
            "Number of boxes %s does not match walk matrixes %s", nrOfBoxes, matrix.size());

        return new ChunkBX(boxes, matrix);
    }

    private static ChunkBXBox readBox(final EnhancedByteBuffer bx) {
        final int ulx = bx.readU16();
        final int uly = bx.readU16();
        final int urx = bx.readU16();
        final int ury = bx.readU16();
        final int lrx = bx.readU16();
        final int lry = bx.readU16();
        final int llx = bx.readU16();
        final int lly = bx.readU16();
        final int mask = bx.readU8();
        final int flags = bx.readU8();

        final int scaleTmp = bx.readU16();
        final int scale;
        final int scaleSlot;
        if ((scaleTmp & 0x8000) != 0) {
            scale = 0;
            scaleSlot = scaleTmp & 0x7FFF;
        } else {
            scale = scaleTmp;
            scaleSlot = 0;
        }

        return new ChunkBXBox(ulx, uly, urx, ury, lrx, lry, llx, lly, mask, flags, scale, scaleSlot);
    }

    public static void bxToImage(final ChunkBX chunkBX, final BufferedImage image) {
        final Graphics g = image.getGraphics();

        for (final ChunkBXBox box : chunkBX.boxes()) {
            final Polygon polygon = new Polygon(new int[]{
                box.ulx(),
                box.urx(),
                box.lrx(),
                box.llx(),
            }, new int[]{
                box.uly(),
                box.ury(),
                box.lry(),
                box.lly(),
            }, 4);

            g.drawPolygon(polygon);
        }

        g.drawImage(image, 0, 0, null);
    }

}
