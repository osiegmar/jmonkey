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

package de.siegmar.jmonkey.encoder.index;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.encoder.LeWriter;
import de.siegmar.jmonkey.encoder.Node;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.ObjectMeta;
import de.siegmar.jmonkey.index.RoomDirectory;
import de.siegmar.jmonkey.index.RoomName;
import de.siegmar.jmonkey.index.RoomOffset;

public final class IndexWriter {

    private IndexWriter() {
    }

    public static void writeIndex(final Index index, final Path file) throws IOException {
        try (LeWriter leWriter = new LeWriter(new BufferedOutputStream(Files.newOutputStream(file)))) {
            writeNode(leWriter, "RN", buildRoomNames(index.roomNames()));
            writeNode(leWriter, "0R", buildRoomLocations(index.roomLocations()));
            writeNode(leWriter, "0S", buildDataDirectory(index.scripts()));
            writeNode(leWriter, "0N", buildDataDirectory(index.sounds()));
            writeNode(leWriter, "0C", buildDataDirectory(index.costumes()));
            writeNode(leWriter, "0O", buildObjectDirectory(index.objects()));
        }
    }

    private static void writeNode(final LeWriter leWriter, final String nodeName, final ByteString data)
        throws IOException {
        Node.createRoot(nodeName).setData(data).writeTo(leWriter);
    }

    private static ByteString buildRoomNames(final List<RoomName> roomNames) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (LeWriter leWriter = new LeWriter(bos)) {
            for (final RoomName roomName : roomNames) {
                leWriter.writeU8(roomName.roomId());
                leWriter.write(buildRoomName(roomName.name()));
            }
            leWriter.writeU8(0);
        }
        return ByteString.wrap(bos.toByteArray());
    }

    private static ByteString buildRoomLocations(final List<RoomDirectory> roomLocations) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final int size = 1 + roomLocations.stream().mapToInt(RoomDirectory::roomId).max().orElseThrow();

        try (LeWriter leWriter = new LeWriter(bos)) {
            leWriter.writeU16(size);
            for (int i = 0; i < size; i++) {
                final int roomId = i;
                final Optional<RoomDirectory> room = roomLocations.stream()
                    .filter(r -> roomId == r.roomId())
                    .findFirst();
                if (room.isPresent()) {
                    leWriter.writeU8(room.get().fileNumber());
                    leWriter.writeU32(room.get().fileOffset());
                } else {
                    leWriter.writeU8(0);
                    leWriter.writeU32(0);
                }
            }
        }
        return ByteString.wrap(bos.toByteArray());
    }

    private static ByteString buildDataDirectory(final List<RoomOffset> data) throws IOException {
        final int size = 199;

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (LeWriter leWriter = new LeWriter(bos)) {
            leWriter.writeU16(size);
            for (int i = 0; i < size; i++) {
                final int itemId = i;
                final Optional<RoomOffset> roomOffset = data.stream()
                    .filter(d -> itemId == d.itemId())
                    .findFirst();
                if (roomOffset.isPresent()) {
                    leWriter.writeU8(roomOffset.get().roomId());
                    leWriter.writeU32(roomOffset.get().roomOffset());
                } else {
                    leWriter.writeU8(0);
                    leWriter.writeU32(0);
                }
            }
        }
        return ByteString.wrap(bos.toByteArray());
    }

    private static ByteString buildObjectDirectory(final List<ObjectMeta> objects) throws IOException {
        final int size = 1000;

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (LeWriter leWriter = new LeWriter(bos)) {
            leWriter.writeU16(size);
            for (int i = 0; i < size; i++) {
                final int objectId = i;
                final Optional<ObjectMeta> objectMeta = objects.stream()
                    .filter(d -> objectId == d.objectId())
                    .findFirst();
                if (objectMeta.isPresent()) {
                    final int classData = objectMeta.get().classData().classData();
                    final byte[] encodedClassData = new byte[]{
                        (byte) (0xFF & classData),
                        (byte) (0xFF & classData >> 8),
                        (byte) (0xFF & classData >> 16),
                    };
                    leWriter.write(encodedClassData);

                    final int owner = objectMeta.get().owner();
                    final int state = objectMeta.get().state();

                    final byte stateAndOwner = (byte) (state << 4 | owner);
                    leWriter.write(stateAndOwner);
                } else {
                    leWriter.writeU32(0);
                }
            }
        }
        return ByteString.wrap(bos.toByteArray());
    }

    private static byte[] buildRoomName(final String roomName) {
        if (roomName.length() > 9) {
            throw new IllegalArgumentException("Length of room name must be <= 9");
        }

        byte[] bytes = roomName.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length < 9) {
            bytes = Arrays.copyOf(bytes, 9);
        }
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= 0xFF;
        }
        return bytes;
    }

}
