package org.jevis.jeconfig.plugin.nonconformities;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.TableFilter;
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
    private ObservableList<Nonconformities> actionPlans;
    private TabPane tabPane = new TabPane();

    public NonconformitiesController(NonconformitiesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadActionView() {
        actionPlans = FXCollections.observableArrayList();
        actionPlans.addListener(new ListChangeListener<Nonconformities>() {
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

        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        contentPane.getChildren().add(tabPane);


    }


    private void buildTabPane(Nonconformities plan) {
        NonconformitiesTable nonconformitiesTable = new NonconformitiesTable(plan.getActionData());
        //actionTable.enableSumRow(true);
        NonconformitiesTab tab = new NonconformitiesTab(plan, nonconformitiesTable);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("new tab selected: {}", newValue);

            Nonconformities nonconformities = ((NonconformitiesTab) newValue).getNonconformities();
            nonconformities.loadNonconformityList();

        });

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        double maxListHeight = 100;


        Label lSuche = new Label("Suche");
        JFXTextField fsearch = new JFXTextField();
        fsearch.setPromptText("Suche nach...");

        org.jevis.jeconfig.plugin.action.ui.TagButton mediumButton = new org.jevis.jeconfig.plugin.action.ui.TagButton(I18n.getInstance().getString("plugin.action.filter.medium"), plan.getMediumTags(), plan.getMediumTags());

        ComboBox<String> datumBox = new ComboBox<>();
        datumBox.setItems(FXCollections.observableArrayList("Umsetzung", "Abgeschlossen", "Erstellt"));
        datumBox.getSelectionModel().selectFirst();
        JFXTextField filterDatumText = new JFXTextField();
        filterDatumText.setPromptText("Datum...");
        ComboBox<String> comparatorBox = new ComboBox<>();
        comparatorBox.setItems(FXCollections.observableArrayList(">", "<", "="));
        comparatorBox.getSelectionModel().selectFirst();

        fsearch.textProperty().addListener((observable, oldValue, newValue) -> {
            nonconformitiesTable.setTextFilter(newValue);
            nonconformitiesTable.filter();
        });

        TimeFilterSelector dateSelector = new TimeFilterSelector(plan);

        dateSelector.getValuePropertyProperty().addListener(new ChangeListener<DateFilter>() {
            @Override
            public void changed(ObservableValue<? extends DateFilter> observableValue, DateFilter dateFilter, DateFilter t1) {
                nonconformitiesTable.setDateFilter(t1);
                nonconformitiesTable.filter();
            }
        });


        Separator vSep1 = new Separator(Orientation.VERTICAL);
        gridPane.addColumn(0, lSuche, fsearch);
        gridPane.addColumn(1, vSep1);
        gridPane.addColumn(2, new Label("Zeitbereich"), dateSelector);

        gridPane.addColumn(3, new Region(), mediumButton);


        //HBox hBox = new HBox(filterDatumText, comparatorBox, datumBox);
        EventHandler<ActionEvent> dateFilerEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (comparatorBox.getSelectionModel().getSelectedItem().equals(">")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.BIGGER_THAN);
                } else if (comparatorBox.getSelectionModel().getSelectedItem().equals("<")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.SMALLER_THAN);
                } else if (comparatorBox.getSelectionModel().getSelectedItem().equals("=")) {
                    nonconformitiesTable.getTableFilter().setPlannedDateComp(TableFilter.DATE_COMPARE.EQUALS);
                }
                nonconformitiesTable.getTableFilter().setPlannedDateFilter(filterDatumText.getText());
                //nonconformitiesTable.filter();

            }
        };

        comparatorBox.setOnAction(dateFilerEvent);





        //gridPane.add(hBox, 0, 1, 3, 1);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(gridPane);
        borderPane.setCenter(nonconformitiesTable);

        nonconformitiesTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    openDataForm();//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });
        tab.setContent(borderPane);
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
                actionPlans.remove(tab.getNonconformities());
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
                actionPlans.add(nonconformities);
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
                getSelectedData().delete();
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
        NonconformitiesPlanForm nonconformitiesPlanForm = new NonconformitiesPlanForm(getActiveTab().getNonconformities());
        Optional<ButtonType> result = nonconformitiesPlanForm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            getActiveTab().getNonconformities().commit();
        }

    }

    public void loadActionPlans() {
        try {
            JEVisClass actionPlanClass = plugin.getDataSource().getJEVisClass("Nonconformities");
            List<JEVisObject> planObjs = plugin.getDataSource().getObjects(actionPlanClass, true);

            AtomicBoolean isFirstPlan = new AtomicBoolean(true);

            planObjs.forEach(jeVisObject -> {
                Nonconformities plan = new Nonconformities(jeVisObject);
                actionPlans.add(plan);
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
        btOk.addEventFilter(ActionEvent.ACTION,getCloseRequest(data));
        btCancel.setOnAction(actionEvent -> {
            data.reload();
        });
        nonconformityForm.show();




    }
    @NotNull
    private static EventHandler getCloseRequest(NonconformityData data) {
        return dialogEvent -> {
            String errorText = data.checkForRequirements();
            if (errorText != NonconformityData.REQUIREMENTS_MET) {
                dialogEvent.consume();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Failed to Save Nonconformity");
                alert.setHeaderText(errorText);
                //alert.setContentText("Connect to the database successfully!");

                alert.showAndWait();
                dialogEvent.consume();

            }else {
                data.commit();
            }
        };
    }


}
