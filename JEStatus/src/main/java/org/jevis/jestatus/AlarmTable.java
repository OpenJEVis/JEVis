package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmTable.class);
    final String tableCSS = "background-color:#FFF;"
            + "text-color: #024457;"
            + "outer-border: 1px solid #167F92;"
            + "empty-cells:show;"
            + "border-collapse:collapse;"
            //                + "border: 2px solid #D9E4E6;"
            + "cell-border: 1px solid #D9E4E6";
    final String headerCSS = "background-color: #1a719c;"
            + "color: #FFF;";
    final String rowCss = "text-color: #024457;padding: 5px;";//"border: 1px solid #D9E4E6;"
    final String highlight = "background-color: #EAF3F3";
    final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private List<JEVisObject> listCheckedData = new ArrayList<>();
    private String tableString;

    private boolean isJEVisClassOrInherit(JEVisObject obj, JEVisClass jclass2) {
        try {
            return isJEVisClassOrInherit(obj.getJEVisClass(), jclass2);
        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }
    }

    private boolean isJEVisClassOrInherit(JEVisClass jclass, JEVisClass jclass2) {
        try {
            if (jclass.equals(jclass2)) {
                return true;
            }

            return jclass2.getHeirs().contains(jclass);

        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }

    }

    public List<JEVisObject> getListCheckedData() {
        return listCheckedData;
    }

    String getParentName(JEVisObject obj, JEVisClass jclass) {
        StringBuilder name = new StringBuilder();
        try {
            for (JEVisObject parent : obj.getParents()) {
                if (isJEVisClassOrInherit(parent, jclass)) {
                    name.append(parent.getName());
                } else {
                    name.append(getParentName(parent, jclass));
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        return name.toString();
    }

    public String getTableString() {
        return tableString;
    }

    public void setTableString(String tableString) {
        this.tableString = tableString;
    }
}
