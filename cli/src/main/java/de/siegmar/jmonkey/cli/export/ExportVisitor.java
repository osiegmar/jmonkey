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

package de.siegmar.jmonkey.cli.export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.siegmar.jmonkey.cli.StatusInfo;
import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.RoomOffset;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecFile;
import de.siegmar.jmonkey.lecscanner.LecVisitor;

public class ExportVisitor implements LecVisitor {

    private final Path outDir;
    private final Index index;
    private final AtomicReference<Integer> lastROPos = new AtomicReference<>();
    private final List<Integer> roomIds = new ArrayList<>();
    private LfExporter lfExporter;

    public ExportVisitor(final Path outDir, final Index index) {
        this.outDir = outDir;
        this.index = index;
    }

    public List<Integer> getRoomIds() {
        return List.copyOf(roomIds);
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public boolean visit(final LecFile lecFile, final LecChunk chunk) {
        StatusInfo.status("Export chunk '%s'", chunk.type().name());
        switch (chunk.type()) {
            case LE, FO, LC, NL, SL -> {
                // ignore
            }
            case LF -> newLflBuilder(lecFile.readChunk(chunk));
            case RO -> lastROPos.set(chunk.pos());
            case BM -> lfExporter.addBM(lecFile.readChunk(chunk));
            case BX -> lfExporter.addBX(lecFile.readChunk(chunk));
            case CC -> lfExporter.addCC(lecFile.readChunk(chunk));
            case EN -> lfExporter.addEN(lecFile.readChunk(chunk));
            case EX -> lfExporter.addEX(lecFile.readChunk(chunk));
            case HD -> lfExporter.addHD(lecFile.readChunk(chunk));
            case LS -> lfExporter.addLS(lecFile.readChunk(chunk));
            case OC -> lfExporter.addOC(lecFile.readChunk(chunk));
            case OI -> lfExporter.addOI(lecFile.readChunk(chunk));
            case PA -> lfExporter.addPA(lecFile.readChunk(chunk));
            case SA -> lfExporter.addSA(lecFile.readChunk(chunk));
            case SP -> lfExporter.addSP(lecFile.readChunk(chunk));
            case SC -> {
                final int relativePos = chunk.pos() - lastROPos.get();
                final int scriptId = findItemId(index.scripts(), relativePos);
                lfExporter.addSC(scriptId, lecFile.readChunk(chunk));
            }
            case CO -> {
                final int relativePos = chunk.pos() - lastROPos.get();
                final int costumeId = findItemId(index.costumes(), relativePos);
                lfExporter.addCO(costumeId, lecFile.readChunk(chunk));
            }
            case SO -> {
                final int relativePos = chunk.pos() - lastROPos.get();
                lfExporter.addSO(findItemId(index.sounds(), relativePos));
            }
            case WA -> lfExporter.addWA(lecFile.readChunk(chunk));
            case AD -> lfExporter.addAD(lecFile.readChunk(chunk));
            case AM -> {
                final int relativePos = chunk.pos() - lastROPos.get();
                final int itemId = findItemId(index.sounds(), relativePos);
                lfExporter.addAM(itemId, lecFile.readChunk(chunk));
            }
            case ROL -> {
                final int relativePos = chunk.pos() - lastROPos.get();
                final int itemId = findItemId(index.sounds(), relativePos);
                lfExporter.addROL(itemId, lecFile.readChunk(chunk));
            }
            default -> throw new IllegalStateException("Unknown chunk: " + chunk);
        }

        StatusInfo.success();

        return true;
    }

    private void newLflBuilder(final BasicChunk readChunk) {
        finalizeLF();
        lfExporter = new LfExporter(readChunk, index, outDir);
    }

    private void finalizeLF() {
        if (lfExporter != null) {
            roomIds.add(lfExporter.getRoomId());
            lfExporter.end();
        }
    }

    private int findItemId(final List<RoomOffset> offsets, final int relativePos) {
        final List<Integer> elements = offsets.stream()
            .filter(c -> c.roomId() == lfExporter.getRoomId())
            .filter(c -> c.roomOffset() == relativePos)
            .map(RoomOffset::itemId)
            .toList();

        if (elements.size() != 1) {
            throw new IllegalStateException("Found %d elements for relativePos %d in room %d"
                .formatted(elements.size(), relativePos, lfExporter.getRoomId()));
        }

        return elements.get(0);
    }

    @Override
    public void end() {
        finalizeLF();
    }

}
