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

import de.siegmar.jmonkey.commons.io.BitBuffer;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;

public final class Method1ImageDecoder {

    private Method1ImageDecoder() {
    }

    @SuppressWarnings("PMD.EmptyIfStmt")
    public static void decode(final BuildImage img, final int paletteBitLength, final EnhancedByteBuffer bb) {
        int paletteIndex = bb.readU8();
        img.draw(paletteIndex);

        int subtractionValue = 1;

        final BitBuffer bis = bb.bitstream();

        do {
            if (!bis.readBit()) {
                // 0: Draw next pixel with current palette index.
            } else if (!bis.readBit()) {
                // 10: Read a new palette index from the bit stream, i.e., read the number of bits that the
                // parameter specifies as a value (see the Tiny Bits of Decompression chapter).
                // Set the subtraction variable to 1, and draw the next pixel.
                paletteIndex = bis.readBits(paletteBitLength) & 0xff;
                subtractionValue = 1;
            } else if (!bis.readBit()) {
                // 110: Subtract the subtraction variable from the palette index, and draw the next pixel.
                paletteIndex -= subtractionValue;
            } else {
                // 111: Negate the subtraction variable (i.e., if it's 1, change it to -1, if it's -1,
                // change it to 1). Subtract it from the palette index, and draw the next pixel.
                subtractionValue = -subtractionValue;
                paletteIndex -= subtractionValue;
            }
        } while (!img.draw(paletteIndex));
    }

}
