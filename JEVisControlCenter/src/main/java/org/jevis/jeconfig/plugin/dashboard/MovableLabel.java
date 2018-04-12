/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
//import jfxtras.labs.scene.control.window.SelectableNode;

/**
 *
 * @author br
 */
public class MovableLabel extends Label{// implements SelectableNode {

    private static BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    private static BooleanProperty selectableProperty = new SimpleBooleanProperty(true);
    private boolean moveable;
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;
    private DataObject object;

    public MovableLabel(DataObject object) {
        super();
        this.object = object;
        init();

    }

    public MovableLabel(String text, DataObject object) {
        super(text);
        this.object = object;
        init();
    }

    public MovableLabel(String text, Node graphic, DataObject object) {
        super(text, graphic);
        this.object = object;
        init();
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void moveable(boolean isMoveable) {
        moveable = isMoveable;
    }

    private void init() {
        this.setOnMousePressed(onMousePressedEventHandler);
        this.setOnMouseDragged(onMouseDraggedEventHandler);
        moveable = false;

        //Cordinates from DataObjekt
        this.setTranslateX(object.getCoordinates().getX());
        this.setTranslateY(object.getCoordinates().getY());

        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    EventHandler<MouseEvent> onMousePressedEventHandler = (MouseEvent t) -> {
        if (moveable) {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            orgTranslateX = ((Label) (t.getSource())).getTranslateX();
            orgTranslateY = ((Label) (t.getSource())).getTranslateY();
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = (MouseEvent t) -> {
        if (moveable) {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            object.setCoordinates(newTranslateX, newTranslateY);
            ((Label) (t.getSource())).setTranslateX(newTranslateX);
            ((Label) (t.getSource())).setTranslateY(newTranslateY);
        }
    };

    public boolean requestSelection(boolean select) {

        if (!select) {
            selectedProperty.set(false);
        }

        if (isSelectable()) {
            selectedProperty.set(select);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the selectableProperty
     */
    public BooleanProperty selectableProperty() {
        return selectableProperty;
    }

    public void setSelectable(Boolean selectable) {
        selectableProperty.set(selectable);
    }

    public boolean isSelectable() {
        return selectableProperty.get();
    }

    public DataObject getDataObject() {
        return object;
    }
}
