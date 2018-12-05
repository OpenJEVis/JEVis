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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.dialog.*;
import org.jevis.application.jevistree.*;
import org.jevis.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.bulkedit.CreateTable;
import org.jevis.jeconfig.bulkedit.EditTable;
import org.jevis.jeconfig.tool.I18n;
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

    private StringProperty name = new SimpleStringProperty("*NO_NAME*");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane viewPane;
    //    private ObjectTree tf;
//    private ObjectTree tree;
    private JEVisTree tree;
    private LoadingPane editorLodingPane = new LoadingPane();
    private LoadingPane treeLodingPane = new LoadingPane();
    private ToolBar toolBar;
    private ObjectEditor _editor = new ObjectEditor();
    private SimpleBooleanProperty loadingObjectProperty = new SimpleBooleanProperty();
    private String tooltip = I18n.getInstance().getString("pluginmanager.object.tooltip");

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
//        if (tree.getSelectionModel().getSelectedItem() == null) {
        try {
            if (tree.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> {
                    try {
                        tree.getSelectionModel().getModelItem(0).expandedProperty().setValue(Boolean.TRUE);
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

            tree = JEVisTreeFactory.buildBasicDefault(ds);
            tree.setId("objecttree");
//            tree.getStylesheets().add("/styles/Styles.css");
            tree.setStyle("-fx-background-color: #E2E2E2;");
            //tree.getSelectionModel().selectFirst();

            VBox left = new VBox();
            left.setPrefWidth(460);
            left.setStyle("-fx-background-color: #E2E2E2;");

            VBox.setVgrow(tree, Priority.ALWAYS);
//            VBox.setVgrow(search, Priority.NEVER);


            List<JEVisTreeFilter> allObjects = new ArrayList<>();
            allObjects.add(SelectTargetDialog.buildAllObjects());
            allObjects.add(SelectTargetDialog.buildAllDataFilter());
            allObjects.add(SelectTargetDialog.buildAllAttributesFilter());
            allObjects.add(SelectTargetDialog.buildCalanderFilter());
            allObjects.add(SelectTargetDialog.buildAllDataSources(this.ds));
            allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "Calculation"));
            allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "User"));
            allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "Group"));
            allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "Analysis"));
            allObjects.add(SelectTargetDialog.buildClassFilter(this.ds, "Report"));


            Finder finder = new Finder(tree);
            SearchFilterBar searchBar = new SearchFilterBar(tree, allObjects, finder);


            treeLodingPane.setContent(left);
            editorLodingPane.setContent(_editor.getView());
            left.getChildren().addAll(tree, searchBar);

            SplitPane sp = new SplitPane();
            sp.setDividerPositions(.3d);
            sp.setOrientation(Orientation.HORIZONTAL);
            sp.setId("mainsplitpane");
            sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
//            sp.getItems().setAll(left, tree.getEditor().getView());
            sp.getItems().setAll(treeLodingPane, editorLodingPane);

            treeLodingPane.endLoading();
            editorLodingPane.endLoading();

            viewPane = new BorderPane();
            viewPane.setCenter(sp);
            viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

            //public void changed(ObservableValue<? extends TreeItem<JEVisObject>> ov, TreeItem<JEVisObject> t, TreeItem<JEVisObject> t1) {
            //TreeItem<JEVisTreeRow>
            tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    if (newValue instanceof JEVisTreeItem) {
                        JEVisTreeItem item = (JEVisTreeItem) newValue;
                        JEVisObject obj = item.getValue().getJEVisObject();

                        loadingObjectProperty.setValue(true);

                        //new
                        if (_editor.needSave()) {
                            ConfirmDialog dia = new ConfirmDialog();
                            ConfirmDialog.Response re = dia.show(JEConfig.getStage(), "Save", "Save Attribute Changes", "Changes will be lost if not saved, do you want to save now?");
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
                                logger.info("changed: oh object is a link so im loading the linked object");
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
            });

        }

        return viewPane;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {

        if (toolBar == null) {
            toolBar = new ToolBar();
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
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, delete, Constants.Plugin.Command.DELTE);

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


            toolBar.getItems().addAll(save, newB, delete, reload, collapseTree, sep1);// addTable, editTable, createWizard);
        }

        return toolBar;
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
            case Constants.Plugin.Command.DELTE:
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
            final TreeItem<JEVisTreeRow> parent = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
//                    eventSaveAttributes();
                    saveWithAnimation();
                    break;
                case Constants.Plugin.Command.DELTE:
                    TreeHelper.EventDelete(tree);
                    break;
                case Constants.Plugin.Command.RENAME:
                    TreeHelper.EventRename(tree, parent.getValue().getJEVisObject());
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
                    TreeHelper.EventNew(tree);
                    break;
                case Constants.Plugin.Command.RELOAD:
                    TreeHelper.EventReload(((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject());
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
                    TreeHelper.EventOpenObject(tree, JEVisTreeFactory.findNode);
                    break;
                case Constants.Plugin.Command.FIND_AGAIN:
                    TreeHelper.EventOpenObject(tree, JEVisTreeFactory.findAgain);
                    break;
                case Constants.Plugin.Command.PASTE:
                    TreeHelper.EventDrop(tree, tree.getCopyObject(), parent.getValue().getJEVisObject(), CopyObjectDialog.DefaultAction.COPY);
                    break;
                case Constants.Plugin.Command.COPY:
                    tree.setCopyObject(parent.getValue().getJEVisObject());
                    break;
                default:
                    logger.info("Unknown command ignore...");
            }
        } catch (Exception ex) {
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
                        for (int j = 0; j < attributes.size(); j++) {
                            if (attributes.get(j).getName().equals("Value")) {
                                JEVisAttribute attributeValue = attributes.get(j);

                                //get objekt mit ID from Tabelle
                                if (table.getPairList().get(i).getValue().get(0).isEmpty() && table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                    attributeValue.setDisplayUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                } else {
                                    String displaySymbol = table.getPairList().get(i).getValue().get(1);
                                    if (table.getPairList().get(i).getValue().get(0).isEmpty() && !table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                        attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        JEVisUnit.Prefix prefixDisplayUnit = JEVisUnit.Prefix.valueOf(table.getPairList().get(i).getValue().get(0));
                                        attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                    }
                                }

                                if (table.getPairList().get(i).getValue().get(3).isEmpty() && table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                    attributeValue.setInputUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                } else {
                                    String inputSymbol = table.getPairList().get(i).getValue().get(4);
                                    if (table.getPairList().get(i).getValue().get(3).isEmpty() && !table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                        attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        JEVisUnit.Prefix prefixInputUnit = JEVisUnit.Prefix.valueOf(table.getPairList().get(i).getValue().get(3));
                                        attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", prefixInputUnit));
                                    }
                                }

                                if (table.getPairList().get(i).getValue().get(2).isEmpty()) {
                                    attributeValue.setDisplaySampleRate(Period.parse("PT0S"));//Period.ZERO
                                } else {
                                    String displaySampleRate = table.getPairList().get(i).getValue().get(2);
                                    attributeValue.setDisplaySampleRate(Period.parse(displaySampleRate));
                                }

                                if (table.getPairList().get(i).getValue().get(5).isEmpty()) {
                                    attributeValue.setInputSampleRate(Period.parse("PT0S"));//Period.ZERO
                                } else {
                                    String inputSampleRate = table.getPairList().get(i).getValue().get(5);
                                    attributeValue.setInputSampleRate(Period.parse(inputSampleRate));
                                }

                                attributeValue.commit();
                            } else {
                                attributes.get(j).buildSample(new DateTime(), table.getPairList().get(i).getValue().get(counter)).commit();
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
                            for (int j = 0; j < attributes.size(); j++) {
                                if (attributes.get(j).getName().equals("Value")) {

                                    JEVisAttribute attributeValue = attributes.get(j);

                                    if (table.getPairList().get(i).getValue().get(0).isEmpty() && table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                        attributeValue.setDisplayUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        String displaySymbol = table.getPairList().get(i).getValue().get(1);
                                        if (table.getPairList().get(i).getValue().get(0).isEmpty() && !table.getPairList().get(i).getValue().get(1).isEmpty()) {
                                            attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", JEVisUnit.Prefix.NONE));
                                        } else {
                                            JEVisUnit.Prefix prefixDisplayUnit = JEVisUnit.Prefix.valueOf(table.getPairList().get(i).getValue().get(0));
                                            attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                        }
                                    }

                                    if (table.getPairList().get(i).getValue().get(3).isEmpty() && table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                        attributeValue.setInputUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        String inputSymbol = table.getPairList().get(i).getValue().get(4);
                                        if (table.getPairList().get(i).getValue().get(3).isEmpty() && !table.getPairList().get(i).getValue().get(4).isEmpty()) {
                                            attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", JEVisUnit.Prefix.NONE));
                                        } else {
                                            JEVisUnit.Prefix prefixInputUnit = JEVisUnit.Prefix.valueOf(table.getPairList().get(i).getValue().get(3));
                                            attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(inputSymbol), "", prefixInputUnit));
                                        }
                                    }

                                    if (table.getPairList().get(i).getValue().get(2).isEmpty()) {
                                        attributeValue.setDisplaySampleRate(Period.parse("PT0S"));//Period.ZERO
                                    } else {
                                        String displaySampleRate = table.getPairList().get(i).getValue().get(2);
                                        attributeValue.setDisplaySampleRate(Period.parse(displaySampleRate));
                                    }

                                    if (table.getPairList().get(i).getValue().get(5).isEmpty()) {
                                        attributeValue.setInputSampleRate(Period.parse("PT0S"));//Period.ZERO
                                    } else {
                                        String inputSampleRate = table.getPairList().get(i).getValue().get(5);
                                        attributeValue.setInputSampleRate(Period.parse(inputSampleRate));
                                    }
                                    attributeValue.commit();
                                } else {
                                    attributes.get(j).buildSample(new DateTime(), table.getPairList().get(i).getValue().get(counter)).commit();
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
                            info.show(JEConfig.getStage(), "Waring", "Could not create user", "Could not create new user because this user exists already.");

                        } else {
                            ExceptionDialog errorDia = new ExceptionDialog();
                            errorDia.show(JEConfig.getStage(), "Error", "Could not create user", "Could not create new user.", ex, JEConfig.PROGRAM_INFO);

                        }
                    }
                }
            }
        }
    }
}
