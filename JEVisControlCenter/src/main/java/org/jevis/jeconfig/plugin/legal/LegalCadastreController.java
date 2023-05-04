package org.jevis.jeconfig.plugin.legal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.legal.data.LegalCadastre;

import org.jevis.jeconfig.plugin.legal.data.LegalCastreOverviewData;
import org.jevis.jeconfig.plugin.legal.data.LegislationData;
import org.jevis.jeconfig.plugin.legal.ui.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class LegalCadastreController {
    private static final Logger logger = LogManager.getLogger(LegalCadastreController.class);

    private final LegalCatasdrePlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<LegalCadastre> legalCadastres;
    private TabPane tabPane = new TabPane();
    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);

    private BooleanProperty inAlarm = new SimpleBooleanProperty();

    public LegalCadastreController(LegalCatasdrePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {
        legalCadastres = FXCollections.observableArrayList();
        legalCadastres.addListener(new ListChangeListener<LegalCadastre>() {
            @Override
            public void onChanged(Change<? extends LegalCadastre> c) {
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
            isOverviewTab.set(getActiveNonconformityPlan() instanceof LegalCastreOverviewData);
            
        });


        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(LegalCadastre legalCadastre) {

        LegalCadastreTable legalCadastreTable = new LegalCadastreTable(legalCadastre, legalCadastre.getLegislationDataList());

        //actionTable.enableSumRow(true);
        LegalCadastreTab tab = new LegalCadastreTab(legalCadastre, this);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("new tab selected: {}", newValue);

            if (newValue instanceof LegalCadastreTab) {
                LegalCadastre nonconformityPlan = ((LegalCadastreTab) newValue).getLegalCadastre();
                //actionPlan.loadActionList();
            }

        });

        legalCadastreTable.sort();

        //actionTable.setItems(createTestData());

    }

    public void deletePlan() {
        LegalCadastreTab tab = getActiveTab();

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
                legalCadastres.remove(tab.getLegalCadastre());
                tabPane.getTabs().remove(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void createNewPlan() {
        NewlegalCadastreDialog newlegalCadastreDialog = new NewlegalCadastreDialog();
        try {
            NewlegalCadastreDialog.Response response = newlegalCadastreDialog.show(JEConfig.getStage(), plugin.getDataSource());
            if (response == NewlegalCadastreDialog.Response.YES) {

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass(JC.LegalCadastre.name);
                JEVisObject parentDir = newlegalCadastreDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newlegalCadastreDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                LegalCadastre nonconformityPlan = new LegalCadastre(newObject);
                this.legalCadastres.add(nonconformityPlan);
                tabPane.getSelectionModel().selectLast();

//                DateTime now = new DateTime();
////                JEVisSample mediumAtt = newObject.getAttribute("Custom Medium").buildSample(now, "Strom;Gas;Wasser");
//                mediumAtt.commit();
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


    }


    public void deleteItem() {
        LegalCadastreTab tab = getActiveTab();

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
                tab.getLegalCadastre().removeLegislation(tab.getLegalCadastreTable().getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void createItem() {
        LegalCadastreTab tab = getActiveTab();
        try {
            JEVisClass nonconformityPlanDirClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.LegalCadastre.LegalCadastreDirectory.name);
            JEVisClass nonconformityClass = getActiveNonconformityPlan().getObject().getDataSource().getJEVisClass(JC.LegalCadastre.LegalCadastreDirectory.Legislation.name);
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
            LegislationData legislationData = new LegislationData(nonconformityObject, tab.getLegalCadastre());
            legislationData.commit();
            legislationData.nrProperty().set(nextNonconformityNr);
            tab.getLegalCadastre().addLegislation(legislationData);

            tab.getLegalCadastreTable().getSelectionModel().select(legislationData);
            openDataForm(true);//tab.getActionTable().getSelectionModel().getSelectedItem()
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openPlanSettings() {
        LegalCadastreForm cadastreForm = new LegalCadastreForm(getActiveTab().getLegalCadastre());
        Optional<ButtonType> result = cadastreForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getLegalCadastre().commit();
        }

    }

    public void loadNonconformityPlans() {
        try {
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass(JC.LegalCadastre.name);
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            System.out.println(planObjs);

            LegalCastreOverviewData overviewData = new LegalCastreOverviewData(this);
            LegalCadastreTab overviewTab = new LegalCadastreTab(overviewData,this);
            overviewTab.setClosable(false);
            tabPane.getTabs().add(0, overviewTab);


            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            planObjs.forEach(jeVisObject -> {
                LegalCadastre plan = new LegalCadastre(jeVisObject);
                legalCadastres.add(plan);
                if (isFirstPlan.get()) plan.loadNonconformityList();
                isFirstPlan.set(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Node getContent() {
        return contentPane;
    }

    public LegalCadastreTab getActiveTab() {
        LegalCadastreTab tab = (LegalCadastreTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public LegalCadastre getActiveNonconformityPlan() {
        return getActiveTab().getLegalCadastre();
    }

    public LegislationData getSelectedData() {
        LegalCadastreTab tab = getActiveTab();
        if (getActiveTab().getLegalCadastreTable().getSelectionModel().getSelectedItem() != null) {
            return getActiveTab().getLegalCadastreTable().getSelectionModel().getSelectedItem();
        } else {
            if (tab.getLegalCadastreTable().getItems().isEmpty()) {
                {
                    return null;
                }
            }

            return tab.getLegalCadastreTable().getItems().get(0);
        }
    }

    public void openDataForm(boolean isNew) {
        LegislationForm legislationForm = new LegislationForm(getActiveNonconformityPlan());
        LegislationData data = getSelectedData();
        legislationForm.setData(data);
        legislationForm.setNew(isNew);
        legislationForm.setTitle(I18n.getInstance().getString("plugin.nonconformities.nonconformity.dialog.title"));
        legislationForm.setHeaderText(I18n.getInstance().getString("plugin.nonconformities.nonconformity.dialog.header"));
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.nonconformities.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.nonconformities.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        legislationForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) legislationForm.getDialogPane().lookupButton(buttonTypeOne);
        final Button btCancel = (Button) legislationForm.getDialogPane().lookupButton(buttonTypeTwo);
        btOk.setOnAction(actionEvent -> {
            try {
                data.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        });
        btCancel.addEventFilter(ActionEvent.ACTION, event -> {
            if (legislationForm.isNew()) {
                try {
                    data.getObject().delete();
                    getActiveTab().getLegalCadastre().getLegislationDataList().remove(data);

                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                reload(data);
            }
        });
        legislationForm.show();


    }

    private void reload(LegislationData legislationData) {
        try {
            legislationData = getActiveNonconformityPlan().loadNonconformties(legislationData.getObject());
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

    public ObservableList<LegalCadastre> getLegalCadastres() {
        return legalCadastres;
    }

    public void setLegalCadastres(ObservableList<LegalCadastre> legalCadastres) {
        this.legalCadastres = legalCadastres;
    }
}
