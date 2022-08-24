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

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

public final class EgaImageDecoder {

    private EgaImageDecoder() {
    }

    @SuppressWarnings({
        "checkstyle:NestedIfDepth",
        "checkstyle:ExecutableStatementCount",
        "checkstyle:CyclomaticComplexity"
    })
    public static void decode(final WritableRasterImage imageLayer, final EnhancedByteBuffer bb,
                              final int height, final ColorPalette palette, final int stripe) {
        int color;
        int run;
        int x = 0;
        int y = 0;
        int z;

        while (x < 8) {
            color = bb.readU8();

            if ((color & 0x80) != 0) {
                run = color & 0x3f;

                if ((color & 0x40) != 0) {
                    color = bb.readU8();

                    if (run == 0) {
                        run = bb.readU8();
                    }
                    for (z = 0; z < run; z++) {
                        final int col;
                        if ((z & 1) != 0) {
                            col = color & 0xf;
                        } else {
                            col = color >> 4;
                        }

                        imageLayer.draw(stripe * 8 + x, y, palette.color(col));

                        y++;
                        if (y >= height) {
                            y = 0;
                            x++;
                        }
                    }
                } else {
                    if (run == 0) {
                        run = bb.readU8();
                    }

                    for (z = 0; z < run; z++) {
                        imageLayer.drawPreviousColor(stripe * 8 + x, y);

                        y++;
                        if (y >= height) {
                            y = 0;
                            x++;
                        }
                    }
                }
            } else {
                run = color >> 4;
                if (run == 0) {
                    run = bb.readU8();
                }

                for (z = 0; z < run; z++) {
                    imageLayer.draw(stripe * 8 + x, y, palette.color(color & 0xf));

                    y++;
                    if (y >= height) {
                        y = 0;
                        x++;
                    }
                }
            }
        }
    }

}
