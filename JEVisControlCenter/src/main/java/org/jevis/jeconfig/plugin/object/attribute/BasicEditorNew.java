/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXTextField;
import java.text.NumberFormat;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

/**
 * This sample editor implements the primitiv types
 *
 * @author fs
 */
public class BasicEditorNew implements AttributeEditor {

    private final Logger logger = LogManager.getLogger(BasicEditorNew.class);
    private final JEVisAttribute attribute;

    public enum TYPE {
        DOUBLE, LONG, STRING, STRING_MULTY_LINE
    }

    public enum STATUS {
        OK, CHANGE, ERROR
    }
    private STATUS status;
    private final TYPE type;
    private JEVisSample orgSample;
    private boolean readonly = false;
    private boolean withUnit = false;
    private boolean withChart = false;
    private boolean withDelete = false;
    private boolean hasSample = false;
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(false);
//    private ValidationSupport validate = new ValidationSupport();
    private String newestValue = "";
    private final double height = 28;
    private final double maxwidth = GenericAttributeExtension.editorWhith.getValue();
    private final HBox editorNode = new HBox();
    private Object finalNewValue;

    /**
     *
     * @param att
     * @param type
     */
    public BasicEditorNew(JEVisAttribute att, TYPE type, boolean withUnit, boolean withChart, boolean withDelete) {
        logger.error("BasicEditor -=- Att: '{}' Type: '{}' unit: {} chart: {} delete: {} ", att, type, withUnit, withChart, withDelete);
        this.attribute = att;
        this.type = type;
//        this.withChart = withChart;
        this.withDelete = withDelete;
        this.withUnit = withUnit;

        orgSample = att.getLatestSample();

        update();

    }

    private void update() {
        editorNode.getChildren().removeAll(editorNode.getChildren());
        editorNode.getChildren().add(buildGui(attribute, type, withUnit, withChart, withDelete));
    }

    private Node buildGui(JEVisAttribute att, TYPE type, boolean withUnit, boolean withChart, boolean withDelete) {
        HBox hbox = new HBox();
        JFXTextField valueField = new JFXTextField();
//        valueField.setLabelFloat(true);
//        valueField.setPromptText(att.getName());

        valueField.setPrefWidth(maxwidth);

        //Textformate
        switch (type) {
            case DOUBLE:
                valueField.setAlignment(Pos.CENTER_RIGHT);
//                Decorator.addDecoration(valueField, new GraphicDecoration(deleteButton, Pos.CENTER_LEFT));
                break;
            case LONG:
                valueField.setAlignment(Pos.CENTER_RIGHT);
//                Decorator.addDecoration(valueField, new GraphicDecoration(deleteButton, Pos.CENTER_LEFT));//,10.0, 0.0
                break;
            default:
                valueField.setAlignment(Pos.CENTER_LEFT);
//                Decorator.addDecoration(valueField, new GraphicDecoration(deleteButton, Pos.CENTER_RIGHT));//,-10.0, 0.0
        }

        logger.error("sample: {}", orgSample);
        try {
            JEVisUnit unit = new JEVisUnitImp(Unit.ONE);
            if (att.getInputUnit() != null && att.getDisplayUnit() != null) {
                unit = att.getDisplayUnit();
            }

            if (orgSample != null) {
                switch (type) {
                    case DOUBLE:

                        valueField.setText(NumberFormat.getInstance().format(orgSample.getValueAsDouble(unit)));
                        break;
                    case LONG:
                        valueField.setText(NumberFormat.getInstance().format(orgSample.getValueAsLong(unit)));
                        break;
                    default:
                        logger.error("Default Type");
                        valueField.setText(orgSample.getValueAsString());
                        break;
                }
                Tooltip tt = new Tooltip("TimeStamp: " + orgSample.getTimestamp());
                tt.setOpacity(0.5);
                valueField.setTooltip(tt);
            } else {
//                valueField.setPromptText("[emty]");
            }

        } catch (Exception ex) {
            logger.catching(ex);
//            Decorator.addDecoration(valueField, new GraphicDecoration(new Label("Error loading value"), Pos.TOP_RIGHT));
        }
        valueField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                newValue = newValue.replace(',', '.');
//                newestValue = newValue;
//                Decorator.removeAllDecorations(valueField);

                if (newValue.isEmpty()) {
                    switch (type) {
                        case DOUBLE:
                            finalNewValue = null;
                            break;
                        case LONG:
                            finalNewValue = null;
                            break;
                        default:
                            finalNewValue = newValue;
                            break;
                    }

                    return;
                }

                if (!verify(newValue, type)) {
                    logger.error("Value NOK");
                    status = STATUS.ERROR;
                    String message = "";
                    switch (type) {
                        case DOUBLE:
                            message = "not an number";
                            break;
                        case LONG:
                            message = "is not an integer";
                            break;
                    }
                    Label messageL = new Label(message);
//                    Decorator.addDecoration(valueField, new GraphicDecoration(JEConfig.getImage("1404237042_Error.png", 12, 12)));
//                    Decorator.addDecoration(valueField, new StyleClassDecoration("-fx-background-color: #FF000055"));
//                    Decorator.addDecoration(valueField, new GraphicDecoration(messageL, Pos.TOP_RIGHT));

                    valueField.setText(oldValue);
//                    finalNewValue = null;

                } else {
                    logger.error("Value OK");
                    status = STATUS.OK;

                    try {
                        switch (type) {
                            case DOUBLE:
                                finalNewValue = Double.parseDouble(newValue);
                                break;
                            case LONG:
                                finalNewValue = Long.parseLong(newValue);
                                break;

                            default:
                                logger.error("--------------------------Default Case");
                                finalNewValue = newValue;
                                break;
                        }

                        if (orgSample == null || !newValue.equals(orgSample.getValueAsString())) {
                            changedProperty.setValue(true);
                        } else {
                            finalNewValue = null;
                            changedProperty.setValue(false);
                        }

                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                }

            }
        });

        hbox.getChildren().addAll(valueField);

        try {
            if (att.getInputUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                JFXTextField ubutton = new JFXTextField();
                ubutton.setPrefWidth(25);
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

//         hbox.getChildren().add(ubotton);
//         if(!withUnit){
//             ubotton.setDisable(true);//for the l&f
//         }
//        if (withUnit) {
//            hbox.getChildren().add(buildUnitButton());
//        }
        return hbox;
    }

//    private Button buildUnitButton() {
//        Button unitb = new Button();
//        JFXButton unitb = new JFXButton();
    JFXTextField unitb = new JFXTextField();

//        unitb.setPrefWidth(80);
//        unitb.setPrefHeight(height);
//        unitb.setStyle("-jfx-button-type: 0 10 10 0; -fx-base: rgba(75, 106, 139, 0.89);");
//        unitb.setAlignment(Pos.BOTTOM_LEFT);
//        unitb.setDisable(true);
//        try {
//            unitb.setText(attribute.getDisplayUnit().getLabel());
//        } catch (Exception ex) {
//            logger.catching(ex);
//        }
//        unitb.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//
//                AttributeSettingsDialog asd = new AttributeSettingsDialog();
//
//                try {
//                    if (asd.show(JEConfig.getStage(), attribute) == AttributeSettingsDialog.Response.YES) {
//                        asd.saveInDataSource();
//                        unitb.setText(attribute.getDisplayUnit().getLabel());
//
//                        //TODO convert value...
//                    }
//                } catch (JEVisException ex) {
//                    logger.catching(ex);
//                }
//            }
//        });
//        return unitb;
//    }
    @Override
    public boolean hasChanged() {
        return changedProperty.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.error("Commit() ");
        if (hasChanged()) {
            logger.error("Commit: " + finalNewValue);
            JEVisSample newSample = attribute.buildSample(new DateTime(), finalNewValue);
            newSample.commit();
        }

    }

    @Override
    public Node getEditor() {
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

    /**
     * Verify the value
     *
     * @param text
     * @param type
     * @return
     */
    private boolean verify(String text, TYPE type) {
        try {
            switch (type) {
                case DOUBLE:
                    Double.parseDouble(text);
                    return true;
                case LONG:
                    Long.parseLong(text);
                    return true;
                case STRING:
                    return true;
                case STRING_MULTY_LINE:
                    return true;

            }
        } catch (Exception ex) {
            logger.warn("Incorect value: {}", ex.getLocalizedMessage());
        }
        return false;
    }

}
