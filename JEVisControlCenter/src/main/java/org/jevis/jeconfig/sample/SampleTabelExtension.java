/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTabelExtension implements SampleEditorExtension {

    private final static String TITEL = "Editor";
    private final BorderPane _view = new BorderPane();
    private JEVisAttribute _att;
    private List<JEVisSample> _samples;
    private boolean _dataChanged = true;
    Stage owner = JEConfig.getStage();

    public SampleTabelExtension(JEVisAttribute att, Stage stage) {
        _att = att;
        owner = stage;
    }

    public SampleTabelExtension(JEVisAttribute att) {
        _att = att;
    }
    
    private void buildGui(final JEVisAttribute att, final List<JEVisSample> samples) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        final SampleTable table = new SampleTable(samples);
//        final org.jevis.jeconfig.sampletable.SampleTableView table = new org.jevis.jeconfig.sampletable.SampleTableView(samples);
        table.setPrefSize(1000, 1000);

//        if (box.getScene().getWindow() instanceof Stage) {
////            Stage stage = (Stage) window;
//            owner = (Stage) box.getScene().getWindow();
//            System.out.println("fix fix");
//        }
        Button deleteAll = new Button("Delete All");
        deleteAll.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                try {
//                    if (!samples.isEmpty()) {
                    ConfirmDialog dia = new ConfirmDialog();
                    if (dia.show(JEConfig.getStage(), "Delete", "Delete Samples", "Do you really want to delete all existing samples?") == ConfirmDialog.Response.YES) {
                        att.deleteAllSample();
                        setSamples(att, att.getAllSamples());
                        update();
                    }
//                    }
                } catch (Exception ex) {
                    //TODO: do something...
                    ex.printStackTrace();
                }
            }
        }
        );

        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setOnAction(
                new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t
            ) {
                try {
                    if (!samples.isEmpty()) {
                        DateTime startDate = samples.get(0).getTimestamp();
                        DateTime endDate = samples.get(samples.size() - 1).getTimestamp();
                        ConfirmDialog dia = new ConfirmDialog();

                        if (dia.show(owner, "Delete", "Delete Samples", "Do you really want to delete all selected samples?") == ConfirmDialog.Response.YES) {
                            ObservableList<TableSample> list = table.getSelectionModel().getSelectedItems();
                            for (TableSample tsample : list) {
                                try {
                                    //TODO: the JEAPI cound use to have an delte funtion for an list of samples
                                    att.deleteSamplesBetween(tsample.getSample().getTimestamp(), tsample.getSample().getTimestamp());
                                } catch (JEVisException ex) {
                                    Logger.getLogger(SampleTabelExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }

                            //TODO: add workflow selection
                            setSamples(att, DataProcessing.getSamples(att, startDate, endDate, ""));

//                                    setSamples(att, att.getSamples(startDate, endDate));
                            update();
                            System.out.println("-------");
                        }
                    }

                } catch (Exception ex) {
                    //TODO: do something...
                    ex.printStackTrace();
                }

            }
        }
        );

        Button deleteInBetween = new Button("Delete in between");
        deleteInBetween.setOnAction(
                new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t
            ) {
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
                                if (dia.show(owner, "Delete", "Delete Samples", "Do you really want to delete all samples between\n "
                                        + ISODateTimeFormat.dateTime().print(startDate)
                                        + " and " + ISODateTimeFormat.dateTime().print(endDate)) == ConfirmDialog.Response.YES) {

                                    att.deleteSamplesBetween(startDate, endDate);

                                    //TODO: add workflow selection
                                    setSamples(att, DataProcessing.getSamples(att, startDate, endDate, ""));
                                    update();
                                }
                            }

                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Select exactly two samples(from-until)");

                            alert.showAndWait();
                        }

                    }

                } catch (Exception ex) {
                    //TODO: do something...
                    ex.printStackTrace();
                }

            }
        }
        );

//        Button useDataPorcessor = new Button("Clean");
//        useDataPorcessor.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                try {
//                    if (!samples.isEmpty()) {
//                        List<JEVisObject> dataProcessor = _att.getObject().getChildren(_att.getObject().getDataSource().getJEVisClass("Data Processor"), true);
//                        if (!dataProcessor.isEmpty()) {
//                            System.out.println("Class: " + dataProcessor.get(0).getJEVisClass());
//                            Task cleanTask = ProcessorObjectHandler.getTask(dataProcessor.get(0));
//                            setSamples(att, cleanTask.getResult());
//                            update();
//                        } else {
//                            System.out.println("has no Data porcessor");
//                        }
//
//                    }
//                } catch (Exception ex) {
//                    //TODO: do something...
//                    ex.printStackTrace();
//                }
//            }
//        }
//        );
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
        return TITEL;
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
