package org.jevis.jeconfig.plugin.dashboard.config2;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SankeyDataRow {

    JEVisObject jeVisObject;
    List<JEVisObject> children = new ArrayList<>();



    public SankeyDataRow(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public JEVisObject getJeVisObject() {
        return jeVisObject;
    }

    public void setJeVisObject(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public List<JEVisObject> getChildren() {
        return children;
    }

    public void setChildren(List<JEVisObject> children) {
        this.children = children;
    }

    public void addChildren(JEVisObject child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }
    public void setAllChildren(List<SankeyDataRow> children) {
        System.out.println("test3");
        System.out.println(children);
        this.children.clear();
        this.children.addAll(children.stream().map(sankeyDataRow -> sankeyDataRow.getJeVisObject()).collect(Collectors.toList()));
        System.out.println(this.children);
    }


    @Override
    public String toString() {
        try {
            if (jeVisObject.getJEVisClassName().equals(JC.Data.CleanData.name)) {
                return new String(jeVisObject.getID()+": "+ jeVisObject.getParent().getName() + " / " + jeVisObject.getName());

            }else {
                return new String(jeVisObject.getID() + ": " + jeVisObject.getName());
            }
        } catch (JEVisException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SankeyDataRow that = (SankeyDataRow) o;
        return Objects.equals(jeVisObject.getID(), that.jeVisObject.getID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jeVisObject);
    }
}
