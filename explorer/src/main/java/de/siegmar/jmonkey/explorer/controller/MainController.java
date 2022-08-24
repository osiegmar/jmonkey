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

package de.siegmar.jmonkey.explorer.controller;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import de.siegmar.jmonkey.explorer.util.FXUtil;
import de.siegmar.jmonkey.explorer.util.FileType;
import de.siegmar.jmonkey.explorer.util.FileTypeDetector;
import de.siegmar.jmonkey.explorer.util.Resource;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class MainController {

    @FXML
    private MenuBar menubar;

    @FXML
    private Pane contentPane;

    public void openGame(final ActionEvent e) throws IOException {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open game");

        final File file = chooser.showDialog(null);
        if (file == null) {
            return;
        }

        final Path dir = file.toPath();
        if (!checkGameDir(dir)) {
            new Alert(Alert.AlertType.ERROR, "Required game files not found", ButtonType.CLOSE).show();
        }

        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/game_explorer.fxml"));

        final Node explorer = fxmlLoader.load();
        final GameExplorerController controller = fxmlLoader.getController();
        controller.setMenuBar(menubar);
        controller.openGame(dir);

        FXUtil.prepare(contentPane, (Region) explorer);
    }

    private boolean checkGameDir(final Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files
                .anyMatch(f -> f.getFileName().toString().matches("(?i)^000.LFL$"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void openFile(final ActionEvent e) throws IOException {
        final FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("lec file", "*.lec", "*.lfl"));

        chooser.setTitle("Open file");
        final File file = chooser.showOpenDialog(null);
        if (file == null) {
            return;
        }

        final Path path = file.toPath();
        final Optional<FileType> fileType = FileTypeDetector.detect(path);

        if (fileType.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "File type not detected", ButtonType.CLOSE).show();
        } else {
            final Region region = (Region) switch (fileType.get()) {
                case INDEX -> Resource.openIndex(path);
                case FONT -> Resource.openFont(path);
                case DATA -> Resource.openLec(path);
            };

            FXUtil.prepare(contentPane, region);
        }
    }

    public void about(final ActionEvent e) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setContentText("Monkey Island Explorer (C) 2021 by Oliver Siegmar");
        alert.showAndWait();
    }

    public void quit(final ActionEvent e) {
        Platform.exit();
    }

}
