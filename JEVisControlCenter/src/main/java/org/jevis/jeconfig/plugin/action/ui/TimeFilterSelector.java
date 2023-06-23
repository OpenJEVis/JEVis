package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.joda.time.DateTime;

import java.time.Month;
import java.time.format.TextStyle;

import static org.jevis.jeconfig.plugin.action.ui.DateFilter.DateField.*;

public class TimeFilterSelector extends GridPane {

    private static final Logger logger = LogManager.getLogger(TimeFilterSelector.class);
    private final SimpleObjectProperty<DateFilter> valueProperty = new SimpleObjectProperty<>(null);
    //TODo locale name from Column
    JFXComboBox<DateFilter.DateField> fDateField = new JFXComboBox<>(FXCollections.observableArrayList(ALL, UMSETZUNG, ABGESCHLOSSEN, ERSTELLT));
    JFXComboBox<Month> fFromMonth = generateMonthBox();
    JFXComboBox<Month> fToMonth = generateMonthBox();
    JFXComboBox<Integer> fFromYear = generateYearBox();
    JFXComboBox<Integer> fToYear = generateYearBox();


    public TimeFilterSelector(ActionPlanData actionPlan) {
        super();

        setHgap(8);
        setVgap(8);

        initValues(actionPlan);
        ActionData fakeNames = new ActionData();

        Callback<ListView<DateFilter.DateField>, ListCell<DateFilter.DateField>> cellFactory = new Callback<ListView<DateFilter.DateField>, ListCell<DateFilter.DateField>>() {
            @Override
            public ListCell<DateFilter.DateField> call(ListView<DateFilter.DateField> param) {
                return new ListCell<DateFilter.DateField>() {
                    @Override
                    protected void updateItem(DateFilter.DateField item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            switch (item) {
                                case ALL:
                                    setText(I18n.getInstance().getString("plugin.action.tfiler.all"));
                                    break;
                                case ABGESCHLOSSEN:
                                    setText(fakeNames.doneDate.getName());
                                    break;
                                case UMSETZUNG:
                                    setText(fakeNames.plannedDate.getName());
                                    break;
                                case ERSTELLT:
                                    setText(fakeNames.createDate.getName());
                                    break;
                                default:
                                    setText("");
                            }
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };

        fDateField.setCellFactory(cellFactory);
        fDateField.setButtonCell(cellFactory.call(null));


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

        /*
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
         */
        fFromMonth.setValue(Month.of(1));
        fToMonth.setValue(Month.of(12));
        fFromYear.getSelectionModel().selectFirst();
        fToYear.getSelectionModel().selectLast();


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

    private JFXComboBox<Month> generateMonthBox() {

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

        JFXComboBox<Month> field = new JFXComboBox(months);
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
            logger.error("Unfinished Filter", ex);
        }
    }

    public SimpleObjectProperty<DateFilter> getValuePropertyProperty() {
        return valueProperty;
    }

    private JFXComboBox<Integer> generateYearBox() {

        ObservableList<Integer> years = FXCollections.observableArrayList();
        int year = 2010; //ISO5001 started 2010
        DateTime now = DateTime.now();
        while (year <= (now.getYear() + 10)) {
            years.add(year);
            year++;
        }
        JFXComboBox<Integer> field = new JFXComboBox(years);
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
