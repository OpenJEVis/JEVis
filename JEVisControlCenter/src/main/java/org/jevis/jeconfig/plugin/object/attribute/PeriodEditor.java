/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.Collections;

/**
 * @author Florian Simon
 */
public class PeriodEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(PeriodEditor.class);
    private final JFXDatePicker pickerDate = new JFXDatePicker();
    private final JFXTimePicker pickerTime = new JFXTimePicker();
    private final HBox editor = new HBox(4);
    private final JEVisAttribute att;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final JEVisSample originalSample;
    private SamplingRateUI samplingRateUI;
    private JEVisDataSource ds;
    private final SimpleBooleanProperty showTs = new SimpleBooleanProperty(true);

    public PeriodEditor(JEVisAttribute att) {
        this.att = att;
        originalSample = att.getLatestSample();
        buildGUI();
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.error("commit: {}", att.getName());
        if (hasChanged()) {
            DateTime datetime = new DateTime(
                    pickerDate.valueProperty().get().getYear(),
                    pickerDate.valueProperty().get().getMonthValue(),
                    pickerDate.valueProperty().get().getDayOfMonth(),
                    pickerTime.valueProperty().get().getHour(),
                    pickerTime.valueProperty().get().getMinute(),
                    pickerTime.valueProperty().get().getSecond(),
                    DateTimeZone.getDefault()); // is this timezone correct?

            if (originalSample != null && originalSample.getTimestamp().equals(datetime)) {
                att.deleteSamplesBetween(datetime, datetime);
            }
            JEVisSample newSample = att.buildSample(datetime, samplingRateUI.getPeriod());
            att.addSamples(Collections.singletonList(newSample));

            logger.trace("commit.done: {}", att.getName());
        }

    }

    @Override
    public Node getEditor() {
        return editor;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            editor.getChildren().clear();
            buildGUI();
        });
    }

    private void buildGUI() {
        Period p = Period.minutes(15);
        try {
            if (originalSample != null) {
                p = new Period(originalSample.getValueAsString());
            } else if (att.getInputSampleRate() != null) {
                JEVisObject object = att.getObject();
                JEVisAttribute value = object.getAttribute("Value");
                p = value.getInputSampleRate();
            }
        } catch (Exception e) {
            logger.error("Could not get period from sample", e);
        }
        samplingRateUI = new SamplingRateUI(p);

        pickerDate.setPrefWidth(120d);
        pickerTime.setPrefWidth(110d);
        pickerTime.set24HourView(true);
        pickerTime.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        if (originalSample != null) {
            try {
                DateTime date = originalSample.getTimestamp();
                LocalDateTime lDate = LocalDateTime.of(
                        date.get(DateTimeFieldType.year()), date.get(DateTimeFieldType.monthOfYear()), date.get(DateTimeFieldType.dayOfMonth()), date.get(DateTimeFieldType.hourOfDay()), date.get(DateTimeFieldType.minuteOfHour()), date.get(DateTimeFieldType.secondOfMinute()));
                lDate.atZone(ZoneId.of(date.getZone().getID()));
                pickerDate.valueProperty().setValue(lDate.toLocalDate());
                pickerTime.valueProperty().setValue(lDate.toLocalTime());

            } catch (Exception ex) {
                logger.catching(ex);
            }
        } else {
            pickerDate.valueProperty().setValue(LocalDate.of(2001, 1, 1));
            pickerTime.valueProperty().setValue(LocalTime.of(0, 0, 0));
            try {
                logger.info("Setting initial value {} {} : {}", pickerDate.getValue().toString(), pickerTime.getValue().toString(), p);
                _changed.set(true);
                commit();
            } catch (JEVisException e) {
                logger.error("Could not commit initial value", e);
            }
        }

        Label validFrom = new Label(I18n.getInstance().getString("plugin.object.attribute.periodeditor.validfrom"));
        validFrom.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(validFrom);
        vBox.setAlignment(Pos.CENTER);

        editor.getChildren().setAll(samplingRateUI, vBox, pickerDate, pickerTime);

        pickerDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("date changed: " + newValue);
            if (!newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        pickerTime.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("time changed: " + newValue);
            if (!newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        samplingRateUI.samplingRateProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("period changed: " + newValue);
            if (!newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        showTs.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> editor.getChildren().setAll(samplingRateUI, validFrom, pickerDate, pickerTime));
            } else {
                Platform.runLater(() -> editor.getChildren().setAll(samplingRateUI));
            }
        });
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        editor.setDisable(canRead);
    }

    @Override
    public JEVisAttribute getAttribute() {
        return att;
    }


    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }

    /**
     * Convert {@link java.time.LocalDate} to {@link org.joda.time.DateTime}
     */
    public DateTime toDateTime(LocalDate localDate) {
        return new DateTime(DateTimeZone.UTC).withDate(
                localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
        ).withTime(0, 0, 0, 0);
    }

    /**
     * Convert {@link org.joda.time.DateTime} to {@link java.time.LocalDate}
     */
    public LocalDate toLocalDate(DateTime dateTime) {
        DateTime dateTimeUtc = dateTime.withZone(DateTimeZone.UTC);
        return LocalDate.of(dateTimeUtc.getYear(), dateTimeUtc.getMonthOfYear(), dateTimeUtc.getDayOfMonth());
    }

    public void showTs(boolean showTs) {
        this.showTs.set(showTs);
    }
}
