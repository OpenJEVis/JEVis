package org.jevis.jeconfig.application.Chart.ChartElements;

import de.gsi.chart.axes.AxisLabelOverlapPolicy;
import de.gsi.chart.axes.spi.AxisRange;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.axes.spi.TickMark;
import de.gsi.chart.ui.geometry.Side;
import de.jollyday.Holiday;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.tools.Holidays;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.Set;

public class DefaultDateAxis extends DefaultNumericAxis {

    private static JEVisObject site = null;

    public DefaultDateAxis() {
        super();
    }

    @Override
    protected AxisRange autoRange(final double minValue, final double maxValue, final double length,
                                  final double labelSize) {
        double min = minValue > 0 && isForceZeroInRange() ? 0 : minValue;
        if (isLogAxis && minValue <= 0) {
            min = DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE;
            isUpdating = true;
            // TODO: check w.r.t. inverted axis (lower <-> upper bound exchange)
            setMin(DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE);
            isUpdating = false;
        }

        double max = maxValue < 0 && isForceZeroInRange() ? 0 : maxValue;

        if (isTimeAxis && minValue == maxValue) {
            //TODO check why column charts with one sample don't work properly
        }

        final double padding = DefaultNumericAxis.getEffectiveRange(min, max) * getAutoRangePadding();
        final double paddingScale = 1.0 + getAutoRangePadding();
        final double paddedMin = isLogAxis ? minValue / paddingScale
                : DefaultNumericAxis.clampBoundToZero(min - padding, min);
        final double paddedMax = isLogAxis ? maxValue * paddingScale
                : DefaultNumericAxis.clampBoundToZero(max + padding, max);

        return computeRange(paddedMin, paddedMax, length, labelSize);
    }

    protected static void drawTickMarkLabel(final GraphicsContext gc, final double x, final double y,
                                            final double scaleFont, final TickMark tickMark) {

        boolean isHoliday = false;
        try {
            Double value = tickMark.getValue() * 1000d;
            DateTime dateTime = new DateTime(value.longValue());
            String toolTipString = "";
            if (Holidays.getDefaultHolidayManager().isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale()))
                    || (Holidays.getSiteHolidayManager(site) != null && Holidays.getSiteHolidayManager(site).isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale()), Holidays.getStateCode()))
                    || (Holidays.getCustomHolidayManager(site) != null && Holidays.getCustomHolidayManager(site).isHoliday(dateTime.toCalendar(I18n.getInstance().getLocale())))) {
                LocalDate localDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());

                Set<Holiday> holidays = Holidays.getDefaultHolidayManager().getHolidays(localDate, localDate, "");
                if (Holidays.getSiteHolidayManager(site) != null) {
                    holidays.addAll(Holidays.getSiteHolidayManager(site).getHolidays(localDate, localDate, Holidays.getStateCode()));
                }

                if (Holidays.getCustomHolidayManager(site) != null) {
                    holidays.addAll(Holidays.getCustomHolidayManager(site).getHolidays(localDate, localDate, Holidays.getStateCode()));
                }

                for (Holiday holiday : holidays) {
                    toolTipString = holiday.getDescription(I18n.getInstance().getLocale());
                }
            }
            if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7 || !toolTipString.equals("")) {
//                if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7) {
//                    if (toolTipString.equals("")) {
//                        toolTipString = DateTimeFormat.forPattern("EEEE").print(dateTime);
//                    } else {
//                        toolTipString += ", " + dateTime.toString("dddd");
//                    }
//                }
                isHoliday = true;
//                if (!toolTipString.equals("")) {
//                    Tooltip tooltip = new TooltiptoolTipString);
//                    Tooltip.install(tick.textNode, tooltip);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gc.save();

        gc.setFont(tickMark.getFont());
        if (!isHoliday) {
            gc.setFill(Color.GRAY);
        } else {
            gc.setFill(Color.BLACK);
        }
        gc.setTextAlign(tickMark.getTextAlignment());
        gc.setTextBaseline(tickMark.getTextOrigin());

        gc.translate(x, y);
        if (tickMark.getRotate() != 0.0) {
            gc.rotate(tickMark.getRotate());
        }
        gc.setGlobalAlpha(tickMark.getOpacity());
        if (scaleFont != 1.0) {
            gc.scale(scaleFont, 1.0);
        }

        gc.fillText(tickMark.getText(), 0, 0);
        // gc.fillText(tickMark.getText(), x, y);C
        gc.restore();

    }

    @Override
    protected void drawTickLabels(GraphicsContext gc, double axisWidth, double axisHeight, ObservableList<TickMark> tickMarks, double tickLength) {
        if ((tickLength <= 0) || tickMarks.isEmpty()) {
            return;
        }
        final double paddingX = getSide().isHorizontal() ? getAxisPadding() : 0.0;
        final double paddingY = getSide().isVertical() ? getAxisPadding() : 0.0;
        // for relative positioning of axes drawn on top of the main canvas
        final double axisCentre = getCenterAxisPosition();
        final AxisLabelOverlapPolicy overlapPolicy = getOverlapPolicy();
        final double tickLabelGap = getTickLabelGap();

        // save css-styled label parameters
        gc.save();
        // N.B. important: translate by padding ie. canvas is +padding larger on
        // all size compared to region
        gc.translate(paddingX, paddingY);
        // N.B. streams, filter, and forEach statements have been evaluated and
        // appear to give no (or negative) performance for little/arguable
        // readability improvement (CG complexity from 40 -> 28, but mere
        // numerical number and not actual readability), thus sticking to 'for'
        // loops

        final TickMark firstTick = tickMarks.get(0);
        gc.setGlobalAlpha(firstTick.getOpacity());
        int counter = ((int) firstTick.getValue()) % 2;

        switch (getSide()) {
            case LEFT:
                for (final TickMark tickMark : tickMarks) {
                    final double position = tickMark.getPosition();
                    if (!tickMark.isVisible()) {
                        // skip invisible labels
                        continue;
                    }

                    double x = axisWidth - tickLength - tickLabelGap;
                    final double y = position;
                    switch (overlapPolicy) {
                        case DO_NOTHING:
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case SHIFT_ALT:
                            if (isLabelOverlapping()) {
                                x -= ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            }
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case FORCED_SHIFT_ALT:
                            x -= ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case NARROW_FONT:
                        case SKIP_ALT:
                        default:
                            if (((counter % 2) == 0) || !isLabelOverlapping() || scaleFont < MAX_NARROW_FONT_SCALE) {
                                drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            }
                            break;
                    }
                    counter++;
                }
                break;

            case RIGHT:
            case CENTER_VER:
                for (final TickMark tickMark : tickMarks) {
                    final double position = tickMark.getPosition();
                    if (!tickMark.isVisible()) {
                        // skip invisible labels
                        continue;
                    }

                    double x = tickLength + tickLabelGap;
                    final double y = position;
                    if (getSide().equals(Side.CENTER_VER)) {
                        // additional special offset for horizontal centre axis
                        x += axisCentre * axisWidth;
                    }
                    switch (overlapPolicy) {
                        case DO_NOTHING:
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case SHIFT_ALT:
                            if (isLabelOverlapping()) {
                                x += ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            }
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case FORCED_SHIFT_ALT:
                            x += ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case NARROW_FONT:
                        case SKIP_ALT:
                        default:
                            if (((counter % 2) == 0) || !isLabelOverlapping() || scaleFont < MAX_NARROW_FONT_SCALE) {
                                drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            }
                            break;
                    }
                    counter++;
                }
                break;

            case TOP:
                for (final TickMark tickMark : tickMarks) {
                    final double position = tickMark.getPosition();
                    if (!tickMark.isVisible()) {
                        // skip invisible labels
                        continue;
                    }

                    final double x = position;
                    double y = axisHeight - tickLength - tickLabelGap;
                    switch (overlapPolicy) {
                        case DO_NOTHING:
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case SHIFT_ALT:
                            if (isLabelOverlapping()) {
                                y -= ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            }
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case FORCED_SHIFT_ALT:
                            y -= ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case NARROW_FONT:
                        case SKIP_ALT:
                        default:
                            if (((counter % 2) == 0) || !isLabelOverlapping() || scaleFont < MAX_NARROW_FONT_SCALE) {
                                drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            }
                            break;
                    }
                    counter++;
                }
                break;

            case BOTTOM:
            case CENTER_HOR:
                for (final TickMark tickMark : tickMarks) {
                    final double position = tickMark.getPosition();
                    if (!tickMark.isVisible()) {
                        // skip invisible labels
                        continue;
                    }

                    final double x = position;
                    double y = tickLength + tickLabelGap;
                    if (getSide().equals(Side.CENTER_HOR)) {
                        // additional special offset for horizontal centre axis
                        y += axisCentre * axisHeight;
                    }

                    switch (overlapPolicy) {
                        case DO_NOTHING:
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case SHIFT_ALT:
                            if (isLabelOverlapping()) {
                                y += ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            }
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case FORCED_SHIFT_ALT:
                            y += ((counter % 2) * tickLabelGap) + ((counter % 2) * tickMark.getFont().getSize());
                            drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            break;
                        case NARROW_FONT:
                        case SKIP_ALT:
                        default:
                            if (((counter % 2) == 0) || !isLabelOverlapping() || scaleFont < MAX_NARROW_FONT_SCALE) {
                                drawTickMarkLabel(gc, x, y, scaleFont, tickMark);
                            }
                            break;
                    }
                    counter++;
                }
                break;
            default:
                break;
        }

        gc.restore();
    }

    public static void setSite(JEVisObject site) {
        DefaultDateAxis.site = site;
    }
}
