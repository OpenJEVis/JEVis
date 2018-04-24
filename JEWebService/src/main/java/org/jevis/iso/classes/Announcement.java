/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.io.File;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Announcement extends ManagementDocument {

    private File AnnouncementFile;

    public Announcement(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);

        List<JsonAttribute> listAnnouncementAttributes = getDs().getAttributes(input.getId());

        for (JsonAttribute att : listAnnouncementAttributes) {
            String name = att.getType();

            final String attAnnouncementFile = "Announcement File";
            switch (name) {
                //this.setAnnouncementFile(att.getLatestSample().getValueAsFile());
                case attAnnouncementFile:
                    break;
                default:
                    break;
            }
        }
    }

    public File getAnnouncementFile() {
        return AnnouncementFile;
    }

    public void setAnnouncementFile(File AnnouncementFile) {
        this.AnnouncementFile = AnnouncementFile;
    }

}
