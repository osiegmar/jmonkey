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

package de.siegmar.jmonkey.explorer.misc;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public final class ColorPaletteUtil {

    private ColorPaletteUtil() {
    }

    public static StackPane colorTile(final int pos, final int color) {
        final Rectangle rectangle = new Rectangle(35, 35);
        rectangle.setFill(intToColor(color));
        final Text text = new Text(Integer.toString(pos));
        text.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLACK);
        final StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(rectangle, text);

        return stackPane;
    }

    private static Color intToColor(final int value) {
        final int r = (value >>> 16) & 0xFF;
        final int g = (value >>> 8) & 0xFF;
        final int b = value & 0xFF;
        return Color.rgb(r, g, b);
    }

}
