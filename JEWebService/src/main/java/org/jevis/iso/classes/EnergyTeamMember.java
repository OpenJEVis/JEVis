/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergyTeamMember {

    private final String AttAssignmentDocument = "Assignment Document";

    private String name;
    private long ID;
    private String email;
    private String firstname;
    private String lastname;
    private String function;
    private String phone;

    public EnergyTeamMember(SQLDataSource ds, JsonObject input) throws Exception {
        name = "";
        ID = 0;
        email = "";
        firstname = "";
        lastname = "";
        function = "";
        phone = "";
        this.ID = input.getId();
        this.name = input.getName();

        for (JsonAttribute att : ds.getAttributes(input.getId())) {
            String name = att.getType();

            final String attPhone = "Phone";
            final String attLastName = "Name";
            final String attFunction = "Function";
            final String attFirstName = "First Name";
            final String attEMail = "EMail";
            switch (name) {
                case attEMail:
                    this.setEmail(getValueString(att, ""));
                    break;
                case attFirstName:
                    this.setFirstname(getValueString(att, ""));
                    break;
                case attFunction:
                    this.setFunction(getValueString(att, ""));
                    break;
                case attLastName:
                    this.setLastname(getValueString(att, ""));
                    break;
                case attPhone:
                    this.setPhone(getValueString(att, ""));
                    break;
                default:
                    break;
            }
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "EnergyTeamMember{" + "name=" + name + ", ID=" + ID + ", email=" + email + ", firstname=" + firstname + ", lastname=" + lastname + ", function=" + function + ", phone=" + phone + '}';
    }


}
