package org.jevis.jeconfig.tool.template;

public class NullTemplate extends Template {

    @Override
    public boolean isNotAnTemplate() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
