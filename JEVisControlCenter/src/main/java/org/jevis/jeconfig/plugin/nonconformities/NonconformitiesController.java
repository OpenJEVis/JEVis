package org.jevis.jeconfig.plugin.nonconformities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityPlan;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformtiesOverviewData;
import org.jevis.jeconfig.plugin.nonconformities.ui.*;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class NonconformitiesController {
    private static final Logger logger = LogManager.getLogger(NonconformitiesController.class);

    private final NonconformitiesPlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<NonconformityPlan> nonconformityPlanList;
    private TabPane tabPane = new TabPane();
    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);

    public NonconformitiesController(NonconformitiesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {
        nonconformityPlanList = FXCollections.observableArrayList();
        nonconformityPlanList.addListener(new ListChangeListener<NonconformityPlan>() {
            @Override
            public void onChanged(Change<? extends NonconformityPlan> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(actionPlan -> {
                            buildTabPane(actionPlan);
                        });

                    }
                }


            }
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            isOverviewTab.set(getActiveNonconformityPlan() instanceof NonconformtiesOverviewData);
        });


        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(NonconformityPlan nonconformityPlanPlan) {

        NonconformityPlanTable nonconformityPlanTable = new NonconformityPlanTable(nonconformityPlanPlan, nonconformityPlanPlan.getActionData());

        //actionTable.enableSumRow(true);
        NonconformityPlanTab tab = new NonconformityPlanTab(nonconformityPlanPlan, this);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("new tab selected: {}", newValue);

            if (newValue instanceof NonconformityPlanTab) {
                NonconformityPlan nonconformityPlan = ((NonconformityPlanTab) newValue).getNonconformityPlan();
                //actionPlan.loadActionList();
            }

        });


        //actionTable.setItems(createTestData());

    }

    public void deletePlan() {
        NonconformityPlanTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformityPlan.content") + "\n" + getActiveNonconformityPlan().getName());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getActiveNonconformityPlan().delete();
                nonconformityPlanList.remove(tab.getNonconformityPlan());
                tabPane.getTabs().remove(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void createNewNonconformityPlan() {
        NewNonconformitiesDialog newNonconformitiesDialog = new NewNonconformitiesDialog();
        try {
            NewNonconformitiesDialog.Response response = newNonconformitiesDialog.show(JEConfig.getStage(), plugin.getDataSource());
            if (response == NewNonconformitiesDialog.Response.YES) {

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass(JC.Nonconformities.name);
                JEVisObject parentDir = newNonconformitiesDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newNonconformitiesDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                NonconformityPlan nonconformityPlan = new NonconformityPlan(newObject);
                this.nonconformityPlanList.add(nonconformityPlan);
                tabPane.getSelectionModel().selectLast();

                DateTime now = new DateTime();
                JEVisSample mediumAtt = newObject.getAttribute("Custom Medium").buildSample(now, "Strom;Gas;Wasser");
                mediumAtt.commit();
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


    }


    public void deleteNonconformity() {
        NonconformityPlanTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getSelectedData().setDeleted(true);
                getSelectedData().commit();
                tab.getNonconformityPlan().removeNonconformity(tab.getActionTable().getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void createNonconformity() {
        NonconformityPlanTab tab = getActiveTab();
        try {
            JEVisClass nonconformityPlanDirClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.Nonconformities.NonconformitiesDirectory.name);
            JEVisClass nonconformityClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.Nonconformities.NonconformitiesDirectory.Nonconformity.name);
            JEVisObject nonconformityPlanDirObj = null;
            if (getActiveNonconformityPlan().getObject().getChildren(nonconformityPlanDirClass, false).isEmpty()) {
                nonconformityPlanDirObj = getActiveNonconformityPlan().getObject().buildObject(nonconformityPlanDirClass.getName(), nonconformityPlanDirClass);

                nonconformityPlanDirObj.commit();
            } else {
                nonconformityPlanDirObj = getActiveNonconformityPlan().getObject().getChildren(nonconformityPlanDirClass, false).get(0);
            }

            int nextNonconformityNr = getActiveNonconformityPlan().getNextNonconformityNr();

            JEVisObject nonconformityObject = nonconformityPlanDirObj.buildObject(String.valueOf(nextNonconformityNr), nonconformityClass);
            nonconformityObject.commit();
            NonconformityData newNonconformityData = new NonconformityData(nonconformityObject, tab.getNonconformityPlan());
            newNonconformityData.commit();
            newNonconformityData.nrProperty().set(nextNonconformityNr);
            newNonconformityData.setCreator(getActiveNonconformityPlan().getObject().getDataSource().getCurrentUser().getFirstName() + " " + getActiveNonconformityPlan().getObject().getDataSource().getCurrentUser().getLastName());
            tab.getNonconformityPlan().addAction(newNonconformityData);

            tab.getActionTable().getSelectionModel().select(newNonconformityData);
            openDataForm();//tab.getActionTable().getSelectionModel().getSelectedItem()
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openPlanSettings() {
        NonconfomityPlanForm nonconfomityPlanForm = new NonconfomityPlanForm(getActiveTab().getNonconformityPlan());
        Optional<ButtonType> result = nonconfomityPlanForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getNonconformityPlan().commit();
        }

    }

    public void loadNonconformityPlans() {
        try {
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("NonconformityPlan");
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            NonconformtiesOverviewData overviewData = new NonconformtiesOverviewData(this);
            NonconformityPlanTab overviewTab = new NonconformityPlanTab(overviewData,this);
            tabPane.getTabs().add(0, overviewTab);


            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            planObjs.forEach(jeVisObject -> {
                NonconformityPlan plan = new NonconformityPlan(jeVisObject);
                nonconformityPlanList.add(plan);
                if (isFirstPlan.get()) plan.loadNonconformityList();
                isFirstPlan.set(false);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Node getContent() {
        return contentPane;
    }

    public NonconformityPlanTab getActiveTab() {
        NonconformityPlanTab tab = (NonconformityPlanTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public NonconformityPlan getActiveNonconformityPlan() {
        return getActiveTab().getNonconformityPlan();
    }

    public NonconformityData getSelectedData() {
        NonconformityPlanTab tab = getActiveTab();
        if (getActiveTab().getActionTable().getSelectionModel().getSelectedItem() != null) {
            return getActiveTab().getActionTable().getSelectionModel().getSelectedItem();
        } else {
            if (tab.getActionTable().getItems().isEmpty()) {
                {
                    return null;
                }
            }

            return tab.getActionTable().getItems().get(0);
        }
    }

    public void openDataForm() {
        NonconformityForm nonconformityForm = new NonconformityForm(getActiveNonconformityPlan());
        NonconformityData data = getSelectedData();
        nonconformityForm.setData(data);
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.nonconformities.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.nonconformities.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        nonconformityForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) nonconformityForm.getDialogPane().lookupButton(buttonTypeOne);
        final Button btCancel = (Button) nonconformityForm.getDialogPane().lookupButton(buttonTypeTwo);
        btOk.addEventFilter(ActionEvent.ACTION,getCloseRequest(data, nonconformityForm));
        btCancel.addEventFilter(ActionEvent.ACTION,event -> {
            reload(data);
        });
        nonconformityForm.show();




    }
    @NotNull
    private static EventHandler getCloseRequest(NonconformityData data, NonconformityForm nonconformityForm) {
        return dialogEvent -> {
            String errorText = data.checkForRequirements();
            if (errorText != NonconformityData.REQUIREMENTS_MET) {
                nonconformityForm.showNotification(errorText,Icon.Warning);
                dialogEvent.consume();

            }else {
                data.commit();
            }
        };
    }

    private void reload(NonconformityData nonconformityData) {
        try {
            nonconformityData = getActiveNonconformityPlan().loadNonconformties(nonconformityData.getObject());
        } catch (Exception e) {
           logger.error(e);
        }
    }


    public boolean isIsOverviewTab() {
        return isOverviewTab.get();
    }

    public BooleanProperty isOverviewTabProperty() {
        return isOverviewTab;
    }

    public void setIsOverviewTab(boolean isOverviewTab) {
        this.isOverviewTab.set(isOverviewTab);
    }

    public ObservableList<NonconformityPlan> getNonconformityPlanList() {
        return nonconformityPlanList;
    }

    public void setNonconformityPlanList(ObservableList<NonconformityPlan> nonconformityPlanList) {
        this.nonconformityPlanList = nonconformityPlanList;
    }
}
