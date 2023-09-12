package org.jevis.jeconfig.plugin.metersv2;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterForm;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterPlanTab;
import org.jevis.jeconfig.plugin.metersv2.ui.NewMeterDialog;

import java.util.List;
import java.util.Optional;

public class MeterController {
    private static final Logger logger = LogManager.getLogger(MeterController.class);

    private final MeterPlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<MeterPlan> meterPlans = FXCollections.observableArrayList();
    private TabPane tabPane;
    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);

    private BooleanProperty updateTrigger = new SimpleBooleanProperty(false);
    private JEVisDataSource ds;

    public MeterController(MeterPlugin plugin,JEVisDataSource ds) {
        this.ds = ds;
        this.plugin = plugin;
    }

//    public void loadActionView() {
//        nonconformityPlanList = FXCollections.observableArrayList();
//        nonconformityPlanList.addListener(new ListChangeListener<NonconformityPlan>() {
//            @Override
//            public void onChanged(Change<? extends NonconformityPlan> c) {
//                while (c.next()) {
//                    if (c.wasAdded()) {
//                        c.getAddedSubList().forEach(actionPlan -> {
//                            buildTabPane(actionPlan);
//                        });
//
//                    }
//                }
//
//
//            }
//        });
//        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            isOverviewTab.set(getActiveNonconformityPlan() instanceof NonconformtiesOverviewData);
//        });
//
//
//        AnchorPane.setBottomAnchor(tabPane, 0.0);
//        AnchorPane.setTopAnchor(tabPane, 0.0);
//        AnchorPane.setRightAnchor(tabPane, 0.0);
//        AnchorPane.setLeftAnchor(tabPane, 0.0);
//        contentPane.getChildren().add(tabPane);
//
//
//    }


//    private void buildTabPane(NonconformityPlan nonconformityPlanPlan) {
//
//        NonconformityPlanTable nonconformityPlanTable = new NonconformityPlanTable(nonconformityPlanPlan, nonconformityPlanPlan.getActionData(), updateTrigger);
//
//        //actionTable.enableSumRow(true);
//        NonconformityPlanTab tab = new NonconformityPlanTab(nonconformityPlanPlan, this, updateTrigger);
//        tab.setClosable(false);
//        tabPane.getTabs().add(tab);
//        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            logger.info("new tab selected: {}", newValue);
//
//            if (newValue instanceof NonconformityPlanTab) {
//                NonconformityPlan nonconformityPlan = ((NonconformityPlanTab) newValue).getNonconformityPlan();
//                //actionPlan.loadActionList();
//            }
//
//        });
//
//        nonconformityPlanTable.sort();
//
//        //actionTable.setItems(createTestData());
//
//    }

//    public void deletePlan() {
//        NonconformityPlanTab tab = getActiveTab();
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.deletetitle"));
//        alert.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.delete"));
//        Label text = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.content") + "\n" + getActiveNonconformityPlan().getName());
//        text.setWrapText(true);
//        alert.getDialogPane().setContent(text);
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == ButtonType.OK) {
//            try {
//                getActiveNonconformityPlan().delete();
//                nonconformityPlanList.remove(tab.getNonconformityPlan());
//                tabPane.getTabs().remove(tab);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//    }

//    public void createNewNonconformityPlan() {
//        NewNonconformitiesDialog newNonconformitiesDialog = new NewNonconformitiesDialog();
//        try {
//            NewNonconformitiesDialog.Response response = newNonconformitiesDialog.show(JEConfig.getStage(), plugin.getDataSource());
//            if (response == NewNonconformitiesDialog.Response.YES) {
//
//                JEVisClass nonconformityPlanClass = plugin.getDataSource().getJEVisClass(JC.NonconformitiesPlan.name);
//                JEVisObject parentDir = newNonconformitiesDialog.getParent();
//
//                JEVisObject newObject = parentDir.buildObject(newNonconformitiesDialog.getCreateName(), nonconformityPlanClass);
//                newObject.commit();
//                NonconformityPlan nonconformityPlan = new NonconformityPlan(newObject);
//                this.nonconformityPlanList.add(nonconformityPlan);
//                tabPane.getSelectionModel().selectLast();
//
//                DateTime now = new DateTime();
//                JEVisSample mediumAtt = newObject.getAttribute("Custom Medium").buildSample(now, "Strom;Gas;Wasser");
//                mediumAtt.commit();
//            }
//
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
//
//
//    }


//    public void deleteNonconformity() {
//        NonconformityPlanTab tab = getActiveTab();
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.deletetitle"));
//        alert.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.delete"));
//        Label text = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
//        text.setWrapText(true);
//        alert.getDialogPane().setContent(text);
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == ButtonType.OK) {
//            try {
//                getSelectedData().setDeleted(true);
//                getSelectedData().commit();
//                tab.getNonconformityPlan().removeNonconformity(tab.getActionTable().getSelectionModel().getSelectedItem());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }


//    public void createNonconformity() {
//        NonconformityPlanTab tab = getActiveTab();
//        try {
//            JEVisClass nonconformityPlanDirClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.NonconformitiesPlan.NonconformityPlanDirectory.Nonconformity.NonconformityDirectory.name);
//            JEVisClass nonconformityClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.NonconformitiesPlan.NonconformityPlanDirectory.Nonconformity.name);
//            JEVisObject nonconformityPlanDirObj = null;
//            if (getActiveNonconformityPlan().getObject().getChildren(nonconformityPlanDirClass, false).isEmpty()) {
//                nonconformityPlanDirObj = getActiveNonconformityPlan().getObject().buildObject(nonconformityPlanDirClass.getName(), nonconformityPlanDirClass);
//
//                nonconformityPlanDirObj.commit();
//            } else {
//                nonconformityPlanDirObj = getActiveNonconformityPlan().getObject().getChildren(nonconformityPlanDirClass, false).get(0);
//            }
//
//            int nextNonconformityNr = getActiveNonconformityPlan().getNextNonconformityNr();
//
//            JEVisObject nonconformityObject = nonconformityPlanDirObj.buildObject(String.valueOf(nextNonconformityNr), nonconformityClass);
//            nonconformityObject.commit();
//            NonconformityData newNonconformityData = new NonconformityData(nonconformityObject, tab.getNonconformityPlan());
//            newNonconformityData.commit();
//            newNonconformityData.nrProperty().set(nextNonconformityNr);
//            newNonconformityData.setCreator(getActiveNonconformityPlan().getObject().getDataSource().getCurrentUser().getFirstName() + " " + getActiveNonconformityPlan().getObject().getDataSource().getCurrentUser().getLastName());
//            tab.getNonconformityPlan().addAction(newNonconformityData);
//
//            tab.getActionTable().getSelectionModel().select(newNonconformityData);
//            openDataForm(true);//tab.getActionTable().getSelectionModel().getSelectedItem()
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
//    }

//    public void openPlanSettings() {
//        NonconfomityPlanForm nonconfomityPlanForm = new NonconfomityPlanForm(getActiveTab().getNonconformityPlan());
//        Optional<ButtonType> result = nonconfomityPlanForm.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            getActiveTab().getNonconformityPlan().commit();
//        }
//
//    }


    public void loadNonconformityPlans() {
        tabPane = new TabPane();
        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);
        try {
            JEVisClass measurementDirectory = plugin.getDataSource().getJEVisClass(JC.Directory.MeasurementDirectory.name);
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(measurementDirectory, false);

            //NonconformtiesOverviewData overviewData = new NonconformtiesOverviewData(this);
            //NonconformityPlanTab overviewTab = new NonconformityPlanTab(overviewData,this, updateTrigger);
            //overviewTab.setClosable(false);
            //tabPane.getTabs().add(0, overviewTab);


//            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            planObjs.forEach(jeVisObject -> {
                MeterPlan meterPlan = new MeterPlan(jeVisObject);
                meterPlan.loadMeterList();
                meterPlans.add(meterPlan);
                MeterPlanTab meterPlanTab = new MeterPlanTab(meterPlan, this, ds);
                tabPane.getTabs().add(meterPlanTab);
//                if (isFirstPlan.get()) plan.loadNonconformityList();
//                isFirstPlan.set(false);
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
        System.out.println(contentPane);
        System.out.println(contentPane.getChildren());
        return contentPane;
    }

//    public Node getContent() {
//        return contentPane;
//    }
//
//    public NonconformityPlanTab getActiveTab() {
//        NonconformityPlanTab tab = (NonconformityPlanTab) tabPane.getSelectionModel().getSelectedItem();
//        return tab;
//    }

//    public NonconformityPlan getActiveNonconformityPlan() {
//        return getActiveTab().getNonconformityPlan();
//    }
//
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

    public void openDataForm(Optional<MeterData> meterData) {
        MeterData data = meterData.orElse(getSelectedData());
        MeterForm meterForm = new MeterForm(data,ds);
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        meterForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) meterForm.getDialogPane().lookupButton(buttonTypeOne);
        btOk.setOnAction(actionEvent -> {
            meterForm.getNewSamples().values().forEach(jeVisSample -> {
                try {
                    jeVisSample.commit();

                } catch (JEVisException e) {
                    logger.error(e);
                }
            });
            if(meterData.isPresent()){
                loadNonconformityPlans();
            }else {
                getActiveTab().getMeterPlanTable().replaceItem(data);
                getActiveTab().getMeterPlanTable().sort();
                getActiveTab().getMeterPlanTable().refresh();
            }

            //getActiveTab().getMeterPlanTable().getItems().add(data);
        });

        meterForm.show();

    }

    public void addMeter(){
        NewMeterDialog newMeterDialog = new NewMeterDialog(ds,getActiveTab().getPlan());
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        newMeterDialog.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) newMeterDialog.getDialogPane().lookupButton(buttonTypeOne);

        btOk.setOnAction(actionEvent -> {

            try{

                JEVisObject parent = newMeterDialog.getParent().orElse(newMeterDialog.getMeterPlan().getJeVisObject());
                JEVisObject jeVisObject = parent.buildObject(newMeterDialog.getNameProperty(),newMeterDialog.getJeVisClassSingleSelectionModel().getSelectedItem().getJeVisClass());
                if(jeVisObject.isAllowedUnder(parent)){
                    jeVisObject.commit();
                    MeterData meterData = new MeterData(jeVisObject);
                    openDataForm(Optional.of(meterData));
                }
            }catch (Exception e){
                logger.error(e);
            }
        });


        newMeterDialog.showAndWait();



    }
}
