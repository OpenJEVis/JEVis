package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;


import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeZoneBox extends ComboBox<DateTimeZone> {

    public TimeZoneBox() {

        //TODO JFX17
        setConverter(new StringConverter<DateTimeZone>() {
            @Override
            public String toString(DateTimeZone object) {
                String text = "";

                if (object != null) {
                    text = object.getID() + " | " +
                            DateTimeZone.getNameProvider().getShortName(I18n.getInstance().getLocale(), object.getID(), object.getNameKey(0));
//                                    + " | " + DateTimeZone.getNameProvider().getName(I18n.getInstance().getLocale(), dateTimeZone.getID(), dateTimeZone.getNameKey(0));
                }

                return text;
            }

            @Override
            public DateTimeZone fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });

        List<String> allTimeZoneStrings = new ArrayList<>(DateTimeZone.getAvailableIDs());
        allTimeZoneStrings.add(0, "Europe/Berlin");
        allTimeZoneStrings.add(0, "UTC");
        List<DateTimeZone> allTimeZones = allTimeZoneStrings.stream().map(DateTimeZone::forID).collect(Collectors.toList());

        setItems(FXCollections.observableArrayList(allTimeZones));

        getSelectionModel().select(DateTimeZone.getDefault());

    }
}