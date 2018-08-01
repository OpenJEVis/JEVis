/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import javax.measure.unit.Unit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * This sample editor implements the primitiv types
 *
 * @author fs
 */
public abstract class BasicEditor implements AttributeEditor {

    private final Logger logger = LogManager.getLogger(BasicEditor.class);
    private final JEVisAttribute attribute;



    private JEVisSample orgSample;
    private boolean readonly = false;
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(false);

    private final double height = 28;
    private final double maxwidth = GenericAttributeExtension.editorWhith.getValue();
    private final HBox editorNode = new HBox();
    private Object finalNewValue;
    private BooleanProperty isValied= new SimpleBooleanProperty(false);

    /**
     * @param att
     */
    public BasicEditor(JEVisAttribute att) {
        this.attribute = att;
        orgSample = att.getLatestSample();
    }



    private void update() {
        editorNode.getChildren().removeAll(editorNode.getChildren());
        editorNode.getChildren().add(buildGui(attribute));
    }



    private Node buildGui(JEVisAttribute att) {
        HBox hbox = new HBox();
        JFXTextField valueField = new JFXTextField();

        valueField.getValidators().add(getValidator() );
        valueField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    try {
                        if(     (valueField.getText().isEmpty() && validateEmptyValue())
                                || (!valueField.getText().isEmpty( ) )
                                ){

                            if(valueField.validate() ){
                                isValied.setValue(true);
                                finalNewValue = parseValue(valueField.getText());
                                BasicEditor.this.changedProperty.setValue(true);
                            }else{
                                isValied.setValue(false);
                            }
                        }else{
                            isValied.setValue(true);
                        }

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });


        valueField.setPrefWidth(maxwidth);
        valueField.setAlignment(Pos.CENTER_RIGHT);

        try {
            if (orgSample != null) {
                valueField.setText(formatSample(orgSample));
                isValied.setValue(true);

                Tooltip tt = new Tooltip("TimeStamp: " + orgSample.getTimestamp());
                tt.setOpacity(0.5);
                valueField.setTooltip(tt);
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }

        hbox.getChildren().addAll(valueField);

        try {
            if (att.getInputUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                JFXTextField ubutton = new JFXTextField();
                ubutton.setPrefWidth(35);
                ubutton.setEditable(false);
                if (att.getDisplayUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                    ubutton.setText(attribute.getDisplayUnit().getLabel());
                } else {
                    ubutton.setText(attribute.getInputUnit().getLabel());
                }

                hbox.getChildren().add(ubutton);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return hbox;
    }

    @Override
    public boolean hasChanged() {
        return changedProperty.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.debug("Commit() ");
        if (hasChanged() && isValid()) {
            JEVisSample newSample = attribute.buildSample(new DateTime(), finalNewValue);
            newSample.commit();
        }
    }

    @Override
    public Node getEditor() {
        update();
        return editorNode;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return changedProperty;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        this.readonly = canRead;
        update();
    }

    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }


    public abstract ValidatorBase getValidator();

    public abstract String formatValue(Object value)throws ParseException,JEVisException;

    public abstract String formatSample(JEVisSample value)throws ParseException,JEVisException;

    public abstract Object parseValue(String value) throws ParseException;

    /**
     * Check if en empty string shall be validated
     * Workaround for the problem that null is not a valid numbers and we dont know how to handel this in the GUI or JEVis for now
     * @return
     */
    public abstract boolean validateEmptyValue();

    @Override
    public boolean isValid() {
        return isValied.getValue();
    }
}
