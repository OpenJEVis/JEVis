/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.iso.add.Snippets;
import org.jevis.ws.sql.SQLDataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Organisation {

    private final String DirectoryName;
    List<Meeting> SuperiorLevelMeetings;
    InitialContact FirstContact;
    List<Site> Sites;
    private long ID;
    private String name;
    private SQLDataSource ds;
    private String Address;
    private String Branche;
    private File CompanyLogo;
    private String CompanyName;
    private String Location;
    private String Mail;
    private String Members;
    private String Phone;
    private String WorkingOrOperatingTime;
    private Long SuperiorMeetingsDirID;
    private String SuperiorMeetingsDirName;

    public Organisation(SQLDataSource input) throws Exception {

        this.ds = input;
        DirectoryName = "Organization";
        SuperiorLevelMeetings = new ArrayList<>();
        FirstContact = new InitialContact();
        Sites = new ArrayList<>();
        ID = 0;
        name = "";
        Address = "";
        Branche = "";
        CompanyName = "";
        Location = "";
        Mail = "";
        Members = "";
        Phone = "";
        WorkingOrOperatingTime = "";
        SuperiorMeetingsDirID = 0L;
        SuperiorMeetingsDirName = "";

        for (JsonAttribute att : getDs().getAttributes(getID())) {

            final String attWorkingOrOpeningTime = "Working or Opening Time";
            final String attPhone = "Phone";
            final String attMembers = "Members";
            final String attMail = "Mail";
            final String attLocation = "Location";
            final String attCompanyName = "Company Name";
            final String attCompanyLogo = "Company Logo";
            final String attBranche = "Branche";
            final String attAddress = "Address";
            switch (att.getType()) {
                case attAddress:
                    this.setAddress(Snippets.getValueString(att, ""));
                    break;
                case attBranche:
                    this.setBranche(Snippets.getValueString(att, ""));
                    break;
                case attCompanyLogo:

                    break;
                case attCompanyName:
                    this.setCompanyName(Snippets.getValueString(att, ""));
                    break;
                case attLocation:
                    this.setLocation(Snippets.getValueString(att, ""));
                    break;
                case attMail:
                    this.setMail(Snippets.getValueString(att, ""));
                    break;
                case attMembers:
                    this.setMembers(Snippets.getValueString(att, ""));
                    break;
                case attPhone:
                    this.setPhone(Snippets.getValueString(att, ""));
                    break;
                case attWorkingOrOpeningTime:
                    this.setWorkingOrOperatingTime(Snippets.getValueString(att, ""));
                    break;
                default:
                    break;
            }
        }
    }

    public List<Long> getListYearsSupMeetings() {
        List<Long> output = new ArrayList<>();
        for (Meeting m : SuperiorLevelMeetings) {
            if (!output.contains(m.getYear())) {
                output.add(m.getYear());
            }
        }
        return output;
    }

    public Long getYearInitialContacat() {
        return FirstContact.getYear();

    }

    public long getID() throws Exception {
        this.ID = Snippets.getUniqueObjectId(getDs(), ISO50001.getJc().getOrganizationDir().getName());
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getBranche() {
        return Branche;
    }

    public void setBranche(String Branche) {
        this.Branche = Branche;
    }

    public File getCompanyLogo() {
        return CompanyLogo;
    }

    public void setCompanyLogo(File CompanyLogo) {
        this.CompanyLogo = CompanyLogo;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getMail() {
        return Mail;
    }

    public void setMail(String Mail) {
        this.Mail = Mail;
    }

    public String getMembers() {
        return Members;
    }

    public void setMembers(String Members) {
        this.Members = Members;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String Phone) {
        this.Phone = Phone;
    }

    public String getWorkingOrOperatingTime() {
        return WorkingOrOperatingTime;
    }

    public void setWorkingOrOperatingTime(String WorkingOrOperatingTime) {
        this.WorkingOrOperatingTime = WorkingOrOperatingTime;
    }

    public List<Meeting> getSuperiorLevelMeetings() throws Exception {
        SuperiorLevelMeetings.clear();

        SuperiorMeetingsDirID = getDs().getObjects(ISO50001.getJc().getSuperiorLevelMeetingsDir().getName(), false).get(0).getId();

        List<JsonObject> SuperiorMeetingsDirObjectList = getDs().getObjects(ISO50001.getJc().getSuperiorLevelMeetingsDir().getName(), true);

        for (JsonObject obj : SuperiorMeetingsDirObjectList) {

            if (obj.getJevisClass().equals(ISO50001.getJc().getMeeting().getName())) {

                Meeting m = new Meeting(getDs(), obj);

                SuperiorLevelMeetings.add(m);
            }
        }
        return SuperiorLevelMeetings;
    }

    public void setSuperiorLevelMeetings(List<Meeting> SuperiorLevelMeetings) {
        this.SuperiorLevelMeetings = SuperiorLevelMeetings;
    }

    public InitialContact getFirstContact() throws Exception {
        //get Initial Contact
        FirstContact = new InitialContact(getDs(), ISO50001.getJc());

        return FirstContact;
    }

    public void setFirstContact(InitialContact FirstContact) {
        this.FirstContact = FirstContact;
    }

    public List<Site> getSites() throws JEVisException {
        Sites.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getSite().getName(), false)) {
            Site s = new Site(obj, getDs());
            Sites.add(s);
        }
        return Sites;
    }

    public void setSites(List<Site> Sites) {
        this.Sites = Sites;
    }

    public List<String> getSiteNames() throws JEVisException {
        List<String> s = new LinkedList<>();
        List<JsonObject> sites = Snippets.getChildren(getDs(), ISO50001.getJc().getSite().getName());
        for (JsonObject obj : sites) {
            s.add(obj.getName());
        }
        return s;
    }

    public Site getSite(int index) throws JEVisException {
        return getSites().get(index);
    }

    public Site getSite(String siteName) throws JEVisException {
        if (Sites.isEmpty()) {
            getSites();
        }
        for (Site s : Sites) {
            if (siteName.equals(s.getName())) {
                return s;
            }
        }
        return null;
    }

    public String getName() throws JEVisException {
        this.name = Snippets.getChildName(getDs(), ISO50001.getJc().getOrganizationDir().getName());
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSuperiorMeetingsDirID() throws JEVisException {
        this.SuperiorMeetingsDirID = Snippets.getUniqueObjectId(getDs(), ISO50001.getJc().getSuperiorLevelMeetingsDir().getName());
        return SuperiorMeetingsDirID;
    }

    public void setSuperiorMeetingsDirID(Long SuperiorMeetingsDirID) {
        this.SuperiorMeetingsDirID = SuperiorMeetingsDirID;
    }

    public String getSuperiorMeetingsDirName() throws JEVisException {
        this.SuperiorMeetingsDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getSuperiorLevelMeetingsDir().getName());
        return SuperiorMeetingsDirName;
    }

    public void setSuperiorMeetingsDirName(String SuperiorMeetingsDirName) {
        this.SuperiorMeetingsDirName = SuperiorMeetingsDirName;
    }
}
