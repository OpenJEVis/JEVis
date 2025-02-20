package org.jevis.jecc.application.control;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.dialog.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveUnderDialog extends Dialog {

    private static final Logger logger = LogManager.getLogger(SaveUnderDialog.class);
    private JEVisObject target;
    private Response response = Response.CANCEL;

    public SaveUnderDialog(JEVisDataSource jeVisDataSource, JEVisObject selectedObj, JEVisClass analysisClass, String promptName, Saver saver) {
        this(jeVisDataSource, "Dashboard Directory", selectedObj, analysisClass, promptName, saver);
    }

    public SaveUnderDialog(JEVisDataSource jeVisDataSource, String directoryClass, JEVisObject selectedObj, JEVisClass analysisClass, String promptName, Saver saver) {
        super();
        setTitle(I18n.getInstance().getString("plugin.trc.saveunderdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.dashboard.saveunderdialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        TextField name = new TextField();
        name.setMinWidth(350);
        ObjectRelations objectRelations = new ObjectRelations(jeVisDataSource);

        JEVisClass saveDirectory = null;
        List<JEVisObject> listSaveDirectories = null;
        AtomicBoolean hasMultiDirs = new AtomicBoolean(false);
        try {
            saveDirectory = jeVisDataSource.getJEVisClass(directoryClass);
            listSaveDirectories = jeVisDataSource.getObjects(saveDirectory, false);
            hasMultiDirs.set(listSaveDirectories.size() > 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        ObjectProperty<JEVisObject> currentSaveDirectory = new SimpleObjectProperty<>(null);
        ComboBox<JEVisObject> parentsDirectories = new ComboBox<>(FXCollections.observableArrayList(listSaveDirectories));

        //TODO JFX17

        parentsDirectories.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                String text = "";
                if (object != null) {
                    if (!hasMultiDirs.get())
                        text = object.getName();
                    else {
                        String prefix = objectRelations.getObjectPath(object);
                        text = prefix + object.getName();
                    }
                }

                return text;
            }

            @Override
            public JEVisObject fromString(String string) {
                JEVisObject returnObject = null;

                for (JEVisObject jeVisObject : parentsDirectories.getItems()) {
                    String text = "";
                    if (!hasMultiDirs.get())
                        text = jeVisObject.getName();
                    else {
                        String prefix = objectRelations.getObjectPath(jeVisObject);
                        text = prefix + jeVisObject.getName();
                    }

                    if (text.equals(string)) {
                        returnObject = jeVisObject;
                        break;
                    }
                }

                return returnObject;
            }
        });

        parentsDirectories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                currentSaveDirectory.setValue(newValue);
            }
        });
        parentsDirectories.getSelectionModel().selectFirst();


        if (selectedObj != null) {
            try {
                if (selectedObj.getParents() != null) {
                    JEVisObject parenObj = selectedObj.getParents().get(0);
                    parentsDirectories.getSelectionModel().select(parenObj);
                }
            } catch (Exception e) {
                logger.error("Couldn't select current Analysis Directory: " + e);
            }

            name.setText(selectedObj.getName());
        }

        if (promptName != null && !promptName.isEmpty()) {
            name.setText(promptName);
        }

        name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
            if (name.isFocused() && !name.getText().isEmpty()) {
                name.selectAll();
            }
        }));

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);

        gridLayout.add(directoryText, 0, 0);
        gridLayout.add(parentsDirectories, 0, 1, 2, 1);
        GridPane.setFillWidth(parentsDirectories, true);
        parentsDirectories.setMinWidth(200);
        gridLayout.add(newText, 0, 2);
        gridLayout.add(name, 0, 3, 2, 1);
        GridPane.setFillWidth(name, true);

        getDialogPane().setContent(gridLayout);

        ObjectProperty<JEVisObject> finalTarget = new SimpleObjectProperty<>();
        okButton.setOnAction(event -> {
            List<String> check = new ArrayList<>();
            JEVisObject toOverwriteObj = null;
            AtomicBoolean userWantOverwrite = new AtomicBoolean(false);

            try {
                for (JEVisObject jeVisObject : currentSaveDirectory.getValue().getChildren()) {
                    if (jeVisObject.getName().equals(name.getText())) toOverwriteObj = jeVisObject;
                }
            } catch (Exception e) {
                logger.error("Error in current analysis directory: " + e);
            }


            if (toOverwriteObj != null) {
                Dialog dialogOverwrite = new Dialog();
                dialogOverwrite.initOwner(ControlCenter.getStage());
                dialogOverwrite.initModality(Modality.APPLICATION_MODAL);
                Stage overwriteStage = (Stage) dialogOverwrite.getDialogPane().getScene().getWindow();
                TopMenu.applyActiveTheme(overwriteStage.getScene());
                overwriteStage.setAlwaysOnTop(true);
                dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.dashboard.dialog.overwrite.title"));
                dialogOverwrite.setResizable(true);
                Label message = new Label(I18n.getInstance().getString("plugin.dashboard.dialog.overwrite.message"));

                ButtonType overwriteOkType = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                ButtonType overwriteCancelType = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                dialogOverwrite.getDialogPane().getButtonTypes().setAll(overwriteOkType, overwriteCancelType);

                Button overwriteOkButton = (Button) dialogOverwrite.getDialogPane().lookupButton(overwriteOkType);
                overwriteOkButton.setDefaultButton(true);

                Button overwriteCancelButton = (Button) dialogOverwrite.getDialogPane().lookupButton(overwriteCancelType);
                overwriteCancelButton.setCancelButton(true);

                overwriteCancelButton.setOnAction(event1 -> dialogOverwrite.close());

                dialogOverwrite.getDialogPane().setContent(message);

                JEVisObject finalToOverwriteObj = toOverwriteObj;
                overwriteOkButton.setOnAction(event1 -> {
                    userWantOverwrite.set(true);

                    try {
                        if (userWantOverwrite.get()) {
                            target = finalToOverwriteObj;
                        } else {
                            logger.error("Cancel save");
                        }

                        saver.save(target, false);

                        response = Response.OK;
                        dialogOverwrite.close();
                        this.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                boolean writeOk = true;
                try {
                    writeOk = jeVisDataSource.getCurrentUser().canWrite(toOverwriteObj.getID());
                    if (!writeOk) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.write.denied"));
                        alert.initOwner(ControlCenter.getStage());
                        alert.initModality(Modality.APPLICATION_MODAL);
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        TopMenu.applyActiveTheme(stageAlert.getScene());
                        stageAlert.setAlwaysOnTop(true);
                        alert.showAndWait();
                        this.close();
                    }
                } catch (Exception e) {
                    logger.error("Could not do write check", e);
                }

                if (writeOk) {
                    dialogOverwrite.show();
                }
            } else if (selectedObj != null && selectedObj.getName().equals(name.getText())) {
                target = selectedObj;

                boolean writeOk = true;
                try {
                    writeOk = jeVisDataSource.getCurrentUser().canWrite(target.getID());
                    if (!writeOk) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.write.denied"));
                        alert.initOwner(ControlCenter.getStage());
                        alert.initModality(Modality.APPLICATION_MODAL);
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        TopMenu.applyActiveTheme(stageAlert.getScene());
                        stageAlert.setAlwaysOnTop(true);
                        alert.showAndWait();
                        this.close();
                    }
                } catch (Exception e) {
                    logger.error("Could not do write check", e);
                }

                if (writeOk) {
                    saver.save(target, true);

                    response = Response.OK;
                    this.close();
                }
            } else if (selectedObj == null || !selectedObj.getName().equals(name.getText())) {
                JEVisObject newObject = null;
                try {
                    boolean createOk = true;
                    try {
                        createOk = jeVisDataSource.getCurrentUser().canCreate(currentSaveDirectory.get().getID());
                        if (!createOk) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.write.denied"));
                            alert.initOwner(ControlCenter.getStage());
                            alert.initModality(Modality.APPLICATION_MODAL);
                            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                            TopMenu.applyActiveTheme(stageAlert.getScene());
                            stageAlert.setAlwaysOnTop(true);
                            alert.showAndWait();
                            this.close();
                        }
                    } catch (Exception e) {
                        logger.error("Could not do write check", e);
                    }

                    if (createOk) {
                        newObject = currentSaveDirectory.get().buildObject(name.getText(), analysisClass);

                        newObject.commit();

                        target = newObject;

                        saver.save(target, false);

                        response = Response.OK;
                        this.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        cancelButton.setOnAction(event -> close());
    }

    public JEVisObject getTarget() {
        return target;
    }

    public Response getResponse() {
        return response;
    }

    @FunctionalInterface
    public interface Saver {

        boolean save(JEVisObject target, boolean sameObject);

    }
}
