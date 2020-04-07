package org.jevis.jeconfig.tool;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Layouts {

    public static void setAnchor(Node node, double value) {
        AnchorPane.setTopAnchor(node, value);
        AnchorPane.setRightAnchor(node, value);
        AnchorPane.setBottomAnchor(node, value);
        AnchorPane.setLeftAnchor(node, value);
    }

    public static void setSize(Pane node, double maxWidth, double maxHeight){
        node.setMinSize(maxWidth,maxHeight);
        node.setMinSize(maxWidth,maxHeight);
        node.setPrefSize(maxWidth,maxHeight);
    }

}
