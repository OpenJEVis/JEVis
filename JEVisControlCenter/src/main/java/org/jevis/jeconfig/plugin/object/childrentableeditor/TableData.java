package org.jevis.jeconfig.plugin.object.childrentableeditor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.ClassHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableData {

    private static final Logger logger = LogManager.getLogger(TableData.class);
    private final Map<Long, Long> calcMap;
    private final Map<Long, Long> targetLoytecXML;
    private final Map<Long, Long> targetOPCUA;
    private JEVisObject object = null;
    private final Map<Long, Long> targetVIDA;
    private final Map<Long, Long> targetCSV;
    private final Map<Long, Long> targetXML;
    private final Map<Long, Long> targetDWD;
    private final Map<Long, Long> targetDataPoint;
    private String sourceString = "";
    private String sourceDetailed = "";
    private String classString = "";
    private List<JEVisAttribute> attributeList = new ArrayList<>();
    private JEVisDataSource ds;

    public TableData(JEVisObject object, Map<Long, Long> calcMap, Map<Long, Long> targetLoytecXML, Map<Long, Long> targetOPCUA,
                     Map<Long, Long> targetVIDA, Map<Long, Long> targetCSV, Map<Long, Long> targetXML,
                     Map<Long, Long> targetDWD, Map<Long, Long> targetDataPoint) {
        this.object = object;
        this.calcMap = calcMap;
        this.targetLoytecXML = targetLoytecXML;
        this.targetOPCUA = targetOPCUA;
        this.targetVIDA = targetVIDA;
        this.targetCSV = targetCSV;
        this.targetXML = targetXML;
        this.targetDWD = targetDWD;
        this.targetDataPoint = targetDataPoint;
        try {
            this.ds = object.getDataSource();
            this.attributeList = object.getAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String resultString = object.getJEVisClassName();
            try {
                resultString = I18nWS.getInstance().getClassName(resultString);
            } catch (Exception e) {
                logger.error("Could not get class name for {} class", resultString, e);
            }

            if (ClassHelper.isDirectory(object.getJEVisClass())) {
                resultString += " (" + I18n.getInstance().getString("plugin.graph.dialog.new.directory") + ")";
            }
            this.classString = resultString;
        } catch (Exception ex) {
            logger.error(ex);
        }

        try {
            String jeVisClassName = object.getJEVisClassName();
            String resultString = "";
            String sourceDetails = "";
            if (jeVisClassName.equals("Data")) {
                boolean hasPreviousResult = false;
                JEVisObject sourceObject = ds.getObject(calcMap.get(object.getID()));
                if (sourceObject != null) {
                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);

                    hasPreviousResult = true;
                }

                sourceObject = null;
                sourceObject = ds.getObject(targetLoytecXML.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }

                sourceObject = null;
                sourceObject = ds.getObject(targetOPCUA.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }
                sourceObject = null;
                sourceObject = ds.getObject(targetVIDA.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }
                sourceObject = null;
                sourceObject = ds.getObject(targetCSV.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }
                sourceObject = null;
                sourceObject = ds.getObject(targetXML.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }
                sourceObject = null;
                sourceObject = ds.getObject(targetDWD.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }

                sourceObject = null;
                sourceObject = ds.getObject(targetDataPoint.get(object.getID()));
                if (sourceObject != null) {
                    if (hasPreviousResult) resultString += "\n";
                    else hasPreviousResult = true;

                    resultString += getSourceDetails(sourceObject);
                    sourceDetails += getSourceDetails(sourceObject);
                    sourceDetails += getSourceAttributes(sourceObject);
                }

                this.sourceString = resultString;
                this.sourceDetailed = sourceDetails;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private String getSourceDetails(JEVisObject sourceObject) throws JEVisException {
        return sourceObject.getName() + " (" + I18nWS.getInstance().getClassName(sourceObject.getJEVisClass()) + "):" + sourceObject.getID();
    }

    private String getSourceAttributes(JEVisObject sourceObject) throws JEVisException {
        StringBuilder resultString = new StringBuilder();

        for (JEVisAttribute attribute : sourceObject.getAttributes()) {
            resultString.append("\n");
            resultString.append(I18nWS.getInstance().getAttributeName(attribute));
            resultString.append(": ");
            if (attribute.hasSample()) {
                resultString.append(attribute.getLatestSample().getValueAsString());
                resultString.append("@");
                resultString.append(attribute.getLatestSample().getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
            }
        }

        return resultString.toString();
    }

    public JEVisObject getObject() {
        return this.object;
    }

    public String getClassString() {
        return classString;
    }

    public List<JEVisAttribute> getAttributeList() {
        return attributeList;
    }

    public String getSourceString() {
        return sourceString;
    }

    public String getSourceDetailed() {
        return sourceDetailed;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }
}
