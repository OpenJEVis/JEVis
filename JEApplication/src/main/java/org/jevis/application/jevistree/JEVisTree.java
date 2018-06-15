/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplicationL. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.jevistree;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.*;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.object.tree.UserSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTree extends TreeTableView {

    private ViewFilter _filter = ViewFilterFactory.createDefaultGraphFilter();
    private ObservableList<TreePlugin> plugins = FXCollections.observableArrayList();
    private JEVisDataSource ds;
    private JEVisObject copyObject;
//    public final DataFormat JEVisTreeRowFormate = new DataFormat("JEVisTreeRow");
//    private ObservableList<ViewFilter> filter = FXCollections.observableArrayList();

    private JEVisTreeRow dragItem;
    private SaveResourceBundle rb;

    public JEVisTree(JEVisDataSource ds) {
        super();
        this.ds = ds;
        rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
        try {
            JEVisTreeItem root = new JEVisTreeItem(this, ds);
            root.setExpanded(true);

            setRoot(root);
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
                                }
                            }

                        }
                    }

                }
            });

            setRowFactory(new Callback<TreeTableView, TreeTableRow<JEVisTreeRow>>() {
                @Override
                public TreeTableRow<JEVisTreeRow> call(final TreeTableView param) {
                    final TreeTableRow<JEVisTreeRow> row = new TreeTableRow<JEVisTreeRow>();

                    row.setOnDragDetected(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            try {
                                // drag was detected, start drag-and-drop gesture
                                TreeItem<JEVisTreeRow> selected = (TreeItem<JEVisTreeRow>) getSelectionModel().getSelectedItem();
                                // to access your RowContainer use 'selected.getValue()'

                                if (selected != null) {
                                    Dragboard db = startDragAndDrop(TransferMode.ANY);

//                                 create a miniature of the row you're dragging
                                    db.setDragView(SwingFXUtils.toFXImage(selected.getValue().getJEVisObject().getJEVisClass().getIcon(), null));
//                                 Keep whats being dragged on the clipboard
                                    ClipboardContent content = new ClipboardContent();
                                    content.putString(selected.getValue().getJEVisObject().getID() + "");
                                    System.out.println("---------------- " + selected.getValue().getJEVisObject().getName());
                                    setDragRow(selected.getValue());

                                    db.setContent(content);
                                    event.consume();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    row.setOnDragOver(new EventHandler<DragEvent>() {
                        @Override
                        public void handle(DragEvent event) {
                            // data is dragged over the target
                            Dragboard db = event.getDragboard();
                            if (event.getDragboard().hasString()) {
                                event.acceptTransferModes(TransferMode.MOVE);
                            }
                            event.consume();
                        }
                    });
                    row.setOnDragDropped(new EventHandler<DragEvent>() {
                        @Override
                        public void handle(DragEvent event) {

                            Dragboard db = event.getDragboard();
                            boolean success = false;
                            if (event.getDragboard().hasString()) {

                                if (!row.isEmpty()) {
                                    // This is were you do your magic.
                                    // Move your row in the tree etc
                                    // Here is two examples of how to access
                                    // the drop destination:

//                                    ClipboardContent content = new ClipboardContent();
//                                    JEVisObject toCopyObj = ((JEVisTreeRow) content.get(JEVisTreeRowFormate)).getJEVisObject();
                                }
                            }
                            success = true;
                            event.setDropCompleted(success);
                            event.consume();

                            int dropIndex = row.getIndex();
                            TreeItem<JEVisTreeRow> droppedon = row.getTreeItem();

                            if (getDragRow() != null) {
                                JEVisObject toCopyObj = getDragRow().getJEVisObject();

                                setCursor(Cursor.DEFAULT);
                                TreeHelper.EventDrop(JEVisTree.this, toCopyObj, droppedon.getValue().getJEVisObject());
                            }

                        }
                    });
                    return row;
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(JEVisTree.class.getName()).log(Level.SEVERE, null, ex);
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

    public void openUserSelection(List<UserSelection> selection) {
//        System.out.println("OpenUserselection: " + selection.size());
        for (UserSelection sel : selection) {
            List<JEVisObject> parents = new ArrayList<>();
            parents.add(sel.getSelectedObject());
            findNodePath(parents, sel.getSelectedObject());
            openPath(parents, getRoot());
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

    private void findNodePath(List<JEVisObject> parents, JEVisObject obj) {
        try {
            if (obj.getParents().size() >= 1) {
                JEVisObject parent = obj.getParents().get(0);
                parents.add(parent);
                findNodePath(parents, parent);
            }
        } catch (Exception ex) {
            System.out.println("Error while searching parent: " + ex);
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

    public void setFiler(ViewFilter filter) {
        _filter = filter;
    }

    public ViewFilter getFilter() {
        return _filter;
    }

    public void setCopyObject(JEVisObject obj) {
        copyObject = obj;
    }

    public JEVisObject getCopyObject() {
        return copyObject;
    }

}
