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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.sound.midi.MidiUnavailableException;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.ImageAdapter;
import de.siegmar.jmonkey.commons.misc.MaskLayer;
import de.siegmar.jmonkey.decoder.LecFileScriptPrintDecoder;
import de.siegmar.jmonkey.decoder.header.ChunkFOItem;
import de.siegmar.jmonkey.decoder.room.ChunkCC;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkLSDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ColorCycle;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.decoder.script.PrintOpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.ScriptDecoder;
import de.siegmar.jmonkey.explorer.misc.ColorPaletteUtil;
import de.siegmar.jmonkey.explorer.misc.MidiPlayer;
import de.siegmar.jmonkey.explorer.model.IndexNodeInfo;
import de.siegmar.jmonkey.explorer.model.TreeViewContainer;
import de.siegmar.jmonkey.explorer.util.LecDecoder;
import de.siegmar.jmonkey.explorer.util.Resource;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.TreeIndex;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling",
    "checkstyle:ClassFanOutComplexity", "checkstyle:CyclomaticComplexity"})
public class ExplorerController implements Initializable {

    @FXML
    private Label statusLabel;

    @FXML
    private Button exportRawData;

    @FXML
    private Pane centerPane;

    @FXML
    private TableColumn<IndexNodeInfo, String> propName;

    @FXML
    private TableColumn<IndexNodeInfo, String> propValue;

    @FXML
    private TableView<IndexNodeInfo> tableView;

    @FXML
    private TreeView<TreeViewContainer> selectionTreeView;

    private MidiPlayer midiPlayer;

    private LecDecoder lecDecoder;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        try {
            midiPlayer = new MidiPlayer();
        } catch (final MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }

        propName.setCellValueFactory(new PropertyValueFactory<>("propName"));
        propValue.setCellValueFactory(new PropertyValueFactory<>("propValue"));
    }

    public void openLec(final Path file) {
        statusLabel.setText("File: " + file.toString());

        lecDecoder = new LecDecoder(file);

        final TreeIndex<LecChunk> indexNode = lecDecoder.getIndex();
        final TreeItem<TreeViewContainer> rootItem = new TreeItem<>(new TreeViewContainer(indexNode));

        map(indexNode, rootItem);

        rootItem.setExpanded(true);

        selectionTreeView.setRoot(rootItem);
        selectionTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            final TreeIndex<LecChunk> indexNode1 = newValue.getValue().getIndexNode();

            updateInfoPane(indexNode1);

            centerPane.getChildren().clear();

            final Node n = switch (indexNode1.chunk().type()) {
                case FO -> displayInfo(indexNode1);
                case LF -> displayLF(indexNode1);
                case HD -> displayHD(indexNode1);
                case AD -> displaySound(indexNode1);
                case BM -> displayBm(indexNode1);
                case OI -> displayOi(indexNode1);
                case LC -> displayLC(indexNode1);
                case OC -> displayOC(indexNode1);
                case EN, EX, SC -> displayGeneric(indexNode1);
                case LS -> displayLocalScript(indexNode1);
                case CO -> displayCostume(indexNode1);
                case SL, AM -> displayHex(indexNode1);
                case  NL -> displayNL(indexNode1);
                case PA -> displayPalette(indexNode1);
                case CC -> displayColorCycle(indexNode1);
                default -> null;
            };

            if (n != null) {
                centerPane.getChildren().setAll(n);
            }

            exportRawData.setOnAction(event -> {
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(indexNode1.chunk().type() + "_" + indexNode1.chunk().pos() + ".bin");

                //Show save file dialog
                final File file1 = fileChooser.showSaveDialog(null);

                if (file1 != null) {
                    lecDecoder.export(indexNode1.chunk(), file1.toPath());
                }
            });

        });
    }

    private Node displayNL(final TreeIndex<LecChunk> indexNode) {
        final List<Integer> soundIds = lecDecoder.readNL(indexNode.chunk()).soundIds();
        final ObservableList<IndexNodeInfo> tableViewItems = tableView.getItems();
        for (final Integer soundId : soundIds) {
            tableViewItems.add(new IndexNodeInfo("sound #", String.valueOf(soundId)));
        }
        return null;
    }

    private Node displayColorCycle(final TreeIndex<LecChunk> indexNode1) {
        final ChunkCC chunkCC = lecDecoder.readCC(indexNode1.chunk());

        final ObservableList<IndexNodeInfo> items = tableView.getItems();

        int i = 0;
        for (final ColorCycle colorCycle : chunkCC.colorCycles()) {
            items.add(new IndexNodeInfo("Color Cycle " + i++,
                "start=%d, end=%d, delay=%d".formatted(colorCycle.start(), colorCycle.end(), colorCycle.delay())));
        }

        return null;
    }

    private Node displayPalette(final TreeIndex<LecChunk> indexNode1) {
        final ColorPalette colorPalette = lecDecoder.readPA(indexNode1.chunk());
        final TilePane tilePane = new TilePane();

        final ObservableList<Node> children = tilePane.getChildren();
        for (int i = 0; i < colorPalette.size(); i++) {
            children.add(ColorPaletteUtil.colorTile(i, colorPalette.color(i)));
        }

        tilePane.prefHeightProperty().bind(centerPane.heightProperty());
        tilePane.prefWidthProperty().bind(centerPane.widthProperty());
        return tilePane;
    }

    private Node displayInfo(final TreeIndex<LecChunk> indexNode) {
        final var chunkFO = lecDecoder.readFO(indexNode.chunk());

        final TableColumn<ChunkFOItem, Object> colRoom = new TableColumn<>("Room #");
        colRoom.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().roomId()));

        final TableColumn<ChunkFOItem, Object> colOffset = new TableColumn<>("LF Offset");
        colOffset.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().lfOffset()));

        final var table = new TableView<>(FXCollections.observableList(chunkFO.items()));
        table.getColumns().add(colRoom);
        table.getColumns().add(colOffset);

        table.prefWidthProperty().bind(centerPane.widthProperty());
        table.prefHeightProperty().bind(centerPane.heightProperty());

        return table;
    }

    private Node displayLF(final TreeIndex<LecChunk> indexNode) {
        final var roomId = lecDecoder.readLF(indexNode.chunk()).roomId();
        tableView.getItems().add(new IndexNodeInfo("room #", String.valueOf(roomId)));
        return null;
    }

    private TextArea displayOC(final TreeIndex<LecChunk> indexNode) {
        final ChunkOC oc = lecDecoder.readOC(indexNode.chunk());
        final ObservableList<IndexNodeInfo> items = tableView.getItems();

        items.add(new IndexNodeInfo("objectId", String.valueOf(oc.objectId())));
        items.add(new IndexNodeInfo("xPosition", String.valueOf(oc.xPosition())));
        items.add(new IndexNodeInfo("yPosition", String.valueOf(oc.yPosition())));
        items.add(new IndexNodeInfo("width", String.valueOf(oc.width())));
        items.add(new IndexNodeInfo("parent", String.valueOf(oc.parent())));
        items.add(new IndexNodeInfo("walkX", String.valueOf(oc.walkX())));
        items.add(new IndexNodeInfo("walkY", String.valueOf(oc.walkY())));
        items.add(new IndexNodeInfo("height", String.valueOf(oc.height())));
        items.add(new IndexNodeInfo("actorDirection", String.valueOf(oc.actorDirection())));
        items.add(new IndexNodeInfo("type", String.valueOf(oc.name())));

        final StringWriter sw = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(sw);
        final PrintOpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(printWriter::println);
        final ScriptDecoder scriptDecoder = new ScriptDecoder();
        final LecFileScriptPrintDecoder lfspd = new LecFileScriptPrintDecoder(sw, scriptDecoder, opcodeDelegate);
        lfspd.decodeOC(oc).execute();

        return prepareTextArea(sw.toString(), centerPane);
    }

    private Node displayHD(final TreeIndex<LecChunk> indexNode) {
        final var hd = lecDecoder.readHD(indexNode.chunk());
        final var items = tableView.getItems();
        items.add(new IndexNodeInfo("width", String.valueOf(hd.width())));
        items.add(new IndexNodeInfo("height", String.valueOf(hd.height())));
        items.add(new IndexNodeInfo("# objects", String.valueOf(hd.noOfObjects())));
        return null;
    }

    private void updateInfoPane(final TreeIndex<LecChunk> indexNode) {
        final List<IndexNodeInfo> indexNodeInfos = new ArrayList<>();
        indexNodeInfos.add(new IndexNodeInfo("offset start",
            NumberFormat.getInstance().format(indexNode.chunk().pos())));
        indexNodeInfos.add(new IndexNodeInfo("offset end",
            NumberFormat.getInstance().format(indexNode.chunk().pos() + indexNode.chunk().length())));
        indexNodeInfos.add(new IndexNodeInfo("length",
            NumberFormat.getInstance().format(indexNode.chunk().length())));

        if (!indexNode.getChildren().isEmpty()) {
            // FIXME
            final int sizeOfChildren = indexNode.getChildren().stream()
                .mapToInt(c -> c.chunk().length())
                .sum();

            // payload + parent header
            int expectedSize = sizeOfChildren + 6;
            if (indexNode.chunk().type() == DataChunkType.LF) {
                // LF itself contains 2 bytes data
                expectedSize += 2;
            }

            indexNodeInfos.add(new IndexNodeInfo("# children",
                NumberFormat.getInstance().format(indexNode.getChildren().size())));
            indexNodeInfos.add(new IndexNodeInfo("length of children",
                NumberFormat.getInstance().format(sizeOfChildren)));
            indexNodeInfos.add(new IndexNodeInfo("correct length",
                Boolean.toString(indexNode.chunk().length() == expectedSize)));
        }

        tableView.setItems(FXCollections.observableList(indexNodeInfos));
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Node displaySound(final TreeIndex<LecChunk> indexNode) {
        final Button playButton = new Button("Play sound");
        playButton.setOnAction(event -> {
            final ByteString data = lecDecoder.readAD(indexNode.chunk());
            try {
                midiPlayer.playFile(data);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        });

        return playButton;
    }

    private Node displayBm(final TreeIndex<LecChunk> indexNode) {
        final var image = lecDecoder.readBM(indexNode);

        final int layerCnt = image.map(i -> i.getMasks().size() + 1).orElse(0);
        tableView.getItems().add(new IndexNodeInfo("image layers", String.valueOf(layerCnt)));

        if (image.isEmpty()) {
            return null;
        }

        final Pane pane = new VBox(4);
        final ObservableList<Node> vboxChildren = pane.getChildren();

        vboxChildren.add(buildImageView(toImage(image.get())));
        masksToVbox(image.get().getMasks(), vboxChildren);

        pane.prefWidthProperty().bind(centerPane.widthProperty());
        pane.prefHeightProperty().bind(centerPane.heightProperty());

        return pane;
    }

    private void masksToVbox(final List<MaskLayer> masks, final ObservableList<Node> vboxChildren) {
        for (final MaskLayer mask : masks) {
            final ImageAdapter<Image> imageAdapter = newImageAdapter();
            mask.writeTo(imageAdapter, 0xFF_FF_FF_FF, 0xFF_00_00_00);
            vboxChildren.add(buildImageView(imageAdapter.getImage()));
        }
    }

    private Image toImage(final LayeredImage monkeyImage) {
        final ImageAdapter<Image> imageAdapter = newImageAdapter();
        monkeyImage.writeTo(imageAdapter);
        return imageAdapter.getImage();
    }

    private Node buildImageView(final Image image) {
        final ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(image.getWidth() * 2);
        imageView.setFitHeight(image.getHeight() * 2);
        return imageView;
    }

    private Node displayOi(final TreeIndex<LecChunk> indexNode) {
        final var objectImage = lecDecoder.readOI(indexNode.chunk());
        tableView.getItems().add(new IndexNodeInfo("object id", String.valueOf(objectImage.objectId())));

        final var image = lecDecoder.readOI(indexNode);
        final int layerCnt = image.map(i -> i.getMasks().size() + 1).orElse(0);
        tableView.getItems().add(new IndexNodeInfo("image layers", String.valueOf(layerCnt)));

        if (image.isEmpty()) {
            return null;
        }

        final VBox vbox = new VBox(4);
        final ObservableList<Node> vboxChildren = vbox.getChildren();

        vboxChildren.add(buildImageView(toImage(image.get())));
        masksToVbox(image.get().getMasks(), vboxChildren);

        return vbox;
    }

    private Node displayLC(final TreeIndex<LecChunk> indexNode) {
        final int scriptCnt = lecDecoder.readLC(indexNode.chunk()).scriptCount();
        tableView.getItems().add(new IndexNodeInfo("script #", String.valueOf(scriptCnt)));
        return null;
    }

    private Node displayGeneric(final TreeIndex<LecChunk> indexNode) {
        final ByteString data = lecDecoder.sliceChunk(indexNode.chunk()).data();

        final StringWriter sw = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(sw);
        final PrintOpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(printWriter::println);
        final ScriptDecoder scriptDecoder = new ScriptDecoder();
        final LecFileScriptPrintDecoder lfspd = new LecFileScriptPrintDecoder(sw, scriptDecoder, opcodeDelegate);
        lfspd.decodeGeneric(data).execute();

        return prepareTextArea(sw.toString(), centerPane);
    }

    private TextArea prepareTextArea(final String sw, final Pane parentPane) {
        final var textArea = new TextArea(sw);
        textArea.prefWidthProperty().bind(parentPane.widthProperty());
        textArea.prefHeightProperty().bind(parentPane.heightProperty());
        textArea.setStyle("-fx-font-family: 'Menlo', monospace");
        textArea.setEditable(false);
        return textArea;
    }

    private Node displayLocalScript(final TreeIndex<LecChunk> indexNode) {
        final ChunkLS data = ChunkLSDecoder.decode(lecDecoder.sliceChunk(indexNode.chunk()));

        final StringWriter sw = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(sw);
        final PrintOpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(printWriter::println);
        final ScriptDecoder scriptDecoder = new ScriptDecoder();
        final LecFileScriptPrintDecoder lfspd = new LecFileScriptPrintDecoder(sw, scriptDecoder, opcodeDelegate);
        lfspd.decodeLS(data).execute();

        return prepareTextArea(sw.toString(), centerPane);
    }

    private Node displayCostume(final TreeIndex<LecChunk> indexNode) {
        lecDecoder.readCO(indexNode);
//        CostumeDecoder.exportImagesOfCostume();

        return null;
    }

    @SuppressWarnings({"PMD.AvoidStringBufferField", "checkstyle:AnonInnerLength"})
    private Node displayHex(final TreeIndex<LecChunk> indexNode) {
        final Appendable sw = new Appendable() {
            private final StringBuilder sb = new StringBuilder();

            private void breakIfNecessary() {
                if (sb.length() % 25 == 0) {
                    sb.append('\n');
                }
            }

            @Override
            public Appendable append(final CharSequence csq) {
                breakIfNecessary();
                return sb.append(csq);
            }

            @Override
            public Appendable append(final CharSequence csq, final int start, final int end) {
                breakIfNecessary();
                return sb.append(csq, start, end);
            }

            @Override
            public Appendable append(final char c) {
                breakIfNecessary();
                return sb.append(c);
            }

            @Override
            public String toString() {
                return sb.toString();
            }
        };

        final EnhancedByteBuffer bb = lecDecoder.sliceChunk(indexNode.chunk()).ebbLE();

        final HexFormat of = HexFormat.of().withDelimiter(" ");
        of.formatHex(sw, bb.readBytes(bb.remaining()));

        return prepareTextArea(sw.toString(), centerPane);
    }

    private ImageAdapter<Image> newImageAdapter() {
        return new ImageAdapter<>() {
            private WritableImage wi;
            private PixelWriter pixelWriter;

            @Override
            public void init(final int width, final int height) {
                wi = new WritableImage(width, height);
                pixelWriter = wi.getPixelWriter();
            }

            @Override
            public void setRGB(final int x, final int y, final int color) {
                pixelWriter.setArgb(x, y, color);
            }

            @Override
            public WritableImage getImage() {
                return wi;
            }
        };
    }

    private void map(final TreeIndex<LecChunk> indexNode, final TreeItem<TreeViewContainer> rootItem) {
        for (final var node : indexNode) {
            final TreeItem<TreeViewContainer> leafNode = new TreeItem<>(new TreeViewContainer(node), nodeImage(node));
            rootItem.getChildren().add(leafNode);
            map(node, leafNode);
        }
    }

    private Node nodeImage(final TreeIndex<LecChunk> node) {
        final String iconName = switch (node.chunk().type()) {
            case LE, RO, SO -> "folder";
            case LF -> "folder_table";
            case HD, LC -> "information";
            case CC -> "color_wheel";
            case BM -> "photo";
            case OI -> "picture";
            case PA, SP -> "palette";
            case SA -> "zoom";
            case SC, LS -> "script_code";
            case EN -> "door_in";
            case EX -> "door_out";
            case AD, WA, ROL, AM -> "music";
            case CO -> "user_red";
            case BX -> "vector";
            case FO, NL, SL -> "table_relationship";
            case OC -> "picture_link";
        };

        return Resource.imageView(iconName);
    }

}
