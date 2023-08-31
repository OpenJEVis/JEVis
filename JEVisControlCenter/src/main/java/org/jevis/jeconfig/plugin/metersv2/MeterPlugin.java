package org.jevis.jeconfig.plugin.metersv2;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;

public class MeterPlugin implements Plugin {

    private final BorderPane rootPane = new BorderPane();
    private final StringProperty uuidProperty = new SimpleStringProperty("Nonconformity");
    MeterController meterController;
    private SimpleStringProperty nameProperty = new SimpleStringProperty(I18n.getInstance().getString("plugin.nonconformities.name"));
    private JEVisDataSource ds;
    private boolean isInit = false;
    private NonconformitiesToolbar toolbar;
    public static final String PLUGIN_NAME = "Meter Plugin v2";


    public MeterPlugin(JEVisDataSource ds, String name) {
        this.ds = ds;
        this.nameProperty.set(name);

        meterController = new MeterController(this,ds);
        rootPane.setCenter(meterController.getContent());
        toolbar = new NonconformitiesToolbar(meterController);
    }

    private void initGUI() {
        if (isInit) return;
        isInit = true;

        //nonconformitiesController.loadActionView();
        meterController.loadNonconformityPlans();
    }

    @Override
    public String getClassName() {
        return "Deviation Plugin";
    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        nameProperty.set(name);
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public String getUUID() {
        return this.uuidProperty.getValue();
    }

    @Override
    public void setUUID(String id) {
        this.uuidProperty.setValue(id);
    }

    @Override
    public String getToolTip() {
        return nameProperty.get();
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
        return toolbar;
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

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        return rootPane;
    }

    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.MINUS_CIRCLE, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {
        initGUI();
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 8;
    }
}
