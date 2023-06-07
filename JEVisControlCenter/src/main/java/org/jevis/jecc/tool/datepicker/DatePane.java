package org.jevis.jecc.tool.datepicker;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.GridPane;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class for the {@link MonthView}, {@link YearView} and {@link DecadesView}.
 *
 * @author Christian Schudt
 */
abstract class DatePane extends GridPane {

    /**
     * The calendar view.
     */
    protected CalendarView calendarView;
    protected StringProperty title = new SimpleStringProperty();

    /**
     * Sets basic stuff
     *
     * @param calendarView The calendar view.
     */
    protected DatePane(final CalendarView calendarView) {
        this.calendarView = calendarView;

        // When the date changed, updateData the days.
        calendarView.calendarDate.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateContent();
            }
        });

        // Every time the calendar changed, rebuild the pane and updateData the content.
        calendarView.calendarProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                getChildren().clear();
                buildContent();
                updateContent();
            }
        });

        buildContent();
        updateContent();
    }

    /**
     * This is the date, this pane operates on.
     *
     * @param date The date.
     */
    protected void setDate(Date date) {
        calendarView.getCalendar().setTime(date);
        updateContent();
        // Restore
        calendarView.getCalendar().setTime(calendarView.calendarDate.get());
    }

    /**
     * Builds the content.
     */
    protected abstract void buildContent();

    /**
     * Updates the content.
     */
    protected abstract void updateContent();

    /**
     * The title property which is defined by the pane.
     *
     * @return The property.
     */
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    /**
     * Gets the date format, associated with the current calendar.
     *
     * @param format The date format as String.
     * @return The date format.
     */
    protected DateFormat getDateFormat(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format, calendarView.localeProperty().get());
        dateFormat.setCalendar(calendarView.getCalendar());
        return dateFormat;
    }
}
