package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.DataTypeBox;
import org.jevis.jeconfig.application.control.DayBox;
import org.jevis.jeconfig.application.control.MonthBox;
import org.jevis.jeconfig.application.control.YearBox;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.plugin.object.attribute.AttributeEditor;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class MeterDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(MeterDialog.class);
    private final JEVisClass jeVisClass;
    private final List<JEVisClass> possibleParents = new ArrayList<>();
    private final StackPane dialogContainer;
    private final JEVisDataSource ds;
    private Response response;
    private GridPane gp;
    private JEVisObject newObject;
    private final List<AttributeEditor> attributeEditors = new ArrayList<>();
    private String name;
    private final DataTypeBox dataTypeBox = new DataTypeBox();
    private final YearBox yearBox = new YearBox(null);
    private final DayBox dayBox = new DayBox();
    private final MonthBox monthBox = new MonthBox();
    private final JFXDatePicker datePicker = new JFXDatePicker(LocalDate.now());
    private final JFXTimePicker timePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));
    private final Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
    private final Label oldCounterValueLabel = new Label(I18n.getInstance().getString("plugin.meters.meterdialog.oldcountervalue"));
    private final Label newCounterValueLabel = new Label(I18n.getInstance().getString("plugin.meters.meterdialog.newcountervalue"));
    private final JFXTextField oldCounterValue = new JFXTextField();
    private final JFXTextField newCounterValue = new JFXTextField();
    private final JFXCheckBox enterCounterValues = new JFXCheckBox(I18n.getInstance().getString("plugin.meters.meterdialog.entercountervalues"));
    private final GridPane innerGridPane = new GridPane();
    private int row;
    private int column;

    public MeterDialog(StackPane dialogContainer, JEVisDataSource ds, JEVisClass jeVisClass) {
        super();
        this.dialogContainer = dialogContainer;
        this.ds = ds;
        this.jeVisClass = jeVisClass;
        setDialogContainer(dialogContainer);

        dataTypeBox.getSelectionModel().select(EnterDataTypes.DAY);
        monthBox.setRelations(yearBox, dayBox, null);
        yearBox.setRelations(monthBox, dayBox);

        timePicker.set24HourView(true);
        timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        int innerRow = 0;
        innerGridPane.addRow(innerRow, dataTypeBox);
        innerRow++;
        innerRow++;

        innerGridPane.addRow(innerRow, oldCounterValueLabel, oldCounterValue);
        innerRow++;

        innerGridPane.addRow(innerRow, newCounterValueLabel, newCounterValue);

        dataTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                filterGridPane();
            }
        });

        enterCounterValues.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                filterGridPane();
            }
        });

        try {
            for (JEVisClass aClass : ds.getJEVisClasses()) {
                List<JEVisClassRelationship> relationships = aClass.getRelationships(3);

                for (JEVisClassRelationship jeVisClassRelationship : relationships) {
                    if (jeVisClassRelationship.getStart() != null && jeVisClassRelationship.getEnd() != null) {
                        if (jeVisClassRelationship.getStart() != null && jeVisClassRelationship.getEnd() != null) {
                            if (jeVisClassRelationship.getStart().equals(jeVisClass) && jeVisClassRelationship.getEnd().equals(aClass)) {
                                possibleParents.add(aClass);
                            }
                        }
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        possibleParents.remove(jeVisClass);
        response = Response.CANCEL;

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);

        Label parentLabel = new Label(I18n.getInstance().getString("jevis.types.parent"));
        VBox parentVBox = new VBox(parentLabel);
        parentVBox.setAlignment(Pos.CENTER);

        JFXButton treeButton = new JFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        JFXTextField nameField = new JFXTextField();

        Region targetSpace = new Region();
        targetSpace.setPrefWidth(20);

        HBox targetBox = new HBox(parentVBox, treeButton, targetSpace, nameVBox, nameField);
        targetBox.setSpacing(4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        JFXButton ok = new JFXButton(I18n.getInstance().getString("jevistree.dialog.new.ok"));
        HBox.setHgrow(ok, Priority.NEVER);
        JFXButton cancel = new JFXButton(I18n.getInstance().getString("jevistree.dialog.new.cancel"));
        HBox.setHgrow(cancel, Priority.NEVER);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        HBox buttonRow = new HBox(spacer, cancel, ok);
        buttonRow.setPadding(new Insets(4));
        buttonRow.setSpacing(10);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonRow, Priority.NEVER);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("measurement_instrument.png", I18n.getInstance().getString("plugin.meters.title")), targetBox, gp, sep1, buttonRow);

        treeButton.setOnAction(event -> {
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allCurrentClassFilter = SelectTargetDialog.buildMultiClassFilter(jeVisClass, possibleParents);
            allFilter.add(allCurrentClassFilter);

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(dialogContainer, allFilter, allCurrentClassFilter, null, SelectionMode.SINGLE, ds, null);

            List<UserSelection> openList = new ArrayList<>();

            selectTargetDialog.setOnDialogClosed(event1 -> {
                if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                    logger.trace("Selection Done");

                    List<UserSelection> selections = selectTargetDialog.getUserSelection();
                    for (UserSelection us : selections) {
                        try {
                            newObject = us.getSelectedObject().buildObject(I18n.getInstance().getString("newobject.new.title"), jeVisClass);
                            newObject.commit();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    treeButton.setText(newObject.getName());
                    nameField.setText(newObject.getName());

                    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.equals(oldValue)) {
                            name = newValue;
                        }
                    });

                    updateGrid(false);
                }
            });
        });

        ok.setOnAction(event -> {
            for (AttributeEditor attributeEditor : attributeEditors) {
                if (attributeEditor.hasChanged()) {
                    try {
                        attributeEditor.commit();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                newObject.setName(name);
                newObject.commit();
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            response = Response.OK;
            close();
        });

        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            if (newObject != null) {
                try {
                    ds.deleteObject(newObject.getID());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
            close();
        });

        setContent(vBox);
    }

    public void showReplaceWindow(JEVisObject selectedMeter) {
        response = Response.CANCEL;

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(12));
        vBox.setSpacing(4);

        gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        JFXTextField nameField = new JFXTextField(selectedMeter.getName());

        HBox targetBox = new HBox(nameVBox, nameField);
        targetBox.setSpacing(4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        JFXButton ok = new JFXButton(I18n.getInstance().getString("jevistree.dialog.new.ok"));
        HBox.setHgrow(ok, Priority.NEVER);
        JFXButton cancel = new JFXButton(I18n.getInstance().getString("jevistree.dialog.new.cancel"));
        HBox.setHgrow(cancel, Priority.NEVER);

        Separator sep1 = new Separator(Orientation.HORIZONTAL);

        HBox buttonRow = new HBox(spacer, cancel, ok);
        buttonRow.setPadding(new Insets(4));
        buttonRow.setSpacing(10);

        VBox.setVgrow(targetBox, Priority.NEVER);
        VBox.setVgrow(gp, Priority.ALWAYS);
        VBox.setVgrow(buttonRow, Priority.NEVER);
        vBox.setFillWidth(true);
        vBox.getChildren().setAll(DialogHeader.getDialogHeader("measurement_instrument.png", I18n.getInstance().getString("plugin.meters.title")), targetBox, gp, sep1, buttonRow);

        newObject = selectedMeter;
        updateGrid(true);

        ok.setOnAction(event -> {
            for (AttributeEditor attributeEditor : attributeEditors) {
                if (attributeEditor.hasChanged()) {
                    try {
                        attributeEditor.commit();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                newObject.setName(name);
                newObject.commit();
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            if (enterCounterValues.isSelected()) {
                DateTime ts = null;

                Integer year = yearBox.getSelectionModel().getSelectedItem();
                Integer month = monthBox.getSelectionModel().getSelectedIndex() + 1;
                Integer day = dayBox.getSelectionModel().getSelectedItem();
                switch (dataTypeBox.getSelectionModel().getSelectedItem()) {
                    case YEAR:
                        ts = new DateTime(year,
                                1,
                                1,
                                0, 0, 0);
                        break;
                    case MONTH:
                        ts = new DateTime(year,
                                month,
                                1,
                                0, 0, 0);
                        break;
                    case DAY:
                        ts = new DateTime(year,
                                month,
                                day,
                                0, 0, 0);
                        break;
                    case SPECIFIC_DATETIME:
                        ts = new DateTime(
                                datePicker.valueProperty().get().getYear(),
                                datePicker.valueProperty().get().getMonthValue(),
                                datePicker.valueProperty().get().getDayOfMonth(),
                                timePicker.valueProperty().get().getHour(),
                                timePicker.valueProperty().get().getMinute(),
                                timePicker.valueProperty().get().getSecond());
                        break;
                }

                JEVisAttribute targetAttribute = null;
                for (AttributeEditor attributeEditor : attributeEditors) {
                    try {
                        if (attributeEditor.getAttribute().getType().getName().equals("Online ID")) {
                            targetAttribute = attributeEditor.getAttribute();
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }

                if (targetAttribute != null) {
                    try {
                        TargetHelper th = new TargetHelper(ds, targetAttribute);

                        JEVisObject jeVisObject = th.getObject().get(0);
                        if (jeVisObject != null) {
                            JEVisAttribute valueAttribute = jeVisObject.getAttribute("Value");
                            if (valueAttribute != null) {
                                DoubleValidator validator = new DoubleValidator();
                                Double newVal = validator.validate(newCounterValue.getText(), I18n.getInstance().getLocale());
                                Double oldVal = validator.validate(oldCounterValue.getText(), I18n.getInstance().getLocale());

                                if (newVal != null && oldVal != null) {
                                    List<JEVisSample> newSamples = new ArrayList<>();
                                    JEVisSample oldCounterValue = valueAttribute.buildSample(ts.minusSeconds(1), oldVal, NoteConstants.Differential.COUNTER_CHANGE + "," + I18n.getInstance().getString("menu.file.import.manual") + " " + DateTime.now());
                                    newSamples.add(oldCounterValue);
                                    JEVisSample newCounterValue = valueAttribute.buildSample(ts, newVal, I18n.getInstance().getString("menu.file.import.manual") + " " + DateTime.now());
                                    newSamples.add(newCounterValue);

                                    valueAttribute.addSamples(newSamples);
                                }
                            }
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            response = Response.OK;
            close();
        });

        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            close();
        });

        setContent(vBox);
        show();
    }

    private void updateGrid(boolean showCounterValues) {
        if (newObject != null) {
            gp.getChildren().clear();
            attributeEditors.clear();
            try {
                column = 0;
                row = 0;
                boolean isLinked = false;
                List<JEVisAttribute> attributes = newObject.getAttributes();

                for (JEVisAttribute attribute : attributes) {
                    int index = attributes.indexOf(attribute);
                    if (attribute.getName().equals("Online ID")) {
                        if (attribute.hasSample()) {
                            JEVisSample latestSample = attribute.getLatestSample();
                            if (latestSample != null) {
                                TargetHelper th = new TargetHelper(ds, latestSample.getValueAsString());
                                if (th.isValid() && th.targetAccessible()) {
                                    isLinked = true;
                                }
                            }
                        }
                    }

                    if (index == 2 || (index > 2 && index % 2 == 0)) {
                        column = 0;
                        row++;
                    }

                    Label typeName = new Label(I18nWS.getInstance().getTypeName(attribute.getType()));
                    VBox typeBox = new VBox(typeName);
                    typeBox.setAlignment(Pos.CENTER);

                    AttributeEditor attributeEditor = GenericAttributeExtension.getEditor(dialogContainer, attribute.getType(), attribute);
                    attributeEditor.setReadOnly(false);
                    attributeEditors.add(attributeEditor);
                    VBox editorBox = new VBox(attributeEditor.getEditor());
                    editorBox.setAlignment(Pos.CENTER);

                    if (column < 2) {
                        gp.add(typeBox, column, row);
                    } else {
                        gp.add(typeBox, column + 1, row);
                    }
                    column++;

                    if (column < 2) {
                        gp.add(editorBox, column, row);
                    } else {
                        gp.add(editorBox, column + 1, row);
                    }
                    column++;
                }

                Separator separator = new Separator(Orientation.VERTICAL);
                gp.add(separator, 2, 0, 1, row + 1);

                row++;

                gp.add(enterCounterValues, 0, row, column, 1);
                row++;

                filterGridPane();

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
    }

    private void filterGridPane() {
        if (enterCounterValues.isSelected()) {
            Platform.runLater(() -> gp.add(innerGridPane, 0, row, column, 1));
        } else {
            Platform.runLater(() -> gp.getChildren().remove(innerGridPane));
        }

        switch (dataTypeBox.getSelectionModel().getSelectedItem()) {
            case YEAR:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 0, 1, 3, 1));
                break;
            case MONTH:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 0, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(monthBox, 1, 1, 1, 1));
                break;
            case DAY:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 0, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(monthBox, 1, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(dayBox, 2, 1, 1, 1));
                break;
            case SPECIFIC_DATETIME:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.addRow(1, dateLabel, datePicker, timePicker));
                break;
        }
    }

    public Response getResponse() {
        return response;
    }
}
