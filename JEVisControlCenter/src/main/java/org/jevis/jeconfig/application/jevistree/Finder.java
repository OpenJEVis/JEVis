package org.jevis.jeconfig.application.jevistree;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class Finder {

    private final ObservableList<JEVisObject> objects;
    private final JEVisTree tree;
    private JEVisObject selected;
    private final int lastIndex = 0;

    public Finder(JEVisTree jevisTree) {
        this.objects = jevisTree.getVisibleObjects();
        this.tree = jevisTree;
    }

    public void goNext() {
        if (objects.isEmpty()) {
            return;
        }
        if (selected == null) {
            selected = objects.get(0);
        } else {
            int lastIndex = objects.indexOf(selected);
            lastIndex++;

            /** at the end go back to first **/
            if (lastIndex >= objects.size()) {
                lastIndex = 0;
            }

            selected = objects.get(lastIndex);
        }

        showObject(selected);

    }

    public void showObject(JEVisObject obj) {
        Platform.runLater(() -> {
            try {
                JEVisTreeItem item = tree.getItemForObject(obj);
                if (item != null) {
                    tree.getSelectionModel().clearSelection();
                    Platform.runLater(() -> {
                        item.setExpanded(true);
                        tree.openPathToObject(obj);
                        tree.getSelectionModel().select(item);
                        int index = tree.getRow(item) - 5 < 0 ? 0 : tree.getRow(item) - 5;

                        tree.scrollTo(index);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void goPrevious() {
        if (objects.isEmpty()) {
            return;
        }
        if (selected == null) {
            selected = objects.get(0);
        } else {
            int lastIndex = objects.indexOf(selected);
            lastIndex--;

            /** at the beginning to to end **/
            if (lastIndex < 0) {
                lastIndex = objects.size() - 1;
            }

            selected = objects.get(lastIndex);
        }

        showObject(selected);
    }

    public boolean findMatch(String text) {
        selected = null;

        List<JEVisObject> result = new ArrayList<>();
        for (JEVisObject object : tree.getVisibleObjects()) {

            if (StringUtils.containsIgnoreCase(object.getName(), text)) {
//                System.out.println("Found match: " + object);
                result.add(object);
            }

            if (StringUtils.containsIgnoreCase(object.getID().toString(), text)) {
//                System.out.println("Found match ID: " + object);
                result.add(object);
            }
        }

//        System.out.println("Results: " + result.size() + " : " + Arrays.toString(result.toArray()));
        //TODO: order in the same order as the tree
        objects.clear();
        if (!result.isEmpty()) {
            objects.setAll(result);
            if (!objects.isEmpty()) {
                goNext();
                return true;
            }

        }

        return false;
    }

}
