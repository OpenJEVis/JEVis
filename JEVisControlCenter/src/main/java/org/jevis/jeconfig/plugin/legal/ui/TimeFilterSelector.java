package org.jevis.jeconfig.plugin.legal.ui;

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
import org.jevis.jeconfig.plugin.legal.data.LegalCadastre;
import org.jevis.jeconfig.plugin.legal.data.LegislationData;
import org.joda.time.DateTime;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static org.jevis.jeconfig.plugin.legal.ui.DateFilter.DateField.*;


public class TimeFilterSelector extends GridPane {

    private static final Logger logger = LogManager.getLogger(TimeFilterSelector.class);


    //TODo locale name from Column
    JFXComboBox<DateFilter.DateField> fDateField = new JFXComboBox<>(FXCollections.observableArrayList(ALL, ISSUE_DATE, Date_Of_Examination, VERSION));
    JFXComboBox<Month> fFromMonth = generateMonthBox();
    JFXComboBox<Month> fToMonth = generateMonthBox();
    JFXComboBox<Integer> fFromYear = generateYearBox();
    JFXComboBox<Integer> fToYear = generateYearBox();
    private SimpleObjectProperty<DateFilter> valueProperty = new SimpleObjectProperty<>(null);


    public TimeFilterSelector(LegalCadastre legalCadastre) {
        super();

        setHgap(8);
        setVgap(8);

        Label lFrom = new Label("Start Datum");
        Label lTo = new Label("End Datum");
        Label lDatum = new Label("Zeitbereich");


        initValues(legalCadastre);
        LegislationData fakeNames = new LegislationData();

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
                                case VERSION:
                                    setText(fakeNames.currentVersionDateProperty().getName());
                                    break;
                                case ISSUE_DATE:
                                    setText(fakeNames.issueDateProperty().getName());
                                    break;
                                case Date_Of_Examination:
                                    setText(fakeNames.dateOfExaminationProperty().getName());
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

    private void initValues(LegalCadastre legalCadastre) {

        logger.debug("MonthSelector.initValues: {} {}",legalCadastre, legalCadastre.getLegislationDataList().size());
        DateTime minDate = null;
        DateTime maxDate = null;


       List<DateTime> dateTimes = legalCadastre.getLegislationDataList().stream().map(data -> getDate(data)).flatMap(Collection::stream).collect(Collectors.toList());

        Optional<DateTime> optionalDateTimeMax = dateTimes.stream().max(DateTime::compareTo);
        Optional<DateTime> optionalDateTimeMin = dateTimes.stream().min(DateTime::compareTo);

        if (maxDate == null) {
            if (optionalDateTimeMax.isPresent()) {
                maxDate = optionalDateTimeMax.get();
            }
        }
        if (minDate == null) {
            if (optionalDateTimeMin.isPresent()) {
                minDate = optionalDateTimeMin.get();
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

    private List<DateTime> getDate(LegislationData data) {
        List<DateTime> dateTimes = new ArrayList<>();
        DateFilter.DateField dateField = fDateField.getValue();
        if (dateField == Date_Of_Examination) {
            setDateOfExaminationDate(data, dateTimes);
        } else if (dateField == ISSUE_DATE) {
            setIssueDate(data, dateTimes);
        } else if (dateField == VERSION) {
            setVersionDate(data, dateTimes);
        } else if (dateField == ALL) {
            setDateOfExaminationDate(data, dateTimes);
            setIssueDate(data, dateTimes);
            setVersionDate(data, dateTimes);
        }
        return null;
    }
    private static void setVersionDate(LegislationData data, List<DateTime> dateTimes) {
        if (data.getCurrentVersionDate() != null) {
            dateTimes.add(data.getCurrentVersionDate());
        }
    }

    private static void setIssueDate(LegislationData data, List<DateTime> dateTimes) {
        if (data.getIssueDate() != null) {
            dateTimes.add(data.getIssueDate());
        }
    }

    private static void setDateOfExaminationDate(LegislationData data, List<DateTime> dateTimes) {
        if (data.getDateOfExamination() != null) {
            dateTimes.add(data.getDateOfExamination());
        }
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
            ex.printStackTrace();
            logger.error(ex);
        }
    }

    public SimpleObjectProperty<DateFilter> getValuePropertyProperty() {
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
