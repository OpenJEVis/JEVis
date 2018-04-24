/*
  Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>

  This file is part of JECommons.

  JECommons is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  JECommons. If not, see <http://www.gnu.org/licenses/>.

  JECommons is part of the OpenJEVis project, further project information are
  published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.iso.add;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.ws.sql.SQLDataSource;

/**
 * @author fs & <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class TargetHelper {

    private final static String SEPERATOR = ":";
    private JsonObject targetObject;
    private JsonAttribute targetAttribute;
    private SQLDataSource ds;
    private String sourceValue = "";
    private boolean isAttribute = false;
    private boolean isObject = false;
    private boolean targetIsAccessable = false;
    private boolean isValid = true;

    private Long targetObjectID = -1l;
    private String targetAttibute = "";

    /**
     * Creates an new TargetHelper. It will get the last value of the given
     * attribute to parse the target.
     *
     * @param ds
     * @param sourceAtt attribute which holds the target String
     */
    public TargetHelper(SQLDataSource ds, JsonAttribute sourceAtt) {
        JsonAttribute sourceAtt1 = sourceAtt;
        this.ds = ds;

        if (sourceAtt != null) {
            JsonSample lastV = sourceAtt.getLatestValue();
            if (lastV != null) {
                sourceValue = lastV.getValue();
            }
        }

        parse();
    }

    /**
     * Creates an new TargetHelper.
     *
     * @param ds
     * @param value String who holds an target string "[ObjectID]:[At]"
     */
    public TargetHelper(SQLDataSource ds, String value) {
        this.sourceValue = value;
        this.ds = ds;
        parse();
    }

    public TargetHelper(SQLDataSource ds, JsonObject newTargetObj, JsonAttribute newTargetAtt) {

        if (newTargetObj != null) {
            this.sourceValue = String.valueOf(newTargetObj.getId());
        }

        if (newTargetAtt != null) {
            this.sourceValue += ":" + newTargetAtt.getType();
        }

        this.ds = ds;
        parse();
    }

    public String getSourceString() {
        return this.sourceValue;
    }

    private void parse() {
        //is emty
        if (sourceValue.isEmpty() == false) {
            //has seperator
            if (sourceValue.contains(SEPERATOR)) {
                int seperator = sourceValue.indexOf(":");
                String objectStrg = sourceValue.substring(0, seperator);
                try {
                    targetObjectID = Long.parseLong(objectStrg);
                } catch (Exception ex) {
                    isValid = false;
                }

                if ((seperator + 1) < sourceValue.length()) {
                    targetAttibute = sourceValue.substring(seperator + 1, sourceValue.length());
                } else {
                    isValid = false;
                }

            } else {
                try {
                    targetObjectID = Long.parseLong(sourceValue);
                } catch (Exception ex) {
                    isValid = false;
                }
            }

            //check if the target Exist
            try {
                targetObject = ds.getObject(targetObjectID);
                isObject = true;
                targetIsAccessable = true;
            } catch (Exception ex) {
                targetIsAccessable = false;
            }

            if (!targetAttibute.isEmpty() && targetObject != null) {
                try {
                    targetAttribute = targetObject.getAttributes().get(targetObject.getAttributes().indexOf(targetAttribute));
                    if (targetAttribute == null) {
                        isValid = false;
                        targetIsAccessable = false;
                    } else {
                        isAttribute = true;
                    }
                } catch (Exception ex) {
                    isValid = false;

                }
            }

        }

    }

    public boolean isValid() {
        return isValid;
    }

    public boolean targetAccessable() {
        return targetIsAccessable;
    }

    public boolean hasAttribute() {
        return isAttribute;
    }

    public boolean hasObject() {
        return isObject;
    }

    public JsonAttribute getAttribute() {
        return targetAttribute;
    }

    public JsonObject getObject() {
        return targetObject;
    }

    @Override
    public String toString() {
        return "TargetHelper{" +
                "targetObject=" + targetObject +
                ", targetAttribute=" + targetAttribute +
                ", sourceValue='" + sourceValue + '\'' +
                ", isAttribute=" + isAttribute +
                ", isObject=" + isObject +
                ", targetIsAccessable=" + targetIsAccessable +
                ", isValid=" + isValid +
                ", targetObjectID=" + targetObjectID +
                ", targetAttibute='" + targetAttibute + '\'' +
                '}';
    }
}
