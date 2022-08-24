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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.siegmar.jmonkey.cli.RoomMeta;
import de.siegmar.jmonkey.cli.StatusInfo;
import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.decoder.BufferedImageAdapter;
import de.siegmar.jmonkey.decoder.LecFileScriptPrintDecoder;
import de.siegmar.jmonkey.decoder.header.ChunkLF;
import de.siegmar.jmonkey.decoder.header.ChunkLFDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkLSDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ChunkOCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkSA;
import de.siegmar.jmonkey.decoder.room.ChunkSADecoder;
import de.siegmar.jmonkey.decoder.room.ObjectImageMeta;
import de.siegmar.jmonkey.decoder.room.box.ChunkBX;
import de.siegmar.jmonkey.decoder.room.box.ChunkBXDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.ChunkHDDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkOIDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkPADecoder;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.PrintOpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.Script;
import de.siegmar.jmonkey.decoder.script.ScriptDecoder;
import de.siegmar.jmonkey.decoder.script.ScummOptimizer;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.ObjectMeta;
import de.siegmar.jmonkey.index.RoomName;

@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class LfExporter {

    private final ObjectWriter objectWriter = new ObjectMapper()
        .writerWithDefaultPrettyPrinter();

    private final ScriptDecoder scriptDecoder = new ScriptDecoder();

    private final Index index;
    private final Path outDir;
    private final int roomId;
    private final List<ChunkOC> parkOC = new ArrayList<>();
    private final List<BasicChunk> parkOI = new ArrayList<>();
    private final List<Integer> soundIds = new ArrayList<>();
    private final List<Integer> objectImageIds = new ArrayList<>();
    private final List<Integer> objectIds = new ArrayList<>();
    private final List<Integer> costumeIds = new ArrayList<>();
    private final List<Integer> scriptIds = new ArrayList<>();
    private ChunkHD hdChunk;
    private BasicChunk parkBM;
    private ColorPalette paChunk;
    private Integer lastSOId;

    public LfExporter(final BasicChunk readChunk, final Index index, final Path expDir) {
        this.index = index;

        final ChunkLF chunkLF = ChunkLFDecoder.decode(readChunk);
        roomId = chunkLF.roomId();
        final String roomName = nameOfRoom();

        try {
            this.outDir = Files.createDirectories(expDir.resolve("rooms").resolve(roomName));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        StatusInfo.status("Export room #%03d (%s)", roomId, roomName);
    }

    public int getRoomId() {
        return roomId;
    }

    private String nameOfRoom() {
        return index.roomNames().stream()
            .filter(rn -> roomId == rn.roomId())
            .findFirst()
            .map(RoomName::name)
            .orElse("unknown-" + roomId);
    }

    public void addSO(final int itemId) {
        soundIds.add(itemId);
        lastSOId = itemId;
    }

    public void addWA(final BasicChunk readChunk) {
        readChunk.data().writeTo(soundDir(lastSOId).resolve("WA.bin"));
    }

    public void addAD(final BasicChunk readChunk) {
        readChunk.data().writeTo(soundDir(lastSOId).resolve("AD.bin"));
    }

    public void addAM(final int itemId, final BasicChunk readChunk) {
        soundIds.add(itemId);
        readChunk.data().writeTo(soundDir(itemId).resolve("AM.bin"));
    }

    public void addROL(final int itemId, final BasicChunk readChunk) {
        soundIds.add(itemId);
        readChunk.data().writeTo(soundDir(itemId).resolve("ROL.bin"));
    }

    public void addHD(final BasicChunk readChunk) {
        readChunk.data().writeTo(backgroundDir().resolve("HD.bin"));
        hdChunk = ChunkHDDecoder.decode(readChunk);
    }

    public void addCC(final BasicChunk readChunk) {
        readChunk.data().writeTo(paletteDir().resolve("CC.bin"));
    }

    public void addBM(final BasicChunk readChunk) {
        readChunk.data().writeTo(backgroundDir().resolve("BM.bin"));
        parkBM = readChunk;
    }

    public void addPA(final BasicChunk readChunk) {
        readChunk.data().writeTo(paletteDir().resolve("PA.bin"));
        paChunk = ChunkPADecoder.readPalette(readChunk);
    }

    public void end() {
        if (parkBM != null) {
            final Optional<LayeredImage> image = ChunkBMDecoder.decode(parkBM, hdChunk, paChunk);
            image.ifPresent(i -> {
                writeImage(i, backgroundDir().resolve("image.png").toFile());
                final List<MaskLayer> masks = i.getMasks();
                for (int j = 0; j < masks.size(); j++) {
                    final MaskLayer mask = masks.get(j);
                    final BufferedImageAdapter bia = new BufferedImageAdapter();
                    mask.writeTo(bia, 0xFFFFFFFF, 0);

                    writeImage(bia, backgroundDir().resolve("image_mask_%d.png".formatted(j)).toFile());
                }
            });
        }

        for (final BasicChunk p : parkOI) {
            final ObjectImageMeta objectImageMeta = ChunkOIDecoder.decodeImageMeta(p);
            final ChunkOC chunkOC = parkOC.stream()
                .filter(oc -> objectImageMeta.objectId() == oc.objectId())
                .findFirst()
                .orElseThrow();

            final Optional<LayeredImage> objectImage = ChunkOIDecoder.decodeImage(p, chunkOC, paChunk);
            objectImage.ifPresent(oimg -> {
                final String filename = "object.png";
                writeImage(oimg, objectsDir(chunkOC.objectId()).resolve(filename).toFile());
            });
        }

        try {
            objectWriter.writeValue(outDir.resolve("room.json").toFile(),
                new RoomMeta(roomId, soundIds, objectIds, objectImageIds, costumeIds, scriptIds));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        StatusInfo.success();
    }

    private void writeImage(final LayeredImage i, final File output) {
        final BufferedImageAdapter bia = new BufferedImageAdapter();
        i.writeTo(bia);
        writeImage(bia, output);
    }

    private void writeImage(final BufferedImageAdapter bia, final File output) {
        try {
            ImageIO.write(bia.getImage(), "PNG", output);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void addBX(final BasicChunk readChunk) {
        readChunk.data().writeTo(backgroundDir().resolve("BX.bin"));
        final ChunkBX chunkBX = ChunkBXDecoder.decode(readChunk);
        writeJson(chunkBX, backgroundDir().resolve("boxes.json"));
    }

    private void writeJson(final Object obj, final Path path) {
        try {
            objectWriter.writeValue(path.toFile(), obj);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void addOC(final BasicChunk readChunk) {
        final ChunkOC chunkOC = ChunkOCDecoder.decode(readChunk);
        parkOC.add(chunkOC);
        readChunk.data().writeTo(objectsDir(chunkOC.objectId()).resolve("OC.bin"));

        dumpScript(objectsDir(chunkOC.objectId()).resolve("OC.scu"),
            decoder -> decoder.decodeOC(ChunkOCDecoder.decode(readChunk)));

        final Optional<ObjectMeta> objectMeta = index.objects().stream()
            .filter(o -> chunkOC.objectId() == o.objectId())
            .findFirst();

        if (objectMeta.isPresent()) {
            try {
                objectWriter.writeValue(objectsDir(chunkOC.objectId())
                    .resolve("object.json").toFile(), objectMeta.get());
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        objectIds.add(chunkOC.objectId());
    }

    public void addOI(final BasicChunk readChunk) {
        parkOI.add(readChunk);
        final ObjectImageMeta objectImageMeta = ChunkOIDecoder.decodeImageMeta(readChunk);
        readChunk.data().writeTo(objectsDir(objectImageMeta.objectId()).resolve("OI.bin"));
        objectImageIds.add(objectImageMeta.objectId());
    }

    public void addSA(final BasicChunk readChunk) {
        readChunk.data().writeTo(backgroundDir().resolve("SA.bin"));
        final ChunkSA chunkSA = ChunkSADecoder.decode(readChunk);
        writeJson(chunkSA, backgroundDir().resolve("scale_slots.json"));
    }

    public void addSP(final BasicChunk readChunk) {
        readChunk.data().writeTo(paletteDir().resolve("SP.bin"));
    }

    public void addCO(final int costumeId, final BasicChunk readChunk) {
        costumeIds.add(costumeId);
        readChunk.data().writeTo(costumesDir(costumeId).resolve("CO.bin"));
    }

    public void addEN(final BasicChunk readChunk) {
        readChunk.data().writeTo(scriptsDir().resolve("EN.bin"));
        dumpScript(scriptsDir().resolve(scriptsDir().resolve("EN.scu")),
            decoder -> decoder.decodeGeneric(readChunk.data()));
    }

    public void addEX(final BasicChunk readChunk) {
        readChunk.data().writeTo(scriptsDir().resolve("EX.bin"));
        dumpScript(scriptsDir().resolve(scriptsDir().resolve("EX.scu")),
            decoder -> decoder.decodeGeneric(readChunk.data()));
    }

    public void addLS(final BasicChunk readChunk) {
        final ChunkLS chunkLS = ChunkLSDecoder.decode(readChunk);

        final String baseFilename = "LS_%03d".formatted(chunkLS.scriptId());
        chunkLS.scriptData().writeTo(scriptsDir().resolve(baseFilename + ".bin"));
        dumpScript(scriptsDir().resolve(baseFilename + ".scu"),
            decoder -> decoder.decodeGeneric(chunkLS.scriptData()));
    }

    public void addSC(final int scriptId, final BasicChunk readChunk) {
        final String baseFilename = "SC_%03d".formatted(scriptId);
        readChunk.data().writeTo(scriptsDir().resolve(baseFilename + ".bin"));

        dumpScript(scriptsDir().resolve(baseFilename + ".scu"),
            decoder -> decoder.decodeGeneric(readChunk.data()));

        scriptIds.add(scriptId);
    }

    private void dumpScript(final Path file, final Function<LecFileScriptPrintDecoder, Script> cn) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            final List<String> statements = new ArrayList<>();
            final OpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(statements::add);
            final LecFileScriptPrintDecoder lfspd =
                new LecFileScriptPrintDecoder(writer, scriptDecoder, opcodeDelegate);
            final Script script = cn.apply(lfspd);

            opcodeDelegate.setScript(script);
            script.execute();

            for (final String statement : ScummOptimizer.optimize(statements)) {
                writer.append(statement).append('\n');
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path paletteDir() {
        return getPath("palette");
    }

    private Path backgroundDir() {
        return getPath("background");
    }

    private Path soundDir(final int soundId) {
        try {
            return Files.createDirectories(outDir.resolve("sounds")
                .resolve("%03d".formatted(soundId)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path objectsDir(final int objectId) {
        try {
            return Files.createDirectories(outDir.resolve("objects")
                .resolve("%03d".formatted(objectId)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path costumesDir(final int costumeId) {
        try {
            return Files.createDirectories(outDir.resolve("costumes")
                .resolve("%03d".formatted(costumeId)));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path scriptsDir() {
        return getPath("scripts");
    }

    private Path getPath(final String subDirName) {
        try {
            return Files.createDirectories(outDir.resolve(subDirName));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
