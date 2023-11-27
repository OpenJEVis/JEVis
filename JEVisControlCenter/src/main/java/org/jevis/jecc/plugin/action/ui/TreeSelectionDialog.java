package org.jevis.jecc.plugin.action.ui;


import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartPluginElements.tree.FilterableTreeItem;
import org.jevis.jecc.application.Chart.ChartPluginElements.tree.JEVisTreeView;
import org.jevis.jecc.application.Chart.ChartPluginElements.tree.TreeItemPredicate;
import org.jevis.jecc.application.Chart.ChartPluginElements.tree.TreeViewPath;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.methods.DataMethods;
import org.jevis.jecc.application.tools.DisabledItemsComboBox;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.tool.ScreenSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TreeSelectionDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(TreeSelectionDialog.class);
    public static List<String> allData = new ArrayList<>(Arrays.asList("Data", "Base Data"));
    public static List<String> allCleanData = new ArrayList<>(Arrays.asList("Data", "Clean Data", "Base Data", "Math Data", "Forecast Data"));
    public static List<String> alarms = new ArrayList<>(Collections.singletonList("Alarm Configuration"));
    public static List<String> analyses = new ArrayList<>(Collections.singletonList("Analysis"));
    public static List<String> calculations = new ArrayList<>(Arrays.asList("Calculation", "Input", "Output"));
    public static List<String> calendars = new ArrayList<>(Collections.singletonList("Custom Period"));
    public static List<String> dashboards = new ArrayList<>(Collections.singletonList("Dashboard Analysis"));
    public static List<String> dataSources = new ArrayList<>(Arrays.asList("Data Server", "Data Point", "Channel"));
    public static List<String> documents = new ArrayList<>(Collections.singletonList("Document"));
    public static List<String> meters = new ArrayList<>(Collections.singletonList("Measurement Instrument"));
    public static List<String> reports = new ArrayList<>(Arrays.asList("Report", "Report Link", "Report Attribute", "Report Period Configuration"));
    private final List<JEVisClass> allDataClasses = new ArrayList<>();
    private final List<JEVisClass> allAlarmClasses = new ArrayList<>();
    private final List<JEVisClass> allAnalysesClasses = new ArrayList<>();
    private final List<JEVisClass> allCalculationClasses = new ArrayList<>();
    private final List<JEVisClass> allCalendarClasses = new ArrayList<>();
    private final List<JEVisClass> allDashboardClasses = new ArrayList<>();
    private final List<JEVisClass> allDataSourceClasses = new ArrayList<>();
    private final List<JEVisClass> allDocumentClasses = new ArrayList<>();
    private final List<JEVisClass> allMeterClasses = new ArrayList<>();
    private final List<JEVisClass> allReportClasses = new ArrayList<>();

    private final JEVisTreeView treeView;
    private final TextField filterTextField = new TextField();
    public ButtonType buttonOK = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.APPLY);
    public ButtonType buttonCancel = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
    private final Response response = Response.CANCEL;

    public TreeSelectionDialog(JEVisDataSource ds, List<JEVisClass> classFilter, SelectionMode selectionMode, List<UserSelection> selection, boolean showAttributes) {
        super();
        initOwner(ControlCenter.getStage());
        setResizable(true);
        buildClasses(ds);
        setOnCloseRequest(event -> {
            logger.debug("why close?");
        });

        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setSpacing(8);

        setWidth(ScreenSize.fitScreenWidth(800));
        setHeight(ScreenSize.fitScreenHeight(600));

        treeView = new JEVisTreeView(ds, selectionMode, selection, showAttributes);

        Label filterLabel = new Label(I18n.getInstance().getString("searchbar.search"));
        VBox filterVBox = new VBox(filterLabel);
        filterVBox.setAlignment(Pos.CENTER);

        filterTextField.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        Label classFilterLabel = new Label(I18n.getInstance().getString("searchbar.filter"));
        VBox classVBox = new VBox(classFilterLabel);
        classVBox.setAlignment(Pos.CENTER);

        DisabledItemsComboBox<String> filterBox = new DisabledItemsComboBox<>();
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.nofilter"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.data"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.alarms"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.analyses"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.calculations"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.calendar"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.dashboards"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.datasources"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.documents"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.meters"));
        filterBox.getItems().add(I18n.getInstance().getString("tree.filter.reports"));

        filterBox.getSelectionModel().selectFirst();

        filterBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int i = newValue.intValue();
                if (i == 0) {
                    updateFilter(new ArrayList<>());
                } else if (i == 1) {
                    updateFilter(allDataClasses);
                } else if (i == 2) {
                    updateFilter(allAlarmClasses);
                } else if (i == 3) {
                    updateFilter(allAnalysesClasses);
                } else if (i == 4) {
                    updateFilter(allCalculationClasses);
                } else if (i == 5) {
                    updateFilter(allCalendarClasses);
                } else if (i == 6) {
                    updateFilter(allDashboardClasses);
                } else if (i == 7) {
                    updateFilter(allDataSourceClasses);
                } else if (i == 8) {
                    updateFilter(allDocumentClasses);
                } else if (i == 9) {
                    updateFilter(allMeterClasses);
                } else if (i == 10) {
                    updateFilter(allReportClasses);
                }
            }
        });

        updateFilter(classFilter);

        Button ok = new Button(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {

                boolean correctChoice = true;
                List<JEVisObject> incorrectObjects = new ArrayList<>();
                for (JEVisObject object : treeView.getSelectedObjects()) {
                    if (!classFilter.isEmpty() && !classFilter.contains(object.getJEVisClass())) {
                        correctChoice = false;
                        incorrectObjects.add(object);
                    }
                }

                if (correctChoice) {
                    setResult(buttonOK);
                    this.close();
                } else {
                    StringBuilder stringBuilder = new StringBuilder();

                    for (JEVisObject object : incorrectObjects) {
                        stringBuilder.append("\n").append(object.getName());
                    }
                    Alert selectionShow = new Alert(Alert.AlertType.ERROR, "Error: " + stringBuilder);
                    selectionShow.show();
                    setResult(buttonCancel);
                }

                this.hide();
            } catch (Exception e) {

            }
        });

        Button cancel = new Button(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event ->
        {
            this.setResult(buttonCancel);
            this.hide();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, filterVBox, filterTextField, classVBox, filterBox, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(new TreeViewPath(treeView), treeView, buttonBar);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        //box.setMinHeight(dialogContainer.getHeight() - 80);
        //box.setMinWidth(1024);
        //ButtonType buttonTypeOne = new ButtonType(I18n.getInstance().getString("plugin.action.form.save"), ButtonBar.ButtonData.APPLY);
        //ButtonType buttonTypeTwo = new ButtonType(I18n.getInstance().getString("plugin.action.form.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        //actionForm.getDialogPane().getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

        getDialogPane().setContent(box);

    }

    private static String getItemText(Cell<JEVisObject> cell) {
        return (DataMethods.getObjectName(cell.getItem()));
    }

    private void buildClasses(JEVisDataSource ds) {
        try {
            for (String className : allData) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allDataClasses.add(jeVisClass);
                allDataClasses.addAll(jeVisClass.getHeirs());

            }
            for (String className : alarms) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allAlarmClasses.add(jeVisClass);
                allAlarmClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : analyses) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allAnalysesClasses.add(jeVisClass);
                allAnalysesClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : calculations) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allCalculationClasses.add(jeVisClass);
                allCalculationClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : calendars) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allCalendarClasses.add(jeVisClass);
                allCalendarClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : dashboards) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allDashboardClasses.add(jeVisClass);
                allDashboardClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : dataSources) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allDataSourceClasses.add(jeVisClass);
                allDataSourceClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : documents) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allDocumentClasses.add(jeVisClass);
                allDocumentClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : meters) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allMeterClasses.add(jeVisClass);
                allMeterClasses.addAll(jeVisClass.getHeirs());
            }
            for (String className : reports) {
                JEVisClass jeVisClass = ds.getJEVisClass(className);
                allReportClasses.add(jeVisClass);
                allReportClasses.addAll(jeVisClass.getHeirs());
            }
        } catch (Exception e) {
            logger.error("Could not create class filter", e);
        }
    }

    private void updateFilter(List<JEVisClass> classFilter) {
        if (treeView.getRoot() instanceof FilterableTreeItem) {
            ((FilterableTreeItem) treeView.getRoot()).predicateProperty().unbind();
            ((FilterableTreeItem) treeView.getRoot()).predicateProperty().bind(Bindings.createObjectBinding(() -> {
                if ((filterTextField.getText() == null || filterTextField.getText().isEmpty()) && (classFilter.isEmpty()))
                    return null;
                else if ((filterTextField.getText() == null || filterTextField.getText().isEmpty()) && (!classFilter.isEmpty())) {
                    return TreeItemPredicate.create(jeVisTreeViewItem -> {
                        try {
                            return classFilter.contains(jeVisTreeViewItem.getObject().getJEVisClass());
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        return false;
                    });
                } else return TreeItemPredicate.create(jeVisTreeViewItem -> {
                    boolean containsName = jeVisTreeViewItem.getObject().getLocalName(I18n.getInstance().getLocale().getLanguage()).toLowerCase(I18n.getInstance().getLocale()).contains(filterTextField.getText().toLowerCase(I18n.getInstance().getLocale()));
                    boolean classFiltered = false;
                    try {
                        classFiltered = classFilter.contains(jeVisTreeViewItem.getObject().getJEVisClass());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return containsName && classFiltered;
                });
            }, filterTextField.textProperty()));
        }
    }

    public JEVisTreeView getTreeView() {
        return treeView;
    }

    public Response getResponse() {
        return response;
    }

    public List<UserSelection> getUserSelection() {
        return treeView.getUserSelection();
    }
}
