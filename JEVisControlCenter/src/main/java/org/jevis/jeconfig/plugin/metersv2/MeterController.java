package org.jevis.jeconfig.plugin.metersv2;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterForm;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterPlanTab;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterPlanTable;
import org.jevis.jeconfig.plugin.metersv2.ui.NewMeterDialog;

import java.util.List;
import java.util.Optional;

public class MeterController {
    private static final Logger logger = LogManager.getLogger(MeterController.class);

    private final MeterPlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private final ObservableList<MeterPlan> meterPlans = FXCollections.observableArrayList();
    private TabPane tabPane;
    private final JEVisDataSource ds;

    private final IntegerProperty lastRawValuePrecision = new SimpleIntegerProperty(2);

    public MeterController(MeterPlugin plugin, JEVisDataSource ds) {
        this.ds = ds;
        this.plugin = plugin;
    }

    public void loadNonconformityPlans() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);
        try {
            JEVisClass measurementDirectory = plugin.getDataSource().getJEVisClass(JC.Directory.MeasurementDirectory.name);
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(measurementDirectory, false);

            planObjs.forEach(jeVisObject -> {
                MeterPlan meterPlan = new MeterPlan(jeVisObject);
                meterPlan.loadMeterList();
                meterPlans.add(meterPlan);
                MeterPlanTab meterPlanTab = new MeterPlanTab(meterPlan, this, ds);
                tabPane.getTabs().add(meterPlanTab);

            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MeterPlanTab getActiveTab() {
        MeterPlanTab tab = (MeterPlanTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public Node getContent() {
        return contentPane;
    }

    public MeterData getSelectedData() {
        MeterPlanTab tab = getActiveTab();
        if (tab.getMeterPlanTable().getSelectionModel().getSelectedItem() != null) {
            return tab.getMeterPlanTable().getSelectionModel().getSelectedItem();
        } else {
            if (tab.getMeterPlanTable().getItems().isEmpty()) {
                {
                    return null;
                }
            }

            return tab.getMeterPlanTable().getItems().get(0);
        }
    }

    public void openDataForm(MeterData meterData, boolean switchMeter, boolean isNew) {

        MeterForm meterForm = new MeterForm(meterData, ds, switchMeter);

        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        meterForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) meterForm.getDialogPane().lookupButton(buttonTypeOne);
        btOk.setOnAction(actionEvent -> {
            meterForm.commit();
            if (isNew) {
                getActiveTab().getMeterPlanTable().addItem(meterData);
                getActiveTab().getMeterPlanTable().sort();
            } else {
                getActiveTab().getMeterPlanTable().replaceItem(meterData);
                getActiveTab().getMeterPlanTable().sort();

            }
            getActiveTab().getMeterPlanTable().refresh();

        });
        meterForm.show();

    }

    public void addMeter() {
        NewMeterDialog newMeterDialog = new NewMeterDialog(ds, getActiveTab().getPlan());
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        newMeterDialog.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) newMeterDialog.getDialogPane().lookupButton(buttonTypeOne);

        btOk.setOnAction(actionEvent -> {
            JEVisObject jeVisObject = newMeterDialog.commit();
            if (jeVisObject == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Object Could not Created");
                alert.showAndWait();
            } else {
                MeterData meterData = new MeterData(jeVisObject);
                openDataForm(meterData,false,true);
            }
        });


        newMeterDialog.showAndWait();


    }

    public int getLastRawValuePrecision() {
        return lastRawValuePrecision.get();
    }

    public void setLastRawValuePrecision(int lastRawValuePrecision) {
        this.lastRawValuePrecision.set(lastRawValuePrecision);
    }

    public IntegerProperty lastRawValuePrecisionProperty() {
        return lastRawValuePrecision;
    }

    public MeterData getSelectedItem() {
        return getActiveTab().getMeterPlanTable().getSelectedItem();
    }

    public MeterPlanTable getActiveTable() {
        return getActiveTab().getMeterPlanTable();
    }


}