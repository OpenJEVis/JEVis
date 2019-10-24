package org.jevis.commons.utils;

public class PrettyError {

    public static String getJEVisLineFilter(Exception ex) {
        try {
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                if (stackTraceElement.getFileName().startsWith("org.jevis")) {
                    String errorMsg = String.format("[%s] %s:%s - %s"
                            , stackTraceElement.getLineNumber()
                            , stackTraceElement.getClassName()
                            , stackTraceElement.getMethodName()
                            , ex.getMessage());
                    return errorMsg;
                }
            }
            return ex.getMessage();
        } catch (Exception exOut) {
            return ex.getMessage();
        }
    }
}
