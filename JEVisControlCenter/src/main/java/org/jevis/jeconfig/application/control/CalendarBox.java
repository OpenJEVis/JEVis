package org.jevis.jeconfig.application.control;

import de.jollyday.CalendarHierarchy;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarBox extends ComboBox<CalendarRow> {

    public CalendarBox() {
        List<CalendarRow> list = new ArrayList<>();
        for (String countryCode : HolidayManager.getSupportedCalendarCodes()) {
            HolidayManager instance = HolidayManager.getInstance(ManagerParameters.create(countryCode));
            String countryName = instance.getCalendarHierarchy().getDescription(I18n.getInstance().getLocale());
            for (Map.Entry<String, CalendarHierarchy> entry : instance.getCalendarHierarchy().getChildren().entrySet()) {
                String stateCode = entry.getKey();
                CalendarHierarchy calendarHierarchy = entry.getValue();
                String stateName = calendarHierarchy.getDescription();
                CalendarRow calendarRow = new CalendarRow(countryCode, countryName, stateCode, stateName);
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

        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        setItems(FXCollections.observableArrayList(list));
    }


}
