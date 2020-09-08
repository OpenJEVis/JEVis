/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.unit;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.Period;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SamplingRateUI extends ComboBox<Period> {

    private final Period FREE_SELECTION = Period.years(999);//Workaround, value to show the selection
    private final Period CANCELD_SELECTION = Period.years(998);//workaround, value if the new persio was cancled

    private final ObjectProperty<Period> periodProperty;

    public SamplingRateUI(Period period) {
        super();
        this.periodProperty = new SimpleObjectProperty<>(period);
//        this.period = period;

        this.getItems().add(Period.minutes(15));
        this.getItems().add(Period.minutes(1));
        this.getItems().add(Period.hours(1));
        this.getItems().add(Period.days(1));
        this.getItems().add(Period.months(1));
        this.getItems().add(Period.years(1));
        this.getItems().add(Period.ZERO);
        this.getItems().add(FREE_SELECTION);//free Section workaround

        Callback<ListView<Period>, ListCell<Period>> cellFactory = new Callback<ListView<Period>, ListCell<Period>>() {
            @Override
            public ListCell<Period> call(ListView<Period> param) {
                return new ListCell<Period>() {
                    @Override
                    protected void updateItem(Period item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(getLocalString(item));
                        }
                    }

                };
            }

        };
        this.setCellFactory(cellFactory);
        this.setButtonCell(cellFactory.call(null));

        if (!this.getItems().contains(period)) {
            this.getItems().add(period);
        }

        this.getSelectionModel().select(period);

//        this.valueProperty().bindBidirectional(periodProperty);
        this.valueProperty().addListener(new ChangeListener<Period>() {
            @Override
            public void changed(ObservableValue<? extends Period> observable, Period oldValue, Period newValue) {

                if (newValue == FREE_SELECTION) {
                    Period newperiod = ShowNewPeriod();
                    if (newperiod != CANCELD_SELECTION) {
                        periodProperty.setValue(newperiod);
                        SamplingRateUI.this.getItems().add(periodProperty.getValue());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                SamplingRateUI.this.getSelectionModel().select(periodProperty.getValue());
                            }
                        });

                    } else {
                        SamplingRateUI.this.getItems().add(periodProperty.getValue());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                SamplingRateUI.this.getSelectionModel().select(oldValue);
                            }
                        });

                    }

                } else {
                    periodProperty.setValue(newValue);
                }
            }
        });
    }

    public ObjectProperty<Period> samplingRateProperty() {
        return periodProperty;
    }

    private String getLocalString(Period period) {
        if (period == Period.ZERO) {
            return I18n.getInstance().getString("plugin.unit.samplingrate.async");
        }

        if (period.equals(FREE_SELECTION)) {
            return I18n.getInstance().getString("plugin.unit.samplingrate.selection");
        }

        String returnV = "[" + period + "] ";
        boolean isFirst = true;

        if (period.getYears() > 0) {
            if (Period.years(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.yearly");
            }
            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getYears() + " "
                        + I18n.getInstance().getString("plugin.unit.samplingrate.years");
                isFirst = false;
            }
        }

        if (period.getMonths() > 0) {
            if (Period.months(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.monthly");
            }

            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getMonths()
                        + I18n.getInstance().getString("plugin.unit.samplingrate.months");
                isFirst = false;
            } else {
                returnV += " " + I18n.getInstance().getString("plugin.unit.samplingrate.and") + " " + period.getMonths()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.months");
            }
        }

        if (period.getDays() > 0) {
            if (Period.days(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.daily");
            }
            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getDays() + " " +
                        I18n.getInstance().getString("plugin.unit.samplingrate.days");
                isFirst = false;
            } else {
                returnV += " " + I18n.getInstance().getString("plugin.unit.samplingrate.and") + " "
                        + period.getDays() + " " + I18n.getInstance().getString("plugin.unit.samplingrate.days");
            }
        }

        if (period.getHours() > 0) {
            if (Period.hours(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.hourly");
            }
            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getHours()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.hours");
                isFirst = false;
            } else {
                returnV += " " + I18n.getInstance().getString("plugin.unit.samplingrate.and") + " " + period.getHours()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.hours");
            }
        }

        if (period.getMinutes() > 0) {
            if (Period.minutes(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.everyminute");
            }
            if (Period.minutes(15).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.every15minutes");
            }
            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getMinutes()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.minutes");
                isFirst = false;
            } else {
                returnV += " " + I18n.getInstance().getString("plugin.unit.samplingrate.and") + " " + period.getMinutes()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.minutes");
            }
        }

        if (period.getSeconds() > 0) {
            if (Period.days(1).equals(period)) {
                return I18n.getInstance().getString("plugin.unit.samplingrate.everysecond");
            }
            if (isFirst) {
                returnV += I18n.getInstance().getString("plugin.unit.samplingrate.every") + " " + period.getSeconds() +
                        " " + I18n.getInstance().getString("plugin.unit.samplingrate.seconds");
                isFirst = false;
            } else {
                returnV += " " + I18n.getInstance().getString("plugin.unit.samplingrate.and") + " " + period.getSeconds()
                        + " " + I18n.getInstance().getString("plugin.unit.samplingrate.seconds");
            }
        }

        return returnV;

    }

    private Period ShowNewPeriod() {
        GridPane gp = new GridPane();
        //---------------------------------------------
        final Label l_monthlabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.months") + ":");
        final Label l_weekslabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.weeks") + ":");
        final Label l_hourslabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.hours") + ":");
        final Label l_minuteslabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.minutes") + ":");
        final Label l_secoundslabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.seconds") + ":");
        final Label l_periodLabel = new Label(I18n.getInstance().getString("plugin.unit.newperiod.iso") + ":");
        final Slider sliderMonth = new Slider();
        final Slider sliderWeek = new Slider();
        final Slider sliderHours = new Slider();
        final Slider sliderMinutes = new Slider();
        final Slider sliderSecounds = new Slider();
        final TextField fieldISOString = new TextField("");
        final ObjectProperty<Period> newPeriod = new SimpleObjectProperty<>(Period.minutes(15));

        sliderMonth.setMin(0);
        sliderMonth.setMax(12);
        sliderMonth.setShowTickLabels(true);
        sliderMonth.setShowTickMarks(true);
        sliderMonth.setMajorTickUnit(6);
        sliderMonth.setBlockIncrement(1);

        sliderWeek.setMin(0);
        sliderWeek.setMax(56);
        sliderWeek.setShowTickLabels(true);
        sliderWeek.setShowTickMarks(true);
        sliderWeek.setMajorTickUnit(1);
        sliderWeek.setBlockIncrement(1);

        sliderHours.setMin(0);
        sliderHours.setMax(180);
        sliderHours.setShowTickLabels(true);
        sliderHours.setShowTickMarks(true);
        sliderHours.setMajorTickUnit(15);
        sliderHours.setBlockIncrement(1);

        sliderMinutes.setMin(0);
        sliderMinutes.setMax(60);
        sliderMinutes.setShowTickLabels(true);
        sliderMinutes.setShowTickMarks(true);
        sliderMinutes.setMajorTickUnit(15);
        sliderMinutes.setBlockIncrement(1);

        sliderSecounds.setMin(0);
        sliderSecounds.setMax(60);
        sliderSecounds.setShowTickLabels(true);
        sliderSecounds.setShowTickMarks(true);
        sliderSecounds.setMajorTickUnit(15);
        sliderSecounds.setBlockIncrement(1);

        HBox buttonBar = new HBox();
        Region spaceBetween = new Region();
        Stage dia = new Stage();
        Button okButton = new Button(I18n.getInstance().getString("plugin.graph.dialog.new.ok").toUpperCase());
        Button calcelButton = new Button(I18n.getInstance().getString("plugin.graph.dialog.new.cancel").toUpperCase());
        okButton.setDefaultButton(true);
        okButton.setAlignment(Pos.BASELINE_RIGHT);
//        okButton.setButtonType(JFXButton.ButtonType.FLAT);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dia.close();
            }
        });
        calcelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newPeriod.setValue(CANCELD_SELECTION);
                dia.close();
            }
        });
        buttonBar.getChildren().addAll(calcelButton, spaceBetween, okButton);
        HBox.setHgrow(spaceBetween, Priority.ALWAYS);
        HBox.setHgrow(okButton, Priority.NEVER);

        //localize
//        Label enableLabel = new Label("Has fix sample rate:");
//        final CheckBox enable = new CheckBox("Set fixed sample rate");
        gp.setHgap(5);
        gp.setVgap(5);
        gp.setPadding(new Insets(10, 10, 10, 10));

        int row = 0;
        gp.add(l_monthlabel, 0, ++row);
        gp.add(l_weekslabel, 0, ++row);
        gp.add(l_hourslabel, 0, ++row);
        gp.add(l_minuteslabel, 0, ++row);
        gp.add(l_secoundslabel, 0, ++row);
        gp.add(l_periodLabel, 0, ++row);

        row = 0;

        gp.add(sliderMonth, 1, ++row);
        gp.add(sliderWeek, 1, ++row);
        gp.add(sliderHours, 1, ++row);
        gp.add(sliderMinutes, 1, ++row);
        gp.add(sliderSecounds, 1, ++row);
        gp.add(fieldISOString, 1, ++row);

        gp.add(buttonBar, 0, ++row, 2, 1);

        ChangeListener<Number> sliderChanged = new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Period p = newPeriod.getValue();
                p = p.withMinutes((int) sliderMinutes.getValue());
                p = p.withHours((int) sliderHours.getValue());
                p = p.withWeeks((int) sliderWeek.getValue());
                p = p.withSeconds((int) sliderSecounds.getValue());
                p = p.withMonths((int) sliderMonth.getValue());
                newPeriod.setValue(p);

            }
        };
        newPeriod.addListener(new ChangeListener<Period>() {
            @Override
            public void changed(ObservableValue<? extends Period> observable, Period oldValue, Period newValue) {
                fieldISOString.setText(newValue.toString());
            }
        });

        sliderMinutes.valueProperty()
                .addListener(sliderChanged);
        sliderSecounds.valueProperty()
                .addListener(sliderChanged);
        sliderMonth.valueProperty()
                .addListener(sliderChanged);
        sliderWeek.valueProperty()
                .addListener(sliderChanged);
        sliderHours.valueProperty()
                .addListener(sliderChanged);

        sliderMinutes.setValue(15);//default value

        dia.initOwner(JEConfig.getStage());
//        dia.setAlwaysOnTop(true);
        dia.setScene(new Scene(gp));
        dia.initStyle(StageStyle.UNDECORATED);
        dia.initModality(Modality.APPLICATION_MODAL);
        dia.sizeToScene();
        dia.showAndWait();
        return newPeriod.getValue();
    }

    public Period getPeriod() {
        return periodProperty.getValue();
    }
}
