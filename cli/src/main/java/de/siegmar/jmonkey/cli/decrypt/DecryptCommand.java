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

package de.siegmar.jmonkey.cli.decrypt;

import static picocli.CommandLine.Help.Ansi.AUTO;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.siegmar.jmonkey.commons.io.XorInputStream;
import picocli.CommandLine;

@CommandLine.Command(name = "decrypt",
    description = "Decrypt game file")
public class DecryptCommand implements Runnable {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private Path inputFile;

    @CommandLine.Parameters(description = "The .LEC file to decrypt.")
    public void setInputFile(final Path inputFile) {
        this.inputFile = getValidatedFile(inputFile);
    }

    private Path getValidatedFile(final Path file) {
        if (Files.notExists(file)) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("File '%s' does not exist.", file));
        }

        if (!Files.isRegularFile(file)) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Path '%s' is not a file.", file));
        }

        return file;
    }

    @Override
    public void run() {
        final Path fileName = inputFile.getFileName();
        if (fileName == null) {
            throw new IllegalStateException("Filename of '" + inputFile + "' is null");
        }
        final Path outputFile = inputFile.resolveSibling(fileName + ".decrypted");

        try (XorInputStream in = new XorInputStream(Files.newInputStream(inputFile), (byte) 0x69);
             OutputStream out = Files.newOutputStream(outputFile)) {
            in.transferTo(out);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        System.out.printf(AUTO.string("@|bold,green Decrypted '%s' to '%s'%n|@"), inputFile, outputFile);
    }

}
