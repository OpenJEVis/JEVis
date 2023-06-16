package org.jevis.jecc.plugin.legal.ui;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.legal.data.IndexOfLegalProvisions;
import org.jevis.jecc.plugin.legal.data.ObligationData;
import org.joda.time.DateTime;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jevis.jecc.plugin.legal.ui.DateFilter.DateField.*;


public class TimeFilterSelector extends GridPane {

    private static final Logger logger = LogManager.getLogger(TimeFilterSelector.class);


    //TODo locale name from Column
    MFXComboBox<DateFilter.DateField> fDateField = new MFXComboBox<>(FXCollections.observableArrayList(ALL, ISSUE_DATE, Date_Of_Examination, VERSION));
    MFXComboBox<Month> fFromMonth = generateMonthBox();
    MFXComboBox<Month> fToMonth = generateMonthBox();
    MFXComboBox<Integer> fFromYear = generateYearBox();
    MFXComboBox<Integer> fToYear = generateYearBox();
    private SimpleObjectProperty<DateFilter> valueProperty = new SimpleObjectProperty<>(null);


    public TimeFilterSelector(IndexOfLegalProvisions indexOfLegalProvisions) {
        super();

        setHgap(8);
        setVgap(8);

        Label lFrom = new Label("Start Datum");
        Label lTo = new Label("End Datum");
        Label lDatum = new Label("Zeitbereich");


        initValues(indexOfLegalProvisions);
        ObligationData fakeNames = new ObligationData();

        fDateField.setFloatMode(FloatMode.DISABLED);
        fDateField.setConverter(new StringConverter<DateFilter.DateField>() {
            @Override
            public String toString(DateFilter.DateField object) {
                String text = "";
                if (object != null) {
                    text = switch (object) {
                        case ALL -> (I18n.getInstance().getString("plugin.action.tfiler.all"));
                        case VERSION -> (fakeNames.currentVersionDateProperty().getName());
                        case ISSUE_DATE -> (fakeNames.issueDateProperty().getName());
                        case Date_Of_Examination -> (fakeNames.dateOfExaminationProperty().getName());
                    };
                }

                return text;
            }

            @Override
            public DateFilter.DateField fromString(String string) {
                DateFilter.DateField returnField = null;

                for (DateFilter.DateField dateField : fDateField.getItems()) {
                    String text = "";
                    text = switch (dateField) {
                        case ALL -> (I18n.getInstance().getString("plugin.action.tfiler.all"));
                        case VERSION -> (fakeNames.currentVersionDateProperty().getName());
                        case ISSUE_DATE -> (fakeNames.issueDateProperty().getName());
                        case Date_Of_Examination -> (fakeNames.dateOfExaminationProperty().getName());
                    };
                    if (text.equals(string)) {
                        returnField = dateField;
                        break;
                    }
                }

                return returnField;
            }
        });


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


        TimeFilterSelector.this.addRow(0, fDateField, new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.date.from")), fFromMonth, fFromYear, new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.date.from")), fToMonth, fToYear);


    }

    private static void setVersionDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getCurrentVersionDate() != null) {
            dateTimes.add(data.getCurrentVersionDate());
        }
    }

    private static void setIssueDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getIssueDate() != null) {
            dateTimes.add(data.getIssueDate());
        }
    }

    private static void setDateOfExaminationDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getDateOfExamination() != null) {
            dateTimes.add(data.getDateOfExamination());
        }
    }

    private void initValues(IndexOfLegalProvisions indexOfLegalProvisions) {

        logger.debug("MonthSelector.initValues: {} {}", indexOfLegalProvisions, indexOfLegalProvisions.getLegislationDataList().size());
        DateTime minDate = null;
        DateTime maxDate = null;


        List<DateTime> dateTimes = indexOfLegalProvisions.getLegislationDataList().stream().map(data -> getDate(data)).flatMap(Collection::stream).collect(Collectors.toList());

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

    private List<DateTime> getDate(ObligationData data) {
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
            ex.printStackTrace();
            logger.error(ex);
        }
    }

    public SimpleObjectProperty<DateFilter> getValuePropertyProperty() {
        return valueProperty;
    }

    private MFXComboBox<Integer> generateYearBox() {

        ObservableList<Integer> months = FXCollections.observableArrayList();
        months.add(2018);
        months.add(2019);
        months.add(2020);
        months.add(2021);
        months.add(2022);
        months.add(2023);
        months.add(2024);

        MFXComboBox<Integer> field = new MFXComboBox<>(months);
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
