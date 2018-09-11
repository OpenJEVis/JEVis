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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bf
 */
public class JEVisImporter implements Importer {

    private JEVisDataSource _client = null;
    private DateTimeZone _timezone;
//    private DateTime _latestDateTime;

    public static void main(String[] args) {
        DateTime dateTime = new DateTime();
        //System.out.println(dateTime.toString(DateTimeFormat.fullDateTime()));
        DateTime convertTime = TimeConverter.convertTime(DateTimeZone.UTC, dateTime);
        //System.out.println(convertTime.toString(DateTimeFormat.fullDateTime()));
    }

    @Override
    public void initialize(JEVisObject dataSource) {
        try {
            _client = dataSource.getDataSource();
            JEVisClass dataSourceClass = _client.getJEVisClass(DataCollectorTypes.DataSource.NAME);
            JEVisType timezoneType = dataSourceClass.getType(DataCollectorTypes.DataSource.TIMEZONE);
            String timezone = DatabaseHelper.getObjectAsString(dataSource, timezoneType);
            _timezone = DateTimeZone.forID(timezone);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(JEVisImporter.class.getName()).log(java.util.logging.Level.SEVERE, "Timezone setup in Importer failed", ex);
        }
    }

    //    @Override
//    public DateTime getLatestDatapoint() {
//        String toString = _latestDateTime.toString(DateTimeFormat.forPattern("HH:mm:ss dd.MM.yyyy"));
//        return _latestDateTime;
//    }
    @Override
    public Object getLatestDatapoint() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //TODO: support other attributes then "Value"
    @Override
    public DateTime importResult(List<Result> results) {
        Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "--Starting SampleImport v2.5  --");
        try {
            DateTime lastTSTotal = null;
            if (results.isEmpty()) {
                return null;
            }

            int errorImport = 0;

            Map<Long, JEVisAttribute> targets = new HashMap<>();
            List<Long> targetErrors = new ArrayList<>();

            //create an access map for the configured target once
            for (Result s : results) {
                try {
                    if (s == null || s.getOnlineID() == null) {
                        continue;
                    }

//                    Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Target Sample.getID: " + s.getOnlineID());
                    //System.out.println("Target Sample.getID: " + s.getOnlineID());
                    if (targets.containsKey(s.getOnlineID())) {
                        continue;
                    }
                    if (targetErrors.contains(s.getOnlineID())) {
//                        errorImport++;
                        continue;
                    }

                    //Check the Object existens
                    JEVisObject onlineData = _client.getObject(s.getOnlineID());
                    if (onlineData == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.ERROR, "Target Object not found: " + s.getOnlineID());
                        targetErrors.add(s.getOnlineID());//invalid Object, be keep it so the other with the smae id need not check again
                        errorImport++;
                        continue;
                    }

//                    JEVisAttribute valueAtt = onlineData.getAttribute("Value");
                    JEVisAttribute valueAtt = onlineData.getAttribute(s.getAttribute());
                    if (valueAtt == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.ERROR, "Target has no Attribute 'Value'");
                        targetErrors.add(s.getOnlineID());
                        errorImport++;
                        continue;
                    }

                    if (_timezone == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.ERROR, "Timezone missing for " + onlineData.getID());
                        targetErrors.add(s.getOnlineID());
                        errorImport++;
                        continue;
                    }

                    targets.put(s.getOnlineID(), valueAtt);
                } catch (Exception ex) {
                    Logger.getLogger(JEVisImporter.class.getName()).log(Level.ERROR, "Unexpected error while sample creation: " + ex);
                    targetErrors.add(s.getOnlineID());
                }
            }

            String errorIDs = "";
            for (Long targetError : targetErrors) {
                errorIDs += targetError + ",";
            }
            Logger.getLogger(JEVisImporter.class.getName()).log(Level.INFO, "Erroneously target configurations for: [" + errorIDs + "]");

            String okIDs = "";
            for (Map.Entry<Long, JEVisAttribute> entrySet : targets.entrySet()) {
                Long key = entrySet.getKey();
                JEVisAttribute value = entrySet.getValue();
                okIDs += key + ",";
            }
            Logger.getLogger(JEVisImporter.class.getName()).log(Level.INFO, "Failed target configurations for: [" + okIDs + "]");

            //build the Samples per attribute so we can bulk import them
            Map<JEVisAttribute, List<JEVisSample>> toImportList = new HashMap<>();
            for (Result s : results) {
                try {
                    //how can there to invalied samples....
                    if (s == null || s.getOnlineID() == null) {
                        continue;
                    }

                    if (s.getValue() == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Error: Value is empty");
                        continue;
                    }

                    if (s.getDate() == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Error: Value has no timestamp ignore");
                        continue;
                    }

                    DateTime convertedDate = TimeConverter.convertTime(_timezone, s.getDate());

                    if (convertedDate == null) {
                        Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Error: Could not convert Date");
                        continue;
                    }
                    JEVisAttribute target = targets.get(s.getOnlineID());
                    if (!toImportList.containsKey(target)) {
                        toImportList.put(target, new ArrayList<JEVisSample>());
                    }

                    List<JEVisSample> sList = toImportList.get(target);

                    JEVisSample sample = target.buildSample(convertedDate, s.getValue());
                    sList.add(sample);
                } catch (Exception ex) {
                    errorImport++;
                    Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Unexpected error while sample creation: " + ex);
                }

            }

            //do the import into the JEVis DB
            for (Map.Entry<JEVisAttribute, List<JEVisSample>> entrySet : toImportList.entrySet()) {
                try {
                    JEVisAttribute key = entrySet.getKey();
                    List<JEVisSample> values = entrySet.getValue();

                    //Bulk Import
                    key.addSamples(values);

                    DateTime lastTSForAtt = null;
                    for (JEVisSample s : values) {
                        if (lastTSTotal == null || lastTSTotal.isBefore(s.getTimestamp())) {
                            lastTSTotal = s.getTimestamp();
                        }
                        if (lastTSForAtt == null || lastTSForAtt.isBefore(s.getTimestamp())) {
                            lastTSForAtt = s.getTimestamp();
                        }

                    }
                    Logger.getLogger(JEVisImporter.class.getName()).log(Level.INFO,
                            "Object: [" + key.getObject().getID() + "] " + key.getObject().getName()
                            + "  Imported: " + values.size() + " LastTS: " + lastTSForAtt);

                } catch (Exception ex) {
                    Logger.getLogger(JEVisImporter.class.getName()).log(Level.DEBUG, "Unexpected error while import: " + ex);
                }
            }
            return lastTSTotal.withZone(_timezone);

        } catch (Exception ex) {
            Logger.getLogger(JEVisImporter.class.getName()).log(Level.ERROR, null, ex);
        }
        return null;
    }
}
