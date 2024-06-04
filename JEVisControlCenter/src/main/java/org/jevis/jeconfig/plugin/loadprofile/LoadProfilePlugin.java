package org.jevis.jeconfig.plugin.loadprofile;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.*;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.tools.JEVisHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LoadProfilePlugin implements Plugin {
    public static final String PLUGIN_NAME = "Load Profile Plugin";
    private static final Logger logger = LogManager.getLogger(LoadProfilePlugin.class);
    private final int iconSize = 20;
    private final JEVisDataSource ds;
    private final String title;
    private final ToolBar toolBar = new ToolBar();
    private final BorderPane borderPane = new BorderPane();
    private boolean initialized = false;

    public LoadProfilePlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;

        initToolbar();
    }

    @Override
    public String getClassName() {
        return "Load Profile Plugin";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    @Override
    public String getUUID() {
        return "";
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return I18n.getInstance().getString("plugin.loadprofile.tooltip");
    }

    @Override
    public StringProperty uuidProperty() {
        return null;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public boolean supportsRequest(int cmdType) {

        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                return false;
            case Constants.Plugin.Command.DELETE:
                return false;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return false;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return false;
            case Constants.Plugin.Command.EDIT_TABLE:
                return false;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return false;
            case Constants.Plugin.Command.FIND_OBJECT:
                return false;
            case Constants.Plugin.Command.PASTE:
                return false;
            case Constants.Plugin.Command.COPY:
                return false;
            case Constants.Plugin.Command.CUT:
                return false;
            case Constants.Plugin.Command.FIND_AGAIN:
                return false;
            default:
                return false;
        }
    }

    @Override
    public Node getToolbar() {
        return null;
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

    }

    @Override
    public void handleRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                break;
            case Constants.Plugin.Command.DELETE:
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                break;
            case Constants.Plugin.Command.RELOAD:
                break;
            case Constants.Plugin.Command.ADD_TABLE:
                break;
            case Constants.Plugin.Command.EDIT_TABLE:
                break;
            case Constants.Plugin.Command.CREATE_WIZARD:
                break;
            case Constants.Plugin.Command.FIND_OBJECT:
                break;
            case Constants.Plugin.Command.PASTE:
                break;
            case Constants.Plugin.Command.COPY:
                break;
            case Constants.Plugin.Command.CUT:
                break;
            case Constants.Plugin.Command.FIND_AGAIN:
                break;
        }
    }

    @Override
    public Node getContentNode() {
        return borderPane;
    }

    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.LOADPROFILE, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {
        if (!initialized) {
            initGui();
            initialized = true;
        }
    }

    private void initGui() {

        Button addInput = new Button("", JEConfig.getSVGImage(Icon.PLUS, iconSize, iconSize));
        Button removeInput = new Button("", JEConfig.getSVGImage(Icon.MINUS, iconSize, iconSize));
        HBox inputListMenu = new HBox(6, addInput, removeInput);

        ListView<JEVisObject> inputListView = new ListView<>();
        ObservableList<JEVisObject> inputList = FXCollections.observableArrayList();
        inputListView.setItems(inputList);

        BorderPane inputControl = new BorderPane();
        inputControl.setTop(inputListMenu);
        inputControl.setCenter(inputListView);

        addInput.setOnAction(actionEvent -> {
            List<JEVisClass> classList = new ArrayList<>();
            for (String className : TreeSelectionDialog.allData) {
                try {
                    classList.add(ds.getJEVisClass(className));
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, classList, SelectionMode.MULTIPLE);
            selectionDialog.setOnCloseRequest(dialogEvent -> selectionDialog.getUserSelection().forEach(userSelection -> inputList.add(userSelection.getSelectedObject())));

            selectionDialog.showAndWait();
        });

        removeInput.setOnAction(actionEvent -> inputList.remove(inputListView.getSelectionModel().getSelectedItem()));

        Label outputButtonLabel = new Label(I18n.getInstance().getString("plugin.object.calc.output"));
        Button outputButton = new Button("Output");
        HBox outputControl = new HBox(6, outputButtonLabel, outputButton);
        AtomicReference<JEVisObject> outputObject = new AtomicReference<>();

        outputButton.setOnAction(actionEvent -> {
            List<JEVisClass> classList = new ArrayList<>();
            for (String className : TreeSelectionDialog.allData) {
                try {
                    classList.add(ds.getJEVisClass(className));
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, classList, SelectionMode.SINGLE);
            selectionDialog.setOnCloseRequest(dialogEvent -> selectionDialog.getUserSelection().forEach(userSelection -> outputObject.set(userSelection.getSelectedObject())));

            selectionDialog.showAndWait();
        });
    }

    private void initToolbar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        toolBar.getItems().setAll(reload, new Separator());

        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);

        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        JEVisHelp.getInstance().addHelpItems(this.getClass().getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 0;
    }
}
