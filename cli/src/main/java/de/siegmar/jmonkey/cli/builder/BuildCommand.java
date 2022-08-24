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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.siegmar.jmonkey.cli.RoomDisk;
import de.siegmar.jmonkey.cli.RoomInfo;
import de.siegmar.jmonkey.cli.RoomMeta;
import de.siegmar.jmonkey.cli.StatusInfo;
import de.siegmar.jmonkey.encoder.index.IndexBuilder;
import de.siegmar.jmonkey.encoder.index.IndexWriter;
import picocli.CommandLine;

@CommandLine.Command(name = "build",
    description = "Build game")
public class BuildCommand implements Runnable {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private Path inputDir;
    private Path outputDir;

    @CommandLine.Option(names = {"-i", "--input"},
        description = "the input (game) directory",
        required = true)
    public void setInputDir(final Path inputDir) {
        this.inputDir = getValidatedDir(inputDir);
    }

    @CommandLine.Option(names = {"-o", "--output"},
        description = "the output directory",
        required = true)
    public void setOutputDir(final Path outputDir) {
        this.outputDir = getValidatedDir(outputDir);
    }

    private Path getValidatedDir(final Path dir) {
        if (Files.notExists(dir)) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Directory '%s' does not exist.", dir));
        }

        if (!Files.isDirectory(dir)) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Path '%s' is not a directory.", dir));
        }

        return dir;
    }

    @Override
    public void run() {
        try {
            final List<Path> roomDirs = Files.list(inputDir.resolve("rooms"))
                .toList();

            final IndexBuilder indexBuilder = new IndexBuilder();

            // Build LFL files
            final List<LflFile> lflFiles = new ArrayList<>();
            StatusInfo.status("Build rooms");
            for (final Path roomDir : roomDirs) {
                final Path roomMetaFile = roomDir.resolve("room.json");

                if (Files.exists(roomMetaFile)) {
                    final RoomMeta roomMeta = new ObjectMapper().readValue(roomMetaFile.toFile(), RoomMeta.class);
                    final Path lflFile = outputDir.resolve("%03d.LFL".formatted(roomMeta.roomId()));

                    StatusInfo.status("Build LFL file %s", lflFile);
                    lflFiles.add(new LflBuilder(roomMeta, roomDir, indexBuilder, lflFile).buildLFLFile());
                    StatusInfo.success();
                }
            }
            StatusInfo.success();

            final RoomInfo roomInfo = new ObjectMapper()
                .readValue(inputDir.resolve("room_info.json").toFile(), RoomInfo.class);

            final List<RoomDisk> roomDisks = roomInfo.roomDisks();
            for (int i = 0; i < roomDisks.size(); i++) {
                final List<LflFile> fileRooms = roomDisks.get(i).roomIds().stream()
                    .map(roomId -> lflFiles.stream().filter(l -> l.roomId() == roomId).findFirst().orElseThrow())
                    .toList();

                final int fileNo = i + 1;
                LecBuilder.buildLecFile(fileRooms, indexBuilder, outputDir, fileNo);
            }

            // Create index file
            final Path indexFile = outputDir.resolve("000.LFL");
            IndexWriter.writeIndex(indexBuilder.build(), indexFile);

            // Copy font files
            copyFiles(Files.list(inputDir.resolve("fonts")).toList(), outputDir);

            final Path amigasoundDir = inputDir.resolve("amigasound");
            final List<Path> amigaSoundFiles = Files.exists(amigasoundDir)
                ? Files.list(inputDir.resolve("amigasound")).toList() : List.of();
            if (!amigaSoundFiles.isEmpty()) {
                copyFiles(amigaSoundFiles, outputDir);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copyFiles(final List<Path> files, final Path target) throws IOException {
        Files.createDirectories(target);
        files.forEach(file -> {
            try {
                StatusInfo.status("Copy %s", file);
                Files.copy(file, target.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                StatusInfo.success();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
