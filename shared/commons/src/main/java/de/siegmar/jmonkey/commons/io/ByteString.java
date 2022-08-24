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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class ByteString {

    private final ByteBuffer data;

    public ByteString(final ByteBuffer data) {
        this.data = Objects.requireNonNull(data.asReadOnlyBuffer());
    }

    public static ByteString copyFrom(final byte[] data) {
        return new ByteString(ByteBuffer.wrap(data.clone()));
    }

    public static ByteString wrap(final byte[] data) {
        return new ByteString(ByteBuffer.wrap(data));
    }

    public static ByteString readFrom(final Path file) {
        try {
            return wrap(Files.readAllBytes(file));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteString readFrom(final InputStream in) {
        try {
            return wrap(in.readAllBytes());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteString readFrom(final InputStream in, final int length) {
        try {
            return wrap(in.readNBytes(length));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return data.limit();
    }

    public byte get(final int pos) {
        return data.get(pos);
    }

    public EnhancedByteBuffer ebbLE() {
        return ebb(ByteOrder.LITTLE_ENDIAN);
    }

    public EnhancedByteBuffer ebb(final ByteOrder endian) {
        return new EnhancedByteBuffer(data.duplicate().order(Objects.requireNonNull(endian)));
    }

    public ByteString slice(final int pos) {
        return slice(pos, size() - pos);
    }

    public ByteString slice(final int pos, final int len) {
        return new ByteString(data.slice(pos, len));
    }

    public InputStream asInputStream() {
        // FIXME
        return new ByteArrayInputStream(dumpCopy());
    }

    public byte[] dumpCopy() {
        final byte[] ret = new byte[size()];
        data.get(0, ret, 0, size());
        return ret;
    }

    public void writeTo(final OutputStream os) {
        try {
            Channels.newChannel(os).write(data);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            data.rewind();
        }
    }

    public void writeTo(final Path file) {
        try {
            try (SeekableByteChannel channel = Files.newByteChannel(file,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                channel.write(data);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            data.rewind();
        }
    }

    // use MD5 in order to have checksums that can be compared with those from ScummVM
    public String toMD5() {
        final MessageDigest messageDigest = initMessageDigest();
        messageDigest.update(data);
        data.rewind();
        return HexFormat.of().formatHex(initMessageDigest().digest());
    }

    private static MessageDigest initMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
