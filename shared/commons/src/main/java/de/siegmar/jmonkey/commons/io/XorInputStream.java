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

package de.siegmar.jmonkey.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XorInputStream extends FilterInputStream {

    private final byte pattern;

    public XorInputStream(final InputStream in, final byte pattern) {
        super(in);
        if (pattern < 0) {
            throw new IllegalArgumentException("Pattern must be >= 0 but was " + pattern);
        }
        this.pattern = pattern;
    }

    public byte getPattern() {
        return pattern;
    }

    @Override
    public int read() throws IOException {
        final int b = in.read();
        return b == -1 ? b : (b ^ pattern) & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int n = in.read(b, off, len);

        if (n > 0) {
            for (int i = 0; i < n; i++) {
                b[off + i] = (byte) ((b[off + i] ^ pattern) & 0xFF);
            }
        }

        return n;
    }

}
