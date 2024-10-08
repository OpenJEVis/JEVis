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
import org.jevis.jeconfig.plugin.legal.data.IndexOfLegalProvisions;
import org.jevis.jeconfig.plugin.legal.data.ObligationData;
import org.jevis.jeconfig.plugin.legal.ui.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class LegalCadastreController {
    private static final Logger logger = LogManager.getLogger(LegalCadastreController.class);

    private final LegalCatasdrePlugin plugin;

    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private ObservableList<IndexOfLegalProvisions> indexOfLegalProvisions;
    private final TabPane tabPane = new TabPane();

    private final BooleanProperty updateTrigger = new SimpleBooleanProperty(false);
//    private BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);

    private final BooleanProperty inAlarm = new SimpleBooleanProperty();

    public LegalCadastreController(LegalCatasdrePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadIndexOfLegalProvisions() {
        indexOfLegalProvisions = FXCollections.observableArrayList();
        indexOfLegalProvisions.addListener(new ListChangeListener<IndexOfLegalProvisions>() {
            @Override
            public void onChanged(Change<? extends IndexOfLegalProvisions> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(indexOfLegalProvisions -> {
                            buildTabPane(indexOfLegalProvisions);
                        });

                    }
                }


            }
        });

        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(IndexOfLegalProvisions indexOfLegalProvisions) {

        IndexOfLegalProvisionsTable indexOfLegalProvisionsTable = new IndexOfLegalProvisionsTable(indexOfLegalProvisions, indexOfLegalProvisions.getLegislationDataList(), updateTrigger);

        //actionTable.enableSumRow(true);
        LegalCadastreTab tab = new LegalCadastreTab(indexOfLegalProvisions, this, updateTrigger);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("new tab selected: {}", newValue);

            if (newValue instanceof LegalCadastreTab) {
                IndexOfLegalProvisions legalPlan = ((LegalCadastreTab) newValue).getLegalCadastre();
                legalPlan.loadLegalList();
            }

        });

        indexOfLegalProvisionsTable.sort();

        //actionTable.setItems(createTestData());

    }

    public void deletePlan() {
        LegalCadastreTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.content") + "\n" + getActivePlan().getName());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getActivePlan().delete();
                indexOfLegalProvisions.remove(tab.getLegalCadastre());
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

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass(JC.IndexofLegalProvisions.name);
                JEVisObject parentDir = newlegalCadastreDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newlegalCadastreDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                IndexOfLegalProvisions legalPlan = new IndexOfLegalProvisions(newObject);
                this.indexOfLegalProvisions.add(legalPlan);
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
        alert.setTitle(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.obligation.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.obligation.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.indexoflegalprovisions.delete.obligation.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
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
            JEVisClass legalPlanDirClass = getActivePlan().getObject().getDataSource().getJEVisClass(JC.IndexofLegalProvisions.IndexofLegalProvionsDirectory.Obligation.ObligationDirectory.name);
            JEVisClass legalClass = getActivePlan().getObject().getDataSource().getJEVisClass(JC.IndexofLegalProvisions.IndexofLegalProvionsDirectory.Obligation.name);
            JEVisObject legalPlanDirObj = null;
            if (getActivePlan().getObject().getChildren(legalPlanDirClass, false).isEmpty()) {
                legalPlanDirObj = getActivePlan().getObject().buildObject(legalPlanDirClass.getName(), legalPlanDirClass);

                legalPlanDirObj.commit();
            } else {
                legalPlanDirObj = getActivePlan().getObject().getChildren(legalPlanDirClass, false).get(0);
            }

            int nextLegalNr = getActivePlan().getNextLegalNr();

            JEVisObject legalObject = legalPlanDirObj.buildObject(String.valueOf(nextLegalNr), legalClass);
            legalObject.commit();
            ObligationData obligationData = new ObligationData(legalObject, tab.getLegalCadastre());
            obligationData.commit();
            obligationData.nrProperty().set(nextLegalNr);
            tab.getLegalCadastre().addLegislation(obligationData);

            tab.getLegalCadastreTable().getSelectionModel().select(obligationData);
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

    public void loadLegalPlans() {
        try {
            JEVisClass indexOfLegalProvisionsClass = plugin.getDataSource().getJEVisClass(JC.IndexofLegalProvisions.name);
            List<JEVisObject> indexOfLegalProvisionPlans = plugin.getDataSource().getObjects(indexOfLegalProvisionsClass, true);


            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            indexOfLegalProvisionPlans.forEach(jeVisObject -> {
                IndexOfLegalProvisions plan = new IndexOfLegalProvisions(jeVisObject);
                indexOfLegalProvisions.add(plan);
                if (isFirstPlan.get()) plan.loadLegalList();
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

    public IndexOfLegalProvisions getActivePlan() {
        return getActiveTab().getLegalCadastre();
    }

    public ObligationData getSelectedData() {
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
        ObligationForm obligationForm = new ObligationForm(getActivePlan());
        ObligationData data = getSelectedData();
        obligationForm.setData(data);
        obligationForm.setNew(isNew);
        obligationForm.setTitle(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.dialog.title"));
        obligationForm.setHeaderText(I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.dialog.header"));
        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.indexoflegalprovisions.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        obligationForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
        final Button btOk = (Button) obligationForm.getDialogPane().lookupButton(buttonTypeOne);
        final Button btCancel = (Button) obligationForm.getDialogPane().lookupButton(buttonTypeTwo);
        btOk.setOnAction(actionEvent -> {
            try {
                data.commit();
                updateTrigger.set(!updateTrigger.get());

            } catch (Exception e) {
                logger.error(e);
            }
        });
        btCancel.addEventFilter(ActionEvent.ACTION, event -> {
            if (obligationForm.isNew()) {
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
        obligationForm.show();


    }

    private void reload(ObligationData obligationData) {
        try {
            obligationData = getActivePlan().loadObligations(obligationData.getObject());
        } catch (Exception e) {
            logger.error(e);
        }
    }


    public ObservableList<IndexOfLegalProvisions> getLegalCadastres() {
        return indexOfLegalProvisions;
    }

    public void setLegalCadastres(ObservableList<IndexOfLegalProvisions> indexOfLegalProvisions) {
        this.indexOfLegalProvisions = indexOfLegalProvisions;
    }
}
