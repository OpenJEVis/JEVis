package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.WidgetTreePlugin;
import org.jevis.jeconfig.plugin.dashboard.widget.GenericConfigNode;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.I18n;

public class WidgetConfigDialog extends Alert {

    private TabPane tabPane = new TabPane();
    private DataModelDataHandler dataModelDataHandler;
    private WidgetTreePlugin widgetTreePlugin;
    private Widget widget;

    /**
     * Create an new Widget Config Dialog.
     */
    public WidgetConfigDialog(Widget widget) {
        super(AlertType.INFORMATION);

        this.widget = widget;
        setTitle(I18n.getInstance().getString("dashboard.widget.editor.title"));
        setHeaderText(I18n.getInstance().getString("dashboard.widget.editor.header"));
        setResizable(true);

//        getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1400));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);
        borderPane.setPrefWidth(780d);
        borderPane.setPrefHeight(650d);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        getDialogPane().setContent(borderPane);

    }


    public void requestFirstTabFocus() {
        Platform.runLater(() -> {
            tabPane.getTabs().get(0).getContent().requestFocus();

        });
    }

    private void addGeneralTab(DataModelDataHandler dataModelDataHandler) {
        GenericConfigNode genericConfigNode = new GenericConfigNode(widget.getDataSource(), widget, dataModelDataHandler);
        addTab(genericConfigNode);
    }

    public void addTab(Tab tab) {
//        Platform.runLater(() -> {
        this.tabPane.getTabs().add(tab);
//        });

    }

    public void addGeneralTabsDataModel(DataModelDataHandler dataModelDataHandler) {
        addGeneralTab(dataModelDataHandler);

        if (dataModelDataHandler != null) {
            this.dataModelDataHandler = dataModelDataHandler;
            this.widgetTreePlugin = new WidgetTreePlugin();

//            Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));
            Tab tab = new DataModelTab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel")
                    , dataModelDataHandler, widgetTreePlugin);


            JEVisTree tree = JEVisTreeFactory.buildDefaultWidgetTree(dataModelDataHandler.getJeVisDataSource(), this.widgetTreePlugin);
            tab.setContent(tree);
            this.widgetTreePlugin.setUserSelection(dataModelDataHandler.getDateNode().getData());

            addTab(tab);
        }

    }


    public void updateDataModel() {
        this.dataModelDataHandler.setData(this.widgetTreePlugin.getUserSelection());


    }

    public void commitSettings() {
        this.tabPane.getTabs().forEach(tab -> {
            try {
                if (tab instanceof ConfigTab) {
                    ((ConfigTab) tab).commitChanges();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private class DataModelTab extends Tab implements ConfigTab {
        DataModelDataHandler dataModelDataHandler;
        WidgetTreePlugin widgetTreePlugin;

        public DataModelTab(String text, DataModelDataHandler dataModelDataHandler, WidgetTreePlugin widgetTreePlugin) {
            super(text);
            this.dataModelDataHandler = dataModelDataHandler;
            this.widgetTreePlugin = widgetTreePlugin;
        }

        @Override
        public void commitChanges() {
            dataModelDataHandler.setData(this.widgetTreePlugin.getUserSelection());
        }
    }

}
