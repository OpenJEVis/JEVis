package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.*;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReportWizardDialog extends Dialog<ButtonType> {
    private static final Logger logger = LogManager.getLogger(ReportWizardDialog.class);
    private JEVisDataSource ds;
    private JEVisObject reportLinkDirectory;
    private JEVisObject emailNotification;
    private List<UserSelection> selections;
    private List<ReportLink> reportLinkList = new ArrayList<>();
    private int row;

    public ReportWizardDialog(JEVisObject newObject) {
        try {
            ds = newObject.getDataSource();

            JEVisClass reportLinksDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass emailNotificationClass = ds.getJEVisClass("E-Mail Notification");

            reportLinkDirectory = newObject.buildObject("Report Link Directory", reportLinksDirectoryClass);
            reportLinkDirectory.commit();
            emailNotification = newObject.buildObject("E-Mail Notification", emailNotificationClass);
            emailNotification.commit();
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        init();
    }

    private void init() {
        VBox vBox = new VBox();
        HBox hbox = new HBox();
        Button addButton = new Button("Add");

        this.setTitle(I18n.getInstance().getString("graph.dialog.note"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);

        row = 0;

        if (reportLinkList.isEmpty()) {
            reportLinkList.add(new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("", PeriodMode.CURRENT))));
        }

        for (ReportLink reportLink : reportLinkList) {
            Label reportVariable = new Label("Report Variables");
            gridPane.add(reportVariable, 0, row);
            gridPane.add(createBox(reportLink), 1, row);
            row++;
        }

        addButton.setOnAction(event -> {
            ReportLink reportLink = new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("", PeriodMode.CURRENT)));
            reportLinkList.add(reportLink);
            Label reportVariable = new Label("Report Variables");
            gridPane.add(reportVariable, 0, row);
            gridPane.add(createBox(reportLink), 1, row);
            row++;
        });

        hbox.getChildren().add(gridPane);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(ok, cancel);
        vBox.getChildren().add(hbox);
        vBox.getChildren().add(addButton);
        this.getDialogPane().setContent(vBox);

        this.setResizable(true);
        this.initOwner(JEConfig.getStage());

        this.getDialogPane().setPrefWidth(1220);
    }

    private Node createBox(ReportLink reportLink) {
        HBox hBox = new HBox();
        Button targetsButton = new Button("Select Target");
        ComboBox<String> aggregationPeriodComboBox = new ComboBox<String>(FXCollections.observableArrayList(ReportAggregation.values()));
        ComboBox<PeriodMode> periodModeComboBox = new ComboBox<>(FXCollections.observableArrayList(PeriodMode.values()));
        AtomicReference<String> targetString = new AtomicReference<>();
        if (reportLink.getjEVisID() != null) {
            targetString.set(reportLink.getjEVisID().toString());
        }

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllObjects();
        JEVisTreeFilter allAttributeFilter = SelectTargetDialog.buildAllAttributesFilter();
        allFilter.add(basicFilter);
        allFilter.add(allAttributeFilter);

        SelectTargetDialog selectionDialog = new SelectTargetDialog(allFilter, basicFilter, null, SelectionMode.SINGLE);

        targetsButton.setOnAction(event -> {
            TargetHelper th = null;
            if (targetString.get() != null) {
                th = new TargetHelper(ds, targetString.get());
                if (th.isValid() && th.targetAccessible()) {
                    logger.info("Target Is valid");
                    setButtonText(targetString.get(), targetsButton);
                }
            }

            List<UserSelection> openList = new ArrayList<>();
            if (th != null && !th.getAttribute().isEmpty()) {
                for (JEVisAttribute att : th.getAttribute())
                    openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
            } else if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject())
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
            }

            if (selectionDialog.show(
                    ds,
                    I18n.getInstance().getString("dialog.target.data.title"),
                    openList
            ) == SelectTargetDialog.Response.OK) {
                logger.trace("Selection Done");

                String newTarget = "";
                selections = selectionDialog.getUserSelection();
                for (UserSelection us : selections) {
                    int index = selections.indexOf(us);
                    if (index > 0) newTarget += ";";

                    newTarget += us.getSelectedObject().getID();
                    reportLink.setjEVisID(us.getSelectedObject().getID());
                    try {
                        reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(us.getSelectedObject()));
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    ReportAttribute reportAttribute = reportLink.getReportAttribute();
                    if (us.getSelectedAttribute() != null) {
                        reportAttribute.setAttributeName(us.getSelectedAttribute().getName());
                    } else {
                        reportAttribute.setAttributeName("Value");
                    }

                    if (us.getSelectedAttribute() != null) {
                        newTarget += ":" + us.getSelectedAttribute().getName();
                    } else {
                        newTarget += ":Value";
                    }
                }
                targetString.set(newTarget);
            }
            setButtonText(targetString.get(), targetsButton);
        });

        aggregationPeriodComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                ReportPeriodConfiguration reportPeriodConfiguration = reportLink.getReportAttribute().getReportPeriodConfiguration();
                reportPeriodConfiguration.setReportAggregation(newValue);
            }
        });

        periodModeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                ReportPeriodConfiguration reportPeriodConfiguration = reportLink.getReportAttribute().getReportPeriodConfiguration();
                reportPeriodConfiguration.setPeriodMode(newValue);
            }
        });

        hBox.getChildren().addAll(targetsButton, aggregationPeriodComboBox, periodModeComboBox);

        return hBox;
    }


    private void setButtonText(String targetString, Button targetsButton) {
        TargetHelper th;
        try {
            if (targetString != null) {
                th = new TargetHelper(ds, targetString);
            } else {
                th = new TargetHelper(ds, targetString);
            }

            if (th.isValid() && th.targetAccessible()) {

                StringBuilder bText = new StringBuilder();

                JEVisClass cleanData = ds.getJEVisClass("Clean Data");

                for (JEVisObject obj : th.getObject()) {
                    int index = th.getObject().indexOf(obj);
                    if (index > 0) bText.append("; ");

                    if (obj.getJEVisClass().equals(cleanData)) {
                        List<JEVisObject> parents = obj.getParents();
                        if (!parents.isEmpty()) {
                            for (JEVisObject parent : parents) {
                                bText.append("[");
                                bText.append(parent.getID());
                                bText.append("] ");
                                bText.append(parent.getName());
                                bText.append(" / ");
                            }
                        }
                    }

                    bText.append("[");
                    bText.append(obj.getID());
                    bText.append("] ");
                    bText.append(obj.getName());

                    if (th.hasAttribute()) {

                        bText.append(" - ");
                        bText.append(th.getAttribute().get(index).getName());

                    }
                }

                Platform.runLater(() -> targetsButton.setText(bText.toString()));
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    public List<UserSelection> getSelections() {
        return selections;
    }

    public JEVisObject getReportLinkDirectory() {
        return reportLinkDirectory;
    }

    public List<ReportLink> getReportLinkList() {
        return reportLinkList;
    }
}
