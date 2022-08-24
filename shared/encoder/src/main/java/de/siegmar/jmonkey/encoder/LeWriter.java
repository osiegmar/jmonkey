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

package de.siegmar.jmonkey.encoder;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class LeWriter extends FilterOutputStream {

    public LeWriter(final OutputStream out) {
        super(out);
    }

    @Override
    public void write(final byte[] data) {
        try {
            out.write(data);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeU8(final int data) {
        try {
            out.write(data);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeU16(final int data) {
        try {
            out.write(0xFF & data);
            out.write(0xFF & (data >> 8));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeU32(final int data) {
        try {
            out.write(0xFF & data);
            out.write(0xFF & (data >> 8));
            out.write(0xFF & (data >> 16));
            out.write(0xFF & (data >> 24));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
