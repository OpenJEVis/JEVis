/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;

/**
 *
 * @author fs
 */
public class ErrorEditor implements AttributeEditor {

    private BooleanProperty ValueChangedProperty = new SimpleBooleanProperty(false);
    private Label label = new Label();

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public void commit() throws JEVisException {
        ;
    }

    @Override
    public Node getEditor() {
        label.setText("Error while loading values");
        return label;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return ValueChangedProperty;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        ;
    }

    public void setException(Exception ex) {
        label.setText("Error: " + ex.toString());
    }

    @Override
    public JEVisAttribute getAttribute() {
        return null;
    }

}
