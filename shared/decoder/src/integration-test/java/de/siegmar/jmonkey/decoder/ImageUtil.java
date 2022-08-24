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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public final class ImageUtil {

    private static final boolean WRITE_TO_DISK = false;

    private ImageUtil() {
    }

    public static void write(final BufferedImage bufferedImage, final String id) {
        try {
            if (WRITE_TO_DISK) {
                final Path tmpDir = Files.createTempDirectory("monkey");
                ImageIO.write(bufferedImage, "PNG", tmpDir.resolve(id + ".png").toFile());
            } else {
                ImageIO.write(bufferedImage, "PNG", OutputStream.nullOutputStream());
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
