/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.dashboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.Node;

/**
 *
 * @author br
 */
public class SelectionModel {

    Set<Node> selection = new HashSet<>();

    public void add(Node node) {

        if (!node.getStyleClass().contains("highlight")) {
            node.getStyleClass().add("highlight");
        }

        selection.add(node);
    }

    public void remove(Node node) {
        node.getStyleClass().remove("highlight");
        selection.remove(node);
    }

    public void clear() {

        while (!selection.isEmpty()) {
            remove(selection.iterator().next());
        }

    }

    public boolean contains(Node node) {
        return selection.contains(node);
    }

    public int size() {
        return selection.size();
    }

    public void log() {
        System.out.println("Items in model: " + Arrays.asList(selection.toArray()));
    }

}
