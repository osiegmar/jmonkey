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

package de.siegmar.jmonkey.commons.misc;

import java.util.Arrays;

public class ColorPalette {

    public static final ColorPalette EMPTY = new ColorPalette(new int[256]);

    public static final ColorPalette EGA = new ColorPalette(new int[]{
        0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA,
        0xFFAA0000, 0xFFAA00AA, 0xFFAA5500, 0xFFAAAAAA,
        0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF,
        0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF,
    });

    private static final int AMIGA_PAL_SIZE = 32;

    private final int[] palette;

    public ColorPalette(final int[] palette) {
        this.palette = palette.clone();
    }

    public int color(final int pos) {
        return palette[pos];
    }

    public int size() {
        return palette.length;
    }

    public int[] getPalette() {
        return palette.clone();
    }

    public ColorPalette roomPalette() {
        if (palette.length == AMIGA_PAL_SIZE) {
            return new ColorPalette(Arrays.copyOfRange(palette, 16, AMIGA_PAL_SIZE));
        }
        return this;
    }

}
