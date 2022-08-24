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

package de.siegmar.jmonkey.explorer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;

import de.siegmar.jmonkey.explorer.controller.ExplorerController;
import de.siegmar.jmonkey.explorer.controller.FontController;
import de.siegmar.jmonkey.explorer.controller.IndexExplorerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class Resource {

    private Resource() {
    }

    public static InputStream stream(final String name) {
        return Resource.class.getResourceAsStream(name);
    }

    public static URL url(final String name) {
        return Resource.class.getResource(name);
    }

    public static ImageView imageView(final String iconName) {
        return new ImageView(image(iconName));
    }

    public static Image image(final String iconName) {
        return new Image(Resource.stream("/icon/%s.png".formatted(iconName)));
    }

    public static Node openIndex(final Path file) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/index_explorer.fxml"));

        final Node explorer = fxmlLoader.load();
        final IndexExplorerController controller = fxmlLoader.getController();
        controller.openIndex(file);

        return explorer;
    }

    public static Node openLec(final Path file) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/explorer.fxml"));

        final Node explorer = fxmlLoader.load();
        final ExplorerController explorerController = fxmlLoader.getController();
        explorerController.openLec(file);

        return explorer;
    }

    public static Node openFont(final Path file) {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/font_viewer.fxml"));

        try {
            final Node explorer = fxmlLoader.load();
            final FontController fontController = fxmlLoader.getController();
            fontController.openFont(file);

            return explorer;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
