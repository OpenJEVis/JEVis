package org.jevis.jecc.plugin.charts;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.Bidi;

import static javafx.scene.control.OverrunStyle.*;

public class Utils {
    static final Text helper = new Text();
    static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
    static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
    static final String DEFAULT_TEXT = helper.getText();
//    static final TextLayout layout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();

    static String computeClippedText(Font font, String text, double width,
                                     OverrunStyle type, String ellipsisString) {
        if (font == null) {
            throw new IllegalArgumentException("Must specify a font");
        }
        OverrunStyle style = (type == null || type == CLIP) ? ELLIPSIS : type;
        final String ellipsis = (type == CLIP) ? "" : ellipsisString;
        // if the text is empty or null or no ellipsis, then it always fits
        if (text == null || "".equals(text)) {
            return text;
        }
        // if the string width is < the available width, then it fits and
        // doesn't need to be clipped.  We use a double point comparison
        // of 0.001 (1/1000th of a pixel) to account for any numerical
        // discrepancies introduced when the available width was calculated.
        // MenuItemSkinBase.doLayout, for example, does a number of double
        // point operations when computing the available width.
        final double stringWidth = computeTextWidth(font, text, 0);
        if (stringWidth - width < 0.0010F) {
            return text;
        }
        // the width used by the ellipsis string
        final double ellipsisWidth = computeTextWidth(font, ellipsis, 0);
        // the available maximum width to fit chars into. This is essentially
        // the width minus the space required for the ellipsis string
        final double availableWidth = width - ellipsisWidth;

        if (width < ellipsisWidth) {
            // The ellipsis doesn't fit.
            return "";
        }

        // if we got here, then we must clip the text with an ellipsis.
        // this can be pretty expensive depending on whether "complex" text
        // layout needs to be taken into account. So each ellipsis option has
        // to take into account two code paths: the easy way and the correct
        // way. This is flagged by the "complexLayout" boolean
        // TODO make sure this function call takes into account ligatures, kerning,
        // and such as that will change the layout characteristics of the text
        // and will require a full complex layout
        // TODO since we don't have all the stuff available in FX to determine
        // complex text, I'm going to for now assume complex text is always false.
        final boolean complexLayout = false;
        //requiresComplexLayout(font, text);

        // generally all we want to do is count characters and add their widths.
        // For ellipsis that breaks on words, we do NOT want to include any
        // hanging whitespace.
        if (style == ELLIPSIS ||
                style == WORD_ELLIPSIS ||
                style == LEADING_ELLIPSIS ||
                style == LEADING_WORD_ELLIPSIS) {

            final boolean wordTrim =
                    (style == WORD_ELLIPSIS || style == LEADING_WORD_ELLIPSIS);
            String substring;
            if (complexLayout) {
                //            AttributedString a = new AttributedString(text);
                //            LineBreakMeasurer m = new LineBreakMeasurer(a.getIterator(), frc);
                //            substring = text.substring(0, m.nextOffset((double)availableWidth));
            } else {
                // RT-23458: Use a faster algorithm for the most common case
                // where truncation happens at the end, i.e. for ELLIPSIS and
                // CLIP, but not for other cases such as WORD_ELLIPSIS, etc.
                if (style == ELLIPSIS && !new Bidi(text, Bidi.DIRECTION_LEFT_TO_RIGHT).isMixed()) {
                    int hit = computeTruncationIndex(font, text, width - ellipsisWidth);
                    if (hit < 0 || hit >= text.length()) {
                        return text;
                    } else {
                        return text.substring(0, hit) + ellipsis;
                    }
                }

                // simply total up the widths of all chars to determine how many
                // will fit in the available space. Remember the last whitespace
                // encountered so that if we're breaking on words we can trim
                // and omit it.
                double total = 0.0F;
                int whitespaceIndex = -1;
                // at the termination of the loop, index will be one past the
                // end of the substring
                int index = 0;
                int start = (style == LEADING_ELLIPSIS || style == LEADING_WORD_ELLIPSIS) ? (text.length() - 1) : (0);
                int end = (start == 0) ? (text.length() - 1) : 0;
                int stepValue = (start == 0) ? 1 : -1;
                boolean done = (start == 0) ? start > end : start < end;
                for (int i = start; !done; i += stepValue) {
                    index = i;
                    char c = text.charAt(index);
                    total = computeTextWidth(font,
                            (start == 0) ? text.substring(0, i + 1)
                                    : text.substring(i, start + 1),
                            0);
                    if (Character.isWhitespace(c)) {
                        whitespaceIndex = index;
                    }
                    if (total > availableWidth) {
                        break;
                    }
                    done = start == 0 ? i >= end : i <= end;
                }
                final boolean fullTrim = !wordTrim || whitespaceIndex == -1;
                substring = (start == 0) ?
                        (text.substring(0, fullTrim ? index : whitespaceIndex)) :
                        (text.substring((fullTrim ? index : whitespaceIndex) + 1));
                assert (!text.equals(substring));
            }
            if (style == ELLIPSIS || style == WORD_ELLIPSIS) {
                return substring + ellipsis;
            } else {
                //style is LEADING_ELLIPSIS or LEADING_WORD_ELLIPSIS
                return ellipsis + substring;
            }
        } else {
            // these two indexes are INCLUSIVE not exclusive
            int leadingIndex = 0;
            int trailingIndex = 0;
            int leadingWhitespace = -1;
            int trailingWhitespace = -1;
            // The complex case is going to be killer. What I have to do is
            // read all the chars from the left up to the leadingIndex,
            // and all the chars from the right up to the trailingIndex,
            // and sum those together to get my total. That is, I cannot have
            // a running total but must retotal the cummulative chars each time
            if (complexLayout) {
            } else /*            double leadingTotal = 0;
               double trailingTotal = 0;
               for (int i=0; i<text.length(); i++) {
               double total = computeStringWidth(metrics, text.substring(0, i));
               if (total + trailingTotal > availableWidth) break;
               leadingIndex = i;
               leadingTotal = total;
               if (Character.isWhitespace(text.charAt(i))) leadingWhitespace = leadingIndex;

               int index = text.length() - (i + 1);
               total = computeStringWidth(metrics, text.substring(index - 1));
               if (total + leadingTotal > availableWidth) break;
               trailingIndex = index;
               trailingTotal = total;
               if (Character.isWhitespace(text.charAt(index))) trailingWhitespace = trailingIndex;
               }*/ {
                // either CENTER_ELLIPSIS or CENTER_WORD_ELLIPSIS
                // for this case I read one char on the left, then one on the end
                // then second on the left, then second from the end, etc until
                // I have used up all the availableWidth. At that point, I trim
                // the string twice: once from the start to firstIndex, and
                // once from secondIndex to the end. I then insert the ellipsis
                // between the two.
                leadingIndex = -1;
                trailingIndex = -1;
                double total = 0.0F;
                for (int i = 0; i <= text.length() - 1; i++) {
                    char c = text.charAt(i);
                    //total += metrics.charWidth(c);
                    total += computeTextWidth(font, "" + c, 0);
                    if (total > availableWidth) {
                        break;
                    }
                    leadingIndex = i;
                    if (Character.isWhitespace(c)) {
                        leadingWhitespace = leadingIndex;
                    }
                    int index = text.length() - 1 - i;
                    c = text.charAt(index);
                    //total += metrics.charWidth(c);
                    total += computeTextWidth(font, "" + c, 0);
                    if (total > availableWidth) {
                        break;
                    }
                    trailingIndex = index;
                    if (Character.isWhitespace(c)) {
                        trailingWhitespace = trailingIndex;
                    }
                }
            }
            if (leadingIndex < 0) {
                return ellipsis;
            }
            if (style == CENTER_ELLIPSIS) {
                if (trailingIndex < 0) {
                    return text.substring(0, leadingIndex + 1) + ellipsis;
                }
                return text.substring(0, leadingIndex + 1) + ellipsis + text.substring(trailingIndex);
            } else {
                boolean leadingIndexIsLastLetterInWord =
                        Character.isWhitespace(text.charAt(leadingIndex + 1));
                int index = (leadingWhitespace == -1 || leadingIndexIsLastLetterInWord) ? (leadingIndex + 1) : (leadingWhitespace);
                String leading = text.substring(0, index);
                if (trailingIndex < 0) {
                    return leading + ellipsis;
                }
                boolean trailingIndexIsFirstLetterInWord =
                        Character.isWhitespace(text.charAt(trailingIndex - 1));
                index = (trailingWhitespace == -1 || trailingIndexIsFirstLetterInWord) ? (trailingIndex) : (trailingWhitespace + 1);
                String trailing = text.substring(index);
                return leading + ellipsis + trailing;
            }
        }
    }

    static double computeTextWidth(Font font, String string, double wrappingWidth) {

        //TODO JFX17 Testen
        final Text text = new Text(string);
        text.applyCss();
        final double width = text.getLayoutBounds().getWidth();
        return width;
    }

    static int computeTruncationIndex(Font font, String text, double width) {
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth(0);
        helper.setLineSpacing(0);
        // The -2 is a fudge to make sure the result more often matches
        // what we get from using computeTextWidth instead. It's not yet
        // clear what causes the small discrepancies.
        Bounds bounds = helper.getLayoutBounds();
        Point2D endPoint = new Point2D(width - 2, bounds.getMinY() + bounds.getHeight() / 2);
        //TODO JFX17 Testen
        final int index = helper.hitTest(endPoint).getCharIndex();
        // RESTORE STATE
        helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        helper.setLineSpacing(DEFAULT_LINE_SPACING);
        helper.setText(DEFAULT_TEXT);
        return index;
    }
}
