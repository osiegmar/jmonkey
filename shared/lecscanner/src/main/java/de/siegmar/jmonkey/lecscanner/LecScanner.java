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

package de.siegmar.jmonkey.lecscanner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Scanner for data files (DISK0X.LEC).
 *
 * @see <a href="https://osiegmar.github.io/jmonkey/file-formats/data-files.html">Data file format specification</a>
 */
public final class LecScanner {

    private LecScanner() {
    }

    public static TreeIndex<LecChunk> scanTree(final Path file) {
        try (LecFile lecFile = new LecFile(file)) {
            return scanTree(lecFile);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static TreeIndex<LecChunk> scanTree(final LecFile lecFile) {
        return scan(lecFile, new TreeLecVisitor()).getTree();
    }

    public static <T extends LecVisitor> T scan(final Path file, final T lecVisitor) {
        return scan(file, c -> true, lecVisitor);
    }

    public static <T extends LecVisitor> T scan(final LecFile lecFile, final T lecVisitor) {
        return scan(lecFile, c -> true, lecVisitor);
    }

    public static <T extends LecVisitor> T scan(final Path file, final Predicate<LecChunk> filter, final T lecVisitor) {
        try (LecFile lecFile = new LecFile(file)) {
            return scan(lecFile, filter, lecVisitor);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends LecVisitor> T scan(final LecFile lecFile, final Predicate<LecChunk> filter,
                                                final T lecVisitor) {
        final LecChunkIterator lecChunkIterator = new LecChunkIterator(lecFile);

        while (lecChunkIterator.hasNext()) {
            final LecChunk chunk = lecChunkIterator.next();
            if (filter.test(chunk)) {
                if (!lecVisitor.visit(lecFile, chunk)) {
                    return lecVisitor;
                }
            }
        }

        lecVisitor.end();

        return lecVisitor;
    }

}
