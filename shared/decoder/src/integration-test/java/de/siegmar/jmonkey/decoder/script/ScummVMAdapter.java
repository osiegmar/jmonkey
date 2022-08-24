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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import de.siegmar.jmonkey.commons.io.BasicChunk;

/**
 * Adapter for descumm (tested with version 2.6.0).
 */
class ScummVMAdapter implements ScriptAdapter {

    private final Path dstFile;
    private final ProcessBuilder pb;

    ScummVMAdapter() {
        try {
            dstFile = Files.createTempDirectory("scummvm").resolve("chunk.bin");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        pb = new ProcessBuilder("descumm", "-4", "-i", "-e", "-f", "-w", "-b",
            dstFile.toAbsolutePath().toString());
        pb.redirectErrorStream(true);
    }

    @Override
    public String decode(final BasicChunk chunk) {
        try {
            try (OutputStream out = Files.newOutputStream(dstFile)) {
                chunk.dataWithHeader().writeTo(out);
            }

            final Process process = pb.start();
            final StringBuilder sb = new StringBuilder();
            try (InputStreamReader in = new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
                 BufferedReader br = new BufferedReader(in)) {
                final int returnCode = process.waitFor();

                if (returnCode != 0) {
                    throw new IllegalStateException(consume(br));
                }

                br.lines().forEach(line -> sb.append(cleanLine(line)).append('\n'));
            }
            return sb.toString();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                Files.deleteIfExists(dstFile);
            } catch (final IOException ignored) {
                // ignore
            }
        }
    }

    @SuppressWarnings("checkstyle:NPathComplexity")
    private static String cleanLine(final String line) {
        if (line.contains("/*")) {
            /* goto 0083; */
            return line.replace("/* ", "").replace(" */", "");
        }

        String retLine = line;
        if (retLine.contains("PutCodeInString")) {
            /*
                PutCodeInString(30, "Threepwood");
                -------------------^
             */
            retLine = retLine.replaceFirst(", ", ",");
        }

        if (retLine.contains(");]")) {
            /*
                VerbOps(Var[100],[SetXY(16,Var[228]),SetToString(24),On(),Key(Var[229]);]);
                -----------------------------------------------------------------------^
             */
            retLine = retLine.replace(";]);", "]);");
        }

        if (retLine.contains(")keepText(")) {
            /*
                print(255,[Color(Local[8]),Center(),Text(getString(VAR_HEAPSPACE)keepText())]);
                -----------------------------------------------------------------^
             */
            retLine = retLine.replace(")keepText(", ") + keepText(");
        }

        if (retLine.contains(")newline(")) {
            retLine = retLine.replace(")newline(", ") + newline(");
        }

        if (retLine.contains(")getString(")) {
            retLine = retLine.replace(")getString(", ") + getString(");
        }

        if (retLine.contains("unknown8(8224) + \"")) {
            // descumm doesn't know verbNewline()
            retLine = retLine.replace("unknown8(8224) + \"", "verbNewline() + \"  ");
        }

        if (retLine.matches(".*(RoomColor|RoomScroll|SetPalColor|SetScreen).*")) {
            /*
                SetScreen(0,200)
                                ^
             */
            retLine += ";";
        }

        return retLine;
    }

    private static String consume(final BufferedReader br) throws IOException {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        String l;
        while ((l = br.readLine()) != null) {
            pw.println(l);
        }
        return sw.toString();
    }

}
