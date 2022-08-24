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

package de.siegmar.jmonkey.testhelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TestUtil {

    private static final String ENV_VAR = "MONKEY_TEST_BASE";

    private static final Pattern FONT_FILE_PATTERN =
        Pattern.compile("^90[1-4]\\.lfl$", Pattern.CASE_INSENSITIVE);

    private TestUtil() {
    }

    public static Stream<Path> provideGameDir() throws IOException {
        final Path base = monkeyPath();
        return Files.find(base, 1, (p, attr) -> attr.isDirectory())
            .filter(p -> !p.equals(base));
    }

    public static Stream<Path> provideIndexFiles() throws IOException {
        final Path base = TestUtil.monkeyPath();
        return Files.find(base, 2, (path, attr) ->
            "000.lfl".equalsIgnoreCase(path.getFileName().toString()));
    }

    public static Stream<Path> provideFontFiles() throws IOException {
        return Files.find(TestUtil.monkeyPath(), 2, (path, attr) ->
            FONT_FILE_PATTERN.matcher(path.getFileName().toString()).matches());
    }

    public static Stream<Path> provideLecFiles() throws IOException {
        final Path base = TestUtil.monkeyPath();
        return Files.find(base, 2, (path, attr) ->
            path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".lec"));
    }

    public static Path monkeyPath() {
        return provideTestBase()
            .orElseThrow(() -> new IllegalStateException("MONKEY_TEST_BASE not set"));
    }

    private static Optional<Path> provideTestBase() {
        return Optional.ofNullable(System.getenv(ENV_VAR))
            .map(Path::of);
    }

}
