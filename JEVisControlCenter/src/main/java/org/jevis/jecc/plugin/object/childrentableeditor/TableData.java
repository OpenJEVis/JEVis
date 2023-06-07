package org.jevis.jecc.plugin.object.childrentableeditor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.classes.ClassHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.CalcMethods;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.application.application.I18nWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableData {

    private static final Logger logger = LogManager.getLogger(TableData.class);
    private final Map<Long, List<Long>> calcMap;
    private final Map<Long, List<Long>> targetLoytecXML;
    private final Map<Long, List<Long>> targetOPCUA;
    private final Map<Long, List<Long>> targetVIDA;
    private final Map<Long, List<Long>> targetCSV;
    private final Map<Long, List<Long>> targetXML;
    private final Map<Long, List<Long>> targetDWD;
    private final Map<Long, List<Long>> targetDataPoint;
    private JEVisObject object = null;
    private String sourceString = "";
    private String sourceDetailed = "";
    private String classString = "";
    private List<JEVisAttribute> attributeList = new ArrayList<>();
    private JEVisDataSource ds;
    private boolean duplicate = false;
    private long sampleCount = 0l;
    private DateTime minTs;
    private DateTime maxTs;
    private DateTime minTsOverall;
    private DateTime maxTsOverall;

    public TableData(JEVisObject object, Map<Long, List<Long>> calcMap, Map<Long, List<Long>> targetLoytecXML, Map<Long, List<Long>> targetOPCUA,
                     Map<Long, List<Long>> targetVIDA, Map<Long, List<Long>> targetCSV, Map<Long, List<Long>> targetXML,
                     Map<Long, List<Long>> targetDWD, Map<Long, List<Long>> targetDataPoint) {
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
            StringBuilder resultString = new StringBuilder();
            StringBuilder sourceDetails = new StringBuilder();
            if (jeVisClassName.equals("Data")) {
                boolean hasPreviousResult = false;
                List<JEVisObject> sourceObjects = new ArrayList<>();
                if (calcMap.get(object.getID()) != null) {
                    for (Long id : calcMap.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetLoytecXML.get(object.getID()) != null) {
                    for (Long id : targetLoytecXML.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetOPCUA.get(object.getID()) != null) {
                    for (Long id : targetOPCUA.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetVIDA.get(object.getID()) != null) {
                    for (Long id : targetVIDA.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetCSV.get(object.getID()) != null) {
                    for (Long id : targetCSV.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetXML.get(object.getID()) != null) {
                    for (Long id : targetXML.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetDWD.get(object.getID()) != null) {
                    for (Long id : targetDWD.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }
                if (targetDataPoint.get(object.getID()) != null) {
                    for (Long id : targetDataPoint.get(object.getID())) {
                        sourceObjects.add(ds.getObject(id));
                    }
                }

                for (JEVisObject sourceObject : sourceObjects) {
                    if (hasPreviousResult) resultString.append(" \n");
                    else hasPreviousResult = true;

                    String string = getSourceDetails(sourceObject);
                    resultString.append(string);
                    sourceDetails.append(string);
                    sourceDetails.append(getSourceAttributes(sourceObject));
                }

                if (!sourceObjects.isEmpty() && sourceObjects.size() > 1) {
                    duplicate = true;
                }

                this.sourceString = resultString.toString();
                this.sourceDetailed = sourceDetails.toString();
            } else if (jeVisClassName.equals("Clean Data")) {
                JEVisObject firstParentalDataObject = CommonMethods.getFirstParentalDataObject(object);
                if (firstParentalDataObject != null) {
                    this.sourceString = firstParentalDataObject.getName();
                }
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
            resultString.append(" \n");
            resultString.append(I18nWS.getInstance().getAttributeName(attribute));
            resultString.append(": \"");
            if (attribute.hasSample()) {
                if (attribute.getName().equals(CalcJobFactory.Calculation.EXPRESSION.getName()) && sourceObject.getJEVisClassName().equals("Calculation")) {
                    resultString.append(CalcMethods.getTranslatedFormula(sourceObject));
                } else {
                    resultString.append(attribute.getLatestSample().getValueAsString());
                }
                resultString.append("\"@\"");
                resultString.append(attribute.getLatestSample().getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
                resultString.append("\"");
            }
        }

        return resultString.toString();
    }

    public JEVisObject getObject() {
        return this.object;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
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

    public boolean isDuplicate() {
        return duplicate;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public DateTime getMinTs() {
        return minTs;
    }

    public void setMinTs(DateTime minTs) {
        this.minTs = minTs;
    }

    public DateTime getMaxTs() {
        return maxTs;
    }

    public void setMaxTs(DateTime maxTs) {
        this.maxTs = maxTs;
    }

    public DateTime getMinTsOverall() {
        return minTsOverall;
    }

    public void setMinTsOverall(DateTime minTsOverall) {
        this.minTsOverall = minTsOverall;
    }

    public DateTime getMaxTsOverall() {
        return maxTsOverall;
    }

    public void setMaxTsOverall(DateTime maxTsOverall) {
        this.maxTsOverall = maxTsOverall;
    }
}
