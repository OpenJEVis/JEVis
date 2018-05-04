/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import org.jevis.commons.ws.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Form {

    List<FormAttribute> attributes = new ArrayList<>();
    List<JsonRelationshipHelper> permissions = new ArrayList<>();
    List<JsonObject> userGroups = new ArrayList<>();
    private long ID = 0;
    private Boolean translated = false;
    private String name = new String();
    private String objectname = "Object Name";
    private String deleteobject = "Delete Object";
    private String uploaded = "Uploaded";
    private String bauth = new String();
    private String tabAttributes = "Attributes";
    private String tabPermissions = "Permissions";
    private String buttonUpload = "Upload";
    private String buttonDownload = "Download";
    private String buttonDelete = "Delete Object";

    public List<JsonObject> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<JsonObject> userGroups) {
        this.userGroups = userGroups;
    }

    public List<JsonRelationshipHelper> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<JsonRelationshipHelper> permissions) {
        this.permissions = permissions;
    }

    public String getButtonUpload() {
        return buttonUpload;
    }

    public void setButtonUpload(String buttonUpload) {
        this.buttonUpload = buttonUpload;
    }

    public String getButtonDownload() {
        return buttonDownload;
    }

    public void setButtonDownload(String buttonDownload) {
        this.buttonDownload = buttonDownload;
    }

    public String getButtonDelete() {
        return buttonDelete;
    }

    public void setButtonDelete(String buttonDelete) {
        this.buttonDelete = buttonDelete;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Boolean getTranslated() {
        return translated;
    }

    public void setTranslated(Boolean translated) {
        this.translated = translated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    public String getTabAttributes() {
        return tabAttributes;
    }

    public void setTabAttributes(String tabAttributes) {
        this.tabAttributes = tabAttributes;
    }

    public String getTabPermissions() {
        return tabPermissions;
    }

    public void setTabPermissions(String tabPermissions) {
        this.tabPermissions = tabPermissions;
    }

    public String getObjectname() {
        return objectname;
    }

    public void setObjectname(String objectname) {
        this.objectname = objectname;
    }

    public String getDeleteobject() {
        return deleteobject;
    }

    public void setDeleteobject(String deleteobject) {
        this.deleteobject = deleteobject;
    }

    public String getBauth() {
        return bauth;
    }

    public void setBauth(String bauth) {
        this.bauth = bauth;
    }

    public String getOutput() {
        String output = "";
        if (ID != 0 && attributes != null) {
            HashMap<String, Object> map = new HashMap<>();

            map.put("bauth", getBauth());

            map.put("FormID", getID());
            map.put("name", getName());
            map.put("translated", getTranslated());
            map.put("objectname", getObjectname());
            map.put("deleteobject", getDeleteobject());
            map.put("uploaded", getUploaded());
            map.put("tabAttributes", getTabAttributes());
            map.put("tabPermissions", getTabPermissions());
            map.put("buttonUpload", getButtonUpload());
            map.put("buttonDownload", getButtonDownload());
            map.put("buttonDelete", getButtonDelete());

            map.put("attributes", getAttributes());
            map.put("permissions", getPermissions());
            map.put("userGroups", getUserGroups());
            TemplateChooser tc = new TemplateChooser(map, "form");

            output = tc.getOutput();
        } else {
            output = "not enough data!";
        }
        return output;
    }

    public List<FormAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<FormAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Form{" + "ID=" + ID + ", attributes=" + attributes + '}';
    }

}
