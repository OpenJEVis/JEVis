package org.jevis.commons.constants;

public interface AlarmConstants {

    enum Operator {
        BIGGER, BIGGER_EQUALS, EQUALS, NOT_EQUALS, SMALLER, SMALLER_EQUALS;

        public static String getValue(Operator operator) {
            switch (operator) {
                case BIGGER:
                    return ">";
                case BIGGER_EQUALS:
                    return ">=";
                case EQUALS:
                    return "=";
                case NOT_EQUALS:
                    return "!=";
                case SMALLER:
                    return "<";
                case SMALLER_EQUALS:
                    return "<=";
                default:
                    return "";
            }
        }

        public static Operator parse(String operator) {
            switch (operator) {
                case "BIGGER":
                    return BIGGER;
                case "BIGGER_EQUALS":
                    return BIGGER_EQUALS;
                case "EQUALS":
                    return EQUALS;
                case "NOT_EQUALS":
                    return NOT_EQUALS;
                case "SMALLER":
                    return SMALLER;
                case "SMALLER_EQUALS":
                    return SMALLER_EQUALS;
                default:
                    return null;
            }
        }
    }
}
