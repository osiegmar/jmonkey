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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.siegmar.jmonkey.datarepository.DataRepository;
import de.siegmar.jmonkey.datarepository.RoomDecoder;
import de.siegmar.jmonkey.explorer.model.CostumeViewItem;
import de.siegmar.jmonkey.explorer.model.FontViewItem;
import de.siegmar.jmonkey.explorer.model.RoomViewItem;
import de.siegmar.jmonkey.explorer.model.ScriptViewItem;
import de.siegmar.jmonkey.explorer.model.SoundViewItem;
import de.siegmar.jmonkey.explorer.model.TreeViewItem;
import de.siegmar.jmonkey.explorer.util.FXUtil;
import de.siegmar.jmonkey.explorer.util.Resource;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.NamedRoomDirectory;
import de.siegmar.jmonkey.index.RoomOffset;
import de.siegmar.jmonkey.index.WrappedIndex;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

@SuppressWarnings({"checktyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity",
    "checkstyle:ClassDataAbstractionCoupling"})
public class GameExplorerController {

    @FXML
    private Label statusLabel;

    @FXML
    private TreeView<TreeViewItem> selectionTreeView;

    private TreeItem<TreeViewItem> dataTree;

    @FXML
    private Pane centerPane;

    private MenuBar menuBar;

    private Index rawIndex;
    private WrappedIndex index;
    private DataRepository dataRepository;
    private IndexExplorerController indexExplorerController;

    private TreeItem<TreeViewItem> tiRooms;

    public void setMenuBar(final MenuBar menuBar) {
        this.menuBar = menuBar;
    }

    public void openGame(final Path dir) {
        statusLabel.setText("Dir: " + dir.toString());

        dataRepository = new DataRepository(dir);
        rawIndex = dataRepository.getIndex();
        index = dataRepository.getWrappedIndex();

        final CheckMenuItem menuItem = new CheckMenuItem("Group by room");
        menuBar.getMenus().add(new Menu("View", null, menuItem));
        menuItem.setOnAction(this::groupByRoom);

        final TreeItem<TreeViewItem> root = new TreeItem<>(new TreeViewItem("Root"));
        dataTree = new TreeItem<>(new TreeViewItem("Data"), Resource.imageView("folder"));
        dataTree.getChildren().setAll(collectData(false));
        root.getChildren().setAll(collectIndex(), collectFonts(dir), dataTree);

        selectionTreeView.setRoot(root);
        selectionTreeView.setShowRoot(false);

        selectionTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            if (newValue == tiRooms) {
                displayRooms();
            } else {
                final TreeViewItem item = newValue.getValue();
                if (item instanceof RoomViewItem r) {
                    displayRoom(r.getRoomId());
                } else if (item instanceof FontViewItem f) {
                    displayFont(f.getFile());
                } else if (item instanceof CostumeViewItem c) {
                    displayCostume(c.getCostumeId());
                } else if (item instanceof ScriptViewItem s) {
                    displayScript(s.getScriptId());
                } else if (item instanceof SoundViewItem s) {
                    displaySound(s.getSoundId());
                }
            }
        });
    }

    private void displaySound(final int soundId) {
        final Pane soundPane = SoundViewController.load(dataRepository, soundId);
        FXUtil.prepare(centerPane, soundPane);
    }

    private void displayScript(final int scriptId) {
        final Pane scriptPane = ScriptViewController.load(dataRepository, scriptId);
        FXUtil.prepare(centerPane, scriptPane);
    }

    private void groupByRoom(final ActionEvent e) {
        final CheckMenuItem cmi = (CheckMenuItem) e.getSource();
        final boolean groupByRoom = cmi.isSelected();
        dataTree.getChildren().setAll(collectData(groupByRoom));
    }

    private void displayFont(final Path fontFile) {
        final Region node = (Region) Resource.openFont(fontFile);

        node.prefWidthProperty().bind(centerPane.widthProperty());
        node.prefHeightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().setAll(node);
    }

    private TreeItem<TreeViewItem> collectIndex() {
        final TreeItem<TreeViewItem> indexItems = new TreeItem<>(
            new TreeViewItem("Index"), Resource.imageView("table"));
        indexExplorerController = new IndexExplorerController();
        indexExplorerController.setIndex(rawIndex);
        indexExplorerController.addItems(indexItems);
        indexExplorerController.setCenterPane(centerPane);
        indexExplorerController.setSelectionTreeView(selectionTreeView);
        indexExplorerController.setupListener();

        return indexItems;
    }

    private TreeItem<TreeViewItem> collectFonts(final Path dir) {
        final TreeItem<TreeViewItem> fonts = new TreeItem<>(new TreeViewItem("Fonts"), Resource.imageView("style"));

        try (Stream<Path> files = Files.list(dir)) {
            final List<Path> fontFiles = files
                .filter(p -> p.getFileName().toString().matches("(?i)^90[0-9]\\.LFL$"))
                .toList();

            for (final Path fontFile : fontFiles) {
                fonts.getChildren().add(new TreeItem<>(
                    new FontViewItem(Objects.toString(fontFile.getFileName()), fontFile)));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return fonts;
    }

    private List<TreeItem<TreeViewItem>> collectData(final boolean groupByRoom) {
        if (groupByRoom) {
            return List.of(collectGroupedRooms());
        } else {
            return List.of(collectRooms(), collectCostumes(null), collectSounds(null), collectScripts(null));
        }
    }

    private TreeItem<TreeViewItem> collectGroupedRooms() {
        final ObservableList<TreeItem<TreeViewItem>> roomsChildren = tiRooms.getChildren();
        roomsChildren.clear();

        for (final NamedRoomDirectory room : index.listNamedRooms()) {
            final TreeItem<TreeViewItem> e = new TreeItem<>(new RoomViewItem(roomToStr(room), room.roomId()));
            final ObservableList<TreeItem<TreeViewItem>> children = e.getChildren();

            final TreeItem<TreeViewItem> costumes = collectCostumes(room.roomId());
            if (!costumes.getChildren().isEmpty()) {
                children.add(costumes);
            }

            final TreeItem<TreeViewItem> sounds = collectSounds(room.roomId());
            if (!sounds.getChildren().isEmpty()) {
                children.add(sounds);
            }

            final TreeItem<TreeViewItem> scripts = collectScripts(room.roomId());
            if (!scripts.getChildren().isEmpty()) {
                children.add(scripts);
            }

            roomsChildren.add(e);
        }

        return tiRooms;
    }

    private TreeItem<TreeViewItem> collectRooms() {
        tiRooms = new TreeItem<>(new TreeViewItem("Rooms"), Resource.imageView("door_open"));

        final ObservableList<TreeItem<TreeViewItem>> roomsChildren = tiRooms.getChildren();
        for (final NamedRoomDirectory room : index.listNamedRooms().stream().toList()) {
            roomsChildren.add(new TreeItem<>(new RoomViewItem(roomToStr(room), room.roomId())));
        }
        return tiRooms;
    }

    private TreeItem<TreeViewItem> collectCostumes(final Integer roomId) {
        Stream<RoomOffset> costumeStream = index.listCostumeRelativeOffsets().stream();
        if (roomId != null) {
            costumeStream = costumeStream.filter(c -> roomId.equals(c.roomId()));
        }

        final TreeItem<TreeViewItem> costumesTreeItem = new TreeItem<>(
            new TreeViewItem("Costumes"), Resource.imageView("user_red"));
        final ObservableList<TreeItem<TreeViewItem>> children = costumesTreeItem.getChildren();

        final List<CostumeViewItem> costumeItems = costumeStream
            .map(c -> new CostumeViewItem("Costume " + c.itemId(), c.itemId()))
            .toList();

        for (final CostumeViewItem costumeItem : costumeItems) {
            children.add(new TreeItem<>(costumeItem));
        }

        return costumesTreeItem;
    }

    private TreeItem<TreeViewItem> collectSounds(final Integer roomId) {
        Stream<RoomOffset> soundStream = index.listSoundRelativeOffsets().stream();
        if (roomId != null) {
            soundStream = soundStream.filter(s -> roomId.equals(s.roomId()));
        }

        final TreeItem<TreeViewItem> soundsTreeItem = new TreeItem<>(
            new TreeViewItem("Sounds"), Resource.imageView("music"));
        final ObservableList<TreeItem<TreeViewItem>> children = soundsTreeItem.getChildren();

        final List<SoundViewItem> soundItems = soundStream
            .map(s -> new SoundViewItem("Sound " + s.itemId(), s.itemId()))
            .toList();

        for (final SoundViewItem soundViewItem : soundItems) {
            children.add(new TreeItem<>(soundViewItem));
        }
        return soundsTreeItem;
    }

    private TreeItem<TreeViewItem> collectScripts(final Integer roomId) {
        Stream<RoomOffset> scriptStream = index.listGlobalScriptRelativeOffsets().stream();
        if (roomId != null) {
            scriptStream = scriptStream.filter(s -> roomId.equals(s.roomId()));
        }

        final List<ScriptViewItem> scriptOffsets = scriptStream
            .map(so -> new ScriptViewItem("Script " + so.itemId(), so.itemId()))
            .toList();

        final TreeItem<TreeViewItem> scriptsTreeItem = new TreeItem<>(
            new TreeViewItem("Scripts"), Resource.imageView("script_code"));
        final ObservableList<TreeItem<TreeViewItem>> children = scriptsTreeItem.getChildren();

        for (final ScriptViewItem scriptOffset : scriptOffsets) {
            children.add(new TreeItem<>(scriptOffset));
        }

        return scriptsTreeItem;
    }

    private void displayRoom(final int roomId) {
        if (dataRepository.findRoomOffset(roomId).isEmpty()) {
            return;
        }

        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/room_details.fxml"));
        final Control roomPane;
        try {
            roomPane = fxmlLoader.load();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final RoomDetailsController controller = fxmlLoader.getController();

        controller.setRoom(dataRepository.loadRoom(roomId));

        roomPane.prefWidthProperty().bind(centerPane.widthProperty());
        roomPane.prefHeightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().setAll(roomPane);
    }

    private void displayCostume(final int costumeId) {
        final Control roomPane = CostumeDetailsController.load(dataRepository, costumeId);
        FXUtil.prepare(centerPane, roomPane);
    }

    private void displayRooms() {
        final TilePane tilePane = new TilePane();
        tilePane.setPadding(new Insets(15, 15, 15, 15));
        tilePane.setVgap(15);
        tilePane.setHgap(15);

        for (final NamedRoomDirectory room : index.listNamedRooms()) {
            if (dataRepository.findRoomOffset(room.roomId()).isEmpty()) {
                continue;
            }

            final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/room_preview.fxml"));
            final Pane roomPane;
            try {
                roomPane = fxmlLoader.load();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
            final RoomPreviewController controller = fxmlLoader.getController();
            controller.setTitle(roomToStr(room));
            RoomDecoder.decodeRoom(dataRepository.readRoom(room.roomId())).image()
                .ifPresent(controller::setImage);

            tilePane.getChildren().add(roomPane);
        }

        final ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToWidth(true);

        scrollPane.prefWidthProperty().bind(centerPane.widthProperty());
        scrollPane.prefHeightProperty().bind(centerPane.heightProperty());

        centerPane.getChildren().setAll(scrollPane);
    }

    public String roomToStr(final NamedRoomDirectory room) {
        return "%d (%s)".formatted(room.roomId(), room.name());
    }

}
