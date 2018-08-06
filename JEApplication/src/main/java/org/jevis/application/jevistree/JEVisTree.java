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

import javafx.application.Platform;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");


    public void reload() {
        init();
    }

    private void init() {
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

//            setRowFactory(new Callback<TreeTableView, TreeTableRow<JEVisTreeRow>>() {
//                @Override
//                public TreeTableRow<JEVisTreeRow> call(final TreeTableView param) {
//                    final TreeTableRow<JEVisTreeRow> row = new TreeTableRow<JEVisTreeRow>();
//
//                    row.setOnDragDetected(event -> {
//                        try {
//                            if (!row.isEmpty()) {
//                                System.out.println("drag detect: " + row.getTreeItem().getValue().getID());
//                                System.out.println("is not null");
//                                Dragboard db = row.startDragAndDrop(TransferMode.ANY);
////                            db.setDragView(row.snapshot(null, null));
//                                try {
//                                    db.setDragView(SwingFXUtils.toFXImage(row.getTreeItem().getValue().getJEVisObject().getJEVisClass().getIcon(), null));
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                                ClipboardContent cc = new ClipboardContent();
//                                cc.putString(row.getIndex() + "");
//                                db.setContent(cc);
//                                event.consume();
//                            }
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    });
//
//                    row.setOnDragOver(event -> {
//                        try {
//                            System.out.println("drag over");
//                            Dragboard db = event.getDragboard();
//                            if (acceptable(db, row)) {
//                                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//                                event.consume();
//                            }
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    });
//
//                    row.setOnDragDropped(event -> {
//                        System.out.println("drag dropped");
//                        Dragboard db = event.getDragboard();
//                        System.out.println("Dropped: " + row.getTreeItem().getValue().getID());
//                        if (acceptable(db, row)) {
//                            int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
////                            TreeItem item = getTreeItem(index);
////                            item.getParent().getChildren().remove(item);
//
//
////                            getTarget(row).getChildren().add(item);
//                            event.setDropCompleted(true);
////                            tree.getSelectionModel().select(item);
//                            event.consume();
//                        }
//                    });
//                    row.setOnDragEntered(event -> {
//                        System.out.println("Drag enterd");
//                    });
//                    row.setOnDragDone(event -> {
//                        System.out.println("Drag done");
//                    });
//                    return row;
//                }
//            });

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(JEVisTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean acceptable(Dragboard db, TreeTableRow<JEVisTreeRow> row) {
        return true;
//        boolean result = false;
//        if (db.hasContent(SERIALIZED_MIME_TYPE)) {
//            int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
//            if (row.getIndex() != index) {
//                TreeItem target = getTarget(row);
//                TreeItem item = tree.getTreeItem(index);
//                result = !isParent(item, target);
//            }
//        }
//        return result;
    }

    public JEVisTree(JEVisDataSource ds) {
        super();
        this.ds = ds;
        rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
        init();
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
