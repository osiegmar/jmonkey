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

package de.siegmar.jmonkey.datarepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.testhelper.PathType;
import de.siegmar.jmonkey.testhelper.PathsProvider;

class LecSeekableFileTest {

    @ParameterizedTest
    @PathsProvider(PathType.DATA)
    void validateChunkSizes(final Path file) throws IOException {
        try (LecSeekableFile lecSeekableFile = new LecSeekableFile(file)) {
            final LecIndex lecIndex = lecSeekableFile.getLecIndex();
            assertThat(lecIndex.getRooms()).isNotEmpty();
        }
    }

}
