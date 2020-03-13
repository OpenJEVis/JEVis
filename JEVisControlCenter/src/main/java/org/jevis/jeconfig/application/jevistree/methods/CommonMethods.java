package org.jevis.jeconfig.application.jevistree.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.joda.time.DateTime;

import java.util.List;

public class CommonMethods {
    private static final Logger logger = LogManager.getLogger(CommonMethods.class);

    public static JEVisObject getFirstCleanObject(JEVisObject jeVisObject) throws JEVisException {
        for (JEVisObject object : jeVisObject.getChildren()) {
            if (object.getJEVisClassName().equals("Data") || object.getJEVisClassName().equals("Clean data")) {
                return object;
            } else {
                return getFirstCleanObject(object);
            }
        }
        return jeVisObject;
    }

    public static void setEnabled(JEVisObject object, String selectedClass, boolean b) {
        try {
            JEVisAttribute enabled = object.getAttribute("Enabled");
            if (enabled != null) {
                if (object.getJEVisClassName().equals(selectedClass) || selectedClass.equals("All")) {
                    JEVisSample sample = enabled.buildSample(new DateTime(), b);
                    sample.commit();
                }
            }
            for (JEVisObject child : object.getChildren()) {
                setEnabled(child, selectedClass, b);
            }
        } catch (JEVisException e) {
            logger.error("Could not set enabled for {}:{}", object.getName(), object.getID());
        }
    }

    public static void deleteSamplesInList(ProgressForm pForm, DateTime from, DateTime to, List<JEVisObject> list) {
        for (JEVisObject object : list) {
            JEVisAttribute valueAtt = null;
            try {
                valueAtt = object.getAttribute("Value");
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            if (valueAtt != null) {
                if (from == null && to == null) {
                    try {
                        pForm.addMessage("Deleting all samples of object " + object.getName() + ":" + object.getID());
                        valueAtt.deleteAllSample();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                } else if (from != null && to != null) {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        valueAtt.deleteSamplesBetween(from, to);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                } else if (from != null) {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + new DateTime().toString("YYYY-MM-dd HH:mm:ss"));
                        valueAtt.deleteSamplesBetween(from, new DateTime());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + new DateTime(2001, 1, 1, 0, 0, 0).toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        valueAtt.deleteSamplesBetween(new DateTime(2001, 1, 1, 0, 0, 0), to);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }
            pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID());
        }
    }

    public static void deleteAllSamples(ProgressForm pForm, JEVisObject object, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if ((object.getJEVisClassName().equals("Clean Data") && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    pForm.addMessage("Deleting all samples of object " + object.getName() + ":" + object.getID());
                    value.deleteAllSample();
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(pForm, child, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    public static void deleteAllSamples(ProgressForm pForm, JEVisObject object, DateTime from, DateTime to, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if ((object.getJEVisClassName().equals("Clean Data") && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    DateTime f = null;
                    if (from == null) {
                        f = new DateTime(2001, 1, 1, 0, 0, 0);
                    } else {
                        f = from;
                    }

                    DateTime t = null;
                    if (to == null) {
                        t = new DateTime();
                    } else {
                        t = to;
                    }
                    pForm.addMessage("Deleting samples of object " + object.getName() + ":" + object.getID()
                            + " from " + f.toString("YYYY-MM-dd HH:mm:ss") + " to " + t.toString("YYYY-MM-dd HH:mm:ss"));
                    value.deleteSamplesBetween(f, t);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(pForm, child, from, to, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

}
