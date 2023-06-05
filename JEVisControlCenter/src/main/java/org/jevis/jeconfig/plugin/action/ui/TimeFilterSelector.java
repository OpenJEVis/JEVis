package org.jevis.jeconfig.plugin.action.ui;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.joda.time.DateTime;

import java.time.Month;
import java.time.format.TextStyle;

import static org.jevis.jeconfig.plugin.action.ui.DateFilter.DateField.*;

public class TimeFilterSelector extends GridPane {


    //TODo locale name from Column
    MFXComboBox<DateFilter.DateField> fDateField = new MFXComboBox<>(FXCollections.observableArrayList(ALL, UMSETZUNG, ABGESCHLOSSEN, ERSTELLT));
    MFXComboBox<Month> fFromMonth = generateMonthBox();
    MFXComboBox<Month> fToMonth = generateMonthBox();
    MFXComboBox<Integer> fFromYear = generateYearBox();
    MFXComboBox<Integer> fToYear = generateYearBox();
    private SimpleObjectProperty<DateFilter> valueProperty = new SimpleObjectProperty<>(null);


    public TimeFilterSelector(ActionPlanData actionPlan) {
        super();

        setHgap(8);
        setVgap(8);

        Label lFrom = new Label("Start Datum");
        Label lTo = new Label("End Datum");
        Label lDatum = new Label("Zeitbereich");


        initValues(actionPlan);
        ActionData fakeNames = new ActionData();

        //TODO JFX71

        fDateField.setConverter(new StringConverter<DateFilter.DateField>() {
            @Override
            public String toString(DateFilter.DateField object) {
                String text = "";
                if (object != null) {
                    switch (object) {
                        case ALL:
                            text = I18n.getInstance().getString("plugin.action.tfiler.all");
                            break;
                        case ABGESCHLOSSEN:
                            text = fakeNames.doneDate.getName();
                            break;
                        case UMSETZUNG:
                            text = fakeNames.plannedDate.getName();
                            break;
                        case ERSTELLT:
                            text = fakeNames.createDate.getName();
                            break;
                        default:
                            break;
                    }
                }

                return text;
            }

            @Override
            public DateFilter.DateField fromString(String string) {
                return fDateField.getItems().get(fDateField.getSelectedIndex());
            }
        });


        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                /*
                lFrom.setDisable(newValue == ALLES);
                lTo.setDisable(newValue == ALLES);
                lDatum.setDisable(newValue == ALLES);
                fFromMonth.setDisable(newValue == ALLES);
                fToMonth.setDisable(newValue == ALLES);
                fToMonth.setDisable(newValue == ALLES);
                fToYear.setDisable(newValue == ALLES);
                fFromYear.setDisable(newValue == ALLES);

                 */

                updateValue();
            }
        };

        fDateField.valueProperty().addListener(changeListener);
        fFromMonth.valueProperty().addListener(changeListener);
        fToMonth.valueProperty().addListener(changeListener);
        fToMonth.valueProperty().addListener(changeListener);
        fToYear.valueProperty().addListener(changeListener);
        fFromYear.valueProperty().addListener(changeListener);
        fDateField.getSelectionModel().selectFirst();

        TimeFilterSelector.this.addRow(0, fDateField, new Label("von"), fFromMonth, fFromYear, new Label("bis"), fToMonth, fToYear);

    }

    private void initValues(ActionPlanData actionPlan) {
        //System.out.println("MonthSelector.initValues: " + actionPlan + "  " + actionPlan.getActionData().size());
        DateTime minDate = null;
        DateTime maxDate = null;

        for (ActionData actionData : actionPlan.getActionData()) {
            if (minDate == null) {
                if (getDate(actionData) != null) {
                    minDate = getDate(actionData);
                    maxDate = getDate(actionData);
                }

            } else {
                if (getDate(actionData) != null && getDate(actionData).isBefore(minDate)) {
                    minDate = getDate(actionData);
                }

                if (getDate(actionData) != null && getDate(actionData).isAfter(maxDate)) {
                    maxDate = getDate(actionData);
                }

            }
        }

        if (minDate != null) {
            fFromMonth.setValue(Month.of(minDate.getMonthOfYear()));
            fFromYear.setValue(minDate.getYear());

            fFromMonth.setValue(Month.of(maxDate.getMonthOfYear()));
            fToYear.setValue(maxDate.getYear());
        } else {
            fFromMonth.setValue(Month.of(1));
            fFromYear.setValue(2018);

            fToMonth.setValue(Month.of(12));
            fToYear.setValue(2024);
        }


    }

    private DateTime getDate(ActionData data) {
        DateFilter.DateField dateField = fDateField.getValue();
        if (dateField == ABGESCHLOSSEN) {
            return data.doneDate.get();
        } else if (dateField == ERSTELLT) {
            return data.createDate.get();
        } else if (dateField == UMSETZUNG) {
            return data.plannedDate.get();
        }
        return null;
    }

    public DateFilter getValueProperty() {
        return valueProperty.get();
    }

    private MFXComboBox<Month> generateMonthBox() {

        ObservableList<Month> months = FXCollections.observableArrayList();
        months.add(Month.JANUARY);
        months.add(Month.FEBRUARY);
        months.add(Month.MARCH);
        months.add(Month.APRIL);
        months.add(Month.MAY);
        months.add(Month.JUNE);
        months.add(Month.JULY);
        months.add(Month.AUGUST);
        months.add(Month.SEPTEMBER);
        months.add(Month.OCTOBER);
        months.add(Month.NOVEMBER);
        months.add(Month.DECEMBER);

        MFXComboBox<Month> field = new MFXComboBox(months);
        field.setConverter(new StringConverter<Month>() {
            @Override
            public String toString(Month object) {
                return object.getDisplayName(TextStyle.FULL, I18n.getInstance().getLocale());
            }

            @Override
            public Month fromString(String string) {
                return Month.valueOf(string);
            }
        });

        return field;
    }

    private void updateValue() {
        try {
            DateTime from = new DateTime(fFromYear.getValue(), fFromMonth.getValue().getValue(), 1, 0, 0);
            DateTime until = new DateTime(fToYear.getValue(), fToMonth.getValue().getValue(), fToMonth.getValue().maxLength(), 23, 59);

            DateFilter dateRange = new DateFilter(fDateField.getValue(), from, until);
            valueProperty.set(dateRange);
        } catch (Exception ex) {
            System.out.println("Unfinished Filter");
        }
    }

    public SimpleObjectProperty<DateFilter> getValuePropertyProperty() {
        return valueProperty;
    }

    private MFXComboBox<Integer> generateYearBox() {

        ObservableList<Integer> years = FXCollections.observableArrayList();
        years.add(2018);
        years.add(2019);
        years.add(2020);
        years.add(2021);
        years.add(2022);
        years.add(2023);
        years.add(2024);

        MFXComboBox<Integer> field = new MFXComboBox(years);
        field.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                return Integer.valueOf(string);
            }
        });

        return field;
    }

}
