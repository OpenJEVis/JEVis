package org.jevis.jeconfig.plugin.layout;

import eu.hansolo.tilesfx.Tile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.tool.LoadingPane;

import java.util.ArrayList;
import java.util.List;

public class LayoutmanagerPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(LayoutmanagerPlugin.class);
    private final Object lock = new Object();
    private StringProperty name = new SimpleStringProperty("puginmanager.dashboard.title");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private AnchorPane viewPane;
    private JEVisTree tree;
    private LoadingPane editorLoadingPane = new LoadingPane();
    private LoadingPane treeLoadingPane = new LoadingPane();
    private ToolBar toolBar;
    private FlowPane flowPane = new FlowPane();
    private Thread thread;
    private List<Tile> tiles = new ArrayList<>();

    public LayoutmanagerPlugin(JEVisDataSource ds, String newname) {
        this.ds = ds;
        name.set(newname);
    }

    @Override
    public String getClassName() {
        return "Layout Plugin";
    }

    @Override
    public void setHasFocus() {

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 90;
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
        return "";
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getContentNode() {
        if (viewPane == null) {


            ScrollPane scrollPane = new ScrollPane(flowPane);
            scrollPane.setFitToWidth(true);
            viewPane = new AnchorPane();
            viewPane.getChildren().add(scrollPane);
            AnchorPane.setTopAnchor(scrollPane, 0.0);
            AnchorPane.setBottomAnchor(scrollPane, 0.0);
            AnchorPane.setLeftAnchor(scrollPane, 0.0);
            AnchorPane.setRightAnchor(scrollPane, 0.0);
            viewPane.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);


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


            ToggleButton newB = new ToggleButton("", JEConfig.getImage("iconfinder_ic_play_circle_outline_48px_352074.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            GlobalToolBar.BuildEventhandler(LayoutmanagerPlugin.this, newB, 99);

            ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
            GlobalToolBar.BuildEventhandler(LayoutmanagerPlugin.this, save, Constants.Plugin.Command.SAVE);

            ToggleButton delete = new ToggleButton("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
            GlobalToolBar.BuildEventhandler(LayoutmanagerPlugin.this, delete, Constants.Plugin.Command.DELETE);

            ToggleButton expandTree = new ToggleButton("", JEConfig.getImage("create_wizard.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(expandTree);
            GlobalToolBar.BuildEventhandler(LayoutmanagerPlugin.this, expandTree, Constants.Plugin.Command.EXPAND);


            toolBar.getItems().addAll(save, newB);// addTable, editTable, createWizard);
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
//        _editor.commitAll();

    }

    @Override
    public boolean supportsRequest(int cmdType) {
        switch (cmdType) {
            case 99:
                return true;
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
            logger.error("handleRequest: {}", cmdType);
//            final TreeItem<JEVisTreeRow> selectedObj = (TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem();
            switch (cmdType) {
                case 99:
                    if (!thread.isAlive()) {
                        thread.start();
                    } else {
                        thread.interrupt();
                    }
                    break;
                case Constants.Plugin.Command.SAVE:
                    break;
                case Constants.Plugin.Command.DELETE:
                    break;
                case Constants.Plugin.Command.RENAME:
                    break;
                case Constants.Plugin.Command.COLLAPSE:

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
                case Constants.Plugin.Command.FIND_AGAIN:
                    break;
                case Constants.Plugin.Command.PASTE:
                    break;
                case Constants.Plugin.Command.COPY:
                    break;
                case Constants.Plugin.Command.CUT:
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
}
