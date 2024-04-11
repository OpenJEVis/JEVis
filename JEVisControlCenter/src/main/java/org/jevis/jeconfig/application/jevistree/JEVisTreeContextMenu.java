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
package org.jevis.jeconfig.application.jevistree;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.export.TreeExporterDelux;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.ObjectHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.ImageConverter;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.dialog.KPIWizard;
import org.jevis.jeconfig.dialog.LocalNameDialog;
import org.jevis.jeconfig.dialog.ReportWizardDialog;
import org.jevis.jeconfig.plugin.object.extension.OPC.OPCBrowser;
import org.jevis.jeconfig.plugin.object.extension.revpi.RevPiAssistant;
import org.jevis.jeconfig.tool.AttributeCopy;
import org.jevis.jeconfig.tool.Calculations;
import org.jevis.jeconfig.tool.CleanDatas;
import org.jevis.jeconfig.tool.CreateAlarms;
import org.jevis.jeconfig.tool.dwdbrowser.DWDBrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeContextMenu extends ContextMenu {
    private static final Logger logger = LogManager.getLogger(JEVisTreeContextMenu.class);

    private JEVisObject obj;
    private JEVisTree tree;

    public JEVisTreeContextMenu() {
        super();
    }

    public static void goToSource(JEVisTree tree, JEVisObject obj) {
        try {
            AtomicBoolean foundTarget = new AtomicBoolean(false);
            JEVisDataSource ds = obj.getDataSource();


            if (tree.getCalculationIDs().get(obj.getID()) != null) {
                logger.info("target is a calculation");
                try {
                    JEVisObject calculationObject = ds.getObject(tree.getCalculationIDs().get(obj.getID()));
                    JEVisClass outputClass = ds.getJEVisClass("Output");

                    for (JEVisObject object : calculationObject.getChildren(outputClass, false)) {
                        try {
                            if (object.getAttribute("Output").hasSample()) {
                                TargetHelper targetHelper = new TargetHelper(ds, object.getAttribute("Output"));
                                if ((targetHelper.isObject() || targetHelper.isAttribute()) && targetHelper.getObject().get(0).getID().equals(obj.getID())) {
                                    logger.info("found target");
                                    foundTarget.set(true);

                                    List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                                    toOpen.add(object);
                                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                logger.info("target is not a calculation");
                try {


                    if (tree.getTargetAndChannel().get(obj.getID()) != null) {
                        JEVisObject object = ds.getObject(tree.getTargetAndChannel().get(obj.getID()));
                        logger.info("found target");
                        foundTarget.set(true);
                        List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                        toOpen.add(object);
                        TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!foundTarget.get()) {

                GridPane gridPane = new GridPane();
                gridPane.setPadding(new Insets(8));
                gridPane.setHgap(8);
                gridPane.setVgap(8);

                gridPane.add(new Label(I18n.getInstance().getString("jevistree.menu.gotosrc.error")), 0, 0);
                gridPane.add(new Separator(), 0, 1);

                Dialog dialog = new Dialog();
                dialog.setResizable(true);
                dialog.initOwner(JEConfig.getStage());
                dialog.initModality(Modality.APPLICATION_MODAL);
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                TopMenu.applyActiveTheme(stage.getScene());
                stage.setAlwaysOnTop(true);

                ButtonType okType = new ButtonType(I18n.getInstance().getString("jevistree.menu.gotosrc.close"), ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(okType);

                Button okButton = (Button) dialog.getDialogPane().lookupButton(okType);
                okButton.setDefaultButton(true);

                dialog.getDialogPane().setContent(gridPane);

                okButton.setOnAction(event -> {
                    dialog.close();
                });

                dialog.show();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void exportAction(JEVisTree tree) {
        TreeExporterDelux exportMaster = new TreeExporterDelux();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JEVis Export", "*.jex"));
        File file = fileChooser.showSaveDialog(JEConfig.getStage());

        if (file != null) {
            List<JEVisObject> objects = new ArrayList<>();
            tree.getSelectionModel().getSelectedItems().forEach(o -> {
                JEVisTreeItem jeVisTreeItem = (JEVisTreeItem) o;
                objects.add(jeVisTreeItem.getValue().getJEVisObject());
            });

            Task<Void> exportTask = exportMaster.exportToFileTask(file, objects);
            JEConfig.getStatusBar().addTask("Tree Exporter", exportTask, JEConfig.getImage("save.gif"), true);

        }
    }

    public static void importAction(JEVisObject obj) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JEVis File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JEVis Export", "*.jex"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                TreeExporterDelux exportMaster = new TreeExporterDelux();
                Task<Void> exportTask = exportMaster.importFromFile(selectedFile, obj);
                JEConfig.getStatusBar().addTask("Tree Importer", exportTask, JEConfig.getImage("save.gif"), true);
                //List<DimpexObject> objects = DimpEX.readFile(selectedFile);
                //DimpEX.importALL(obj.getDataSource(), objects, obj);
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        }
    }

    private JEVisObject getObject() {
        return ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
    }

    private MenuItem buildGoToSource() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.gotosrc"), JEConfig.getSVGImage(Icon.GO_TO_SOURCE, 20, 20));
        menu.setOnAction(t -> {
                    goToSource(tree, obj);
                }

        );
        return menu;
    }

    private MenuItem buildImportDWDData() {
        MenuItem menuItem = new MenuItem(I18n.getInstance().getString("jevistree.menu.importdwd"), JEConfig.getSVGImage(Icon.IMPORT, 20, 20));
        menuItem.setOnAction(actionEvent -> {
            try {
                DWDBrowser dwdBrowser = new DWDBrowser(obj.getDataSource(), obj);

                dwdBrowser.show();
            } catch (Exception e) {
                logger.error(e);
            }
        });
        return menuItem;
    }

    public void setTree(JEVisTree tree) {
        this.tree = tree;
        tree.setOnMouseClicked(event -> {
            try {
                obj = getObject();
                if (obj.getDeleteTS() != null) {
                    getItems().setAll(
                            buildDelete(true),
                            //buildCopy(), // need additional checks
                            buildCut(),
                            new SeparatorMenuItem(),
                            buildCopyFormat(),
                            buildParsedFormat(),
                            new SeparatorMenuItem(),
                            buildExport(),
                            buildImport());
                } else {
                    getItems().setAll(
                            buildNew2(),
                            buildReload(),
                            new SeparatorMenuItem(),
                            buildDelete(false),
                            //buildRename(),
                            buildMenuLocalize(),
                            buildCopy(),
                            buildCut(),
                            buildPaste(),
                            new SeparatorMenuItem(),
                            buildCopyFormat(),
                            buildParsedFormat(),
                            new SeparatorMenuItem(),
                            buildExport(),
                            buildImport()
                    );
                    logger.debug(obj.getJEVisClassName());

                    if (obj.getJEVisClassName().equals("Calculation")) {
                        getItems().add(new SeparatorMenuItem());
                        getItems().add(buildMenuAddInput());
                        getItems().add(buildRecalculate());
                    } else if (obj.getJEVisClassName().equals("Loytec XML-DL Server")) {
                        getItems().add(new SeparatorMenuItem());
                        getItems().add(buildOCP());
                    } else if (obj.getJEVisClassName().equals("Revolution PI Server")) {
                        getItems().add(new SeparatorMenuItem());
                        getItems().add(buildRevPi());
                    } else if (JEConfig.getExpert() && obj.getJEVisClassName().equals("Data Directory")) {
                        getItems().addAll(new SeparatorMenuItem(), buildKPIWizard());
                        getItems().add(buildCreateAlarms());
                    } else if (obj.getJEVisClassName().equals("Data") || obj.getJEVisClassName().equals("Base Data")) {
                        getItems().addAll(new SeparatorMenuItem(), buildGoToSource(), buildImportDWDData());
                        getItems().add(buildReCalcClean());
                    } else if (obj.getJEVisClassName().equals("String Data")) {
                        getItems().addAll(new SeparatorMenuItem(), buildGoToSource());
                    } else if (obj.getJEVisClassName().equals("Clean Data") || obj.getJEVisClassName().equals("Math Data")) {
                        getItems().add(new SeparatorMenuItem());
                        getItems().add(buildReCalcClean());
                    } else if (obj.getJEVisClassName().equals("Periodic Report")) {
                        getItems().add(buildReportWizard());
                    }

                    if (obj.getAttribute("Value") != null) {
                        getItems().add(buildManualSample());
                    }
                }


            } catch (Exception ex) {
                logger.fatal(ex);
            }

        });
    }

    private MenuItem buildReCalcClean() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.recalculate"), JEConfig.getSVGImage(Icon.CALCULATOR, 20, 20));

        menu.setOnAction(t -> {
            CleanDatas.createTask(tree);
        });
        return menu;
    }

    private MenuItem buildCreateAlarms() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.createalarms"), JEConfig.getSVGImage(Icon.ALARM, 20, 20));

        menu.setOnAction(t -> {
            CreateAlarms.createTask(tree);
        });
        return menu;
    }

    private MenuItem buildRecalculate() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.recalculate"), JEConfig.getSVGImage(Icon.CALCULATOR, 20, 20));

        menu.setOnAction(t -> {
                    Calculations.createCalcJobs(tree);
                }
        );
        return menu;
    }

    private MenuItem buildOCP() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.opc"), JEConfig.getSVGImage(Icon.WIZARD_HAT, 20, 20));

        menu.setOnAction(t -> {
                    OPCBrowser opcEditor = new OPCBrowser(obj);
                }
        );
        return menu;
    }

    private MenuItem buildRevPi() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.revpi.assistant"), JEConfig.getSVGImage(Icon.WIZARD_HAT, 20, 20));

        menu.setOnAction(t -> {
                    RevPiAssistant revPiAssistant = new RevPiAssistant(obj);
                }
        );
        return menu;
    }

    private MenuItem buildPaste() {
        //TODO: disable if not allowed
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.paste"), JEConfig.getSVGImage(Icon.PASTE, 20, 20));

        menu.setOnAction(t -> TreeHelper.EventDrop(tree, tree.getCopyObjects(), obj, CopyObjectDialog.DefaultAction.COPY)
        );
        return menu;
    }

    private MenuItem buildCopy() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.copy"), JEConfig.getSVGImage(Icon.PASTE, 20, 20));
        menu.setOnAction(t -> tree.setCopyObjectsBySelection(false)
        );
        return menu;
    }

    private MenuItem buildCut() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.cut"), JEConfig.getSVGImage(Icon.CUT, 20, 20));
        menu.setOnAction(t -> tree.setCopyObjectsBySelection(true)
        );
        return menu;
    }

    private MenuItem buildKPIWizard() {
        MenuItem menu = new MenuItem("KPI Wizard", JEConfig.getSVGImage(Icon.WIZARD_WAND, 20, 20));
        menu.setOnAction(t -> {
                    KPIWizard wizard = new KPIWizard(obj);
                    wizard.show();
                }
        );
        return menu;
    }

    private MenuItem buildExport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.export"), JEConfig.getSVGImage(Icon.EXPORT, 20, 20));
        menu.setOnAction(t -> {
                    exportAction(tree);
                }
        );
        return menu;
    }

    private MenuItem buildImport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.import"), JEConfig.getSVGImage(Icon.IMPORT, 20, 20));
        menu.setOnAction(event -> importAction(obj)
        );
        return menu;
    }

    public List<MenuItem> buildMenuNewContent() {

        logger.debug("buildMenuNewContent()");
        Object obj2 = this.getUserData();
        logger.debug("obj2: " + obj2);
        Object obj3 = this.getOwnerNode();
        logger.debug("obj3: " + obj3);

        List<MenuItem> newContent = new ArrayList<>();
        try {
            for (JEVisClass jlass : obj.getAllowedChildrenClasses()) {
                MenuItem classItem;

                classItem = new CheckMenuItem(jlass.getName(), getIcon(jlass));
                classItem.setOnAction(new EventHandler<ActionEvent>() {

                                          @Override
                                          public void handle(ActionEvent t) {
                                              TreeHelper.EventNew(tree, obj);
                                          }
                                      }
                );
                newContent.add(classItem);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        return newContent;
    }

    public MenuItem buildMenuAddInput() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.addinput"), JEConfig.getSVGImage(Icon.EXPORT, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    TreeHelper.createCalcInput(obj, null, null, null);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        });

        return menu;
    }

    public MenuItem buildMenuLocalize() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.localename"), JEConfig.getSVGImage(Icon.TRANSLATE, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    LocalNameDialog localNameDialog = new LocalNameDialog(obj);
                    localNameDialog.show();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            }
        });

        return menu;
    }

    public MenuItem buildManualSample() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("menu.file.import.manual"), JEConfig.getSVGImage(Icon.MANUAL_DATA_ENTRY, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    JEVisSample lastValue = obj.getAttribute("Value").getLatestSample();
                    EnterDataDialog enterDataDialog = new EnterDataDialog(obj.getDataSource());
                    enterDataDialog.setTarget(false, obj.getAttribute("Value"));
                    enterDataDialog.setSample(lastValue);
                    enterDataDialog.setShowValuePrompt(true);

                    enterDataDialog.show();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            }
        });

        return menu;
    }


    public MenuItem buildMenuExport() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.export"), JEConfig.getSVGImage(Icon.EXPORT, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Platform.runLater(() -> {
                    try {
                        TreeHelper.EventExportTree(obj);
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                });


            }
        });

        return menu;
    }


    private Menu buildMenuNew() {
        Menu addMenu = new Menu(I18n.getInstance().getString("jevistree.menu.new"), JEConfig.getSVGImage(Icon.TABLE_PLUS, 20, 20));
        addMenu.getItems().addAll(buildMenuNewContent());

        return addMenu;

    }

    private ImageView getIcon(JEVisClass jclass) {
        try {
            return ImageConverter.convertToImageView(jclass.getIcon(), 20, 20);
        } catch (Exception ex) {
            return ResourceLoader.getImage("1393615831_unknown2.png", 20, 20);
        }
    }

    private MenuItem buildProperties() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.expand")
        );//shoud be edit but i use it for expand for the time
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                PopOver popup = new PopOver(new HBox());
//                popup.show(_item.getGraphic(), 200d, 200d, Duration.seconds(1));
                //TMP test

//                logger.info("expand all");
//                _item.expandAll(true);
            }
        });
        return menu;
    }

    private MenuItem buildNew2() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.new"), JEConfig.getSVGImage(Icon.PLUS_CIRCLE, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                Object obj2 = getUserData();
                logger.debug("userdate: " + obj2);
                TreeHelper.EventNew(tree, obj);

            }
        });
        return menu;
    }

    /**
     * private MenuItem buildRename() {
     * MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.rename"), ResourceLoader.getImage("Rename.png", 20, 20));
     * menu.setOnAction(new EventHandler<ActionEvent>() {
     *
     * @Override public void handle(ActionEvent t) {
     * TreeHelper.EventRename(tree, obj);
     * }
     * });
     * return menu;
     * }
     **/

    private MenuItem buildReload() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.reload"), JEConfig.getSVGImage(Icon.REFRESH, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                TreeHelper.EventReload(obj, ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()));
            }
        });
        return menu;
    }

    private MenuItem buildDelete(boolean deleteForever) {
        MenuItem menu;
        if (deleteForever) {
            menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.deleteforever"), JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 20, 20));
        } else {
            menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.delete"), JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 20, 20));
        }
        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TreeHelper.EventDelete(tree, deleteForever);
            }
        });
        return menu;
    }

    private MenuItem buildReportWizard() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("plugin.object.report.dialog.wizard"), JEConfig.getSVGImage(Icon.WIZARD_HAT, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                ReportWizardDialog reportWizardDialog = new ReportWizardDialog(obj, ReportWizardDialog.UPDATE);
            }
        });
        return menu;
    }


    private MenuItem buildCopyFormat() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("jevistree.menu.copyformat"), JEConfig.getSVGImage(Icon.COPY_PROPERTIES, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                AttributeCopy attributeCopy = new AttributeCopy();
                attributeCopy.showAttributeSelection(obj);
                tree.setConfigObject(AttributeCopy.CONFIG_NAME, attributeCopy.getSelectedAttributes());

            }
        });
        return menu;
    }

    private MenuItem buildParsedFormat() {
        MenuItem menu = new MenuItem(I18n.getInstance().getString("dialog.attributecopy.paste"), JEConfig.getSVGImage(Icon.PASTE_PROPERTIES, 20, 20));
        menu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                AttributeCopy attributeCopy = new AttributeCopy();
                List<JEVisAttribute> jeVisAttributes = (List<JEVisAttribute>) tree.getConfigObject(AttributeCopy.CONFIG_NAME);
                attributeCopy.startPaste(jeVisAttributes, tree.getSelectionModel().getSelectedItems());

            }
        });
        return menu;
    }

}
