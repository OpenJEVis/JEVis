package org.jevis.commons.datetime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
    private JEVisClass organisationClass;
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private boolean enabled = true;
    private DateTimeZone dateTimeZone = DateTimeZone.getDefault();

    public WorkDays(JEVisObject currentObject) {
        this.currentObject = currentObject;
        if (currentObject != null) {
            try {
                siteClass = currentObject.getDataSource().getJEVisClass("Building");
                organisationClass = currentObject.getDataSource().getJEVisClass("Organization");
            } catch (Exception e) {
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

                if (site != null) {
                    setStartAndEndForSite(site);
                } else {
                    logger.warn("Could not get site object parent for object {}:{}. Trying to get next child site", currentObject.getName(), currentObject.getID());

                    JEVisObject organisation = CommonMethods.getFirstParentalObjectOfClass(currentObject, "Organization");

                    if (organisation != null) {
                        site = getNextChildSiteRecursive(organisation);
                        if (site != null) {
                            setStartAndEndForSite(site);
                        } else {
                            logger.warn("Could not get site object parent for object {}:{}.", currentObject.getName(), currentObject.getID());

                            for (JEVisObject foundSite : currentObject.getDataSource().getObjects(siteClass, false)) {
                                logger.warn("Falling back to first visible site {}:{}.", foundSite.getName(), foundSite.getID());
                                setStartAndEndForSite(foundSite);
                                break;
                            }

                        }
                    } else {
                        logger.warn("Could not get site object parent for object {}:{}.", currentObject.getName(), currentObject.getID());
                    }
                }
            } catch (Exception e) {
                logger.error("Could not get site for current object {}:{}", currentObject.getName(), currentObject.getID(), e);
            }
        }
    }

    private void setStartAndEndForSite(JEVisObject site) {
        try {
            JEVisAttribute attStart = site.getAttribute("Workday Beginning");
            JEVisAttribute attEnd = site.getAttribute("Workday End");
            JEVisAttribute zoneAtt = site.getAttribute("Timezone");
            LocalTime start = null;
            LocalTime end = null;

            if (attStart.hasSample()) {
                String startStr = attStart.getLatestSample().getValueAsString();
                DateTime dtStart = DateTime.parse(startStr);
                start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), dtStart.getSecondOfMinute(), 0);
            }
            if (attEnd.hasSample()) {
                String endStr = attEnd.getLatestSample().getValueAsString();
                DateTime dtEnd = DateTime.parse(endStr);
                end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), dtEnd.getSecondOfMinute(), 0);
            }
            if (zoneAtt.hasSample()) {
                String zoneStr = zoneAtt.getLatestSample().getValueAsString();
                dateTimeZone = DateTimeZone.forID(zoneStr);
            }

            if (start != null && end != null) {
                workdayStart = start;
                workdayEnd = end;
            }
        } catch (Exception e) {
            logger.error("Could not get start and end for Building {}:{}", site.getName(), site.getID(), e);
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
                        JsonAttribute zoneAtt = sqlDataSource.getAttribute(site.getId(), "Timezone");
                        if (attStart.getLatestValue() != null) {
                            String startStr = attStart.getLatestValue().getValue();
                            DateTime dtStart = DateTime.parse(startStr);
                            start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), dtStart.getSecondOfMinute(), 0);
                        }
                        if (attEnd.getLatestValue() != null) {
                            String endStr = attEnd.getLatestValue().getValue();
                            DateTime dtEnd = DateTime.parse(endStr);
                            end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), dtEnd.getSecondOfMinute(), 0);
                        }
                        if (zoneAtt.getLatestValue() != null) {
                            String zoneStr = zoneAtt.getLatestValue().getValue();
                            dateTimeZone = DateTimeZone.forID(zoneStr);
                        }
                    } catch (Exception e) {
                        logger.error("Could not get start and end for Building {}:{}", site.getName(), site.getId(), e);
                    }
                } else {
                    logger.warn("Could not get site object for object {}:{}.", currentJsonObject.getName(), currentJsonObject.getId());
                }

                if (start != null && end != null) {
                    workdayStart = start;
                    workdayEnd = end;
                }
            } catch (Exception e) {
                logger.error("Could not get site for current object {}:{}", currentJsonObject.getName(), currentJsonObject.getId(), e);
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

    private JEVisObject getNextChildSiteRecursive(JEVisObject object) throws JEVisException {

        for (JEVisObject child : object.getChildren()) {
            if (child.getJEVisClass().equals(siteClass)) {
                nextSiteParent = child;
                break;
            } else if (nextSiteParent != null) {
                break;
            } else {
                getNextChildSiteRecursive(child);
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

    public LocalTime getWorkdayStart(DateTime currentDate) {
        int offset = dateTimeZone.getOffset(currentDate);
        if (enabled && offset < 0) {
            return workdayStart.minusSeconds(offset / 1000);
        } else if (enabled) {
            return workdayStart.plusSeconds(offset / 1000);
        } else {
            return LocalTime.of(0, 0, 0, 0).minusSeconds(offset / 1000);
        }
    }

    public LocalTime getWorkdayStart() {
        if (enabled) {
            return workdayStart;
        } else {
            return LocalTime.of(0, 0, 0, 0);
        }
    }

    public LocalTime getWorkdayEnd(DateTime currentDate) {
        int offset = dateTimeZone.getOffset(currentDate);
        if (enabled && offset < 0) {
            return workdayEnd.minusSeconds(offset / 1000);
        } else if (enabled) {
            return workdayEnd.plusSeconds(offset / 1000);
        } else {
            return LocalTime.of(23, 59, 59, 999999999).minusSeconds(offset / 1000);
        }
    }

    public LocalTime getWorkdayEnd() {
        if (enabled) {
            return workdayEnd;
        } else {
            return LocalTime.of(23, 59, 59, 999999999);
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

    public boolean isCustomWorkDay() {
        return !(workdayStart.equals(LocalTime.of(0, 0, 0, 0)) && workdayEnd.equals(LocalTime.of(23, 59, 59, 999999999)));
    }

    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }
}
