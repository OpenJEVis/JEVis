package org.jevis.jecc.plugin.meters.ui;

import com.jfoenix.controls.JFXTimePicker;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jecc.application.control.DataTypeBox;
import org.jevis.jecc.application.control.DayBox;
import org.jevis.jecc.application.control.MonthBox;
import org.jevis.jecc.application.control.YearBox;
import org.jevis.jecc.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jecc.plugin.meters.data.MeterData;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.time.YearMonth;


public class MeterFormReadingsTab extends Tab implements MeterFormTab {

    private static final Logger logger = LogManager.getLogger(MeterForm.class);

    private final MeterData meterData;
    private final JEVisDataSource ds;
    private final GridPane gridPane = new GridPane();
    private final DataTypeBox dataTypeBox = new DataTypeBox();
    private final YearBox yearBox = new YearBox(null);
    private final DayBox dayBox = new DayBox();
    private final MonthBox monthBox = new MonthBox();
    private final MFXDatePicker datePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final JFXTimePicker timePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));

    private final Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
    private final TargetHelper targetHelper;

    private JEVisSample oldMeterSample;
    private JEVisSample newMeterSample;

    public MeterFormReadingsTab(MeterData meterData, JEVisDataSource ds, String name) throws JEVisException {

        super(name);
        this.meterData = meterData;
        this.ds = ds;

        setContent(gridPane);


        JEVisTypeWrapper targetType = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_OnlineID));
        this.targetHelper = createTargetHelper(targetType);


        gridPane.addRow(0, dataTypeBox);

        setMeterValueTimeIntervall(dataTypeBox.getValue());
        buildReplaceMeterDialog();

        replacementGridPane(true);


    }

    private DateTime getDate(EnterDataTypes enterDataTypes) {
        DateTime ts = null;
        Integer year = yearBox.getSelectionModel().getSelectedItem();
        Integer month = monthBox.getSelectionModel().getSelectedIndex() + 1;
        Integer day = dayBox.getSelectionModel().getSelectedItem();

        switch (enterDataTypes) {
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
        return ts;
    }

    private void setMeterValueTimeIntervall(EnterDataTypes enterDataTypes) {
        switch (enterDataTypes) {
            case YEAR:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 1, 1, 3, 1));
                break;
            case MONTH:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 1, 1, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 2, 1, 1, 1));
                break;
            case DAY:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 1, 1, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 2, 1, 1, 1));
                Platform.runLater(() -> gridPane.add(dayBox, 3, 1, 1, 1));
                break;
            case SPECIFIC_DATETIME:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.addRow(1, dateLabel, datePicker, timePicker));
                break;
        }
    }

    private JEVisSample buildSample(double value, int offsetSecond, JEVisAttribute jeVisAttribute, String note) throws JEVisException {
        return jeVisAttribute.buildSample(getDate(dataTypeBox.getValue()).minusSeconds(offsetSecond), value, note);
    }

    private void replacementGridPane(boolean visible) {
        if (visible) {
            gridPane.setVisible(true);
        } else {
            gridPane.setVisible(false);
            oldMeterSample = null;
            newMeterSample = null;
        }
    }

    private JEVisType getJEVisType(String string) throws JEVisException {

        JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
        JEVisType jeVisType = jeVisClass.getType(string);
        return jeVisType;


    }

    private void buildReplaceMeterDialog() {

        gridPane.setPrefHeight(900);
        gridPane.setPrefWidth(800);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(30, 30, 30, 30));

        Label oldMeter_Label = new Label("Alter Zählerstand");

        Label newMeter_Label = new Label("Neuer Zählerstand");


        MFXTextField oldMeter_Value = new MFXTextField();

        MFXTextField newMeter_Value = new MFXTextField();


        // gridPane.add(gridPane, 0, rowcount, 4, 1);


        gridPane.addRow(4, oldMeter_Label, oldMeter_Value);
        gridPane.addRow(5, newMeter_Label, newMeter_Value);


        dataTypeBox.valueProperty().addListener((observableValue, enterDataTypes, t1) -> {
            setMeterValueTimeIntervall(t1);

        });

        newMeter_Value.textProperty().addListener((observableValue, s, t1) -> {

            try {
                Double doubleValue = Double.valueOf(t1);
                newMeterSample = buildSample(doubleValue, 0, targetHelper.getObject().get(0).getAttribute("Value"), "Meter Tausch");

            } catch (NumberFormatException numberFormatException) {
                logger.error(numberFormatException);
            } catch (JEVisException jeVisException) {
                logger.error(jeVisException);
            }


        });

        oldMeter_Value.textProperty().addListener((observableValue, s, t1) -> {
            try {
                Double doubleValue = Double.valueOf(t1);
                oldMeterSample = buildSample(doubleValue, 1, targetHelper.getObject().get(0).getAttribute("Value"), "Meter Tausch");

            } catch (NumberFormatException numberFormatException) {
                logger.error(numberFormatException);
            } catch (JEVisException jeVisException) {
                logger.error(jeVisException);
            }
        });

    }

    private int getMaxRow() {
        int rowcount = gridPane.getChildren().stream().mapToInt(value -> {
            Integer row = GridPane.getRowIndex(value);
            Integer rowSpan = GridPane.getRowSpan(value);
            return (row == null ? 0 : row) + (rowSpan == null ? 0 : rowSpan - 1);
        }).max().orElse(-1);
        return rowcount;
    }

    private TargetHelper createTargetHelper(JEVisTypeWrapper jeVisTypeWrapper) throws JEVisException {

        JEVisAttribute onlineIdAttribute = meterData.getJeVisObject().getAttribute(jeVisTypeWrapper.getJeVisType());
        TargetHelper targetHelper = new TargetHelper(ds, onlineIdAttribute);
        return targetHelper;

    }


    @Override
    public void commit() {
        try {
            if (newMeterSample != null && oldMeterSample != null) {
                newMeterSample.commit();
                oldMeterSample.commit();
            }
        } catch (JEVisException jeVisException) {
            logger.error(jeVisException);
        }

    }
}
