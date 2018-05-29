/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.classes;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.application.cache.CacheEvent;
import org.jevis.application.cache.CacheEventHandler;
import org.jevis.application.cache.Cached;
import org.jevis.application.dialog.ConfirmDialog;
import org.jevis.commons.drivermanagment.ClassExporter;
import org.jevis.commons.relationship.RelationshipFactory;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.classes.editor.ClassEditor;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.NewClassDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassTree extends TreeView<JEVisClass> {

    private ClassEditor _editor = new ClassEditor();
    private boolean _editable = false;
    private JEVisDataSource _ds;

    private HashMap<String, TreeItem<JEVisClass>> _itemCache;
    private HashMap<String, ClassGraphic> _graphicCache;
    private HashMap<TreeItem<JEVisClass>, ObservableList<TreeItem<JEVisClass>>> _itemChildren;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ClassTree.class);

    private JEVisClass _dragClass;

    public ClassTree() {

    }

    public ClassTree(JEVisDataSource ds) {
        super();
        try {
            _ds = ds;

            _itemCache = new HashMap<>();
            _graphicCache = new HashMap<>();
            _itemChildren = new HashMap<>();

            JEVisClass root = new JEVisRootClass(ds);
            TreeItem<JEVisClass> rootItem = buildItem(root);

            setShowRoot(true);
            rootItem.setExpanded(true);

            getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            _editor.setTreeView(this);

            setCellFactory(new Callback<TreeView<JEVisClass>, TreeCell<JEVisClass>>() {

                //                @Override
//                public TreeCell<JEVisClass> call(TreeView<JEVisClass> p) {
//                    return new ClassCell();
//                }
                @Override
                public TreeCell<JEVisClass> call(TreeView<JEVisClass> param) {
                    return new TreeCell<JEVisClass>() {
//                        private ImageView imageView = new ImageView();

                        @Override
                        protected void updateItem(JEVisClass item, boolean empty) {
                            super.updateItem(item, empty);

                            if (!empty) {
                                ClassGraphic gc = getClassGraphic(item);
                                setContextMenu(gc.getContexMenu());
//                                setText(item);
                                setGraphic(gc.getGraphic());

                            } else {
                                setText(null);
                                setGraphic(null);
                            }
                        }
                    };
                }
            });

            getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<JEVisClass>>() {

                @Override
                public void changed(ObservableValue<? extends TreeItem<JEVisClass>> ov, TreeItem<JEVisClass> t, TreeItem<JEVisClass> t1) {
                    try {
                        if (t1 != null) {
                            _editor.setJEVisClass(t1.getValue());
                        }
                    } catch (Exception ex) {
                        System.out.println("Error while changing editor: " + ex);
                    }

                }
            });

            addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.F2) {
                        System.out.println("F2 rename event");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                fireEventRename();
                            }
                        });

                    } else if (t.getCode() == KeyCode.DELETE) {

                        fireDelete(getSelectionModel().getSelectedItem().getValue());
                    }
                }

            });

            setId("objecttree");

            getStylesheets().add("/styles/Styles.css");
            setPrefWidth(500);

            setRoot(rootItem);
            setEditable(true);

        } catch (Exception ex) {
//            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    public TreeItem<JEVisClass> buildItem(JEVisClass object) {
        if (object != null) {
            try {

                if (_itemCache.containsKey(object.getName())) {
                    return _itemCache.get(object.getName());
                }

                final TreeItem<JEVisClass> newItem = new ClassItem(object, this);
                _itemCache.put(object.getName(), newItem);

                if (object instanceof Cached) {
                    ((Cached) object).addEventHandler(new CacheEventHandler() {
                        @Override
                        public void handle(CacheEvent event) {
                            if (event.getType() == CacheEvent.TYPE.CLASS_CHILD_DELETE) {

                            } else if (event.getType() == CacheEvent.TYPE.CLASS_DELETE) {
                                try {

                                    _itemCache.remove(object.getName());
                                    newItem.getParent().getChildren();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                            } else if (event.getType() == CacheEvent.TYPE.CLASS_BUILD_CHILD) {
                                newItem.getChildren();
                            }
                        }
                    });
                }
                return newItem;
            } catch (JEVisException ex) {
                Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }

        return null;
    }

    public TreeItem<JEVisClass> getObjectTreeItem(JEVisClass object) {
        return buildItem(object);
    }

    public void addChildrenList(TreeItem<JEVisClass> item, ObservableList<TreeItem<JEVisClass>> list) {
        _itemChildren.put(item, list);
        try {
            for (JEVisClass child : item.getValue().getHeirs()) {

                //what was this again? Case rootnode maybe?
                //if (item.getValue().getName().equals("Classes")) {
                if (item.getValue().getName().equals(JEVisRootClass._name)) {
                    TreeItem<JEVisClass> newItem = buildItem(child);
                    list.add(newItem);
                } else if (child.getInheritance() != null && item.getValue().equals(child.getInheritance())) {
                    TreeItem<JEVisClass> newItem = buildItem(child);
                    list.add(newItem);

                }

//                for (JEVisClassRelationship rel : child.getRelationships(JEVisConstants.ClassRelationship.INHERIT, JEVisConstants.Direction.FORWARD)) {
//                    System.out.println("rel: " + rel);
//                    if (rel.getOtherClass(item.getValue().getInheritance())) {
//                        System.out.println("from: " +);
//                    } else {
//                        System.out.println("to");
//                    }
//                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
        }
//        sortList(list);

    }

    private void getAllExpanded(List<TreeItem<JEVisClass>> list, TreeItem<JEVisClass> item) {
        if (item.isExpanded()) {
            list.add(item);
            for (TreeItem<JEVisClass> i : item.getChildren()) {
                getAllExpanded(list, i);
            }
        }
    }

    private void expandAll(List<TreeItem<JEVisClass>> list, TreeItem<JEVisClass> root) {
//        System.out.println("expand all");
        for (final TreeItem<JEVisClass> item : root.getChildren()) {
            for (final TreeItem<JEVisClass> child : list) {
                try {
                    if (item.getValue().getName().equals(child.getValue().getName())) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                item.setExpanded(true);
                            }
                        });

                    }
                } catch (JEVisException ex) {
                    ex.printStackTrace();
                }
            }
            expandAll(list, item);
        }
    }

    public void reload(JEVisClass jclass) {
        if (jclass != null) {

            TreeItem newItem = buildItem(jclass);
            newItem.expandedProperty().setValue(false);
            newItem.expandedProperty().setValue(true);
        }

    }

    public ObservableList<TreeItem<JEVisClass>> getChildrenList(TreeItem<JEVisClass> item) {
        if (item == null || item.getValue() == null) {
            return FXCollections.emptyObservableList();
        }

        if (_itemChildren.containsKey(item)) {
            return _itemChildren.get(item);
        }

        ObservableList<TreeItem<JEVisClass>> list = FXCollections.observableArrayList();
        try {
            for (JEVisClass child : item.getValue().getHeirs()) {
                TreeItem<JEVisClass> newItem = buildItem(child);
                list.add(newItem);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
        }
//        sortList(list);
        _itemChildren.put(item, list);

        return list;

    }

    private void expand(TreeItem<JEVisClass> item, boolean expand) {
        if (!item.isLeaf()) {
            if (item.isExpanded() && !expand) {
                item.setExpanded(expand);
            } else if (!item.isExpanded() && expand) {
                item.setExpanded(expand);
            }

            for (TreeItem<JEVisClass> child : item.getChildren()) {
                expand(child, expand);
            }
        }
    }

    public void fireEventRename() {
        System.out.println("fireRename");

//        edit(_cl.getCurrentItem());
    }

    public void fireSaveAttributes(boolean ask) throws JEVisException {
//        TreeItem<JEVisClass> selectedItem = getSelectionModel().getSelectedItem();

//        getSelectionModel().getSelectedItem().getParent().setExpanded(false);
        if (ask) {
            _editor.checkIfSaved(null);
        } else {
            _editor.commitAll();

            //TODO: replace this dump way of refeshing
//
        }
//        getSelectionModel().getSelectedItem().setExpanded(true);

//        getSelectionModel().getSelectedItem().getParent().setExpanded(true);
//        getSelectionModel().select(selectedItem);
    }

    public void fireDelete(JEVisClass jclass) {
        System.out.println("======delete event");
        if (jclass == null) {
            jclass = getSelectionModel().getSelectedItem().getValue();
        }

        if (jclass != null) {
            try {
                ConfirmDialog dia = new ConfirmDialog();
                String question = "Do you want to delete the Class \"" + jclass.getName() + "\" ?";

                if (dia.show(JEConfig.getStage(), I18n.getInstance().getString("plugin.classes.tree.delete.title"),
                        I18n.getInstance().getString("plugin.classes.tree.delete.title_long"), question) == ConfirmDialog.Response.YES) {
                    try {
                        System.out.println("User want to delete: " + jclass.getName());

                        //done by datasource(server)
//                        if (jclass.getInheritance() != null) {
//                            for (JEVisClassRelationship rel : jclass.getRelationships(JEVisConstants.ClassRelationship.INHERIT)) {
//                                jclass.deleteRelationship(rel);
//                            }
//                        }

                        final TreeItem<JEVisClass> item = getObjectTreeItem(jclass);
                        final TreeItem<JEVisClass> parentItem = item.getParent();

                        jclass.delete();
                        deteleItemFromTree(item);

//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                parentItem.getChildren().remove(item);
//                                getSelectionModel().select(parentItem);
//                                parentItem.setExpanded(false);
//
//                            }
//                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (JEVisException ex) {
                Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void deteleItemFromTree(final TreeItem<JEVisClass> item) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        String className = item.getValue().getName();

                        _itemCache.remove(className);

                        for (Map.Entry<TreeItem<JEVisClass>, ObservableList<TreeItem<JEVisClass>>> entry : _itemChildren.entrySet()) {

                            if (entry.getValue().contains(item)) {
                                entry.getValue().remove(item);
                            }
                        }

                        getSelectionModel().select(item.getParent());

                        if (_graphicCache.containsKey(className)) {
                            _graphicCache.remove(className);
                        }

//                    parentItem.setExpanded(false);
                    } catch (JEVisException ex) {
                        Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void fireEventExport(ObservableList<TreeItem<JEVisClass>> items) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.getInstance().getString("plugin.classes.save.title"));
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("JEVis Files", "*.jev"),
                new ExtensionFilter("All Files", "*.*"));

        DateTime now = DateTime.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        if (items.size() > 1) {
            fileChooser.setInitialFileName("JEViClassExport_" + fmt.print(now) + ".jev");
        } else {
            try {
                fileChooser.setInitialFileName(items.get(0).getValue().getName() + "_" + fmt.print(now) + ".jev");
            } catch (JEVisException ex) {
                Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
        if (selectedFile != null) {
            List<JEVisClass> classes = new ArrayList<>();
            for (TreeItem<JEVisClass> item : items) {
                classes.add(item.getValue());
            }

            String extension = FilenameUtils.getExtension(selectedFile.getName());
            if (extension.isEmpty()) {
                selectedFile = new File(selectedFile.getAbsoluteFile() + ".jsv");
            }

            ClassExporter ce = new ClassExporter(selectedFile, classes);
//            mainStage.display(selectedFile);
        }
    }

    public void fireEventNew(TreeItem<JEVisClass> item) {
        try {
            logger.trace("Event.new {}", item.getValue().getName());
            NewClassDialog dia = new NewClassDialog();

            JEVisClass currentClass = null;

            if (item != null) {
                currentClass = item.getValue();
            } else if (item == null && getSelectionModel().getSelectedItem() != null) {
                currentClass = getSelectionModel().getSelectedItem().getValue();
            } else if (currentClass != null && currentClass.getName().equals("Classes")) {
                currentClass = null;
            }

            if (currentClass != null && currentClass.getName().equals("Classes")) {
                currentClass = null;
            }

            if (dia.show(JEConfig.getStage(), currentClass, _ds) == NewClassDialog.Response.YES
                    && dia.getClassName() != null
                    && !dia.getClassName().equals("")) {

                JEVisClass newClass = _ds.buildClass(dia.getClassName());
                //allway set the default icon
                if (dia.getInheritance() != null && dia.getInheritance().getIcon() != null) {
                    newClass.setIcon(dia.getInheritance().getIcon());
                }
                newClass.commit();
                TreeItem<JEVisClass> newItem;
                if (dia.getInheritance() != null) {
                    JEVisClassRelationship cr = RelationshipFactory.buildInheritance(dia.getInheritance(), newClass);
                    newItem = getObjectTreeItem(newClass);
//                    getChildrenList(getObjectTreeItem(dia.getInheritance())).add(getObjectTreeItem(newClass));
                    getChildrenList(getObjectTreeItem(dia.getInheritance())).add(newItem);
                } else {
                    newItem = getObjectTreeItem(newClass);
                    getChildrenList(getObjectTreeItem(getRoot().getValue())).add(newItem);
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (newItem != null) {
                            getSelectionModel().select(newItem);
                        }

                    }
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO i dont like this way
    public ClassEditor getEditor() {
        return _editor;
    }

    public ClassGraphic getClassGraphic(final JEVisClass object) {
        try {
            if (_graphicCache.containsKey(object.getName())) {
                return _graphicCache.get(object.getName());
            }

//        System.out.println("grahic does not exist create for: " + object);
            ClassGraphic graph = new ClassGraphic(object, this);

            _graphicCache.put(object.getName(), graph);

            return graph;
        } catch (JEVisException ex) {
            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public JEVisClass getDragItem() {
        return _dragClass;
    }

    public void setDragItem(JEVisClass obj) {
        _dragClass = obj;
    }

    public class ClassCell extends TreeCell<JEVisClass> {

        @Override
        protected void updateItem(final JEVisClass obj, boolean emty) {
            super.updateItem(obj, emty);
            if (!emty) {
                ClassGraphic grph = getClassGraphic(obj);
                setText(grph.getText());
                setGraphic(grph.getGraphic());
//                setTooltip(grph.getToolTip());
                setContextMenu(grph.getContexMenu());

                //---------------------- Drag & Drop part --------------
                setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent e) {
                        try {
                            ClipboardContent content = new ClipboardContent();
//                        content.putString(obj.getName());
                            Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
                            content.put(DataFormat.PLAIN_TEXT, obj.getName());
                            dragBoard.setContent(content);

                            setDragItem(obj);
                            e.consume();
                        } catch (JEVisException ex) {
                            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                setOnDragDone(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        try {
                            dragEvent.consume();
                        } catch (Exception ex) {
                            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                //TODO: check if its ok to move the Object here
                setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        try {
                            if (obj != null) {
                                if (!obj.equals(getDragItem()) && !getDragItem().getHeirs().contains(obj)) {
                                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                                }

                            } else {
//                                System.out.println("Drag Over NULL!!");
                            }

                            dragEvent.consume();
                        } catch (JEVisException ex) {
                            Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        try {
                            //if the inherit is the faske root we create no now relationship but set the inherit to null
                            if (obj.getName().equals(getRoot().getValue().getName())) {
                                for (JEVisClassRelationship rel : getDragItem().getRelationships(JEVisConstants.ClassRelationship.INHERIT)) {
                                    getDragItem().deleteRelationship(rel);
                                }

                            } else {
                                JEVisClassRelationship newRel = getDragItem().buildRelationship(obj, JEVisConstants.ClassRelationship.INHERIT, JEVisConstants.Direction.FORWARD);
                                for (JEVisClassRelationship rel : getDragItem().getRelationships(JEVisConstants.ClassRelationship.INHERIT)) {
                                    if (!rel.equals(newRel)) {
                                        getDragItem().deleteRelationship(rel);

                                        TreeItem<JEVisClass> dragParentItem = getObjectTreeItem(rel.getOtherClass(getDragItem().getInheritance()));
                                        getChildrenList(dragParentItem).remove(getObjectTreeItem(getDragItem()));

                                    }
                                }
                            }

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        TreeItem<JEVisClass> dragItem = getObjectTreeItem(getDragItem());

                                        TreeItem<JEVisClass> dragParentItem = dragItem.getParent();
                                        System.out.println("ParentItem: " + dragParentItem.getValue().getName());
                                        TreeItem<JEVisClass> targetItem = getObjectTreeItem(obj);

                                        getChildrenList(dragParentItem).remove(dragItem);
                                        getChildrenList(targetItem).add(dragItem);
                                        targetItem.setExpanded(true);

                                    } catch (JEVisException ex) {
                                        Logger.getLogger(ClassTree.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            });

                        } catch (JEVisException ex) {
                            Logger.getLogger(ClassCell.class.getName()).log(Level.SEVERE, null, ex);
                            ex.printStackTrace();
                        }

                        dragEvent.consume();
                    }
                });

            }

        }
    }
}
