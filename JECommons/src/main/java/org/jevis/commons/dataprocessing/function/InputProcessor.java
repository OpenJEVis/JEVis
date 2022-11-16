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

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class InputProcessor {
    public static final int LIMIT = 1000000;
    private static final Logger logger = LogManager.getLogger(InputProcessor.class);
    private final SQLDataSource ds;
    private List<JsonSample> _jsonResult = null;
    private JsonSampleGenerator jsonSampleGenerator;

    public InputProcessor(SQLDataSource ds) {
        this.ds = ds;
    }

    public void resetResult() {
        _jsonResult = null;
    }


    public List<JsonSample> getJsonResult(JsonObject object, JsonAttribute attribute, DateTime from, DateTime to) {
        if (_jsonResult != null) {
            return _jsonResult;
        } else {
            try {
                _jsonResult = new ArrayList<>();

                if (object != null && attribute != null) {
                    try {
                        JsonObject correspondingUserDataObject = null;
                        boolean foundUserDataObject = false;

                        List<JsonRelationship> relationships = ds.getRelationships(object.getId());
                        for (JsonRelationship rel : relationships) {
                            if (rel.getType() == 1 && rel.getFrom() == object.getId()) {
                                JsonObject parent = ds.getObject(rel.getTo());
                                List<JsonRelationship> parentRelationships = ds.getRelationships(parent.getId());
                                for (JsonRelationship pRel : parentRelationships) {
                                    if (rel.getType() == 1 && rel.getTo() == parent.getId()) {
                                        JsonObject child = ds.getObject(pRel.getFrom());
                                        if (child.getJevisClass().equals("User Data") && child.getName().contains(object.getName())) {
                                            correspondingUserDataObject = child;
                                            foundUserDataObject = true;
                                            break;
                                        }
                                    }
                                }
                                if (foundUserDataObject) break;
                            }
                        }

                        if (foundUserDataObject) {

                            SortedMap<DateTime, JsonSample> map = new TreeMap<>();
                            for (JsonSample jeVisSample : ds.getSamples(object.getId(), attribute.getType(), from, to, LIMIT)) {
                                map.put(new DateTime(jeVisSample.getTs()), jeVisSample);
                            }

                            List<JsonSample> userValues = ds.getSamples(correspondingUserDataObject.getId(), "Value", from, to, LIMIT);

                            for (JsonSample userValue : userValues) {
                                DateTime ts = new DateTime(userValue.getTs());
                                JsonSample originalSample = map.get(ts);
                                String note = "";
                                if (originalSample != null) {
                                    note += originalSample.getNote();
                                }

                                JsonSample jsonSample = new JsonSample();
                                jsonSample.setTs(userValue.getTs());
                                jsonSample.setValue(userValue.getValue());
                                jsonSample.setNote(note + "," + USER_VALUE);

                                map.remove(ts);
                                map.put(ts, jsonSample);
                            }

                            _jsonResult = new ArrayList<>(map.values());
                        } else {
                            _jsonResult = ds.getSamples(object.getId(), attribute.getType(), from, to, LIMIT);
                        }

                        logger.info("Input result: {}", _jsonResult.size());
                    } catch (Exception ex) {
                        logger.error("Error when creating sample list for object {}:{} and attribute {}", object.getName(), object.getId(), attribute.getType(), ex);
                    }
                } else {
                    logger.warn("Missing options {} and {}", object, attribute);
                }
            } catch (Exception ex) {
                logger.error("Error when creating sample list for object {} and attribute {}", object, attribute, ex);
            }
        }
        return _jsonResult;
    }
}
