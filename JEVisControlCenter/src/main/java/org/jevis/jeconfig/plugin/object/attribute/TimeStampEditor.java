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
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;

/**
 * @author Florian Simon
 */
public class TimeStampEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(TimeStampEditor.class);
    private final JFXDatePicker pickerDate = new JFXDatePicker();
    private final JFXTimePicker pickerTime = new JFXTimePicker();
    //    private final LocalDateTimeTextField picker = new LocalDateTimeTextField();
    private final HBox editor = new HBox();
    private final JEVisAttribute att;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisDataSource ds;
    private JEVisSample originalSample;
    private DateTime originalDateTime;

    public TimeStampEditor(JEVisAttribute att) {
        this.att = att;
        originalSample = att.getLatestSample();
        buildGUI();
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    public DateTime getDateTime() {
        return new DateTime(
                pickerDate.valueProperty().get().getYear(), pickerDate.valueProperty().get().getMonthValue(), pickerDate.valueProperty().get().getDayOfMonth(),
                pickerTime.valueProperty().get().getHour(), pickerTime.valueProperty().get().getMinute(), pickerTime.valueProperty().get().getSecond());
    }

    @Override
    public void commit() throws JEVisException {
//        logger.error("commit: {}", att.getName());
//        if (hasChanged()) {
//            DateTime datetime = new DateTime(
//                    pickerDate.valueProperty().get().getYear(), pickerDate.valueProperty().get().getMonthValue(), pickerDate.valueProperty().get().getDayOfMonth(),
//                    pickerTime.valueProperty().get().getHour(), pickerTime.valueProperty().get().getMinute(), pickerTime.valueProperty().get().getSecond());
//
//
//            JEVisSample newSample = new VirtualSample(datetime, originalSample.getValueAsDouble(), originalSample.getUnit());
//            List<JEVisSample> jeVisSampleList = new ArrayList<>(Collections.singletonList(newSample));
//            newSample.setNote(originalSample.getNote());
//
//            att.deleteSamplesBetween(originalSample.getTimestamp().minusMillis(1), originalSample.getTimestamp().plusMillis(1));
//            att.addSamples(jeVisSampleList);
//
//            logger.trace("commit.done: {}", att.getName());
//        }

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

        pickerDate.setPrefWidth(120d);
        pickerTime.setPrefWidth(110d);
        pickerTime.set24HourView(true);
        pickerTime.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        if (originalSample != null) {
            try {
                DateTime date = originalSample.getTimestamp();
                originalDateTime = date;
                LocalDateTime lDate = LocalDateTime.of(
                        date.get(DateTimeFieldType.year()), date.get(DateTimeFieldType.monthOfYear()), date.get(DateTimeFieldType.dayOfMonth()),
                        date.get(DateTimeFieldType.hourOfDay()), date.get(DateTimeFieldType.minuteOfHour()), date.get(DateTimeFieldType.secondOfMinute()));
                lDate.atZone(ZoneId.of(date.getZone().getID()));
                pickerDate.valueProperty().setValue(lDate.toLocalDate());
                pickerTime.valueProperty().setValue(lDate.toLocalTime());

            } catch (Exception ex) {
                logger.catching(ex);
            }

        }

        pickerDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("///// Value changed: " + newValue);
            if (!newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        pickerTime.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        editor.getChildren().addAll(pickerDate, pickerTime);
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

    public DateTime getOriginalDateTime() {
        return originalDateTime;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
