package org.jevis.application.tools;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.List;
import java.util.regex.Pattern;

public class CalculationNameFormater {

    public final static String[] expressions = new String[]{"(", ")", "+", "-", "*", "/", "%", "=", "!", "<", ">", "&", "|", " "};
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
        return name.trim();
    }

    /**
     * Create an name suggestion based on the attribute
     *
     * @param target
     * @return
     * @throws JEVisException
     */
    public static String crateVarName(JEVisAttribute target) throws JEVisException {
        return crateVarName(target.getObject());

    }

    /**
     * Create an name suggestion based on the attribute
     *
     * @param target
     * @return
     * @throws JEVisException
     */
    public static String crateVarName(JEVisObject target) throws JEVisException {
        String name = target.getName();
        if (target.getJEVisClassName().equals("Clean Data")) {
            List<JEVisObject> targetObj = target.getParents();
            if (!targetObj.isEmpty()) {
                name = targetObj.get(0).getName();
            }
        }
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
