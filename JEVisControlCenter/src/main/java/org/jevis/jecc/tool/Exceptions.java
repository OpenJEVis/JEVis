package org.jevis.jecc.tool;

public class Exceptions {

    public static String toString(Exception ex) {
        String message = "";

        try {
            message += "\n" + ex.getMessage() + " at: ";

        } catch (Exception ex2) {

        }

        try {
            boolean foundFirstInJEVis = false;
            StackTraceElement[] stack = ex.getStackTrace();
            if (stack.length > 0) {
                for (StackTraceElement stackTraceElement : stack) {
                    if (stackTraceElement.getClassName().startsWith("org.jevis") && !foundFirstInJEVis) {
                        message += stackTraceElement.toString();
                        foundFirstInJEVis = true;
                    }
                }
                if (!foundFirstInJEVis) {
                    message += stack[0].toString();
                }

            }
        } catch (Exception ex3) {

        }

        return message;
    }
}
