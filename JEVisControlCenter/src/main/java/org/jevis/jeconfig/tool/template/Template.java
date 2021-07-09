package org.jevis.jeconfig.tool.template;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public abstract class Template {

    public String getName() {
        return "";
    }

    public boolean create(JEVisClass jclass, JEVisObject parent, String name) throws JEVisException {
        return false;
    }

    public boolean supportsClass(JEVisClass jclass) throws JEVisException {
        return false;
    }

    public void setAttribute(JEVisObject object, String attribute, DateTime ts, Object value) throws JEVisException {
        try {
            JEVisAttribute periodAttribute = object.getAttribute(attribute);
            periodAttribute.buildSample(ts, value).commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isNotAnTemplate() {
        return false;
    }
}
