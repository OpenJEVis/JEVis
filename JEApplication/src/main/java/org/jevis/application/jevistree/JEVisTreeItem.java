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
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeItem extends TreeItem<JEVisTreeRow> {

    private static final Logger logger = LogManager.getLogger(JEVisTreeItem.class);
    private final JEVisTree jevisTree;
    //    final JEVisObject _obj;
//    private boolean _childLoaded = false;
    private boolean isParentForFilter = false;
    private boolean isFilterd = false;
    private String attributeName = "";


    /**
     * Constructor for the Root Item. This them will call getRoot from the
     * Datasource
     *
     * @param tree
     * @throws JEVisException
     */
    public JEVisTreeItem(JEVisTree tree) {
        super();
//        logger.trace("== init1 for root node==");
        JEVisObject _obj = new JEVisRootObject();
        JEVisTreeRow sobj = new JEVisTreeRow(_obj);
        setValue(sobj);

        jevisTree = tree;
//        addEventHandler();
    }

    public JEVisTreeItem(JEVisTree tree, JEVisObject obj) {
        super();
//        logger.trace("== init2 == {}", obj.getID());
        JEVisTreeRow sobj = new JEVisTreeRow(obj);
        setValue(sobj);
        jevisTree = tree;
//        addEventHandler();

    }

//    public JEVisTreeItem(JEVisTree tree, String attributeName) {
////        logger.trace("== init2 == {}", obj.getID());
//        this.attributeName = attributeName;
////        JEVisTreeRow sobj = new JEVisTreeRow(obj);
////        setValue(sobj);
//        jevisTree = tree;
////        addEventHandler();
//
//    }

    public JEVisTreeItem(JEVisTree tree, JEVisAttribute att) {
        super();
        JEVisTreeRow sobj = new JEVisTreeRow(att);
        setValue(sobj);
        jevisTree = tree;
//        addEventHandler();
    }

//    public JEVisTreeItem(JEVisTree tree, JEVisType type) {
////        logger.trace("== init2 == {}", obj.getID());
//        JEVisTreeRow sobj = new JEVisTreeRow(obj);
//        setValue(sobj);
//        jevisTree = tree;
//        addEventHandler();
//
//    }

    public boolean isObject() {
        return getValue().getType() == JEVisTreeRow.TYPE.OBJECT;
    }

    /**
     * True if this item is needed to display an positive filtered child.
     *
     * @return
     */
    public boolean isParentForFilter() {
        return isParentForFilter;
    }

    /**
     * @param parentForFilter
     */
    public void setParentForFilter(boolean parentForFilter) {
        isParentForFilter = parentForFilter;
    }

    /**
     * True if this element positive filters, so its visible
     *
     * @return
     */
    public boolean isFilterd() {
        return isFilterd;
    }

    public void setFilterd(boolean filterd) {
        isFilterd = filterd;
    }

//    public void setChildrenWorkaround(List<JEVisTreeItem> children) {
//        _childLoaded = true;
//        super.getChildren().setAll(children);
//    }

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
//                            _childLoaded = false;
                            getChildren();

                            setExpanded(true);

                        });
                        break;
                    case OBJECT_CHILD_DELETED:
                        logger.error("Delete Child Event: {}", getValue().getJEVisObject().getID());
                        Platform.runLater(() -> {
                            setExpanded(false);
//                            _childLoaded = false;
                            getChildren();

                            setExpanded(true);

                        });
                        break;
                    case OBJECT_UPDATED:
                        logger.trace("New Update Event: {}", getValue().getJEVisObject().getID());
                        Platform.runLater(() -> {

//                            _childLoaded = false;

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

//    @Override
//    public ObservableList<TreeItem<JEVisTreeRow>> getChildren() {
//
//        if (!_childLoaded) {
//            _childLoaded = true;
//            super.getChildren().clear();
//            try {
//
//
////
////                if (getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
////
////                    for (JEVisAttribute att : getValue().getJEVisObject().getAttributes()) {
////                        JEVisTreeItem newAttribute = new JEVisTreeItem(jevisTree, att);
////                        boolean showRow = cellFilter.showRow(newAttribute);
////                        if (showRow) {
////                            super.getChildren().add(newAttribute);
////                        } else {
////                            newAttribute = null;//GC help
////                        }
////                    }
////
////                    List<JEVisTreeItem> treeItems = new ArrayList<>();
////
////                    for (JEVisObject child : getValue().getJEVisObject().getChildren()) {
////                        try {
////                            JEVisTreeItem newChildObj = new JEVisTreeItem(jevisTree, child);
////                            boolean showRow = cellFilter.showRow(newChildObj);
////                            if (showRow) {
////                                treeItems.add(newChildObj);
////                            } else {
////                                newChildObj = null;//GC help
////                            }
////
////                        } catch (Exception ex) {
////                            logger.catching(ex);
////                        }
////                    }
////                    super.getChildren().addAll(treeItems);
////                }
//
//
//            } catch (Exception ex) {
//                logger.catching(ex);
//            }
//
//            FXCollections.sort(super.getChildren(), new Comparator<TreeItem<JEVisTreeRow>>() {
//                @Override
//                public int compare(TreeItem<JEVisTreeRow> o1, TreeItem<JEVisTreeRow> o2) {
////                    logger.trace("Compare: {} to: {}", o1.getValue().getID(), o2.getValue().getID());
//
//                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT && o2.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
////                    logger.trace("2");
//                        try {
//                            boolean o1IsDir = ClassHelper.isDirectory(o1.getValue().getJEVisObject().getJEVisClass());
//                            boolean o2IsDir = ClassHelper.isDirectory(o2.getValue().getJEVisObject().getJEVisClass());
//
//
//                            if (o1IsDir && !o2IsDir) {//o1 is dir
//                                return -1;
//                            } else if (!o1IsDir && o2IsDir) {//o2 is dir
//                                return 1;
//                            } else { //non or both are a dir
//                                AlphanumComparator ac = new AlphanumComparator();
//
//                                return ac.compare(o1.getValue().getJEVisObject().getName(), o2.getValue().getJEVisObject().getName());
//                            }
//                        } catch (JEVisException jex) {
//                            logger.error(jex);
//                        }
//
//                    }
//
//                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE && o2.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
////                    logger.trace("3");
//                        return -1;
//                    }
//
//                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT && o2.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
////                    logger.trace("4");
//                        return 1;
//                    }
//
//                    if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE && o2.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
////                    logger.trace("5");
//                        return o1.getValue().getJEVisAttribute().getName().compareTo(o2.getValue().getJEVisAttribute().getName());
//                    }
////                logger.trace("6");
//                    return 0;
//                }
//            });
//        }
//
//        return super.getChildren();
////        return super.getChildren(); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
//        System.out.println("TreeItem.equals");
//        if (obj != null && obj instanceof JEVisTreeItem) {
//            JEVisTreeRow otherRow = ((JEVisTreeItem) obj).getValue();
//
//            return otherRow.equals(((JEVisTreeItem) obj).getValue());
//
//        }
//        return false;
        return super.equals(obj);
    }


    @Override
    public String toString() {
        try {
            return "JEVisTreeItem: [" + getValue() + "]";
        } catch (Exception ex) {
            return "JEVisTreeItem: [ null ]";
        }
    }
}
