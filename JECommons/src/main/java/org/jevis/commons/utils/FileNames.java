package org.jevis.commons.utils;

public class FileNames {

    /* windows filenames
    https://docs.microsoft.com/de-de/windows/win32/fileio/naming-a-file?redirectedfrom=MSDN
     */

    /**
     * Replace invalid chars in a filename
     *
     * @param name
     * @return
     */
    public static String fixName(String name) {
        name = name.replaceAll("<", "_");
        name = name.replaceAll(">", "_");
        name = name.replaceAll(":", "_");
        name = name.replaceAll("\"", "_");
        name = name.replaceAll("\\|", "_");
        name = name.replaceAll("\\?", "_");

        return name;
    }
}
