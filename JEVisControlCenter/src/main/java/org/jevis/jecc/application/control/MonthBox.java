package org.jevis.jecc.application.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.datetime.Months;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.time.YearMonth;

public class MonthBox extends ComboBox<Months> {
    private YearBox yearBox;
    private DayBox dayBox;
    private WorkDays workDays;

    public MonthBox() {
        super();

        ObservableList<Months> months = FXCollections.observableArrayList(Months.values());
        setItems(months);

        Callback<ListView<Months>, ListCell<Months>> cellFactory = new Callback<ListView<Months>, ListCell<Months>>() {
            @Override
            public ListCell<Months> call(ListView<Months> param) {
                return new ListCell<Months>() {
                    @Override
                    public void updateItem(Months item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            try {
                                if (workDays != null && workDays.isCustomWorkDay() && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
                                    switch (item) {
                                        case DECEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jan"));
                                            break;
                                        case JANUARY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.feb"));
                                            break;
                                        case FEBRUARY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.mar"));
                                            break;
                                        case MARCH:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.apr"));
                                            break;
                                        case APRIL:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.may"));
                                            break;
                                        case MAY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jun"));
                                            break;
                                        case JUNE:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jul"));
                                            break;
                                        case JULY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.aug"));
                                            break;
                                        case AUGUST:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.sep"));
                                            break;
                                        case SEPTEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.oct"));
                                            break;
                                        case OCTOBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.nov"));
                                            break;
                                        case NOVEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.dec"));
                                            break;
                                    }
                                } else {
                                    switch (item) {
                                        case JANUARY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jan"));
                                            break;
                                        case FEBRUARY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.feb"));
                                            break;
                                        case MARCH:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.mar"));
                                            break;
                                        case APRIL:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.apr"));
                                            break;
                                        case MAY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.may"));
                                            break;
                                        case JUNE:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jun"));
                                            break;
                                        case JULY:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jul"));
                                            break;
                                        case AUGUST:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.aug"));
                                            break;
                                        case SEPTEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.sep"));
                                            break;
                                        case OCTOBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.oct"));
                                            break;
                                        case NOVEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.nov"));
                                            break;
                                        case DECEMBER:
                                            setText(I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.dec"));
                                            break;
                                    }
                                }
                            } catch (Exception ex) {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };

        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().selectFirst();
    }

    public void setRelations(YearBox yearBox, DayBox dayBox, DateTime nextTS) {
        this.yearBox = yearBox;
        this.dayBox = dayBox;

        getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                Integer year = yearBox.getSelectionModel().getSelectedItem();
                YearMonth yearMonthObject = YearMonth.of(year, newValue.intValue() + 1);
                dayBox.setDays(yearMonthObject.lengthOfMonth());
            }
        });

        if (nextTS != null) {
            Platform.runLater(() -> {
                getSelectionModel().select(nextTS.getMonthOfYear() - 1);
                dayBox.getSelectionModel().select(Integer.valueOf(nextTS.getDayOfMonth()));
            });
        } else {
            Platform.runLater(() -> getSelectionModel().select(DateTime.now().getMonthOfYear() - 1));
        }
    }

    public void setWorkDays(WorkDays workDays) {
        this.workDays = workDays;
    }
}
