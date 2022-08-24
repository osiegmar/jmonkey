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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ClassData(int classData) {

    public ClassData {
        if (classData >= (1 << 25)) {
            throw new IllegalArgumentException();
        }
    }

    public List<Integer> toIntList() {
        if (classData == 0) {
            return List.of();
        }

        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if ((1 << i & classData) != 0) {
                list.add(i + 1);
            }
        }

        return Collections.unmodifiableList(list);
    }

}
