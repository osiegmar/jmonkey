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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ColorCycle;
import de.siegmar.jmonkey.decoder.room.ObjectItem;
import de.siegmar.jmonkey.decoder.room.Room;
import de.siegmar.jmonkey.decoder.room.ScaleSlot;
import de.siegmar.jmonkey.decoder.room.box.ChunkBXBox;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.explorer.misc.JavaFxImageAdapter;
import de.siegmar.jmonkey.explorer.model.NumberedItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.StringConverter;

@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public class RoomDetailsController implements Initializable {

    @FXML
    private ChoiceBox<NumberedItem<MaskLayer>> imageLayers;

    @FXML
    private ListView<NumberedItem<ChunkBXBox>> walkBoxes;

    @FXML
    private ListView<NumberedItem<ScaleSlot>> scalingSlots;

    @FXML
    private ListView<NumberedItem<ColorCycle>> colorCycles;

    @FXML
    private ListView<ObjectItem> objects;

    @FXML
    private Slider imageSizeSlider;

    @FXML
    private Label imageDimension;

    @FXML
    private Group imageGroup;

    private final Group backgroundImageGroup = new Group();
    private final Group objectImageGroup = new Group();
    private final Group boxGroup = new Group();
    private final Group scaleGroup = new Group();
    private ImageView roomImage;
    private Room room;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        imageGroup.getChildren().addAll(backgroundImageGroup, objectImageGroup, boxGroup, scaleGroup);
    }

    public void setRoom(final Room room) {
        this.room = room;

        final ChunkHD hd = room.hd();
        imageDimension.setText("%d x %d".formatted(hd.width(), hd.height()));

        imageLayers.setConverter(new StringConverter<>() {
            @Override
            public String toString(final NumberedItem<MaskLayer> object) {
                return "Layer " + object.number();
            }

            @Override
            public NumberedItem<MaskLayer> fromString(final String string) {
                return null;
            }
        });
        imageLayers.getItems().add(new NumberedItem<>(0, null));
        imageLayers.getSelectionModel().select(0);

        if (room.image().isPresent()) {
            final JavaFxImageAdapter javaFxImageAdapter = new JavaFxImageAdapter();
            room.image().get().writeTo(javaFxImageAdapter);
            roomImage = new ImageView(javaFxImageAdapter.getImage());
            backgroundImageGroup.getChildren().setAll(roomImage);

            imageLayers.getItems().addAll(wrapNumbered(room.image().get().getMasks(), 1));
            imageLayers.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showMask(newValue.item()));
        }

        if (room.bx().isPresent()) {
            walkBoxes.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(final NumberedItem<ChunkBXBox> item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : "Box " + item.number());
                }
            });
            walkBoxes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showBox(newValue.item()));
            walkBoxes.setItems(FXCollections.observableList(wrapNumbered(room.bx().get().boxes(), 0)));
        }

        if (room.sa().isPresent()) {
            scalingSlots.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(final NumberedItem<ScaleSlot> item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : "Scale Slot " + item.number());
                }
            });
            scalingSlots.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showScalingSlot(newValue.item()));
            scalingSlots.setItems(FXCollections.observableList(wrapNumbered(room.sa().get().scaleSlots(), 0)));
        }

        if (room.cc().isPresent()) {
            colorCycles.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(final NumberedItem<ColorCycle> item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : "Cycle " + item.number());
                }
            });
            colorCycles.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showColorCycle(newValue.item()));
            colorCycles.setItems(FXCollections.observableList(wrapNumbered(room.cc().get().colorCycles(), 0)));
        }

        objects.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(final ObjectItem item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "Object #" + item.chunkOC().objectId());
            }
        });
        objects.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> showObject(newValue));
        objects.setItems(FXCollections.observableList(room.objects()));

        imageSizeSlider.valueProperty()
            .addListener((observable, oldValue, newValue) -> {
                imageGroup.scaleXProperty().setValue(newValue);
                imageGroup.scaleYProperty().setValue(newValue);
            });
    }

    private void showScalingSlot(final ScaleSlot scaleSlot) {
        final int width = room.hd().width();

        scaleGroup.getChildren().setAll(
            drawLine(scaleSlot.y1(), Color.GREEN, width, "y1"),
            drawLine(scaleSlot.y2(), Color.RED, width, "y2")
        );
    }

    private Group drawLine(final int scaleSlot, final Color color, final int width, final String text) {
        final Line line = new Line(-50, scaleSlot, width, scaleSlot);
        line.setStroke(color);

        final Label label = new Label(text);
        label.setTextFill(color);
        label.setLayoutX(-15);
        label.setLayoutY(scaleSlot - 15);

        return new Group(line, label);
    }

    private static <T> List<NumberedItem<T>> wrapNumbered(final Collection<T> item, final int start) {
        final AtomicInteger cnt = new AtomicInteger(start);
        return item.stream()
            .map(i -> new NumberedItem<>(cnt.getAndIncrement(), i))
            .collect(Collectors.toList());
    }

    private void showMask(final MaskLayer maskLayer) {
        if (maskLayer == null) {
            backgroundImageGroup.getChildren().setAll(roomImage);
        } else {
            final JavaFxImageAdapter javaFxImageAdapter = new JavaFxImageAdapter();
            maskLayer.writeTo(javaFxImageAdapter, 0x99FFFFFF, 0x99000000);
            final ImageView maskImage = new ImageView(javaFxImageAdapter.getImage());
            maskImage.setBlendMode(BlendMode.SRC_OVER);

            backgroundImageGroup.getChildren().setAll(roomImage, maskImage);
        }
    }

    private void showBox(final ChunkBXBox box) {
        final Polygon polygon = new Polygon(
            box.ulx(),
            box.uly(),
            box.urx(),
            box.ury(),
            box.lrx(),
            box.lry(),
            box.llx(),
            box.lly()
        );
        polygon.setStroke(Color.WHITE);
        polygon.setFill(Color.WHITE);
        polygon.setBlendMode(BlendMode.DIFFERENCE);
        boxGroup.getChildren().setAll(polygon);
    }

    private void showColorCycle(final ColorCycle colorCycle) {
        final ColorPalette pa = room.pa().orElseThrow();
        final int[] palette = pa.getPalette();

        cycle(palette, colorCycle.start(), colorCycle.end(), 1);

        final ColorPalette newPalette = new ColorPalette(palette);

        final LayeredImage image = ChunkBMDecoder.decode(room.imageData().orElseThrow(), room.hd(), newPalette)
            .orElseThrow();
        final JavaFxImageAdapter javaFxImageAdapter = new JavaFxImageAdapter();
        image.writeTo(javaFxImageAdapter);
        roomImage.setImage(javaFxImageAdapter.getImage());
    }

    private static void cycle(final int[] data, final int begin, final int end, final int shift) {
        final int len = end - begin;

        final int[] tmp = Arrays.copyOfRange(data, end - shift + 1, end + 1);
        System.arraycopy(data, begin, data, begin + shift, len - shift + 1);
        System.arraycopy(tmp, 0, data, begin, shift);
    }

    private void showObject(final ObjectItem objectItem) {
        final ChunkOC chunkOC = objectItem.chunkOC();
        if (chunkOC.height() == 0 || chunkOC.width() == 0) {
            return;
        }

        final ObservableList<Node> objectGroupChildren = objectImageGroup.getChildren();
        objectGroupChildren.clear();

        final Optional<LayeredImage> data = objectItem.objectImage();
        if (data.isPresent()) {
            final LayeredImage image = data.get();

            final JavaFxImageAdapter javaFxImageAdapter = new JavaFxImageAdapter();
            image.writeTo(javaFxImageAdapter);

            final var objectImage = new ImageView();
            objectImage.setImage(javaFxImageAdapter.getImage());
            objectImage.setX(chunkOC.xPosition());
            objectImage.setY(chunkOC.yPosition());

            objectGroupChildren.add(objectImage);
        }

        final Rectangle rectangle = new Rectangle(chunkOC.xPosition(),
            chunkOC.yPosition(), chunkOC.width(), chunkOC.height());
        rectangle.setStroke(Color.WHITE);
        rectangle.setStrokeType(StrokeType.INSIDE);
        rectangle.setBlendMode(BlendMode.DIFFERENCE);
        objectGroupChildren.add(rectangle);
    }

}
