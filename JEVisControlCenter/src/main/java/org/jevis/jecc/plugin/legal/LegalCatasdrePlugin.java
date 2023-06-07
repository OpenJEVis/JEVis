package org.jevis.jecc.plugin.legal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.commons.classes.JEVisClassPrinter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.Plugin;

public class LegalCatasdrePlugin implements Plugin {

    public static final String PLUGIN_NAME = "Index of Legal Provisions Plugin";
    private final BorderPane rootPane = new BorderPane();
    private final StringProperty uuidProperty = new SimpleStringProperty("Nonconformity");
    public LegalCadastreController legalCadastreController;
    BooleanProperty inAlarm = new SimpleBooleanProperty();
    private SimpleStringProperty nameProperty = new SimpleStringProperty(I18n.getInstance().getString("plugin.nonconformities.name"));
    private JEVisDataSource ds;
    private boolean isInit = false;
    private LegalCatasdreToolbar toolbar;


    public LegalCatasdrePlugin(JEVisDataSource ds, String name) {
        this.ds = ds;
        this.nameProperty.set(name);

        legalCadastreController = new LegalCadastreController(this);
        rootPane.setCenter(legalCadastreController.getContent());
        toolbar = new LegalCatasdreToolbar(legalCadastreController);
        initGUI();
    }

    private void initGUI() {


        JEVisClassPrinter jeVisClassPrinter = new JEVisClassPrinter(ds);
        JEVisClass jeVisClass = null;
        //JC
        try {
            jeVisClass = ds.getJEVisClass("Legislation");
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        jeVisClassPrinter.printClass(3, jeVisClass);
        if (isInit) return;
        isInit = true;

        legalCadastreController.loadActionView();
        legalCadastreController.loadNonconformityPlans();

    }

    @Override
    public String getClassName() {
        return PLUGIN_NAME;
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
        return ControlCenter.getSVGImage(Icon.BALANCE, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
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

    public boolean isInAlarm() {
        return inAlarm.get();
    }

    public void setInAlarm(boolean inAlarm) {
        this.inAlarm.set(inAlarm);
    }

    public BooleanProperty inAlarmProperty() {
        return inAlarm;
    }
}
