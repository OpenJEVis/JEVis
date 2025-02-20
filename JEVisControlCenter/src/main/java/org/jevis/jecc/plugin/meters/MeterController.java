package org.jevis.jecc.plugin.meters;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.meters.data.MeterData;
import org.jevis.jecc.plugin.meters.data.MeterList;
import org.jevis.jecc.plugin.meters.ui.MeterForm;
import org.jevis.jecc.plugin.meters.ui.MeterListTab;
import org.jevis.jecc.plugin.meters.ui.MeterTable;
import org.jevis.jecc.plugin.meters.ui.NewMeterDialog;

import java.util.List;

public class MeterController {
    private static final Logger logger = LogManager.getLogger(MeterController.class);

    private final MeterPlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private final ObservableList<MeterList> meterLists = FXCollections.observableArrayList();
    private final JEVisDataSource ds;
    private final IntegerProperty lastRawValuePrecision = new SimpleIntegerProperty(2);
    private final BooleanProperty canWrite = new SimpleBooleanProperty(false);
    private final BooleanProperty canDelete = new SimpleBooleanProperty(false);
    private TabPane tabPane;

    public MeterController(MeterPlugin plugin, JEVisDataSource ds) {
        this.ds = ds;
        this.plugin = plugin;
    }

    public void loadMeters() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);
        try {
            JEVisClass measurementDirectory = plugin.getDataSource().getJEVisClass(JC.Directory.MeasurementDirectory.name);
            List<JEVisObject> measurementDirectories = plugin.getDataSource().getObjects(measurementDirectory, false);

            measurementDirectories.forEach(jeVisObject -> {
                MeterList meterList = new MeterList(jeVisObject);
                meterList.loadMeterList();
                meterLists.add(meterList);
                MeterListTab meterListTab = new MeterListTab(meterList, this, ds);
                tabPane.getTabs().add(meterListTab);

            });

            tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, tab, t1) -> {
                try {
                    checkRights((MeterListTab) t1);

                } catch (Exception e) {
                    logger.error(e);
                }


            });
            checkRights(getActiveTab());


        } catch (Exception e) {
            logger.error(e);
        }
    }

    public MeterListTab getActiveTab() {
        MeterListTab tab = (MeterListTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public Node getContent() {
        return contentPane;
    }

    public MeterData getSelectedData() {
        MeterListTab tab = getActiveTab();
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

        disableSaveIfUserCannotWrite(meterData, btOk);
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

    private void disableSaveIfUserCannotWrite(MeterData meterData, Button btOk) {
        try {
            if (!ds.getCurrentUser().canWrite(meterData.getJeVisObject().getID())) btOk.setDisable(true);
        } catch (Exception e) {
            logger.error(e);
        }

    }

    public void addMeter() {
        NewMeterDialog newMeterDialog = new NewMeterDialog(ds, getActiveTab().getPlan(), getSelectedItem() != null ? getSelectedItem().getJeVisObject() : null);
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        newMeterDialog.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) newMeterDialog.getDialogPane().lookupButton(buttonTypeOne);

        btOk.setOnAction(actionEvent -> {
            JEVisObject jeVisObject = newMeterDialog.commit();
            if (jeVisObject == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.create.denied"));
                alert.showAndWait();
            } else {
                MeterData meterData = new MeterData(jeVisObject);
                openDataForm(meterData, false, true);
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

    public MeterTable getActiveTable() {
        return getActiveTab().getMeterPlanTable();
    }

    public void checkRights(MeterListTab meterListTab) throws JEVisException {
        canWrite.set(ds.getCurrentUser().canCreate(meterListTab.getPlan().getJeVisObject().getID()));
        canDelete.set(ds.getCurrentUser().canDelete(meterListTab.getPlan().getJeVisObject().getID()));

    }


    public boolean isCanWrite() {
        return canWrite.get();
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite.set(canWrite);
    }

    public BooleanProperty canWriteProperty() {
        return canWrite;
    }

    public boolean isCanDelete() {
        return canDelete.get();
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete.set(canDelete);
    }

    public BooleanProperty canDeleteProperty() {
        return canDelete;
    }
}
