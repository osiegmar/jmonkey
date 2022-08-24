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

package de.siegmar.jmonkey.explorer.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public final class FileTypeDetector {

    private FileTypeDetector() {
    }

    public static Optional<FileType> detect(final Path file) {
        final ByteBuffer bb = ByteBuffer.allocate(1);

        try (FileChannel open = FileChannel.open(file, StandardOpenOption.READ)) {
            open.read(bb, 4);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        bb.flip();

        final char chunkName = (char) bb.get();
        return switch (chunkName) {
            case 'R' -> Optional.of(FileType.INDEX);
            case 'c' -> Optional.of(FileType.FONT);
            case 'L' ^ 0x69 -> Optional.of(FileType.DATA);
            default -> Optional.empty();
        };
    }

}
