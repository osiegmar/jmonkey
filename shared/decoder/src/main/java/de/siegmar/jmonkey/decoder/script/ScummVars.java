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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("checkstyle:ExecutableStatementCount")
public final class ScummVars {

    private static final Map<Integer, String> VAR_NAMES = new HashMap<>();

    static {
        VAR_NAMES.put(0, "VAR_RESULT");
        VAR_NAMES.put(1, "VAR_EGO");
        VAR_NAMES.put(2, "VAR_CAMERA_POS_X");
        VAR_NAMES.put(3, "VAR_HAVE_MSG");
        VAR_NAMES.put(4, "VAR_ROOM");
        VAR_NAMES.put(5, "VAR_OVERRIDE");
        VAR_NAMES.put(6, "VAR_MACHINE_SPEED");
        VAR_NAMES.put(7, "VAR_ME");
        VAR_NAMES.put(8, "VAR_NUM_ACTOR");
        VAR_NAMES.put(9, "VAR_CURRENT_LIGHTS");
        VAR_NAMES.put(11, "VAR_TMR_1");
        VAR_NAMES.put(12, "VAR_TMR_2");
        VAR_NAMES.put(14, "VAR_MUSIC_TIMER");
        VAR_NAMES.put(19, "VAR_TIMER_NEXT");
        VAR_NAMES.put(20, "VAR_VIRT_MOUSE_X");
        VAR_NAMES.put(21, "VAR_VIRT_MOUSE_Y");
        VAR_NAMES.put(24, "VAR_CUTSCENEEXIT_KEY");
        VAR_NAMES.put(25, "VAR_TALK_ACTOR");
        VAR_NAMES.put(26, "VAR_CAMERA_FAST_X");
        VAR_NAMES.put(27, "VAR_SCROLL_SCRIPT");
        VAR_NAMES.put(28, "VAR_ENTRY_SCRIPT");
        VAR_NAMES.put(29, "VAR_ENTRY_SCRIPT2");
        VAR_NAMES.put(30, "VAR_EXIT_SCRIPT");
        VAR_NAMES.put(32, "VAR_VERB_SCRIPT");
        VAR_NAMES.put(33, "VAR_SENTENCE_SCRIPT");
        VAR_NAMES.put(34, "VAR_INVENTORY_SCRIPT");
        VAR_NAMES.put(35, "VAR_CUTSCENE_START_SCRIPT");
        VAR_NAMES.put(36, "VAR_CUTSCENE_END_SCRIPT");
        VAR_NAMES.put(37, "VAR_CHARINC");
        VAR_NAMES.put(38, "VAR_WALKTO_OBJ");
        VAR_NAMES.put(39, "VAR_DEBUGMODE");
        VAR_NAMES.put(40, "VAR_HEAPSPACE");
        VAR_NAMES.put(41, "VAR_TALK_ACTOR");
        VAR_NAMES.put(42, "VAR_RESTART_KEY");
        VAR_NAMES.put(43, "VAR_PAUSE_KEY");
        VAR_NAMES.put(44, "VAR_MOUSE_X");
        VAR_NAMES.put(45, "VAR_MOUSE_Y");
        VAR_NAMES.put(48, "VAR_SOUNDCARD");
        VAR_NAMES.put(49, "VAR_VIDEOMODE");
        VAR_NAMES.put(50, "VAR_MAINMENU_KEY");
        VAR_NAMES.put(51, "VAR_FIXEDDISK");
        VAR_NAMES.put(52, "VAR_CURSORSTATE");
        VAR_NAMES.put(54, "VAR_V5_TALK_STRING_Y");
    }

    private ScummVars() {
    }

    public static Optional<String> resolve(final int i) {
        return Optional.ofNullable(VAR_NAMES.get(i));
    }

    public static Optional<Integer> resolve(final String name) {
        return VAR_NAMES.entrySet().stream()
            .filter(e -> e.getValue().equals(name))
            .map(Map.Entry::getKey)
            .findFirst();
    }

}
