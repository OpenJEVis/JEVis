/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.text.ParseException;

/**
 * This sample editor implements the primitiv types
 *
 * @author fs
 */
public abstract class BasicEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(BasicEditor.class);
    private final JEVisAttribute attribute;
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(false);
    private final double height = 28;
    private final double maxWidth = GenericAttributeExtension.editorWidth.getValue();
    private final HBox editorNode = new HBox();
    private JEVisSample orgSample;
    private boolean readonly = false;
    private Object finalNewValue;
    private BooleanProperty isValid = new SimpleBooleanProperty(false);

    /**
     * @param att
     */
    public BasicEditor(JEVisAttribute att) {
        this.attribute = att;
        this.orgSample = att.getLatestSample();


    }


    @Override
    public void update() {
        logger.trace("Update()");
        Platform.runLater(() -> {
            this.editorNode.getChildren().clear();
            this.editorNode.getChildren().add(buildGui(this.attribute));
        });
    }


    private Node buildGui(JEVisAttribute att) {
        HBox hbox = new HBox();
        JFXTextField valueField = new JFXTextField();

        valueField.getValidators().add(getValidator());
        valueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    if ((valueField.getText().isEmpty() && validateEmptyValue())
                            || (!valueField.getText().isEmpty())
                    ) {

                        if (valueField.validate()) {
                            this.isValid.setValue(true);
                            this.finalNewValue = parseValue(valueField.getText());
                            BasicEditor.this.changedProperty.setValue(true);
                        } else {
                            this.isValid.setValue(false);
                        }
                    } else {
                        this.isValid.setValue(true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        valueField.setPrefWidth(this.maxWidth);
        valueField.setAlignment(Pos.CENTER_RIGHT);

        try {
            if (this.orgSample != null) {
                valueField.setText(formatSample(this.orgSample));
                this.isValid.setValue(true);

                Tooltip tt = new Tooltip("TimeStamp: " + this.orgSample.getTimestamp());
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
                ubutton.setPrefWidth(40);
                ubutton.setEditable(false);
                if (att.getDisplayUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                    ubutton.setText(UnitManager.getInstance().format(this.attribute.getDisplayUnit().getLabel()));
                } else {
                    ubutton.setText(UnitManager.getInstance().format(this.attribute.getInputUnit().getLabel()));
                }

                hbox.getChildren().add(ubutton);
            }
        } catch (Exception ex) {
            logger.error("Could not build unit field: " + ex);
        }

        if (att.getName().equals("Value") || att.getName().equals("value")) {
            try {
                DateHelper dateHelper = new DateHelper(DateHelper.TransformType.TODAY);
                WorkDays workDays = new WorkDays(att.getObject());
                dateHelper.setStartTime(workDays.getWorkdayStart());
                dateHelper.setEndTime(workDays.getWorkdayEnd());

                AnalysisRequest analysisRequest = new AnalysisRequest(att.getObject(),
                        AggregationPeriod.NONE,
                        ManipulationMode.NONE,
                        new AnalysisTimeFrame(TimeFrame.TODAY),
                        dateHelper.getStartDate(), dateHelper.getEndDate());
                analysisRequest.setAttribute(att);

                JFXButton button = new JFXButton("", JEConfig.getImage("1415314386_Graph.png", 20, 20));

                hbox.getChildren().add(button);

                button.setOnAction(event -> JEConfig.openObjectInPlugin(GraphPluginView.PLUGIN_NAME, analysisRequest));
            } catch (Exception e) {
                logger.error("Could not build analysis link button: " + e);
            }
        }

        return hbox;
    }

    @Override
    public boolean hasChanged() {
        return this.changedProperty.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.debug("Commit() ");
        if (hasChanged() && isValid()) {
            JEVisSample newSample = this.attribute.buildSample(new DateTime(), this.finalNewValue);
            newSample.commit();
        }
    }

    @Override
    public Node getEditor() {
        update();
        return this.editorNode;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return this.changedProperty;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        this.readonly = canRead;
//        updateData();
    }

    @Override
    public JEVisAttribute getAttribute() {
        return this.attribute;
    }


    public abstract ValidatorBase getValidator();

    public abstract String formatValue(Object value) throws ParseException, JEVisException;

    public abstract String formatSample(JEVisSample value) throws ParseException, JEVisException;

    public abstract Object parseValue(String value) throws ParseException;

    /**
     * Check if en empty string shall be validated
     * Workaround for the problem that null is not a valid numbers and we dont know how to handel this in the GUI or JEVis for now
     *
     * @return
     */
    public abstract boolean validateEmptyValue();

    @Override
    public boolean isValid() {
        return this.isValid.getValue();
    }
}
