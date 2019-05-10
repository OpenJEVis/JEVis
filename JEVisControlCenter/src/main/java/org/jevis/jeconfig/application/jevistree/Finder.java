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
    private int lastIndex = 0;

    public Finder(JEVisTree jevisTree) {
        this.objects = jevisTree.getHighlighterList();
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

    private void showObject(JEVisObject obj) {
        Platform.runLater(() -> {
            try {
                System.out.println("showFind: " + obj);
                tree.openPathToObject(obj);

                JEVisTreeItem item = tree.getItemForObject(obj);
                if (item != null) {
                    tree.getSelectionModel().clearSelection();
                    tree.getSelectionModel().clearAndSelect(tree.getRow(item));

//                    tree.getRow(item);
                    tree.getSelectionModel().focus(tree.getRow(item));
                    tree.scrollTo(tree.getRow(item));


//                    tree.getSelectionModel().select(item);


//                    tree.getSelectionModel().select(item);

//                    System.out.println("Selected item: " + tree.getSelectionModel().getSelectedItem().toString());
//                    int selected = tree.getSelectionModel().getSelectedIndex();
//                    System.out.println("Index: " + selected);
//                    tree.scrollTo(selected);
                }

//                tree.getSelectionModel().select(selected);

//                VirtualFlow flow = (VirtualFlow) tree.getChildrenUnmodifiable().get(1);
//                int selected = tree.getSelectionModel().getSelectedIndex();
//                flow.show(selected);
//                tree.getSelectionModel().focus(selected);
//                tree.scrollTo(selected);

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
