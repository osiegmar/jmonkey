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

import java.io.StringWriter;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.decoder.LecFileScriptPrintDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkLSDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ChunkOCDecoder;

public class JMonkeyAdapter implements ScriptAdapter {

    private final StringWriter sw = new StringWriter();
    private final ScriptDecoder scriptDecoder = new ScriptDecoder();
    private final OpcodeDelegate opcodeDelegate = new ScummVMPrintOpcodeDelegate(sw);
    private final LecFileScriptPrintDecoder lfspd = new LecFileScriptPrintDecoder(sw, scriptDecoder, opcodeDelegate);

    @Override
    public String decode(final BasicChunk chunk) {
        final StringBuilder sb = new StringBuilder();

        sw.getBuffer().setLength(0);

        final Script script;
        if ("LS".equals(chunk.header().name())) {
            final ChunkLS chunksLS = ChunkLSDecoder.decode(chunk);
            script = lfspd.decodeLS(chunksLS);
        } else if ("OC".equals(chunk.header().name())) {
            final ChunkOC chunkOC = ChunkOCDecoder.decode(chunk);
            script = lfspd.decodeOC(chunkOC);
        } else {
            script = lfspd.decodeGeneric(chunk.data());
        }

        opcodeDelegate.setScript(script);
        script.execute();
        sb.append(sw);

        return sb.toString();
    }

}
