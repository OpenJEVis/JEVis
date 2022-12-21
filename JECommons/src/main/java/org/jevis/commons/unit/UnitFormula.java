/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.unit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisUnit;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitFormula {
    private static final Logger logger = LogManager.getLogger(UnitFormula.class);

    public UnitFormula(String formula, String label) {
        inputFormula = formula;
//        logger.info("----------------------new Part: " + formula);
        if (isValid(formula)) {
            String formulaWithoutPunctuation = formula;
            if (hasSubParts(formula)) {

                int lastPart = 0;
                for (int i = 0; i < countParts(formula); i++) {
                    int[] range = getNextSubPartRange(formula);
//                    logger.info("next Range: " + Arrays.toString(range));
                    UnitFormula upart = getNextSubPart(formula, range[0] + 1, range[1] - 1);
                    _parts.add(upart);
//                    logger.info("remove: " + upart.getInputFormula());
                    formulaWithoutPunctuation = StringUtils.replace(formulaWithoutPunctuation, upart.getInputFormula(), upart.getUnit().toString());
                }

            }
            if (parseFunction(formulaWithoutPunctuation)) {
//                logger.info("Build complex unit: ");
                _unit = buildUnit(formulaWithoutPunctuation);
            } else {
//                logger.info("Build simple unitfrom: " + formulaWithoutPunctuation);
                _unit = Unit.valueOf(formulaWithoutPunctuation);
            }

        } else {
//            logger.info("Forma is not valid");
        }
    }

    //    private Unit buildFactorUnit(Factor factor, Unit unit2){
//        switch(factor){
//
//        }
//
//    }
    private Unit buildUnit(String formula) {
        String seperator = "\\" + TIMES + "\\" + DEVIDED + "\\" + MINUS + "\\" + PLUS;
        String[] parts = StringUtils.split(formula, seperator);
//        logger.info("parts: " + Arrays.toString(parts));
        if (parts.length == 2) {

            boolean isNumber = false;
            if (parts[0].matches("[0-9]+") && !parts[1].matches("[0-9]+")) {
                isNumber = true;
                String tmp0 = parts[0];
                String tmp1 = parts[1];
                parts[0] = tmp1;
                parts[1] = tmp0;
            }
            if (parts[1].matches("[0-9]+")) {
                isNumber = true;
            }

            Unit u0 = Unit.valueOf(parts[0]);
//            Unit u2 = Unit.valueOf(parts[1]);
            if (formula.contains(TIMES)) {
                if (isNumber) {
                    return u0.times(Double.parseDouble(parts[1]));
                } else {
                    return u0.times(Unit.valueOf(parts[1]));
                }

            } else if (formula.contains(DEVIDED)) {
                if (isNumber) {
                    return u0.divide(Double.parseDouble(parts[1]));
                } else {
                    return u0.divide(Unit.valueOf(parts[1]));
                }
            } else if (formula.contains(MINUS)) {
//                u1.plus(u2);
            } else if (formula.contains(PLUS)) {
//                u1.times(u2);
            }
            logger.info("Building uning faild");
            return Unit.ONE;
        } else {
            logger.info("Error part is not 2 long");
            return Unit.ONE;
        }

    }

    public static final String PUNCTUATION_START = "(";
    public static final String PUNCTUATION_END = ")";

    public static final String TIMES = "*";
    public static final String DEVIDED = "/";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String LABEL = "=";

    private Function _function = Function.NONE;
    private final List<UnitFormula> _parts = new ArrayList<UnitFormula>();

    private int _lastPart = 0;
    private Unit _unit = Unit.ONE;
    private String inputFormula = "";

    public Unit getUnit() {
//        logger.info("Parsed unit: " + _unit);
        return _unit;
    }

    public boolean hasPrefix() {
        return false;
    }

    public List<UnitFormula> getParts() {
        return _parts;
    }

    public Function getFunction() {
        return _function;
    }

    public String getInputFormula() {
        return inputFormula;
    }

    private UnitFormula getNextSubPart(String formula, int from, int to) {
//        logger.info("split: " + formula);
//        int start = StringUtils.indexOf(formula, PUNCTUATION_START);//start?
//        int end = getPartEnd(formula, _lastPart);

//        logger.info("range: " + start + " " + end);
        String subString = StringUtils.substring(formula, from, to);
//        logger.info("substring: " + subString);
        UnitFormula part = new UnitFormula(subString, null);

        return part;
    }

    public static Prefix parsePrefix(String prefixString) {
        try {
            Integer i = Integer.parseInt(prefixString);

            if (i == 1000) {
                return Prefix.KILO;
            }
            return Prefix.NUMBER;

        } catch (NumberFormatException nfe) {
            return Prefix.NOTANUMBER;
        }
    }

    private int getPartEnd(String formula, int start) {
        int startCount = 0;
        int endCount = 0;

        for (int i = start; i < formula.length(); i++) {
//            logger.info("is klamme: " + formula.charAt(i));
            if (formula.charAt(i) == PUNCTUATION_START.charAt(0)) {
                startCount++;
            } else if (formula.charAt(i) == PUNCTUATION_END.charAt(0)) {
                endCount++;
            }

//            logger.info("check: " + startCount + " " + endCount);
            if (endCount == startCount) {
//                logger.info("return i: " + i);
                return i + 1;
            }
        }

        return -1;
    }

    public String printUnit() {
        return UnitManager.getInstance().format(getUnit());
    }

    private int[] getNextSubPartRange(String formula) {
        int start = StringUtils.indexOf(formula, PUNCTUATION_START, _lastPart);
        int end = getPartEnd(formula, _lastPart);

        _lastPart = end;
        int[] range = {start, end};
        return range;

    }

    private boolean hasSubParts(String formula) {

        if (StringUtils.contains(formula, PUNCTUATION_START) || StringUtils.contains(formula, PUNCTUATION_END)) {
//            logger.info("hasPuctuation");
            int counterStart = 0;
            int counterEnd = 0;
            for (int i = 0; i < formula.length(); i++) {
                if (formula.charAt(i) == PUNCTUATION_START.charAt(0)) {
                    counterStart++;
                } else if (formula.charAt(i) == PUNCTUATION_END.charAt(0)) {
                    counterEnd++;
                }
            }
//            logger.info("count: " + counterStart + " " + counterEnd);

            //Check if this is the outer punctuation of this formula
            return counterStart != 1 || formula.charAt(0) == PUNCTUATION_START.charAt(0);

        }

//        logger.info("hasNOSubParts");
        return false;
    }

    private int countParts(String formula) {
        int counterStart = 0;
        for (int i = 0; i < formula.length(); i++) {
            if (formula.charAt(i) == PUNCTUATION_START.charAt(0)) {
                counterStart++;
            }
        }
        return counterStart;
    }

    private boolean isValid(String formula) {
        if (StringUtils.contains(formula, PUNCTUATION_START) || StringUtils.contains(formula, PUNCTUATION_END)) {
            int counterStart = 0;
            int counterEnd = 0;
            for (int i = 0; i < formula.length(); i++) {
                if (formula.charAt(i) == PUNCTUATION_START.charAt(0)) {
                    counterStart++;
                } else if (formula.charAt(i) == PUNCTUATION_END.charAt(0)) {
                    counterEnd++;
                }
            }

//            logger.info("isValid: " + counterStart + " " + counterEnd);
            if (counterStart != counterEnd) {
                logger.info("Not Valied, uneave count of punctuation");

                return false;
            }

        }

        return true;

    }

    private boolean parseFunction(String formula) {
        if (formula.contains(TIMES)) {
            _function = Function.TIMES;
            return true;
        } else if (formula.contains(DEVIDED)) {
            _function = Function.DEVIDED;
            return true;
        } else if (formula.contains(MINUS)) {
            _function = Function.MINUS;
            return true;
        } else if (formula.contains(PLUS)) {
            _function = Function.PLUS;
            return true;
        }

        return false;

    }

    public enum Function {

        TIMES, DEVIDED, PLUS, MINUS, LABEL, NONE
    }

    public enum Prefix {

        NOTANUMBER, NUMBER, ZETTA, EXA, PETA, TERA, GIGA, MEGA, KILO, HECTO, DEKA, DECI, CENTI, MILLI, MICRO, NANOPICO, FEMTO, ATTO, ZEPTO, YOCTO
    }

    public static String unitToString(JEVisUnit unit) {
        String unitString = "";

        return unitString;
    }
}
