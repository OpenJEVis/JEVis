/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

//import envidatec.jevis.capi.nodes.AlarmNode;
import envidatec.jevis.capi.nodes.AlarmNode;
import envidatec.jevis.capi.nodes.INode;
import envidatec.jevis.capi.nodes.NodeManager;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author broder
 */
public class AlarmData {

    List<Object> adfraiseTimestamps = new ArrayList<Object>();
    List<String> adfraiseIds = new ArrayList<String>();
    List<String> adfraiseExplanations = new ArrayList<String>();
    List<String> adfraiseNames = new ArrayList<String>();
    List<Object> adfacknowledgedTimestamps = new ArrayList<Object>();
    List<String> adfacknowledgedIds = new ArrayList<String>();
    List<String> adfacknowledgedExplanations = new ArrayList<String>();
    List<String> adfacknowledgedNames = new ArrayList<String>();
    List<String> budgetraiseValues = new ArrayList<String>();
    List<String> budgetacknowledgedValues = new ArrayList<String>();

    public AlarmData(List<RegTreeNode> alarmNodes, NodeManager nm) {
        for (RegTreeNode t : alarmNodes) {
            AlarmNode alarm = AlarmNode.createAlarmNode(t);

            long reason = alarm.getAlarmReason();

            if (alarm.getAlarmStatus() == AlarmNode.RAISE) {

                String id = ((INode) alarm.getParent()).getID().toString();
                String path = ((INode) alarm.getParent()).getPathToRoot().toString();
                String name = ((INode) alarm.getParent()).getName();
                double alarmTime = AbstractTimestamp.transformTimestampsToExcelTime(alarm.getAlarmTime());

                boolean added = false;
                for (int i = 0; i < adfraiseTimestamps.size(); i++) {
                    if (alarmTime < (Double) adfraiseTimestamps.get(i)) {
                        if (i == 0) {
                            adfraiseTimestamps.add(0, alarmTime);
                            adfraiseIds.add(0, id);
                            adfraiseExplanations.add(0, alarm.getExplanation());
                            adfraiseNames.add(0, name);
                            added = true;
                            break;
                        } else {
                            adfraiseTimestamps.add(i - 1, alarmTime);
                            adfraiseIds.add(i - 1, id);
                            adfraiseExplanations.add(i - 1, alarm.getExplanation());
                            adfraiseNames.add(i - 1, name);
                            added = true;
                            break;
                        }
                    }
                }
                if (!added) {
                    adfraiseTimestamps.add(alarmTime);
                    adfraiseIds.add(id);
                    adfraiseExplanations.add(alarm.getExplanation());
                    adfraiseNames.add(name);
                }
            } else if (alarm.getAlarmStatus() == AlarmNode.ACKNOWLEDGE) {
                String id = ((INode) alarm.getParent()).getID().toString();
                String path = ((INode) alarm.getParent()).getPathToRoot().toString();
                String name = ((INode) alarm.getParent()).getName();
                adfacknowledgedTimestamps.add(alarm.getAlarmTime().toString());
                adfacknowledgedIds.add(id);
                adfacknowledgedExplanations.add(alarm.getExplanation());
                adfacknowledgedNames.add(name);
            }
        }
    }

    public List<Object> getADFRaiseTimestamps() {
        if (adfraiseTimestamps.isEmpty()) {
            adfraiseTimestamps.add("empty");
        }
        return adfraiseTimestamps;
    }

    public List<String> getADFRaiseIds() {
        if (adfraiseIds.isEmpty()) {
            adfraiseIds.add("empty");
        }
        return adfraiseIds;
    }

    public List<String> getADFRaiseNames() {
        if (adfraiseNames.isEmpty()) {
            adfraiseNames.add("empty");
        }
        return adfraiseNames;
    }

    public List<String> getADFRaiseExplanations() {
        if (adfraiseExplanations.isEmpty()) {
            adfraiseExplanations.add("empty");
        }
        return adfraiseExplanations;
    }

    public List<Object> getADFAcknowlegdeTimestamps() {
        if (adfacknowledgedTimestamps.isEmpty()) {
            adfacknowledgedTimestamps.add("empty");
        }
        return adfacknowledgedTimestamps;
    }

    public List<String> getADFAcknowlegdeIds() {
        if (adfacknowledgedIds.isEmpty()) {
            adfacknowledgedIds.add("empty");
        }
        return adfacknowledgedIds;
    }

    public List<String> getADFAcknowlegdeNames() {
        if (adfacknowledgedNames.isEmpty()) {
            adfacknowledgedNames.add("empty");
        }
        return adfacknowledgedNames;
    }

    public List<String> getADFAcknowlegdeExplanations() {
        if (adfacknowledgedExplanations.isEmpty()) {
            adfacknowledgedExplanations.add("empty");
        }
        return adfacknowledgedExplanations;
    }
}
