/**
 * Copyright (C) 2019 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.plugin.dashboard;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.resource.ResourceLoader;

import java.util.List;

/**
 * @author fs
 */
public class NewAnalysisDialog {
    private static final Logger logger = LogManager.getLogger(NewAnalysisDialog.class);

    public static String ICON = "1403104602_brick_add.png";

    private JEVisObject selectedParent;
    private String createName = "No Name";
    private Response response = Response.CANCEL;
    private final ObjectProperty<Response> responseProperty = new SimpleObjectProperty<>(this.response);

    /**
     * @param owner
     * @return
     */
    public Response show(Stage owner, JEVisDataSource ds) throws JEVisException {
        JEVisClass analysesClass = ds.getJEVisClass("Analyses Directory");
        List<JEVisObject> anaylsisDirs = ds.getObjects(analysesClass, true);
        boolean canWrite = true;

        if (anaylsisDirs.isEmpty()) {
            //Error Missing Analyse Directory
        }


        Dialog<ButtonType> dialog = new Dialog();
        dialog.initOwner(ControlCenter.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(I18n.getInstance().getString("dialog.analyses.title"));
        dialog.setHeaderText(I18n.getInstance().getString("dialog.analyses.header"));
        dialog.getDialogPane().getButtonTypes().setAll();
        dialog.setGraphic(ResourceLoader.getImage(ICON, 50, 50));
        VBox root = new VBox();

        dialog.getDialogPane().setContent(root);


        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int x = 0;

        Label lName = new Label(I18n.getInstance().getString("jevistree.dialog.new.name"));
        final TextField fName = new TextField();
        fName.setPromptText(I18n.getInstance().getString("jevistree.dialog.new.name.prompt"));


        Label lClass = new Label(I18n.getInstance().getString("dialog.analyses.saveplace"));

        ObservableList<JEVisObject> optionsParents = FXCollections.observableArrayList(anaylsisDirs);

        final ComboBox<JEVisObject> comboBox = new ComboBox<>(optionsParents);

        //TODO JFX17
        comboBox.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                String text = "";
                String parentName = "";
                if (object != null) {
                    try {
                        JEVisObject parent = object.getParents().get(0);//not save
                        parentName = parent.getName();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    text = parentName + " / " + object.getName();
                }

                return text;
            }

            @Override
            public JEVisObject fromString(String string) {
                JEVisObject returnObject = null;
                for (JEVisObject jeVisObject : comboBox.getItems()) {
                    String text = "";
                    String parentName = "";
                    try {
                        JEVisObject parent = jeVisObject.getParents().get(0);//not save
                        parentName = parent.getName();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    text = parentName + " / " + jeVisObject.getName();

                    if (text.equals(string)) {
                        returnObject = jeVisObject;
                        break;
                    }
                }
                return returnObject;
            }
        });

        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.selectedParent = newValue;
        });
        comboBox.getSelectionModel().selectFirst();

        comboBox.setMinWidth(250);
        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround


        gp.add(lName, 0, x);
        gp.add(fName, 1, x);


        gp.add(lClass, 0, ++x, 1, 1);
        gp.add(comboBox, 1, x, 1, 1);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(gp);
        VBox.setVgrow(gp, Priority.ALWAYS);

        if (anaylsisDirs.size() == 1) {
            comboBox.setDisable(true);
        }


        fName.onKeyPressedProperty().addListener((observable, oldValue, newValue) -> {
            this.createName = fName.getText();
        });

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        Platform.runLater(() -> fName.requestFocus());
        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.FINISH.getButtonData().getTypeCode())) {

                        this.createName = fName.getText();

                        NewAnalysisDialog.this.response = Response.YES;
                    } else {
                        NewAnalysisDialog.this.response = Response.CANCEL;
                    }
                });


        return this.response;
    }


    public String getCreateName() {
        return this.createName;
    }

    public JEVisObject getParent() {
        return this.selectedParent;
    }

    public enum Type {

        NEW, RENAME
    }

    public enum Response {

        NO, YES, CANCEL
    }


}
