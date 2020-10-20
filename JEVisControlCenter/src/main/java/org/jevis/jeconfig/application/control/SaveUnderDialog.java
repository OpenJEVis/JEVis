package org.jevis.jeconfig.application.control;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SaveUnderDialog {

    private static final Logger logger = LogManager.getLogger(SaveUnderDialog.class);

    public static JEVisObject saveUnderAnalysis(JEVisDataSource jeVisDataSource, JEVisObject selectedObj, JEVisClass analysisClass, String promptName, Saver saver) {
        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        TextField name = new TextField();
        ObjectRelations objectRelations = new ObjectRelations(jeVisDataSource);

        JEVisClass analysesDirectory = null;
        List<JEVisObject> listAnalysesDirectories = null;
        AtomicBoolean hasMultiDirs = new AtomicBoolean(false);
        try {
            analysesDirectory = jeVisDataSource.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = jeVisDataSource.getObjects(analysesDirectory, false);
            hasMultiDirs.set(listAnalysesDirectories.size() > 1);

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        ObjectProperty<JEVisObject> currentAnalysisDirectory = new SimpleObjectProperty<>(null);
        ComboBox<JEVisObject> parentsDirectories = new ComboBox<>(FXCollections.observableArrayList(listAnalysesDirectories));

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
                currentAnalysisDirectory.setValue(newValue);
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

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

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
        name.setMinWidth(200);

        newAnalysis.getDialogPane().setContent(gridLayout);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
        newAnalysis.getDialogPane().setPrefWidth(450d);
        newAnalysis.initOwner(JEConfig.getStage());
        ObjectProperty<JEVisObject> finalTarget = new SimpleObjectProperty<>();
        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                        List<String> check = new ArrayList<>();
                        JEVisObject toOverwriteObj = null;
                        AtomicBoolean userWantOverwrite = new AtomicBoolean(false);

                        AtomicReference<JEVisObject> currentAnalysis = new AtomicReference<>();
                        try {
                            /**
                             currentAnalysisDirectory.getValue().getChildren().forEach(jeVisObject -> {
                             if (!check.contains(jeVisObject.getName())) {
                             check.add(jeVisObject.getName());
                             }
                             });
                             **/
                            for (JEVisObject jeVisObject : currentAnalysisDirectory.getValue().getChildren()) {
                                if (jeVisObject.getName().equals(name.getText())) toOverwriteObj = jeVisObject;
                            }
                        } catch (JEVisException e) {
                            logger.error("Error in current analysis directory: " + e);
                        }


                        if (toOverwriteObj != null) {
                            Dialog<ButtonType> dialogOverwrite = new Dialog<>();
                            dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                            dialogOverwrite.getDialogPane().setContentText(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                            final ButtonType overwrite_ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                            final ButtonType overwrite_cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                            dialogOverwrite.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

                            Optional<ButtonType> result = dialogOverwrite.showAndWait();
                            if (result.isPresent() && result.get() == overwrite_ok) {
                                userWantOverwrite.set(true);
                            }
                        }


                        try {

                            boolean sameObject = false;
                            if (toOverwriteObj != null) {
                                if (userWantOverwrite.get()) {
                                    finalTarget.set(toOverwriteObj);
                                } else {
                                    logger.error("Cancel save");
                                }
                            } else if (selectedObj != null && selectedObj.getName().equals(name.getText())) {
                                finalTarget.set(selectedObj);
                                sameObject = true;
                            } else if (selectedObj == null || !selectedObj.getName().equals(name.getText())) {
                                JEVisObject newObject = currentAnalysisDirectory.get().buildObject(name.getText(), analysisClass);
                                newObject.commit();
                                finalTarget.set(newObject);
                            }
                            saver.save(finalTarget.get(), sameObject);


                            //dashboardPojo.setTitle(name.getText());
                            //saveDashboard(dashboardPojo, widgetList, name.getText(), currentAnalysisDirectory.getValue(), wallpaper);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                });
        return finalTarget.get();
    }

    @FunctionalInterface
    public interface Saver {

        boolean save(JEVisObject target, boolean sameObject);

    }


}
