package org.jevis.jecc.application.table;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;

import java.util.Set;

public interface TableFindScrollbar {

    default ScrollBar findScrollBar(TableView tableView, Orientation orientation) {

        ScrollBar scrollBar = null;

        Set<Node> nodes = tableView.lookupAll(".scroll-bar");

        for (Node node : nodes) {
            if (node instanceof ScrollBar bar) {
                if (bar.getOrientation() == orientation) {
                    scrollBar = bar;
                    break;
                }
            }
        }
        return scrollBar;

    }
}
