package org.jevis.jenotifier.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.PrettyError;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

public abstract class ExportLink {

    private static final Logger logger = LogManager.getLogger(ExportLink.class);
    protected JEVisObject linkObject;
    public static String CLASS_NAME = "Export Link";
    public static String TYPE_OPTIONAL = "Optional";
    public static String TYPE_VARIABLE_NAME = "Template Variable Name";
    public static String TYPE_JEVISID = "JEVis ID";


    protected JEVisAttribute attOptional;
    protected JEVisAttribute attJEVsiID;
    protected JEVisAttribute attVarName;

    protected String varname = UUID.randomUUID().toString();
    protected boolean hasVariableName = false;
    protected JEVisAttribute targetAttribute;

    public ExportLink(JEVisObject object) {
        linkObject = object;

//        init();
    }

    public void init() {
        try {
            attOptional = linkObject.getAttribute(TYPE_OPTIONAL);
        } catch (Exception e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
        }

        try {
            attJEVsiID = linkObject.getAttribute(TYPE_JEVISID);
            TargetHelper targetHelper = new TargetHelper(attJEVsiID.getDataSource(), attJEVsiID);
            targetAttribute = targetHelper.getAttribute().get(0);
        } catch (Exception e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
        }

        try {
            attVarName = linkObject.getAttribute(TYPE_VARIABLE_NAME);
            varname = attVarName.getLatestSample().getValueAsString();
            hasVariableName = true;
        } catch (Exception e) {
            logger.error(PrettyError.getJEVisLineFilter(e));
        }


        try {
            initAttributes();
        } catch (Exception ex) {
            logger.error(PrettyError.getJEVisLineFilter(ex));
        }
    }

    abstract Map<DateTime, JEVisSample> getSamples(DateTime from, DateTime until);

    abstract String formatValue(Object sample);

    public boolean isHasVariableName() {
        return hasVariableName;
    }

    public JEVisAttribute getTargetAttribute() {
        return targetAttribute;
    }

    public String getTypeVariableName() {
        return varname;
    }

    abstract int getColumn();

    abstract void initAttributes();
}
