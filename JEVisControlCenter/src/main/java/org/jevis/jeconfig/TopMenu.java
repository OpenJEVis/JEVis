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

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.drivermanagment.ClassImporter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.FavoriteAnalysis;
import org.jevis.jeconfig.application.Chart.data.FavoriteAnalysisHandler;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.csv.CSVImportDialog;
import org.jevis.jeconfig.dialog.AboutDialog;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.HiddenConfig;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.tool.PasswordDialog;
import org.jevis.jeconfig.tool.PatchNotesPage;
import org.jevis.jeconfig.tool.TrianglePerformanceTest;
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
    private static final String stylesString = "/styles/Styles.css";
    private static final String chartString = "/styles/charts.css";
    private static final String rtfString = "rtf/richtext/rich-text.css";
    private static final String standardString = "/styles/Standard.css";
    private static final String darkString = "/styles/Dark.css";
    private static final String amberString = "/styles/Amber.css";
    private static final String greenString = "/styles/Green.css";
    private static final String indigoString = "/styles/Indigo.css";
    private static final String redString = "/styles/Red.css";
    private static final String whiteString = "/styles/White.css";
    private static final List<String> allThemes = Arrays.asList(stylesString, chartString, rtfString, standardString, darkString, amberString,
            greenString, indigoString, redString, whiteString);
    private static String activeTheme;
    private final List<MenuItem> items = new ArrayList<>();
    private final SimpleObjectProperty<Plugin> activePlugin = new SimpleObjectProperty<>();

    public TopMenu() {
        super();

        updateLayout();

        this.activePluginProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateLayout));
    }

    public static void applyActiveTheme(Scene scene) {
        scene.getStylesheets().removeAll(allThemes);
        scene.getStylesheets().add(stylesString);
        scene.getStylesheets().add(chartString);
        scene.getStylesheets().add(rtfString);
        scene.getStylesheets().add(activeTheme);
    }

    private void updateLayout() {
        getMenus().clear();

        // --- File Menu
        getMenus().add(createFileMenu());

        // --- Edit Menu
        getMenus().add(createEditMenu());

        // --- Plugin Menu
        if (getActivePlugin() != null) {
            getMenus().add(createPluginMenu());
        }

        // --- Options Menu
        getMenus().add(createOptionsMenu());

        // --- System Menu
        getMenus().add(createSystemMenu());

        // --- View Menu
        getMenus().add(createViewMenu());

        // --- Help Menu
        getMenus().add(createHelpMenu());
    }

    private Menu createPluginMenu() {
        Menu pluginMenu = new Menu(I18n.getInstance().getString("menu.plugins.chart.favorites"));

        if (getActivePlugin() != null && getActivePlugin() instanceof ChartPlugin) {
            ChartPlugin chartPlugin = (ChartPlugin) getActivePlugin();
            FavoriteAnalysisHandler favoriteAnalysisHandler = chartPlugin.getFavoriteAnalysisHandler();
            favoriteAnalysisHandler.loadDataModel();

            MenuItem addAnalysisToFavorites = new MenuItem(I18n.getInstance().getString("menu.plugins.chart.addfavorite"));

            addAnalysisToFavorites.setOnAction(event -> {
                JEVisObject selectedAnalysis = chartPlugin.getToolBarView().getAnalysesComboBox().getSelectionModel().getSelectedItem();
                DataSettings dataSettings = chartPlugin.getDataSettings();

                if (selectedAnalysis != null) {
                    Dialog confirmationDialog = new Dialog();

                    confirmationDialog.setTitle(I18n.getInstance().getString("menu.confirmationdialog.title"));
                    confirmationDialog.setHeaderText(I18n.getInstance().getString("menu.confirmationdialog.header"));
                    confirmationDialog.setResizable(true);
                    confirmationDialog.initOwner(JEConfig.getStage());
                    confirmationDialog.initModality(Modality.APPLICATION_MODAL);
                    Stage stage = (Stage) confirmationDialog.getDialogPane().getScene().getWindow();
                    TopMenu.applyActiveTheme(stage.getScene());
                    stage.setAlwaysOnTop(true);

                    ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                    confirmationDialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

                    Button okButton = (Button) confirmationDialog.getDialogPane().lookupButton(okType);
                    okButton.setDefaultButton(true);

                    Button cancelButton = (Button) confirmationDialog.getDialogPane().lookupButton(cancelType);
                    cancelButton.setCancelButton(true);
                    cancelButton.setOnAction(actionEvent -> confirmationDialog.close());

                    FavoriteAnalysis favoriteAnalysis = new FavoriteAnalysis();
                    favoriteAnalysis.setId(selectedAnalysis.getID());
                    favoriteAnalysis.setAggregationPeriod(dataSettings.getAggregationPeriod());
                    favoriteAnalysis.setManipulationMode(dataSettings.getManipulationMode());
                    favoriteAnalysis.setTimeFrame(dataSettings.getAnalysisTimeFrame().getTimeFrame());
                    if (dataSettings.getAnalysisTimeFrame().getTimeFrame() == TimeFrame.CUSTOM) {
                        favoriteAnalysis.setStart(dataSettings.getAnalysisTimeFrame().getStart().toString());
                        favoriteAnalysis.setEnd(dataSettings.getAnalysisTimeFrame().getEnd().toString());
                    }

                    String suggestedName = favoriteAnalysis.createName(JEConfig.getDataSource());
                    JFXTextField favoriteName = new JFXTextField();
                    favoriteName.setMinWidth(450);
                    favoriteName.textProperty().bindBidirectional(favoriteAnalysis.nameProperty());
                    favoriteName.setText(suggestedName);
                    favoriteName.selectAll();

                    okButton.setOnAction(actionEvent -> {
                        favoriteAnalysisHandler.getFavoriteAnalysesList().add(favoriteAnalysis);
                        favoriteAnalysisHandler.saveDataModel();
                        confirmationDialog.close();
                        Platform.runLater(this::updateLayout);
                    });

                    VBox content = new VBox(6, favoriteName);
                    content.setPadding(new Insets(15));

                    confirmationDialog.getDialogPane().setContent(content);

                    confirmationDialog.show();
                }
            });

            MenuItem removeAnalysisFromFavorites = new MenuItem(I18n.getInstance().getString("menu.plugins.chart.removefavorite"));

            removeAnalysisFromFavorites.setOnAction(event -> {
                JEVisObject selectedAnalysis = chartPlugin.getToolBarView().getAnalysesComboBox().getSelectionModel().getSelectedItem();
                DataSettings dataSettings = chartPlugin.getDataSettings();
                List<FavoriteAnalysis> toBeRemoved = new ArrayList<>();
                favoriteAnalysisHandler.getFavoriteAnalysesList().forEach(favoriteAnalysis -> {
                    if (selectedAnalysis.getID().equals(favoriteAnalysis.getId())
                            && dataSettings.getAnalysisTimeFrame().getTimeFrame() == favoriteAnalysis.getTimeFrame()
                            && dataSettings.getAggregationPeriod() == favoriteAnalysis.getAggregationPeriod()
                            && dataSettings.getManipulationMode() == favoriteAnalysis.getManipulationMode()) {
                        toBeRemoved.add(favoriteAnalysis);
                    }
                });

                favoriteAnalysisHandler.getFavoriteAnalysesList().removeAll(toBeRemoved);
                favoriteAnalysisHandler.saveDataModel();

                Platform.runLater(this::updateLayout);
            });

            MenuItem separatorMenuItem = new SeparatorMenuItem();

            List<MenuItem> analysisItems = new ArrayList<>();
            for (FavoriteAnalysis favoriteAnalysis : favoriteAnalysisHandler.getFavoriteAnalysesList()) {
                try {
                    JEVisObject analysisObject = JEConfig.getDataSource().getObject(favoriteAnalysis.getId());

                    DataSettings dataSettings = new DataSettings();

                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(JEConfig.getDataSource(), analysisObject, favoriteAnalysis.getTimeFrame());

                    if (favoriteAnalysis.getTimeFrame() == TimeFrame.CUSTOM) {
                        analysisTimeFrame.setStart(new DateTime(favoriteAnalysis.getStart()));
                        analysisTimeFrame.setEnd(new DateTime(favoriteAnalysis.getEnd()));
                    }

                    dataSettings.setAnalysisTimeFrame(analysisTimeFrame);
                    dataSettings.setAggregationPeriod(favoriteAnalysis.getAggregationPeriod());
                    dataSettings.setManipulationMode(favoriteAnalysis.getManipulationMode());

                    MenuItem favoriteAnalysisMenuItem = new MenuItem(favoriteAnalysis.getName());
                    favoriteAnalysisMenuItem.setOnAction(event -> chartPlugin.openObject(analysisObject, dataSettings));

                    analysisItems.add(favoriteAnalysisMenuItem);
                } catch (Exception e) {
                    logger.error("Could not create menu item for analysis {}", favoriteAnalysis.getId());
                }
            }

            pluginMenu.getItems().setAll(addAnalysisToFavorites, removeAnalysisFromFavorites, separatorMenuItem);
            pluginMenu.getItems().addAll(analysisItems);
        }

        return pluginMenu;
    }

    private Menu createSystemMenu() {
        Menu system = new Menu(I18n.getInstance().getString("menu.system"));

        MenuItem driverImport = new MenuItem(I18n.getInstance().getString("menu.system.driver"));

        system.getItems().add(driverImport);
        if (JEConfig.getExpert()) {
            MenuItem benchmark = new MenuItem("Benchmark");
            benchmark.setOnAction(event -> {
                try {
                    TrianglePerformanceTest trianglePerformanceTest = new TrianglePerformanceTest();
                    Stage stage = new Stage();
                    trianglePerformanceTest.start(stage);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });
            system.getItems().add(benchmark);
        }


        //TODO: replace this very simple driver import
        driverImport.setOnAction(event -> {

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
            driverImport.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {
            logger.fatal(ex);
            driverImport.setDisable(true);
        }
        return system;
    }

    private Menu createHelpMenu() {
        Menu help = new Menu(I18n.getInstance().getString("menu.help"));

        MenuItem showChangelog = new MenuItem(I18n.getInstance().getString("menu.options.patchnotes"));
        MenuItem showHelp = new MenuItem(I18n.getInstance().getString("menu.showToolTips"));
        MenuItem about = new MenuItem(I18n.getInstance().getString("menu.about"));
        CheckMenuItem debug = new CheckMenuItem("Debug");
        help.getItems().addAll(showHelp, showChangelog, about);
        if (JEConfig.getExpert()) {
            help.getItems().add(debug);
        }

        debug.setOnAction(event -> {


            if (debug.isSelected()) {
                logger.debug("Is debug");
                Logger logger = LogManager.getRootLogger();
                Configurator.setAllLevels(logger.getName(), Level.DEBUG);
                LogManager.getLogger(TopMenu.class);
            } else {
                logger.debug("Is not debug");
                Logger logger = LogManager.getRootLogger();
                Configurator.setAllLevels(logger.getName(), Level.INFO);
                LogManager.getLogger(TopMenu.class);
            }
        });

        about.setOnAction(t -> {
            AboutDialog dia = new AboutDialog();
            dia.show(I18n.getInstance().getString("menu.about.title")
                    , I18n.getInstance().getString("menu.about.message")
                    , JEConfig.PROGRAM_INFO, JEConfig.getImage("JEConfig_mac.png"));

        });
        showHelp.setOnAction(event -> JEVisHelp.getInstance().toggleHelp());

        showChangelog.setOnAction(event -> {
            PatchNotesPage patchNotesPage = new PatchNotesPage();
            patchNotesPage.show(JEConfig.getStage());
        });
        return help;
    }

    private Menu createViewMenu() {
        Menu menuView = new Menu(I18n.getInstance().getString("menu.view"));

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
        return view;
    }

    private Menu createOptionsMenu() {
        Menu options = new Menu(I18n.getInstance().getString("menu.option"));

        final Preferences preview = Preferences.userRoot().node("JEVis.JEConfig.preview");
        CheckMenuItem enablePreview = new CheckMenuItem(I18n.getInstance().getString("menu.options.preview"));
        enablePreview.setSelected(preview.getBoolean("enabled", true));
        enablePreview.setOnAction(e -> preview.putBoolean("enabled", !preview.getBoolean("enabled", true)));

        final Preferences prefWelcome = Preferences.userRoot().node("JEVis.JEConfig.Welcome");
        CheckMenuItem welcome = new CheckMenuItem(I18n.getInstance().getString("menu.options.welcome"));
        welcome.setSelected(prefWelcome.getBoolean("show", true));
        welcome.setOnAction(e -> prefWelcome.putBoolean("show", !prefWelcome.getBoolean("show", false)));

        MenuItem changePassword = new MenuItem(I18n.getInstance().getString("menu.options.changepassword"));
        changePassword.setOnAction(event -> {
            PasswordDialog dia = new PasswordDialog();
            try {
                if (dia.show(JEConfig.getStage(), JEConfig.getDataSource().getCurrentUser().getUserObject()) == PasswordDialog.Response.YES) {
                    String note = String.format("Password set by %s", getActivePlugin().getDataSource().getCurrentUser().getAccountName());

                    JEVisSample sample;
                    /*
                    if (getActivePlugin().getDataSource().getCurrentUser().getUserObject().getAttribute("Password").hasSample()) {
                        sample = getActivePlugin().getDataSource().getCurrentUser().getUserObject().getAttribute("Password").getLatestSample();
                        sample.setValue(dia.getPassword());
                    } else {*/
                    sample = getActivePlugin().getDataSource().getCurrentUser().getUserObject().getAttribute("Password").buildSample(new DateTime(), dia.getPassword(), note);

                    // }
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
            getActivePlugin().updateToolbar();
            Platform.runLater(() -> updateLayout());
        });

        final Preferences prefThreads = Preferences.userRoot().node("JEVis.JEConfig.threads");
        int optCores = Math.max(Runtime.getRuntime().availableProcessors(), 1);
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

        options.getItems().addAll(changePassword, enablePreview, welcome, expertMode);

        if (JEConfig.getExpert()) {
            options.getItems().add(threadCount);
        }
        return options;
    }

    private Menu createEditMenu() {
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
            getActivePlugin().handleRequest(999);
            event.consume();
        });

        paste.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.PASTE);
            event.consume();
        });

        copy.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.COPY);
            event.consume();
        });

        reload.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.RELOAD);
            event.consume();
        });

        cut.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.CUT);
            event.consume();
        });

        delete.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.DELETE);
            event.consume();
        });

        rename.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.RENAME);
            event.consume();
        });

        findObject.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.FIND_OBJECT);
            event.consume();
        });

        findAgain.setOnAction(event -> {
            getActivePlugin().handleRequest(Constants.Plugin.Command.FIND_AGAIN);
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
                getActivePlugin().handleRequest(Constants.Plugin.Command.DELETE_ALL_CLEAN_AND_RAW);
                event.consume();
            });

            MenuItem createMultiplierAndDifferential = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setMultiplierAndDifferential.title"));
            createMultiplierAndDifferential.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            createMultiplierAndDifferential.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.CREATE_MULTIPLIER_AND_DIFFERENTIAL);
                event.consume();
            });

            MenuItem setLimits = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title"));
            setLimits.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setLimits.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.SET_LIMITS);
                event.consume();
            });

            MenuItem setSubstitutionSettings = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setSubstitutionSettingsRecursive.title"));
            setSubstitutionSettings.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setSubstitutionSettings.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.SET_SUBSTITUTION_SETTINGS);
                event.consume();
            });

            MenuItem setUnitsAndPeriods = new MenuItem(I18n.getInstance().getString("jevistree.dialog.setUnitAndPeriodRecursive.title"));
            setUnitsAndPeriods.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            setUnitsAndPeriods.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.SET_UNITS_AND_PERIODS);
                event.consume();
            });

            MenuItem enableAll = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.enable"));
            enableAll.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            enableAll.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.ENABLE_ALL);
                event.consume();
            });

            MenuItem disableAll = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.disable"));
            disableAll.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            disableAll.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.DISABLE_ALL);
                event.consume();
            });

            MenuItem resetCalculation = new MenuItem(I18n.getInstance().getString("jevistree.dialog.enable.title.resetcalc"));
            resetCalculation.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            resetCalculation.setOnAction(event -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.RESET_CALCULATION);
                event.consume();
            });

            MenuItem deleteDependencies = new MenuItem(I18n.getInstance().getString("plugin.objects.dialog.deletedependencies.title"));
            deleteDependencies.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            deleteDependencies.setOnAction(actionEvent -> {
                getActivePlugin().handleRequest(Constants.Plugin.Command.DELETE_DEPENDENCIES);
                actionEvent.consume();
            });

            menuEdit.getItems().addAll(new SeparatorMenuItem(), deleteAllCleanAndRaw, setLimits, setSubstitutionSettings, createMultiplierAndDifferential, setUnitsAndPeriods, enableAll, disableAll, resetCalculation, deleteDependencies);
        }

        return menuEdit;
    }

    private Menu createFileMenu() {
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
            EnterDataDialog enterDataDialog = new EnterDataDialog(getActivePlugin().getDataSource());
            enterDataDialog.show();
        });

        return menuFile;
    }

    public void setPlugin(Plugin plugin) {
        setActivePlugin(plugin);
    }

    private void applyTheme(String themeString) {
        Platform.runLater(() -> {
            JEConfig.getStage().getScene().getStylesheets().removeAll(allThemes);
            JEConfig.getStage().getScene().getStylesheets().add(stylesString);
            JEConfig.getStage().getScene().getStylesheets().add(chartString);
            JEConfig.getStage().getScene().getStylesheets().add(rtfString);
            JEConfig.getStage().getScene().getStylesheets().add(themeString);
        });

        activeTheme = themeString;
    }

    public Plugin getActivePlugin() {
        return activePlugin.get();
    }

    public void setActivePlugin(Plugin activePlugin) {
        this.activePlugin.set(activePlugin);
    }

    public SimpleObjectProperty<Plugin> activePluginProperty() {
        return activePlugin;
    }
}
