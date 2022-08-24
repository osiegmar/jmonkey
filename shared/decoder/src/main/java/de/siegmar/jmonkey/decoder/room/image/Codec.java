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

import static de.siegmar.jmonkey.decoder.room.image.Codec.CompressionMethod.EGA;
import static de.siegmar.jmonkey.decoder.room.image.Codec.CompressionMethod.METHOD_1;
import static de.siegmar.jmonkey.decoder.room.image.Codec.CompressionMethod.UNCOMPRESSED;
import static de.siegmar.jmonkey.decoder.room.image.Codec.RenderingDirection.HORIZONTAL;
import static de.siegmar.jmonkey.decoder.room.image.Codec.RenderingDirection.VERTICAL;

import java.util.StringJoiner;

@SuppressWarnings("checkstyle:MagicNumber")
public final class Codec {

    private final int codecNo;
    private final CompressionMethod method;
    private final RenderingDirection direction;
    private final int paramSubtraction;
    private final int paletteBitLength;

    private Codec(final int codecNo, final CompressionMethod method,
                  final RenderingDirection direction,
                  final int paramSubtraction) {
        this.codecNo = codecNo;
        this.method = method;
        this.direction = direction;
        this.paramSubtraction = paramSubtraction;

        final int pbl = codecNo - paramSubtraction;
        if (method == METHOD_1 && (pbl < 4 || pbl > 8)) {
            throw new IllegalStateException("Invalid paletteBitLength " + pbl);
        }
        paletteBitLength = pbl;
    }

    public static Codec of(final int codec) {
        return switch (codec) {
            case 1                  -> new Codec(codec, UNCOMPRESSED, HORIZONTAL,  0);
            case 10                 -> new Codec(codec, EGA,          HORIZONTAL,  0);
            case 14, 15, 16, 17, 18 -> new Codec(codec, METHOD_1,     VERTICAL,   10);
            case 24, 25, 26, 27, 28 -> new Codec(codec, METHOD_1,     HORIZONTAL, 20);
            default -> throw new IllegalArgumentException("Unknown codec: " + codec);
        };
    }

    public CompressionMethod getMethod() {
        return method;
    }

    public RenderingDirection getDirection() {
        return direction;
    }

    public int getPaletteBitLength() {
        return paletteBitLength;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Codec.class.getSimpleName() + "[", "]")
            .add("compressionId=" + codecNo)
            .add("method=" + method)
            .add("direction=" + direction)
            .add("paramSubtraction=" + paramSubtraction)
            .add("paletteBitLength=" + paletteBitLength)
            .toString();
    }

    public enum CompressionMethod {

        UNCOMPRESSED, METHOD_1, EGA

    }

    public enum RenderingDirection {

        HORIZONTAL, VERTICAL

    }

}
