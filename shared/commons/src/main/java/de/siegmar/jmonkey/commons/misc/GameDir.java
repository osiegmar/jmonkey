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

package de.siegmar.jmonkey.commons.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GameDir {

    private static final Pattern FONT_FILE_PATTERN =
        Pattern.compile("^90[1-4]\\.lfl$", Pattern.CASE_INSENSITIVE);

    private static final Pattern AMIGA_SOUND_FILE_PATTERN =
        Pattern.compile("^(sample|music)\\.dat$", Pattern.CASE_INSENSITIVE);

    private final Path path;

    public GameDir(final Path path) {
        this.path = Objects.requireNonNull(path);
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("Directory does not exist: " + path);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path is not a directory: " + path);
        }
    }

    public Path provideIndexFile() throws IOException {
        return Files.find(path, 1, (p, a) ->
                "000.lfl".equalsIgnoreCase(p.getFileName().toString()))
            .findFirst()
            .orElseThrow(() -> new IOException("No file 000.LFL found"));
    }

    public Stream<Path> provideFontFiles() throws IOException {
        return Files.find(path, 1, (p, a) ->
            FONT_FILE_PATTERN.matcher(p.getFileName().toString()).matches());
    }

    public Stream<Path> provideAmigaSoundFiles() throws IOException {
        return Files.find(path, 1, (p, a) ->
            AMIGA_SOUND_FILE_PATTERN.matcher(p.getFileName().toString()).matches());
    }

    public Stream<Path> provideLecFiles() throws IOException {
        return Files.find(path, 1, (p, a) ->
            p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lec"));
    }

}
