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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;

/**
 * @author bf
 */
public class JEVisImporter implements Importer {
    private static final Logger logger = LogManager.getLogger(JEVisImporter.class);

    private JEVisDataSource client = null;
    private JEVisObject dataSource;
    private DateTimeZone timezone;
    private Boolean overwrite = false;
//    private DateTime _latestDateTime;

    public static void main(String[] args) {
        DateTime dateTime = new DateTime();
        //logger.info(dateTime.toString(DateTimeFormat.fullDateTime()));
        DateTime convertTime = TimeConverter.convertTime(DateTimeZone.UTC, dateTime);
        //logger.info(convertTime.toString(DateTimeFormat.fullDateTime()));
    }

    @Override
    public void initialize(JEVisObject dataSource) {
        try {
            this.dataSource = dataSource;
            client = dataSource.getDataSource();
            JEVisAttribute timezoneAttribute = dataSource.getAttribute(DataCollectorTypes.DataSource.TIMEZONE);
            timezone = DateTimeZone.UTC;
            if (timezoneAttribute != null) {
                JEVisSample lastSample = timezoneAttribute.getLatestSample();
                if (lastSample != null) {
                    try {
                        DateTimeZone dateTimeZone = DateTimeZone.forID(lastSample.getValueAsString());
                        if (dateTimeZone != null)
                            timezone = dateTimeZone;
                    } catch (IllegalArgumentException e) {
                        logger.error("Timezone readout failed, falling back to UTC");
                    }
                }
            }

            JEVisAttribute overwriteAttribute = dataSource.getAttribute(DataCollectorTypes.DataSource.OVERWRITE);
            if (overwriteAttribute != null) {
                JEVisSample lastSample = overwriteAttribute.getLatestSample();
                if (lastSample != null) {
                    overwrite = lastSample.getValueAsBoolean();
                }
            }
        } catch (Exception ex) {
            logger.fatal("Datasource initializing in Importer failed", ex);
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

    @Override
    public boolean getOverwrite() {
        return overwrite;
    }

    @Override
    public DateTime importResult(List<Result> results) {
        logger.debug("--Starting SampleImport v2.5  --");
        try {
            DateTime lastTSTotal = null;
            if (results.isEmpty()) {
                return null;
            }

            int errorImport = 0;

            List<JEVisAttribute> targets = new ArrayList<>();
            List<String> targetErrors = new ArrayList<>();

            //create an access map for the configured target once
            for (Result s : results) {
                try {
                    if (s == null || s.getTargetStr() == null) {
                        continue;
                    }

                    if (targetErrors.contains(s.getTargetStr())) {
                        continue;
                    }

                    //Check the Object exists
                    TargetHelper targetHelper = new TargetHelper(dataSource.getDataSource(), s.getTargetStr());

                    for (JEVisObject onlineData : targetHelper.getObject()) {
                        int index = targetHelper.getObject().indexOf(onlineData);
                        JEVisAttribute valueAtt = null;
                        if (targetHelper.getAttribute().isEmpty()) {
                            valueAtt = onlineData.getAttribute("Value");
                        } else {
                            valueAtt = targetHelper.getAttribute().get(index);
                        }

                        if (valueAtt == null) {
                            logger.error("Target has no Attribute 'Value'");
                            targetErrors.add(s.getTargetStr());
                            errorImport++;
                            continue;
                        }

                        targets.add(valueAtt);
                    }

                    if (targetHelper.getObject().isEmpty()) {
                        logger.error("Target Object not found: {}", s.getTargetStr());
                        targetErrors.add(s.getTargetStr());//invalid Object, be keep it so the other with the same id need not check again
                        errorImport++;
                    }
                } catch (Exception ex) {
                    logger.fatal("Unexpected error while sample creation: ", ex);
                    targetErrors.add(s.getTargetStr());
                }
            }

            StringBuilder errorIDs = new StringBuilder();
            for (String targetError : targetErrors) {
                errorIDs.append(targetError).append(",");
            }
            logger.info("Erroneously target configurations for: [{}]", errorIDs);

            StringBuilder okIDs = new StringBuilder();
            for (JEVisAttribute attribute : targets) {
                String key = attribute.getObject().getName() + ":" + attribute.getObjectID() + ":" + attribute.getName();
                okIDs.append(key).append(",");
            }
            logger.info("ok target configurations for: [{}]", okIDs);

            //build the Samples per attribute, so we can bulk import them
            Map<JEVisAttribute, List<JEVisSample>> toImportList = new HashMap<>();
            for (Result s : results) {
                try {
                    logger.debug("Result to import: att: {}, date: {}, value: {}, targetS: {}", s.getAttribute(), s.getDate(), s.getValue(), s.getTargetStr());
                    if (s == null || s.getTargetStr() == null || s.getTargetStr().isEmpty() || s.getAttribute() == null || s.getAttribute().isEmpty()) {
                        logger.error("Skip import for missing target");
                        continue;
                    }

                    TargetHelper targetHelper = new TargetHelper(dataSource.getDataSource(), s.getTargetStr());
                    for (JEVisObject targetObject : targetHelper.getObject()) {
                        int index = targetHelper.getObject().indexOf(targetObject);
                        JEVisAttribute targetAttribute = null;
                        if (targetHelper.getAttribute().isEmpty()) {
                            targetAttribute = targetObject.getAttribute("Value");
                        } else {
                            targetAttribute = targetHelper.getAttribute().get(index);
                        }

                        if (s.getValue() == null) {
                            logger.error("Error: Value is empty");
                            continue;
                        }

                        if (s.getDate() == null) {
                            logger.error("Error: Value has no timestamp ignore");
                            continue;
                        }

                        DateTime convertedDate = TimeConverter.convertTime(timezone, s.getDate());

                        if (convertedDate == null) {
                            logger.error("Error: Could not convert Date");
                            continue;
                        }

                        if (!toImportList.containsKey(targetAttribute)) {
                            toImportList.put(targetAttribute, new ArrayList<JEVisSample>());
                        }

                        List<JEVisSample> sList = toImportList.get(targetAttribute);

                        if (overwrite) {
                            logger.info("Overwrite is enabled, delete samples in between: {}-{}", convertedDate, convertedDate);
                            targetAttribute.deleteSamplesBetween(convertedDate, convertedDate);
                        }

                        JEVisSample sample = targetAttribute.buildSample(convertedDate, s.getValue());
                        sList.add(sample);
                    }
                } catch (Exception ex) {
                    errorImport++;
                    logger.fatal("Unexpected error while sample creation: {}", ex, ex);
                }

            }

            //do the import into the JEVis DB
            //logger.info("Total List Count: {}",toImportList.size());
            for (Map.Entry<JEVisAttribute, List<JEVisSample>> entrySet : toImportList.entrySet()) {
                try {
                    JEVisAttribute key = entrySet.getKey();
                    List<JEVisSample> values = entrySet.getValue();
                    values.sort(Comparator.comparing(jeVisSample -> {
                        try {
                            return jeVisSample.getTimestamp();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                        return null;
                    }));

                    //Bulk Import
                    logger.info("Import samples: key: {} , values: {}", key.getObject().getName(), values.size());
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

                    if (overwrite) {
                        try {
                            List<JEVisObject> objects = new ArrayList<>();
                            objects.add(key.getObject());

                            CommonMethods.cleanDependentObjects(objects, values.get(0).getTimestamp());
                        } catch (Exception e) {
                            logger.error("Failed cleaning of dependencies", e);
                        }
                    }

                    logger.info("Object: [{}] {}  Imported: {} LastTS: {}", key.getObject().getID(), key.getObject().getName(), values.size(), lastTSForAtt);

                } catch (Exception ex) {
                    logger.fatal("Unexpected error while import: ", ex);
                }
            }
            if (lastTSTotal != null) {
                return lastTSTotal.withZone(timezone);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }
        return null;
    }
}
