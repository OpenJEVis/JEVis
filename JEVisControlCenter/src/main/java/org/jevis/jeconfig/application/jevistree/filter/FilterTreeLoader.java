package org.jevis.jeconfig.application.jevistree.filter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterTreeLoader {

    private JEVisTreeFilter filter = null;
    private JEVisTree tree;

    public void setTree(JEVisTree tree) {
        this.tree = tree;
        filter = tree.getFilter();
    }

    private void findFilterItems(Set<JEVisObject> foundItems, JEVisObject parent) {
        try {
            for (JEVisObject object : parent.getChildren()) {
                if (filter.showItem(object)) {
                    foundItems.add(object);
                }
                findFilterItems(foundItems, object);
            }
        } catch (Exception ex) {

        }
    }

    private void foundParents(Set<JEVisObject> foundItems, JEVisObject child) {
        try {
            if (child.getParents() != null) {
                for (JEVisObject parent : child.getParents()) {
                    if (!foundItems.contains(parent)) {
                        foundItems.add(parent);
                        foundParents(foundItems, parent);
                    }
                }
            }

        } catch (Exception ex) {

        }
    }

    private JEVisTreeItem buildItem(Set<JEVisObject> filterdObjs, JEVisObject object) throws JEVisException {
        JEVisTreeItem newItem = new JEVisTreeItem(object);

        // && filter.showItem(object)) {


        List<JEVisTreeItem> children = new ArrayList<>();
        for (JEVisObject child : object.getChildren()) {
            try {
                JEVisTreeItem newChild = buildItem(filterdObjs, child);
                if (newChild != null) {
                    children.add(newChild);
                }
            } catch (Exception ex) {
            }
        }

        for (JEVisType type : object.getJEVisClass().getTypes()) {
            if (filter.showItem(type)) {
                JEVisTreeItem newChild = new JEVisTreeItem(object.getAttribute(type.getName()));
                children.add(newChild);
            }
        }

//            newItem.setChildrenWorkaround(childeren);
        return newItem;

    }

    public ObservableList<JEVisTreeItem> loadTreeItems(List<JEVisObject> rootItems) {
        ObservableList<JEVisTreeItem> allItems = FXCollections.observableArrayList();

        Set<JEVisObject> foundItems = new HashSet<>();
        rootItems.forEach(jeVisObject -> {
            findFilterItems(foundItems, jeVisObject);

        });

        rootItems.forEach(jeVisObject -> {
            try {
                JEVisTreeItem item = buildItem(foundItems, jeVisObject);
                if (item != null) {
                    allItems.add(item);
                }
            } catch (Exception ex) {
            }
        });


        return allItems;

    }
}
