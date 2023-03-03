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
import org.jevis.jeconfig.plugin.action.data.ActionPlanOverviewData;
import org.jevis.jeconfig.plugin.action.ui.ActionPlanForm;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;
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
    private ObservableList<Nonconformities> nonconformitiesList;
    private TabPane tabPane = new TabPane();
    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);

    public NonconformitiesController(NonconformitiesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {
        nonconformitiesList = FXCollections.observableArrayList();
        nonconformitiesList.addListener(new ListChangeListener<Nonconformities>() {
            @Override
            public void onChanged(Change<? extends Nonconformities> c) {
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
            isOverviewTab.set(getActiveNonconformities() instanceof NonconformtiesOverviewData);
        });


        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(Nonconformities nonconformitiesPlan) {

        NonconformitiesTable nonconformitiesTable = new NonconformitiesTable(nonconformitiesPlan,nonconformitiesPlan.getActionData());

        //actionTable.enableSumRow(true);
        NonconformitiesTab tab = new NonconformitiesTab(nonconformitiesPlan, this);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("new tab selected: {}", newValue);

            if (newValue instanceof NonconformitiesTab) {
                Nonconformities nonconformities = ((NonconformitiesTab) newValue).getNonconformities();
                //actionPlan.loadActionList();
            }

        });


        //actionTable.setItems(createTestData());

    }

    public void deletePlan() {
        NonconformitiesTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.plan.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.plan.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.plan.content") + "\n" + getActiveNonconformities().getName());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getActiveNonconformities().delete();
                nonconformitiesList.remove(tab.getNonconformities());
                tabPane.getTabs().remove(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void createNewNonconformities() {
        NewNonconformitiesDialog newNonconformitiesDialog = new NewNonconformitiesDialog();
        try {
            NewNonconformitiesDialog.Response response = newNonconformitiesDialog.show(JEConfig.getStage(), plugin.getDataSource());
            if (response == NewNonconformitiesDialog.Response.YES) {

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass(JC.Nonconformities.name);
                JEVisObject parentDir = newNonconformitiesDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newNonconformitiesDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                Nonconformities nonconformities = new Nonconformities(newObject);
                this.nonconformitiesList.add(nonconformities);
                tabPane.getSelectionModel().selectLast();

                DateTime now = new DateTime();
                JEVisSample statusAtt = newObject.getAttribute("Custom Status").buildSample(now, "Offen;Geschlosse");
                JEVisSample fieldsAtt = newObject.getAttribute("Custom Fields").buildSample(now, "Büro,Lager,Produktion");
                JEVisSample mediumAtt = newObject.getAttribute("Custom Medium").buildSample(now, "Strom;Gas;Wasser");
                statusAtt.commit();
                fieldsAtt.commit();
                mediumAtt.commit();
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


    }


    public void deleteNonconformity() {
        NonconformitiesTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.action.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.action.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.action.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getSelectedData().setDeleted(true);
                getSelectedData().commit();
                tab.getNonconformities().removeNonconformity(tab.getActionTable().getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void createNonconformity() {
        NonconformitiesTab tab = getActiveTab();
        try {
            JEVisClass nonconformitiesDirClass = getActiveNonconformities().getObject().getDataSource().getJEVisClass(JC.Nonconformities.NonconformitiesDirectory.name);
            JEVisClass nonconformityClass = getActiveNonconformities().getObject().getDataSource().getJEVisClass(JC.Nonconformities.NonconformitiesDirectory.Nonconformity.name);
            JEVisObject nonconformitiesDirObj = null;
            if (getActiveNonconformities().getObject().getChildren(nonconformitiesDirClass, false).isEmpty()) {
                nonconformitiesDirObj = getActiveNonconformities().getObject().buildObject(nonconformitiesDirClass.getName(), nonconformitiesDirClass);

                nonconformitiesDirObj.commit();
            } else {
                nonconformitiesDirObj = getActiveNonconformities().getObject().getChildren(nonconformitiesDirClass, false).get(0);
            }

            int nextNonconformityNr = getActiveNonconformities().getNextNonconformityNr();

            JEVisObject nonconformityObject = nonconformitiesDirObj.buildObject(String.valueOf(nextNonconformityNr), nonconformityClass);
            nonconformityObject.commit();
            NonconformityData newNonconformityData = new NonconformityData(nonconformityObject, tab.getNonconformities());
            newNonconformityData.commit();
            newNonconformityData.nrProperty().set(nextNonconformityNr);
            newNonconformityData.setCreator(getActiveNonconformities().getObject().getDataSource().getCurrentUser().getFirstName() + " " + getActiveNonconformities().getObject().getDataSource().getCurrentUser().getLastName());
            tab.getNonconformities().addAction(newNonconformityData);

            tab.getActionTable().getSelectionModel().select(newNonconformityData);
            openDataForm();//tab.getActionTable().getSelectionModel().getSelectedItem()
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openPlanSettings() {
        NonconfomitiesForm nonconfomitiesForm = new NonconfomitiesForm(getActiveTab().getNonconformities());
        Optional<ButtonType> result = nonconfomitiesForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getNonconformities().commit();
        }

    }

    public void loadActionPlans() {
        try {
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Nonconformities");
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            NonconformtiesOverviewData overviewData = new NonconformtiesOverviewData(this);
            NonconformitiesTab overviewTab = new NonconformitiesTab(overviewData,this);
            tabPane.getTabs().add(0, overviewTab);


            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            planObjs.forEach(jeVisObject -> {
                Nonconformities plan = new Nonconformities(jeVisObject);
                nonconformitiesList.add(plan);
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

    public NonconformitiesTab getActiveTab() {
        NonconformitiesTab tab = (NonconformitiesTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public Nonconformities getActiveNonconformities() {
        return getActiveTab().getNonconformities();
    }

    public NonconformityData getSelectedData() {
        NonconformitiesTab tab = getActiveTab();
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
        NonconformityForm nonconformityForm = new NonconformityForm(getActiveNonconformities());
        NonconformityData data = getSelectedData();
        nonconformityForm.setData(data);
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


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
            nonconformityData = getActiveNonconformities().loadNonconformties(nonconformityData.getObject());
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

    public ObservableList<Nonconformities> getNonconformitiesList() {
        return nonconformitiesList;
    }

    public void setNonconformitiesList(ObservableList<Nonconformities> nonconformitiesList) {
        this.nonconformitiesList = nonconformitiesList;
    }
}
