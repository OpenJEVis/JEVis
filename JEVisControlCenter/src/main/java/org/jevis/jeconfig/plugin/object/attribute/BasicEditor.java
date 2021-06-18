/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.control.AnalysisLinkButton;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.jevis.jeconfig.tool.FavUnitList;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.measure.quantity.Dimensionless;
import java.text.ParseException;

/**
 * This sample editor implements the primitiv types
 *
 * @author fs
 */
public abstract class BasicEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(BasicEditor.class);
    private final StackPane dialogContainer;
    private final JEVisAttribute attribute;
    private final BooleanProperty changedProperty = new SimpleBooleanProperty(false);
    private final double height = 28;
    private final double maxWidth = GenericAttributeExtension.editorWidth.getValue();
    private final HBox editorNode = new HBox();
    private final JEVisSample orgSample;
    private boolean readonly = false;
    private Object finalNewValue;
    private final BooleanProperty isValid = new SimpleBooleanProperty(false);

    /**
     * @param att
     */
    public BasicEditor(StackPane dialogContainer, JEVisAttribute att) {
        this.dialogContainer = dialogContainer;
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
        HBox hbox = new HBox(6);
        JFXTextField valueField = new JFXTextField();

        valueField.getValidators().add(getValidator());

        valueField.setPrefWidth(this.maxWidth);
        valueField.setAlignment(Pos.CENTER_RIGHT);

        try {
            if (this.orgSample != null) {
                valueField.setText(formatSample(this.orgSample));
                this.isValid.setValue(true);

                Tooltip tt = new Tooltip("TimeStamp: " + this.orgSample.getTimestamp().toString(DateTimeFormat.patternForStyle("MS", I18n.getInstance().getLocale())));
                tt.setOpacity(0.5);
                valueField.setTooltip(tt);
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }

        hbox.getChildren().addAll(valueField);
        try {
            JEVisUnit selectedUnit = new JEVisUnitImp(Dimensionless.UNIT, "", "");
            if (att.getDisplayUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                selectedUnit = this.attribute.getDisplayUnit();
            } else {
                selectedUnit = this.attribute.getInputUnit();
            }
            FavUnitList favUnitList = new FavUnitList(dialogContainer, this.attribute, selectedUnit, true);
            hbox.getChildren().add(favUnitList);
        } catch (Exception ex) {
            logger.error(ex, ex);
        }


        if (attribute.getName().equals("Value") || attribute.getName().equals("value")) {
            hbox.getChildren().add(new AnalysisLinkButton(att));
        }

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                try {
                    if ((valueField.getText().isEmpty() && validateEmptyValue())
                            || (!valueField.getText().isEmpty())) {

                        if (valueField.validate()) {
                            this.isValid.setValue(true);
                            this.finalNewValue = parseValue(valueField.getText());
                            this.changedProperty.setValue(true);
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

        return hbox;
    }


    public boolean isOtherUnit(JEVisUnit unit) {
        return unit.getLabel().equals("other");
    }

    private Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>> getUnitLIstFactory() {
        Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>> unitRenderer = new Callback<ListView<JEVisUnit>, ListCell<JEVisUnit>>() {
            @Override
            public ListCell<JEVisUnit> call(ListView<JEVisUnit> param) {
                return new ListCell<JEVisUnit>() {
                    {
                        //super.setMinWidth(260);
                    }

                    @Override
                    protected void updateItem(JEVisUnit unitItem, boolean empty) {
                        super.updateItem(unitItem, empty);
                        setGraphic(null);
                        if (!empty) {
                            System.out.println("Unit Text: " + unitItem);
                            setAlignment(Pos.CENTER);
                            setText("");

                            if (isOtherUnit(unitItem)) {
                                setText("Other...");
                            } else {
                                setText(UnitManager.getInstance().format(unitItem));
                            }
                        }
                    }
                };
            }
        };

        return unitRenderer;
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

            this.changedProperty.setValue(false);
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
     * Workaround for the problem that null is not a valid numbers and we dont know how to handle this in the GUI or JEVis for now
     *
     * @return
     */
    public abstract boolean validateEmptyValue();

    @Override
    public boolean isValid() {
        return this.isValid.getValue();
    }

}
