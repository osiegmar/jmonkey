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
import de.siegmar.jmonkey.cli.StatusInfo;
import de.siegmar.jmonkey.commons.misc.GameDir;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.IndexReader;
import de.siegmar.jmonkey.lecscanner.LecScanner;
import picocli.CommandLine;

@CommandLine.Command(name = "export",
    description = "Export game")
public class ExportCommand implements Runnable {

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
            final GameDir gameDir = new GameDir(inputDir);

//            StatusInfo.status("Export game");

            final Path indexfile = gameDir.provideIndexFile();

            StatusInfo.status("Read index");
            final Index index = IndexReader.readFile(indexfile);
            StatusInfo.success();

            // As the index sometimes references non-existing chunks (for unused resource IDs)
            // we have to traverse over the actual data.

            final List<RoomDisk> roomDisks = new ArrayList<>();

            final List<Path> lecFiles = gameDir.provideLecFiles().sorted().toList();
            for (final Path lecFile : lecFiles) {
                StatusInfo.status("Export %s", lecFile);

                final ExportVisitor lecVisitor = new ExportVisitor(outputDir, index);
                LecScanner.scan(lecFile, lecVisitor);
                roomDisks.add(new RoomDisk(lecVisitor.getRoomIds()));

                StatusInfo.success();
            }

            final RoomInfo roomInfo = new RoomInfo(roomDisks);
            new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(
                outputDir.resolve("room_info.json").toFile(), roomInfo);

            StatusInfo.status("Copy font files");
            copyFiles(gameDir.provideFontFiles().toList(), outputDir.resolve("fonts"));
            StatusInfo.success();

            final List<Path> amigaSoundFiles = gameDir.provideAmigaSoundFiles().toList();
            if (!amigaSoundFiles.isEmpty()) {
                StatusInfo.status("Copy amiga sound files");
                copyFiles(amigaSoundFiles, outputDir.resolve("amigasound"));
                StatusInfo.success();
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

//        StatusInfo.success();
//        System.out.printf(AUTO.string("@|bold,green Game successfully exported to '%s'%n|@"), outputDir);
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
