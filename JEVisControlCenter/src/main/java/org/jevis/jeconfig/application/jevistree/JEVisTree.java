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
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datasource.ChannelTools;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.application.jevistree.filter.JEVisItemLoader;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;

import java.util.*;

/**
 * The Central tree representation of the JEVisSystem.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTree extends TreeTableView {
    private static final Logger logger = LogManager.getLogger(JEVisTree.class);
    private final ObservableList<TreePlugin> plugins = FXCollections.observableArrayList();
    private final JEVisDataSource ds;
    private List<JEVisObject> copyObject = new ArrayList<>();
    private JEVisTreeRow dragItem;
    private final UUID uuid = UUID.randomUUID();
    private JEVisTreeFilter cellFilter;
    private JEVisItemLoader itemLoader;
    private final ObservableList<JEVisObject> highlighterList = FXCollections.observableArrayList();
    private boolean isCut = false;
    private SearchFilterBar searchBar;
    private final HashMap<String, Object> configMap = new HashMap<>();
    private final Map<Long, Long> calculationIDs = new HashMap<>();
    private final ItemActionController itemActionController;
    private JEVisObject recycleBinObject;
    private final Map<Long, Long> targetAndChannel = new HashMap<>();

    /**
     * Create an default Tree for the given JEVisDataSource by using all accessible JEVisObjects starting by the
     * root objects.
     *
     * @param ds
     */
    public JEVisTree(JEVisDataSource ds, JEVisTreeFilter filter) {
        super();

        this.setId("JEVisTree");
        this.ds = ds;
//        cellFilter = FilterFactory.buildDefaultItemFilter();
        this.cellFilter = filter;
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

//        addCellFactory();
        initialize();
        itemActionController = new ItemActionController(this);

    }

    protected void initialize() {
        try {
            loadCalcFilter();
            loadChannelFilter();

            List<JEVisObject> rootObjects = this.ds.getRootObjects();
            recycleBinObject = new JEVisRecycleBinObject(this.ds);
            rootObjects.add(recycleBinObject);

            this.itemLoader = new JEVisItemLoader(this, this.ds.getObjects(), rootObjects, recycleBinObject);
            this.itemLoader.filterTree(this.cellFilter);
            setShowRoot(false);

            setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
            setTableMenuButtonVisible(true);

            this.plugins.addListener(this::onChanged);

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    public JEVisObject getRecycleBinObject() {
        return recycleBinObject;
    }

    /**
     * @return Map of objects which are a calculation and there corresponding calculation object
     */
    public Map<Long, Long> getCalculationIDs() {
        return calculationIDs;
    }

    /**
     * @return Map of objects which are a channel target and there corresponding channel object
     */
    public Map<Long, Long> getTargetAndChannel() {
        return targetAndChannel;
    }

    /**
     * Find all calculations used to select icon
     */
    private void loadCalcFilter() {
        try {
            this.calculationIDs.clear();
            JEVisClass outputClass = ds.getJEVisClass("Output");
            ds.getObjects(outputClass, true).forEach(object -> {
                try {
                    TargetHelper th = new TargetHelper(ds, object.getAttribute("Output"));
                    calculationIDs.put(th.getObject().get(0).getID(), object.getParent().getID());
                } catch (Exception e) {
                    logger.error("Found calculation without valid target; {}", object.getID());
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Find all channel used to select icon
     */
    private void loadChannelFilter() {
        try {
            ChannelTools channelTools = new ChannelTools();
            channelTools.createChannelMaps(ds);

            this.targetAndChannel.clear();
            this.targetAndChannel.putAll(channelTools.getTargetAndChannel());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get all calculations and the corresponding data datarows.
     *
     * @return
     * @throws JEVisException
     */
    public Map<Long, Long> getENPICalcMap() throws JEVisException {

        Map<Long, Long> calcAndResult = new HashMap<>();

        JEVisClass calculation = this.getJEVisDataSource().getJEVisClass("Calculation");
        JEVisClass outputClass = this.getJEVisDataSource().getJEVisClass("Output");

        for (JEVisObject calculationObj : this.getJEVisDataSource().getObjects(calculation, true)) {
            try {
                List<JEVisObject> outputs = calculationObj.getChildren(outputClass, true);

                if (outputs != null && !outputs.isEmpty()) {
                    for (JEVisObject output : outputs) {
                        JEVisAttribute targetAttribute = output.getAttribute("Output");
                        if (targetAttribute != null) {
                            try {
                                TargetHelper th = new TargetHelper(this.getJEVisDataSource(), targetAttribute);
                                if (th.getObject() != null && !th.getObject().isEmpty()) {
                                    calcAndResult.put(th.getObject().get(0).getID(), calculationObj.getID());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return calcAndResult;
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
                logger.debug("3. Drag entered");
                event.consume();
            });

            row.setOnDragDropped(event -> {
                logger.debug("4. Drag dropped");
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
        //init();
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
        if (this.copyObject.isEmpty()) {
            return null;
        }
        return this.copyObject.get(0);
    }

    public List<JEVisObject> getCopyObjects() {
        return this.copyObject;
    }

    /**
     * @param obj
     * @param cut
     * @deprecated use setCopyObjectsBySelection(boolean cut)
     */
    @Deprecated
    public void setCopyObject(JEVisObject obj, boolean cut) {
        this.copyObject = new ArrayList<>();
        this.copyObject.add(obj);
        this.isCut = cut;
    }

    /**
     * @param objs
     * @param cut
     * @deprecated use setCopyObjectsBySelection(boolean cut)
     */
    @Deprecated
    public void setCopyObjects(List<JEVisObject> objs, boolean cut) {
        this.copyObject = objs;
        this.isCut = cut;
    }

    public void setCopyObjectsBySelection(boolean cut) {
        List<JEVisObject> selection = new ArrayList<>();

        logger.trace("--COPY--");
        getSelectionModel().getSelectedItems().forEach(o -> {
            try {
                JEVisObject toCopy = ((JEVisTreeItem) o).getValue().getJEVisObject();
                if (!isParentInList(selection, toCopy)) {
                    selection.add(toCopy);
                    logger.trace("--add: " + toCopy);
                }

            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        });
        logger.trace("--------");

        setCopyObjects(selection, false);
        this.isCut = cut;
    }

    public boolean isParentInList(List<JEVisObject> parents, JEVisObject object) {
        try {
            if (!object.getParents().isEmpty()) {
                if (parents.contains(object.getParents().get(0))) {
                    return true;
                } else {
                    return isParentInList(parents, object.getParents().get(0));
                }

            } else {
                return false;
            }
        } catch (Exception ex) {
            logger.error(ex);
            return false;
        }

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

    public ItemActionController getItemActionController() {
        return itemActionController;
    }

    public void setConfigObject(String key, Object object) {
        configMap.put(key, object);
    }

    public Object getConfigObject(String key) {
        return configMap.get(key);
    }

    public SearchFilterBar getSearchFilterBar() {
        return this.searchBar;
    }

    public void setSearchFilterBar(SearchFilterBar searchBar) {
        this.searchBar = searchBar;
    }

    /**
     * Help the garbage collector.
     * we have some kind of memory leak most likely because of the listeners.
     */
    public void destroy() {
        getItems().forEach(jeVisTreeItem -> {
            //jeVisTreeItem.getValue().getJEVisObject().get
        });
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
