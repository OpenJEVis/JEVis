package org.jevis.application.jevistree.filter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeItem;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.commons.utils.Benchmark;

import java.util.*;

/**
 * The JEVisItemLoader creates the items for an JEVisTree. It also handel the filtering for the tree.
 *
 * @author florian.simon@envidatec.com
 */
public class JEVisItemLoader {

    private static final Logger logger = LogManager.getLogger(JEVisItemLoader.class);
    final JEVisTree jeVisTree;
    final List<JEVisObject> allObjects;
    final List<JEVisObject> roots;
    final List<JEVisTreeItem> treeObjectItems = new ArrayList<>();
    final List<JEVisTreeItem> treeAttributeItems = new ArrayList<>();
    final Map<JEVisObject, JEVisTreeItem> itemObjectLinker = new TreeMap<>();
    final Map<JEVisAttribute, JEVisTreeItem> itemAttributeLinker = new HashMap<>();
    private Comparator<JEVisTreeItem> comperator;


    /**
     * Create an new TreeItem loader.
     *
     * @param jeVisTree
     * @param objects
     * @param roots
     */
    public JEVisItemLoader(JEVisTree jeVisTree, List<JEVisObject> objects, List<JEVisObject> roots) {
        this.jeVisTree = jeVisTree;
        this.allObjects = objects;
        this.roots = roots;

        /**
         * Default JEVisTree sort.
         * Order:
         * 1. Objects before Attributes
         * 2. Directory's before other JEVisClasses
         * 3. objects compared by JEVisClasses
         * 4. Objects compared by name
         * 5. Attributes by name
         */
        this.comperator = new Comparator<JEVisTreeItem>() {
            @Override
            public int compare(JEVisTreeItem o1, JEVisTreeItem o2) {
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

                /** if something goes wrong rerun eequal **/
                return 0;
            }
        };

        buildItems(this.allObjects);
    }

    /**
     * Rest items
     */
    private void cleanFilter() {
        for (JEVisTreeItem item : treeObjectItems) {
            item.getChildren().clear();
            item.setParentForFilter(false);
            item.setFilterd(false);
        }
    }

    /**
     * Clear all items for the garbage collector
     */
    public void clearForGC() {
        for (JEVisTreeItem item : treeObjectItems) {
            item = null;
        }
        treeObjectItems.clear();
        treeAttributeItems.clear();
        itemAttributeLinker.clear();
        itemObjectLinker.clear();
    }

    public void setItemComperator(Comparator<JEVisTreeItem> comperator) {

    }

    /**
     * Create JEVisTreeItem all objects and attributes.
     * NOTE: loading all attributes at this point will take some time if we do not
     * optimize the Webservice for it
     *
     * @param objects
     */
    public void buildItems(List<JEVisObject> objects) {
        for (JEVisObject object : objects) {
            try {
                if (object.getID().equals(1l)) {
                    System.out.println("found system");
                }
//                logger.error("Create item for object: {}", object.getName());
                JEVisTreeItem item = new JEVisTreeItem(jeVisTree, object);
                treeObjectItems.add(item);
                itemObjectLinker.put(object, item);
                for (JEVisAttribute attribute : object.getAttributes()) {
                    try {
//                        logger.error("Create item for attribute: {}", attribute.getName());
                        JEVisTreeItem attributeItem = new JEVisTreeItem(jeVisTree, attribute);
                        treeAttributeItems.add(attributeItem);
                        itemAttributeLinker.put(attribute, attributeItem);
                    } catch (Exception aex) {
                        logger.error("Error while loading type {}", attribute.getName(), aex);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error while loading object {}", object.getID(), ex);
            }
        }
    }

    /**
     * Travers objects and set parents of filtered object as needed the tree so the user can navigate
     * to the filtered objects or attributes.
     *
     * @param parents
     * @param object
     */
    private void parentCheck(Set<JEVisObject> parents, JEVisObject object) {
        try {
            for (JEVisObject parent : object.getParents()) {
                /** continue travers if parent is not in list  **/
                if (!parents.contains(parent)) {
                    parents.add(parent);
                    parentCheck(parents, parent);
                }
            }
        } catch (Exception ex) {
            logger.error("Error while finding parents {}", object.getID(), ex);
        }

    }

    /**
     * Filter the the Objects list and set the new root item for the tree
     *
     * @param filter
     * @return
     */
    public void filterTree(CellFilter filter) {
        System.out.println("===== Start treeFilter ======");
        Benchmark benchmark = new Benchmark();
        cleanFilter();
        Set<JEVisObject> neededParents = new HashSet<>();

        /** find matching objects **/
        for (JEVisTreeItem item : treeObjectItems) {
            boolean show = filter.showItem(item.getValue().getJEVisObject());
            item.setFilterd(show);
            parentCheck(neededParents, item.getValue().getJEVisObject());
        }

        benchmark.printBechmark("find matching objects");

        /** find matching attributes **/
        for (JEVisTreeItem item : treeAttributeItems) {
            boolean show = filter.showItem(item.getValue().getJEVisAttribute());
            item.setFilterd(show);
            if (show) {
                item.setParentForFilter(true);
            }
            parentCheck(neededParents, item.getValue().getJEVisObject());
        }
        benchmark.printBechmark("find matching attributes");

        /** set needed parents **/
        for (JEVisTreeItem item : treeObjectItems) {
            if (neededParents.contains(item.getValue().getJEVisObject())) {
                item.setParentForFilter(true);
            }
        }
        benchmark.printBechmark("find parents");
        /** build children lists **/
        for (JEVisTreeItem item : treeObjectItems) {
            if ((item.isFilterd() || item.isParentForFilter()) && item.isObject()) {

                ObservableList<JEVisTreeItem> newChildrenList = FXCollections.observableArrayList();
                try {
                    for (JEVisObject objChild : item.getValue().getJEVisObject().getChildren()) {
                        /** add object children **/
                        if (itemObjectLinker.containsKey(objChild)) {
                            JEVisTreeItem itemChild = itemObjectLinker.get(objChild);
                            if (itemChild.isParentForFilter() || itemChild.isFilterd()) {
                                newChildrenList.add(itemChild);
                            }

                        }
                        /** add attributes children **/
                        for (JEVisAttribute attribute : item.getValue().getJEVisObject().getAttributes()) {
                            if (itemAttributeLinker.containsKey(attribute)) {
                                JEVisTreeItem itemChild = itemAttributeLinker.get(attribute);
                                if (itemChild.isParentForFilter() || itemChild.isFilterd()) {
                                    newChildrenList.add(itemChild);
                                }
                            }
                        }
                    }
                    newChildrenList.sort(comperator);
                    item.getChildren().setAll(newChildrenList);
                } catch (Exception ex) {
                    logger.error("Error while adding children {}", item, ex);
                }
            }
        }
        benchmark.printBechmark("build chindren");

//        Arrays.sort();

        /** create an fake rootItem and add the root objects ad children if visible **/
        JEVisTreeItem rootItem = new JEVisTreeItem(jeVisTree);
        rootItem.setExpanded(true);
        for (JEVisObject rooObject : roots) {
            if (itemObjectLinker.containsKey(rooObject)) {
                JEVisTreeItem rootChild = itemObjectLinker.get(rooObject);
                if (rootChild.isFilterd() || rootChild.isParentForFilter()) {
                    rootItem.getChildren().add(rootChild);
                    System.out.println("Add to root: " + rootChild.getValue().getJEVisObject());
                } else {
                    logger.warn("Root is filtered out: {}", rooObject);
                }

            } else {
                logger.error("Root is not build: {}", rooObject);
            }

        }

        benchmark.printBechmark("build root");

        jeVisTree.setRoot(rootItem);
    }

}
