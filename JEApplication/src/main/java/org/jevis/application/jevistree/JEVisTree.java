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
package org.jevis.application.jevistree;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.filter.JEVisItemLoader;
import org.jevis.application.jevistree.filter.JEVisTreeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The Central tree representation of the JEVisSystem.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTree extends TreeTableView {
    private static final Logger logger = LogManager.getLogger(JEVisTree.class);
    private ObservableList<TreePlugin> plugins = FXCollections.observableArrayList();
    private JEVisDataSource ds;
    private JEVisObject copyObject;
    private JEVisTreeRow dragItem;
    private SaveResourceBundle rb;
    private UUID uuid = UUID.randomUUID();
    private JEVisTreeFilter cellFilter;
    private JEVisItemLoader itemLoader;
    private ObservableList<JEVisObject> highliterList = FXCollections.observableArrayList();
    private List<JEVisObject> objects = new ArrayList<>();
    private boolean isCut = false;
    private SearchFilterBar searchBar;

    /**
     * Create an default Tree for the given JEVisDatasource by using all accessable JEVisOBjects starting by the
     * root objects.
     *
     * @param ds
     */
    public JEVisTree(JEVisDataSource ds, JEVisTreeFilter filter) {
        super();
        this.ds = ds;
        this.rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
//        cellFilter = FilterFactory.buildDefaultItemFilter();
        this.cellFilter = filter;
        this.getStylesheets().add(JEVisTree.class.getResource("/styles/JEVisTree.css").toExternalForm());
        init();
    }


    public UUID getUUID() {
        return uuid;
    }

    public void reload() {
        init();
    }

    public List<JEVisObject> getObjects() {
        return objects;
    }

    public List<JEVisObject> getVisibleObjects() {
        return itemLoader.getVisibleObjects();
    }

    public JEVisTreeItem getItemForObject(JEVisObject obj) {
        return itemLoader.getItemForObject(obj);
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

        node.getChildren().forEach(o -> {
            TreeItem item = (TreeItem) o;
            collapseAll(item, collapse);
        });
    }

    public void toggleItemCollapse(TreeItem node) {
        System.out.println("toggleItemCollapse: " + !node.isExpanded());
        collapseAll(node, !node.isExpanded());

    }


    /**
     * Initialize the jevis tree
     */
    private void init() {
        try {
            objects = ds.getObjects();
            itemLoader = new JEVisItemLoader(this, objects, ds.getRootObjects());
            itemLoader.filterTree(cellFilter);
            setShowRoot(false);

            setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
            setTableMenuButtonVisible(true);

            plugins.addListener(new ListChangeListener<TreePlugin>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends TreePlugin> c) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (TreePlugin plugin : c.getAddedSubList()) {
                                plugin.setTree(JEVisTree.this);
                                for (TreeTableColumn<JEVisTreeRow, Long> column : plugin.getColumns()) {
                                    JEVisTree.this.getColumns().add(column);
                                    System.out.println("Add plugin column to tree: " + column.getText());
                                }
                            }

                        }
                    }

                }
            });

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    public SaveResourceBundle getRB() {
        return rb;
    }

    public void setLocale(Locale locale) {
        rb = new SaveResourceBundle("jeapplication", locale);
    }

    @Deprecated
    public JEVisTreeRow getDragRow() {
        return dragItem;
    }

    public void setDragRow(JEVisTreeRow row) {
        this.dragItem = row;
    }

    public JEVisDataSource getJEVisDataSource() {
        return ds;
    }

    public ObservableList<JEVisObject> getHighliterList() {
        return highliterList;
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
                    child.expandedProperty().setValue(Boolean.TRUE);
                    openPathNoChildren(toOpen, child, selectedObject);
                }
                if (findObj.getID().equals(selectedObject.getID())) child.setExpanded(Boolean.FALSE);
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
        return plugins;
    }

    //    public ObservableList<ViewFilter> getFilter(){
//        return filter;
//    }
    public void setUserSelectionEnded() {
        for (TreePlugin plugin : plugins) {
            plugin.selectionFinished();
        }
    }

    public JEVisTreeFilter getFilter() {
        return this.cellFilter;
    }

    public void setFilter(JEVisTreeFilter filter) {
        this.cellFilter = filter;
        this.itemLoader.filterTree(filter);
    }

    public JEVisObject getCopyObject() {
        return copyObject;
    }

    public void setCopyObject(JEVisObject obj, boolean cut) {
        this.copyObject = obj;
        this.isCut = cut;
    }

    public boolean isCut() {
        return isCut;
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
        System.out.println("getColumn: " + columnName + " liste: " + getColumns().size());
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
        System.out.println("Did not found Column: " + columnName);
        return null;

    }

    public SearchFilterBar getSearchFilterBar() {
        return searchBar;
    }

    public void setSearchFilterBar(SearchFilterBar searchBar) {
        this.searchBar = searchBar;
    }

}
