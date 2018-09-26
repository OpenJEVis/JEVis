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
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.jevistree;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.ClassHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeItem extends TreeItem<JEVisTreeRow> {

    //    final JEVisObject _obj;
    private boolean _childLoaded = false;
    private final JEVisTree _tree;
    public static Logger logger = LogManager.getLogger(JEVisTreeItem.class);

    /**
     * Constructor for the Root Item. This them will call getRoot from the
     * Datasource
     *
     * @param tree
     * @param ds
     * @throws JEVisException
     */
    public JEVisTreeItem(JEVisTree tree, JEVisDataSource ds) throws JEVisException {
//        logger.trace("== init1 for root node==");
        JEVisObject _obj = new JEVisRootObject(ds);
        JEVisTreeRow sobj = new JEVisTreeRow(_obj);
        setValue(sobj);

        _tree = tree;

        addEventHandler();
    }

    public JEVisTreeItem(JEVisTree tree, JEVisObject obj) {
//        logger.trace("== init2 == {}", obj.getID());
        JEVisTreeRow sobj = new JEVisTreeRow(obj);
        setValue(sobj);
        _tree = tree;
        addEventHandler();

    }

    public JEVisTreeItem(JEVisTree tree, JEVisAttribute att) {
        JEVisTreeRow sobj = new JEVisTreeRow(att);
        setValue(sobj);
        _tree = tree;
        addEventHandler();
    }

    private void addEventHandler() {
        try {
            getValue().getJEVisObject().addEventListener(event -> {
                switch (event.getType()) {
                    case OBJECT_DELETE:
                        if (getParent() != null) {
                            getParent().getChildren().remove(JEVisTreeItem.this);

                            try {
                                logger.error("###Delete### Parent: {}", getParent().getValue().getJEVisObject().getName());
                                for (JEVisObject child : getParent().getValue().getJEVisObject().getChildren()) {
                                    logger.error("###Delete### child In DB: {}", child.getName());

                                }
                                for (TreeItem<JEVisTreeRow> child : getParent().getChildren()) {
                                    logger.error("###Delete### child In Tree: {}", child.getValue().getJEVisObject().getName());

                                }

                            } catch (Exception ex) {
                                logger.catching(ex);
                            }

                        }
                        break;
                    case OBJECT_NEW_CHILD:
                        JEVisObject ob = (JEVisObject) event.getSource();
                        logger.error("New Child Event: {}", ob.getID());

                        Platform.runLater(() -> {
                            setExpanded(false);
                            _childLoaded = false;
                            getChildren();

                            setExpanded(true);

                        });
                        break;
                    case OBJECT_CHILD_DELETED:
                        logger.error("Delete Child Event: {}", getValue().getJEVisObject().getID());
                        Platform.runLater(() -> {
                            setExpanded(false);
                            _childLoaded = false;
                            getChildren();

                            setExpanded(true);

                        });
                        break;
                    case OBJECT_UPDATED:
                        logger.trace("New Update Event: {}", getValue().getJEVisObject().getID());
                        Platform.runLater(() -> {

                            _childLoaded = false;

                            JEVisTreeRow sobj = new JEVisTreeRow(getValue().getJEVisObject());
                            this.setValue(sobj);

                        });
                        break;
                    default:
                        break;
                }

            });

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public ObservableList<TreeItem<JEVisTreeRow>> getChildren() {

        if (!_childLoaded) {
            _childLoaded = true;
//            super.getChildren().remove(0, super.getChildren().size());
            super.getChildren().clear();
            try {

                ViewFilter filter = _tree.getFilter();

                if (getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {

                    if (filter.showAttributes()) {
                        for (JEVisAttribute att : getValue().getJEVisObject().getAttributes()) {

                            if (filter.showAttribute(att)) {
                                super.getChildren().add(new JEVisTreeItem(_tree, att));
                            }
                        }
                    }

                    List<JEVisTreeItem> treeItems = new ArrayList<>();

                    for (JEVisObject child : getValue().getJEVisObject().getChildren()) {
                        try {
                            if (filter.showJEvisClass(child.getJEVisClass())) {
                                treeItems.add(new JEVisTreeItem(_tree, child));
//                                super.getChildren().add(new JEVisTreeItem(_tree, child));
                            }
                        } catch (NullPointerException ex) {
                            logger.catching(ex);
                        }
                    }
                    super.getChildren().addAll(treeItems);

                }

            } catch (JEVisException ex) {
                logger.catching(ex);
            }

            FXCollections.sort(super.getChildren(), new Comparator<TreeItem<JEVisTreeRow>>() {
                @Override
                public int compare(TreeItem<JEVisTreeRow> o1, TreeItem<JEVisTreeRow> o2) {
//                    logger.trace("Compare: {} to: {}", o1.getValue().getID(), o2.getValue().getID());

                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT && o2.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
//                    logger.trace("2");
                        try {
                            boolean o1IsDir = ClassHelper.isDirectory(o1.getValue().getJEVisObject().getJEVisClass());
                            boolean o2IsDir = ClassHelper.isDirectory(o2.getValue().getJEVisObject().getJEVisClass());


                            if (o1IsDir && !o2IsDir) {//o1 is dir
                                return -1;
                            } else if (!o1IsDir && o2IsDir) {//o2 is dir
                                return 1;
                            } else { //non or both are a dir
                                AlphanumComparator ac = new AlphanumComparator();

                                return ac.compare(o1.getValue().getJEVisObject().getName(), o2.getValue().getJEVisObject().getName());
                            }
                        } catch (JEVisException jex) {
                            logger.error(jex);
                        }

                    }

                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE && o2.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
//                    logger.trace("3");
                        return -1;
                    }

                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT && o2.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
//                    logger.trace("4");
                        return 1;
                    }

                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE && o2.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
//                    logger.trace("5");
                        return o1.getValue().getJEVisAttribute().getName().compareTo(o2.getValue().getJEVisAttribute().getName());
                    }
//                logger.trace("6");
                    return 0;
                }
            });
        }

        return super.getChildren();
//        return super.getChildren(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

}
