package org.jevis.jeconfig.plugin.metersv2.data;

import org.jevis.api.JEVisType;

import java.util.Objects;

public class JEVisTypeWrapper {
    public JEVisType getJeVisType() {
        return jeVisType;
    }

    private final JEVisType jeVisType;

    private String name;

    public JEVisTypeWrapper(JEVisType jeVisType) {
        this.jeVisType = jeVisType;
        try {
            this.name = jeVisType.getName();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {

        if(o.getClass() != this.getClass()) return false;
        JEVisTypeWrapper object = (JEVisTypeWrapper) o;
       return name.equals(object.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
}
