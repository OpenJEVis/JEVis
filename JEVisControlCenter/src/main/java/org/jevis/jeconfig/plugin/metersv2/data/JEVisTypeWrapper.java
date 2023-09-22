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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() != this.getClass()) return false;
        if(((JEVisTypeWrapper) o).getName().equals(name)) return true;
        return false;
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
