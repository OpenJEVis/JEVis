/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.driver;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 *
 * @author Broder
 */
public class DataSourceHelper {

    public static void test() {

    }

    static public void doTrustToCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    public static List<String> getFTPMatchedFileNames(FTPClient fc, DateTime lastReadout, String filePath) {
        filePath = filePath.replace("\\", "/");
        String[] pathStream = getPathTokens(filePath);

        String startPath = "";
        if (filePath.startsWith("/")) {
            startPath = "/";
        }

        List<String> folderPathes = getMatchingPathes(startPath, pathStream, new ArrayList<String>(), fc, lastReadout, new DateTimeFormatterBuilder());
//        System.out.println("foldersize,"+folderPathes.size());
        List<String> fileNames = new ArrayList<String>();

        if (folderPathes.isEmpty()) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable folder on the device");
            return fileNames;
        }

//        String fileName = null;
        String fileNameScheme = pathStream[pathStream.length - 1];
        String currentfolder = null;
        try {
            for (String folder : folderPathes) {
                //                fc.changeWorkingDirectory(folder);
                //                System.out.println("currentFolder,"+folder);
                currentfolder = folder;
//                for (FTPFile file : fc.listFiles(folder)) {
//                    System.out.println(file.getName());
//                }
                fc.changeWorkingDirectory(folder);
                for (FTPFile file : fc.listFiles()) {
//                    org.apache.log4j.Logger.getLogger(Launcher.class.getName()).log(org.apache.log4j.Level.ALL, "CurrentFileName: " + fileName);
//                    fileName = removeFoler(fileName, folder);
                    DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
                    String fileName = file.getName();
                    String timePart = fc.getModificationTime(folder + fileName).split(" ")[1].trim();
                    DateTime modificationTime = dateFormat.parseDateTime(timePart);
                    if (modificationTime.isBefore(lastReadout)) {
                        continue;
                    }
                    boolean match = false;
                    System.out.println(file.getName());
                    if (DataSourceHelper.containsTokens(fileNameScheme)) {
                        boolean matchDate = matchDateString(file.getName(), fileNameScheme);
                        DateTime folderTime = getFileTime(folder + file.getName(), pathStream);
                        boolean isLater = folderTime.isAfter(lastReadout);
                        if (matchDate && isLater) {
                            match = true;
                        }
                    } else {
                        Pattern p = Pattern.compile(fileNameScheme);
                        Matcher m = p.matcher(file.getName());
                        match = m.matches();
                    }
                    if (match) {
                        fileNames.add(folder + file.getName());
                    }
                }
            }
        } catch (IOException ex) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Error while searching a matching file");
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Folder: " + currentfolder);
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "FileName: " + fileNameScheme);
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        }
        if (folderPathes.isEmpty()) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable files on the device");
        }
//        System.out.println("filenamesize"+fileNames.size());
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

    private static String removeFoler(String fileName, String folder) {
        if (fileName.startsWith(folder)) {
            return fileName.substring(folder.length(), fileName.length());
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

    private static List<String> getMatchingPathes(String path, String[] pathStream, ArrayList<String> arrayList, FTPClient fc, DateTime lastReadout, DateTimeFormatterBuilder dtfbuilder) {
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
//                DateTimeFormatter ftmTemp = getDateFormatter(nextToken);
                for (FTPFile folder : listDirectories) {
                    if (!matchDateString(folder.getName(), nextToken)) {
                        continue;
                    }
//                    System.out.println("listdir," + folder.getName());
//                    if (containsDate(folder.getName(), ftmTemp)) {
                    DateTime folderTime = getFolderTime(path + folder.getName() + "/", pathStream);
                    if (folderTime.isAfter(lastReadout)) {
                        nextFolder = folder.getName();
//                        System.out.println("dateFolder," + nextFolder);
                        getMatchingPathes(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, dtfbuilder);
                    }
//                    }
                }
            } else {
                nextFolder = nextToken;
//                System.out.println("normalFolder," + nextFolder);
                getMatchingPathes(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, dtfbuilder);
            }
        } catch (IOException ex) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        }
        return arrayList;
    }

    private static boolean containsDateToken(String string) {
        if (string.contains("${D:")) {
            return true;
        } else {
            return false;
        }
    }

    public static String[] getPathTokens(String filePath) {
//        List<String> tokens = new ArrayList<String>();
        //        filePath.substring("\\$\\{","\\}");
        String[] tokens = StringUtils.split(filePath, "/");
//         String[] tokens = filePath.trim().split("\\%");
        for (int i = 0; i < tokens.length; i++) {
            System.out.println(tokens[i]);
        }
        return tokens;
    }

    public static String replaceDateFrom(String template, DateTime date) {
        DateTimeFormatter dtf = getFromDateFormat(template);
        int startindex = template.indexOf("${DF:");
        int endindex = template.indexOf("}") + 1;
        String first = template.substring(0, startindex);
        String last = template.substring(endindex, template.length());
        return first + date.toString(dtf) + last;
    }

    public static String replaceDateUntil(String template, DateTime date) {
        DateTimeFormatter dtf = getUntilDateFormat(template);
        int startindex = template.indexOf("${DU:");
        int endindex = template.indexOf("}") + 1;
        String first = template.substring(0, startindex);
        String last = template.substring(endindex, template.length());
        return first + date.toString(dtf) + last;
    }

    public static String replaceDateFromUntil(DateTime from, DateTime until, String filePath) {
//        String replacedString = null;
        while (filePath.indexOf("${DF:") != -1 || filePath.indexOf("${DF:") != -1) {
            int fromstartindex = filePath.indexOf("${DF:");
            int untilstartindex = filePath.indexOf("${DU:");
            if (fromstartindex < untilstartindex) {
                filePath = replaceDateFrom(filePath, from);
                filePath = replaceDateUntil(filePath, until);
            } else {
                filePath = replaceDateUntil(filePath, until);
                filePath = replaceDateFrom(filePath, from);
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
        if (path.contains("${")) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> getSFTPMatchedFileNames(ChannelSftp _channel, DateTime lastReadout, String filePath) {
        filePath = filePath.replace("\\", "/");
        String[] pathStream = getPathTokens(filePath);

        String startPath = "";
        if (filePath.startsWith("/")) {
            startPath = "/";
        }

        List<String> folderPathes = getSFTPMatchingPathes(startPath, pathStream, new ArrayList<String>(), _channel, lastReadout, new DateTimeFormatterBuilder());
//        System.out.println("foldersize,"+folderPathes.size());
        List<String> fileNames = new ArrayList<String>();
        if (folderPathes.isEmpty()) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable folder on the device");
            return fileNames;
        }

        if (folderPathes.isEmpty()) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable folder on the device");
            return fileNames;
        }

        String fileNameScheme = pathStream[pathStream.length - 1];
        String currentfolder = null;
        try {
            for (String folder : folderPathes) {
                //                fc.changeWorkingDirectory(folder);
                //                System.out.println("currentFolder,"+folder);
                currentfolder = folder;
                //                for (FTPFile file : fc.listFiles(folder)) {
                //                    System.out.println(file.getName());
                //                }
//                Vector ls = _channel.ls(folder);
                for (Object fileName : _channel.ls(folder)) {
                    LsEntry currentFile = (LsEntry) fileName;
                    String currentFileName = currentFile.getFilename();
                    currentFileName = removeFoler(currentFileName, folder);
                    boolean match = false;
                    System.out.println(currentFileName);
                    if (DataSourceHelper.containsTokens(fileNameScheme)) {
                        boolean matchDate = matchDateString(currentFileName, fileNameScheme);
                        DateTime folderTime = getFileTime(folder + currentFileName, pathStream);
                        boolean isLater = folderTime.isAfter(lastReadout);
                        if (matchDate && isLater) {
                            match = true;
                        }
                    } else {
                        Pattern p = Pattern.compile(fileNameScheme);
                        Matcher m = p.matcher(currentFileName);
                        match = m.matches();
                    }
                    if (match) {
                        fileNames.add(folder + currentFileName);
                    }
                }
            }
        } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Error while searching a matching file");
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Folder: " + currentfolder);
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "FileName: " + fileNameScheme);
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        }
        if (folderPathes.isEmpty()) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable files on the device");
        }
//        System.out.println("filenamesize"+fileNames.size());
        return fileNames;
    }

    private static List<String> getSFTPMatchingPathes(String path, String[] pathStream, ArrayList<String> arrayList, ChannelSftp fc, DateTime lastReadout, DateTimeFormatterBuilder dtfbuilder) {
        int nextTokenPos = getPathTokens(path).length;
        if (nextTokenPos == pathStream.length - 1) {
            arrayList.add(path);
            return arrayList;
        }

        String nextToken = pathStream[nextTokenPos];
        String nextFolder = null;

        try {
            if (containsDateToken(nextToken)) {
                Vector listDirectories = fc.ls(path);
                for (Object folder : listDirectories) {
                    LsEntry currentFolder = (LsEntry) folder;

                    if (!matchDateString(currentFolder.getFilename(), nextToken)) {
                        continue;
                    }
                    DateTime folderTime = getFolderTime(path + currentFolder.getFilename() + "/", pathStream);
                    if (folderTime.isAfter(lastReadout)) {
                        nextFolder = currentFolder.getFilename();
                        getSFTPMatchingPathes(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, dtfbuilder);
                    }
//                    }
                }
            } else {
                nextFolder = nextToken;
                getSFTPMatchingPathes(path + nextFolder + "/", pathStream, arrayList, fc, lastReadout, dtfbuilder);
            }
        } catch (SftpException ex) {
            org.apache.log4j.Logger.getLogger(DataSourceHelper.class).log(org.apache.log4j.Level.ERROR, "Cant find suitable files on the device");
        }
        return arrayList;
    }

    public static void setLastReadout(JEVisObject channel, Object latestDatapoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
