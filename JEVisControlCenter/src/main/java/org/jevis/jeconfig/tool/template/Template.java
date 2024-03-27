package org.jevis.jeconfig.tool.template;

import javafx.scene.Node;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public abstract class Template {

    public String getName() {
        return "";
    }

    public boolean create(JEVisClass jclass, JEVisObject parent, String name) throws JEVisException, InterruptedException {
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

    public Map<String, Node> getOptions() {
        return new HashMap<>();
    }

    public boolean isNotAnTemplate() {
        return false;
    }
}
