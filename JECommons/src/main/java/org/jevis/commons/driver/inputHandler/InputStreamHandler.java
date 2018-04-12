/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver.inputHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author bf
 */
public class InputStreamHandler extends InputHandler {

    public InputStreamHandler(InputStream input, Charset charset) {
        super(input, charset);
    }

    @Override
    public void convertInput() {
        try {

            //byte[] buffer = new byte[1024];
            //int len;
//        try {
//            while ((len = (InputStream) _rawInput.read(buffer)) > -1) {
//                baos.write(buffer, 0, len);
//            }
//            baos.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(this.getClass().getName()).log(Level.ERROR, ex.getMessage());
//        }
            byte[] bytes = IOUtils.toByteArray((InputStream)_rawInput);

            InputStream zippedCopy = new ByteArrayInputStream(bytes);
            InputStream unzipedCopy = new ByteArrayInputStream(bytes);
            try {
                boolean isZiped = false;
                ZipInputStream zin = new ZipInputStream((InputStream) zippedCopy);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    isZiped = true;
                    StringBuilder sb = new StringBuilder();
                    String[] pathStream = getPathTokens(_filePattern);
                    boolean match = false;

                    if (containsTokens(_filePattern)) {
                        DateTime folderTime = getFolderTime(ze.getName(), pathStream);
                        String regExDateString = returnDateStringRegEx(_filePattern);
                        boolean isLater = folderTime.isAfter(_lastReadout);
                        if (isLater) {
                            match = matchRegEx(regExDateString, _filePattern);
                        }
                    } else {
                        match = matchRegEx(ze.getName(), _filePattern);
                    }
                    if (match) {
                        List<String> tmp = new ArrayList<String>();
                        for (int c = zin.read(); c != -1; c = zin.read()) {
                            sb.append((char) c);
                        }
                        _inputStream.add(new ByteArrayInputStream(sb.toString().getBytes()));
                    }
                    zin.closeEntry();
                }
                IOUtils.closeQuietly(zin);
                if (!isZiped) {
                    _inputStream.add(new BufferedInputStream(unzipedCopy));
                }
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.ERROR, ex.getMessage());
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(InputStreamHandler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private static String returnDateStringRegEx(String nextToken) {
        String[] substringsBetween = StringUtils.substringsBetween(nextToken, "${D:", "}");
        for (int i = 0; i < substringsBetween.length; i++) {
            nextToken = nextToken.replace("${D:" + substringsBetween[i] + "}", ".{" + substringsBetween[i].length() + "}");
        }
//        Pattern p = Pattern.compile(nextToken);
//        Matcher m = p.matcher(currentFolder);
//        return m.matches();
        return nextToken;
    }

    public static boolean matchRegEx(String current, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(current);
        return m.matches();
    }

    private DateTime getFolderTime(String name, String[] pathStream) {
        String compactDateString = getCompactDateString(name, pathStream);
        String compactDataFormatString = getCompactDateFormatString(name, pathStream);

        DateTimeFormatter dtf = DateTimeFormat.forPattern(compactDataFormatString);

        DateTime parseDateTime = dtf.parseDateTime(compactDateString);
        if (parseDateTime.year().get() == parseDateTime.year().getMinimumValue()) {
            parseDateTime = parseDateTime.year().withMaximumValue();
        }
        if (parseDateTime.monthOfYear().get() == parseDateTime.monthOfYear().getMinimumValue()) {
            parseDateTime = parseDateTime.monthOfYear().withMaximumValue();
        }
        if (parseDateTime.dayOfMonth().get() == parseDateTime.dayOfMonth().getMinimumValue()) {
            parseDateTime = parseDateTime.dayOfMonth().withMaximumValue();
        }
        if (parseDateTime.hourOfDay().get() == parseDateTime.hourOfDay().getMinimumValue()) {
            parseDateTime = parseDateTime.hourOfDay().withMaximumValue();
        }
        if (parseDateTime.minuteOfHour().get() == parseDateTime.minuteOfHour().getMinimumValue()) {
            parseDateTime = parseDateTime.minuteOfHour().withMaximumValue();
        }
        if (parseDateTime.secondOfMinute().get() == parseDateTime.secondOfMinute().getMinimumValue()) {
            parseDateTime = parseDateTime.secondOfMinute().withMaximumValue();
        }
        if (parseDateTime.millisOfSecond().get() == parseDateTime.millisOfSecond().getMinimumValue()) {
            parseDateTime = parseDateTime.millisOfSecond().withMaximumValue();
        }
        return parseDateTime;
    }

    private String getCompactDateString(String name, String[] pathStream) {
        String[] realTokens = StringUtils.split(name, "/");
        String compactDateString = null;
        for (int i = 0; i < realTokens.length; i++) {
            String currentString = pathStream[i];
            if (currentString.contains("${D:")) {
                int startindex = currentString.indexOf("${D:");
                int endindex = currentString.indexOf("}");
                if (compactDateString == null) {
                    compactDateString = realTokens[i].substring(startindex, endindex - 4);
                } else {
                    compactDateString += " " + realTokens[i].substring(startindex, endindex - 4);
                }
            }
        }
        return compactDateString;
    }

    private String getCompactDateFormatString(String name, String[] pathStream) {
        String[] realTokens = StringUtils.split(name, "/");
        String compactDateString = null;
        //contains more than one date token?
        for (int i = 0; i < realTokens.length; i++) {
            String currentString = pathStream[i];
            if (currentString.contains("${")) {
                int startindex = currentString.indexOf("${");
                int endindex = currentString.indexOf("}");
                if (compactDateString == null) {
                    compactDateString = currentString.substring(startindex + 4, endindex);
                } else {
                    compactDateString += " " + currentString.substring(startindex + 4, endindex);
                }
            }
        }
        return compactDateString;
    }

    private String[] getPathTokens(String filePath) {
//        List<String> tokens = new ArrayList<String>();
        //        filePath.substring("\\$\\{","\\}");
        String[] tokens = StringUtils.split(filePath, "/");
//         String[] tokens = filePath.trim().split("\\%");
//        for (int i = 0; i < tokens.length; i++) {
//            System.out.println(tokens[i]);
//        }
        return tokens;
    }

    private static Boolean containsTokens(String path) {
        if (path.contains("${")) {
            return true;
        } else {
            return false;
        }
    }
}
