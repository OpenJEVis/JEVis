package org.jevis.jeconfig.application.jevistree.filter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The JEVisItemLoader creates the items for an JEVisTree. It also handel the filtering for the tree.
 *
 * @author florian.simon@envidatec.com
 */
public class JEVisItemLoader {

    private static final Logger logger = LogManager.getLogger(JEVisItemLoader.class);
    private final JEVisTree jeVisTree;
    private final List<JEVisObject> roots;
    private final ConcurrentHashMap<JEVisObject, JEVisTreeItem> itemObjectLinker = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JEVisTreeItem> itemAttributeLinker = new ConcurrentHashMap<>();


    /**
     * Create an new TreeItem loader.
     *
     * @param jeVisTree
     * @param objects
     * @param roots
     */
    public JEVisItemLoader(JEVisTree jeVisTree, List<JEVisObject> objects, List<JEVisObject> roots) {
        this.jeVisTree = jeVisTree;
        this.roots = roots;

        buildItems(objects);
        buildItems(this.roots);
    }

    /**
     * Reset items
     */
    private void cleanFilter() {
        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            try {
                jeVisTreeItem.getChildren().clear();
                jeVisTreeItem.setParentForFilter(false);
                jeVisTreeItem.setFiltered(false);
            } catch (Exception ex) {
            }
        });

        this.itemAttributeLinker.forEach((s, jeVisTreeItem) -> {
            try {
                jeVisTreeItem.getChildren().clear();
                jeVisTreeItem.setParentForFilter(false);
                jeVisTreeItem.setFiltered(false);
            } catch (Exception ex) {
            }
        });
    }

    public Collection<JEVisTreeItem> getAllItems() {
        return this.itemObjectLinker.values();
    }

    public List<JEVisObject> getVisibleObjects() {
        List<JEVisObject> result = new ArrayList<>();
        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            if (jeVisTreeItem.isFiltered() || jeVisTreeItem.isParentForFilter()) {
                result.add(jeVisTreeItem.getValue().getJEVisObject());
            }
        });

        return result;
    }

    /**
     * Clear all items for the garbage collector
     */
    public void clearForGC() {
//        for (JEVisTreeItem item : treeObjectItems) {
//            item = null;
//        }
//        treeObjectItems.clear();
        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            jeVisTreeItem = null;
        });
        this.itemObjectLinker.clear();
//        treeAttributeItems.clear();
        this.itemAttributeLinker.clear();

    }


    /**
     * Create JEVisTreeItem all objects and attributes.
     * NOTE: loading all attributes at this point will take some time if we do not
     * optimize the Webservice for it
     *
     * @param objects
     */
    private void buildItems(List<JEVisObject> objects) {

        for (JEVisObject object : objects) {
            try {
//                logger.debug("Create item for object: {}", object.getName());
                JEVisTreeItem item = new JEVisTreeItem(object);
                registerEventHandler(object);
                this.itemObjectLinker.put(object, item);
                for (JEVisAttribute attribute : object.getAttributes()) {
                    try {
                        JEVisTreeItem attributeItem = new JEVisTreeItem(attribute);
                        this.itemAttributeLinker.put(attributeKey(attribute), attributeItem);
                    } catch (Exception aex) {
                        logger.error("Error while loading type {}", attribute.getName(), aex);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error while loading object {}", object.getID(), ex);
            }
        }
    }

    private String attributeKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }

    public JEVisTreeItem getItemForObject(JEVisObject object) {
        if (this.itemObjectLinker.containsKey(object)) {
            return this.itemObjectLinker.get(object);
        }
        return null;
    }

    /**
     * Build an new single JEVisTreeItem out of an JEVIsoObject and check filter
     * This function will also set filter to true. Use this if after the tree is already filtered an new object is added.
     *
     * @param object
     */
    private void buildItems(JEVisObject object) {
        List<JEVisObject> list = new ArrayList<>();
        list.add(object);
        buildItems(list);

        /** the new item is always visible **/
        JEVisTreeItem newItem = this.itemObjectLinker.get(object);
        newItem.setFiltered(true);
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
            if (object == null || parents == null) {
                logger.debug("debug anker");
                return;
            }
            for (JEVisObject parent : object.getParents()) {
                /** continue travers if parent is not in list  **/
                if (!parents.contains(parent)) {
                    parents.add(parent);
                    parentCheck(parents, parent);
                }
            }
        } catch (Exception ex) {
            logger.error("Error while finding parents {}", Objects.requireNonNull(object).getID(), ex);
        }

    }

    /**
     * Internal debug help
     */
    private void printItemDebug() {
//        for (JEVisTreeItem item : treeObjectItems) {
//            if (item.getValue().getType() == JEVisTreeRow.TYPE.OBJECT) {
//                System.out.println("item: " + item);
//            }
//        }
    }


    /**
     * Filter the the Objects list and set the new root item for the tree
     *
     * @param filter
     * @return
     */
    public void filterTree(JEVisTreeFilter filter) {
        cleanFilter();
//        Benchmark benchmark = new Benchmark();

        Set<JEVisObject> neededParents = new HashSet<>();

        /** find matching objects **/
        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            boolean show = filter.showItem(jeVisTreeItem.getValue().getJEVisObject());
            if (show) {
                jeVisTreeItem.setFiltered(true);
                parentCheck(neededParents, jeVisTreeItem.getValue().getJEVisObject());
            }
        });

//        benchmark.printBenchmarkDetail("find matching objects");

        /** find matching attributes **/
        this.itemAttributeLinker.forEach((s, jeVisTreeItem) -> {
            try {
                boolean show = filter.showItem(jeVisTreeItem.getValue().getJEVisAttribute());
                if (show) {
                    jeVisTreeItem.setFiltered(true);
//                item.setParentForFilter(true);
                    parentCheck(neededParents, jeVisTreeItem.getValue().getJEVisObject());
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

//        benchmark.printBenchmarkDetail("find matching attributes");

        /** set needed parents **/
        for (JEVisObject obj : neededParents) {
            if (this.itemObjectLinker.containsKey(obj)) {
                this.itemObjectLinker.get(obj).setParentForFilter(true);
            } else {
                logger.error("Parent item not found: " + obj);
            }
        }


//        benchmark.printBenchmarkDetail("find parents");
        /** build children lists **/
        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            if ((jeVisTreeItem.isFiltered() || jeVisTreeItem.isParentForFilter()) && jeVisTreeItem.isObject()) {
                try {
                    update(jeVisTreeItem.getValue().getJEVisObject());
                } catch (Exception ex) {
                    logger.error("Error while adding children {}", jeVisTreeItem, ex);
                }
            }
        });
//        benchmark.printBenchmarkDetail("build children");

        this.itemObjectLinker.forEach((object, jeVisTreeItem) -> {
            if (!jeVisTreeItem.getChildren().isEmpty()) {
                jeVisTreeItem.getChildren().sort(JEVisTreeItem.jeVisTreeItemComparator);
            }
        });


        /** create an fake rootItem and add the root objects ad children if visible **/
        /** TODO: does not always work with all users. bug #455 **/
        JEVisTreeItem rootItem = new JEVisTreeItem();
        rootItem.setExpanded(true);
        for (JEVisObject rooObject : this.roots) {
            if (this.itemObjectLinker.containsKey(rooObject)) {
                JEVisTreeItem rootChild = this.itemObjectLinker.get(rooObject);
                if (rootChild.isFiltered() || rootChild.isParentForFilter()) {
                    rootItem.getChildren().add(rootChild);
                } else {
                    logger.debug("Root is filtered out: {}", rooObject);
                }

            } else {
                logger.error("Root is not build: {}", rooObject);
            }

        }

//        benchmark.printBenchmarkDetail("build root");

        this.jeVisTree.setRoot(rootItem);
    }

    /**
     * Update an object and its children
     *
     * @param object
     */
    public void update(JEVisObject object) {
        try {
            logger.trace("Update: {}", object);
            JEVisTreeItem item = this.itemObjectLinker.get(object);
            ObservableList<JEVisTreeItem> newChildrenList = FXCollections.observableArrayList();

            for (JEVisObject objChild : object.getChildren()) {
                /** add object children **/
                if (this.itemObjectLinker.containsKey(objChild)) {
                    JEVisTreeItem itemChild = this.itemObjectLinker.get(objChild);
                    if (itemChild.isParentForFilter() || itemChild.isFiltered()) {
                        newChildrenList.add(itemChild);
                    }
                }
            }
            /** add attributes children **/
            for (JEVisAttribute attribute : object.getAttributes()) {
//                    System.out.println(" ~~~> " + attribute.getName());
                if (this.itemAttributeLinker.containsKey(attributeKey(attribute))) {
                    JEVisTreeItem itemChild = this.itemAttributeLinker.get(attributeKey(attribute));
                    if (itemChild.isParentForFilter() || itemChild.isFiltered()) {
                        newChildrenList.add(itemChild);
                    }
                }
            }
            newChildrenList.sort(JEVisTreeItem.jeVisTreeItemComparator);
            if (item.getChildren() == null) {
                logger.error("Why is the list null");

            }
            item.getChildren().clear();
            item.getChildren().addAll(newChildrenList);

//            item.getChildren().forEach(jeVisTreeRowTreeItem -> {
//                System.out.println(" +++++> " + jeVisTreeRowTreeItem.getValue().getID());
//            });

        } catch (Exception ex) {
            logger.error(ex);
        }
    }


    /**
     * Add the EventListeners to the JEVisObject to handle delete,updateData, new and so on
     *
     * @param object
     */
    private void registerEventHandler(JEVisObject object) {
        /** TODO: an weak listener would be better **/
        object.addEventListener(event -> {
            logger.debug("Object Event [{}]: object [{}]{}  Source: {}", event.getType(), object.getID(), object.getName(), event.getSource());
            switch (event.getType()) {
                case OBJECT_DELETE:
                    /** nothing to do, we listen to the parent OBJECT_CHILD_DELETED event **/
                    break;
                case OBJECT_NEW_CHILD:
                    JEVisObject newObject = (JEVisObject) event.getObject();

                    if (newObject != null && !this.itemObjectLinker.containsKey(newObject)) {
                        buildItems(newObject);
                    } else if (newObject != null && this.itemObjectLinker.containsKey(newObject)) {
                        logger.error("Remove item from cache: {}", newObject);
                        this.itemObjectLinker.remove(newObject);
                        buildItems(newObject);
//                        if (this.itemObjectLinker.containsKey(newObject)) {
//                        logger.error("Remove item from jevis tree cache: {}", newObject);
//                        itemObjectLinker.remove(newObject);
//                        buildItems(newObject);
//                        }
                    }

                    if (newObject != null) {
                        Platform.runLater(() -> {
                            update(object);
                            update(newObject);
                            this.itemObjectLinker.get(object).setExpanded(true);
                            this.itemObjectLinker.get(newObject).setExpanded(false);

                            /** We do not want to select the new object for now, but maybe later in some cases **/
                            //jeVisTree.getSelectionModel().select(itemObjectLinker.get(newObject));
                        });
                    }
                    break;
                case OBJECT_CHILD_DELETED:
                    Platform.runLater(() -> update(object));
                    break;
                case OBJECT_UPDATED:
                    JEVisTreeItem itemToUpdate = this.itemObjectLinker.get(object);
                    if (itemToUpdate != null) {
                        TreeItem.TreeModificationEvent<JEVisTreeRow> treeEvent = new TreeItem.TreeModificationEvent<>(JEVisTreeItem.valueChangedEvent(), itemToUpdate);
                        Event.fireEvent(itemToUpdate, treeEvent);
                    }

                    break;
                default:
                    break;
            }
        });

    }


}
