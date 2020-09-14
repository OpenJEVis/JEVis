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
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class TrainingCourse extends ManagementDocument {

    private final String AttTrainingName = "Training Name";

    private String TrainingCourseName;
    private File PresentationFile;

    public TrainingCourse(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);
        TrainingCourseName = "";

        this.TrainingCourseName = input.getName();

        List<JsonAttribute> listTrainingCourseAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listTrainingCourseAttributes) {
            String name = att.getType();

            final String attPresentationFile = "Presentation File";
            switch (name) {
                case attPresentationFile:
                    //this.setPresentationFile((Organisation.getFile(att, "")));
                    break;
                default:
                    break;
            }
        }

    }

    public String getTrainingCourseName() {
        return TrainingCourseName;
    }

    public void setTrainingCourseName(String TrainingCourseName) {
        this.TrainingCourseName = TrainingCourseName;
    }

    public File getPresentationFile() {
        return PresentationFile;
    }

    public void setPresentationFile(File PresentationFile) {
        this.PresentationFile = PresentationFile;
    }

}
