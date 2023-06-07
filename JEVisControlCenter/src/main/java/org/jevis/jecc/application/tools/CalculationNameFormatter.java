package org.jevis.jecc.application.tools;

import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.CalcMethods;
import org.jevis.jecc.application.application.I18nWS;

import java.util.regex.Pattern;

public class CalculationNameFormatter {

    public final static String[] expressions = new String[]{"(", ")", "+", "-", "*", "#", "/", "%", "=", "!", "<", ">", "&", "|", " ", ",", ".", ";", ":", "'"};
    public final static String replacement = "_";


    /**
     * Replace all Chars which are keywords in the JEVal expression.
     *
     * @param name
     * @return
     */
    public static String formatInputVariable(String name) {
        for (String exp : expressions) {
            name = name.replaceAll(Pattern.quote(exp), replacement);
        }
        name = replaceUmlaut(name);
        name = removeDuplicatedReplacements(name);
        name = removeNumbersInFront(name);
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
     * Create an name suggestion based on the attribute
     *
     * @param target
     * @return
     * @throws JEVisException
     */
    public static String createVariableName(JEVisAttribute target) throws JEVisException {
        return createVariableName(target.getObject());
    }

    /**
     * Create an name suggestion based on the attribute
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
        name = formatInputVariable(name);

        return name;

    }

    public static String createVariableName(JEVisClass target, JEVisType attribute) throws JEVisException {
        String name = I18nWS.getInstance().getClassName(target) + I18nWS.getInstance().getTypeName(target.getName(), attribute.getName());

        name = formatInputVariable(name);

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
