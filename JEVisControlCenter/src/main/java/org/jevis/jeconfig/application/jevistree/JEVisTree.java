/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplicationL. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.jevistree;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.filter.JEVisItemLoader;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Central tree representation of the JEVisSystem.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTree extends TreeTableView {
    private static final Logger logger = LogManager.getLogger(JEVisTree.class);
    private final ObservableList<TreePlugin> plugins = FXCollections.observableArrayList();
    private final JEVisDataSource ds;
    private JEVisObject copyObject;
    private JEVisTreeRow dragItem;
    private final UUID uuid = UUID.randomUUID();
    private JEVisTreeFilter cellFilter;
    private JEVisItemLoader itemLoader;
    private final ObservableList<JEVisObject> highlighterList = FXCollections.observableArrayList();
    private boolean isCut = false;
    private SearchFilterBar searchBar;

    /**
     * Create an default Tree for the given JEVisDataSource by using all accessable JEVisObjects starting by the
     * root objects.
     *
     * @param ds
     */
    public JEVisTree(JEVisDataSource ds, JEVisTreeFilter filter) {
        super();
        this.ds = ds;
//        cellFilter = FilterFactory.buildDefaultItemFilter();
        this.cellFilter = filter;
        this.getStylesheets().add(JEVisTree.class.getResource("/styles/JEVisTree.css").toExternalForm());

//        addCellFactory();
        init();
    }


    /**
     * Not working and in use. Will implement a drag&drop function
     */
    private void addCellFactory() {

        setRowFactory(param -> {
            final TreeTableRow row = new TreeTableRow();


            row.setOnDragDetected(event -> {
                try {
                    logger.debug("1. Drag go");
//                        TreeItem selected = (TreeItem) getSelectionModel().getSelectedItem();
//                        if (selected != null) {
//                        JEVisTreeRow jevisRow = (JEVisTreeRow) row.getValue();

//                        logger.debug("Drag Object: " + row.getValue().getClass());

                    Dragboard db = row.startDragAndDrop(TransferMode.ANY);
////                        // create a miniature of the row you're dragging
//                        db.setDragView(row.snapshot(null, null));
                    ClipboardContent content = new ClipboardContent();
                    content.putString("test");
                    db.setContent(content);
//                        dragItem = jevisRow;
                    event.consume();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

            row.setOnDragOver(event -> {
                logger.debug("2. Drag over");
                try {
                    if (event.getGestureSource() != row &&
                            event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }


//                        Dragboard db = event.getDragboard();

//                    if (event.getDragboard().hasString()) {
//                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                event.consume();
            });

            row.setOnDragEntered(event -> {
                logger.debug("3. Drag entert");
                event.consume();
            });

            row.setOnDragDropped(event -> {
                logger.debug("4. Drag droped");
                try {
//                        TreeItem selected = (TreeItem) getSelectionModel().getSelectedItem();
//                        logger.debug("d-Target: " + row.getTreeItem());
//                        logger.debug("d-select: " + selected);
                } catch (Exception ex) {
                    logger.error(ex);
                }
                event.setDropCompleted(true);
                event.consume();
            });

            row.setOnDragExited(value -> {
                logger.debug("5. Drag exit");
                // Reset the original color here
            });


            return row;
        });

    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void reload() {
        init();
    }

    public ObservableList<JEVisObject> getVisibleObjects() {
        return this.itemLoader.getVisibleObjects();
    }

    public JEVisTreeItem getItemForObject(JEVisObject obj) {
        return this.itemLoader.getItemForObject(obj);
    }

    public List<JEVisTreeItem> getItems() {
        return new ArrayList<>(this.itemLoader.getAllItems());
    }

    public void openPathToObject(JEVisObject obj) {
        isParentSelect(getRoot(), obj);
    }

    private boolean isParentSelect(TreeItem<JEVisTreeRow> parent, JEVisObject obj) {
        for (TreeItem<JEVisTreeRow> child : parent.getChildren()) {
            if (child.getValue().getJEVisObject().equals(obj)) {
                parent.setExpanded(true);
                return true;
            }
            if (!parent.getChildren().isEmpty()) {
                if (isParentSelect(child, obj)) {
                    parent.setExpanded(true);
                    return true;
                }
            } else {
                return false;
            }

        }
        return false;

    }

    public void collapseAll(TreeItem node, boolean collapse) {
        if (!node.equals(getRoot())) {
            node.setExpanded(collapse);
        }

        node.getChildren().forEach(o -> collapseAll((TreeItem) o, collapse));
    }

    public void toggleItemCollapse(TreeItem node) {
        logger.debug("toggleItemCollapse: " + !node.isExpanded());
        collapseAll(node, !node.isExpanded());

    }


    /**
     * Initialize the jevis tree
     */
    private void init() {
        try {
            this.itemLoader = new JEVisItemLoader(this, this.ds.getObjects(), this.ds.getRootObjects());
            this.itemLoader.filterTree(this.cellFilter);
            setShowRoot(false);

            setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
            setTableMenuButtonVisible(true);

            this.plugins.addListener(this::onChanged);

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    @Deprecated
    public JEVisTreeRow getDragRow() {
        return this.dragItem;
    }

    public void setDragRow(JEVisTreeRow row) {
        this.dragItem = row;
    }

    public JEVisDataSource getJEVisDataSource() {
        return this.ds;
    }

    public ObservableList<JEVisObject> getHighlighterList() {
        return this.highlighterList;
    }

    public void openUserSelection(List<UserSelection> selection) {
//        logger.info("OpenUserselection: " + selection.size());
        for (UserSelection sel : selection) {
            List<JEVisObject> parents = new ArrayList<>();
            parents.add(sel.getSelectedObject());
            findNodePath(parents, sel.getSelectedObject());
            openPath(parents, getRoot());
        }
    }


    /**
     * This UserSelection doesn't open the children of the selected object
     **/
    public void openUserSelectionNoChildren(List<UserSelection> selection) {
        for (UserSelection sel : selection) {
            List<JEVisObject> parents = new ArrayList<>();
            parents.add(sel.getSelectedObject());
            findNodePath(parents, sel.getSelectedObject());
            openPathNoChildren(parents, getRoot(), sel.getSelectedObject());
        }
    }

    private void openPath(List<JEVisObject> toOpen, TreeItem<JEVisTreeRow> parentNode) {
        for (TreeItem<JEVisTreeRow> child : parentNode.getChildren()) {
            for (JEVisObject findObj : toOpen) {
                if (findObj.getID().equals(child.getValue().getJEVisObject().getID())) {
                    child.expandedProperty().setValue(Boolean.TRUE);
                    openPath(toOpen, child);
                }
            }

        }
    }

    private void openPathNoChildren(List<JEVisObject> toOpen, TreeItem<JEVisTreeRow> parentNode, JEVisObject selectedObject) {
        for (TreeItem<JEVisTreeRow> child : parentNode.getChildren()) {
            for (JEVisObject findObj : toOpen) {
                if (findObj.getID().equals(child.getValue().getJEVisObject().getID())) {
                    if (!findObj.getID().equals(selectedObject.getID())) {
                        child.expandedProperty().setValue(Boolean.TRUE);
                    } else {
                        child.setExpanded(Boolean.FALSE);
                    }
                    openPathNoChildren(toOpen, child, selectedObject);
                }
            }

        }
    }

    private void findNodePath(List<JEVisObject> parents, JEVisObject obj) {
        try {
            if (obj.getParents().size() >= 1) {
                JEVisObject parent = obj.getParents().get(0);
                parents.add(parent);
                findNodePath(parents, parent);
            }
        } catch (Exception ex) {
            logger.fatal("Error while searching parent: " + ex);
        }
    }

    public ObservableList<TreePlugin> getPlugins() {
        return this.plugins;
    }

    //    public ObservableList<ViewFilter> getFilter(){
//        return filter;
//    }
    public void setUserSelectionEnded() {
        for (TreePlugin plugin : this.plugins) {
            plugin.selectionFinished();
        }
    }

    public JEVisTreeFilter getFilter() {
        return this.cellFilter;
    }

    public void setFilter(JEVisTreeFilter filter) {
        this.cellFilter = filter;
        this.itemLoader.filterTree(filter);
        this.getVisibleObjects();
    }

    public JEVisObject getCopyObject() {
        return this.copyObject;
    }

    public void setCopyObject(JEVisObject obj, boolean cut) {
        this.copyObject = obj;
        this.isCut = cut;
    }

    public boolean isCut() {
        return this.isCut;
    }

    private TreeTableColumn findColumn(TreeTableColumn parentColumn, String columnName) {
        for (Object col : parentColumn.getColumns()) {
            TreeTableColumn column = (TreeTableColumn) col;
            if (column.getId() != null && column.getId().equals(columnName)) {
                return column;
            }
            TreeTableColumn childCol = findColumn(column, columnName);
            if (childCol != null) {
                return childCol;
            }
        }
        return null;
    }

    public TreeTableColumn getColumn(String columnName) {
        logger.debug("getColumn: " + columnName + " list size: " + getColumns().size());
        for (Object col : getColumns()) {
            TreeTableColumn column = (TreeTableColumn) col;

            if (column.getId() != null && column.getId().equals(columnName)) {
                return column;
            }

            TreeTableColumn childCol = findColumn(column, columnName);
            if (childCol != null) {
                return childCol;
            }
        }
        logger.debug("Did not find Column: " + columnName);
        return null;

    }

    public SearchFilterBar getSearchFilterBar() {
        return this.searchBar;
    }

    public void setSearchFilterBar(SearchFilterBar searchBar) {
        this.searchBar = searchBar;
    }

    private void onChanged(ListChangeListener.Change<? extends TreePlugin> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                for (TreePlugin plugin : c.getAddedSubList()) {
                    plugin.setTree(this);
                    getColumns().addAll(plugin.getColumns());
                }

            }
        }
    }
}
