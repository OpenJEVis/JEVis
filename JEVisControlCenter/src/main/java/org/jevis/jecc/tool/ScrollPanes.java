package org.jevis.jecc.tool;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScrollPanes {

    private static final Logger logger = LogManager.getLogger(ScrollPanes.class);

    /**
     * Resets the viewport of the first ScrollPane parent of the given Node
     *
     * @param node
     */
    public static void resetParentScrollView(Node node) {
        try {
            if (node.getParent() != null) {
                if (node.getParent() instanceof ScrollPane) {
                    ScrollPane parentScroll = (ScrollPane) node.getParent();
                    parentScroll.setVvalue(0);
                    parentScroll.setHvalue(0);
                } else {
                    resetParentScrollView(node.getParent());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
