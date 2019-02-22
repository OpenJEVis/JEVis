/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public interface Finisher {

    void finishReport(Report report, ReportProperty property);

    void continueWithNextReport(JEVisObject reportObject);
}
