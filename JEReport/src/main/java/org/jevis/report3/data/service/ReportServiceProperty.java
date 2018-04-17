/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public class ReportServiceProperty {

    private JEVisFile notificationFile;
    private Long mailID;

    public void initialize(JEVisObject jevisobject) {
        try {
            JEVisAttribute notificationAttribute = jevisobject.getAttribute(JEReportService.NOTIFICATION_FILE);
            notificationFile = notificationAttribute.getLatestSample().getValueAsFile();
            mailID = jevisobject.getAttribute(JEReportService.REPORT_MAIL_ID).getLatestSample().getValueAsLong();
        } catch (JEVisException ex) {
            Logger.getLogger(ReportServiceProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getNotificationFile() {
        File tmpJar = null;
        try {
            tmpJar = File.createTempFile("notification", ".jar");
            tmpJar.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tmpJar);
            fos.write(notificationFile.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(ReportServiceProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmpJar;
    }

    public Long getMailID() {
        return mailID;
    }
}
