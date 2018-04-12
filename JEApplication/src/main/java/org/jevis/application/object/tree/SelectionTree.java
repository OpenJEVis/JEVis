/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.object.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SelectionTree extends TreeView<ObjectContainer> {

    private JEVisDataSource _ds;

    private HashMap<String, TreeItem<ObjectContainer>> _containerCache = new HashMap<String, TreeItem<ObjectContainer>>();
//    private HashMap<String, TreeItem<ObjectContainer>> _itemCacheAtt = new HashMap<String, TreeItem<ObjectContainer>>();
    private HashMap<String, ObjectGraphic> _graphicCache;
    private HashMap<TreeItem<ObjectContainer>, ObservableList<TreeItem<ObjectContainer>>> _itemChildren;
    private ObservableList<TreeItem<ObjectContainer>> _emtyList = FXCollections.emptyObservableList();
    private List<ObjectContainer> _selectedContainer = new ArrayList<ObjectContainer>();

    public final BooleanProperty hasSelection = new SimpleBooleanProperty(false);

    private JEVisObject _dragObj;
    private boolean _multySelectAllowed = true;

    public SelectionTree(JEVisDataSource ds) {
        super();
        try {
            System.out.println("SelectionTree.ds: " + ds);
            _ds = ds;
            _containerCache = new HashMap<>();
            _graphicCache = new HashMap<>();
            _itemChildren = new HashMap<>();

            JEVisObject root = new JEVisRootObject(ds);
            final TreeItem<ObjectContainer> rootItem = buildItem(root);

            setShowRoot(false);

            getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//            getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ObjectContainer>>() {
//
//                @Override
//                public void changed(ObservableValue<? extends TreeItem<ObjectContainer>> ov, TreeItem<ObjectContainer> t, TreeItem<ObjectContainer> t1) {
//                    try {
//                        if (t1.getValue().getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
//                            System.out.println("changed: oh object is a link so im loading the linked object");
//                        } else {
//                        }
//
//                    } catch (Exception ex) {
//                    }
//                }
//            });

            setCellFactory(new Callback<TreeView<ObjectContainer>, TreeCell<ObjectContainer>>() {
                @Override
                public TreeCell<ObjectContainer> call(TreeView<ObjectContainer> p) {
                    return new ObjectCell() {

                        @Override
                        protected void updateItem(ObjectContainer obj, boolean emty) {
                            super.updateItem(obj, emty); //To change body of generated methods, choose Tools | Templates.
                            if (!emty) {
                                ObjectGraphic grapthi = getObjectGraphic(obj);
                                setGraphic(grapthi.getGraphic());
                            } else {
                                setGraphic(null);
                                setText(null);
                            }

                        }

                    };

                }
            });

            final KeyCombination copyID = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
            final KeyCombination copyObj = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
            final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
            final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
            final KeyCombination select = new KeyCodeCombination(KeyCode.SPACE);

            addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {

                    try {

                        if (select.match(t)) {
                            ObservableList<TreeItem<ObjectContainer>> selAll = getSelectionModel().getSelectedItems();
                            for (TreeItem<ObjectContainer> item : selAll) {
                                ObjectContainer container = item.getValue();
                                ObjectGraphic graph = getObjectGraphic(container);

                                if (graph.isSelected()) {
                                    graph.setSelect(false);
                                    removeSelected(container);
                                } else {
                                    graph.setSelect(true);
                                    addSelected(container);
                                }

                            }
                            t.consume();
                        } else if (rename.match(t)) {
                            t.consume();

                        }

                    } catch (Exception ex) {
                        System.out.println("execption while tree key event: " + ex);
                    }
                }

            });
            setId("selectiontree");

//            getStylesheets().add("/styles/Styles.css");
            getStylesheets().add("/styles/SelectionTree.css");
            setPrefWidth(500);
            setPrefHeight(1024);

            setRoot(rootItem);
            getSelectionModel().select(rootItem);
            setEditable(true);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    rootItem.setExpanded(true);
                    for (TreeItem item : rootItem.getChildren()) {
                        item.setExpanded(true);
                    }
                }
            });

        } catch (Exception ex) {
//            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    public BooleanProperty getSelectionProperty() {
        return hasSelection;
    }

    public void setAllowMultySelect(boolean isallowed) {
        _multySelectAllowed = isallowed;
        if (isallowed) {
            getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else {
            getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

    }

    public List<UserSelection> getUserSelection() {
        List<UserSelection> list = new ArrayList<>();

        for (ObjectContainer contaner : _selectedContainer) {
            if (contaner.isObject()) {
                UserSelection selection = new UserSelection(UserSelection.SelectionType.Object, contaner.getObject());
                list.add(selection);
            } else {
                UserSelection selection = new UserSelection(UserSelection.SelectionType.Attribute, contaner.getAttribute(), null, null);
                list.add(selection);
            }

        }
        return list;
    }

    public boolean isMultySelectAllowed() {
        return _multySelectAllowed;
    }

    public List<ObjectContainer> getSelected() {
        return _selectedContainer;
    }

    public void removeSelected(ObjectContainer container) {
        _selectedContainer.remove(container);
        if (_selectedContainer.isEmpty()) {
            hasSelection.setValue(false);
        }
    }

    public void addSelected(ObjectContainer container) {
//        System.out.println("add selection: " + container);
        hasSelection.setValue(true);
        if (_multySelectAllowed) {
            _selectedContainer.add(container);
        } else {
            unselectAll();
            _selectedContainer.add(container);
        }
    }

    private void unselectAll() {
        for (ObjectContainer contaner : _selectedContainer) {
            ObjectGraphic graph = getObjectGraphic(contaner);
            graph.setSelect(false);
        }
        _selectedContainer.clear();
    }

    public JEVisObject getDragItem() {
        return _dragObj;
    }

    public void setDragItem(JEVisObject obj) {
        _dragObj = obj;
    }

    public ObjectGraphic getObjectGraphic(ObjectContainer object) {
        if (_graphicCache.containsKey(object.getIdentifier())) {
            return _graphicCache.get(object.getIdentifier());
        }

        ObjectGraphic graph = new ObjectGraphic(object, this);
        _graphicCache.put(object.getIdentifier(), graph);

        return graph;
    }

    public TreeItem<ObjectContainer> getObjectTreeItem(JEVisObject object) {
        return buildItem(object);
    }

    public TreeItem<ObjectContainer> buildItem(JEVisAttribute attribute) {
//        System.out.println("BuildAttribute: " + attribute);
        String key = "" + attribute.getObject().getID() + attribute.getName();
        if (_containerCache.containsKey(key)) {
            return _containerCache.get(key);
        }

//        System.out.println("buildItem: " + object);
        final TreeItem<ObjectContainer> newItem = new ObjectItem(new ObjectContainer(attribute, this), this);
        _containerCache.put(key, newItem);

        return newItem;
    }

    public TreeItem<ObjectContainer> buildItem(JEVisObject object) {
//        System.out.println("buildObject: " + object);
        if (_containerCache.containsKey(object.getID().toString())) {
            return _containerCache.get(object.getID().toString());
        }

        ObjectContainer container = new ObjectContainer(object, this);
        final TreeItem<ObjectContainer> newItem = new ObjectItem(container, this);

        _containerCache.put(container.getIdentifier(), newItem);

        return newItem;
    }

    public void addChildrenList(TreeItem<ObjectContainer> item, ObservableList<TreeItem<ObjectContainer>> list) {
//        System.out.println("Add Children for:  " + item.getValue().getIdentifier());
        _itemChildren.put(item, list);
        try {

//            System.out.println("add attributes");
            //Now also add attributes
            for (JEVisAttribute child : item.getValue().getObject().getAttributes()) {
                TreeItem<ObjectContainer> newItem = buildItem(child);
                list.add(newItem);
            }

//            System.out.println("add Children");
            if (item.getValue().isObject()) {
                for (JEVisObject child : item.getValue().getObject().getChildren()) {
                    TreeItem<ObjectContainer> newItem = buildItem(child);
                    list.add(newItem);
                }
            }

        } catch (JEVisException ex) {
            Logger.getLogger(SelectionTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        sortList(list);

    }

    private void getAllExpanded(List<TreeItem<ObjectContainer>> list, TreeItem<ObjectContainer> item) {
        if (item.isExpanded()) {
            list.add(item);
            for (TreeItem<ObjectContainer> i : item.getChildren()) {
                getAllExpanded(list, i);
            }
        }
    }

//    private void expandAll(List<TreeItem<ObjectContainer>> list, TreeItem<ObjectContainer> root) {
////        System.out.println("expand all");
//        for (final TreeItem<ObjectContainer> item : root.getChildren()) {
//            for (final TreeItem<ObjectContainer> child : list) {
//                if (item.getValue().getID().equals(child.getValue().getID())) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            item.setExpanded(true);
//                        }
//                    });
//
//                }
//            }
//            expandAll(list, item);
//        }
//    }
    public void reload() {

    }

    public void expandSelected(boolean expand) {
        TreeItem<ObjectContainer> item = getSelectionModel().getSelectedItem();
        expand(item, expand);
    }

    private void expand(TreeItem<ObjectContainer> item, boolean expand) {
        if (!item.isLeaf()) {
            if (item.isExpanded() && !expand) {
                item.setExpanded(expand);
            } else if (!item.isExpanded() && expand) {
                item.setExpanded(expand);
            }

            for (TreeItem<ObjectContainer> child : item.getChildren()) {
                expand(child, expand);
            }
        }
    }

    public ObjectContainer getSelectedObject() {
        return getSelectionModel().getSelectedItem().getValue();
    }

    public static void sortList(ObservableList<TreeItem<ObjectContainer>> list) {

//        negativer Rückgabewert: Der erste Parameter ist untergeordnet
//        0 als Rückgabewert: Beide Parameter werden gleich eingeordnet
//        positiver Rückgabewert: Der erste Parameter ist übergeordnet
        Comparator<TreeItem<ObjectContainer>> sort = new Comparator<TreeItem<ObjectContainer>>() {

            @Override
            public int compare(TreeItem<ObjectContainer> o1, TreeItem<ObjectContainer> o2) {
//                System.out.println("Compare: \n " + o1 + " with\n " + o2);
                try {
                    if (o1.getValue().isObject() && o2.getValue().isObject()) {
                        if (o2.getValue().getObject().getJEVisClass() != null) {
                            int classCom = o1.getValue().getObject().getJEVisClass().compareTo(o2.getValue().getObject().getJEVisClass());

                            if (classCom == 0) {//Class is the same now use Name
                                return o2.getValue().getObject().getJEVisClass().compareTo(o2.getValue().getObject().getJEVisClass());
                            } else {
                                return classCom;
                            }
                        } else {
                            return o2.getValue().getObject().getJEVisClass().compareTo(o2.getValue().getObject().getJEVisClass());
                        }
                    } else if (o1.getValue().isObject() && !o2.getValue().isObject()) {
                        return 1;//att>obj
                    } else if (!o1.getValue().isObject() && o2.getValue().isObject()) {
                        return -1;
                    } else if (!o1.getValue().isObject() && !o2.getValue().isObject()) {
                        return o1.getValue().getAttribute().getName().compareTo(o2.getValue().getAttribute().getName());
                    } else {
                        return 0;
                    }

                } catch (JEVisException ex) {
//                    Logger.getLogger(ObjectItem.class.getName()).log(Level.SEVERE, null, ex);
                    throw new NullPointerException();
                }
            }
        };

        FXCollections.sort(list, sort);
    }

    /**
     *
     */
    public class ObjectCell extends TreeCell<ObjectContainer> {

        @Override
        protected void updateItem(final ObjectContainer obj, boolean emty) {
            super.updateItem(obj, emty);
            if (!emty) {
                ObjectGraphic grph = getObjectGraphic(obj);
                setText(grph.getText());
                setGraphic(grph.getGraphic());
//                setTooltip(grph.getToolTip());
//                setContextMenu(grph.getContexMenu())
            }

        }
    }

}
