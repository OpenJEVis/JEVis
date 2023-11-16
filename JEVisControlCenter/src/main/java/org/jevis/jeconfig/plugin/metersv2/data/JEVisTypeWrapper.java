package org.jevis.jeconfig.plugin.metersv2.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisType;

public class JEVisTypeWrapper {

    private static final Logger logger = LogManager.getLogger(JEVisTypeWrapper.class);
    private final JEVisType jeVisType;
    private String name;

    public JEVisTypeWrapper(JEVisType jeVisType) {
        this.jeVisType = jeVisType;
        if (jeVisType == null) return;
        try {
            this.name = jeVisType.getName();

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public JEVisType getJeVisType() {
        return jeVisType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) return false;
        return ((JEVisTypeWrapper) o).getName().equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    @Override
    public String toString() {
        return "JEVisTypeWrapper{" +
                "jeVisType=" + jeVisType +
                ", name='" + name + '\'' +
                '}';
    }
}
