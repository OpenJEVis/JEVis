package org.jevis.jecc.tool;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class Layouts {

    public static void setAnchor(Node node, double value) {
        AnchorPane.setTopAnchor(node, value);
        AnchorPane.setRightAnchor(node, value);
        AnchorPane.setBottomAnchor(node, value);
        AnchorPane.setLeftAnchor(node, value);
    }

    public static void setSize(Region node, double maxWidth, double maxHeight) {
        node.setMinSize(maxWidth, maxHeight);
        node.setMinSize(maxWidth, maxHeight);
        node.setPrefSize(maxWidth, maxHeight);
    }

}
