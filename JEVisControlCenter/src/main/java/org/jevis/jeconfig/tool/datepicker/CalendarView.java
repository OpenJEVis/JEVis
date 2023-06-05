package org.jevis.jeconfig.tool.datepicker;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A calendar control
 *
 * @author Christian Schudt
 */
public class CalendarView extends VBox {

    private static final String CSS_CALENDAR_FOOTER = "calendar-footer";
    private static final String CSS_CALENDAR = "calendar";
    private static final String CSS_CALENDAR_TODAY_BUTTON = "calendar-today-button";

    /**
     * Initializes a calendar with the default locale.
     */
    public CalendarView() {
        this(Locale.getDefault());
    }

    /**
     * Initializes a calendar with the given locale. E.g. if the locale is
     * en-US, the calendar starts the days on Sunday. If it is de-DE the
     * calendar starts the days on Monday.
     * <p/>
     * Note that the Java implementation only knows
     * {@link java.util.GregorianCalendar} and sun.util.BuddhistCalendar.
     *
     * @param locale The locale.
     */
    public CalendarView(final Locale locale) {
        this(locale, Calendar.getInstance(locale));

        // When the locale changes, also change the calendar.
        this.locale.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                calendar.set(Calendar.getInstance(localeProperty().get()));
            }
        });
    }

    private final HBox todayButtonBox;
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<Locale>();

    /**
     * Gets or sets the locale.
     *
     * @return The property.
     */
    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    private final ObjectProperty<Calendar> calendar = new SimpleObjectProperty<Calendar>();

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    /**
     * Gets or sets the calendar.
     *
     * @return The property.
     */
    public ObjectProperty<Calendar> calendarProperty() {
        return calendar;
    }

    private final ObservableList<Integer> disabledWeekdays = FXCollections.observableArrayList();

    public Calendar getCalendar() {
        return calendar.get();
    }

    public void setCalendar(Calendar calendar) {
        this.calendar.set(calendar);
    }

    /**
     * Gets the list of disabled week days. E.g. if you add
     * <code>Calendar.WEDNESDAY</code>, Wednesday will be disabled.
     *
     * @return The list.
     */
    public ObservableList<Integer> getDisabledWeekdays() {
        return disabledWeekdays;
    }

    private final ObservableList<Date> disabledDates = FXCollections.observableArrayList();

    /**
     * Gets the list of disabled dates. You can add specific date, in order to
     * disable them.
     *
     * @return The list.
     */
    public ObservableList<Date> getDisabledDates() {
        return disabledDates;
    }

    private final ObjectProperty<Date> currentDate = new SimpleObjectProperty<Date>();

    /**
     * Gets the selected date.
     *
     * @return The property.
     */
    public ReadOnlyObjectProperty<Date> selectedDateProperty() {
        return selectedDate;
    }

    private final BooleanProperty showTodayButton = new SimpleBooleanProperty();

    public ObjectProperty<Date> currentDateProperty() {
        return currentDate;
    }

    /**
     * Indicates, whether the today button should be shown.
     *
     * @return The property.
     */
    public BooleanProperty showTodayButtonProperty() {
        return showTodayButton;
    }

    private final StringProperty todayButtonText = new SimpleStringProperty("Today");

    public boolean getShowTodayButton() {
        return showTodayButton.get();
    }

    public void setShowTodayButton(boolean showTodayButton) {
        this.showTodayButton.set(showTodayButton);
    }

    /**
     * The text of the today button
     *
     * @return The property.
     */
    public StringProperty todayButtonTextProperty() {
        return todayButtonText;
    }

    private final BooleanProperty showWeeks = new SimpleBooleanProperty(false);

    public String getTodayButtonText() {
        return todayButtonText.get();
    }

    public void setTodayButtonText(String todayButtonText) {
        this.todayButtonText.set(todayButtonText);
    }

    /**
     * Indicates, whether the week numbers are shown.
     *
     * @return The property.
     */
    public BooleanProperty showWeeksProperty() {
        return showWeeks;
    }

    /**
     * Initializes the control with the given locale and the given calendar.
     * <p/>
     * This way, you can pass a custom calendar (e.g. you could implement the
     * Hijri Calendar for the arabic world). Or you can use an American style
     * calendar (starting with Sunday as first day of the week) together with
     * another language.
     * <p/>
     * The locale determines the date format.
     *
     * @param locale The locale.
     * @param calendar The calendar
     */
    public CalendarView(final Locale locale, final Calendar calendar) {

        this.locale.set(locale);
        this.calendar.set(calendar);

        getStyleClass().add(CSS_CALENDAR);

        setMaxWidth(Control.USE_PREF_SIZE);

        currentlyViewing.set(Calendar.MONTH);

        calendarDate.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                calendar.setTime(calendarDate.get());
            }
        });
        this.calendarDate.set(new Date());
        currentDate.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Date date = new Date();
                if (currentDate.get() != null) {
                    date = currentDate.get();
                }
                calendarDate.set(date);
            }
        });
        MainStackPane mainStackPane = new MainStackPane(this);
        VBox.setVgrow(mainStackPane, Priority.ALWAYS);
        mainNavigationPane = new MainNavigationPane(this);

        todayButtonBox = new HBox();
        todayButtonBox.getStyleClass().add(CSS_CALENDAR_FOOTER);

        MFXButton todayButton = new MFXButton();
        todayButton.textProperty().bind(todayButtonText);
        todayButton.getStyleClass().add(CSS_CALENDAR_TODAY_BUTTON);
        todayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Calendar calendar = calendarProperty().get();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                selectedDate.set(calendar.getTime());
            }
        });
        todayButtonBox.setAlignment(Pos.CENTER);
        todayButtonBox.getChildren().add(todayButton);

        getChildren().addAll(mainNavigationPane, mainStackPane);

        showTodayButton.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (showTodayButton.get()) {
                    getChildren().add(todayButtonBox);
                } else {
                    getChildren().remove(todayButtonBox);
                }
            }
        });
        showTodayButton.set(true);

    }

    public boolean getShowWeeks() {
        return showWeeks.get();
    }

    public void setShowWeeks(boolean showWeeks) {
        this.showWeeks.set(showWeeks);
    }

    /**
     * Package internal properties.
     */
    MainNavigationPane mainNavigationPane;
    /**
     * Counts the current transitions. As long as an animation is going, the
     * panels should not move left and right.
     */
    IntegerProperty ongoingTransitions = new SimpleIntegerProperty(0);
    ObjectProperty<Date> selectedDate = new SimpleObjectProperty<Date>();
    ObjectProperty<Date> calendarDate = new SimpleObjectProperty<Date>();
    IntegerProperty currentlyViewing = new SimpleIntegerProperty();
    StringProperty title = new SimpleStringProperty();
}
