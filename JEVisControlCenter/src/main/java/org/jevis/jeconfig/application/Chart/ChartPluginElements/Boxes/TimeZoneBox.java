package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeZoneBox extends ComboBox<DateTimeZone> {

    public TimeZoneBox() {

        Callback<ListView<DateTimeZone>, ListCell<DateTimeZone>> cellFactory = new Callback<javafx.scene.control.ListView<DateTimeZone>, ListCell<DateTimeZone>>() {
            @Override
            public ListCell<DateTimeZone> call(javafx.scene.control.ListView<DateTimeZone> param) {
                return new ListCell<DateTimeZone>() {
                    @Override
                    protected void updateItem(DateTimeZone dateTimeZone, boolean empty) {
                        super.updateItem(dateTimeZone, empty);
                        if (empty || dateTimeZone == null) {
                            setText("");
                        } else {

                            String text = dateTimeZone.getID() + " | " +
                                    DateTimeZone.getNameProvider().getShortName(I18n.getInstance().getLocale(), dateTimeZone.getID(), dateTimeZone.getNameKey(0));
//                                    + " | " + DateTimeZone.getNameProvider().getName(I18n.getInstance().getLocale(), dateTimeZone.getID(), dateTimeZone.getNameKey(0));

                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        List<String> allTimeZoneStrings = new ArrayList<>(DateTimeZone.getAvailableIDs());
        allTimeZoneStrings.add(0, "Europe/Berlin");
        allTimeZoneStrings.add(0, "UTC");
        List<DateTimeZone> allTimeZones = allTimeZoneStrings.stream().map(DateTimeZone::forID).collect(Collectors.toList());

        setItems(FXCollections.observableArrayList(allTimeZones));

        getSelectionModel().select(DateTimeZone.getDefault());

    }
}
