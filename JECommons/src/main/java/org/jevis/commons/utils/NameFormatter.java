package org.jevis.commons.utils;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;

import java.util.regex.Pattern;

public class NameFormatter {

    public final static String[] expressions = new String[]{"(", ")", "+", "-", "*", "#", "/", "%", "=", "!", "<", ">", "&", "|", " ", ",", ".", ";", ":", "'"};
    public final static String replacement = "_";


    /**
     * Replace all Chars which are keywords in the JEVal expression.
     *
     * @param name
     * @return
     */
    public static String formatVariableText(String name) {
        for (String exp : expressions) {
            name = name.replaceAll(Pattern.quote(exp), replacement);
        }
        name = replaceUmlaut(name);
        name = removeDuplicatedReplacements(name);
        name = removeNumbersInFront(name);

        name = name.replaceAll("__", "_");

        return name.trim();
    }

    public static String formatNames(String name) {
        for (String exp : expressions) {
            name = name.replaceAll(Pattern.quote(exp), replacement);
        }
        name = replaceUmlaut(name);

        name = name.replaceAll("__", "_");

        return name.trim();
    }

    private static String removeNumbersInFront(String name) {
        String output = name;

        if (!name.isEmpty() && (output.charAt(0) >= '0' && output.charAt(0) <= '9')) {
            return removeNumbersInFront(output.substring(1));
        }
        return output;
    }

    /**
     * Create a name suggestion based on the attribute
     *
     * @param target
     * @return
     * @throws JEVisException
     */
    public static String createVariableName(JEVisAttribute target) throws JEVisException {
        return createVariableName(target.getObject());
    }

    /**
     * Create a name suggestion based on the attribute
     *
     * @param target
     * @return
     * @throws JEVisException
     */
    public static String createVariableName(JEVisObject target) throws JEVisException {
        String name = target.getLocalName(I18n.getInstance().getDefaultBundle().getLocale().getLanguage());
        if (target.getJEVisClassName().equals("Clean Data")) {
            JEVisObject firstParentalDataObject = CalcMethods.getFirstParentalDataObject(target);
            name = firstParentalDataObject.getLocalName(I18n.getInstance().getDefaultBundle().getLocale().getLanguage());
        }
        name = formatVariableText(name);

        return name;

    }

    /**
     * Replace replacement strings if they are behind each other
     * <p>
     * TODO: make an real implementation....
     *
     * @param input
     * @return
     */
    private static String removeDuplicatedReplacements(String input) {
        input = input.replaceAll(Pattern.quote(replacement + replacement + replacement + replacement + replacement), replacement);
        input = input.replaceAll(Pattern.quote(replacement + replacement + replacement + replacement), replacement);
        input = input.replaceAll(Pattern.quote(replacement + replacement + replacement), replacement);
        input = input.replaceAll(Pattern.quote(replacement + replacement), replacement);
        if (input.endsWith(replacement)) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    /**
     * Replaces all umlauts from an string.
     * TODO: other languages will have the same problem
     */
    private static String replaceUmlaut(String input) {

        //replace all lower Umlauts
        String output = input.replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        output = output.replace("Ü(?=[a-zäöüß ])", "Ue")
                .replace("Ö(?=[a-zäöüß ])", "Oe")
                .replace("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        output = output.replace("Ü", "UE")
                .replace("Ö", "OE")
                .replace("Ä", "AE");

        return output;
    }

}
