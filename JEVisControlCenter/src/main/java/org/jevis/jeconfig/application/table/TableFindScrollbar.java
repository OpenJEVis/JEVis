package org.jevis.jeconfig.application.table;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;

import java.util.Set;

public interface TableFindScrollbar {

    public default ScrollBar findScrollBar(TableView tableView, Orientation orientation) {

        ScrollBar scrollBar = null;

        Set<Node> nodes = tableView.lookupAll(".scroll-bar");

        for (Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation() == orientation) {
                    scrollBar = bar;
                    break;
                }
            }
        }
        return scrollBar;

    }
}
