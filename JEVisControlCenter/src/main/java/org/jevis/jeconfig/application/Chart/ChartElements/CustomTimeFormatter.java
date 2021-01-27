package org.jevis.jeconfig.application.Chart.ChartElements;

import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.TickUnitSupplier;
import de.gsi.chart.axes.spi.format.AbstractFormatter;
import de.gsi.chart.axes.spi.format.DefaultTimeTickUnitSupplier;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author rstein
 */
public class CustomTimeFormatter extends AbstractFormatter {
    private static final Logger logger = LogManager.getLogger(CustomTimeFormatter.class);
    private static final TickUnitSupplier DEFAULT_TICK_UNIT_SUPPLIER = new DefaultTimeTickUnitSupplier();
    private static final DateTimeFormatter HIGHRES_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss +SSS",
            I18n.getInstance().getLocale());
    protected final DateTimeFormatter[] dateFormat;
    protected int oldIndex = -1;
    protected int formatterIndex;

    protected ObjectProperty<ZoneOffset> timeZone = new SimpleObjectProperty<>(ZoneOffset.UTC);

    /**
     * Construct a DefaultFormatter for the given NumberAxis
     */
    public CustomTimeFormatter() {
        this(null);

    }

    /**
     * Construct a DefaultFormatter for the given NumberAxis
     *
     * @param axis The axis to format tick marks for
     */
    public CustomTimeFormatter(final Axis axis) {
        super(axis);
        setTickUnitSupplier(CustomTimeFormatter.DEFAULT_TICK_UNIT_SUPPLIER);

        dateFormat = new DateTimeFormatter[DefaultTimeTickUnitSupplier.TICK_UNIT_FORMATTER_DEFAULTS.length];
        for (int i = 0; i < dateFormat.length; i++) {
            final String format = DefaultTimeTickUnitSupplier.TICK_UNIT_FORMATTER_DEFAULTS[i];
            if (format.contains(DefaultTimeTickUnitSupplier.HIGHRES_MODE)) {
                dateFormat[i] = CustomTimeFormatter.HIGHRES_FORMATTER;
            } else {
                dateFormat[i] = DateTimeFormatter.ofPattern(format, I18n.getInstance().getLocale());
            }
        }
    }

    public String formatHighResString(final Number utcValueSeconds) {
        final double timeAbs = Math.abs(utcValueSeconds.doubleValue());
        final long timeUS = (long) (TimeUnit.SECONDS.toMicros(1) * timeAbs);
        final long longUTCSeconds = Math.abs(utcValueSeconds.longValue());
        final int longNanoSeconds = (int) ((timeAbs - longUTCSeconds) * 1e9);
        final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(longUTCSeconds, longNanoSeconds,
                getTimeZoneOffset(longUTCSeconds));
        return dateTime.format(CustomTimeFormatter.HIGHRES_FORMATTER).concat(Long.toString(timeUS % 1000)).concat("us")
                .replaceAll(" ", System.lineSeparator());
    }

    @Override
    public Number fromString(final String string) {
        return null;
    }

    public String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private String getTimeString(final Number utcValueSeconds) {
        if (formatterIndex <= DefaultTimeTickUnitSupplier.HIGHRES_MODE_INDICES) {
            return formatHighResString(utcValueSeconds);
        }

        long longUTCSeconds = utcValueSeconds.longValue();
        int nanoSeconds = (int) ((utcValueSeconds.doubleValue() - longUTCSeconds) * 1e9);
        if (nanoSeconds < 0) { // Correctly Handle dates before EPOCH
            longUTCSeconds -= 1;
            nanoSeconds += (int) 1e9;
        }
        final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(longUTCSeconds, nanoSeconds,
                getTimeZoneOffset(longUTCSeconds));

        return dateTime.format(dateFormat[formatterIndex]).replaceAll(" ", System.lineSeparator());
    }

    /**
     * @return Returns the A time-zone offset from Greenwich/UTC, such as {@code +02:00}.
     */
    public ZoneOffset getTimeZoneOffset(long utcSeconds) {
//        DateTime ts = new DateTime(utcSeconds * 1000);
        Calendar calendar = Calendar.getInstance(I18n.getInstance().getLocale());
        int offset1 = calendar.getTimeZone().getOffset(utcSeconds * 1000) / 1000;
//        logger.info("offset for date {} is: {}", new DateTime(utcSeconds * 1000).toString("yyyy-MM-dd HH:mm"), offset1 / 60);
        return ZoneOffset.ofTotalSeconds(offset1);
//        return timeZoneOffsetProperty().get();
    }

    /**
     * @param newOffset the ZoneOffset to be taken into account (UTC if 'null'.
     */
    public void setTimeZoneOffset(final ZoneOffset newOffset) {
        if (newOffset != null) {
            timeZoneOffsetProperty().set(newOffset);
            return;
        }
        timeZoneOffsetProperty().set(ZoneOffset.UTC);
    }

    @Override
    protected void rangeUpdated() {
        // set formatter based on range if necessary
        formatterIndex = DefaultTimeTickUnitSupplier.getTickIndex(getRange());
        if (oldIndex != formatterIndex) {
            labelCache.clear();
            oldIndex = formatterIndex;
        }
    }

    /**
     * A time-zone offset from Greenwich/UTC, such as {@code +02:00}.
     * <p>
     * A time-zone offset is the amount of time that a time-zone differs from Greenwich/UTC. This is usually a fixed
     * number of hours and minutes.
     *
     * @return ZoneOffset property that is being used to compute the local time axis
     */
    public ObjectProperty<ZoneOffset> timeZoneOffsetProperty() {
        return timeZone;
    }

    @Override
    public String toString(final Number utcValueSeconds) {
        return labelCache.computeIfAbsent(utcValueSeconds, this::getTimeString);
    }
}
