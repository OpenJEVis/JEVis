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
package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;
import static org.jevis.commons.dataprocessing.ProcessOptions.CUSTOM;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class InputFunction implements ProcessFunction {
    private static final Logger logger = LogManager.getLogger(InputFunction.class);

    public final static String NAME = "Input";
    public final static String OBJECT_ID = "object-id";
    public final static String ATTRIBUTE_ID = "attribute-id";
    public static final int LIMIT = 1000000;
    private List<JEVisSample> _result = null;
    private List<JsonSample> _jsonResult = null;
    private JsonSampleGenerator jsonSampleGenerator;

    public InputFunction() {
    }

    public InputFunction(List<JEVisSample> resultSamples) {
        _result = resultSamples;
    }

    @Override
    public void resetResult() {
        _result = null;
    }

    @Override
    public List<JEVisSample> getResult(Process task) {
        if (_result != null) {
            return _result;
        } else {
            _result = new ArrayList<>();

            JEVisObject object = null;
            if (ProcessOptions.ContainsOption(task, OBJECT_ID)) {
                long oid = Long.parseLong((ProcessOptions.GetLatestOption(task, OBJECT_ID, new BasicProcessOption(OBJECT_ID, "")).getValue()));
                try {
                    object = task.getJEVisDataSource().getObject(oid);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            } else if (task.getObject() != null) {
                try {
                    object = task.getObject().getParents().get(0);//TODO make save
                } catch (JEVisException ex) {
                    logger.error(ex);
                }
            }

            if (object != null && ProcessOptions.ContainsOption(task, ATTRIBUTE_ID)) {

                try {
                    logger.info("Parent object: {}", object);
//                    long oid = Long.valueOf(task.getOptions().get(OBJECT_ID));
//                    JEVisObject object = task.getJEVisDataSource().getObject(oid);

                    JEVisAttribute att = object.getAttribute(ProcessOptions.GetLatestOption(task, ATTRIBUTE_ID, new BasicProcessOption(ATTRIBUTE_ID, "")).getValue());

                    JEVisObject correspondingUserDataObject = null;
                    boolean foundUserDataObject = false;
                    final JEVisClass userDataClass = object.getDataSource().getJEVisClass("User Data");
                    for (JEVisObject parent : object.getParents()) {
                        for (JEVisObject child : parent.getChildren()) {
                            if (child.getJEVisClass().equals(userDataClass)) {
                                correspondingUserDataObject = child;
                                foundUserDataObject = true;
                                break;
                            }
                        }
                    }

                    DateTime[] startEnd = ProcessOptions.getStartAndEnd(task);
                    logger.info("start: {} end: {}", startEnd[0], startEnd[1]);

                    if (foundUserDataObject) {
                        SortedMap<DateTime, JEVisSample> map = new TreeMap<>();
                        for (JEVisSample jeVisSample : att.getSamples(startEnd[0], startEnd[1])) {
                            map.put(jeVisSample.getTimestamp(), jeVisSample);
                        }

                        JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");
                        List<JEVisSample> userValues = userDataValueAttribute.getSamples(startEnd[0], startEnd[1]);

                        for (JEVisSample userValue : userValues) {
                            String note = map.get(userValue.getTimestamp()).getNote();
                            VirtualSample virtualSample = new VirtualSample(userValue.getTimestamp(), userValue.getValueAsDouble(), att.getDisplayUnit());
                            virtualSample.setNote(note + "," + USER_VALUE);
                            virtualSample.setAttribute(map.get(userValue.getTimestamp()).getAttribute());

                            map.remove(userValue.getTimestamp());
                            map.put(virtualSample.getTimestamp(), virtualSample);
                        }

                        _result = new ArrayList<>(map.values());
                    } else {
                        _result = att.getSamples(startEnd[0], startEnd[1]);
                    }

                    logger.info("Input result: {}", _result.size());
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            } else {
                logger.warn("Missing options {} and {}", OBJECT_ID, ATTRIBUTE_ID);
            }
        }
        return _result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<ProcessOption> getAvailableOptions() {
        List<ProcessOption> options = new ArrayList<>();

        options.add(new BasicProcessOption("Object"));
        options.add(new BasicProcessOption("Attribute"));
        options.add(new BasicProcessOption("Workflow"));

        return options;
    }

    @Override
    public List<JsonSample> getJsonResult(BasicProcess task) {
        if (_jsonResult != null) {
            return _jsonResult;
        } else {
            _jsonResult = new ArrayList<>();

            JsonObject object = null;
            SQLDataSource sql = task.getSqlDataSource();
            if (ProcessOptions.ContainsOption(task, OBJECT_ID)) {
                long oid = Long.parseLong((ProcessOptions.GetLatestOption(task, OBJECT_ID, new BasicProcessOption(OBJECT_ID, "")).getValue()));
                try {
                    object = sql.getObject(oid);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            } else if (task.getObject() != null) {
                try {
                    for (JsonRelationship rel : task.getJsonObject().getRelationships()) {
                        if (rel.getType() == 1) {
                            object = sql.getObject(rel.getTo());//TODO make save
                        }
                    }
                } catch (JEVisException ex) {
                    logger.error(ex);
                }
            }

            if (object != null && ProcessOptions.ContainsOption(task, ATTRIBUTE_ID)) {

                try {
                    logger.info("Parent object: {}", object);
//                    long oid = Long.valueOf(task.getOptions().get(OBJECT_ID));
//                    JEVisObject object = task.getJEVisDataSource().getObject(oid);

                    JsonAttribute att = null;
                    for (JsonAttribute attribute : sql.getAttributes(object.getId())) {
                        if (attribute.getType().equals(ProcessOptions.GetLatestOption(task, ATTRIBUTE_ID, new BasicProcessOption(ATTRIBUTE_ID, "")).getValue())) {
                            att = attribute;
                        }
                    }

                    JsonObject correspondingUserDataObject = null;
                    boolean foundUserDataObject = false;

                    for (JsonRelationship rel : sql.getRelationships(object.getId())) {
                        if (rel.getType() == 1) {
                            JsonObject parent = sql.getObject(rel.getTo());
                            for (JsonRelationship pRel : sql.getRelationships(parent.getId())) {
                                if (rel.getType() == 1) {
                                    JsonObject child = sql.getObject(pRel.getFrom());
                                    if (child.getJevisClass().equals("User Data")) {
                                        correspondingUserDataObject = child;
                                        foundUserDataObject = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    DateTime[] startEnd = ProcessOptions.getStartAndEnd(task);
                    logger.info("start: {} end: {}", startEnd[0], startEnd[1]);

                    boolean isCustomWorkDay = true;
                    for (ProcessOption option : task.getOptions()) {
                        if (option.getKey().equals(CUSTOM)) {
                            isCustomWorkDay = Boolean.parseBoolean(option.getValue());
                            break;
                        }
                    }

                    WorkDays workDays = new WorkDays(task.getSqlDataSource(), task.getJsonObject());
                    workDays.setEnabled(isCustomWorkDay);

                    AggregationPeriod aggregationPeriod = task.getJsonSampleGenerator().getAggregationPeriod();
                    switch (aggregationPeriod) {
                        default:
                            break;
                        case QUARTER_HOURLY:
                            startEnd[0] = startEnd[0].withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                        case HOURLY:
                            startEnd[0] = startEnd[0].withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                        case DAILY:
                            startEnd[0] = startEnd[0].withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                        case WEEKLY:
                            startEnd[0] = startEnd[0].withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                        case MONTHLY:
                            startEnd[0] = startEnd[0].withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                        case QUARTERLY:
                            if (startEnd[0].getMonthOfYear() < 4) {
                                startEnd[0] = startEnd[0].withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            } else if (startEnd[0].getMonthOfYear() < 7) {
                                startEnd[0] = startEnd[0].withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            } else if (startEnd[0].getMonthOfYear() < 10) {
                                startEnd[0] = startEnd[0].withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            } else {
                                startEnd[0] = startEnd[0].withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            }
                            break;
                        case YEARLY:
                            startEnd[0] = startEnd[0].withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            break;
                    }

                    if (isCustomWorkDay) {
                        switch (aggregationPeriod) {
                            case DAILY:
                            case WEEKLY:
                            case MONTHLY:
                            case QUARTERLY:
                            case YEARLY:
                            case THREEYEARS:
                            case FIVEYEARS:
                            case TENYEARS:
                                startEnd[0] = startEnd[0].withHourOfDay(workDays.getWorkdayStart().getHour());
                                startEnd[0] = startEnd[0].withMinuteOfHour(workDays.getWorkdayStart().getMinute());
                                startEnd[0] = startEnd[0].withSecondOfMinute(workDays.getWorkdayStart().getSecond());

                                startEnd[1] = startEnd[1].withHourOfDay(workDays.getWorkdayEnd().getHour());
                                startEnd[1] = startEnd[1].withMinuteOfHour(workDays.getWorkdayEnd().getMinute());
                                startEnd[1] = startEnd[1].withSecondOfMinute(workDays.getWorkdayEnd().getSecond());

                                if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
                                    startEnd[0] = startEnd[0].minusDays(1);
                                }
                                break;
                        }
                    }

                    if (foundUserDataObject && att != null) {

                        SortedMap<DateTime, JsonSample> map = new TreeMap<>();
                        for (JsonSample jeVisSample : sql.getSamples(object.getId(), att.getType(), startEnd[0], startEnd[1], LIMIT)) {
                            map.put(new DateTime(jeVisSample.getTs()), jeVisSample);
                        }

                        JsonAttribute userDataValueAttribute = null;
                        for (JsonAttribute attribute : sql.getAttributes(correspondingUserDataObject.getId())) {
                            if (attribute.getType().equals("Value")) {
                                userDataValueAttribute = attribute;
                            }
                        }
                        List<JsonSample> userValues = sql.getSamples(correspondingUserDataObject.getId(), userDataValueAttribute.getType(), startEnd[0], startEnd[1], LIMIT);

                        for (JsonSample userValue : userValues) {
                            String note = map.get(new DateTime(userValue.getTs())).getNote();
                            JsonSample jsonSample = new JsonSample();
                            jsonSample.setTs(userValue.getTs());
                            jsonSample.setValue(userValue.getValue());
                            jsonSample.setNote(note + "," + USER_VALUE);

                            map.remove(new DateTime(userValue.getTs()));
                            map.put(new DateTime(jsonSample.getTs()), jsonSample);
                        }

                        _jsonResult = new ArrayList<>(map.values());
                    } else {
                        _jsonResult = sql.getSamples(object.getId(), att.getType(), startEnd[0], startEnd[1], LIMIT);
                    }

                    logger.info("Input result: {}", _jsonResult.size());
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            } else {
                logger.warn("Missing options {} and {}", OBJECT_ID, ATTRIBUTE_ID);
            }
        }
        return _jsonResult;
    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }
}
