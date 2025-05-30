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
package org.jevis.commons.driver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Broder
 */
public class DataSourceHelper {
    private static final Logger logger = LogManager.getLogger(DataSourceHelper.class);

    public static void test() {

    }

    static public void doTrustToCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    logger.warn("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    public static List<String> getFTPMatchedFileNames(FTPClient fc, DateTime lastReadout, Long maxReadout, DateTimeZone timeZone, String filePath, boolean overwrite) {
        filePath = filePath.replace("\\", "/");
        String[] pathStream = getPathTokens(filePath);

        String startPath = "";
        if (filePath.startsWith("/")) {
            startPath = "/";
        }

        List<String> matchingPaths = getMatchingPaths(startPath, pathStream, new ArrayList<String>(), fc, lastReadout, maxReadout, new DateTimeFormatterBuilder());
//        logger.info("foldersize,"+matchingPaths.size());
        List<String> fileNames = new ArrayList<String>();

        if (matchingPaths.isEmpty()) {
            logger.error("Cant find suitable folder on the device");
            return fileNames;
        }

//        String fileName = null;
        String fileNameScheme = pathStream[pathStream.length - 1];
        String currentfolder = null;
        try {
            for (String folder : matchingPaths) {
                currentfolder = folder;
                fc.changeWorkingDirectory(folder);

                for (FTPFile file : fc.listFiles()) {
                    boolean match = false;
                    logger.debug(file.getName());

                    if (DataSourceHelper.containsTokens(fileNameScheme)) {
                        boolean matchDate = matchDateString(file.getName(), fileNameScheme);
                        DateTime folderTime = getFileTime(folder + file.getName(), pathStream);
                        boolean isLater = folderTime.isAfter(lastReadout);
                        boolean isBefore = true;
                        if (maxReadout != null) {
                            isBefore = folderTime.isBefore(lastReadout.plusSeconds(maxReadout.intValue()));
                        }
                        if (matchDate && isLater && isBefore) {
                            match = true;
                        }
                    } else {
                        Pattern p = Pattern.compile(fileNameScheme);
                        Matcher m = p.matcher(file.getName());
                        DateTime fileTime = Instant.ofEpochMilli(file.getTimestamp().getTimeInMillis()).toDateTime();
                        boolean isBefore = true;
                        if (maxReadout != null) {
                            isBefore = fileTime.isBefore(lastReadout.plusSeconds(maxReadout.intValue()));
                        }
                        match = m.matches() && isBefore;
                    }

                    if (!match) {
                        continue;
                    }

                    DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(timeZone);
                    String fileName = file.getName();
                    logger.debug("Filename: {} , getModificationTime: {}", fileName, fc.getModificationTime(folder + fileName));
                    String timePart = fc.getModificationTime(folder + fileName);
                    if (timePart == null || timePart.isEmpty()) {
                        logger.warn("no date in file: {}", fileName);
                    } else {
                        try {
                            logger.debug("parsing date: {}", timePart);
                            DateTime modificationTime = dateFormat.parseDateTime(timePart);
                            if (modificationTime.isBefore(lastReadout) && !overwrite) {
                                continue;
                            }
                        } catch (Exception ex) {
                            logger.error("cannot parse date in file: {}", ex, ex);
                        }
                    }

                    fileNames.add(folder + file.getName());
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error while searching a matching file, folder {}, filename {}", currentfolder, fileNameScheme, ex);
        }
        if (matchingPaths.isEmpty()) {
            logger.error("Cant find suitable files on the device");
        }
//        logger.info("filenamesize"+fileNames.size());
        return fileNames;
    }

    private static DateTime getFileTime(String name, String[] pathStream) {
        String compactDateString = getCompactDateString(name, pathStream);
        String compactDataFormatString = getCompactDateFormatString(name, pathStream);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(compactDataFormatString);
        DateTime parseDateTime = dtf.parseDateTime(compactDateString);
        return parseDateTime;
    }

    private static String getCompactDateString(String name, String[] pathStream) {
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

    private static String getCompactDateFormatString(String name, String[] pathStream) {
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

    private static String removeFolder(String fileName, String folder) {
        if (fileName.startsWith(folder)) {
            return fileName.substring(folder.length());
        }
        return fileName;
    }

    private static boolean matchDateString(String currentFolder, String nextToken) {
        String[] substringsBetween = StringUtils.substringsBetween(nextToken, "${D:", "}");
        for (int i = 0; i < substringsBetween.length; i++) {
            nextToken = nextToken.replace("${D:" + substringsBetween[i] + "}", ".{" + substringsBetween[i].length() + "}");
        }
        Pattern p = Pattern.compile(nextToken);
        Matcher m = p.matcher(currentFolder);
        return m.matches();
    }

    private static List<String> getMatchingPaths(String path, String[] pathStream, ArrayList<String> arrayList, FTPClient fc, DateTime lastReadout, Long maxReadout, DateTimeFormatterBuilder dtfbuilder) {
        int nextTokenPos = getPathTokens(path).length;
        if (nextTokenPos == pathStream.length - 1) {
            arrayList.add(path);
            return arrayList;
        }

        String nextToken = pathStream[nextTokenPos];
        String nextFolder = null;

        try {
            if (containsDateToken(nextToken)) {
                FTPFile[] listDirectories = fc.listFiles(path);
                if (fc.getReplyCode() == 522) {
                    // encrypt data channel and listFiles again
                    ((FTPSClient) fc).execPROT("P");
                    listDirectories = fc.listFiles(path);
                }
//                DateTimeFormatter ftmTemp = getDateFormatter(nextToken);
                for (FTPFile folder : listDirectories) {
                    if (!matchDateString(folder.getName(), nextToken)) {
                        continue;
                    }
//                    logger.info("listdir," + folder.getName());
//                    if (containsDate(folder.getName(), ftmTemp)) {
                    DateTime folderTime = getFolderTime(path + folder.getName() + "/", pathStream);

                    if (folderTime.isAfter(lastReadout)) {
                        nextFolder = folder.getName();
//                        logger.info("dateFolder," + nextFolder);
                        getMatchingPaths(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, maxReadout, dtfbuilder);
                    }
//                    }
                }
            } else if (nextToken.equals("(.*)")) {
                FTPFile[] listDirectories = fc.listDirectories(path);
                if (fc.getReplyCode() == 522) {
                    // encrypt data channel and listFiles again
                    ((FTPSClient) fc).execPROT("P");
                    listDirectories = fc.listFiles(path);
                }
                for (FTPFile folder : listDirectories) {
                    getMatchingPaths(path + folder.getName() + "/", pathStream, arrayList, fc, lastReadout, maxReadout, dtfbuilder);
                }
            } else {
                nextFolder = nextToken;
//                logger.info("normalFolder," + nextFolder);
                getMatchingPaths(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, maxReadout, dtfbuilder);
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return arrayList;
    }

    private static boolean containsDateToken(String string) {
        return string.contains("${D:");
    }

    public static String[] getPathTokens(String filePath) {
//        List<String> tokens = new ArrayList<String>();
        //        filePath.substring("\\$\\{","\\}");
        String[] tokens = StringUtils.split(filePath, "/");
//         String[] tokens = filePath.trim().split("\\%");
        for (int i = 0; i < tokens.length; i++) {
            logger.info(tokens[i]);
        }
        return tokens;
    }

    public static String replaceDateFrom(String template, DateTime date, DateTimeZone zone) {
        DateTimeFormatter dtf = getFromDateFormat(template);
        int startindex = template.indexOf("${DF:");
        int endindex = template.indexOf("}") + 1;
        String first = template.substring(0, startindex);
        String last = template.substring(endindex);
        return first + date.toString(dtf.withZone(zone)) + last;
    }

    public static String replaceDateUntil(String template, DateTime date, DateTimeZone zone) {
        DateTimeFormatter dtf = getUntilDateFormat(template);
        int startindex = template.indexOf("${DU:");
        int endindex = template.indexOf("}") + 1;
        String first = template.substring(0, startindex);
        String last = template.substring(endindex);
        return first + date.toString(dtf.withZone(zone)) + last;
    }

    public static String replaceDateFromUntil(DateTime from, DateTime until, String filePath, DateTimeZone zone) {
//        String replacedString = null;
        while (filePath.contains("${DF:") && filePath.contains("${DU:")) {
            int fromstartindex = filePath.indexOf("${DF:");
            int untilstartindex = filePath.indexOf("${DU:");
            if (fromstartindex < untilstartindex) {
                filePath = replaceDateFrom(filePath, from, zone);
                filePath = replaceDateUntil(filePath, until, zone);
            } else {
                filePath = replaceDateUntil(filePath, until, zone);
                filePath = replaceDateFrom(filePath, from, zone);
            }

        }
        return filePath;
    }

    private static DateTime getFolderTime(String name, String[] pathStream) {
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

    public static DateTimeFormatter getFromDateFormat(String stringWithDate) {
        int startindex = stringWithDate.indexOf("${DF:");
        int endindex = stringWithDate.indexOf("}");
        String date = stringWithDate.substring(startindex + 5, endindex);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(date);
        return dtf;
    }

    public static DateTimeFormatter getUntilDateFormat(String stringWithDate) {
        int startindex = stringWithDate.indexOf("${DU:");
        int endindex = stringWithDate.indexOf("}");
        String date = stringWithDate.substring(startindex + 5, endindex);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(date);
        return dtf;
    }

    public static Boolean containsTokens(String path) {
        return path.contains("${");
    }


    public static void setLastReadout(JEVisObject channel, Object latestDatapoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
