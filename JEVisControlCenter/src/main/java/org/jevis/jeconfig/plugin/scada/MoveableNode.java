/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.scada;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.tool.I18n;

/**
 * @author
 */
public class MoveableNode extends StackPane {// implements SelectableNode {

    private static final Logger logger = LogManager.getLogger(MoveableNode.class);
    public static BooleanProperty movableProperty = new SimpleBooleanProperty(true);
    public static BooleanProperty movingProperty = new SimpleBooleanProperty(false);
    public Pane parent;
    public Node content;
    private Region ddOverlay = new Region();
    private Cursor cursor = null;
    private SCADAElement scadaElement;

    public MoveableNode() {
        super();
        init();
    }


    public double getRelativeXPos() {
        if (parent == null) {
            return 0;
        }
        logger.info("relX: parentW: " + parent.widthProperty().getValue() + " this.X: " + getLayoutX());
        return (100 / this.parent.widthProperty().getValue()) * this.getLayoutX();
    }

    public double getRelativeYPos() {
        if (parent == null) {
            return 0;
        }
        return (100 / this.parent.heightProperty().getValue()) * this.getLayoutY();
    }


    public void setParent(Pane parent) {
        this.parent = parent;
    }

    public BooleanProperty movableProperty() {
        return movableProperty;
    }

    public void setDCADAElement(SCADAElement scadaElement) {
        this.scadaElement = scadaElement;
    }

    public void setContent(Node content) {
        if (content != null) {
            getChildren().clear();
        }
        getChildren().add(content);
        getChildren().add(ddOverlay);
        this.content = content;
    }

    public void relocateRelativ(double xPercent, double yPercent) {

        double newX = (xPercent * this.parent.getWidth()) / 100;
        double newY = (yPercent * this.parent.getHeight()) / 100;
        this.relocate(newX, newY);
    }

    private Cursor getMoveCursor() {
        if (cursor == null) {
            Platform.runLater(() -> {
                Double width = new Double(this.getWidth());
                Double height = new Double(this.getHeight());
                logger.info("Screenshot size: " + width + " " + height);
                WritableImage image = new WritableImage(width.intValue(), height.intValue());
                snapshot(new SnapshotParameters(), image);
                cursor = new ImageCursor(image);
                setCursor(cursor);
            });

        }

        return cursor;

    }

    private void init() {
        Region head = new Region();

        Color transGrey = Color.web("#3399ff", 0.5);
        ddOverlay.prefWidthProperty().bind(this.widthProperty());
        ddOverlay.prefHeightProperty().bind(this.heightProperty());
        ddOverlay.setBackground(new Background(new BackgroundFill(transGrey
                , new CornerRadii(0, 0, 0, 0, false)
                , Insets.EMPTY)));


        movableProperty.addListener((observable, oldValue, newValue) -> {
            logger.info("movableProperty in label: " + newValue);
            if (newValue) {
//                this.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                ddOverlay.setVisible(true);
//                head.setVisible(true);
//                getChildren().add(ddOverlay);
            } else {
//                this.setBorder(null);
                ddOverlay.setVisible(false);
//                head.setVisible(false);
//                getChildren().remove(ddOverlay);
            }
        });
//        movableProperty.setValue(true);
//        close.setOnAction(event -> {
//            this.setVisible(false);
//        });


        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(I18n.getInstance().getString("plugin.scada.element.config.context.openconfig"));
        item1.setOnAction(event -> {
            scadaElement.openConfig();
        });
        MenuItem item2 = new MenuItem(I18n.getInstance().getString("plugin.scada.element.config.context.delete"));
        item2.setOnAction(event -> {
            //TODO: remove from plugin list
        });

        contextMenu.getItems().addAll(item1, item2);

        ddOverlay.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(ddOverlay, Side.BOTTOM, 0, 0);
            }


        });
        double keyStep = 20;
        head.setOnKeyPressed(event -> {
            logger.info("Key: " + event.getCharacter());
            if (event.getCode().isArrowKey()) {
                logger.info("arrow key");
                relocate(getLayoutX() - keyStep, getLayoutY());
            }
        });


        ddOverlay.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (movingProperty.getValue()) {
                    double newX = MoveableNode.this.getLayoutX() + event.getX();
                    double newY = MoveableNode.this.getLayoutY() + event.getY();

                    MoveableNode.this.relocate(newX, newY);
                }

            }
        });

        ddOverlay.setOnDragDetected(event -> {
            logger.info("Drag Detect");
            ddOverlay.startFullDrag();
            movingProperty.setValue(true);

        });


        ddOverlay.setOnDragDone(event -> {
            logger.info("Drag done");
        });

        ddOverlay.setOnDragExited(event -> {
            logger.info("Drag exit");
        });


    }

}
