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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.ObjectMeta;
import de.siegmar.jmonkey.index.RoomDirectory;
import de.siegmar.jmonkey.index.RoomName;
import de.siegmar.jmonkey.index.RoomOffset;

public class IndexBuilder {

    private Map<Integer, String> roomNames = new LinkedHashMap<>();
    private List<RoomDirectory> roomLocations = new ArrayList<>();
    private List<RoomOffset> scripts = new ArrayList<>();
    private List<RoomOffset> sounds = new ArrayList<>();
    private List<RoomOffset> costumes = new ArrayList<>();
    private List<ObjectMeta> objects = new ArrayList<>();

    public Map<Integer, String> getRoomNames() {
        return roomNames;
    }

    public void setRoomNames(final Map<Integer, String> roomNames) {
        this.roomNames = roomNames;
    }

    public void addRoomName(final int roomId, final String name) {
        roomNames.put(roomId, name);
    }

    public List<RoomDirectory> getRoomLocations() {
        return roomLocations;
    }

    public void setRoomLocations(final List<RoomDirectory> roomLocations) {
        this.roomLocations = roomLocations;
    }

    public void addRoomLocation(final RoomDirectory roomLocation) {
        roomLocations.add(roomLocation);
    }

    public List<RoomOffset> getScripts() {
        return scripts;
    }

    public void setScripts(final List<RoomOffset> scripts) {
        this.scripts = scripts;
    }

    public void addScript(final RoomOffset roomOffset) {
        scripts.add(roomOffset);
    }

    public List<RoomOffset> getSounds() {
        return sounds;
    }

    public void setSounds(final List<RoomOffset> sounds) {
        this.sounds = sounds;
    }

    public void addSound(final RoomOffset roomOffset) {
        sounds.add(roomOffset);
    }

    public List<RoomOffset> getCostumes() {
        return costumes;
    }

    public void setCostumes(final List<RoomOffset> costumes) {
        this.costumes = costumes;
    }

    public void addCostume(final RoomOffset roomOffset) {
        costumes.add(roomOffset);
    }

    public List<ObjectMeta> getObjects() {
        return objects;
    }

    public void setObjects(final List<ObjectMeta> objects) {
        this.objects = objects;
    }

    public void addObject(final ObjectMeta objectMeta) {
        objects.add(objectMeta);
    }

    public Index build() {
        final List<RoomName> indexRoomNames = roomNames.entrySet().stream()
            .map(e -> new RoomName(0, e.getKey(), e.getValue()))
            .toList();
        return new Index(indexRoomNames, roomLocations, scripts, sounds, costumes, objects);
    }

}
