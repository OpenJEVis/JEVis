package org.jevis.jeconfig.application.Chart.ChartPluginElements.tree;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

@FunctionalInterface
public interface TreeItemPredicate<T> {

    static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
        return (parent, value) -> predicate.test(value);
    }

    boolean test(TreeItem<T> parent, T value);

}
