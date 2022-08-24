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

package de.siegmar.jmonkey.cli.builder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.cli.StatusInfo;
import de.siegmar.jmonkey.commons.io.XorOutputStream;
import de.siegmar.jmonkey.decoder.header.ChunkFO;
import de.siegmar.jmonkey.decoder.header.ChunkFOItem;
import de.siegmar.jmonkey.encoder.ChunkFOEncoder;
import de.siegmar.jmonkey.encoder.LeWriter;
import de.siegmar.jmonkey.encoder.Node;
import de.siegmar.jmonkey.encoder.index.IndexBuilder;
import de.siegmar.jmonkey.index.RoomDirectory;

public final class LecBuilder {

    private LecBuilder() {
    }

    static void buildLecFile(final List<LflFile> lflFiles, final IndexBuilder indexBuilder,
                             final Path outputDir, final int fileNo) throws IOException {

        final Path lecFile = outputDir.resolve("DISK%02d.LEC".formatted(fileNo));
        StatusInfo.status("Build LEC file %s", lecFile);

        int offset = 0;

        final Node le = Node.createRoot("LE");
        offset += 6;

        final Node fo = le.createChild("FO");
        offset += 6 + 1 + (5 * lflFiles.size());

        final List<ChunkFOItem> chunkFOItems = new ArrayList<>();
        for (final LflFile lflFile : lflFiles) {
            final int roomId = lflFile.roomId();
            chunkFOItems.add(new ChunkFOItem(roomId, offset));
            indexBuilder.addRoomLocation(new RoomDirectory(roomId, fileNo));
            offset += Files.size(lflFile.file());
        }

        fo.setData(ChunkFOEncoder.encode(new ChunkFO(chunkFOItems)));

        for (final LflFile lflFile : lflFiles) {
            le.createChild("LF").setDataWithHeader(lflFile.file());
        }

        try (OutputStream fout = new BufferedOutputStream(Files.newOutputStream(lecFile));
             XorOutputStream xorOutputStream = new XorOutputStream(fout, (byte) 0x69);
             LeWriter leWriter = new LeWriter(xorOutputStream)) {

            le.writeTo(leWriter);
        }

        StatusInfo.success();
    }

}
