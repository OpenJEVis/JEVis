package org.jevis.jeconfig.dialog;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.controlsfx.control.PropertySheet;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class HiddenConfig {

    public static double CHART_PRECISION = 2.0;
    public static int DASH_THREADS = 4;
    public static int CHART_PRECISION_LIMIT = 1000;
    public static boolean CHART_PRECISION_ON = true;


    public static void showHiddenConfig() {


        Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();

        String chartGroup = "Chart";
        String dashboardGroup = "Dashboard";
        userConfig.put("CDPE", new ConfigSheet.Property("Enable drawing precision", chartGroup, CHART_PRECISION_ON, ""));
        userConfig.put("CDPV", new ConfigSheet.Property("Chart drawing precision", chartGroup, CHART_PRECISION, ""));
        userConfig.put("CDPL", new ConfigSheet.Property("Chart drawing precision lower limit", chartGroup, CHART_PRECISION_LIMIT, ""));

        userConfig.put("DTH", new ConfigSheet.Property("Dashboard Threads", dashboardGroup, DASH_THREADS, "Default 4"));


        ConfigSheet ct = new ConfigSheet();
        PropertySheet propertySheet = ct.getSheet(userConfig);
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);

        ButtonType buttonTypeOk = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        Dialog configDia = new Dialog();
        configDia.setTitle(I18n.getInstance().getString("plugin.scada.element.config.title"));
        configDia.setHeaderText(I18n.getInstance().getString("plugin.scada.element.config.header"));


        configDia.getDialogPane().setContent(propertySheet);
        configDia.resizableProperty().setValue(true);
        configDia.setHeight(800);
        configDia.setWidth(500);

        configDia.getDialogPane().setMinWidth(500);
        configDia.getDialogPane().setMinHeight(500);

        configDia.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();
        if (opt.get().equals(buttonTypeOk)) {
            CHART_PRECISION = (double) userConfig.get("CDPV").getObject();
            CHART_PRECISION_ON = (boolean) userConfig.get("CDPE").getObject();
            CHART_PRECISION_LIMIT = (int) userConfig.get("CDPL").getObject();
            DASH_THREADS = (int) userConfig.get("DTH").getObject();
        }


    }

}
