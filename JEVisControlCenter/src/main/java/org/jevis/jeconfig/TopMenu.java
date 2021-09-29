/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.drivermanagment.ClassImporter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.csv.CSVImportDialog;
import org.jevis.jeconfig.dialog.AboutDialog;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.HiddenConfig;
import org.jevis.jeconfig.tool.PasswordDialog;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * This class builds the top menu bar for the JEVis Control Center.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class TopMenu extends MenuBar {
    private static final Logger logger = LogManager.getLogger(TopMenu.class);
    private final List<MenuItem> items = new ArrayList<>();
    private final StackPane dialogContainer;
    private Plugin activePlugin;
    private static final String stylesString = "/styles/Styles.css";
    private static final String chartString = "/styles/charts.css";
    private static final String standardString = "/styles/Standard.css";
    private static final String darkString = "/styles/Dark.css";
    private static final String amberString = "/styles/Amber.css";
    private static final String greenString = "/styles/Green.css";
    private static final String indigoString = "/styles/Indigo.css";
    private static final String redString = "/styles/Red.css";
    private static final String whiteString = "/styles/White.css";
    private static final List<String> allThemes = Arrays.asList(stylesString, chartString, standardString, darkString, amberString,
            greenString, indigoString, redString, whiteString);
    private static String activeTheme;

    public TopMenu(StackPane dialogContainer) {
        super();
        this.dialogContainer = dialogContainer;

        updateLayout();

    }

    public static void applyActiveTheme(Scene scene) {
        scene.getStylesheets().removeAll(allThemes);
        scene.getStylesheets().add(stylesString);
        scene.getStylesheets().add(chartString);
        scene.getStylesheets().add(activeTheme);
    }

    private void updateLayout() {

        Menu menuFile = new Menu(I18n.getInstance().getString("menu.file"));
        Menu subMenuImport = new Menu(I18n.getInstance().getString("menu.file.import"));
        MenuItem importCSV = new MenuItem(I18n.getInstance().getString("menu.file.import.csv"));
        MenuItem importXML = new MenuItem(I18n.getInstance().getString("menu.file.import.XML"));
        MenuItem importJSON = new MenuItem(I18n.getInstance().getString("menu.file.import.jevis"));
        MenuItem manualData = new MenuItem(I18n.getInstance().getString("menu.file.import.manual"));

        subMenuImport.getItems().addAll(importCSV);//, importXML, importJSON);
//        menuFile.getItems().add(new MenuItem("New"));
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().addAll(subMenuImport, manualData);
        MenuItem exit = new MenuItem(I18n.getInstance().getString("menu.exit"));
//        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
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

        manualData.setOnAction(event -> {
            EnterDataDialog enterDataDialog = new EnterDataDialog(dialogContainer, activePlugin.getDataSource());
            enterDataDialog.show();
        });

        // --- Menu Edit
        Menu menuEdit = new Menu(I18n.getInstance().getString("menu.edit"));

        MenuItem copy = new MenuItem(I18n.getInstance().getString("menu.edit.copy"));
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        MenuItem cut = new MenuItem(I18n.getInstance().getString("menu.edit.cut"));
        cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));

        MenuItem paste = new MenuItem(I18n.getInstance().getString("menu.edit.paste"));
        paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        MenuItem delete = new MenuItem(I18n.getInstance().getString("menu.edit.delete"));
        delete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        MenuItem rename = new MenuItem(I18n.getInstance().getString("menu.edit.rename"));
        rename.setAccelerator(new KeyCodeCombination(KeyCode.F2));

        MenuItem reload = new MenuItem(I18n.getInstance().getString("menu.edit.reload"));
        reload.setAccelerator(new KeyCodeCombination(KeyCode.F5));

        MenuItem findObject = new MenuItem(I18n.getInstance().getString("menu.edit.find"));
        findObject.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

        MenuItem findAgain = new MenuItem(I18n.getInstance().getString("menu.edit.findagain"));
        findAgain.setAccelerator(new KeyCodeCombination(KeyCode.F3));

        MenuItem calcNow = new CheckMenuItem("Calc now");
        calcNow.setOnAction(event -> {
            activePlugin.handleRequest(999);
            event.consume();
        });

        paste.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.PASTE);
            event.consume();
        });

        copy.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.COPY);
            event.consume();
        });

        reload.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.RELOAD);
            event.consume();
        });

        cut.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.CUT);
            event.consume();
        });

        delete.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.DELETE);
            event.consume();
        });

        rename.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.RENAME);
            event.consume();
        });

        findObject.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.FIND_OBJECT);
            event.consume();
        });

        findAgain.setOnAction(event -> {
            activePlugin.handleRequest(Constants.Plugin.Command.FIND_AGAIN);
            event.consume();
        });

        menuEdit.getItems().addAll(copy, cut, paste, new SeparatorMenuItem(), delete, rename, new SeparatorMenuItem(), reload, findObject, findAgain);

        if (JEConfig.getExpert()) {
            /** function does not exists...
             MenuItem replace = new MenuItem(I18n.getInstance().getString("searchbar.button.replace"));
             replace.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
             replace.setOnAction(event -> {
             activePlugin.handleRequest(Constants.Plugin.Command.);
             event.consume();
             });
             **/

            MenuItem deleteAllCleanAndRaw = new MenuItem(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title"));
            deleteAllCleanAndRaw.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            deleteAllCleanAndRaw.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.DELETE_ALL_CLEAN_AND_RAW);
                event.consume();
            });

            MenuItem createMultiplierAndDifferential = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setMultiplierAndDifferential.title"));
            createMultiplierAndDifferential.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            createMultiplierAndDifferential.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.CREATE_MULTIPLIER_AND_DIFFERENTIAL);
                event.consume();
            });

            MenuItem setLimits = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title"));
            setLimits.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setLimits.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.SET_LIMITS);
                event.consume();
            });

            MenuItem setSubstitutionSettings = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setSubstitutionSettingsRecursive.title"));
            setSubstitutionSettings.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setSubstitutionSettings.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.SET_SUBSTITUTION_SETTINGS);
                event.consume();
            });

            MenuItem setUnitsAndPeriods = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setUnitAndPeriodRecursive.title"));
            setUnitsAndPeriods.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setUnitsAndPeriods.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.SET_UNITS_AND_PERIODS);
                event.consume();
            });

            MenuItem enableAll = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.enable"));
            enableAll.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            enableAll.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.ENABLE_ALL);
                event.consume();
            });

            MenuItem disableAll = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.disable"));
            disableAll.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            disableAll.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.DISABLE_ALL);
                event.consume();
            });

            MenuItem resetCalculation = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.resetcalc"));
            resetCalculation.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            resetCalculation.setOnAction(event -> {
                activePlugin.handleRequest(Constants.Plugin.Command.RESET_CALCULATION);
                event.consume();
            });


            menuEdit.getItems().addAll(new SeparatorMenuItem(), deleteAllCleanAndRaw, setLimits, setSubstitutionSettings, createMultiplierAndDifferential, setUnitsAndPeriods, enableAll, disableAll, resetCalculation);
        }

//        menuEdit.getItems().addAll(copie, delete, rename);
        // --- Menu View
        Menu menuView = new Menu(I18n.getInstance().getString("menu.view"));

        Menu options = new Menu(I18n.getInstance().getString("menu.option"));

        final Preferences preview = Preferences.userRoot().node("JEVis.JEConfig.preview");
        CheckMenuItem enablePreview = new CheckMenuItem(I18n.getInstance().getString("menu.options.preview"));
        enablePreview.setSelected(preview.getBoolean("enabled", true));
        enablePreview.setOnAction(e -> preview.putBoolean("enabled", !preview.getBoolean("enabled", true)));

        final Preferences prefWelcome = Preferences.userRoot().node("JEVis.JEConfig.Welcome");
        CheckMenuItem welcome = new CheckMenuItem(I18n.getInstance().getString("menu.options.welcome"));
        welcome.setSelected(prefWelcome.getBoolean("show", true));
        welcome.setOnAction(e -> prefWelcome.putBoolean("show", !prefWelcome.getBoolean("show", false)));

        final Preferences patchNotes = Preferences.userRoot().node("JEVis.JEConfig.patchNotes");
        CheckMenuItem showPatchNotes = new CheckMenuItem(I18n.getInstance().getString("menu.options.patchnotes"));
        showPatchNotes.setSelected(prefWelcome.getBoolean("show", true));
        showPatchNotes.setOnAction(e -> {
            patchNotes.put("version", JEConfig.class.getPackage().getImplementationVersion());
            patchNotes.putBoolean("show", !patchNotes.getBoolean("show", true));
        });

        MenuItem changePassword = new MenuItem(I18n.getInstance().getString("menu.options.changepassword"));
        changePassword.setOnAction(event -> {
            PasswordDialog dia = new PasswordDialog();
            try {
                if (dia.show(JEConfig.getStage(), JEConfig.getDataSource().getCurrentUser().getUserObject()) == PasswordDialog.Response.YES) {
                    String note = String.format("Password set by %s", activePlugin.getDataSource().getCurrentUser().getAccountName());

                    JEVisSample sample;
                    if (activePlugin.getDataSource().getCurrentUser().getUserObject().getAttribute("Password").hasSample()) {
                        sample = activePlugin.getDataSource().getCurrentUser().getUserObject().getAttribute("Password").getLatestSample();
                        sample.setValue(dia.getPassword());
                    } else {
                        sample = activePlugin.getDataSource().getCurrentUser().getUserObject().getAttribute("Password").buildSample(new DateTime(), dia.getPassword(), note);

                    }
                    sample.commit();


                }
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        });

        final Preferences prefExpert = Preferences.userRoot().node("JEVis.JEConfig.Expert");
        CheckMenuItem expertMode = new CheckMenuItem(I18n.getInstance().getString("menu.options.expert"));
        expertMode.setSelected(prefExpert.getBoolean("show", false));
        expertMode.setOnAction(e -> {
            prefExpert.putBoolean("show", !prefExpert.getBoolean("show", false));
            activePlugin.updateToolbar();
            Platform.runLater(() -> updateLayout());
        });

        final Preferences prefThreads = Preferences.userRoot().node("JEVis.JEConfig.threads");
        int optCores = Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors() - 1 : 1;
        HiddenConfig.DASH_THREADS = prefThreads.getInt("count", optCores);
        int selectedThreadCount = prefThreads.getInt("count", optCores);
        Menu threadCount = new Menu(I18n.getInstance().getString("menu.options.threads"));
        for (int i = 1; i <= optCores; i++) {
            CheckMenuItem cmi = new CheckMenuItem(String.valueOf(i));

            if (i == selectedThreadCount) cmi.setSelected(true);

            int finalI = i;
            cmi.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    JEConfig.getStatusBar().setParallelProcesses(finalI);
                    for (MenuItem menuItem : threadCount.getItems()) {
                        if (menuItem instanceof CheckMenuItem) {
                            CheckMenuItem otherItem = (CheckMenuItem) menuItem;
                            if (!otherItem.equals(cmi)) {
                                otherItem.setSelected(false);
                            }
                        }
                    }
                }
            });

            threadCount.getItems().add(cmi);
        }

        options.getItems().addAll(changePassword, enablePreview, welcome, showPatchNotes, expertMode);

        if (JEConfig.getExpert()) {
            options.getItems().add(threadCount);
        }

        Menu view = new Menu(I18n.getInstance().getString("menu.view"));
        Menu theme = new Menu(I18n.getInstance().getString("menu.view.theme"));

        final Preferences prefTheme = Preferences.userRoot().node("JEVis.JEConfig.Theme");

        CheckMenuItem standardTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.standard"));
        CheckMenuItem darkTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.dark"));
        CheckMenuItem amberTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.amber"));
        CheckMenuItem greenTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.green"));
        CheckMenuItem indigoTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.indigo"));
        CheckMenuItem redTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.red"));
        CheckMenuItem whiteTheme = new CheckMenuItem(I18n.getInstance().getString("menu.view.theme.white"));

        standardTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {

                prefTheme.putBoolean("standard", true);
                applyTheme(standardString);

                darkTheme.setSelected(false);
                amberTheme.setSelected(false);
                greenTheme.setSelected(false);
                indigoTheme.setSelected(false);
                redTheme.setSelected(false);
                whiteTheme.setSelected(false);
            } else {
                prefTheme.putBoolean("standard", false);
            }
        });

        darkTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("dark", true);
                applyTheme(darkString);

                standardTheme.setSelected(false);
                amberTheme.setSelected(false);
                greenTheme.setSelected(false);
                indigoTheme.setSelected(false);
                redTheme.setSelected(false);
                whiteTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("dark", false);
            }
        });

        amberTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("amber", true);
                applyTheme(amberString);

                darkTheme.setSelected(false);
                standardTheme.setSelected(false);
                greenTheme.setSelected(false);
                indigoTheme.setSelected(false);
                redTheme.setSelected(false);
                whiteTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("amber", false);
            }
        });

        greenTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("green", true);
                applyTheme(greenString);

                darkTheme.setSelected(false);
                amberTheme.setSelected(false);
                standardTheme.setSelected(false);
                indigoTheme.setSelected(false);
                redTheme.setSelected(false);
                whiteTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("green", false);
            }
        });

        indigoTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("indigo", true);
                applyTheme(indigoString);

                darkTheme.setSelected(false);
                amberTheme.setSelected(false);
                greenTheme.setSelected(false);
                standardTheme.setSelected(false);
                redTheme.setSelected(false);
                whiteTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("indigo", false);
            }
        });

        redTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("red", true);
                applyTheme(redString);

                darkTheme.setSelected(false);
                amberTheme.setSelected(false);
                greenTheme.setSelected(false);
                indigoTheme.setSelected(false);
                standardTheme.setSelected(false);
                whiteTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("red", false);
            }
        });

        whiteTheme.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prefTheme.putBoolean("white", true);
                applyTheme(whiteString);

                darkTheme.setSelected(false);
                amberTheme.setSelected(false);
                greenTheme.setSelected(false);
                indigoTheme.setSelected(false);
                redTheme.setSelected(false);
                standardTheme.setSelected(false);

            } else {
                prefTheme.putBoolean("white", false);
            }
        });

        standardTheme.setSelected(prefTheme.getBoolean("standard", true));
        darkTheme.setSelected(prefTheme.getBoolean("dark", false));
        amberTheme.setSelected(prefTheme.getBoolean("amber", false));
        greenTheme.setSelected(prefTheme.getBoolean("green", false));
        indigoTheme.setSelected(prefTheme.getBoolean("indigo", false));
        redTheme.setSelected(prefTheme.getBoolean("red", false));
        whiteTheme.setSelected(prefTheme.getBoolean("white", false));

        theme.getItems().addAll(standardTheme, darkTheme, amberTheme, greenTheme, indigoTheme, redTheme, whiteTheme);
        view.getItems().add(theme);

        Menu help = new Menu(I18n.getInstance().getString("menu.help"));

        MenuItem showHelp = new MenuItem(I18n.getInstance().getString("menu.showToolTips"));
        MenuItem about = new MenuItem(I18n.getInstance().getString("menu.about"));
        help.getItems().addAll(showHelp, about);

        about.setOnAction(t -> {
            AboutDialog dia = new AboutDialog();
            dia.show(I18n.getInstance().getString("menu.about.title")
                    , I18n.getInstance().getString("menu.about.message")
                    , JEConfig.PROGRAM_INFO, JEConfig.getImage("JEConfig_mac.png"));

        });
        showHelp.setOnAction(event -> {
            //activePlugin.handleRequest(Constants.Plugin.Command.SHOW_TOOLTIP_HELP);
            JEVisHelp.getInstance().toggleHelp();
        });


        MenuItem classImport = new MenuItem(I18n.getInstance().getString("menu.system.driver"));
        Menu system = new Menu(I18n.getInstance().getString("menu.system"));
        system.getItems().add(classImport);

        //TODO: replace this very simple driver import
        classImport.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(I18n.getInstance().getString("menu.system.driver.open.title"));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(I18n.getInstance().getString("menuJEDataProcessor - Gap filling #429.system.driver.open.filter.jevis"), "*.jev"),
                    new FileChooser.ExtensionFilter(I18n.getInstance().getString("menu.system.driver.open.filter.all"), "*.*"));

            File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
            if (selectedFile != null) {
                List<File> files = new ArrayList<>();
                String tmpdir = System.getProperty("java.io.tmpdir");
                ClassImporter ci = new ClassImporter(JEConfig.getDataSource());
                files = ci.unZipIt(tmpdir, selectedFile);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(I18n.getInstance().getString("menu.system.driver.confirm.title"));
                alert.setHeaderText(I18n.getInstance().getString("menu.system.driver.confirm.header", selectedFile.getName()));
                alert.setContentText(I18n.getInstance().getString("menu.system.driver.confirm.message"));

                ButtonType updateButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.update"));
                ButtonType overwriteButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.override"));
                ButtonType cancelButton = new ButtonType(I18n.getInstance().getString("menu.system.driver.confirm.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

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
                    Alert finish = new Alert(Alert.AlertType.INFORMATION);
                    finish.setTitle(I18n.getInstance().getString("menu.system.driver.success.title"));
                    finish.setHeaderText(null);
                    finish.setContentText(I18n.getInstance().getString("menu.system.driver.success.message"));

                    finish.showAndWait();
                }

            }

        });
        try {
            classImport.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {
            logger.fatal(ex);
            classImport.setDisable(true);
        }

        getMenus().setAll(menuFile, menuEdit, options, system, view, help);
    }

    public void setPlugin(Plugin plugin) {
        activePlugin = plugin;
    }

    private void applyTheme(String themeString) {
        JEConfig.getStage().getScene().getStylesheets().removeAll(allThemes);
        JEConfig.getStage().getScene().getStylesheets().add(stylesString);
        JEConfig.getStage().getScene().getStylesheets().add(chartString);
        JEConfig.getStage().getScene().getStylesheets().add(themeString);

        activeTheme = themeString;
    }
}
