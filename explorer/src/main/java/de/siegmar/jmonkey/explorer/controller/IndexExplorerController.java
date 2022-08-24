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
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import de.siegmar.jmonkey.explorer.model.TreeViewItem;
import de.siegmar.jmonkey.explorer.util.Resource;
import de.siegmar.jmonkey.index.Index;
import de.siegmar.jmonkey.index.IndexReader;
import de.siegmar.jmonkey.index.ObjectMeta;
import de.siegmar.jmonkey.index.RoomDirectory;
import de.siegmar.jmonkey.index.RoomName;
import de.siegmar.jmonkey.index.RoomOffset;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class IndexExplorerController implements Initializable {

    @FXML
    private TreeView<TreeViewItem> selectionTreeView;

    @FXML
    private Pane centerPane;

    private Index index;

    private TreeItem<TreeViewItem> tiRoomNames;
    private TreeItem<TreeViewItem> tiRooms;
    private TreeItem<TreeViewItem> tiScripts;
    private TreeItem<TreeViewItem> tiSounds;
    private TreeItem<TreeViewItem> tiCostumes;
    private TreeItem<TreeViewItem> tiObjects;

    public void setSelectionTreeView(final TreeView<TreeViewItem> selectionTreeView) {
        this.selectionTreeView = selectionTreeView;
    }

    public void setCenterPane(final Pane centerPane) {
        this.centerPane = centerPane;
    }

    public void openIndex(final Path file) {
        setIndex(IndexReader.readFile(file));
    }

    public void setIndex(final Index index) {
        this.index = index;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final TreeItem<TreeViewItem> root = treeItem("Root", null);
        addItems(root);

        selectionTreeView.setRoot(root);
        selectionTreeView.setShowRoot(false);

        setupListener();
    }

    public void setupListener() {
        selectionTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == tiRoomNames) {
                displayRoomNames();
            } else if (newValue == tiRooms) {
                displayRooms();
            } else if (newValue == tiScripts) {
                displayOffsets("Script", index.scripts());
            } else if (newValue == tiSounds) {
                displayOffsets("Sound", index.sounds());
            } else if (newValue == tiCostumes) {
                displayOffsets("Costume", index.costumes());
            } else if (newValue == tiObjects) {
                displayObjects();
            }
        });
    }

    public void addItems(final TreeItem<TreeViewItem> root) {
        tiRoomNames = treeItem("Room names", "information");
        tiRooms = treeItem("Rooms", "door_open");
        tiScripts = treeItem("Scripts", "script_code");
        tiSounds = treeItem("Sounds", "music");
        tiCostumes = treeItem("Costumes", "user_red");
        tiObjects = treeItem("Objects", "picture");
        root.getChildren().setAll(tiRoomNames, tiRooms, tiScripts, tiSounds, tiCostumes, tiObjects);
    }

    private void displayRoomNames() {
        final TableView<RoomName> tableView = new TableView<>();

        final TableColumn<RoomName, Number> colItemId = new TableColumn<>("#");
        colItemId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().itemId()));
        tableView.getColumns().add(colItemId);

        final TableColumn<RoomName, Number> colRoomId = new TableColumn<>("Room id");
        colRoomId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().roomId()));
        tableView.getColumns().add(colRoomId);

        final TableColumn<RoomName, String> colRoomName = new TableColumn<>("Room name");
        colRoomName.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().name()));
        tableView.getColumns().add(colRoomName);

        tableView.getItems().setAll(index.roomNames());

        bindPane(tableView);
    }

    private void displayRooms() {
        final TableView<RoomDirectory> tableView = new TableView<>();

        final TableColumn<RoomDirectory, Number> colRoomId = new TableColumn<>("Room Id");
        colRoomId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().roomId()));
        tableView.getColumns().add(colRoomId);

        final TableColumn<RoomDirectory, Number> colFileNumber = new TableColumn<>("File number");
        colFileNumber.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().fileNumber()));
        tableView.getColumns().add(colFileNumber);

        final TableColumn<RoomDirectory, Number> colFileOffset = new TableColumn<>("File offset");
        colFileOffset.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().fileOffset()));
        tableView.getColumns().add(colFileOffset);

        tableView.getItems().setAll(index.roomLocations());

        bindPane(tableView);
    }

    private void bindPane(final TableView<?> tableView) {
        tableView.prefWidthProperty().bind(centerPane.widthProperty());
        tableView.prefHeightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().setAll(tableView);
    }

    private void displayOffsets(final String objectName, final List<RoomOffset> offsets) {
        final TableView<RoomOffset> tableView = new TableView<>();

        final TableColumn<RoomOffset, Number> colItemId = new TableColumn<>(objectName + " Id");
        colItemId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().itemId()));
        tableView.getColumns().add(colItemId);

        final TableColumn<RoomOffset, Number> colRoomId = new TableColumn<>("Room id");
        colRoomId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().roomId()));
        tableView.getColumns().add(colRoomId);

        final TableColumn<RoomOffset, Number> colRoomOffset = new TableColumn<>("Room offset");
        colRoomOffset.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().roomOffset()));
        tableView.getColumns().add(colRoomOffset);

        tableView.getItems().setAll(offsets);

        bindPane(tableView);
    }

    private void displayObjects() {
        final TableView<ObjectMeta> tableView = new TableView<>();

        final TableColumn<ObjectMeta, Number> colItemId = new TableColumn<>("Object id");
        colItemId.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().objectId()));
        tableView.getColumns().add(colItemId);

        final TableColumn<ObjectMeta, Number> colClassData = new TableColumn<>("ClassData (raw)");
        colClassData.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().classData().classData()));
        tableView.getColumns().add(colClassData);

        final TableColumn<ObjectMeta, ObservableList<Integer>> colClassDataList =
            new TableColumn<>("ClassData (properties)");
        colClassDataList.setCellValueFactory(param -> new ReadOnlyListWrapper<>(
            FXCollections.observableList(param.getValue().classData().toIntList())));
        tableView.getColumns().add(colClassDataList);

        final TableColumn<ObjectMeta, Number> colOwner = new TableColumn<>("Owner");
        colOwner.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().owner()));
        tableView.getColumns().add(colOwner);

        final TableColumn<ObjectMeta, Number> colState = new TableColumn<>("State");
        colState.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(param.getValue().state()));
        tableView.getColumns().add(colState);

        tableView.getItems().setAll(index.objects());

        bindPane(tableView);
    }

    private static TreeItem<TreeViewItem> treeItem(final String name, final String iconName) {
        final ImageView graphic = iconName != null ? Resource.imageView(iconName) : null;
        return new TreeItem<>(new TreeViewItem(name), graphic);
    }

}
