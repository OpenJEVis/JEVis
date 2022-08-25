package org.jevis.jeconfig.tool;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Plugin;

import java.util.HashSet;
import java.util.Set;


public class DraggableTab extends Tab {

    private static final Set<TabPane> tabPanes = new HashSet<>();
    private TabPane mainTabPane;
    private Label nameLabel;
    private Text dragText;
    private static final Stage markerStage;
    private Stage dragStage;
    private boolean detachable;
    private BooleanProperty freigestellt = new SimpleBooleanProperty(false);
    private int oldIndex = 0;
    private Plugin plugin;

    static {
        markerStage = new Stage();
        markerStage.initStyle(StageStyle.UNDECORATED);
        Rectangle dummy = new Rectangle(3, 10, Color.web("#555555"));
        StackPane markerStack = new StackPane();
        markerStack.getChildren().add(dummy);
        markerStage.setScene(new Scene(markerStack));
    }

    public DraggableTab(String text, Region icon, Plugin plugin) {
        super(text);
        this.plugin = plugin;
        nameLabel = new Label(text);
        mainTabPane = this.getTabPane();
        this.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (mainTabPane == null) {
                mainTabPane = newValue;
                oldIndex = mainTabPane.getTabs().indexOf(DraggableTab.this);
            }
        });

        setGraphic(icon);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem detachItem = new MenuItem(I18n.getInstance().getString("dragtabs.menu.detach"));
        MenuItem attachItem = new MenuItem(I18n.getInstance().getString("dragtabs.menu.attach"));
        attachItem.setDisable(true);
        contextMenu.getItems().addAll(attachItem, detachItem);
        setContextMenu(contextMenu);

        freigestellt.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                attachItem.setDisable(false);
                detachItem.setDisable(true);
            } else {
                attachItem.setDisable(true);
                detachItem.setDisable(false);
            }
        });

        attachItem.setOnAction(event -> {
            getTabPane().getTabs().remove(DraggableTab.this);
            mainTabPane.getTabs().add(oldIndex, this);
            freigestellt.setValue(false);
        });


        detachItem.setOnAction(event -> {
            TabPane oldTabPane = getTabPane();
            tabPanes.add(oldTabPane);

            final BorderPane borderPane = new BorderPane();
            final Stage newStage = new Stage();
            final StackPane stackPane = new StackPane();
            final TabPane pane = new TabPane();
            tabPanes.add(pane);
            newStage.setOnHiding(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent t) {
                    tabPanes.remove(pane);
                    mainTabPane.getTabs().add(oldIndex, DraggableTab.this);
                }
            });
            getTabPane().getTabs().remove(DraggableTab.this);
            pane.getTabs().add(DraggableTab.this);
            pane.getTabs().addListener(new ListChangeListener<Tab>() {

                @Override
                public void onChanged(ListChangeListener.Change<? extends Tab> change) {
                    if (pane.getTabs().isEmpty()) {
                        newStage.hide();
                    }
                }
            });
            /* Top menu may also work but needs further testing */
            //TopMenu topMenu = new TopMenu(stackPane);
            //topMenu.setPlugin(getPlugin());


            borderPane.setTop(getPlugin().getToolbar());
            borderPane.setCenter(pane);


            freigestellt.setValue(true);
            newStage.setScene(new Scene(borderPane));
            newStage.initStyle(StageStyle.DECORATED);
            newStage.setX(200);
            newStage.setY(200);
            newStage.setWidth(1000);
            newStage.setHeight(600);
            newStage.show();
            pane.requestLayout();
            pane.requestFocus();

        });

    }

    public Plugin getPlugin() {
        return plugin;
    }


}