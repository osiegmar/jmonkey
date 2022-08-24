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

import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.explorer.misc.JavaFxImageAdapter;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class RoomPreviewController {

    @FXML
    private ImageView roomImage;

    @FXML
    private Label roomName;

    public void setImage(final LayeredImage image) {
        final JavaFxImageAdapter javaFxImageAdapter = new JavaFxImageAdapter();
        image.writeTo(javaFxImageAdapter);
        roomImage.setImage(javaFxImageAdapter.getImage());

        final int x = (image.getWidth() - 320) / 2;
        final int y = (image.getHeight() - 200) / 2;
        roomImage.setViewport(new Rectangle2D(x, y, 320, 200));
    }

    public void setTitle(final String name) {
        roomName.setText(name);
    }

}
