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

package de.siegmar.jmonkey.explorer;

import java.awt.Taskbar;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.siegmar.jmonkey.explorer.util.Resource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@SuppressWarnings("checkstyle:UncommentedMain")
public class MonkeyExplorer extends Application {

    private static final String LOGO_PATH = "/icon/monkey.png";

    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws IOException {
        stage.getIcons().add(new Image(Resource.stream(LOGO_PATH)));

        // Set the dock icon for macOS
        if (Taskbar.isTaskbarSupported()) {
            final Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                taskbar.setIconImage(ImageIO.read(Resource.stream(LOGO_PATH)));
            }
        }

        final Parent root = new FXMLLoader(Resource.url("/jfxml/main.fxml")).load();

        final Scene scene = new Scene(root);
        stage.setTitle("Monkey Island Explorer");
        stage.setScene(scene);
        stage.show();
    }

}
