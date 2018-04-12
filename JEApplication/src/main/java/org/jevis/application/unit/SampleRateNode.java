/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.unit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.joda.time.Period;

/**
 * @deprecated 
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleRateNode extends GridPane {

    private final Slider sliderMonth = new Slider();
    private final Slider sliderWeek = new Slider();
    private final Slider sliderHours = new Slider();
    private final Slider sliderMinutes = new Slider();
    private final Slider sliderSecounds = new Slider();

    private Period _returnPeriod;

    public SampleRateNode(Period period) {
        super();
        buildNode(period);
    }

    private void buildNode(Period period) {
//        System.out.println("new SampleRateNode: " + period.toString());
//        final Period newPeriod = period;

        sliderMonth.setMin(0);
        sliderMonth.setMax(12);
//        sliderMonth.setValue(period.getMonths());
        sliderMonth.setShowTickLabels(true);
        sliderMonth.setShowTickMarks(true);
        sliderMonth.setMajorTickUnit(6);
//        sliderMonth.setMinorTickCount(5);
        sliderMonth.setBlockIncrement(1);

        sliderWeek.setMin(0);
        sliderWeek.setMax(5);
//        sliderWeek.setValue(period.getWeeks());
        sliderWeek.setShowTickLabels(true);
        sliderWeek.setShowTickMarks(true);
        sliderWeek.setMajorTickUnit(1);
//        sliderWeek.setMinorTickCount(5);
        sliderWeek.setBlockIncrement(1);

        sliderHours.setMin(0);
        sliderHours.setMax(180);
//        sliderHours.setValue(period.getHours());
        sliderHours.setShowTickLabels(true);
        sliderHours.setShowTickMarks(true);
        sliderHours.setMajorTickUnit(15);
//        sliderHours.setMinorTickCount(5);
        sliderHours.setBlockIncrement(1);

        sliderMinutes.setMin(0);
        sliderMinutes.setMax(60);
//        sliderMinutes.setValue(period.getMinutes());
        sliderMinutes.setShowTickLabels(true);
        sliderMinutes.setShowTickMarks(true);
        sliderMinutes.setMajorTickUnit(15);
//        sliderMonth.setMinorTickCount(5);
        sliderMinutes.setBlockIncrement(1);

        sliderSecounds.setMin(0);
        sliderSecounds.setMax(60);
//        sliderSecounds.setValue(period.getSeconds());
        sliderSecounds.setShowTickLabels(true);
        sliderSecounds.setShowTickMarks(true);
        sliderSecounds.setMajorTickUnit(15);
//        sliderMonth.setMinorTickCount(5);
        sliderSecounds.setBlockIncrement(1);

        final Label monthlabel = new Label("Months:");
        final Label weekslabel = new Label("Weeks:");
        final Label hourslabel = new Label("Hours:");
        final Label minuteslabel = new Label("Minutes:");
        final Label secoundslabel = new Label("Secounds:");
        final Label periodLabel = new Label("Sample Rate:");
        final TextField sampleRate = new TextField();

//        Label enableLabel = new Label("Has fix sample rate:");
        final CheckBox enable = new CheckBox("Set fixed sample rate");

        setHgap(5);
        setVgap(5);
        setPadding(new Insets(10, 10, 10, 10));

        int i = 0;

        add(enable, 0, i, 2, 1);
        add(monthlabel, 0, ++i);
        add(weekslabel, 0, ++i);
        add(hourslabel, 0, ++i);
        add(minuteslabel, 0, ++i);
        add(secoundslabel, 0, ++i);
        add(periodLabel, 0, ++i);

        i = 0;
//        add(enable, 1, i);
        add(sliderMonth, 1, ++i);
        add(sliderWeek, 1, ++i);
        add(sliderHours, 1, ++i);
        add(sliderMinutes, 1, ++i);
        add(sliderSecounds, 1, ++i);
        add(sampleRate, 1, ++i);

        enable.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                System.out.println("actioN!!!!!!!!");
                sliderMonth.setDisable(!enable.isSelected());
                sliderWeek.setDisable(!enable.isSelected());
                sliderHours.setDisable(!enable.isSelected());
                sliderMinutes.setDisable(!enable.isSelected());
                sliderSecounds.setDisable(!enable.isSelected());
                sampleRate.setDisable(!enable.isSelected());
                monthlabel.setDisable(!enable.isSelected());
                weekslabel.setDisable(!enable.isSelected());
                hourslabel.setDisable(!enable.isSelected());
                minuteslabel.setDisable(!enable.isSelected());
                secoundslabel.setDisable(!enable.isSelected());
                periodLabel.setDisable(!enable.isSelected());
            }
        });

        if (period != null) {
            sliderMonth.setValue(period.getMonths());
            sliderWeek.setValue(period.getWeeks());
            sliderHours.setValue(period.getHours());
            sliderMinutes.setValue(period.getMinutes());
            sliderSecounds.setValue(period.getSeconds());
            sliderSecounds.setValue(period.getSeconds());
            sampleRate.setText(period.toString());

            if (period.equals(Period.ZERO)) {
                enable.setSelected(false);
            } else {
                enable.setSelected(true);
            }
        }

        ChangeListener<Number> sliderChanged = new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPeriod(sampleRate);
            }
        };

        sliderMinutes.valueProperty().addListener(sliderChanged);
        sliderSecounds.valueProperty().addListener(sliderChanged);
        sliderMonth.valueProperty().addListener(sliderChanged);
        sliderWeek.valueProperty().addListener(sliderChanged);
        sliderHours.valueProperty().addListener(sliderChanged);

        setPeriod(sampleRate);

    }

    private void setPeriod(TextField field) {
        Period newPeriod = Period.ZERO;

        if (sliderMinutes.getValue() != 0) {
            newPeriod = newPeriod.plusMinutes((int) sliderMinutes.getValue());
        }
        if (sliderSecounds.getValue() != 0) {
            newPeriod = newPeriod.plusSeconds((int) sliderSecounds.getValue());
        }
        if (sliderHours.getValue() != 0) {
            newPeriod = newPeriod.plusHours((int) sliderHours.getValue());
        }
        if (sliderMonth.getValue() != 0) {
            newPeriod = newPeriod.plusMonths((int) sliderMonth.getValue());
        }
        if (sliderWeek.getValue() != 0) {
            newPeriod = newPeriod.plusWeeks((int) sliderWeek.getValue());
        }

        field.setText(newPeriod.toString());
        _returnPeriod = newPeriod;
    }

    public Period getPeriod() {
        return _returnPeriod;
    }
}
