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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import util.LecChunkSource;

class ScriptTest {

    private static final boolean COMPARE_WITH_SCUMMVM = false;

    private static final ScriptAdapter JMONKEY_ADAPTER = new JMonkeyAdapter();
    private static final ScriptAdapter SCUMMVM_ADAPTER = new ScummVMAdapter();

    @ParameterizedTest
    @LecChunkSource(DataChunkType.SC)
    void globalScripts(final LecFile lecFile, final TreeIndex<LecChunk> scNode) {
        test(lecFile, scNode);
    }

    @ParameterizedTest
    @LecChunkSource(DataChunkType.LS)
    void localScripts(final LecFile lecFile, final TreeIndex<LecChunk> lsNode) {
        test(lecFile, lsNode);
    }

    @ParameterizedTest
    @LecChunkSource(DataChunkType.EN)
    void entryScripts(final LecFile lecFile, final TreeIndex<LecChunk> enNode) {
        test(lecFile, enNode);
    }

    @ParameterizedTest
    @LecChunkSource(DataChunkType.EX)
    void exitScripts(final LecFile lecFile, final TreeIndex<LecChunk> exNode) {
        test(lecFile, exNode);
    }

    @ParameterizedTest
    @LecChunkSource(DataChunkType.OC)
    void objectScripts(final LecFile lecFile, final TreeIndex<LecChunk> ocNode) {
        test(lecFile, ocNode);
    }

    private static void test(final LecFile lecFile, final TreeIndex<LecChunk> scNode) {
        final BasicChunk basicChunk = lecFile.readChunk(scNode.chunk());

        final String decoded = JMONKEY_ADAPTER.decode(basicChunk);
        assertThat(decoded).endsWith("END\n");

        if (COMPARE_WITH_SCUMMVM) {
            assertEquals(SCUMMVM_ADAPTER.decode(basicChunk), decoded);
        }
    }

}
