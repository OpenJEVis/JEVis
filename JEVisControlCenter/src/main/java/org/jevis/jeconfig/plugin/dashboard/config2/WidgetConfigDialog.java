package org.jevis.jeconfig.plugin.dashboard.config2;

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
import org.jevis.jeconfig.tool.ScreenSize;

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
        getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1400));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        getDialogPane().setContent(borderPane);

    }

    private void addGeneralTab(DataModelDataHandler dataModelDataHandler) {
        GenericConfigNode genericConfigNode = new GenericConfigNode(widget.getDataSource(), widget, dataModelDataHandler);
        addTab(genericConfigNode);
    }

    public void addTab(Tab tab) {
        this.tabPane.getTabs().add(tab);
    }

    public void addGeneralTabsDataModel(DataModelDataHandler dataModelDataHandler) {
        addGeneralTab(dataModelDataHandler);

        if (dataModelDataHandler != null) {
            System.out.println("WidgetConfigDia.addGeneralTabsDataModel: " + dataModelDataHandler);
            Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));
            this.dataModelDataHandler = dataModelDataHandler;

            this.widgetTreePlugin = new WidgetTreePlugin();

            System.out.println("Open userselection:" + dataModelDataHandler.getDateNode().getData().size());
            JEVisTree tree = JEVisTreeFactory.buildDefaultWidgetTree(dataModelDataHandler.getJeVisDataSource(), this.widgetTreePlugin);
            tab.setContent(tree);
            this.widgetTreePlugin.setUserSelection(dataModelDataHandler.getDateNode().getData());

            addTab(tab);
        }


    }

    public void updateDataModel() {
        System.out.println("WidgetConfigDia.updateDataModel: " + this.widgetTreePlugin.getUserSelection().size());
        this.widgetTreePlugin.getUserSelection().forEach(dataPointNode -> {
            System.out.println("Selected after: " + dataPointNode.getObjectID());
        });

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

}
