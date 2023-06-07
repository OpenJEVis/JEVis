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
package org.jevis.jecc.plugin.legal.ui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.resource.ImageConverter;
import org.jevis.jecc.application.resource.ResourceLoader;

import java.util.List;

/**
 * @author fs
 */
public class NewlegalCadastreDialog {
    private static final Logger logger = LogManager.getLogger(NewlegalCadastreDialog.class);

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
        JEVisClass actionPlanDirClass = ds.getJEVisClass(JC.IndexofLegalProvisions.LegalCadastreDirectory.name);
        List<JEVisObject> anaylsisDirs = ds.getObjects(actionPlanDirClass, true);
        boolean canWrite = true;

        if (anaylsisDirs.isEmpty()) {
            //Error Missing Analyse Directory
        }


        Dialog<ButtonType> dialog = new Dialog();
        dialog.setTitle(I18n.getInstance().getString("dialog.indexoflegalprovisions.dialog.new.title"));
        dialog.setHeaderText(I18n.getInstance().getString("dialog.indexoflegalprovisions.dialog.new.header"));
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
        final JFXTextField fName = new JFXTextField();
        fName.setPromptText(I18n.getInstance().getString("jevistree.dialog.new.name.prompt"));


        Label lClass = new Label(I18n.getInstance().getString("dialog.analyses.saveplace"));

        ObservableList<JEVisObject> optionsParents = FXCollections.observableArrayList(anaylsisDirs);

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(actionPlanDirClass.getIcon(), 15, 15);

                                String parentName = "";
                                try {
                                    JEVisObject parent = item.getParents().get(0);//not save
                                    parentName = parent.getName();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }


                                Label cName = new Label(parentName + "/" + item.getName());
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                                //TODO: set canWrite
                            } catch (JEVisException ex) {
                                logger.fatal(ex);
                            }

                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        final JFXComboBox<JEVisObject> comboBox = new JFXComboBox<>(optionsParents);
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

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

                        NewlegalCadastreDialog.this.response = Response.YES;
                    } else {
                        NewlegalCadastreDialog.this.response = Response.CANCEL;
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
