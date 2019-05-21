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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ConfirmDialog;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.sample.tableview.SampleTable;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTableExtension implements SampleEditorExtension {
    private static final Logger logger = LogManager.getLogger(SampleTableExtension.class);

    private final static String TITLE = "Editor";
    private final BorderPane _view = new BorderPane();
    private final Window owner;
    private JEVisAttribute _att;
    private List<JEVisSample> _samples = new ArrayList<>();
    private boolean _dataChanged = true;
    private BooleanProperty disableEditing = new SimpleBooleanProperty(false);

    public SampleTableExtension(JEVisAttribute att, Window owner) {
        _att = att;
        this.owner = owner;
        buildGui(att, new ArrayList<>());
    }

    private void buildGui(final JEVisAttribute att, final List<JEVisSample> samples) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        final SampleTable table = new SampleTable(att, samples);
        table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        boolean canDelete = false;
        boolean canWrite = false;
        Button deleteAll = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.titlelong"));

        try {
            canDelete = att.getObject().getDataSource().getCurrentUser().canDelete(att.getObject().getID());
            canWrite = att.getObject().getDataSource().getCurrentUser().canWrite(att.getObject().getID());
        } catch (Exception ex) {
            logger.error(ex);
        }
        deleteAll.setDisable(!canDelete);

        deleteAll.setOnAction(event -> {
            try {
                ConfirmDialog dia = new ConfirmDialog();
                if (dia.show(this.owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.title"),
                        I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.titlelong"),
                        I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteall.message")) == ConfirmDialog.Response.YES) {

                    taskWithAnimation(new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            att.deleteAllSample();
                            setSamples(att, att.getAllSamples());
                            update();
                            return null;
                        }
                    });

                    logger.info("Deleted all Samples of Attribute " + att.getName() +
                            " of Object " + att.getObject().getName() + " of ID " + att.getObject().getID());
                }
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        Button deleteSelected = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.titlelong"));
        deleteSelected.setDisable(!table.deleteSelectedProperty().getValue());
        deleteSelected.disableProperty().bind(table.deleteSelectedProperty().not());

        deleteSelected.setOnAction(event -> {
                    try {

                        ConfirmDialog dia = new ConfirmDialog();

                        if (dia.show(this.owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.title"),
                                I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.titlelong"),
                                I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteselected.message")) == ConfirmDialog.Response.YES) {
                            taskWithAnimation(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    table.deleteSelectedData();
                                    update();
                                    return null;
                                }
                            });


                        }

                    } catch (Exception ex) {
                        logger.fatal(ex);
                    }

                }
        );
        Button saveButton = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.save"));
        saveButton.disableProperty().bind(table.needSaveProperty().not());
        saveButton.setOnAction(event -> {
            table.commitChanges();
            update();
        });
        saveButton.setDefaultButton(true);

        Button addNewSample = new Button(null, JEConfig.getImage("list-add.png", 17, 17));
        addNewSample.setDisable(!canWrite);

        addNewSample.setOnAction(event -> {
            /** TODO: implement missing PrimitiveTypes **/
            try {
                Object value;
                switch (att.getPrimitiveType()) {
                    case JEVisConstants.PrimitiveType.DOUBLE:
                        value = 1.0d;
                        break;
                    case JEVisConstants.PrimitiveType.LONG:
                        value = 1l;
                        break;
                    case JEVisConstants.PrimitiveType.BOOLEAN:
                        value = true;
                        break;
                    default:
                        value = "1";
                        break;
                }
                table.addNewSample(new DateTime().withField(DateTimeFieldType.millisOfSecond(), 0), value, "Manual Sample");
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        Button deleteInBetween = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.titlelong"));
        deleteInBetween.setDisable(table.deleteInBetweenProperty().getValue());
        deleteInBetween.disableProperty().bind(table.deleteInBetweenProperty().not());

        deleteInBetween.setOnAction(event -> {
                    try {

                        ConfirmDialog dia = new ConfirmDialog();

                        DateTime[] minMax = table.findSelectedMinMaxDate();
                        DateTime firstDate = minMax[0];
                        DateTime endDate = minMax[1];
                        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                        String message = String.format(I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.message1"), fmt.print(firstDate), fmt.print(endDate));

                        if (dia.show(this.owner, I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.title"),
                                I18n.getInstance().getString("sampleeditor.confirmationdialog.deleteinbetween.titlelong"),
                                message) == ConfirmDialog.Response.YES) {

                            taskWithAnimation(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    table.deleteInBetween();
                                    update();
                                    return null;
                                }
                            });
                        }

                    } catch (Exception ex) {
                        logger.fatal(ex);
                    }

                }
        );


//        boolean disableEdit = true;
        box.getChildren()
                .setAll(addNewSample, deleteAll, deleteSelected, deleteInBetween, saveButton);
//
//        try {
//            if (att.getObject().getDataSource().getCurrentUser().canWrite(att.getObject().getID())) {
//                disableEdit = false;
//
//            }
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
//
//        deleteAll.setDisable(disableEdit);
//        deleteInBetween.setDisable(disableEdit);
//        deleteSelected.setDisable(disableEdit);
//        addNewSample.setDisable(disableEdit);


        _view.setPadding(new Insets(10, 0, 10, 0));
        box.setPadding(new Insets(10, 0, 10, 0));

        _view.setCenter(table);
        _view.setBottom(box);
    }

    public void taskWithAnimation(Task<Void> task) {

        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

        task.setOnSucceeded(event -> pForm.getDialogStage().close());

        task.setOnCancelled(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
            pForm.getDialogStage().hide();
        });

        task.setOnFailed(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
            pForm.getDialogStage().hide();
        });

        pForm.activateProgressBar(task);
        pForm.getDialogStage().show();

        new Thread(task).start();

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
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void setSamples(final JEVisAttribute att, final List<JEVisSample> samples) {
        _samples = samples;
        _att = att;
        _dataChanged = true;
    }

    @Override
    public void disableEditing(boolean disable) {
        disableEditing.setValue(disable);

    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            if (_dataChanged) {
                buildGui(_att, _samples);
                _dataChanged = false;
            }
        });
    }

    @Override
    public boolean sendOKAction() {
        return false;
    }

}
