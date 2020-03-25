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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.jevistree.*;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.*;
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
    private StringProperty name = new SimpleStringProperty("*NO_NAME*");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane viewPane;
    //    private ObjectTree tf;
//    private ObjectTree tree;
    private JEVisTree tree;
    private LoadingPane editorLoadingPane = new LoadingPane();
    private LoadingPane treeLoadingPane = new LoadingPane();
    private final ToolBar toolBar = new ToolBar();
    private ObjectEditor _editor = new ObjectEditor();
    private SimpleBooleanProperty loadingObjectProperty = new SimpleBooleanProperty();
    private String tooltip = I18n.getInstance().getString("pluginmanager.object.tooltip");
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
        return 5;
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
        if (viewPane == null) {

            initGUI();

        }

        return viewPane;
    }

    public void initGUI() {
        tree = JEVisTreeFactory.buildBasicDefault(ds, true);
        tree.setId("objecttree");
//            tree.getStylesheets().add("/styles/Styles.css");
        tree.setStyle("-fx-background-color: #E2E2E2;");
        //tree.getSelectionModel().selectFirst();

        VBox left = new VBox();
        left.setPrefWidth(460);
        left.setStyle("-fx-background-color: #E2E2E2;");

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
        sp.setId("mainsplitpane");
        sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
//            sp.getItems().setAll(left, tree.getEditor().getView());
        sp.getItems().setAll(treeLoadingPane, editorLoadingPane);

        treeLoadingPane.endLoading();
        editorLoadingPane.endLoading();

        viewPane = new BorderPane();
        viewPane.setCenter(sp);
        viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

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
            ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, newB, Constants.Plugin.Command.NEW);

            ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, save, Constants.Plugin.Command.SAVE);

            ToggleButton delete = new ToggleButton("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, delete, Constants.Plugin.Command.DELETE);

            Separator sep1 = new Separator();

            ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, reload, Constants.Plugin.Command.RELOAD);

            ToggleButton addTable = new ToggleButton("", JEConfig.getImage("add_table.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(addTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, addTable, Constants.Plugin.Command.ADD_TABLE);

            ToggleButton editTable = new ToggleButton("", JEConfig.getImage("edit_table.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(editTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, editTable, Constants.Plugin.Command.EDIT_TABLE);

            ToggleButton createWizard = new ToggleButton("", JEConfig.getImage("create_wizard.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(createWizard);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, createWizard, Constants.Plugin.Command.CREATE_WIZARD);


            ToggleButton collapseTree = new ToggleButton("", JEConfig.getImage("1404843819_node-tree.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(collapseTree);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, collapseTree, Constants.Plugin.Command.COLLAPSE);

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


            toolBar.getItems().setAll(save, newB, delete, reload, collapseTree, sep1);// addTable, editTable, createWizard);
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
                return true;
            case Constants.Plugin.Command.DELETE:
                return true;
            case Constants.Plugin.Command.EXPAND:
                return true;
            case Constants.Plugin.Command.NEW:
                return true;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return true;
            case Constants.Plugin.Command.EDIT_TABLE:
                return true;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return true;
            case Constants.Plugin.Command.FIND_OBJECT:
                return true;
            case Constants.Plugin.Command.PASTE:
                return true;
            case Constants.Plugin.Command.COPY:
                return true;
            case Constants.Plugin.Command.CUT:
                return true;
            case Constants.Plugin.Command.FIND_AGAIN:
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
                    TreeHelper.EventDelete(tree);
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
                    break;
                case Constants.Plugin.Command.EDIT_TABLE:
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
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.MOVE);
                        } else {
                            TreeHelper.EventDrop(tree, tree.getCopyObject(), ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                        }
                    }
                    break;
                case Constants.Plugin.Command.COPY:
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), false);
                    break;
                case Constants.Plugin.Command.CUT:
                    tree.setCopyObject(selectedObj.getValue().getJEVisObject(), true);
                    break;
                default:
                    logger.info("Unknown command ignore...");
            }
        } catch (Exception ex) {
            logger.error("Error running command: {}", cmdType);
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
    public ImageView getIcon() {
        return JEConfig.getImage("1394482640_package_settings.png", 20, 20);
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
                        I18n.getInstance().getString("plugin.object.attributes.changes"), "plugin.object.attributes.message");
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
