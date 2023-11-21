package org.jevis.jecc.plugin.meters;

import org.jevis.api.JEVisClass;
import org.jevis.jecc.application.application.I18nWS;

public class JEVisClassWrapper {
    JEVisClass jeVisClass;

    public JEVisClassWrapper(JEVisClass jeVisClass) {
        this.jeVisClass = jeVisClass;
    }

    @Override
    public String toString() {
        try {
            return I18nWS.getInstance().getClassName(jeVisClass);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public JEVisClass getJeVisClass() {
        return jeVisClass;
    }

    public void setJeVisClass(JEVisClass jeVisClass) {
        this.jeVisClass = jeVisClass;
    }
}
