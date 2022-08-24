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

package de.siegmar.jmonkey.cli;

import java.util.ArrayDeque;
import java.util.Deque;

import picocli.CommandLine;

public final class StatusInfo {

    private static final CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    private static final Deque<String> DEQUE = new ArrayDeque<>();
    private static int length;

    private StatusInfo() {
    }

    public static void status(final String text, final Object... args) {
        DEQUE.add(text.formatted(args));
        update();
    }

    public static void success() {
        clean();
        final String line = DEQUE.removeLast();

        if (DEQUE.isEmpty()) {
            final String ansiLine = ANSI.string("\r@|bold,green " + line + " |@");
            System.out.println(ansiLine);
            return;
        }

        update();
    }

    private static void clean() {
        System.out.print("\r" + " ".repeat(length));
    }

    private static void update() {
        final String line = String.join(" > ", DEQUE);
        length = line.length();
        final String ansiLine = ANSI.string(line);
        System.out.print("\r" + ansiLine);
    }

}
