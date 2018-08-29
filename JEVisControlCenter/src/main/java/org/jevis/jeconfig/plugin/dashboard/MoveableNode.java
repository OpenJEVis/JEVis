/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
//import jfxtras.labs.scene.control.window.SelectableNode;

/**
 * @author br
 */
public class MoveableNode extends GridPane {// implements SelectableNode {

    public static BooleanProperty movableProperty = new SimpleBooleanProperty(true);
    public static BooleanProperty movingProperty = new SimpleBooleanProperty(false);
    private static BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    private static BooleanProperty selectableProperty = new SimpleBooleanProperty(true);

    public MoveableNode() {
        super();
        init();
    }


    public BooleanProperty movableProperty() {
        return movableProperty;
    }

    public void setContent(Node content) {

        add(content, 0, 1, 2, 1);
    }


    private void init() {

        /**
         * Add Windows around the node, fro drag and drop
         */
        Label close = new Label();
        close.setMaxHeight(8);
        close.setMaxWidth(8);
        Region head = new Region();
        add(head, 0, 0);
        add(close, 1, 0);
        GridPane.setHgrow(head, Priority.ALWAYS);
        GridPane.setFillWidth(head, true);

        head.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        movableProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                head.setVisible(true);
            } else {
                this.setBorder(null);
                head.setVisible(false);
            }
        });
        movableProperty.setValue(true);
//        close.setOnAction(event -> {
//            this.setVisible(false);
//        });

        head.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                movingProperty.setValue(true);
//                Platform.runLater(() -> {
//                    WritableImage snapshot = MoveableNode.this.snapshot(new SnapshotParameters(), null);
//                    Cursor moveImage = new ImageCursor(snapshot);
////                    event.setDragDetect(true);
//
//                    getScene().setCursor(moveImage);
//                });
//                getScene().setCursor(Cursor.CLOSED_HAND);

            }
        });

        head.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double newX = MoveableNode.this.getLayoutX() + event.getX();
                double newY = MoveableNode.this.getLayoutY() + event.getY();


                MoveableNode.this.relocate(newX, newY);
                getScene().setCursor(Cursor.DEFAULT);
                movingProperty.setValue(false);
                System.out.println("Move end");
            }
        });


    }


}
