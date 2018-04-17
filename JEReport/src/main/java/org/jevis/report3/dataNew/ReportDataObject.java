/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.dataNew;

import java.util.Map;
import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public class ReportDataObject implements ReportData {

    private final JEVisObject jevisObject;

    public ReportDataObject(JEVisObject jevisObject) {
        this.jevisObject = jevisObject;
    }

    @Override
    public Map<String, Object> getDataMap() {
        return null;
    }

}
