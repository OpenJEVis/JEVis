package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.Map;

public interface Chart {

    String getChartName();

    void setTitle(String s);

    Integer getChartId();

    void updateTable(MouseEvent mouseEvent, Number valueForDisplay);

    void showNote(MouseEvent mouseEvent);

    void applyColors();

    default String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

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
                        List<JEVisSample> listSamples = userNoteAttribute.getSamples(timeStamp.minusMillis(1), timeStamp.plusMillis(1));
                        if (listSamples.size() == 1) {
                            for (JEVisSample smp : listSamples) {
                                return smp.getValueAsString();
                            }
                        }
                    } catch (JEVisException e) {

                    }
                }
            }
        } catch (JEVisException e) {

        }
        return userNote;
    }

    default void saveUserNotes(Map<String, RowNote> noteMap) {
        for (Map.Entry<String, RowNote> entry : noteMap.entrySet()) {
            if (entry.getValue().getChanged()) {
                try {
                    JEVisObject obj = entry.getValue().getDataObject();

                    JEVisSample sample = entry.getValue().getSample();
                    DateTime timeStamp = sample.getTimestamp();
                    String newUserNote = entry.getValue().getUserNote();
                    JEVisObject correspondingNoteObject = null;

                    final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
                    boolean foundNoteObject = false;
                    List<JEVisObject> listParents = obj.getParents();
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

                    try {
                        JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");

                        List<JEVisSample> listSamples;
                        if (userNoteAttribute.hasSample()) {
                            listSamples = userNoteAttribute.getSamples(timeStamp.minusMillis(1), timeStamp.plusMillis(1));

                            if (listSamples.size() == 1) {
                                listSamples.get(0).setValue(newUserNote);
                                listSamples.get(0).commit();
                            } else {
                                JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                                newSample.commit();
                            }
                        } else {
                            JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                            newSample.commit();
                        }

                        if (!sample.getNote().contains("userNotes")) {
                            List<JEVisSample> unmodifiedSamples = obj
                                    .getDataSource()
                                    .getObject(obj.getID())
                                    .getAttribute("Value")
                                    .getSamples(timeStamp.minusMillis(1), timeStamp.plusMillis(1));

                            if (unmodifiedSamples.size() == 1) {
                                JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                String note = unmodifiedSample.getNote();
                                note += ",userNotes";
                                unmodifiedSample.setNote(note);
                                unmodifiedSample.commit();
                            }
                        }

                    } catch (JEVisException e) {

                    }

                } catch (JEVisException e) {

                }
            }
        }
    }

    Number getValueForDisplay();

    void setValueForDisplay(Number valueForDisplay);

    javafx.scene.chart.Chart getChart();

    Region getRegion();

    void initializeZoom();

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    DateTime getStartDateTime();

    DateTime getEndDateTime();

    void updateChart();

    void setDataModels(List<ChartDataModel> chartDataModels);

    void setHideShowIcons(Boolean hideShowIcons);

    ChartPanManager getPanner();

    JFXChartUtil getJfxChartUtil();
}
