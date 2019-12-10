/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.jevis.jeconfig.application.Chart.ChartElements;

import com.sun.javafx.charts.ChartLayoutAnimator;
import com.sun.javafx.css.converters.SizeConverter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DateTimeStringConverter;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.jfx.ValueAxis;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An axis class that plots a range of numbers with major tick marks every tickUnit.
 * You can use any Number type with this axis.
 *
 * @since JavaFX 2.0
 */
public class DateAxis extends ValueAxis<Long> {

    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private final StringProperty currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");
    private final DefaultFormatter defaultFormatter = new DefaultFormatter(this);
    private Object currentAnimationID;
    private DateTime firstTS;
    private DateTime lastTS;
    private List<ChartDataModel> chartDataModels = new ArrayList<>();
    private boolean asDuration = false;

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------
    /**
     * When true zero is always included in the visible range. This only has effect if auto-ranging is on.
     */
    private BooleanProperty forceZeroInRange = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            // This will affect layout if we are auto ranging
            if (isAutoRanging()) {
                requestAxisLayout();
                invalidateRange();
            }
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "forceZeroInRange";
        }
    };
    /**
     * The value between each major tick mark in data units. This is automatically set if we are auto-ranging.
     */
    private DoubleProperty tickUnit = new StyleableDoubleProperty(900000) {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public CssMetaData<DateAxis, Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnit";
        }
    };
    /**
     * The value between each major tick mark in data units. This is automatically set if we are auto-ranging.
     */
    private DoubleProperty minorTickUnit = new StyleableDoubleProperty(60000) {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public CssMetaData<DateAxis, Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "minorTickUnit";
        }
    };

    /**
     * Creates an auto-ranging DateAxis.
     */
    public DateAxis() {
    }

    /**
     * Creates a non-auto-ranging DateAxis with the given upper bound, lower bound and tick unit.
     *
     * @param lowerBound The lower bound for this axis, i.e. min plottable value
     * @param upperBound The upper bound for this axis, i.e. max plottable value
     * @param tickUnit   The tick unit, i.e. space between tickmarks
     */
    public DateAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
    }

    /**
     * Creates a non-auto-ranging DateAxis with the given lower bound, upper bound and tick unit.
     *
     * @param axisLabel  The name to display for this axis
     * @param lowerBound The lower bound for this axis, i.e. min plottable value
     * @param upperBound The upper bound for this axis, i.e. max plottable value
     * @param tickUnit   The tick unit, i.e. space between tickmarks
     */
    public DateAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
        setLabel(axisLabel);
    }

    public DateAxis(Boolean asDuration, DateTime firstTS) {
        this.asDuration = asDuration;
        this.firstTS = firstTS;
        forceZeroInRange.set(false);
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its superclasses.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    public String getCurrentFormatterProperty() {
        return currentFormatterProperty.get();
    }

    public void setCurrentFormatterProperty(String currentFormatterProperty) {
        this.currentFormatterProperty.set(currentFormatterProperty);
    }

    public void setAsDuration(boolean asDuration) {
        this.asDuration = asDuration;
    }

    public void setFirstTS(DateTime dateTime) {
        this.firstTS = dateTime;
    }

    public void setLastTS(DateTime dateTime) {
        this.lastTS = dateTime;
    }

    public void setChartDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    public final boolean isForceZeroInRange() {
        return forceZeroInRange.getValue();
    }

    public final void setForceZeroInRange(boolean value) {
        forceZeroInRange.setValue(value);
    }

    public final BooleanProperty forceZeroInRangeProperty() {
        return forceZeroInRange;
    }

    public final double getTickUnit() {
        return tickUnit.get();
    }

    public final void setTickUnit(double value) {
        tickUnit.set(value);
    }

    // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------

    public final DoubleProperty tickUnitProperty() {
        return tickUnit;
    }

    public final double getMinorTickUnit() {
        return minorTickUnit.get();
    }

    public final void setMinorTickUnit(double value) {
        minorTickUnit.set(value);
    }

    public final DoubleProperty minorTickUnitProperty() {
        return minorTickUnit;
    }

    // -------------- PROTECTED METHODS --------------------------------------------------------------------------------

    public DateTime getDateTimeForDisplay(double displayPosition) {
        Number value = getValueForDisplay(displayPosition);
        return new DateTime(value.longValue());
    }

    /**
     * Get the string label name for a tick mark with the given value.
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    public String getTickMarkLabel(Long value) {
        if (!asDuration) {
            DefaultFormatter formatter = new DefaultFormatter(this);
            return formatter.toString(value);
        } else {
            DateTime ts = new DateTime(value);
            return (ts.getMillis() - firstTS.getMillis()) / 1000 / 60 / 60 + " h";
        }
    }

    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
    @Override
    protected Object getRange() {
        return new Object[]{
                getLowerBound(),
                getUpperBound(),
                getTickUnit(),
                getScale(),
                currentFormatterProperty.get()
        };
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range   A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override
    protected void setRange(Object range, boolean animate) {
        final Object[] rangeProps = (Object[]) range;
        final double lowerBound = (Double) rangeProps[0];
        final double upperBound = (Double) rangeProps[1];
        final double tickUnit = (Double) rangeProps[2];
        final double scale = (Double) rangeProps[3];
        final String formatter = (String) rangeProps[4];
        currentFormatterProperty.set(formatter);
        final double oldLowerBound = getLowerBound();
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);
        if (animate) {
            animator.stop(currentAnimationID);
            currentAnimationID = animator.animate(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(currentLowerBound, oldLowerBound),
                            new KeyValue(scalePropertyImpl(), getScale())
                    ),
                    new KeyFrame(Duration.millis(700),
                            new KeyValue(currentLowerBound, lowerBound),
                            new KeyValue(scalePropertyImpl(), scale)
                    )
            );
        } else {
            currentLowerBound.set(lowerBound);
            setScale(scale);
        }
    }

    /**
     * Calculates a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range  A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override
    protected List<Long> calculateTickValues(double length, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final Double lowerBound = (Double) rangeProps[0];
        final Double upperBound = (Double) rangeProps[1];
        final Double tickUnit = (Double) rangeProps[2];
        final Double minorTickUnit = getMinorTickUnit();

        List<Long> tickValues = new ArrayList<>();
        if (lowerBound.equals(upperBound)) {
            tickValues.add(lowerBound.longValue());
        } else if (tickUnit <= 0) {
            tickValues.add(lowerBound.longValue());
            tickValues.add(upperBound.longValue());
        } else if (tickUnit > 0) {
            tickValues.add(lowerBound.longValue());
            if (((upperBound - lowerBound) / tickUnit) > 2000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 2000 major tick marks on a DateAxis. " +
                        "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit);
            } else if (upperBound - lowerBound < Period.days(1).toStandardDuration().getMillis() * 30.4375) {
                if (lowerBound + tickUnit < upperBound) {
                    // If tickUnit is integer, start with the nearest integer
                    Double major = Math.rint(tickUnit) == tickUnit ? Math.ceil(lowerBound) : lowerBound + tickUnit;
                    int count = (int) Math.ceil((upperBound - major) / tickUnit);
                    for (int i = 0; major < upperBound && i < count; major += tickUnit, i++) {
                        if (!tickValues.contains(major.longValue())) {
                            tickValues.add(major.longValue());
                        }
                    }
                }
            } else {
                long startMillis = lowerBound.longValue();
                int count = (int) Math.ceil((upperBound - startMillis) / minorTickUnit);
                for (int i = 0; startMillis < upperBound && i < count; startMillis += minorTickUnit.longValue(), i++) {
                    DateTime currentDate = new DateTime(startMillis).withTime(0, 0, 0, 0);
                    if (!tickValues.contains(currentDate.getMillis()) && currentDate.getDayOfWeek() == 1 && currentDate.getHourOfDay() == 0 && currentDate.getMinuteOfHour() == 0) {
                        tickValues.add(currentDate.getMillis());
                    }
                }
            }

            tickValues.add(upperBound.longValue());
        }
        return tickValues;
    }

    /**
     * Calculates a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    protected List<Long> calculateMinorTickMarks() {
        final List<Long> minorTickMarks = new ArrayList<>();
        final Double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double tickUnit = getTickUnit();
        final double minorUnit = getMinorTickUnit();
        if (tickUnit > 0) {
            if (((upperBound - lowerBound) / minorUnit) > 10000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 10000 minor tick marks on a DateAxis. " +
                        "Lower Bound=" + getLowerBound() + ", Upper Bound=" + getUpperBound() + ", Tick Unit=" + tickUnit);
                return minorTickMarks;
            }
            if (upperBound - lowerBound < Period.days(1).toStandardDuration().getMillis() * 30.4375) {
                final boolean tickUnitIsInteger = Math.rint(tickUnit) == tickUnit;
                if (tickUnitIsInteger) {
                    Double minor = Math.floor(lowerBound) + minorUnit;
                    int count = (int) Math.ceil((Math.ceil(lowerBound) - minor) / minorUnit);
                    for (int i = 0; minor < Math.ceil(lowerBound) && i < count; minor += minorUnit, i++) {
                        if (minor > lowerBound) {
                            minorTickMarks.add(minor.longValue());
                        }
                    }
                }
                double major = tickUnitIsInteger ? Math.ceil(lowerBound) : lowerBound;
                int count = (int) Math.ceil((upperBound - major) / tickUnit);
                for (int i = 0; major < upperBound && i < count; major += tickUnit, i++) {
                    final double next = Math.min(major + tickUnit, upperBound);
                    Double minor = major + minorUnit;
                    int minorCount = (int) Math.ceil((next - minor) / minorUnit);
                    for (int j = 0; minor < next && j < minorCount; minor += minorUnit, j++) {
                        minorTickMarks.add(minor.longValue());
                    }
                }
            } else {
                long startMillis = lowerBound.longValue();
                int count = (int) Math.ceil((upperBound - startMillis) / minorUnit);
                for (int i = 0; startMillis < upperBound && i < count; startMillis += minorUnit, i++) {
                    DateTime currentDate = new DateTime(startMillis).withTime(0, 0, 0, 0);
                    if (!minorTickMarks.contains(currentDate.getMillis()) && currentDate.getHourOfDay() == 0 && currentDate.getMinuteOfHour() == 0) {
                        minorTickMarks.add(currentDate.getMillis());
                    }
                }
            }
        }
        return minorTickMarks;
    }

    /**
     * Measures the size of the label for a given tick mark value. This uses the font that is set for the tick marks.
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    @Override
    public Dimension2D measureTickMarkSize(Long value, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final String formatter = (String) rangeProps[4];
        return measureTickMarkSize(value, getTickLabelRotation(), formatter);
    }

    /**
     * Measures the size of the label for a given tick mark value. This uses the font that is set for the tick marks.
     *
     * @param value        tick mark value
     * @param rotation     The text rotation
     * @param numFormatter The number formatter
     * @return size of tick mark label for given value
     */
    private Dimension2D measureTickMarkSize(Long value, double rotation, String numFormatter) {
        String labelText;
        DefaultFormatter formatter = new DefaultFormatter(this);
        labelText = formatter.toString(value, numFormatter);
        return measureTickMarkLabelSize(labelText, rotation);
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged.
     *
     * @param minValue  The min data value that needs to be plotted on this axis
     * @param maxValue  The max data value that needs to be plotted on this axis
     * @param length    The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     */
    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
        final Side side = getEffectiveSide();
        // check if we need to force zero into range
        if (isForceZeroInRange()) {
            if (maxValue < 0) {
                maxValue = 0;
            } else if (minValue > 0) {
                minValue = 0;
            }
        }
        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int) Math.floor(length / labelSize);
        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);
        int minorTickCount = Math.max(getMinorTickCount(), 1);

        double range = maxValue - minValue;

        if (range != 0 && range / (numOfTickMarks * minorTickCount) <= Math.ulp(minValue)) {
            range = 0;
        }
        // pad min and max by 2%, checking if the range is zero
        final double paddedRange = (range == 0)
                ? minValue == 0 ? 2 : Math.abs(minValue) * 0.02
                : Math.abs(range) * 1.02;
        final double padding = (paddedRange - range) / 2;
        // if min and max are not zero then add padding to them
        double paddedMin = minValue - padding;
        double paddedMax = maxValue + padding;
        // check padding has not pushed min or max over zero line
        if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMin = 0;
        }
        if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMax = 0;
        }
        // calculate tick unit for the number of ticks can have in the given data range
        double tickUnit = paddedRange / (double) numOfTickMarks;
        // search for the best tick unit that fits
        double tickUnitRounded = 0;
        double minRounded = 0;
        double maxRounded = 0;
        int count = 0;
        double reqLength = Double.MAX_VALUE;
        String formatter = "0.00000000";
        // loop till we find a set of ticks that fit length and result in a total of less than 20 tick marks
        while (reqLength > length || count > 20) {
            int exp = (int) Math.floor(Math.log10(tickUnit));
            final double mant = tickUnit / Math.pow(10, exp);
            double ratio = mant;
            if (mant > 5d) {
                exp++;
                ratio = 1;
            } else if (mant > 1d) {
                ratio = mant > 2.5 ? 5 : 2.5;
            }
            if (exp > 1) {
                formatter = "#,##0";
            } else if (exp == 1) {
                formatter = "0";
            } else {
                final boolean ratioHasFrac = Math.rint(ratio) != ratio;
                final StringBuilder formatterB = new StringBuilder("0");
                int n = ratioHasFrac ? Math.abs(exp) + 1 : Math.abs(exp);
                if (n > 0) formatterB.append(".");
                for (int i = 0; i < n; ++i) {
                    formatterB.append("0");
                }
                formatter = formatterB.toString();

            }
            tickUnitRounded = ratio * Math.pow(10, exp);
            // move min and max to nearest tick mark
            minRounded = Math.floor(paddedMin / tickUnitRounded) * tickUnitRounded;
            maxRounded = Math.ceil(paddedMax / tickUnitRounded) * tickUnitRounded;
            // calculate the required length to display the chosen tick marks for real, this will handle if there are
            // huge numbers involved etc or special formatting of the tick mark label text
            double maxReqTickGap = 0;
            double last = 0;
            count = (int) Math.ceil((maxRounded - minRounded) / tickUnitRounded);
            Double major = minRounded;
            for (int i = 0; major <= maxRounded && i < count; major += tickUnitRounded, i++) {
                Dimension2D markSize = measureTickMarkSize(major.longValue(), getTickLabelRotation(), formatter);
                double size = side.isVertical() ? markSize.getHeight() : markSize.getWidth();
                if (i == 0) { // first
                    last = size / 2;
                } else {
                    maxReqTickGap = Math.max(maxReqTickGap, last + 6 + (size / 2));
                }
            }
            reqLength = (count - 1) * maxReqTickGap;
            tickUnit = tickUnitRounded;

            // fix for RT-35600 where a massive tick unit was being selected
            // unnecessarily. There is probably a better solution, but this works
            // well enough for now.
            if (numOfTickMarks == 2 && reqLength > length) {
                break;
            }
            if (reqLength > length || count > 20)
                tickUnit *= 2; // This is just for the while loop, if there are still too many ticks
        }
        // calculate new scale
        final double newScale = calculateNewScale(length, minRounded, maxRounded);
        // return new range
        return new Object[]{minRounded, maxRounded, tickUnitRounded, newScale, formatter};
    }

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<DateAxis, Number> TICK_UNIT =
                new CssMetaData<DateAxis, Number>("-fx-tick-unit",
                        SizeConverter.getInstance(), 5.0) {

                    @Override
                    public boolean isSettable(DateAxis n) {
                        return n.tickUnit == null || !n.tickUnit.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(DateAxis n) {
                        return (StyleableProperty<Number>) n.tickUnitProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<CssMetaData<? extends Styleable, ?>>(ValueAxis.getClassCssMetaData());
            styleables.add(TICK_UNIT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    // -------------- INNER CLASSES ------------------------------------------------------------------------------------

    /**
     * Default number formatter for DateAxis, this stays in sync with auto-ranging and formats values appropriately.
     * You can wrap this formatter to add prefixes or suffixes;
     *
     * @since JavaFX 2.0
     */
    public static class DefaultFormatter extends StringConverter<Long> {
        private DateTimeStringConverter formatter;
        private String prefix = null;
        private String suffix = null;
        private Date tempDate = new Date();

        /**
         * Construct a DefaultFormatter for the given DateAxis
         *
         * @param axis The axis to format tick marks for
         */
        public DefaultFormatter(final DateAxis axis) {
            formatter = getFormatter(axis.currentFormatterProperty.get());
            final ChangeListener<Object> axisListener = (observable, oldValue, newValue) -> {
                formatter = getFormatter(axis.currentFormatterProperty.get());
                axis.requestAxisLayout();
            };
            axis.currentFormatterProperty.addListener(axisListener);
            axis.autoRangingProperty().addListener(axisListener);
        }

        /**
         * Construct a DefaultFormatter for the given DateAxis with a prefix and/or suffix.
         *
         * @param axis   The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can be null if not needed
         */
        public DefaultFormatter(DateAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        private DateTimeStringConverter getFormatter(String format) {
            if (!format.equals("")) {
                return new DateTimeStringConverter(format);
            } else {
                return new DateTimeStringConverter(DateTimeFormat.patternForStyle("SS", I18n.getInstance().getLocale()));
            }
        }

        /**
         * Converts the object provided into its string form.
         * Format of the returned string is defined by this converter.
         *
         * @return a string representation of the object passed in.
         * @see StringConverter#toString
         */
        @Override
        public String toString(Long object) {
            return toString(object, formatter);
        }

        private String toString(Long object, String numFormatter) {
            if (numFormatter == null || numFormatter.isEmpty()) {
                return toString(object, formatter);
            } else {
                return toString(object, new DateTimeStringConverter(numFormatter));
            }
        }

        private String toString(Long object, DateTimeStringConverter formatter) {
            tempDate.setTime(object);
            if (prefix != null && suffix != null) {
                return prefix + formatter.toString(tempDate) + suffix;
            } else if (prefix != null) {
                return prefix + formatter.toString(tempDate);
            } else if (suffix != null) {
                return formatter.toString(tempDate) + suffix;
            } else {
                return formatter.toString(tempDate);
            }
        }

        /**
         * Converts the string provided into a Number defined by the this converter.
         * Format of the string and type of the resulting object is defined by this converter.
         *
         * @return a Number representation of the string passed in.
         * @see StringConverter#toString
         */
        @Override
        public Long fromString(String string) {
            int prefixLength = (prefix == null) ? 0 : prefix.length();
            int suffixLength = (suffix == null) ? 0 : suffix.length();
            return formatter.fromString(string.substring(prefixLength, string.length() - suffixLength)).getTime();
        }
    }

}