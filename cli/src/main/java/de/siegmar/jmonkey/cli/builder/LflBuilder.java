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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.siegmar.jmonkey.cli.RoomMeta;
import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.decoder.header.ChunkLF;
import de.siegmar.jmonkey.decoder.room.ChunkLC;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkNL;
import de.siegmar.jmonkey.decoder.room.ChunkSL;
import de.siegmar.jmonkey.encoder.ChunkLCEncoder;
import de.siegmar.jmonkey.encoder.ChunkLFEncoder;
import de.siegmar.jmonkey.encoder.ChunkLSEncoder;
import de.siegmar.jmonkey.encoder.ChunkNLEncoder;
import de.siegmar.jmonkey.encoder.ChunkSLEncoder;
import de.siegmar.jmonkey.encoder.LeWriter;
import de.siegmar.jmonkey.encoder.Node;
import de.siegmar.jmonkey.encoder.index.IndexBuilder;
import de.siegmar.jmonkey.encoder.script.ScummCompiler;
import de.siegmar.jmonkey.encoder.script.parser.ScummParser;
import de.siegmar.jmonkey.encoder.script.parser.ScummTokenizer;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;
import de.siegmar.jmonkey.index.ObjectMeta;
import de.siegmar.jmonkey.index.RoomOffset;

@SuppressWarnings({
    "checkstyle:ClassDataAbstractionCoupling",
    "checkstyle:ClassFanOutComplexity"
})
public final class LflBuilder {

    private static final String ID_PATTERN = "%03d";

    private final RoomMeta roomMeta;
    private final Path roomDir;
    private final IndexBuilder indexBuilder;
    private final Path lflFile;
    private final Path backgroundDir;
    private final Path paletteDir;
    private final Path scriptsDir;
    private final Path objectsDir;
    private final Path soundsDir;
    private final Path costumesDir;

    LflBuilder(final RoomMeta roomMeta, final Path roomDir, final IndexBuilder indexBuilder, final Path lflFile) {
        this.roomMeta = roomMeta;
        this.roomDir = roomDir;
        this.indexBuilder = indexBuilder;
        this.lflFile = lflFile;
        backgroundDir = roomDir.resolve("background");
        paletteDir = roomDir.resolve("palette");
        scriptsDir = roomDir.resolve("scripts");
        objectsDir = roomDir.resolve("objects");
        soundsDir = roomDir.resolve("sounds");
        costumesDir = roomDir.resolve("costumes");
    }

    LflFile buildLFLFile() throws IOException {
        final Node lf = Node.createRoot(ChunkLFEncoder.encode(new ChunkLF(roomMeta.roomId())));

        final Node ro = lf.createChild("RO");

        appendIfExists("HD", ro, backgroundDir.resolve("HD.bin"));
        appendIfExists("CC", ro, paletteDir.resolve("CC.bin"));
        appendIfExists("SP", ro, paletteDir.resolve("SP.bin"));
        appendIfExists("BX", ro, backgroundDir.resolve("BX.bin"));
        appendIfExists("PA", ro, paletteDir.resolve("PA.bin"));
        appendIfExists("SA", ro, backgroundDir.resolve("SA.bin"));
        appendIfExists("BM", ro, backgroundDir.resolve("BM.bin"));

        for (final Integer objectId : roomMeta.objectImageIds()) {
            appendIfExists("OI", ro, objectsDir.resolve(ID_PATTERN.formatted(objectId)).resolve("OI.bin"));
        }

        ro.createChild("NL").setData(ChunkNLEncoder.encode(new ChunkNL(roomMeta.soundIds())));
        ro.createChild("SL").setData(ChunkSLEncoder.encode(new ChunkSL(0)));

        appendObjectScripts(ro);

        appendEX(ro);
        appendEN(ro);
        appendLocalScripts(ro);

        final int roOffset = ro.getOffset();

        appendGlobalScripts(lf, roOffset);
        appendSounds(lf, soundsDir, roOffset);
        appendCostumes(lf, roOffset);

        writeFile(lf);

        indexBuilder.addRoomName(roomMeta.roomId(), filename(roomDir));

        return new LflFile(roomMeta.roomId(), lflFile);
    }

    private void appendEX(final Node ro) throws IOException {
        final Path srcFile = scriptsDir.resolve("EX.scu");

        if (Files.notExists(srcFile)) {
            return;
        }

        if (Files.size(srcFile) == 0) {
            ro.createChild("EX").setData(scriptsDir.resolve("EX.bin"));
            return;
        }

        final String basename = "EX";
        final ByteString compiledProgram = compileAndCompare(basename);

        ro.createChild("EX").setData(compiledProgram);
    }

    private void appendEN(final Node ro) throws IOException {
        final Path srcFile = scriptsDir.resolve("EN.scu");

        if (Files.notExists(srcFile)) {
            return;
        }

        if (Files.size(srcFile) == 0) {
            ro.createChild("EN").setData(scriptsDir.resolve("EN.bin"));
            return;
        }

        final String basename = "EN";
        final ByteString compiledProgram = compileAndCompare(basename);

        ro.createChild("EN").setData(compiledProgram);
    }

    private void appendIfExists(final String name, final Node node, final Path file) {
        if (Files.exists(file)) {
            node.createChild(name).setData(file);
        }
    }

    private void appendObjectScripts(final Node ro) throws IOException {
        for (final Integer objectId : roomMeta.objectIds()) {
            final Path objectDir = objectsDir.resolve(ID_PATTERN.formatted(objectId));
            appendIfExists("OC", ro, objectDir.resolve("OC.bin"));
            final Path objFile = objectDir.resolve("object.json");
            if (Files.exists(objFile)) {
                final ObjectMapper om = new ObjectMapper();
                final ObjectMeta objectMeta = om.readValue(objFile.toFile(), ObjectMeta.class);
                indexBuilder.addObject(objectMeta);
            }
        }
    }

    private void appendLocalScripts(final Node ro) throws IOException {
        final List<Path> localScripts = listFiles(scriptsDir, "^LS_\\d{3}.bin$");

        ro.createChild("LC").setData(ChunkLCEncoder.encode(new ChunkLC(localScripts.size())));

        for (final Path lsFile : localScripts) {
            appendLS(ro, lsFile);
        }
    }

    private void appendLS(final Node ro, final Path lsFile) throws IOException {
        final int scriptId = Integer.parseInt(filename(lsFile)
            .replaceAll("^LS_(\\d+)\\.bin$", "$1"));

        final String basename = "LS_%03d".formatted(scriptId);
        final ByteString compiledProgram = compileAndCompare(basename);

        final BasicChunk basicChunk = ChunkLSEncoder.encode(new ChunkLS(scriptId, compiledProgram));

        ro.createChild(basicChunk);
    }

    private void appendGlobalScripts(final Node lf, final int roOffset) throws IOException {
        for (final Integer scriptId : roomMeta.scriptIds()) {
            appendSC(lf, roOffset, scriptId);
        }
    }

    private void appendSC(final Node lf, final int roOffset, final Integer scriptId) throws IOException {
        final String basename = "SC_%03d".formatted(scriptId);
        final ByteString compiledProgram = compileAndCompare(basename);

        final Node sc = lf.createChild("SC").setData(compiledProgram);

        indexBuilder.addScript(new RoomOffset(scriptId, roomMeta.roomId(), sc.getOffset() - roOffset));
    }

    private ByteString compileAndCompare(final String basename) throws IOException {
        final Path origFile = scriptsDir.resolve(basename + ".bin");
        final Path srcFile = scriptsDir.resolve(basename + ".scu");
        final Path compiledFile = scriptsDir.resolve(basename + ".compiled");

        final Program parsedProgram = new ScummParser(new ScummTokenizer(Files.readString(srcFile))).parse();
        final ByteString compiledProgram = new ScummCompiler().compile(parsedProgram);

        compiledProgram.writeTo(compiledFile);

        if (Files.mismatch(origFile, compiledFile) != -1) {
            System.out.println("DIFFER: " + origFile + " / " + compiledFile);
        }
        return compiledProgram;
    }

    private void appendSounds(final Node lf, final Path soundDirs, final int roOffset) {
        for (final Integer soundId : roomMeta.soundIds()) {
            final Path soundDir = soundDirs.resolve(ID_PATTERN.formatted(soundId));
            System.out.println("Add sound " + soundDir);
            final Path waFile = soundDir.resolve("WA.bin");
            final Path adFile = soundDir.resolve("AD.bin");

            if (Files.exists(waFile)) {
                final Node so = lf.createChild("SO");
                indexBuilder.addSound(new RoomOffset(soundId, roomMeta.roomId(), so.getOffset() - roOffset));

                so.createChild("WA").setData(waFile);
                so.createChild("AD").setData(adFile);
            } else {
                final Path amFile = soundDir.resolve("AM.bin");
                final Path rolFile = soundDir.resolve("ROL.bin");
                if (Files.exists(amFile)) {
                    final Node am = lf.createChild("AM").setData(amFile);
                    indexBuilder.addSound(new RoomOffset(soundId, roomMeta.roomId(), am.getOffset() - roOffset));
                } else if (Files.exists(rolFile)) {
                    final Node rol = lf.createChild("RO").setData(rolFile);
                    indexBuilder.addSound(new RoomOffset(soundId, roomMeta.roomId(), rol.getOffset() - roOffset));
                }
            }
        }
    }

    private void appendCostumes(final Node lf, final int roOffset) throws IOException {
        for (final Integer costumeId : roomMeta.costumeIds()) {
            final Path costumeDir = costumesDir.resolve(ID_PATTERN.formatted(costumeId));
            final Node co = lf.createChild("CO")
                .setData(costumeDir.resolve("CO.bin"));
            indexBuilder.addCostume(new RoomOffset(costumeId, roomMeta.roomId(), co.getOffset() - roOffset));
        }
    }

    private void writeFile(final Node lf) throws IOException {
        try (LeWriter leWriter = new LeWriter(new BufferedOutputStream(Files.newOutputStream(lflFile)))) {
            lf.writeTo(leWriter);
        }
    }

    private static String filename(final Path file) {
        final Path fn = file.getFileName();
        if (fn == null) {
            throw new IllegalArgumentException("Can't get filename of path " + file);
        }
        return fn.toString();
    }

    private List<Path> listFiles(final Path dir, final String regex) throws IOException {
        if (Files.notExists(dir)) {
            return List.of();
        }
        return Files.find(dir, 1, (p, a) -> a.isRegularFile() && filename(p).matches(regex))
            .sorted()
            .toList();
    }

}
