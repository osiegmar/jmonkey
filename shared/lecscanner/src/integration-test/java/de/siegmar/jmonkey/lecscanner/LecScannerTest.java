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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.testhelper.PathType;
import de.siegmar.jmonkey.testhelper.PathsProvider;

class LecScannerTest {

    @ParameterizedTest
    @PathsProvider(PathType.DATA)
    void validateChunkSizes(final Path file) {
        final TreeIndex<LecChunk> indexNode = LecScanner.scanTree(file);

        final List<String> errors = new ArrayList<>();
        checkSizes(indexNode, s -> errors.add("File " + file + ": " + s));

        // unfortunately chunk length of one SO chunk is wrong in DISK01.LEC
        if (!"disk01.lec".equalsIgnoreCase(file.toFile().getName())) {
            assertThat(errors).isEmpty();
        }
    }

    private void checkSizes(final TreeIndex<LecChunk> current, final Consumer<String> errors) {
        final List<TreeIndex<LecChunk>> children = current.getChildren();

        if (!children.isEmpty()) {
            final int currentSize = current.chunk().length();
            final int sumOfChildrenSizes = children.stream().mapToInt(c -> c.chunk().length()).sum();

            // payload + parent header
            int expectedSize = sumOfChildrenSizes + 6;

            if (current.chunk().type() == DataChunkType.LF) {
                // LF itself contains 2 bytes data
                expectedSize += 2;
            }

            if (currentSize != expectedSize) {
                errors.accept(("Node %s claims to have length %d but sum of %d children is: "
                    + "%d (%d bytes diff)").formatted(
                    current.chunk(), currentSize, children.size(), expectedSize,
                    currentSize - expectedSize));
            }

            children.forEach(c -> checkSizes(c, errors));
        }
    }

}
