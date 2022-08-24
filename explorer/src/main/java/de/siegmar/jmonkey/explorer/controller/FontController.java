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

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ResourceBundle;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.explorer.misc.ColorPaletteUtil;
import de.siegmar.jmonkey.explorer.model.FontGlyphView;
import de.siegmar.jmonkey.font.Font;
import de.siegmar.jmonkey.font.FontGlyph;
import de.siegmar.jmonkey.font.FontReader;
import de.siegmar.jmonkey.font.FontRenderer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

public class FontController implements Initializable {

    private Font font;

    @FXML
    private TextField fontHeight;

    @FXML
    private TextField bitsPerPixel;

    @FXML
    private TilePane colorTiles;

    @FXML
    private TableView<FontGlyphView> charTable;

    @FXML
    private Canvas singlePreviewCanvas;

    @FXML
    private TextArea previewText;

    @FXML
    private Canvas previewCanvas;

    @FXML
    private Label statusLabel;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        resetCanvas(previewCanvas.getGraphicsContext2D());
        resetCanvas(singlePreviewCanvas.getGraphicsContext2D());
    }

    private void resetCanvas(final GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    public void openFont(final Path file) {
        statusLabel.setText("File: " + file);

        font = FontReader.decodeFont(file);
        fontHeight.setText(String.valueOf(font.fontHeight()));
        bitsPerPixel.setText(String.valueOf(font.bitsPerPixel()));

        final ByteString colors = font.colors();

        final ObservableList<Node> ct = colorTiles.getChildren();
        for (int i = 0; i < colors.size(); i++) {
            ct.add(ColorPaletteUtil.colorTile(i, ColorPalette.EGA.color(colors.get(i))));
        }

        final ObservableList<FontGlyphView> list = FXCollections.observableList(font.glyphs().values().stream()
            .map(fontGlyph -> new FontGlyphView(font, fontGlyph))
            .toList());
        charTable.setItems(list);

        charTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldSel, newSel) -> updateSinglePreview(newSel.getAsciiChar()));

        charTable.getSelectionModel().select(0);

        updatePreview(null);

    }

    public void updateSinglePreview(final int fontGlyph) {
        final GraphicsContext gc = singlePreviewCanvas.getGraphicsContext2D();
        resetCanvas(gc);

        final PixelWriter pw = gc.getPixelWriter();

        final FontGlyph glyph = font.glyphs().get(fontGlyph);
        final int startX = glyph.xOffset() < 0 ? Math.abs(glyph.xOffset()) : 0;
        final int startY = glyph.yOffset() < 0 ? Math.abs(glyph.yOffset()) : 0;
        new FontRenderer<>(pw::setArgb, font, startX, startY)
            .writeGlyph(glyph, ColorPalette.EGA, 13);
    }

    public void updatePreview(final KeyEvent e) {
        final GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        resetCanvas(gc);
        final PixelWriter pw = gc.getPixelWriter();

        new FontRenderer<>(pw::setArgb, font, 0, 0)
            .write(ColorPalette.EGA, 13, previewText.getText().getBytes(Charset.forName("CP437")), 5, 5);
    }

}
