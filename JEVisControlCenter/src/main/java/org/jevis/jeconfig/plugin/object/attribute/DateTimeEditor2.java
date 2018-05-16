/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import java.time.LocalDateTime;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import jfxtras.scene.control.LocalDateTimeTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalField;
import java.util.Locale;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;
import javafx.util.converter.FormatStringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.commons.utils.JEVisDates;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Florian Simon
 */
public class DateTimeEditor2 implements AttributeEditor {

    private final JFXDatePicker pickerDate = new JFXDatePicker();
    private final JFXTimePicker pickerTime = new JFXTimePicker();
//    private final LocalDateTimeTextField picker = new LocalDateTimeTextField();
    private final HBox editor = new HBox();
    private final JEVisAttribute att;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    private JEVisDataSource ds;
    private final Logger logger = LogManager.getLogger(DateTimeEditor2.class);
    private JEVisSample originalSample;

    public DateTimeEditor2(JEVisAttribute att) {
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
                    pickerDate.valueProperty().get().getYear(), pickerDate.valueProperty().get().getMonthValue(), pickerDate.valueProperty().get().getDayOfMonth(), pickerTime.valueProperty().get().getHour(), pickerTime.valueProperty().get().getMinute(), pickerTime.valueProperty().get().getSecond(), DateTimeZone.getDefault()); // is this timezone correct?

            JEVisDates.saveDefaultDate(att, new DateTime(), datetime);
            logger.trace("commit.done: {}", att.getName());
        }

    }

    @Override
    public Node getEditor() {
        return editor;
    }

    private void buildGUI() {

        pickerDate.setPrefWidth(120d);
        pickerTime.setPrefWidth(110d);
//        pickerTime._24HourViewProperty().setValue(Boolean.TRUE);
        pickerTime.setIs24HourView(true);
        pickerTime.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));
//        pickerTime.setConverter(new StringConverter<LocalTime>() {
//            @Override
//            public String toString(LocalTime object) {
//                DateTimeFormatter dtf = new DateTimeFormatter().ofPattern("HH:mm");
//            }
//
//            @Override
//            public LocalTime fromString(String string) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//        });

        if (originalSample != null) {
            try {
                DateTime date = JEVisDates.parseDefaultDate(att);
                LocalDateTime lDate = LocalDateTime.of(
                        date.get(DateTimeFieldType.year()), date.get(DateTimeFieldType.monthOfYear()), date.get(DateTimeFieldType.dayOfMonth()), date.get(DateTimeFieldType.hourOfDay()), date.get(DateTimeFieldType.minuteOfHour()), date.get(DateTimeFieldType.secondOfMinute()));
                lDate.atZone(ZoneId.of(date.getZone().getID()));
                pickerDate.valueProperty().setValue(lDate.toLocalDate());
                pickerTime.valueProperty().setValue(lDate.toLocalTime());

            } catch (Exception ex) {
                logger.catching(ex);
            }

        }

        pickerDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                System.out.println("///// Value changed: " + newValue);
                if (!newValue.equals(oldValue)) {
                    _changed.setValue(Boolean.TRUE);
                }
            }
        });

        pickerTime.valueProperty().addListener(new ChangeListener<LocalTime>() {
            @Override
            public void changed(ObservableValue<? extends LocalTime> observable, LocalTime oldValue, LocalTime newValue) {
                if (!newValue.equals(oldValue)) {
                    _changed.setValue(Boolean.TRUE);
                }
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

}
