/**
 * Copyright (C) 2009 - 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessManager;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.*;
import org.jevis.jeconfig.application.jevistree.*;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.bulkedit.CreateTable;
import org.jevis.jeconfig.bulkedit.EditTable;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.tool.AttributeCopy;
import org.jevis.jeconfig.tool.CleanDatas;
import org.jevis.jeconfig.tool.LoadingPane;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;


/**
 * This JEConfig plugin allows the user con work with the Objects in the JEVis
 * System.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(ObjectPlugin.class);
    public static String PLUGIN_NAME = "Configuration Plugin";
    private final StringProperty name = new SimpleStringProperty("*NO_NAME*");
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private final BorderPane viewPane = new BorderPane();
    private final LoadingPane editorLoadingPane = new LoadingPane();
    private final LoadingPane treeLoadingPane = new LoadingPane();
    private final ToolBar toolBar = new ToolBar();
    private final ObjectEditor _editor = new ObjectEditor();
    private final SimpleBooleanProperty loadingObjectProperty = new SimpleBooleanProperty();
    private final String tooltip = I18n.getInstance().getString("pluginmanager.object.tooltip");
    private JEVisDataSource ds;
    //    private ObjectTree tf;
//    private ObjectTree tree;
    private JEVisTree tree;
    private boolean initToolbar = false;

    public ObjectPlugin(JEVisDataSource ds, String newname) {
        this.ds = ds;
        name.set(newname);
    }

    @Override
    public String getClassName() {
        return "Configuration Plugin";
    }

    @Override
    public void setHasFocus() {
        try {

            if (tree.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> {
                    try {
                        /** disabled for now, nils doesnt like it if the analysis folder is open every time **/
//                        tree.getSelectionModel().getModelItem(0).expandedProperty().setValue(Boolean.TRUE);
                        tree.getSelectionModel().selectFirst();
                    } catch (Exception ex) {
                    }
                });
            }

            Platform.runLater(() -> {
                tree.requestFocus();
            });

        } catch (Exception np) {
            logger.error("Empty tree can focus first object", np);
        }
//        }

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {
        try {
//            tree.getSearchFilterBar().showObject((JEVisObject) object);


            tree.getSelectionModel().clearSelection();
            JEVisTreeItem item = tree.getItemForObject((JEVisObject) object);

            Platform.runLater(() -> {
                item.setExpanded(true);

                tree.openPathToObject((JEVisObject) object);
                tree.getSelectionModel().select(item);

            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getPrefTapPos() {
        return 10;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String value) {
        name.set(value);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String newid) {
        id.set(newid);
    }

    @Override
    public String getToolTip() {
        return tooltip;
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getContentNode() {
        if (viewPane.getChildren().isEmpty()) {

            initGUI();

        }

        return viewPane;
    }

    public void initGUI() {
        tree = JEVisTreeFactory.buildBasicDefault(ds, true);
        tree.setId("objecttree");
//            tree.getStylesheets().add("/styles/Styles.css");
//        tree.setStyle("-fx-background-color: #E2E2E2;");
        //tree.getSelectionModel().selectFirst();

        VBox left = new VBox();
        left.setPrefWidth(460);
        left.getStyleClass().add("object-plugin-left");
//        left.setStyle("-fx-background-color: #E2E2E2;");

        VBox.setVgrow(tree, Priority.ALWAYS);

        List<JEVisTreeFilter> allObjects = new ArrayList<>();

        allObjects.add(SelectTargetDialog.buildCalendarFilter());
        allObjects.add(SelectTargetDialog.buildAllDataSources(this.ds));
        allObjects.add(SelectTargetDialog.buildAllMeasurement(this.ds));
        allObjects.add(SelectTargetDialog.buildAllCalculation(this.ds));
        allObjects.add(SelectTargetDialog.buildAllAnalyses(this.ds));
        allObjects.add(SelectTargetDialog.buildAllDocuments(this.ds));
        allObjects.add(SelectTargetDialog.buildAllAlarms(this.ds));
        allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "User"));
        allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "Group"));
        allObjects.add(SelectTargetDialog.buildAllReports(this.ds));

        AlphanumComparator ac = new AlphanumComparator();
        allObjects.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        allObjects.add(0, SelectTargetDialog.buildAllAttributesFilter());
        allObjects.add(0, SelectTargetDialog.buildAllDataAndCleanDataFilter());
        allObjects.add(0, SelectTargetDialog.buildAllObjects());

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, allObjects, finder);
        tree.setSearchFilterBar(searchBar);

        final KeyCombination replaceCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        tree.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            if (replaceCombination.match(t)) {

                searchBar.enableReplaceMode();

                tree.getSearchFilterBar().requestCursor();
                t.consume();
            }
        });

        treeLoadingPane.setContent(left);
        editorLoadingPane.setContent(_editor.getView());
        left.getChildren().addAll(tree, searchBar);

        SplitPane sp = new SplitPane();
        sp.setDividerPositions(.3d);
        sp.setOrientation(Orientation.HORIZONTAL);
        sp.getStyleClass().add("main-split-pane");
//        sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
//            sp.getItems().setAll(left, tree.getEditor().getView());
        sp.getItems().setAll(treeLoadingPane, editorLoadingPane);

        treeLoadingPane.endLoading();
        editorLoadingPane.endLoading();

        viewPane.setCenter(sp);
        viewPane.getStyleClass().add("main-view-pane");
//        viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        //public void changed(ObservableValue<? extends TreeItem<JEVisObject>> ov, TreeItem<JEVisObject> t, TreeItem<JEVisObject> t1) {
        //TreeItem<JEVisTreeRow>

        tree.getSelectionModel().selectedItemProperty().addListener(this::changed);
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {

        if (!initToolbar) {
            toolBar.setId("ObjectPlugin.Toolbar");
            double iconSize = 20;
            ToggleButton newB = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, newB, Constants.Plugin.Command.NEW);

            ToggleButton save = new ToggleButton("", JEConfig.getSVGImage(Icon.SAVE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, save, Constants.Plugin.Command.SAVE);

            ToggleButton delete = new ToggleButton("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, delete, Constants.Plugin.Command.DELETE);

            ToggleButton reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, reload, Constants.Plugin.Command.RELOAD);

            ToggleButton addTable = new ToggleButton("", JEConfig.getSVGImage(Icon.TABLE_PLUS, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(addTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, addTable, Constants.Plugin.Command.ADD_TABLE);

            ToggleButton editTable = new ToggleButton("", JEConfig.getImage("edit_table.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(editTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, editTable, Constants.Plugin.Command.EDIT_TABLE);

            ToggleButton createWizard = new ToggleButton("", JEConfig.getSVGImage(Icon.WIZARD_WAND, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(createWizard);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, createWizard, Constants.Plugin.Command.CREATE_WIZARD);


            ToggleButton collapseTree = new ToggleButton("", JEConfig.getSVGImage(Icon.TREE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(collapseTree);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, collapseTree, Constants.Plugin.Command.COLLAPSE);


            // Eigenschaften Kopieren
            // Eigenschaften übertragen
            // Exportieren
            // Importieren

            ToggleButton copyItem = new ToggleButton("", JEConfig.getSVGImage(Icon.COPY, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(copyItem);
            copyItem.setOnAction(event -> {
                tree.setCopyObjectsBySelection(false);
            });

            ToggleButton pasteItem = new ToggleButton("", JEConfig.getSVGImage(Icon.PASTE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(pasteItem);
            pasteItem.setOnAction(event -> {
                JEVisObject jeVisObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                if (tree.getCopyObject() != null) {
                    if (tree.isCut()) {
                        TreeHelper.EventDrop(tree, tree.getCopyObjects(), jeVisObject, CopyObjectDialog.DefaultAction.MOVE);
                    } else {
                        TreeHelper.EventDrop(tree, tree.getCopyObjects(), jeVisObject, CopyObjectDialog.DefaultAction.COPY);
                    }
                }
            });

            ToggleButton cutItem = new ToggleButton("", JEConfig.getSVGImage(Icon.CUT, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(cutItem);
            cutItem.setOnAction(event -> {
                tree.setCopyObjectsBySelection(true);
            });

            ToggleButton copyAttributeItem = new ToggleButton("", JEConfig.getSVGImage(Icon.COPY_PROPERTIES, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(copyAttributeItem);
            copyAttributeItem.setOnAction(event -> {
                JEVisObject jeVisObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                AttributeCopy attributeCopy = new AttributeCopy();
                attributeCopy.showAttributeSelection(jeVisObject);
                tree.setConfigObject(AttributeCopy.CONFIG_NAME, attributeCopy.getSelectedAttributes());

            });
            ToggleButton pasteAttributeItem = new ToggleButton("", JEConfig.getSVGImage(Icon.PASTE_PROPERTIES, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(pasteAttributeItem);
            pasteAttributeItem.setOnAction(event -> {
                AttributeCopy attributeCopy = new AttributeCopy();
                List<JEVisAttribute> jeVisAttributes = (List<JEVisAttribute>) tree.getConfigObject(AttributeCopy.CONFIG_NAME);
                attributeCopy.startPaste(jeVisAttributes, tree.getSelectionModel().getSelectedItems());

            });
            ToggleButton wizardItem = new ToggleButton("", JEConfig.getSVGImage(Icon.WIZARD_HAT, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(wizardItem);
            wizardItem.setOnAction(event -> {
                JEVisObject jeVisObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                KPIWizard wizard = new KPIWizard(jeVisObject);
                wizard.show();

            });

            ToggleButton reCalcItem = new ToggleButton("", JEConfig.getSVGImage(Icon.CALCULATOR, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(reCalcItem);
            reCalcItem.setOnAction(event -> {
                /* should work to simply call both because they are type same */
                CleanDatas.createTask(tree);
                CleanDatas.createTask(tree);
            });

            ToggleButton exportItem = new ToggleButton("", JEConfig.getSVGImage(Icon.EXPORT, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportItem);
            exportItem.setOnAction(event -> {
                JEVisTreeContextMenu.exportAction(tree);
            });

            ToggleButton importItem = new ToggleButton("", JEConfig.getSVGImage(Icon.IMPORT, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(importItem);
            importItem.setOnAction(event -> {
                JEVisObject jeVisObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                JEVisTreeContextMenu.importAction(jeVisObject);
            });

            ToggleButton toSourceItem = new ToggleButton("", JEConfig.getSVGImage(Icon.GO_TO_SOURCE, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(toSourceItem);
            toSourceItem.setOnAction(event -> {
                JEVisObject jeVisObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                JEVisTreeContextMenu.goToSource(tree, jeVisObject);
            });


            ToggleButton manualSampleItem = new ToggleButton("", JEConfig.getSVGImage(Icon.MANUAL_DATA_ENTRY, iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(manualSampleItem);
            manualSampleItem.setOnAction(event -> {
                try {
                    JEVisObject obj = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
                    JEVisSample lastValue = obj.getAttribute("Value").getLatestSample();
                    EnterDataDialog enterDataDialog = new EnterDataDialog(obj.getDataSource());
                    enterDataDialog.setTarget(false, obj.getAttribute("Value"));
                    enterDataDialog.setSample(lastValue);
                    enterDataDialog.setShowValuePrompt(true);
                    enterDataDialog.show();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            });

            reCalcItem.disableProperty().bind(tree.getItemActionController().recalcEnabledPropertyProperty().not());
            wizardItem.disableProperty().bind(tree.getItemActionController().kpiWizardEnabledPropertyProperty().not());
            toSourceItem.disableProperty().bind(tree.getItemActionController().gotoSourceEnabledPropertyProperty().not());
            manualSampleItem.disableProperty().bind(tree.getItemActionController().manualValueEnabledPropertyProperty().not());
            newB.disableProperty().bind(tree.getItemActionController().createEnabledPropertyProperty().not());
            delete.disableProperty().bind(tree.getItemActionController().deleteEnabledPropertyProperty().not());

            final BooleanProperty toggleProperty = new SimpleBooleanProperty(false);
            collapseTree.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    toggleProperty.setValue(!toggleProperty.getValue());
                    if (toggleProperty.getValue()) {
                        handleRequest(Constants.Plugin.Command.COLLAPSE);
                    } else {
                        handleRequest(Constants.Plugin.Command.EXPAND);
                    }
                }
            });

            ToggleButton expandTree = new ToggleButton("", JEConfig.getImage("create_wizard.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(expandTree);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, expandTree, Constants.Plugin.Command.EXPAND);

            ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
            ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
            infoButton.setOnAction(event -> _editor.toggleHelp());

            save.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.save")));
            newB.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.new")));
            delete.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.delete")));
            reload.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.reload")));
            collapseTree.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.collapse")));
            copyItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.copy")));
            cutItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.cut")));
            pasteItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.paste")));
            copyAttributeItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.copyatttibute")));
            pasteAttributeItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.pasteattributes")));
            wizardItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.wizard")));
            reCalcItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.recalc")));
            manualSampleItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.manualsample")));
            exportItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.export")));
            importItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.toolbar.import")));
            toSourceItem.setTooltip(new Tooltip(I18n.getInstance().getString("jevistree.menu.gotosrc")));


            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            Separator sep3 = new Separator();
            Separator sep4 = new Separator();

            //JEVisHelp.getInstance().addHelpControl(ObjectPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, save, newB, delete, reload, collapseTree, sep1, helpButton);
            toolBar.getItems().setAll(save, newB, delete, reload, collapseTree,
                    sep1, copyItem, cutItem, pasteItem,
                    sep2, copyAttributeItem, pasteAttributeItem,
                    sep3, wizardItem, reCalcItem, manualSampleItem, toSourceItem,
                    sep4, exportItem, importItem);
            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
            JEVisHelp.getInstance().addHelpItems(ObjectPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());

            initToolbar = true;
        }

        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    private void eventSaveAttributes() {
        _editor.commitAll();
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
            case Constants.Plugin.Command.DELETE:
            case Constants.Plugin.Command.EXPAND:
            case Constants.Plugin.Command.NEW:
            case Constants.Plugin.Command.RELOAD:
            case Constants.Plugin.Command.ADD_TABLE:
            case Constants.Plugin.Command.EDIT_TABLE:
            case Constants.Plugin.Command.CREATE_WIZARD:
            case Constants.Plugin.Command.FIND_OBJECT:
            case Constants.Plugin.Command.PASTE:
            case Constants.Plugin.Command.COPY:
            case Constants.Plugin.Command.CUT:
            case Constants.Plugin.Command.CREATE_MULTIPLIER_AND_DIFFERENTIAL:
            case Constants.Plugin.Command.DELETE_ALL_CLEAN_AND_RAW:
            case Constants.Plugin.Command.DISABLE_ALL:
            case Constants.Plugin.Command.ENABLE_ALL:
            case Constants.Plugin.Command.REPLACE:
            case Constants.Plugin.Command.RESET_CALCULATION:
            case Constants.Plugin.Command.DELETE_DEPENDENCIES:
            case Constants.Plugin.Command.SET_LIMITS:
            case Constants.Plugin.Command.SET_SUBSTITUTION_SETTINGS:
            case Constants.Plugin.Command.SET_UNITS_AND_PERIODS:
                return true;
            default:
                return false;
        }
    }

    private void saveWithAnimation() {
        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

        Task<Void> upload = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                eventSaveAttributes();
                Thread.sleep(60);
                return null;
            }
        };
        upload.setOnSucceeded(event -> pForm.getDialogStage().close());

        upload.setOnCancelled(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
            pForm.getDialogStage().hide();
        });

        upload.setOnFailed(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
            pForm.getDialogStage().hide();
        });

        pForm.activateProgressBar(upload);
        pForm.getDialogStage().show();

        new Thread(upload).start();
    }

    @Override
    public void handleRequest(int cmdType) {
        try {
            logger.error("handleRequest: " + cmdType);
            final TreeItem<JEVisTreeRow> selectedObj = (TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem();
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
//                    eventSaveAttributes();
                    saveWithAnimation();
                    break;
                case Constants.Plugin.Command.DELETE:
                    TreeHelper.EventDelete(tree, false);
                    break;
                case Constants.Plugin.Command.RENAME:
                    LocalNameDialog localNameDialog = new LocalNameDialog(selectedObj.getValue().getJEVisObject());
                    localNameDialog.show();
                    break;
                case Constants.Plugin.Command.COLLAPSE:
                    tree.getSelectionModel().getSelectedItems().forEach(o -> {
                        TreeItem item = (TreeItem) o;
//                        tree.collapseAll(item, true);
                        tree.toggleItemCollapse(item);
                    });

                    break;
                case Constants.Plugin.Command.EXPAND:
                    tree.getSelectionModel().getSelectedItems().forEach(o -> {
                        TreeItem item = (TreeItem) o;
//                        tree.collapseAll(item, false);
                        tree.toggleItemCollapse(item);
                    });
                    break;
                case Constants.Plugin.Command.NEW:
                    TreeHelper.EventNew(tree, selectedObj.getValue().getJEVisObject());
                    break;
                case Constants.Plugin.Command.RELOAD:
                    ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("graph.selection.load"));

                    Task<Void> load = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            tree = null;
                            ds.clearCache();
                            ds.preload();
                            Platform.runLater(() -> {
                                initGUI();
                                if (items.size() == 1) {
                                    JEVisObject findObj = items.get(0).getValue().getJEVisObject();
                                    List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);
                                }
                            });
                            return null;
                        }
                    };
                    load.setOnSucceeded(event -> pForm.getDialogStage().close());

                    load.setOnCancelled(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitload.canceled"));
                        pForm.getDialogStage().hide();
                    });

                    load.setOnFailed(event -> {
                        logger.error(I18n.getInstance().getString("plugin.object.waitload.failed"));
                        pForm.getDialogStage().hide();
                    });

                    pForm.activateProgressBar(load);
                    pForm.getDialogStage().show();

                    new Thread(load).start();

                    break;
                case Constants.Plugin.Command.ADD_TABLE:
                    fireEventCreateTable(((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject());
                    break;
                case Constants.Plugin.Command.EDIT_TABLE:
                    fireEventEditTable(((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject());
                    break;
                case Constants.Plugin.Command.CREATE_WIZARD:
                    //TODO: need changes to work with the new JEVisTree
//                    tree.fireEventCreateWizard(tree.getSelectedObject());
                    break;
                case Constants.Plugin.Command.FIND_OBJECT:
                    tree.getSearchFilterBar().requestCursor();
                    break;
                case Constants.Plugin.Command.FIND_AGAIN:
                    if (tree.getSearchFilterBar() != null) {
                        tree.getSearchFilterBar().goNext();
                    }
                    break;
                case Constants.Plugin.Command.PASTE:
                    if (tree.getCopyObject() != null) {
                        if (tree.isCut()) {
                            TreeHelper.EventDrop(tree, tree.getCopyObjects(), ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.MOVE);
                        } else {
                            TreeHelper.EventDrop(tree, tree.getCopyObjects(), ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                        }
                    }
                    break;
                case Constants.Plugin.Command.COPY:
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), false);
                    break;
                case Constants.Plugin.Command.CUT:
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), true);
                    break;
                case Constants.Plugin.Command.CREATE_MULTIPLIER_AND_DIFFERENTIAL:
                    TreeHelper.EventCreateMultiplierAndDifferential(tree);
                    break;
                case Constants.Plugin.Command.DELETE_ALL_CLEAN_AND_RAW:
                    TreeHelper.EventDeleteAllCleanAndRaw(tree);
                    break;
                case Constants.Plugin.Command.DISABLE_ALL:
                    TreeHelper.EventSetEnableAll(tree, false);
                    break;
                case Constants.Plugin.Command.ENABLE_ALL:
                    TreeHelper.EventSetEnableAll(tree, true);
                    break;
                case Constants.Plugin.Command.REPLACE:
                    break;
                case Constants.Plugin.Command.RESET_CALCULATION:
                    TreeHelper.EventDeleteAllCalculations(tree);
                    break;
                case Constants.Plugin.Command.DELETE_DEPENDENCIES:
                    TreeHelper.EventDeleteAllDependencies(tree);
                    break;
                case Constants.Plugin.Command.SET_LIMITS:
                    TreeHelper.EventSetLimitsRecursive(tree);
                    break;
                case Constants.Plugin.Command.SET_SUBSTITUTION_SETTINGS:
                    TreeHelper.EventSetSubstitutionSettingsRecursive(tree);
                    break;
                case Constants.Plugin.Command.SET_UNITS_AND_PERIODS:
                    TreeHelper.EventSetUnitAndPeriodRecursive(tree);
                    break;
                case 999:
                    ProcessManager processManager = new ProcessManager(
                            selectedObj.getValue().getJEVisObject(),
                            new ObjectHandler(selectedObj.getValue().getJEVisObject().getDataSource()),
                            100000);
                    processManager.start();
                    break;
                default:
                    logger.info("Unknown command ignore...");
            }
        } catch (Exception ex) {
            logger.error("Error running command: {}", cmdType, ex);
            JEConfig.showError("Unexpected Error", ex);

        }

    }

    @Override
    public void fireCloseEvent() {
//        try {
//            tree.fireSaveAttributes(true);
//        } catch (JEVisException ex) {
//            Logger.getLogger(ObjectPlugin2.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void Save() {
//        try {
//            tree.fireSaveAttributes(false);
//        } catch (JEVisException ex) {
//            Logger.getLogger(ObjectPlugin2.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.CONFIG, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    //@AITBilal - Edit a new Table!
    public void fireEventEditTable(final JEVisObject parent) throws JEVisException {
        EditTable table = new EditTable();
        if (parent != null) {
            if (table.show(JEConfig.getStage(), null, parent, false, EditTable.Type.EDIT, null) == EditTable.Response.YES) {
                for (int i = 0; i < table.getListChildren().size(); i++) {
                    JEVisObject childObject = null;

                    if (table.getSelectedClass().getName().equals("Data")) {
                        childObject = table.getListChildren().get(i);
//                      childObject.commit();
                        List<JEVisAttribute> attributes = childObject.getAttributes();
                        int counter = 6;
                        for (JEVisAttribute attribute : attributes) {
                            if (attribute.getName().equals("Value")) {

                                //get objekt mit ID from Tabelle
                                if (table.getPairList().get(i).getValue().get(0).isEmpty() && table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                    attribute.setDisplayUnit(new JEVisUnitImp("", "", null));
                                } else {
                                    String displaySymbol = table.getPairList().get(i).getValue().get(1);
                                    if (table.getPairList().get(i).getValue().get(0).isEmpty() && !table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                        attribute.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", null));
                                    } else {
                                        String prefixDisplayUnit = table.getPairList().get(i).getValue().get(0);
                                        attribute.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                    }
                                }

                                if (table.getPairList().get(i).getValue().get(3).isEmpty() && table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                    attribute.setInputUnit(new JEVisUnitImp("", "", null));
                                } else {
                                    String inputSymbol = table.getPairList().get(i).getValue().get(4);
                                    if (table.getPairList().get(i).getValue().get(3).isEmpty() && !table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                        attribute.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", null));
                                    } else {
                                        String prefixInputUnit = table.getPairList().get(i).getValue().get(3);
                                        attribute.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", prefixInputUnit));
                                    }
                                }

                                if (table.getPairList().get(i).getValue().get(2).isEmpty()) {
                                    attribute.setDisplaySampleRate(Period.parse("PT0S"));//Period.ZERO
                                } else {
                                    String displaySampleRate = table.getPairList().get(i).getValue().get(2);
                                    attribute.setDisplaySampleRate(Period.parse(displaySampleRate));
                                }

                                if (table.getPairList().get(i).getValue().get(5).isEmpty()) {
                                    attribute.setInputSampleRate(Period.parse("PT0S"));//Period.ZERO
                                } else {
                                    String inputSampleRate = table.getPairList().get(i).getValue().get(5);
                                    attribute.setInputSampleRate(Period.parse(inputSampleRate));
                                }

                                attribute.commit();
                            } else {
                                attribute.buildSample(new DateTime(), table.getPairList().get(i).getValue().get(counter)).commit();
                                counter++;
                            }
                        }

                    } else {
                        childObject = table.getListChildren().get(i);

//                      childObject.commit();
                        List<JEVisAttribute> attributes = childObject.getAttributes();

                        for (int j = 0; j < attributes.size(); j++) {
                            if (attributes.get(j).getLatestSample() == null) {
                                attributes.get(j).buildSample(new DateTime(), table.getPairList().get(i).getValue().get(j)).commit();
                            } else if (!attributes.get(j).getLatestSample().getValueAsString().equals(table.getPairList().get(i).getValue().get(j))) {
                                attributes.get(j).buildSample(new DateTime(), table.getPairList().get(i).getValue().get(j)).commit();
                            }
                        }
                    }
                }

            }
        }
    }

    //@AITBilal - Create a new Wizard-Table!
    //parent kommt von --> tree.fireEventCreateWizard(tree.getSelectedObject());
    public void fireEventCreateWizard(final JEVisObject parent) {
        //TODO:

//        WizardMain wizardmain = new WizardMain(parent, this);
//        if (parent != null) {
//            // parent.getJEVisClass().getName().equals("Monitored Object Directory")
//            if (parent.getName().equals("Monitored Object Directory")) {
//                wizardmain.showAndWait();
//            } else {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Information Dialog");
//                alert.setHeaderText(null);
//                alert.setContentText("This is not a Monitored Object Directory!");
//                alert.showAndWait();
//            }
//        }
    }

    //@AITBilal - Create a new Table!
    public void fireEventCreateTable(final JEVisObject parent) {
        CreateTable table = new CreateTable();
        if (parent != null) {
            if (table.show(JEConfig.getStage(), null, parent, false, CreateTable.Type.NEW, null) == CreateTable.Response.YES) {
                for (int i = 0; i < table.getPairList().size(); i++) {
                    JEVisObject newObject = null;
                    try {
                        if (table.getCreateClass().getName().equals("Data")) {
                            String objectName = table.getPairList().get(i).getKey();
                            newObject = parent.buildObject(objectName, table.getCreateClass());
                            newObject.commit();

                            List<JEVisAttribute> attributes = newObject.getAttributes();
                            //Counter ist für die attribute. Es anfangt ab Spalte "Input Sample Rate" zu zahlen.
                            int counter = 6;
                            for (JEVisAttribute attribute : attributes) {
                                if (attribute.getName().equals("Value")) {

                                    if (table.getPairList().get(i).getValue().get(0).isEmpty() && table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                        attribute.setDisplayUnit(new JEVisUnitImp("", "", null));
                                    } else {
                                        String displaySymbol = table.getPairList().get(i).getValue().get(1);
                                        if (table.getPairList().get(i).getValue().get(0).isEmpty() && !table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                            attribute.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", null));
                                        } else {
                                            String prefixDisplayUnit = table.getPairList().get(i).getValue().get(0);
                                            attribute.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                        }
                                    }

                                    if (table.getPairList().get(i).getValue().get(3).isEmpty() && table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                        attribute.setInputUnit(new JEVisUnitImp("", "", null));
                                    } else {
                                        String inputSymbol = table.getPairList().get(i).getValue().get(4);
                                        if (table.getPairList().get(i).getValue().get(3).isEmpty() && !table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                            attribute.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", null));
                                        } else {
                                            String prefixInputUnit = table.getPairList().get(i).getValue().get(3);
                                            attribute.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", prefixInputUnit));
                                        }
                                    }

                                    if (table.getPairList().get(i).getValue().get(2).isEmpty()) {
                                        attribute.setDisplaySampleRate(Period.parse("PT0S"));//Period.ZERO
                                    } else {
                                        String displaySampleRate = table.getPairList().get(i).getValue().get(2);
                                        attribute.setDisplaySampleRate(Period.parse(displaySampleRate));
                                    }

                                    if (table.getPairList().get(i).getValue().get(5).isEmpty()) {
                                        attribute.setInputSampleRate(Period.parse("PT0S"));//Period.ZERO
                                    } else {
                                        String inputSampleRate = table.getPairList().get(i).getValue().get(5);
                                        attribute.setInputSampleRate(Period.parse(inputSampleRate));
                                    }
                                    attribute.commit();
                                } else {
                                    attribute.buildSample(new DateTime(), table.getPairList().get(i).getValue().get(counter)).commit();
                                    counter++;
                                }
                            }

                        } else {
                            String objectName = table.getPairList().get(i).getKey();
                            newObject = parent.buildObject(objectName, table.getCreateClass());
                            newObject.commit();

                            List<JEVisAttribute> attributes = newObject.getAttributes();
                            for (int j = 0; j < attributes.size(); j++) {
                                attributes.get(j).buildSample(new DateTime(), table.getPairList().get(i).getValue().get(j)).commit();
                            }
                        }
//
//                        final TreeItem<JEVisObject> newTreeItem = buildItem(newObject);
//                        TreeItem<JEVisObject> parentItem = getObjectTreeItem(parent);
//                        parentItem.getChildren().add(newTreeItem);

//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                getSelectionModel().select(newTreeItem);
//                            }
//                        });
                    } catch (JEVisException ex) {
                        logger.error(ex);

                        if (ex.getMessage().equals("Can not create User with this name. The User has to be unique on the System")) {
                            InfoDialog info = new InfoDialog();
                            info.show("Waring", "Could not create user", "Could not create new user because this user exists already.");

                        } else {
                            ExceptionDialog errorDia = new ExceptionDialog();
                            errorDia.show("Error", "Could not create user", "Could not create new user.", ex, null);

                        }
                    }
                }
            }
        }
    }

    private void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (newValue instanceof JEVisTreeItem) {
            JEVisTreeItem item = (JEVisTreeItem) newValue;
            JEVisObject obj = item.getValue().getJEVisObject();

            loadingObjectProperty.setValue(true);

            //new
            if (_editor.needSave()) {
                ConfirmDialog dia = new ConfirmDialog();
                ConfirmDialog.Response re = dia.show(I18n.getInstance().getString("plugin.object.attributes.save"),
                        I18n.getInstance().getString("plugin.object.attributes.changes"), I18n.getInstance().getString("plugin.object.attributes.message"));
                if (re == ConfirmDialog.Response.YES) {
                    _editor.commitAll();
                } else {
                    _editor.dismissChanges();
                }
            }
            //new end
            try {
                _editor.setTree(tree);
                if (obj.getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
                    logger.debug("changed: object is a link so im loading the linked object");
                    _editor.setObject(obj.getLinkedObject());
                } else {
                    _editor.setObject(obj);
                }
            } catch (Exception ex) {
                logger.error("Error while selecting Object: {}\n{}", obj, ex);
            }
            loadingObjectProperty.setValue(false);
        }
    }
}
