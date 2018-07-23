package org.jevis.application.tools;

import java.util.regex.Pattern;

public class CalculationNameFormater {

    public final static String[] expressions = new String[]{"(",")","+","-","*","/","%","=","!","<",">","&","|"};

    /**
     * Replace all Chars which are keywords in the JEVal expression.
     * 
     * @param name
     * @return
     */
    public static String formatInputVariable(String name){
        for(String exp:expressions){
            name= name.replaceAll(Pattern.quote(exp)," ");
        }
        return name.trim();
    }

}
