package org.jevis.jeconfig.plugin.metersv2;

import org.jevis.api.JEVisClass;

public class JEVisClassWrapper {
    JEVisClass jeVisClass;

    public JEVisClassWrapper(JEVisClass jeVisClass) {
        this.jeVisClass = jeVisClass;
    }

    @Override
    public String toString() {
        try {
            return jeVisClass.getName();

        }catch (Exception e){
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
