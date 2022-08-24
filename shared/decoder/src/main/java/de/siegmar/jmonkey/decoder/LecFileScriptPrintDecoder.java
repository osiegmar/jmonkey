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

package de.siegmar.jmonkey.decoder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.Script;
import de.siegmar.jmonkey.decoder.script.ScriptDecoder;

public class LecFileScriptPrintDecoder {

    private final Writer writer;
    private final ScriptDecoder scriptDecoder;
    private final OpcodeDelegate opcodeDelegate;

    public LecFileScriptPrintDecoder(final Writer writer, final ScriptDecoder scriptDecoder,
                                     final OpcodeDelegate opcodeDelegate) {
        this.writer = writer;
        this.scriptDecoder = scriptDecoder;
        this.opcodeDelegate = opcodeDelegate;
    }

    public Script decodeGeneric(final ByteString scriptData) {
        return buildScript(scriptData, 0);
    }

    public Script decodeLS(final ChunkLS scriptData) {
        write("Script# %d%n", scriptData.scriptId());
        final var data = scriptData.scriptData();
        return buildScript(data, 0);
    }

    public Script decodeOC(final ChunkOC oc) {
        write("Events:%n");
        for (final Map.Entry<Integer, Integer> entry : oc.offsets().entrySet()) {
            write("%4X - %04X%n", entry.getKey(), entry.getValue());
        }
        return buildScript(oc.entireChunk(), oc.scriptStart());
    }

    private Script buildScript(final ByteString scriptData, final int scriptPos) {
        return new Script(-1, scriptDecoder, opcodeDelegate, scriptData, scriptPos, List.of(), false, false);
    }

    private void write(final String msg, final Object... args) {
        try {
            writer.write(msg.formatted(args));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
