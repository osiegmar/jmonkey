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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WrappedIndex {

    private final List<RoomDirectory> roomsList;
    private final Map<Integer, RoomDirectory> rooms;

    private final List<NamedRoomDirectory> namedRoomsList;
    private final Map<Integer, NamedRoomDirectory> namedRooms;

    private final List<RoomOffset> scriptList;
    private final Map<Integer, RoomOffset> scripts;

    private final List<RoomOffset> costumeList;
    private final Map<Integer, RoomOffset> costumes;

    private final List<RoomOffset> soundList;
    private final Map<Integer, RoomOffset> sounds;

    private final List<ObjectMeta> objectList;
    private final Map<Integer, ObjectMeta> objects;

    public WrappedIndex(final Index index) {
        roomsList = index.roomLocations().stream()
            .filter(this::validRoomDirectory)
            .sorted(Comparator.comparingInt(RoomDirectory::roomId))
            .toList();
        rooms = roomsList.stream()
            .collect(Collectors.toUnmodifiableMap(RoomDirectory::roomId, r -> r));

        namedRoomsList = index.roomNames().stream()
            .map(r -> new NamedRoomDirectory(r.roomId(), rooms.get(r.roomId()).fileNumber(), r.name()))
            .sorted(Comparator.comparingInt(NamedRoomDirectory::roomId))
            .toList();
        namedRooms = namedRoomsList.stream()
            .collect(Collectors.toUnmodifiableMap(NamedRoomDirectory::roomId, r -> r));

        scriptList = index.scripts().stream()
            .filter(this::validRoomOffset)
            .sorted(Comparator.comparingInt(RoomOffset::itemId))
            .toList();
        scripts = scriptList.stream()
            .collect(Collectors.toUnmodifiableMap(RoomOffset::itemId, r -> r));

        costumeList = index.costumes().stream()
            .filter(this::validRoomOffset)
            .sorted(Comparator.comparingInt(RoomOffset::itemId))
            .toList();
        costumes = costumeList.stream()
            .collect(Collectors.toUnmodifiableMap(RoomOffset::itemId, r -> r));

        soundList = index.sounds().stream()
            .filter(this::validRoomOffset)
            .sorted(Comparator.comparingInt(RoomOffset::itemId))
            .toList();
        sounds = soundList.stream()
            .collect(Collectors.toUnmodifiableMap(RoomOffset::itemId, r -> r));

        objectList = index.objects().stream()
            .sorted(Comparator.comparingInt(ObjectMeta::objectId))
            .toList();
        objects = objectList.stream()
            .collect(Collectors.toUnmodifiableMap(ObjectMeta::objectId, r -> r));
    }

    private boolean validRoomDirectory(final RoomDirectory roomDirectory) {
        return roomDirectory.fileNumber() >= 1 && roomDirectory.fileNumber() <= 9;
    }

    private boolean validRoomOffset(final RoomOffset roomOffset) {
        return roomOffset.roomOffset() > 0;
    }

    public List<RoomDirectory> listRooms() {
        return roomsList;
    }

    public Optional<RoomDirectory> findRoomById(final int roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public List<NamedRoomDirectory> listNamedRooms() {
        return namedRoomsList;
    }

    public Optional<NamedRoomDirectory> findNamedRoomById(final int roomId) {
        return Optional.ofNullable(namedRooms.get(roomId));
    }

    public List<RoomOffset> listGlobalScriptRelativeOffsets() {
        return scriptList;
    }

    public Optional<RoomOffset> findGlobalScriptRelativeOffset(final int scriptId) {
        return Optional.ofNullable(scripts.get(scriptId));
    }

    public List<RoomOffset> listCostumeRelativeOffsets() {
        return costumeList;
    }

    public Optional<RoomOffset> findCostumeRelativeOffset(final int costumeId) {
        return Optional.ofNullable(costumes.get(costumeId));
    }

    public List<RoomOffset> listSoundRelativeOffsets() {
        return soundList;
    }

    public Optional<RoomOffset> findSoundRelativeOffset(final int soundId) {
        return Optional.ofNullable(sounds.get(soundId));
    }

    public List<ObjectMeta> listObjectMetadata() {
        return objectList;
    }

    public Optional<ObjectMeta> findObjectMetadata(final int objectId) {
        return Optional.ofNullable(objects.get(objectId));
    }

}
