package org.jevis.commons.dimpex;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DimpexObject {

    private String uid = UUID.randomUUID().toString();
    private String name = "NO NAME";
    private String jclass = "";
    private String action = Action.ADD.name();
    private boolean isPublic = false;
    private List<DimpexObject> children = new ArrayList<>();
    private List<DimpexAttribute> attributes = new ArrayList<>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJclass() {
        return jclass;
    }

    public void setJclass(String jclass) {
        this.jclass = jclass;
    }

    public List<DimpexObject> getChildren() {
        return children;
    }

    public void setChildren(List<DimpexObject> children) {
        this.children = children;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<DimpexAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DimpexAttribute> attributes) {
        this.attributes = attributes;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public enum Action {
        DELETE, ADD, UPDATE
    }
}
