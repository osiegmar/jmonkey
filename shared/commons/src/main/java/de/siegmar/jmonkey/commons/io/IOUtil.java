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

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Stream;

public final class IOUtil {

    private IOUtil() {
    }

    public static Optional<Path> findFile(final Path path, final int fileNo) {
        return findFile(path, buildPattern(fileNo));
    }

    public static Optional<Path> findFile(final Path path, final String filename) {
        final String pattern = "(?i)^" + filename + "$";
        try (Stream<Path> files = Files.find(path, 1, (file, attr) -> file.getFileName().toString().matches(pattern))) {
            return files.findFirst();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path getFile(final Path path, final int fileNo) {
        final String filename = buildPattern(fileNo);
        try {
            return findFile(path, filename)
                .orElseThrow(() -> new FileNotFoundException("No " + filename + " found in " + path));
        } catch (final FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String buildPattern(final int fileNo) {
        return String.format(fileNo == 0 || fileNo >= 900 ? "%03d.lfl" : "disk%02d.lec", fileNo);
    }

    public static InputStream openDecryptInputStream(final Path file, final byte pattern) throws IOException {
        return decryptInputStream(Files.newInputStream(file), pattern);
    }

    public static InputStream decryptInputStream(final InputStream in, final byte pattern) {
        return new XorInputStream(in, pattern);
    }

    @SuppressWarnings("PMD.CloseResource")
    public static ByteString read(final ReadableByteChannel ch, final int length) throws IOException {
        final XorInputStream in = new XorInputStream(Channels.newInputStream(ch), (byte) 0x69);
        return ByteString.readFrom(in, length);
    }

    public static ByteString readLEFile(final Path file, final byte pattern) {
        try (InputStream in = openDecryptInputStream(file, pattern)) {
            return ByteString.readFrom(in);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Deprecated
    public static ByteString readLEFile(final Path file) {
        return ByteString.readFrom(file);
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException ignored) {
            // ignore
        }
    }

    public static String toSHA1(final Path file) {
        final MessageDigest md = initMessageDigest();
        final ByteBuffer bb = ByteBuffer.allocate(8192);
        try (ByteChannel ch = Files.newByteChannel(file, StandardOpenOption.READ)) {
            while (ch.read(bb) > 0) {
                md.update(bb.flip());
                bb.flip();
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    private static MessageDigest initMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
