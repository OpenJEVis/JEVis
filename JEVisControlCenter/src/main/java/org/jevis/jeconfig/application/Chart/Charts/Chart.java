package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

public interface Chart {

    String getChartName();

    void setTitle(String s);

    Integer getChartId();

    void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay);

    void updateTableZoom(Long lowerBound, Long upperBound);

    void showNote(MouseEvent mouseEvent);

    void applyColors();

    default String getUserNoteForTimeStamp(JEVisSample nearestSample, DateTime timeStamp) {
        String userNote = "";

        try {
            if (nearestSample.getAttribute() != null) {
                JEVisObject obj = nearestSample.getAttribute().getObject();
                JEVisObject correspondingNoteObject = null;

                final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
                List<JEVisObject> listParents = obj.getParents();
                for (JEVisObject parent : listParents) {
                    for (JEVisObject child : parent.getChildren()) {
                        if (child.getJEVisClass().equals(dataNoteClass) && child.getName().contains(obj.getName())) {
                            correspondingNoteObject = child;
                            break;
                        }
                    }
                }
                if (correspondingNoteObject != null) {
                    try {
                        JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");
                        List<JEVisSample> listSamples = userNoteAttribute.getSamples(timeStamp, timeStamp);
                        if (listSamples.size() == 1) {
                            for (JEVisSample smp : listSamples) {
                                return smp.getValueAsString();
                            }
                        }
                    } catch (JEVisException ignored) {

                    }
                }
            }
        } catch (JEVisException ignored) {

        }
        return userNote;
    }

    default String getUserValueForTimeStamp(JEVisSample nearestSample, DateTime timeStamp) {
        String userValue = "";

        try {
            if (nearestSample.getAttribute() != null) {
                JEVisObject obj = nearestSample.getAttribute().getObject();
                JEVisObject correspondingUserDataObject = null;

                final JEVisClass userDataClass = obj.getDataSource().getJEVisClass("User Data");
                List<JEVisObject> listParents = obj.getParents();
                for (JEVisObject parent : listParents) {
                    for (JEVisObject child : parent.getChildren()) {
                        if (child.getJEVisClass().equals(userDataClass) && child.getName().contains(obj.getName())) {
                            correspondingUserDataObject = child;
                            break;
                        }
                    }
                }
                if (correspondingUserDataObject != null) {
                    try {
                        JEVisAttribute userDataAttribute = correspondingUserDataObject.getAttribute("Value");
                        List<JEVisSample> listSamples = userDataAttribute.getSamples(timeStamp, timeStamp);
                        if (listSamples.size() == 1) {
                            for (JEVisSample smp : listSamples) {
                                return smp.getValueAsString();
                            }
                        }
                    } catch (JEVisException ignored) {

                    }
                }
            }
        } catch (JEVisException ignored) {

        }
        return userValue;
    }

    default void saveUserEntries(Map<String, RowNote> noteMap) {
        for (Map.Entry<String, RowNote> entry : noteMap.entrySet()) {
            if (entry.getValue().getChanged()) {
                try {
                    JEVisObject obj = entry.getValue().getDataObject();
                    JEVisUser currentUser = obj.getDataSource().getCurrentUser();
                    //if (obj.getDataSource().getCurrentUser().canWrite(obj.getID())) {

                    JEVisSample sample = entry.getValue().getSample();
                    DateTime timeStamp = sample.getTimestamp();
                    String newUserNote = entry.getValue().getUserNote();
                    String newUserValue = entry.getValue().getUserValue();
                    List<JEVisObject> listParents = obj.getParents();

                    JEVisObject correspondingNoteObject = null;
                    final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
                    boolean foundNoteObject = false;
                    for (JEVisObject parent : listParents) {
                        for (JEVisObject child : parent.getChildren()) {
                            if (child.getJEVisClass().equals(dataNoteClass)) {
                                correspondingNoteObject = child;
                                foundNoteObject = true;
                                break;
                            }
                        }
                    }

                    if (!foundNoteObject) {
                        for (JEVisObject parent : listParents) {
                            correspondingNoteObject = parent.buildObject(obj.getName() + " Notes", dataNoteClass);
                            correspondingNoteObject.commit();
                        }
                    }

                    if (correspondingNoteObject != null) {
                        try {
                            JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");

                            List<JEVisSample> listSamples;
                            if (userNoteAttribute.hasSample()) {
                                listSamples = userNoteAttribute.getSamples(timeStamp, timeStamp);

                                if (!newUserNote.equals("") && listSamples.size() == 1) {
                                    listSamples.get(0).setValue(newUserNote);
                                    listSamples.get(0).commit();
                                } else if (!newUserNote.equals("")) {
                                    JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                                    newSample.commit();
                                } else {
                                    userNoteAttribute.deleteSamplesBetween(timeStamp.toDateTimeISO(), timeStamp.toDateTimeISO());
                                }
                            } else {
                                if (!newUserNote.equals("")) {
                                    JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                                    newSample.commit();
                                }

                            }

                            if (!newUserNote.equals("") && !sample.getNote().contains("userNotes")) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1 && currentUser.canWrite(obj.getID())) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note += ",userNotes";
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            } else if (newUserNote.equals("") && sample.getNote().contains("userNotes") && currentUser.canWrite(obj.getID())) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note = note.replace(",userNotes", "");
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            }

                        } catch (JEVisException ignored) {

                        }
                    }

                    JEVisObject correspondingUserDataObject = null;
                    final JEVisClass userDataClass = obj.getDataSource().getJEVisClass("User Data");
                    boolean foundUserDataObject = false;
                    for (JEVisObject parent : listParents) {
                        for (JEVisObject child : parent.getChildren()) {
                            if (child.getJEVisClass().equals(userDataClass)) {
                                correspondingUserDataObject = child;
                                foundUserDataObject = true;
                                break;
                            }
                        }
                    }

                    if (!foundUserDataObject) {
                        for (JEVisObject parent : listParents) {
                            if(currentUser.canCreate(parent.getID())){
                                correspondingUserDataObject = parent.buildObject(obj.getName() + " User Data", userDataClass);
                                correspondingUserDataObject.commit();
                            }

                        }
                    }

                    if (correspondingUserDataObject != null && currentUser.canWrite(correspondingUserDataObject.getID())) {
                        try {
                            JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");

                            List<JEVisSample> listSamples;
                            if (userDataValueAttribute.hasSample()) {
                                listSamples = userDataValueAttribute.getSamples(timeStamp, timeStamp);

                                if (!newUserValue.equals("") && listSamples.size() == 1) {
                                    listSamples.get(0).setValue(Double.parseDouble(newUserValue));
                                    listSamples.get(0).commit();
                                } else if (!newUserValue.equals("")) {
                                    JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), Double.parseDouble(newUserValue));
                                    newSample.commit();
                                } else {
                                    userDataValueAttribute.deleteSamplesBetween(timeStamp.toDateTimeISO(), timeStamp.toDateTimeISO());
                                }
                            } else {
                                if (!newUserValue.equals("")) {
                                    JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), Double.parseDouble(newUserValue));
                                    newSample.commit();
                                }
                            }

                            if (!newUserValue.equals("") && !sample.getNote().contains(USER_VALUE)) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note += "," + USER_VALUE;
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            } else if (newUserValue.equals("") && sample.getNote().contains(USER_VALUE)) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note = note.replace("," + USER_VALUE, "");
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            }

                        } catch (JEVisException ignored) {

                        }
                    }
                    //} else {

                    //}

                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert1.showAndWait();
                    });
                }
            }
        }
    }

    DateTime getValueForDisplay();

    void setValueForDisplay(DateTime valueForDisplay);

    org.jevis.jeconfig.application.Chart.Charts.jfx.Chart getChart();

    Region getRegion();

    void initializeZoom();

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    DateTime getStartDateTime();

    DateTime getEndDateTime();

    void updateChart();

    void setDataModels(List<ChartDataModel> chartDataModels);

    void setHideShowIcons(Boolean hideShowIcons);

    void setChartSettings(ChartSettingsFunction function);

    ChartPanManager getPanner();

    JFXChartUtil getJfxChartUtil();

    void setRegion(Region region);

    void checkForY2Axis();

    void applyBounds();

    List<ChartDataModel> getChartDataModels();
}
