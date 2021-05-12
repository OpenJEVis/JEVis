package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.dialog.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveUnderDialog extends JFXDialog {

    private static final Logger logger = LogManager.getLogger(SaveUnderDialog.class);
    private JEVisObject target;
    private Response response = Response.CANCEL;

    public SaveUnderDialog(StackPane dialogContainer, JEVisDataSource jeVisDataSource, JEVisObject selectedObj, JEVisClass analysisClass, String promptName, Saver saver) {
        this(dialogContainer, jeVisDataSource, "Analyses Directory", selectedObj, analysisClass, promptName, saver);
    }

    public SaveUnderDialog(StackPane dialogContainer, JEVisDataSource jeVisDataSource, String directoryClass, JEVisObject selectedObj, JEVisClass analysisClass, String promptName, Saver saver) {
        super();
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        JFXTextField name = new JFXTextField();
        name.setMinWidth(350);
        ObjectRelations objectRelations = new ObjectRelations(jeVisDataSource);

        JEVisClass saveDirectory = null;
        List<JEVisObject> listSaveDirectories = null;
        AtomicBoolean hasMultiDirs = new AtomicBoolean(false);
        try {
            saveDirectory = jeVisDataSource.getJEVisClass(directoryClass);
            listSaveDirectories = jeVisDataSource.getObjects(saveDirectory, false);
            hasMultiDirs.set(listSaveDirectories.size() > 1);

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        ObjectProperty<JEVisObject> currentSaveDirectory = new SimpleObjectProperty<>(null);
        JFXComboBox<JEVisObject> parentsDirectories = new JFXComboBox<>(FXCollections.observableArrayList(listSaveDirectories));

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            if (!hasMultiDirs.get())
                                setText(obj.getName());
                            else {
                                String prefix = objectRelations.getObjectPath(obj);
                                setText(prefix + obj.getName());
                            }
                        }

                    }
                };
            }
        };
        parentsDirectories.setCellFactory(cellFactory);
        parentsDirectories.setButtonCell(cellFactory.call(null));

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

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"));
        cancel.setCancelButton(true);

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

        HBox buttonBox = new HBox(6, cancel, ok);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));
        VBox vBox = new VBox(6, gridLayout, separator, buttonBox);
        vBox.setPadding(new Insets(12));

        setContent(vBox);

        ObjectProperty<JEVisObject> finalTarget = new SimpleObjectProperty<>();
        ok.setOnAction(event -> {
            List<String> check = new ArrayList<>();
            JEVisObject toOverwriteObj = null;
            AtomicBoolean userWantOverwrite = new AtomicBoolean(false);

            try {
                for (JEVisObject jeVisObject : currentSaveDirectory.getValue().getChildren()) {
                    if (jeVisObject.getName().equals(name.getText())) toOverwriteObj = jeVisObject;
                }
            } catch (JEVisException e) {
                logger.error("Error in current analysis directory: " + e);
            }


            if (toOverwriteObj != null) {
                JFXAlert dialogOverwrite = new JFXAlert(this.getScene().getWindow());
                dialogOverwrite.setResizable(true);
                dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                Label message = new Label(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                final JFXButton overwrite_ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"));
                overwrite_ok.setDefaultButton(true);
                final JFXButton overwrite_cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"));
                overwrite_cancel.setOnAction(event1 -> dialogOverwrite.close());

                HBox overwriteButtonBox = new HBox(6, overwrite_cancel, overwrite_ok);
                overwriteButtonBox.setAlignment(Pos.CENTER_RIGHT);

                Separator separator2 = new Separator(Orientation.HORIZONTAL);
                separator2.setPadding(new Insets(8, 0, 8, 0));

                VBox vBox1 = new VBox(6, message, separator2, overwriteButtonBox);
                vBox1.setPadding(new Insets(12));

                dialogOverwrite.setContent(vBox1);

                JEVisObject finalToOverwriteObj = toOverwriteObj;
                overwrite_ok.setOnAction(event1 -> {
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
                dialogOverwrite.show();
            } else if (selectedObj != null && selectedObj.getName().equals(name.getText())) {
                target = selectedObj;

                saver.save(target, true);

                response = Response.OK;
                this.close();
            } else if (selectedObj == null || !selectedObj.getName().equals(name.getText())) {
                JEVisObject newObject = null;
                try {
                    newObject = currentSaveDirectory.get().buildObject(name.getText(), analysisClass);

                    newObject.commit();

                    target = newObject;

                    saver.save(target, false);

                    response = Response.OK;
                    this.close();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

            }

        });

        cancel.setOnAction(event -> close());
    }

    public JEVisObject getTarget() {
        return target;
    }

    @FunctionalInterface
    public interface Saver {

        boolean save(JEVisObject target, boolean sameObject);

    }

    public Response getResponse() {
        return response;
    }
}
