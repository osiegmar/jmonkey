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

package util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.LecScanner;
import de.siegmar.jmonkey.testhelper.TestUtil;

public class LecChunkSourceArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<LecChunkSource> {

    private DataChunkType dataChunkType;

    @Override
    public void accept(final LecChunkSource lecChunkSource) {
        dataChunkType = lecChunkSource.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
        final List<Arguments> resources = new ArrayList<>();

        for (final Path lecFile : TestUtil.provideLecFiles().toList()) {
            LecScanner.scanTree(lecFile).deepStream()
                .filter(c -> c.chunk().type() == dataChunkType)
                .forEach(c -> resources.add(Arguments.of(new LecFile(lecFile), c)));
        }

        return resources.stream();
    }

}
