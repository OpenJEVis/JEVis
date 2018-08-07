/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import org.jevis.application.dialog.AboutDialog;
import org.jevis.commons.drivermanagment.ClassImporter;
import org.jevis.jeconfig.csv.CSVImportDialog;
import org.jevis.jeconfig.tool.I18n;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * This class build the top menu bar for JEConfig.
 *
 *
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class TopMenu extends MenuBar {

    private List<MenuItem> items = new ArrayList<>();
    private Plugin activPlugin;

    public TopMenu() {
        super();

        Menu menuFile = new Menu(I18n.getInstance().getString("menu.file"));
        Menu subMenuImport = new Menu(I18n.getInstance().getString("menu.file.import"));
        MenuItem importCSV = new MenuItem(I18n.getInstance().getString("menu.file.import.csv"));
        MenuItem importXML = new MenuItem(I18n.getInstance().getString("menu.file.import.XML"));
        MenuItem importJSON = new MenuItem(I18n.getInstance().getString("menu.file.import.jevis"));

        subMenuImport.getItems().addAll(importCSV);//, importXML, importJSON);
//        menuFile.getItems().add(new MenuItem("New"));
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(subMenuImport);
        MenuItem exit = new MenuItem(I18n.getInstance().getString("menu.exit"));
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        menuFile.getItems().add(exit);

        importJSON.setDisable(true);
        importXML.setDisable(true);

        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                System.exit(0);
            }
        });

        importCSV.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                CSVImportDialog impDia = new CSVImportDialog();
                impDia.show(JEConfig.getStage(), JEConfig.getDataSource());
            }
        });

        // --- Menu Edit
        Menu menuEdit = new Menu(I18n.getInstance().getString("menu.edit"));

        MenuItem copie = new MenuItem(I18n.getInstance().getString("menu.edit.copy"));
        copie.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        MenuItem paste = new MenuItem(I18n.getInstance().getString("menu.edit.paste"));
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        MenuItem delete = new MenuItem(I18n.getInstance().getString("menu.edit.delete"));
        delete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        MenuItem rename = new MenuItem(I18n.getInstance().getString("menu.edit.rename"));
        rename.setAccelerator(new KeyCodeCombination(KeyCode.F2));

        MenuItem findObject = new MenuItem(I18n.getInstance().getString("menu.edit.find"));
        findObject.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

        paste.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                activPlugin.handleRequest(Constants.Plugin.Command.PASTE);
            }
        });

        copie.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                activPlugin.handleRequest(Constants.Plugin.Command.COPY);
            }
        });

        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                activPlugin.handleRequest(Constants.Plugin.Command.DELTE);
            }
        });

        rename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                activPlugin.handleRequest(Constants.Plugin.Command.RENAME);
            }
        });

        findObject.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                activPlugin.handleRequest(Constants.Plugin.Command.FIND_OBJECT);
            }
        });
        menuEdit.getItems().addAll(copie, paste, delete, rename, findObject);

//        menuEdit.getItems().addAll(copie, delete, rename);
        // --- Menu View
        Menu menuView = new Menu(I18n.getInstance().getString("menu.view"));

        Menu options = new Menu(I18n.getInstance().getString("menu.option"));
        final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.Welcome");
        CheckMenuItem welcome = new CheckMenuItem(I18n.getInstance().getString("menu.options.welcome"));
        welcome.setSelected(pref.getBoolean("show", true));
        welcome.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pref.putBoolean("show", !pref.getBoolean("show", true));
            }
        });
        options.getItems().add(welcome);

        Menu help = new Menu(I18n.getInstance().getString("menu.help"));

        MenuItem about = new MenuItem(I18n.getInstance().getString("menu.about"));
        help.getItems().add(about);
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

                AboutDialog dia = new AboutDialog();
                dia.show(JEConfig.getStage(), I18n.getInstance().getString("menu.about.title")
                        , I18n.getInstance().getString("menu.about.message")
                        , JEConfig.PROGRAMM_INFO, JEConfig.getImage("JEConfig_mac.png"));

            }
        });
        MenuItem classImport = new MenuItem(I18n.getInstance().getString("menu.system.driver"));
        Menu system = new Menu(I18n.getInstance().getString("menu.system"));
        system.getItems().add(classImport);

        //TODO: replace this very simple driver import
        classImport.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(I18n.getInstance().getString("menu.system.driver.open.title"));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter(I18n.getInstance().getString("menu.system.driver.open.filter.jevis"), "*.jev"),
                        new FileChooser.ExtensionFilter(I18n.getInstance().getString("menu.system.driver.open.filter.all"), "*.*"));

                File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    List<File> files = new ArrayList<>();
                    String tmpdir = System.getProperty("java.io.tmpdir");
                    ClassImporter ci = new ClassImporter(JEConfig.getDataSource());
                    files = ci.unZipIt(tmpdir, selectedFile);

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("menu.system.driver.confirm.title"));
                    alert.setHeaderText(I18n.getInstance().getString("menu.system.driver.confirm.header", selectedFile.getName()));
                    alert.setContentText(I18n.getInstance().getString("menu.system.driver.confirm.message"));

                    ButtonType updateButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.update"));
                    ButtonType overwriteButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.override"));
                    ButtonType cancelButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.cancel"), ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(updateButton, overwriteButton, cancelButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    boolean success = true;//dirty solution
                    if (result.get() == updateButton) {
                        ci.setDeleteExisting(false);
                        ci.importFiles(files);
                    } else if (result.get() == overwriteButton) {
                        ci.setDeleteExisting(true);
                        ci.importFiles(files);
                    } else if (result.get() == cancelButton) {
                        success = false;
                    }

                    if (success) {
                        Alert finish = new Alert(AlertType.INFORMATION);
                        finish.setTitle(I18n.getInstance().getString("menu.system.driver.success.title"));
                        finish.setHeaderText(null);
                        finish.setContentText(I18n.getInstance().getString("menu.system.driver.success.message"));

                        finish.showAndWait();
                    }

                }

            }
        });
        try {
            classImport.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {
            classImport.setDisable(true);
        }

        getMenus().addAll(menuFile, menuEdit, options, system, help);
    }

    public void setPlugin(Plugin plugin) {
        activPlugin = plugin;
    }

}
