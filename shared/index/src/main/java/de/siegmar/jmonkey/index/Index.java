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

import java.util.List;

public record Index(List<RoomName> roomNames,
                    List<RoomDirectory> roomLocations,
                    List<RoomOffset> scripts,
                    List<RoomOffset> sounds,
                    List<RoomOffset> costumes,
                    List<ObjectMeta> objects) {

    public Index {
        roomNames = List.copyOf(roomNames);
        roomLocations = List.copyOf(roomLocations);
        scripts = List.copyOf(scripts);
        sounds = List.copyOf(sounds);
        costumes = List.copyOf(costumes);
        objects = List.copyOf(objects);
    }

}
