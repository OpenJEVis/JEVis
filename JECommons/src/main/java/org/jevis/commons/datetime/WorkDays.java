package org.jevis.commons.datetime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import java.time.LocalTime;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

public class WorkDays {

    private static final Logger logger = LogManager.getLogger(WorkDays.class);
    private JEVisObject currentObject;
    private SQLDataSource sqlDataSource;
    private JsonObject currentJsonObject;
    private JEVisObject nextSiteParent;
    private JsonObject nextJsonSiteParent;
    private JEVisClass siteClass;
    private final LocalTime workdayStartDisabled = LocalTime.of(0, 0, 0, 0);
    private final LocalTime workdayEndDisabled = LocalTime.of(23, 59, 59, 999999999);
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private boolean enabled = true;

    public WorkDays(JEVisObject currentObject) {
        this.currentObject = currentObject;
        if (currentObject != null) {
            try {
                siteClass = currentObject.getDataSource().getJEVisClass("Building");
            } catch (JEVisException e) {
                logger.fatal("Could not get JEVisClass for Building");
            }
        }

        getWorkDays();
    }

    public WorkDays(SQLDataSource sqlDataSource, JsonObject currentObject) {
        this.sqlDataSource = sqlDataSource;
        this.currentJsonObject = currentObject;

        getJsonWorkDays();
    }

    private void getWorkDays() {
        if (currentObject != null) {
            try {
                JEVisObject site = getNextSiteRecursive(currentObject);
                LocalTime start = null;
                LocalTime end = null;
                if (site != null) {
                    try {
                        JEVisAttribute attStart = site.getAttribute("Workday Beginning");
                        JEVisAttribute attEnd = site.getAttribute("Workday End");
                        if (attStart.hasSample()) {
                            String startStr = attStart.getLatestSample().getValueAsString();
                            DateTime dtStart = DateTime.parse(startStr);
                            start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                        }
                        if (attEnd.hasSample()) {
                            String endStr = attEnd.getLatestSample().getValueAsString();
                            DateTime dtEnd = DateTime.parse(endStr);
                            end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                        }
                    } catch (Exception e) {
                        logger.error("Could not get start and end for Building {}:{}", site.getName(), site.getID(), e);
                    }
                } else {
                    logger.warn("Could not get site object for object {}:{}.", currentObject.getName(), currentObject.getID());
                }

                if (start != null && end != null) {
                    workdayStart = start;
                    workdayEnd = end;
                }
            } catch (Exception e) {
                logger.error("Could not get site for current object {}:{}", currentObject.getName(), currentObject.getID(), e);
            }
        }
    }

    private void getJsonWorkDays() {
        if (currentJsonObject != null) {
            try {
                JsonObject site = getNextJsonSiteRecursive(currentJsonObject);
                LocalTime start = null;
                LocalTime end = null;
                if (site != null) {
                    try {
                        JsonAttribute attStart = sqlDataSource.getAttribute(site.getId(), "Workday Beginning");
                        JsonAttribute attEnd = sqlDataSource.getAttribute(site.getId(), "Workday End");
                        if (attStart.getLatestValue() != null) {
                            String startStr = attStart.getLatestValue().getValue();
                            DateTime dtStart = DateTime.parse(startStr);
                            start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                        }
                        if (attEnd.getLatestValue() != null) {
                            String endStr = attEnd.getLatestValue().getValue();
                            DateTime dtEnd = DateTime.parse(endStr);
                            end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                        }
                    } catch (Exception e) {
                        logger.error("Could not get start and end for Building {}:{}", site.getName(), site.getId(), e);
                    }
                } else {
                    logger.warn("Could not get site object for object {}:{}.", currentObject.getName(), currentObject.getID());
                }

                if (start != null && end != null) {
                    workdayStart = start;
                    workdayEnd = end;
                }
            } catch (Exception e) {
                logger.error("Could not get site for current object {}:{}", currentObject.getName(), currentObject.getID(), e);
            }
        }
    }

    private JEVisObject getNextSiteRecursive(JEVisObject object) throws JEVisException {

        for (JEVisObject parent : object.getParents()) {
            if (parent.getJEVisClass().equals(siteClass)) {
                nextSiteParent = parent;
                break;
            } else if (nextSiteParent != null) {
                break;
            } else {
                getNextSiteRecursive(parent);
            }
        }

        return nextSiteParent;
    }

    private JsonObject getNextJsonSiteRecursive(JsonObject object) throws JEVisException {
        for (JsonRelationship rel : sqlDataSource.getRelationships(object.getId())) {
            JsonObject parent = sqlDataSource.getObject(rel.getTo());
            if (rel.getFrom() == object.getId() && rel.getType() == 1 && parent.getJevisClass().equals("Building")) {
                nextJsonSiteParent = parent;
                break;
            } else if (rel.getFrom() == object.getId() && rel.getType() == 1) {
                getNextJsonSiteRecursive(parent);
            } else if (nextJsonSiteParent != null) {
                break;
            }
        }

        return nextJsonSiteParent;
    }

    public LocalTime getWorkdayStart() {
        if (enabled) {
            return workdayStart;
        } else {
            return workdayStartDisabled;
        }
    }

    public LocalTime getWorkdayEnd() {
        if (enabled) {
            return workdayEnd;
        } else {
            return workdayEndDisabled;
        }
    }

    public void setWorkdayEnd(LocalTime workdayEnd) {
        this.workdayEnd = workdayEnd;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
