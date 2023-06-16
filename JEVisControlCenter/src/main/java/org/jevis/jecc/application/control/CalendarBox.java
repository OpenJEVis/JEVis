package org.jevis.jecc.application.control;


import de.focus_shift.CalendarHierarchy;
import de.focus_shift.HolidayManager;
import de.focus_shift.ManagerParameters;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarBox extends MFXComboBox<CalendarRow> {

    public CalendarBox() {
        List<CalendarRow> list = new ArrayList<>();
        Locale locale = I18n.getInstance().getLocale();
        for (String countryCode : HolidayManager.getSupportedCalendarCodes()) {
            HolidayManager instance = HolidayManager.getInstance(ManagerParameters.create(countryCode));
            String countryName = instance.getCalendarHierarchy().getDescription(locale);

            if (!instance.getCalendarHierarchy().getChildren().isEmpty()) {
                for (Map.Entry<String, CalendarHierarchy> entry : instance.getCalendarHierarchy().getChildren().entrySet()) {
                    String stateCode = entry.getKey();
                    CalendarHierarchy calendarHierarchy = entry.getValue();
                    String stateName = calendarHierarchy.getDescription();
                    CalendarRow calendarRow = new CalendarRow(countryCode, countryName, stateCode, stateName);
                    list.add(calendarRow);
                }
            } else {
                CalendarRow calendarRow = new CalendarRow(countryCode, countryName);
                list.add(calendarRow);
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        list.sort((o1, o2) -> ac.compare(o1.getCountryName() + " " + o1.getStateName(), o2.getCountryName() + " " + o2.getStateName()));

        Callback<ListView<CalendarRow>, ListCell<CalendarRow>> cellFactory = new Callback<ListView<CalendarRow>, ListCell<CalendarRow>>() {
            @Override
            public ListCell<CalendarRow> call(ListView<CalendarRow> param) {
                return new ListCell<CalendarRow>() {
                    @Override
                    public void updateItem(CalendarRow item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);
                        if (item != null && !empty) {
                            setText(item.getCountryName() + " " + item.getStateName());
                        }
                    }
                };
            }
        };

        //TODO JFX17
        setConverter(new StringConverter<CalendarRow>() {
            @Override
            public String toString(CalendarRow object) {
                return object.getCountryName() + " " + object.getStateName();
            }

            @Override
            public CalendarRow fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

        setItems(FXCollections.observableArrayList(list));
    }


}
