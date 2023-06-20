package org.jevis.commons.driver;

import org.apache.logging.log4j.LogManager;

import java.util.Map;

public class VarFiller {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(VarFiller.class);
    private String uriString = "";
    private Map<Variable, VarFunction> values;

    public VarFiller(String uriString, Map<Variable, VarFunction> values) {
        this.uriString = uriString;
        this.values = values;
    }

    public String getFilledURIString() {
        String filledStrg = uriString;
        for (Map.Entry<Variable, VarFunction> variableVarFunctionEntry : values.entrySet()) {
            try {
                filledStrg = filledStrg.replaceAll("\\{" + variableVarFunctionEntry.getKey().toString() + "}"
                        , variableVarFunctionEntry.getValue().getVarValue());
            } catch (Exception ex) {
                logger.error("Error in Variable: {}", variableVarFunctionEntry.getKey(), ex, ex);
            }
        }


        return filledStrg;
    }

    public interface VarFunction {

        public String getVarValue();
    }

    public enum Variable {
        LAST_TS,// Last timestamp of the existing data in JEVis
        CURRENT_TS
    }
}
