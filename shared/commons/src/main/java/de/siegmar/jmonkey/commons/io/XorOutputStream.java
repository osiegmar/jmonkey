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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class XorOutputStream extends FilterOutputStream {

    private final byte pattern;

    public XorOutputStream(final OutputStream out, final byte pattern) {
        super(out);
        if (pattern < 0) {
            throw new IllegalArgumentException("Pattern must be >= 0 but was " + pattern);
        }
        this.pattern = pattern;
    }

    public byte getPattern() {
        return pattern;
    }

    @Override
    public void write(final int b) throws IOException {
        out.write((b ^ pattern) & 0xFF);
    }

}
