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

import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.jevistree.filter.DirectoryHelper;

import java.util.Comparator;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeItem extends TreeItem<JEVisTreeRow> {

    private static final Logger logger = LogManager.getLogger(JEVisTreeItem.class);
    private static Comparator<TreeItem<JEVisTreeRow>> comparator;
    private final JEVisTree jevisTree;
    private boolean isParentForFilter = false;
    private boolean isFilterd = false;

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

    public JEVisTreeItem(JEVisTree tree, JEVisAttribute att) {
        super();
        JEVisTreeRow sobj = new JEVisTreeRow(att);
        setValue(sobj);
        jevisTree = tree;
//        addEventHandler();
    }


    public static Comparator<TreeItem<JEVisTreeRow>> getComparator() {
        if (comparator == null) {
            comparator = new Comparator<TreeItem<JEVisTreeRow>>() {

                @Override
                public int compare(TreeItem<JEVisTreeRow> o1, TreeItem<JEVisTreeRow> o2) {
                    try {
                        JEVisTreeRow row1 = o1.getValue();
                        JEVisTreeRow row2 = o2.getValue();

                        if (row1.getType() == row2.getType()) {

                            /** if they are objects **/
                            if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {

                                boolean o1isDir = DirectoryHelper.getInstance(row1.getJEVisObject().getDataSource()).getDirectoryNames().contains(row1.getJEVisObject().getJEVisClassName());
                                boolean o2isDir = DirectoryHelper.getInstance(row1.getJEVisObject().getDataSource()).getDirectoryNames().contains(row2.getJEVisObject().getJEVisClassName());

                                /** Check if one of this is an directory, if it will be first **/
                                if (o1isDir && !o2isDir) {
                                    return -1;
                                } else if (!o1isDir && o2isDir) {
                                    return 1;
                                }

                                /** Sort by Classname **/
                                int className = row1.getJEVisObject().getJEVisClassName().compareTo(row2.getJEVisObject().getJEVisClassName());
                                if (className == 0) {
                                    /** if same class sort by name **/
                                    return row1.getJEVisObject().getName().compareTo(row2.getJEVisObject().getName());
                                } else {
                                    return className;
                                }


                            } else if (o1.getValue().getType() == JEVisTreeRow.TYPE.ATTRIBUTE) {
                                /** attributes are sorted by name **/
                                return row1.getJEVisAttribute().getName().compareTo(row2.getJEVisAttribute().getName());
                            }


                        } else {/** one is object the other attribute, Object before attribute **/
                            if (o1.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    } catch (Exception ex) {
                    }

                    /** if something goes wrong return equal **/
                    return 0;
                }

            };
        }
        return comparator;

    }

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

//    public void setChildrenWorkaround(List<JEVisTreeItem> children) {
//        _childLoaded = true;
//        super.getChildren().setAll(children);
//    }

    public void setFilterd(boolean filterd) {
        isFilterd = filterd;
    }

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
            return "JEVisTreeItem: [ isFiltered: " + isFilterd + " isParendFilterd:" + isParentForFilter + " object: " + getValue() + "]";
        } catch (Exception ex) {
            return "JEVisTreeItem: [ null ]";
        }
    }
}
