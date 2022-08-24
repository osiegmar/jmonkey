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

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class PathArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<PathsProvider> {

    private PathType pathType;

    @Override
    public void accept(final PathsProvider pathsProvider) {
        pathType = pathsProvider.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
        final var paths = switch (pathType) {
            case GAME_DIR -> TestUtil.provideGameDir();
            case INDEX -> TestUtil.provideIndexFiles();
            case FONT -> TestUtil.provideFontFiles();
            case DATA -> TestUtil.provideLecFiles();
        };

        return paths.map(Arguments::of);
    }

}
