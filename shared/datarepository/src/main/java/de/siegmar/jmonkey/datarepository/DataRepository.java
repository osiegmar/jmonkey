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

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.IOUtil;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.decoder.room.Room;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.IndexReader;
import de.siegmar.jmonkey.index.RoomDirectory;
import de.siegmar.jmonkey.index.RoomOffset;
import de.siegmar.jmonkey.index.WrappedIndex;

@SuppressWarnings({"checkstyle:IllegalCatch", "PMD.CloseResource"})
public class DataRepository implements Closeable {

    private static final System.Logger LOG = System.getLogger(DataRepository.class.getName());

    private final Path gameDir;
    private final Index index;
    private final WrappedIndex wrappedIndex;
    private final Map<Integer, LecSeekableFile> lecIndexes = new HashMap<>();

    public DataRepository(final Path gameDir) {
        this.gameDir = gameDir;
        index = IndexReader.readFile(IOUtil.getFile(gameDir, 0));
        wrappedIndex = new WrappedIndex(index);
    }

    @Deprecated
    public Index getIndex() {
        return index;
    }

    public WrappedIndex getWrappedIndex() {
        return wrappedIndex;
    }

    private Optional<LecSeekableFile> lecFile(final int fileNum) {
        Preconditions.checkArgument(fileNum > 0, "Invalid fileNum: %s", fileNum);

        try {
            LecSeekableFile lecQuickIndex = lecIndexes.get(fileNum);

            if (lecQuickIndex == null) {
                final String filename = IOUtil.buildPattern(fileNum);
                final Optional<Path> file = IOUtil.findFile(gameDir, filename);
                if (file.isEmpty()) {
                    LOG.log(DEBUG, "File %s not found", filename);
                    return Optional.empty();
                }

                LOG.log(DEBUG, "Load index from %s", file);

                lecQuickIndex = new LecSeekableFile(file.get());
                LOG.log(TRACE, "Load index %s", lecQuickIndex.getLecIndex());

                lecIndexes.put(fileNum, lecQuickIndex);
            }

            return Optional.of(lecQuickIndex);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<FileOffset> findRoomOffset(final int roomId) {
        Preconditions.checkArgument(roomId > 0, "Invalid room id: %s", roomId);

        final RoomDirectory room = wrappedIndex.findRoomById(roomId)
            .orElseThrow(() -> new IllegalStateException("Room %d not found".formatted(roomId)));

        final int fileNumber = room.fileNumber();

        final Optional<FileOffset> fileOffset = lecFile(fileNumber)
            .map(file -> new FileOffset(fileNumber, file.getLecIndex().getRoom(roomId).roOffset()));

        LOG.log(DEBUG, "Load Room %s from %s", room, fileOffset);

        return fileOffset;
    }

    private Optional<FileOffset> findGlobalScriptOffset(final int scriptId) {
        return wrappedIndex.findGlobalScriptRelativeOffset(scriptId)
            .flatMap(this::resolveOffset);
    }

    private Optional<FileOffset> findCostumeOffset(final int costumeId) {
        return wrappedIndex.findCostumeRelativeOffset(costumeId)
            .flatMap(this::resolveOffset);
    }

    private Optional<FileOffset> findSoundOffset(final int soundId) {
        return wrappedIndex.findSoundRelativeOffset(soundId)
            .flatMap(this::resolveOffset);
    }

    private Optional<FileOffset> resolveOffset(final RoomOffset roomOffset) {
        return findRoomOffset(roomOffset.roomId())
            .map(ro -> new FileOffset(ro.fileNumber(), ro.fileOffset() + roomOffset.roomOffset()));
    }

    private BasicChunk readChunk(final FileOffset fileOffset) {
        final LecSeekableFile file = lecFile(fileOffset.fileNumber())
            .orElseThrow(() -> new IllegalStateException("File not found: " + fileOffset));
        try {
            return file.readChunk(fileOffset.fileOffset());
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading chunk " + fileOffset, e);
        }
    }

    public Room loadRoom(final int roomId) {
        return RoomDecoder.decodeRoom(readRoom(roomId));
    }

    public BasicChunk readRoom(final int roomId) {
        final FileOffset fileOffset = findRoomOffset(roomId)
            .orElseThrow(() -> new IllegalStateException("Room %d not found".formatted(roomId)));
        try {
            return readChunk(fileOffset);
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading room " + roomId, e);
        }
    }

    public BasicChunk readGlobalScript(final int scriptId) {
        final FileOffset fileOffset = findGlobalScriptOffset(scriptId)
            .orElseThrow(() -> new IllegalStateException("Global script %d not found".formatted(scriptId)));
        try {
            return readChunk(fileOffset);
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading script " + scriptId, e);
        }
    }

    public BasicChunk readCostume(final int costumeId) {
        final FileOffset fileOffset = findCostumeOffset(costumeId)
            .orElseThrow(() -> new IllegalStateException("Costume %d not found".formatted(costumeId)));
        try {
            return readChunk(fileOffset);
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading costume " + costumeId, e);
        }
    }

    public BasicChunk readSound(final int soundId) {
        // FIXME this could return either an SO, RO(land) or AM chunk
        final FileOffset fileOffset = findSoundOffset(soundId)
            .orElseThrow(() -> new IllegalStateException("Sound %d not found".formatted(soundId)));
        try {
            return readChunk(fileOffset);
        } catch (final Exception e) {
            throw new IllegalStateException("Error reading sound " + soundId, e);
        }
    }

    @Override
    public void close() {
        lecIndexes.values().forEach(IOUtil::closeQuietly);
    }

}
