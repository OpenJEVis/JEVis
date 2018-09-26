/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevCalendar;
import envidatec.jevis.capi.data.JevDataMap;
import envidatec.jevis.capi.data.JevSample;
import envidatec.jevis.capi.data.TimeSet;
import envidatec.jevis.capi.nodes.NodeManager;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.util.*;



/**
 * Diese Klasse hält die verschiedenen möglichen Funktionen!
 *
 * @author broder
 */
public class Datasource {

    private RegTreeNode node;
    private long id;
    private String unit;
    private String identifier;
    private String name;
    private Value value;
    private Timestamp timestamp;
    private ValueOld valueOld;
    private TimestampOld timestampOld;
    private ValueOldOld valueOldOld;
    private TimestampOldOld timestampOldOld;
    private ValueLastYear valueLastYear;
    private TimestampLastYear timeStampLastYear;
    private TimestampOldLastYear timestampOldLastYear;
    private ValueOldLastYear valueOldLastYear;
    private TimeSet timeSet;
//    private boolean isDataAvailable;
    private JevDataMap map;
    private JevDataMap oldMap;
    private JevDataMap oldOldMap;
    private JevDataMap lastYearMap;
    private JevDataMap oldLastYearMap;
    private JevDataMap siMap;
    private JevDataMap siOldMap;
    private JevDataMap siOldOldMap;
    private JevDataMap siLastYearMap;
    private JevDataMap siOldLastYearMap;
    private List<JevSample<Object>> sampleList;
    private boolean ignoreTimestamp;
    private static final Logger _logger = Logger.getLogger(ReportHandler.class.getName());

    public Datasource(RegTreeNode r, String identi, Property property) {
        ignoreTimestamp = false;
        node = r;


        name = ((RegTreeNode) r.getParent().getParent()).getName(); //Todo muss noch angepasst werden, wenn man nicht mehr direkt die Datenreihe auswählen muss
        id = r.getID();

        timeSet = property.getTimeSet();

//        logger.info("CURRENT VAL" + r.getCurrentValue());
        //überprüfen, ob datenreihe auch wirklich die entsprechenden Werte enthält(also zB monatswerte usw)
        identifier = identi;


//        logger.info("Time from " + time.getFrom().toString());
//        logger.info("Time to " + time.getUntil());
//        logger.info("Nodeid " + r.getID());

        siMap = NodeManager.getInstance().registryDataRequest(r, timeSet);
//        logger.info("--SAMPLELIST--SIMAP- " + siMap.size());
//        map = NodeManager.getInstance().registryDataRequest(r, time);
        map = r.getDisplayUnit().convertFromSIMap(siMap);
//        logger.info("--SAMPLELIST--Map-- " + map.size());
        sampleList = map.getListOfSamples();
//        logger.info("--SAMPLELIST--SITE-- " + sampleList.size());
        siOldMap = NodeManager.getInstance().registryDataRequest(r, property.getOldTimeSet());
        oldMap = r.getDisplayUnit().convertFromSIMap(siOldMap);

        siOldOldMap = NodeManager.getInstance().registryDataRequest(r, property.getOldOldTimeSet());
        oldOldMap = r.getDisplayUnit().convertFromSIMap(siOldOldMap);

        siLastYearMap = NodeManager.getInstance().registryDataRequest(r, property.getLastYearTimeSet());
        lastYearMap = r.getDisplayUnit().convertFromSIMap(siLastYearMap);

        siOldLastYearMap = NodeManager.getInstance().registryDataRequest(r, property.getOldLastYearTimeSet());
        oldLastYearMap = r.getDisplayUnit().convertFromSIMap(siOldLastYearMap);

        unit = r.getDisplayUnit().getUnit().toString();
        _logger.log(Level.FINEST, "Unit " + unit);
    }

    Datasource(RegTreeNode registryNode, String identi) {
        node = registryNode;
        ignoreTimestamp = true;
        id = node.getID();
        identifier = identi;
        siMap = new JevDataMap(null);
        siOldMap = new JevDataMap(null);
        siOldOldMap = new JevDataMap(null);
        siLastYearMap = new JevDataMap(null);
        siOldLastYearMap = new JevDataMap(null);
        oldMap = new JevDataMap(null);
        oldOldMap = new JevDataMap(null);
        lastYearMap = new JevDataMap(null);
        oldLastYearMap = new JevDataMap(null);
        name = ((RegTreeNode) node.getParent().getParent()).getName();
        if (node.isPropertyNode()) {
            sampleList = new ArrayList<JevSample<Object>>();

            JevSample<Object> tmp = node.getCurrentValue();
            sampleList.add(tmp);
        } else {
            TimeSet set = node.getTimeSet();
            JevCalendar from = new JevCalendar(new Date(set.getFrom().getTimeInMillis() - 1000000000
                    - Calendar.getInstance().getTimeZone().getOffset(set.getFrom().clone().getTimeInMillis())));
            JevCalendar until = new JevCalendar(new Date(set.getUntil().getTimeInMillis() + 1000000000
                    - Calendar.getInstance().getTimeZone().getOffset(set.getUntil().clone().getTimeInMillis())));
            TimeSet newSet = new TimeSet(from, until);
//            logger.info("TimeSet Kennzahl von " + newSet.getFrom());
//            logger.info("TimeSet Kennzahl bis " + newSet.getUntil());
            siMap = NodeManager.getInstance().registryDataRequest(node, newSet);
            map = NodeManager.getInstance().registryDataRequest(node, newSet);
//        map = NodeManager.getInstance().registryDataRequest(r, time);
//            map = node.getDisplayUnit().convertFromSIMap(siMap);
            sampleList = map.getListOfSamples();
            unit = null;
            try {
                unit = node.getDisplayUnit().getUnit().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void init() {
        _logger.log(Level.FINEST, "Init Datasource for Node " + node.getID());
        value = new Value(sampleList, siMap);
        timestamp = new Timestamp(sampleList);
        valueOld = new ValueOld(oldMap.getListOfSamples(), siOldMap);
        timestampOld = new TimestampOld(oldMap.getListOfSamples());
        valueOldOld = new ValueOldOld(oldOldMap.getListOfSamples(), siOldOldMap);
        timestampOldOld = new TimestampOldOld(oldOldMap.getListOfSamples());
        valueLastYear = new ValueLastYear(lastYearMap.getListOfSamples(), siLastYearMap);
        timeStampLastYear = new TimestampLastYear(lastYearMap.getListOfSamples());
        valueOldLastYear = new ValueOldLastYear(oldLastYearMap.getListOfSamples(), siOldLastYearMap);
        timestampOldLastYear = new TimestampOldLastYear(oldLastYearMap.getListOfSamples());
    }

    public String getIdentifier() {
        return identifier;
    }

    public long getid() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public Value getValue() {
        return value;
    }

    public ValueOld getValueOld() {
        return valueOld;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public TimestampOld getTimestampOld() {
        return timestampOld;
    }

//    public boolean isDataAvailable() {
//        return isDataAvailable;
//    }
    public ValueLastYear getValueLastYear() {
        return valueLastYear;
    }

    public TimestampLastYear getTimestampLastYear() {
        return timeStampLastYear;
    }

    public TimestampOldOld getTimestampOldOld() {
        return timestampOldOld;
    }

    public ValueOldOld getValueOldOld() {
        return valueOldOld;
    }

    public TimestampOldLastYear getTimestampOldLastYear() {
        return timestampOldLastYear;
    }

    public ValueOldLastYear getValueOldLastYear() {
        return valueOldLastYear;
    }

    public boolean ignoreTimestamp() {
        return ignoreTimestamp;
    }
}
