package org.jevis.jeconfig.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.jevis.commons.datetime.Months;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.time.YearMonth;

public class MonthBox extends MFXComboBox<Months> {
    private YearBox yearBox;
    private DayBox dayBox;

    public MonthBox() {
        super();

        ObservableList<Months> months = FXCollections.observableArrayList(Months.values());
        setItems(months);

        //TODO JFX17

        setConverter(new StringConverter<Months>() {
            @Override
            public String toString(Months object) {
                String text = "";
                if (object != null) {
                    try {
                        switch (object) {
                            case JANUARY:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jan");
                                break;
                            case FEBRUARY:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.feb");
                                break;
                            case MARCH:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.mar");
                                break;
                            case APRIL:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.apr");
                                break;
                            case MAY:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.may");
                                break;
                            case JUNE:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jun");
                                break;
                            case JULY:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.jul");
                                break;
                            case AUGUST:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.aug");
                                break;
                            case SEPTEMBER:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.sep");
                                break;
                            case OCTOBER:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.oct");
                                break;
                            case NOVEMBER:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.nov");
                                break;
                            case DECEMBER:
                                text = I18n.getInstance().getString("plugin.object.attribute.scheduleeditor.month.dec");
                                break;
                        }
                    } catch (Exception ignored) {

                    }
                }
                return text;
            }

            @Override
            public Months fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

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
                selectIndex(nextTS.getMonthOfYear() - 1);
                dayBox.selectItem(nextTS.getDayOfMonth());
            });
        } else {
            Platform.runLater(() -> selectIndex(DateTime.now().getMonthOfYear() - 1));
        }
    }

}
