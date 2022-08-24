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

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;

/**
 * Reader for main index (000.LFL).
 *
 * @see <a href="https://osiegmar.github.io/jmonkey/file-formats/index-file.html">Index file format specification</a>
 */
public final class IndexReader {

    private static final System.Logger LOG = System.getLogger(IndexReader.class.getName());

    private final EnhancedByteBuffer bb;

    private IndexReader(final ByteString data) {
        bb = data.ebbLE();
    }

    public static Index readFile(final Path file) {
        final ByteString bs = ByteString.readFrom(file);

        LOG.log(DEBUG, () -> "Read index from %s (SHA-1 hash: %s)"
            .formatted(file, bs.toMD5()));

        return new IndexReader(bs).readFile();
    }

    @SuppressWarnings("checkstyle:InnerAssignment")
    private Index readFile() {
        List<RoomName> roomNames = null;
        List<RoomDirectory> rooms = null;
        List<RoomOffset> scripts = null;
        List<RoomOffset> sounds = null;
        List<RoomOffset> costumes = null;
        List<ObjectMeta> objects = null;

        while (bb.hasRemaining()) {
            final var chunkHeader = IndexChunkHeader.of(bb.readChunkHeader());

            switch (chunkHeader.type()) {
                case ROOM_NAMES -> roomNames = readRoomNames();
                case ROOM_DIR -> rooms = readRoomDirectory();
                case SCRIPT_DIR -> scripts = readDataOffsetDirectory();
                case SOUND_DIR -> sounds = readDataOffsetDirectory();
                case COSTUME_DIR -> costumes = readDataOffsetDirectory();
                case GLOBAL_OBJECTS -> objects = readObjectDirectory();
                default -> throw new IllegalStateException("Unknown chunk: " + chunkHeader);
            }
        }

        Objects.requireNonNull(roomNames, "roomNames not initialized");
        Objects.requireNonNull(rooms, "roomLocations not initialized");
        Objects.requireNonNull(scripts, "scripts not initialized");
        Objects.requireNonNull(sounds, "sounds not initialized");
        Objects.requireNonNull(costumes, "costumes not initialized");
        Objects.requireNonNull(objects, "objects not initialized");

        final Index index = new Index(roomNames, rooms, scripts, sounds, costumes, objects);

        LOG.log(TRACE, "Read index %s", index);

        return index;
    }

    private List<RoomName> readRoomNames() {
        final List<RoomName> roomNames = new ArrayList<>();
        int roomId;
        int item = 0;
        while ((roomId = bb.readU8()) != 0) {
            final String name = decodeString(9);
            roomNames.add(new RoomName(item++, roomId, name));
        }

        return roomNames;
    }

    // room names are 0xff encoded and 0 terminated
    @SuppressWarnings("SameParameterValue")
    private String decodeString(final int length) {
        final byte[] dst = bb.readBytes(length);

        int i = 0;
        for (; i < dst.length; i++) {
            dst[i] ^= 0xff;
            if (dst[i] == 0) {
                break;
            }
        }

        return new String(dst, 0, i, StandardCharsets.US_ASCII);
    }

    private List<RoomDirectory> readRoomDirectory() {
        final int noOfItems = bb.readU16();

        final List<RoomDirectory> fileNumbers = new ArrayList<>(noOfItems);

        for (int item = 0; item < noOfItems; item++) {
            final int fileNo = bb.readU8();
            final int fileOffset = bb.readU32();
            fileNumbers.add(new RoomDirectory(item, fileNo, fileOffset));
        }

        return fileNumbers;
    }

    private List<RoomOffset> readDataOffsetDirectory() {
        final int noOfItems = bb.readU16();

        final List<RoomOffset> roomOffsets = new ArrayList<>(noOfItems);

        for (int item = 0; item < noOfItems; item++) {
            final int pos = bb.readU8();

            // usually this is U32 but the english Amiga version has some -1 script offsets
            final int offset = bb.readS32();

            roomOffsets.add(new RoomOffset(item, pos, offset));
        }

        return roomOffsets;
    }

    private List<ObjectMeta> readObjectDirectory() {
        final int noOfItems = bb.readU16();

        final List<ObjectMeta> objects = new ArrayList<>(noOfItems);

        for (int objectId = 0; objectId < noOfItems; objectId++) {
            int classDataTmp = bb.readU8();
            classDataTmp |= bb.readU8() << 8;
            classDataTmp |= bb.readU8() << 16;

            final int ownerAndState = bb.readU8();
            final int owner = ownerAndState & 0x0F;
            final int state = ownerAndState >> 4;

            objects.add(new ObjectMeta(objectId, new ClassData(classDataTmp), owner, state));
        }

        return objects;
    }

}
