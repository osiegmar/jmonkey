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

package de.siegmar.jmonkey.decoder.script;

import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public interface ResourceManager {

    void clearHeap();

    void loadScript(OpParameter resId);

    void loadSound(OpParameter resId);

    void loadCostume(OpParameter resId);

    void loadRoom(OpParameter resId);

    void lockScript(OpParameter resId);

    void lockSound(OpParameter resId);

    void lockCostume(OpParameter resId);

    void lockRoom(OpParameter resId);

    void unlockScript(OpParameter resId);

    void unlockSound(OpParameter resId);

    void unlockCostume(OpParameter resId);

    void unlockRoom(OpParameter resId);

    void loadCharset(OpParameter resId);

    void nukeRoom(OpParameter resId);

    void nukeCostume(OpParameter resId);

}
