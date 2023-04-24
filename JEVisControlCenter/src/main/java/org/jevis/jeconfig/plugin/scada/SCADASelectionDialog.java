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
package org.jevis.jeconfig.plugin.scada;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.dialog.DialogHeader;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SCADASelectionDialog extends Dialog {

    //    private VBox root = new VBox();
    private final JFXButton ok = new JFXButton("OK");
    private final JFXButton clear = new JFXButton("Clear");
    private final String ICON = "1404313956_evolution-tasks.png";
    private final JEVisDataSource _ds;
    private Response response = Response.CANCEL;
    private JEVisTree tree;
    private final SimpleTargetPlugin stp = new SimpleTargetPlugin();
    private MODE mode = MODE.OBJECT;

    public SCADASelectionDialog(JEVisDataSource ds, String title, List<UserSelection> uselection, MODE mode) {
        setTitle(I18n.getInstance().getString("plugin.scada.selectiondialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.scada.selectiondialog.header"));
        setResizable(true);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        _ds = ds;
        this.mode = mode;

        getDialogPane().setContent(build(ds, title, uselection));
    }

    public void allowMultySelect(boolean allowMulty) {
        stp.setAllowMultiSelection(allowMulty);
    }

    private VBox build(JEVisDataSource ds, String title, List<UserSelection> uselection) {
        VBox root = new VBox(0);
//        root.setPadding(new Insets(10));
        Node header = DialogHeader.getDialogHeader(ICON, title);
        HBox buttonPanel = new HBox(8);
        VBox content = new VBox();

        tree = JEVisTreeFactory.buildBasicDefault(ds, false);
        if (mode == MODE.ATTRIBUTE) {
//            tree.getFilter().showAttributes(true);
        }

        tree.getPlugins().add(stp);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        content.getChildren().setAll(tree);


        JFXCheckBox advanced = new JFXCheckBox("Advanced");


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
                ok.setDisable(false);
            }
        });

        stp.getValidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                ok.setDisable(newValue);
            }
        });

        ok.setDefaultButton(true);
//        ok.setDisable(true);

        JFXButton cancel = new JFXButton("Cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());

        ok.setOnAction(event -> {
            response = Response.OK;
            close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonPanel.getChildren().setAll(advanced, spacer, ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));


//        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL_TOP_LEFT), content, buttonPanel);
        root.getChildren().setAll(header, content, buttonPanel);
        VBox.setVgrow(header, Priority.NEVER);
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(tree, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        return root;
    }

    public List<UserSelection> getUserSelection() {
        return stp.getUserSelection();
    }


    public enum Response {

        OK, CANCEL
    }

    public enum MODE {
        OBJECT, ATTRIBUTE
    }

    public Response getResponse() {
        return response;
    }
}
