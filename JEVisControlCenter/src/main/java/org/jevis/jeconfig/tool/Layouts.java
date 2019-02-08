package org.jevis.jeconfig.tool;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class Layouts {

    public static void setAnchor(Node node, double value) {
        AnchorPane.setTopAnchor(node, value);
        AnchorPane.setRightAnchor(node, value);
        AnchorPane.setBottomAnchor(node, value);
        AnchorPane.setLeftAnchor(node, value);
    }
}
