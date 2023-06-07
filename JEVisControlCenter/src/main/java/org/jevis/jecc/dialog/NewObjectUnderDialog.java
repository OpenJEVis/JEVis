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
package org.jevis.jecc.dialog;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.jevistree.JEVisTree;
import org.jevis.jecc.application.jevistree.JEVisTreeFactory;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.plugin.SimpleTargetPlugin;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class NewObjectUnderDialog extends Dialog {

    //    private VBox root = new VBox();
    private final MFXButton okButton = new MFXButton("OK");
    private final MFXButton clear = new MFXButton("Clear");
    private final String ICON = "1404313956_evolution-tasks.png";
    private final JEVisDataSource _ds;
    private final SimpleTargetPlugin stp = new SimpleTargetPlugin();
    private Response response = Response.CANCEL;
    private JEVisTree tree;
    private MODE mode = MODE.OBJECT;

    public NewObjectUnderDialog(JEVisDataSource ds, String title, List<UserSelection> userSelection, MODE mode) {
        this._ds = ds;
        this.mode = mode;
        setTitle(I18n.getInstance().getString("plugin.configuration.newobjectunder.title"));
        setHeaderText(I18n.getInstance().getString("plugin.configuration.newobjectunder.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        okButton.setDefaultButton(true);
//        ok.setDisable(true);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> close());

        okButton.setOnAction(event -> {
            response = Response.OK;
            close();
        });

        ButtonType okType = new ButtonType(okButton.getText(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(cancelButton.getText(), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(okType, cancelType);

        getDialogPane().setContent(build(ds, title, userSelection));
    }

    public void allowMultiSelect(boolean allowMulti) {
        stp.setAllowMultiSelection(allowMulti);
    }

    private VBox build(JEVisDataSource ds, String title, List<UserSelection> uselection) {
        VBox root = new VBox(0);
//        root.setPadding(new Insets(10));
        Node header = DialogHeader.getDialogHeader(ICON, title);
        VBox content = new VBox();

        tree = JEVisTreeFactory.buildBasicDefault(ds, false);
        if (mode == MODE.ATTRIBUTE) {
//            tree.getFilter().showAttributes(true);
        }

        tree.getPlugins().add(stp);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        content.getChildren().setAll(tree);

        MFXCheckbox advanced = new MFXCheckbox("Advanced");

        tree.openUserSelection(uselection);
        stp.setUserSelection(uselection);
//        if (mode == MODE.ATTRIBUTE) {
//            stp.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);
//            advanced.setSelected(true);
//        } else if (mode == MODE.OBJECT) {
//            stp.setMode(SimpleTargetPlugin.MODE.OBJECT);
//            advanced.setSelected(false);
//        }

        advanced.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                Platform.runLater(() -> {
                    tree.setVisible(false);
//                    tree.getFilter().showAttributes(newValue);
//                    tree.reload(selectedObj);
                    tree.openUserSelection(stp.getUserSelection());
                    tree.setVisible(true);
//                    logger.info("Change mode: "+newValue);
//                    content.setVisible(false);
//                    content.getChildren().removeAll();
//
//                    tree = new JEVisTree(ds);
//                    tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//                    tree.getPlugins().add(stp);
//                    if(newValue){
//                        stp.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);
//                        tree.getFilter().showAttributes(true);
//
//                    }else{
//                        stp.setMode(SimpleTargetPlugin.MODE.OBJECT);
//                        tree.getFilter().showAttributes(false);
//                    }
//                    tree.openUserSelection(uselection);
//
//                    content.getChildren().add(tree);
//                    content.setVisible(true);

                });
            }
        });

        tree.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                okButton.setDisable(false);
            }
        });

        stp.getValidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                ok.setDisable(newValue);
            }
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

//        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL_TOP_LEFT), content, buttonPanel);
        root.getChildren().setAll(header, content);
        VBox.setVgrow(header, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(tree, Priority.ALWAYS);

        return root;
    }

    public List<UserSelection> getUserSelection() {
        return stp.getUserSelection();
    }

    public Response getResponse() {
        return response;
    }

    public enum Response {

        OK, CANCEL
    }

    public enum MODE {
        OBJECT, ATTRIBUTE
    }
}
