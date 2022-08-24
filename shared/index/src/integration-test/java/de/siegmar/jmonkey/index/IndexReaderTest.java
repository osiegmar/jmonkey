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

package de.siegmar.jmonkey.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.testhelper.PathType;
import de.siegmar.jmonkey.testhelper.PathsProvider;

class IndexReaderTest {

    @ParameterizedTest
    @PathsProvider(PathType.INDEX)
    void readIndex(final Path file) {
        final var index = IndexReader.readFile(file);

        assertThat(index.roomLocations()).hasSize(99);

        // Room 'roland' is missing in Amiga version
        final List<RoomName> roomNames = index.roomNames();
        assertThat(roomNames).size().isIn(82, 83);

        assertThat(index.scripts()).hasSize(199);
        assertThat(index.sounds()).hasSize(199);
        assertThat(index.costumes()).hasSize(199);
        assertThat(index.objects()).hasSize(1000);

        assertThat(index.objects())
            .extracting(ObjectMeta::state)
            .containsOnly(0, 1);

        assertThat(index.objects())
            .extracting(ObjectMeta::owner)
            .containsOnly(0, 15);

        assertThat(index.objects())
            .flatExtracting(o -> o.classData().toIntList())
            .containsOnly(1, 2, 3, 5, 6, 7, 8, 10, 12, 13, 14, 15, 16, 18, 24);
    }

    @ParameterizedTest
    @PathsProvider(PathType.INDEX)
    void readWrappedIndex(final Path file) {
        final var index = new WrappedIndex(IndexReader.readFile(file));

        assertThat(index.findNamedRoomById(10))
            .hasValueSatisfying(c -> assertThat(c.name()).isEqualTo("logo"));
    }

}
