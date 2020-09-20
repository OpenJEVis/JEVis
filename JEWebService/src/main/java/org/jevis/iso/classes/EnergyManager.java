/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;

import java.io.File;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergyManager {
    private String name;
    private long ID;
    private String email;
    private String firstname;
    private String lastname;
    private String function;
    private String phone;
    private File assignmentdocument;

    public EnergyManager() {

        name = "";
        ID = 0L;
        email = "";
        firstname = "";
        lastname = "";
        function = "";
        phone = "";
    }

    public EnergyManager(SQLDataSource ds, JsonObject input) throws Exception {
        name = "";
        ID = 0L;
        this.ID = input.getId();
        this.name = input.getName();
        email = "";
        firstname = "";
        lastname = "";
        function = "";
        phone = "";

        for (JsonAttribute att : ds.getAttributes(input.getId())) {
            String attname = att.getType();

            final String attPhone = "Phone";
            final String attLastName = "Surname";
            final String attFunction = "Function";
            final String attFirstName = "Name";
            final String attAssignmentDocument = "Appointment Letter";
            final String attEMail = "EMail";
            switch (attname) {
                case attAssignmentDocument:

                    break;
                case attEMail:
                    this.setemail(getValueString(att, ""));
                    break;
                case attFirstName:
                    this.setfirstname(getValueString(att, ""));
                    break;
                case attFunction:
                    this.setfunction(getValueString(att, ""));
                    break;
                case attLastName:
                    this.setlastname(getValueString(att, ""));
                    break;
                case attPhone:
                    this.setphone(getValueString(att, ""));
                    break;
                default:
                    break;
            }
        }
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    public String getfirstname() {
        return firstname;
    }

    public void setfirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getlastname() {
        return lastname;
    }

    public void setlastname(String lastname) {
        this.lastname = lastname;
    }

    public String getfunction() {
        return function;
    }

    public void setfunction(String function) {
        this.function = function;
    }

    public String getphone() {
        return phone;
    }

    public void setphone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "EnergyManager{" + "name=" + name + ", ID=" + ID + ", email=" + email + ", firstname=" + firstname + ", lastname=" + lastname + ", function=" + function + ", phone=" + phone + ", assignmentdocument=" + assignmentdocument + '}';
    }

}
