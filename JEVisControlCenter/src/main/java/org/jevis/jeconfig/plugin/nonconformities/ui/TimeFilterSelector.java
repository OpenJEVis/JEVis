package org.jevis.jeconfig.plugin.nonconformities.ui;

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
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityPlan;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.joda.time.DateTime;

import java.time.Month;
import java.time.format.TextStyle;

import static org.jevis.jeconfig.plugin.nonconformities.ui.DateFilter.DateField.*;


public class TimeFilterSelector extends GridPane {

    private static final Logger logger = LogManager.getLogger(TimeFilterSelector.class);


    //TODo locale name from Column
    JFXComboBox<DateFilter.DateField> fDateField = new JFXComboBox<>(FXCollections.observableArrayList(ALLES, UMSETZUNG, ABGESCHLOSSEN, ERSTELLT));
    JFXComboBox<Month> fFromMonth = generateMonthBox();
    JFXComboBox<Month> fToMonth = generateMonthBox();
    JFXComboBox<Integer> fFromYear = generateYearBox();
    JFXComboBox<Integer> fToYear = generateYearBox();
    private SimpleObjectProperty<org.jevis.jeconfig.plugin.nonconformities.ui.DateFilter> valueProperty = new SimpleObjectProperty<>(null);


    public TimeFilterSelector(NonconformityPlan nonconformityPlan) {
        super();

        setHgap(8);
        setVgap(8);

        Label lFrom = new Label("Start Datum");
        Label lTo = new Label("End Datum");
        Label lDatum = new Label("Zeitbereich");


        initValues(nonconformityPlan);
        NonconformityData fakeNames = new NonconformityData();

        Callback<ListView<DateFilter.DateField>, ListCell<DateFilter.DateField>> cellFactory = new Callback<ListView<DateFilter.DateField>, ListCell<DateFilter.DateField>>() {
            @Override
            public ListCell<DateFilter.DateField> call(ListView<DateFilter.DateField> param) {
                return new ListCell<DateFilter.DateField>() {
                    @Override
                    protected void updateItem(DateFilter.DateField item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            switch (item) {
                                case ALLES:
                                    setText(I18n.getInstance().getString("plugin.action.tfiler.all"));
                                    break;
                                case ABGESCHLOSSEN:
                                    setText(fakeNames.doneDateProperty().getName());
                                    break;
                                case UMSETZUNG:
                                    setText(fakeNames.deadLineProperty().getName());
                                    break;
                                case ERSTELLT:
                                    setText(fakeNames.createDateProperty().getName());
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
                lFrom.setDisable(newValue == ALLES);
                lTo.setDisable(newValue == ALLES);
                lDatum.setDisable(newValue == ALLES);
                fFromMonth.setDisable(newValue == ALLES);
                fToMonth.setDisable(newValue == ALLES);
                fToMonth.setDisable(newValue == ALLES);
                fToYear.setDisable(newValue == ALLES);
                fFromYear.setDisable(newValue == ALLES);

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

        //GridPane.setColumnSpan(fDateField, 3);
        //TimeFilterSelector.this.addRow(0, lDatum);
        TimeFilterSelector.this.addRow(0, fDateField, new Label("von"), fFromMonth, fFromYear, new Label("bis"), fToMonth, fToYear);

        /*
        this.addRow(0, lDatum, fDateField);
        this.addRow(1, lFrom, fFromMonth, fFromYear);
        this.addRow(2, lTo, fToMonth, fToYear);

         */
    }

    private void initValues(NonconformityPlan nonconformityPlan) {
        logger.debug("MonthSelector.initValues: {} {}",nonconformityPlan, nonconformityPlan.getActionData().size());
        DateTime minDate = null;
        DateTime maxDate = null;

        for (NonconformityData nonconformityData : nonconformityPlan.getActionData()) {
            if (minDate == null) {
                if (getDate(nonconformityData) != null) {
                    minDate = getDate(nonconformityData);
                    maxDate = getDate(nonconformityData);
                }

            } else {
                if (getDate(nonconformityData) != null && getDate(nonconformityData).isBefore(minDate)) {
                    minDate = getDate(nonconformityData);
                }

                if (getDate(nonconformityData) != null && getDate(nonconformityData).isAfter(maxDate)) {
                    maxDate = getDate(nonconformityData);
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

    private DateTime getDate(NonconformityData data) {
        DateFilter.DateField dateField = fDateField.getValue();
        if (dateField == ABGESCHLOSSEN) {
            return data.getDoneDate();
        } else if (dateField == DateFilter.DateField.ERSTELLT) {
            return data.getCreateDate();
        } else if (dateField == UMSETZUNG) {
            return data.getDeadLine();
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
            logger.error(ex);
        }
    }

    public SimpleObjectProperty<org.jevis.jeconfig.plugin.nonconformities.ui.DateFilter> getValuePropertyProperty() {
        return valueProperty;
    }

    private JFXComboBox<Integer> generateYearBox() {

        ObservableList<Integer> months = FXCollections.observableArrayList();
        months.add(2018);
        months.add(2019);
        months.add(2020);
        months.add(2021);
        months.add(2022);
        months.add(2023);
        months.add(2024);

        JFXComboBox<Integer> field = new JFXComboBox(months);
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
