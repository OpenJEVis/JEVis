/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LegalRegulation {
    private long ID;
    private String name;
    private String ContentSummary;
    private String DateOfReview;
    private String IssueDate;
    private String LastAmended;
    private String RegulationDesignation;
    private Boolean RelevanceForIso50001;
    private String SignificanceForTheCompany;

    public LegalRegulation(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0;
        name = "";
        ContentSummary = "";
        DateOfReview = "";
        IssueDate = "";
        LastAmended = "";
        RegulationDesignation = "";
        RelevanceForIso50001 = false;
        SignificanceForTheCompany = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listLegalRegulationAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listLegalRegulationAttributes) {
            String name = att.getType();

            final String attSignificanceForTheCompany = "Significance to the Company";
            final String attRelevanceForIso50001 = "Relevance to ISO 50001";
            final String attRegulationDesignation = "Regualtion Designation";
            final String attLastAmended = "Last Amended";
            final String attIssueDate = "Issue Date";
            final String attDateOfReview = "Date of Review";
            final String attContentSummary = "Content Summary";
            switch (name) {

                case attContentSummary:
                    this.setContentSummary(getValueString(att, ""));
                    break;
                case attDateOfReview:
                    this.setDateOfReview(getValueString(att, ""));
                    break;
                case attIssueDate:
                    this.setIssueDate(getValueString(att, ""));
                    break;
                case attLastAmended:
                    this.setLastAmended(getValueString(att, ""));
                    break;
                case attRegulationDesignation:
                    this.setRegulationDesignation(getValueString(att, ""));
                    break;
                case attRelevanceForIso50001:
                    if (getValueString(att, "") != "") {
                        this.setRelevanceForIso50001(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setRelevanceForIso50001(false);
                    break;
                case attSignificanceForTheCompany:
                    this.setSignificanceForTheCompany(getValueString(att, ""));
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

    public String getContentSummary() {
        return ContentSummary;
    }

    public void setContentSummary(String ContentSummary) {
        this.ContentSummary = ContentSummary;
    }

    public String getDateOfReview() {
        return DateOfReview;
    }

    public void setDateOfReview(String DateOfReview) {
        this.DateOfReview = DateOfReview;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssueDate() {
        return IssueDate;
    }

    public void setIssueDate(String IssueDate) {
        this.IssueDate = IssueDate;
    }

    public String getLastAmended() {
        return LastAmended;
    }

    public void setLastAmended(String LastAmended) {
        this.LastAmended = LastAmended;
    }

    public String getRegulationDesignation() {
        return RegulationDesignation;
    }

    public void setRegulationDesignation(String RegulationDesignation) {
        this.RegulationDesignation = RegulationDesignation;
    }

    public Boolean getRelevanceForIso50001() {
        return RelevanceForIso50001;
    }

    public void setRelevanceForIso50001(Boolean RelevanceForIso50001) {
        this.RelevanceForIso50001 = RelevanceForIso50001;
    }

    public String getSignificanceForTheCompany() {
        return SignificanceForTheCompany;
    }

    public void setSignificanceForTheCompany(String SignificanceForTheCompany) {
        this.SignificanceForTheCompany = SignificanceForTheCompany;
    }

}
