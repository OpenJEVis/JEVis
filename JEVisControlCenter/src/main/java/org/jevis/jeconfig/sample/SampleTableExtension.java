/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.dialog.ConfirmDialog;
import org.jevis.commons.dataprocessing.v2.DataProcessing;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.sampletable.TableSample;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTableExtension implements SampleEditorExtension {

    private final static String TITLE = "Editor";
    private final BorderPane _view = new BorderPane();
    Stage owner = JEConfig.getStage();
    private JEVisAttribute _att;
    private List<JEVisSample> _samples;
    private boolean _dataChanged = true;

    public SampleTableExtension(JEVisAttribute att, Stage stage) {
        _att = att;
        owner = stage;
    }

    public SampleTableExtension(JEVisAttribute att) {
        _att = att;
    }

    private void buildGui(final JEVisAttribute att, final List<JEVisSample> samples) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        final SampleTable table = new SampleTable(att,samples);
//        final org.jevis.jeconfig.sampletable.SampleTableView table = new org.jevis.jeconfig.sampletable.SampleTableView(samples);
        table.setPrefSize(1000, 1000);

        Button deleteAll = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.titlelong"));
        deleteAll.setOnAction(event -> {
            try {
                ConfirmDialog dia = new ConfirmDialog();
                if (dia.show(owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.title"),
                        I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.titlelong"),
                        I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.message")) == ConfirmDialog.Response.YES) {

                    att.deleteAllSample();
                    setSamples(att, att.getAllSamples());
                    update();
                    Logger.getLogger(SampleTableExtension.class.getName()).info("Deleted all Samples of Attribute " + att.getName() +
                            " of Object " + att.getObject().getName() + " of ID " + att.getObject().getID());
                }
            } catch (Exception ex) {
                //TODO: do something...
                ex.printStackTrace();
            }
        });

        Button deleteSelected = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.titlelong"));
        deleteSelected.setOnAction(event -> {
                    try {
                        if (!samples.isEmpty()) {
                            DateTime startDate = samples.get(0).getTimestamp();
                            DateTime endDate = samples.get(samples.size() - 1).getTimestamp();
                            ConfirmDialog dia = new ConfirmDialog();

                            if (dia.show(owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.title"),
                                    I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.titlelong"),
                                    I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.message")) == ConfirmDialog.Response.YES) {
                                ObservableList<TableSample> list = table.getSelectionModel().getSelectedItems();
                                for (TableSample tsample : list) {
                                    try {
                                        //TODO: the JEAPI cound use to have an delte funtion for an list of samples
                                        att.deleteSamplesBetween(tsample.getSample().getTimestamp(), tsample.getSample().getTimestamp());
                                    } catch (JEVisException ex) {
                                        Logger.getLogger(SampleTableExtension.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    Logger.getLogger(SampleTableExtension.class.getName()).info("Deleted Samples " + list.size());

                                }

                                //TODO: add workflow selection
                                setSamples(att, DataProcessing.getSamples(att, startDate, endDate, ""));

//                                    setSamples(att, att.getSamples(startDate, endDate));
                                update();
                            }
                        }

                    } catch (Exception ex) {
                        //TODO: do something...
                        ex.printStackTrace();
                    }

                }
        );

        Button deleteInBetween = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.titlelong"));
        deleteInBetween.setOnAction(event -> {
            try {
                if (!samples.isEmpty()) {
                    DateTime startDate = null;
                    DateTime endDate = null;
                    ConfirmDialog dia = new ConfirmDialog();

                    ObservableList<TableSample> list = table.getSelectionModel().getSelectedItems();
                    if (list.size() == 2) {
                        startDate = list.get(0).getSample().getTimestamp();
                        endDate = list.get(list.size() - 1).getSample().getTimestamp();

                        if (startDate != null && endDate != null) {
                            if (dia.show(owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.title"),
                                    I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.titlelong"),
                                    I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.message1") + "\n "
                                            + ISODateTimeFormat.dateTime().print(startDate)
                                            + " " + I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.message2") + " "
                                            + ISODateTimeFormat.dateTime().print(endDate) +
                                            I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.message3")) == ConfirmDialog.Response.YES) {

                                att.deleteSamplesBetween(startDate, endDate);

                                //TODO: add workflow selection
                                setSamples(att, DataProcessing.getSamples(att, startDate, endDate, ""));
                                update();

                                Logger.getLogger(SampleTableExtension.class.getName()).info("Deleted Samples between: " +
                                        startDate.toDateTimeISO() + " and " + endDate.toDateTimeISO());
                            }
                        }

                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.error.title"));
                        alert.setHeaderText(null);
                        alert.setContentText(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.error.message"));

                        alert.showAndWait();
                    }

                }

            } catch (Exception ex) {
                //TODO: do something...
                ex.printStackTrace();
            }

                }
        );

        box.getChildren()
                .setAll(deleteAll, deleteSelected, deleteInBetween);

        GridPane gp = new GridPane();

        gp.setStyle(
                "-fx-background-color: transparent;");
//        gp.setStyle("-fx-background-color: #E2E2E2;");
        gp.setPadding(new Insets(0, 0, 10, 0));
        gp.setHgap(7);
        gp.setVgap(7);

        int y = 0;

        gp.add(table, 0, y);
        gp.add(box, 0, ++y);

//        box.getChildren().setAll(table, deleteAll);
        _view.setCenter(gp);
//        _view.setCenter(box);
//        _view.setCenter(table);
    }

    @Override
    public boolean isForAttribute(JEVisAttribute obj) {
        return true;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public String getTitel() {
        return TITLE;
    }

    @Override
    public void setSamples(final JEVisAttribute att, final List<JEVisSample> samples) {
        _samples = samples;
        _att = att;
        _dataChanged = true;
    }

    @Override
    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (_dataChanged) {
                    buildGui(_att, _samples);
                    _dataChanged = false;
                }
            }
        });
    }

    @Override
    public boolean sendOKAction() {
        return false;
    }

}
