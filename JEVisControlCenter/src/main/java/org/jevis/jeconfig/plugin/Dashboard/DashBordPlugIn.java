package org.jevis.jeconfig.plugin.Dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.DonutChart;
import org.jevis.jeconfig.plugin.Dashboard.widget.NumberWidget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;

public class DashBordPlugIn implements Plugin {

    private static final Logger logger = LogManager.getLogger(DashBordPlugIn.class);
    private final DashBoardToolbar toolBar;
    private StringProperty nameProperty = new SimpleStringProperty("Dashboard");
    private StringProperty uuidProperty = new SimpleStringProperty("Dashboard");
    private JEVisDataSource jeVisDataSource;
    private boolean isInitialized = false;
    private AnchorPane rootPane = new AnchorPane();

    public DashBordPlugIn(JEVisDataSource ds, String name) {
        nameProperty.setValue(name);
        this.jeVisDataSource = ds;
        this.toolBar = new DashBoardToolbar();


    }

    @Override
    public String getClassName() {
        return this.getClassName();
    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        nameProperty.setValue(name);
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public String getUUID() {
        return uuidProperty.getValue();
    }

    @Override
    public void setUUID(String id) {
        uuidProperty.setValue(id);
    }

    @Override
    public String getToolTip() {
        return nameProperty.getValue();
    }

    @Override
    public StringProperty uuidProperty() {
        return uuidProperty;
    }

    @Override
    public Node getMenu() {
        return new Pane();
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public Node getToolbar() {

        return toolBar;
    }


    @Override
    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        jeVisDataSource = ds;
    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        return rootPane;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("if_dashboard_46791.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {
        if (!isInitialized) {
            isInitialized = true;
            DashBoardPane dashBoardPane = new DashBoardPane();
            toolBar.updateToolbar(dashBoardPane.getDashBordAnalysis());
            AnchorPane.setTopAnchor(dashBoardPane, 0d);
            AnchorPane.setBottomAnchor(dashBoardPane, 0d);
            AnchorPane.setLeftAnchor(dashBoardPane, 0d);
            AnchorPane.setRightAnchor(dashBoardPane, 0d);

            Widget testWidget = new NumberWidget();
            Widget donutWidget = new DonutChart();
            WidgetConfig config1 = new WidgetConfig();
            WidgetConfig config2 = new WidgetConfig();
            config1.size.setValue(WidgetConfig.Size.DEFAULT);
            config2.size.setValue(WidgetConfig.Size.TWO_THREE);
            config1.position.set(WidgetConfig.Position.DEFAULT_1);
            config2.position.set(WidgetConfig.Position.DEFAULT_2);
            testWidget.setConfig(config1);
            donutWidget.setConfig(config2);

            dashBoardPane.addNode(testWidget, config1);
            dashBoardPane.addNode(donutWidget, config2);
            rootPane.getChildren().add(dashBoardPane);

        }
    }
}
