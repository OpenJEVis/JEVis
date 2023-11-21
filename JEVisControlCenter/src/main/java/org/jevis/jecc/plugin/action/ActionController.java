package org.jevis.jecc.plugin.action;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.action.data.ActionData;
import org.jevis.jecc.plugin.action.data.ActionPlanData;
import org.jevis.jecc.plugin.action.data.ActionPlanOverviewData;
import org.jevis.jecc.plugin.action.ui.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ActionController {
    private static final Logger logger = LogManager.getLogger(ActionController.class);
    private final ActionPlugin plugin;
    private final ScrollPane scrollPane = new ScrollPane();
    private final AnchorPane contentPane = new AnchorPane();
    private final TabPane tabPane = new TabPane();
    private final BooleanProperty isOverviewTab = new SimpleBooleanProperty(true);
    private ObservableList<ActionPlanData> actionPlans;
    private ObservableList<ActionPlanData> actionPlansFilters;
    private ObservableList<String> actionPlanNames;


    public ActionController(ActionPlugin plugin) {

        this.plugin = plugin;
        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);
    }

    public void loadActionView() {

        actionPlans = FXCollections.observableArrayList();
        actionPlanNames = FXCollections.observableArrayList();
        actionPlansFilters = FXCollections.observableArrayList();

        actionPlans.addListener(new ListChangeListener<ActionPlanData>() {
            @Override
            public void onChanged(Change<? extends ActionPlanData> c) {
                while (c.next()) {
                    //System.out.println("!!!!!! Controller: " + c);
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(actionPlan -> {
                            buildTabPane(actionPlan);
                            actionPlanNames.add(actionPlan.getName().get());
                        });

                    }
                }
            }
        });


        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            isOverviewTab.set(getActiveActionPlan() instanceof ActionPlanOverviewData);
        });

    }

    public void reload() {
        actionPlans.forEach(actionPlanData -> {
            try {
                ControlCenter.getDataSource().reloadObject(actionPlanData.getObject());
                ControlCenter.getDataSource().reloadAttribute(actionPlanData.getObject());

            } catch (Exception ex) {
                logger.error("Error while reloading ActionPlan", ex);
            }
        });

        try {
            JEVisClass actionClass = ControlCenter.getDataSource().getJEVisClass("Action");
            List<JEVisObject> actions = ControlCenter.getDataSource().getObjects(actionClass, true);
            actions.forEach(jeVisObject -> {
                try {
                    ControlCenter.getDataSource().reloadObject(jeVisObject);
                    ControlCenter.getDataSource().reloadAttribute(jeVisObject);
                } catch (Exception ex) {

                }
            });

        } catch (Exception exception) {

        }

        actionPlans.clear();
        tabPane.getTabs().clear();
        loadActionView();
        loadActionPlans();

    }


    private void buildTabPane(ActionPlanData plan) {
        ActionTab tab = new ActionTab(this, plan);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public ObservableList<ActionPlanData> getActionPlans() {
        return actionPlans;
    }

    public void deletePlan() {
        ActionTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.plan.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.plan.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.plan.content") + "\n=>" + getActiveActionPlan().getName().get());
        text.setWrapText(true);
        text.setTextFill(Color.web("#e45131"));
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getActiveActionPlan().delete();
                actionPlans.remove(tab.getActionPlan());
                tabPane.getTabs().remove(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void exportPDF() {
        ObservableList<ActionPlanData> toExport = FXCollections.observableArrayList();
        getTabPane().getTabs().forEach(tab -> {
            toExport.add(((ActionTab) tab).getActionPlan());
        });
        ExportDialog exportDialog = new ExportDialog(toExport);

        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        exportDialog.getDialogPane().getButtonTypes().setAll(buttonTypeTwo, buttonTypeOne);

        Optional<ButtonType> optional = exportDialog.showAndWait();
        if (optional.get() == buttonTypeOne) {
            ExcelExporter excelExporter = new ExcelExporter(this, exportDialog.getSelection());
        } else {

        }


    }

    public void createNewPlan() {
        NewActionDialog newActionDialog = new NewActionDialog();
        try {
            NewActionDialog.Response response = newActionDialog.show(ControlCenter.getStage(), plugin.getDataSource());
            if (response == NewActionDialog.Response.YES) {

                JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Action Plan v2");
                JEVisObject parentDir = newActionDialog.getParent();

                JEVisObject newObject = parentDir.buildObject(newActionDialog.getCreateName(), actionPlanClass);
                newObject.commit();
                ActionPlanData actionPlan = new ActionPlanData(newObject);

                actionPlan.setDefaultValues(Locale.GERMANY);//For now only German is Supportet
                actionPlan.commit();
                actionPlans.add(actionPlan);
                tabPane.getSelectionModel().selectLast();

            }

        } catch (Exception ex) {
            logger.error(ex);
        }


    }


    public void deleteAction() {
        ActionTab tab = getActiveTab();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.action.action.deletetitle"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.action.action.delete"));
        Label text = new Label(I18n.getInstance().getString("plugin.action.action.content") + "\n" + getSelectedData().nrProperty().get() + " " + getSelectedData().titleProperty().get());
        text.setWrapText(true);
        alert.getDialogPane().setContent(text);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                getSelectedData().delete();
                tab.getActionTable().filter();
                //tab.getActionPlan().removeAction(tab.getActionTable().getSelectionModel().getSelectedItem());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void createNewAction() {
        ActionTab tab = getActiveTab();
        try {
            JEVisClass actionDirClass = getActiveActionPlan().getObject().getDataSource().getJEVisClass("Action Directory");
            JEVisClass actionClass = getActiveActionPlan().getObject().getDataSource().getJEVisClass("Action");
            JEVisObject actionDirObj = null;
            if (getActiveActionPlan().getObject().getChildren(actionDirClass, false).isEmpty()) {
                actionDirObj = getActiveActionPlan().getObject().buildObject(actionDirClass.getName(), actionDirClass);
                actionDirObj.commit();
            } else {
                actionDirObj = getActiveActionPlan().getObject().getChildren(actionDirClass, false).get(0);
            }

            int nextNr = getActiveActionPlan().getNextActionNr();
            JEVisObject actionObject = actionDirObj.buildObject(nextNr + "", actionClass);
            actionObject.commit();
            ActionData newAction = new ActionData(tab.getActionPlan(), actionObject);
            newAction.nrProperty().set(nextNr);
            String userName = actionDirObj.getDataSource().getCurrentUser().getFirstName() + " " + actionDirObj.getDataSource().getCurrentUser().getLastName();
            if (userName.trim().isEmpty()) userName = actionDirObj.getDataSource().getCurrentUser().getFirstName();
            newAction.fromUser.set(userName);


            newAction.commit();
            tab.getActionPlan().addAction(newAction);
            tab.getActionTable().filter();
            tab.getActionTable().getSelectionModel().select(newAction);

            openDataForm();//tab.getActionTable().getSelectionModel().getSelectedItem()
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openPlanSettings() {
        ActionPlanForm actionPlanForm = new ActionPlanForm(getActiveTab().getActionPlan());
        Optional<ButtonType> result = actionPlanForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getActionPlan().commit();
        }

    }

    public void loadActionPlans() {
        try {
            //System.out.println("loadActionPlans.start()");
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Action Plan v2");
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            planObjs.forEach(jeVisObject -> {
                //System.out.println("loadActionPlans: " + jeVisObject.getID());
                ActionPlanData plan = new ActionPlanData(jeVisObject);
                plan.loadActionList();
                actionPlans.add(plan);
            });
            if (planObjs.size() > 1) {
                ActionPlanOverviewData overviewData = new ActionPlanOverviewData(this);
                ActionTab overviewTab = new ActionTab(this, overviewData);
                tabPane.getTabs().add(0, overviewTab);
                overviewData.updateData();
            }


            Platform.runLater(() -> tabPane.getSelectionModel().selectFirst());

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Done loading actionplans");
    }

    public Node getContent() {
        return contentPane;
    }

    public ActionTab getActiveTab() {
        ActionTab tab = (ActionTab) tabPane.getSelectionModel().getSelectedItem();
        return tab;
    }

    public ActionPlanData getActiveActionPlan() {
        if (getActiveTab() == null) {
            return null;
        }
        return getActiveTab().getActionPlan();
    }

    public ActionData getSelectedData() {
        ActionTab tab = getActiveTab();
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
        logger.debug("openDataForm()");
        ActionData data = getSelectedData();
        ActionForm actionForm = new ActionForm(getActiveActionPlan(), data);

        ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        actionForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);


        Optional<ButtonType> optional = actionForm.showAndWait();
        if (optional.get() == buttonTypeOne) {
            data.setNew(false);
            data.commit();
            getActiveTab().updateStatistics();
        } else {
            if (data.isNew()) {
                // data.getActionPlan().removeAction(data);
            } else {
                try {
                    int index = getActiveTab().getActionTable().getSelectionModel().getSelectedIndex();
                    getActiveActionPlan().reloadAction(data);
                    Platform.runLater(() -> {
                        getActiveTab().getActionTable().getSelectionModel().select(index);
                    });

                } catch (Exception ex) {
                    logger.error(ex, ex);
                }
            }

        }


    }

    public boolean isIsOverviewTab() {
        return isOverviewTab.get();
    }

    public BooleanProperty isOverviewTabProperty() {
        return isOverviewTab;
    }
}
