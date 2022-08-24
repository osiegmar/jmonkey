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

import java.io.ByteArrayOutputStream;

public class ByteStringBuilder {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public ByteString build() {
        return ByteString.copyFrom(out.toByteArray());
    }

    public ByteStringBuilder appendU8(final int data) {
        out.write(data);
        return this;
    }

    public ByteStringBuilder appendU16(final int data) {
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        return this;
    }

    public ByteStringBuilder appendU32(final int data) {
        out.write(data & 0xFF);
        out.write((data >>> 8) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>> 24) & 0xFF);
        return this;
    }

    public ByteStringBuilder append(final byte[] data) {
        out.writeBytes(data);
        return this;
    }

    public ByteStringBuilder append(final ByteString data) {
        data.writeTo(out);
        return this;
    }

    public int size() {
        return out.size();
    }

}
