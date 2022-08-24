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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.RasterImage;
import de.siegmar.jmonkey.datarepository.DataRepository;
import de.siegmar.jmonkey.decoder.costume.ChunkCODecoder;
import de.siegmar.jmonkey.decoder.costume.Costume;
import de.siegmar.jmonkey.decoder.costume.CostumeAnimation;
import de.siegmar.jmonkey.decoder.costume.StandaloneCostumeAnimation;
import de.siegmar.jmonkey.decoder.room.Room;
import de.siegmar.jmonkey.explorer.misc.JavaFxImageAdapter;
import de.siegmar.jmonkey.explorer.model.NumberedItem;
import de.siegmar.jmonkey.explorer.util.Resource;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class CostumeDetailsController implements Initializable {

    @FXML
    private ListView<NumberedItem<List<RasterImage>>> costumeAnimations;

    @FXML
    private Slider imageSizeSlider;

    @FXML
    private ImageView costumeView;

    private final Timeline tl = new Timeline();

    public static Control load(final DataRepository dataRepository, final int costumeId) {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/costume_details.fxml"));
        final Control roomPane;
        try {
            roomPane = fxmlLoader.load();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final CostumeDetailsController controller = fxmlLoader.getController();

        final BasicChunk costumeData = dataRepository.readCostume(costumeId);
        final Room roomChunk = dataRepository.loadRoom(
            dataRepository.getWrappedIndex().findCostumeRelativeOffset(costumeId).orElseThrow().roomId());
        final Optional<ColorPalette> colorPalette = roomChunk.pa();

        controller.setCostumeChunk(costumeData);
        controller.setCostume(ChunkCODecoder.decode(costumeData, colorPalette.orElse(ColorPalette.EGA)));

        return roomPane;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        tl.setCycleCount(Animation.INDEFINITE);
    }

    public void setCostumeChunk(final BasicChunk costumeData) {
    }

    /**
     * A Costume defines the look and animation of actors within the game.
     * <p>
     * A Costume can have up to 16 animations (e.g. talking, walking).
     * <p>
     * Every animation defines up to 16 limbs to be used (e.g. head, torso, feed).
     * <p>
     * For every limb an image is defined. Although images can be reused.
     * <p>
     * 16 / 32 colors
     * <p>
     * Costume -> Animations -> Limbs
     *
     * @param costume the costume to set
     */
    public void setCostume(final Costume costume) {
        costumeAnimations.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(final NumberedItem<List<RasterImage>> item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "Animation " + item.number());
            }
        });

        for (int animNo = 0; animNo < costume.header().animOffsets().size(); animNo++) {
            if (costume.getAnimation(animNo) != null) {
                final CostumeAnimation costumeAnimation =
                    StandaloneCostumeAnimation.collectCostumeAnimation(costume, animNo);
                final List<RasterImage> rasterImages = costumeAnimation.drawAnimation();

                if (!rasterImages.isEmpty()) {
                    costumeAnimations.getItems().add(new NumberedItem<>(animNo, rasterImages));
                }
            }
        }

        costumeAnimations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            tl.stop();
            tl.getKeyFrames().clear();
            costumeView.setImage(null);

            if (newValue == null) {
                return;
            }

            final List<Image> rasterImages = newValue.item().stream()
                .map(this::convert)
                .toList();

            // TODO check costume 20, animation 5 (where's the body?)

            if (!rasterImages.isEmpty()) {
                final Images images = new Images(rasterImages);
                tl.getKeyFrames().setAll(new KeyFrame(Duration.millis(100), e -> images.next()));
                tl.play();
            }
        });

        costumeAnimations.getSelectionModel().select(0);

        imageSizeSlider.valueProperty()
            .addListener((observable, oldValue, newValue) -> {
                costumeView.scaleXProperty().setValue(newValue);
                costumeView.scaleYProperty().setValue(newValue);
            });
    }

    private Image convert(final RasterImage rasterImage) {
        final JavaFxImageAdapter imageAdapter = new JavaFxImageAdapter();
        rasterImage.writeTo(imageAdapter);
        return imageAdapter.getImage();
    }

    private class Images {

        private final List<Image> imageList;
        private int pos;

        Images(final List<Image> images) {
            this.imageList = images;
        }

        void next() {
            costumeView.setImage(imageList.get(pos));
            pos = pos >= imageList.size() - 1 ? 0 : pos + 1;
        }

    }

}
