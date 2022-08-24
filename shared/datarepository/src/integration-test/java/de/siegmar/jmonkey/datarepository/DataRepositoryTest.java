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

import static de.siegmar.jmonkey.commons.io.DataChunkType.RO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.index.RoomDirectory;
import de.siegmar.jmonkey.index.RoomOffset;
import de.siegmar.jmonkey.testhelper.PathType;
import de.siegmar.jmonkey.testhelper.PathsProvider;

class DataRepositoryTest {

    @ParameterizedTest
    @PathsProvider(PathType.GAME_DIR)
    void rooms(final Path gameDir) {
        try (DataRepository dataRepository = new DataRepository(gameDir)) {
            final var index = dataRepository.getWrappedIndex();

            final List<Integer> roomIds = index.listRooms().stream()
                .map(RoomDirectory::roomId)
                .toList();

            for (final Integer roomId : roomIds) {
                if (roomId == 94) {
                    // ignore missing Roland room
                    return;
                }

                final EnhancedByteBuffer bb = dataRepository.readRoom(roomId).dataWithHeader().ebbLE();
                final var chunkHeader = DataChunkHeader.read(bb);
                assertEquals(RO, chunkHeader.type());
            }
        }
    }

    @ParameterizedTest
    @PathsProvider(PathType.GAME_DIR)
    void scripts(final Path gameDir) {
        try (DataRepository dataRepository = new DataRepository(gameDir)) {
            final var index = dataRepository.getWrappedIndex();

            final List<Integer> scriptIds = index.listGlobalScriptRelativeOffsets().stream()
                .map(RoomOffset::itemId)
                .toList();

            for (final Integer scriptId : scriptIds) {
                if (scriptId == 15 || scriptId == 42 || scriptId >= 173) {
                    // The index of DOS-VGA-EN-version is corrupt – it references a non-existing block for these
                    // script ids – other versions of the game have proper 0 references
                    continue;
                }

                final BasicChunk basicChunk = dataRepository.readGlobalScript(scriptId);
                final var chunkHeader = basicChunk.header();
                assertEquals("SC", chunkHeader.name());
            }
        }
    }

    @ParameterizedTest
    @PathsProvider(PathType.GAME_DIR)
    void sounds(final Path gameDir) {
        try (DataRepository dataRepository = new DataRepository(gameDir)) {
            final var index = dataRepository.getWrappedIndex();

            final List<Integer> soundIds = index.listSoundRelativeOffsets().stream()
                .map(RoomOffset::itemId)
                .toList();

            for (final Integer soundId : soundIds) {
                if (index.findSoundRelativeOffset(soundId).orElseThrow().roomId() == 94) {
                    // Skip optional roland file
                    continue;
                }

                // The index of DOS-VGA-EN-version is corrupt – it references a non-existing block for these sound ids
                // other versions of the game have proper 0 references
                if (Set.of(72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 95, 96, 97).contains(soundId)) {
                    continue;
                }

                // Broken in Amiga-EN-version
                if (Set.of(107).contains(soundId)) {
                    continue;
                }

                final BasicChunk basicChunk = dataRepository.readSound(soundId);
                final var chunkHeader = basicChunk.header();
                assertThat(chunkHeader.name())
                    .as("Check sound id %d", soundId)
                    .containsAnyOf("SO", "RO", "AM");
            }
        }
    }

    @ParameterizedTest
    @PathsProvider(PathType.GAME_DIR)
    void costumes(final Path gameDir) {
        try (DataRepository dataRepository = new DataRepository(gameDir)) {
            final var index = dataRepository.getWrappedIndex();

            final List<Integer> costumeIds = index.listCostumeRelativeOffsets().stream()
                .map(RoomOffset::itemId)
                .toList();

            for (final Integer costumeId : costumeIds) {
                // The index of DOS-VGA-EN-version is corrupt – it references a non-existing block for this costume id
                // other versions of the game have proper 0 references
                if (Set.of(0, 10, 16, 85, 86, 101, 106).contains(costumeId)) {
                    continue;
                }

                final BasicChunk basicChunk = dataRepository.readCostume(costumeId);
                final var chunkHeader = basicChunk.header();
                assertEquals("CO", chunkHeader.name());
            }
        }
    }

}
