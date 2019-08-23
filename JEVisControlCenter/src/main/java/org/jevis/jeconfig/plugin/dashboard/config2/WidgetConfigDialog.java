package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.WidgetTreePlugin;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ScreenSize;

public class WidgetConfigDialog extends Alert {

    private TabPane tabPane = new TabPane();
    private DataModelDataHandler dataModelDataHandler;
    private WidgetTreePlugin widgetTreePlugin;


    /**
     * Create an new Widget Config Dialog.
     *
     * @param alertType this parameter will be ignored
     */
    public WidgetConfigDialog(AlertType alertType) {
        super(Alert.AlertType.CONFIRMATION);

        setTitle(I18n.getInstance().getString("dashboard.widget.editor.title"));
        setHeaderText(I18n.getInstance().getString("dashboard.widget.editor.header"));
        setResizable(true);
        getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1400));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);


        getDialogPane().setContent(borderPane);

    }

    public void addTab(Tab tab) {
        this.tabPane.getTabs().add(tab);
    }

    public void addDataModel(DataModelDataHandler dataModelDataHandler) {
        Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));
        this.dataModelDataHandler = dataModelDataHandler;

        this.widgetTreePlugin = new WidgetTreePlugin();

        JEVisTree tree = JEVisTreeFactory.buildDefaultWidgetTree(dataModelDataHandler.getJeVisDataSource(), this.widgetTreePlugin);
        tab.setContent(tree);
        this.widgetTreePlugin.setUserSelection(dataModelDataHandler.getDateNode().getData());

        addTab(tab);

    }

    public void updateDataModel() {
        this.widgetTreePlugin.getUserSelection().forEach(dataPointNode -> {
            System.out.println("Selected after: " + dataPointNode.getObjectID());
        });

        this.dataModelDataHandler.setData(this.widgetTreePlugin.getUserSelection());


    }


}
