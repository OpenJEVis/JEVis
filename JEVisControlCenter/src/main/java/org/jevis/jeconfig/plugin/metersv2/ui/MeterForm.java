package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.control.DataTypeBox;
import org.jevis.jeconfig.application.control.DayBox;
import org.jevis.jeconfig.application.control.MonthBox;
import org.jevis.jeconfig.application.control.YearBox;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class MeterForm extends Dialog {

    private static final Logger logger = LogManager.getLogger(MeterForm.class);

    private final MeterData meterData;
    private final JEVisDataSource ds;
   List<Tab> tabs;

    private TabPane tabPane = new TabPane();

    public MeterForm(MeterData meterData, JEVisDataSource ds, boolean switchMeter) {
        this.meterData = meterData;
        this.ds = ds;
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);

        tabs = new ArrayList<>();
        try {
            tabs.add(new MeterFormAttributeTab(meterData, ds, I18n.getInstance().getString("plugin.meters.dialog.tab.attribute")));


            if (switchMeter)
                tabs.add(new MeterFormReadingsTab(meterData, ds, I18n.getInstance().getString("plugin.meters.dialog.tab.readings")));
        } catch (JEVisException jeVisException) {
            logger.error(jeVisException);
            Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
            alert.showAndWait();
            close();
        }



        tabPane.getTabs().addAll(tabs);
        setHeaderText(meterData.getName());

        getDialogPane().setContent(tabPane);


    }


    public JEVisObject commit(){
        tabs.forEach(tab -> {
           MeterFormTab meterFormTab = (MeterFormTab) tab;
           meterFormTab.commit();
        });

        return null;
        }





}