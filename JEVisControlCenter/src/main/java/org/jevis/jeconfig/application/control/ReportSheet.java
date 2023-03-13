package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.util.CellAddress;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.PeriodMode;
import org.jevis.commons.report.ReportAttribute;
import org.jevis.commons.report.ReportLink;
import org.jevis.commons.report.ReportType;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.dialog.ReportWizardDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportSheet extends Tab {
    private static final Logger logger = LogManager.getLogger(ReportSheet.class);
    private final Image imgMarkAll = ResourceLoader.getImage("jetxee-check-sign-and-cross-sign-3.png");
    private final Tooltip tooltipMarkAll = new Tooltip(I18n.getInstance().getString("plugin.graph.dialog.changesettings.tooltip.forall"));
    private final GridPane gridPane = new GridPane();
    private final JEVisDataSource ds;
    private final ReportWizardDialog reportWizardDialog;
    private final List<ReportLink> reportLinks = new ArrayList<>();
    private int row = 1;

    public ReportSheet(JEVisDataSource ds, ReportWizardDialog reportWizardDialog) {
        super();
        this.ds = ds;
        this.reportWizardDialog = reportWizardDialog;
        this.setClosable(false);

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(4);

        initHeader();

        ScrollPane scrollPane = new ScrollPane(gridPane);
        double width = reportWizardDialog.getDialogContainer().getWidth() * 2 / 3;
        double height = reportWizardDialog.getDialogContainer().getHeight() * 2 / 3;
        scrollPane.setMinWidth(width);
        scrollPane.setMinHeight(height);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        setContent(scrollPane);
    }

    private void initHeader() {
        gridPane.getChildren().clear();
        Label reportExcelCell = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.excelcolumn"));
        Label reportVariableLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.reportlink"));
        Label aggregationLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.aggregation"));
        Label manipulationLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.manipulation"));
        Label periodLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.period"));
        Label optionalLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.optional"));

        Platform.runLater(() -> {
            gridPane.add(reportExcelCell, 0, 0);
            gridPane.add(reportVariableLabel, 1, 0);
            gridPane.add(aggregationLabel, 2, 0);
            gridPane.add(manipulationLabel, 4, 0);
            gridPane.add(periodLabel, 6, 0);
            gridPane.add(optionalLabel, 8, 0);
        });

        row = 1;
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public void updateGridPane(Map<CellAddress, JEVisObject> cellAddressJEVisObjectMap) {
        initHeader();

        if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && cellAddressJEVisObjectMap != null) {
            for (Map.Entry<CellAddress, JEVisObject> dataSet : cellAddressJEVisObjectMap.entrySet()) {
                ReportLink reportLink = buildReportLink(dataSet.getValue(), this.getText(), dataSet.getKey());
                if (reportLink != null) {
                    reportLinks.add(reportLink);
                }
            }
        }

        for (ReportLink reportLink : reportLinks) {
            createBox(reportLink);
            row++;
        }
    }

    private void createBox(ReportLink reportLink) {
        Label excelCellLabel = new Label();
        StringBuilder stringBuilder = new StringBuilder();
        boolean disable = false;
        try {
            disable = ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.StringData.name);
        } catch (Exception e) {
            logger.error("Could not get ", e);
        }

        if (reportLink.getSheet() != null) {
            stringBuilder.append(reportLink.getSheet());
            stringBuilder.append(" - ");
        }
        if (reportLink.getCellAddress() != null) {
            Pattern pattern = Pattern.compile("\\D+");
            Matcher matcher = pattern.matcher(reportLink.getCellAddress().formatAsString());
            if (matcher.find()) {
                stringBuilder.append(matcher.group());
                excelCellLabel.setPrefWidth(100d);
            }
        }
        excelCellLabel.setText(stringBuilder.toString());
        int currentRow = row;
        JFXButton targetsButton = new JFXButton("Select Target");
        ReportAggregationBox aggregationPeriodComboBox = new ReportAggregationBox();
        aggregationPeriodComboBox.setDisable(disable);
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation() != null) {
            aggregationPeriodComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
        }

        ReportManipulationBox manipulationComboBox = new ReportManipulationBox();
        manipulationComboBox.setDisable(disable);
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getReportManipulation() != null) {
            manipulationComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getReportManipulation());
        }

        JFXButton tbManipulation = new JFXButton("", JEConfig.getSVGImage(Icon.SELECT_CHECK_BOX, 13, 13));
        tbManipulation.setTooltip(tooltipMarkAll);
        tbManipulation.setOnAction(event -> {
            gridPane.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == 4) {
                    if (node instanceof ReportManipulationBox) {
                        ReportManipulationBox reportAggregationBox = (ReportManipulationBox) node;
                        Platform.runLater(() -> reportAggregationBox.getSelectionModel().select(manipulationComboBox.getSelectionModel().getSelectedItem()));
                    }
                }
            });
        });


        ReportFixedPeriodBox fixedPeriodComboBox = new ReportFixedPeriodBox();
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getFixedPeriod() != null) {
            fixedPeriodComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getFixedPeriod());
        }

        ReportPeriodBox periodModeComboBox = new ReportPeriodBox(FXCollections.observableArrayList(PeriodMode.values()));
        periodModeComboBox.setDisable(disable);
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode() != null) {
            periodModeComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode());
            if (reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().equals(PeriodMode.FIXED) || reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().equals(PeriodMode.FIXED_TO_REPORT_END)) {
                showFixedPeriodComboBox(currentRow, fixedPeriodComboBox);
            }
        }
        ImageView imageMarkAllPeriod = new ImageView(imgMarkAll);
        imageMarkAllPeriod.fitHeightProperty().set(13);
        imageMarkAllPeriod.fitWidthProperty().set(13);

        JFXButton tbPeriod = new JFXButton("", JEConfig.getSVGImage(Icon.SELECT_CHECK_BOX, 13, 13));
        tbPeriod.setTooltip(tooltipMarkAll);
        tbPeriod.setOnAction(event -> {
            gridPane.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == 6) {
                    if (node instanceof ReportPeriodBox) {
                        ReportPeriodBox reportPeriodBox = (ReportPeriodBox) node;
                        Platform.runLater(() -> reportPeriodBox.getSelectionModel().select(periodModeComboBox.getSelectionModel().getSelectedItem()));
                    }
                }
            });
        });

        JFXButton tbAggregation = new JFXButton("", JEConfig.getSVGImage(Icon.SELECT_CHECK_BOX, 13, 13));
        tbAggregation.setTooltip(tooltipMarkAll);
        tbAggregation.setOnAction(event -> {
            gridPane.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == 2) {
                    if (node instanceof ReportAggregationBox) {
                        ReportAggregationBox reportAggregationBox = (ReportAggregationBox) node;
                        Platform.runLater(() -> reportAggregationBox.getSelectionModel().select(aggregationPeriodComboBox.getSelectionModel().getSelectedItem()));
                    }
                }
            });
        });

        ToggleSwitchPlus toggleSwitchPlus = new ToggleSwitchPlus();
        toggleSwitchPlus.setSelected(reportLink.isOptional());
        toggleSwitchPlus.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && reportLink.getLinkStatus() != ReportLink.Status.NEW) {
                    reportLink.setLinkStatus(ReportLink.Status.UPDATE);
                }
                reportLink.setOptional(newValue);
            }
        });

        JFXButton copyButton = new JFXButton("", JEConfig.getSVGImage(Icon.COPY, 16, 16));

        JFXButton removeButton = new JFXButton("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 16, 16));
        removeButton.setOnAction(event -> {
            if (row > 1) {
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE)) {
                    reportLink.setLinkStatus(ReportLink.Status.DELETE);
                    moveNodesGridPane(GridPane.getRowIndex(removeButton));
                    Platform.runLater(() -> gridPane.getChildren().removeAll(removeButton, excelCellLabel, targetsButton, aggregationPeriodComboBox, manipulationComboBox, tbManipulation, tbPeriod, periodModeComboBox, toggleSwitchPlus, copyButton, tbAggregation));
                    row--;
                } else if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.NEW)) {
                    reportLinks.remove(reportLink);
                    Platform.runLater(() -> updateGridPane(null));
                }
            }
        });

        AtomicReference<String> targetString = new AtomicReference<>();
        if (reportLink.getJEVisId() != null) {
            targetString.set(reportLink.getJEVisId().toString());
        }

        List<JEVisClass> classes = new ArrayList<>();

        for (String className : TreeSelectionDialog.allData) {
            try {
                classes.add(ds.getJEVisClass(className));
            } catch (Exception e) {
                logger.error("Could not get JEVisClass for {}", className, e);
            }
        }

        if (reportLink.getJEVisId() != null) {
            String target = "";
            if (reportLink.getReportAttribute() != null) {
                target = reportLink.getJEVisId() + ":" + reportLink.getReportAttribute().getAttributeName();
            } else {
                target = reportLink.getJEVisId().toString();
            }
            TargetHelper th = new TargetHelper(ds, target);
            if (th.isValid() && th.targetObjectAccessible()) {
                logger.info("Target Is valid");
                setButtonText(target, targetsButton);
            }
        }

        targetsButton.setOnAction(event -> {
            TargetHelper th = null;
            if (targetString.get() != null) {
                th = new TargetHelper(ds, targetString.get());
                if (th.isValid() && th.targetObjectAccessible()) {
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

            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(reportWizardDialog.getDialogContainer(), ds, classes, SelectionMode.SINGLE, openList, true);

            selectionDialog.setOnDialogClosed(event1 -> {
                if (selectionDialog.getResponse() == Response.OK) {
                    logger.trace("Selection Done");

                    StringBuilder newTarget = new StringBuilder();
                    List<UserSelection> userSelection = selectionDialog.getUserSelection();
                    for (UserSelection us : userSelection) {
                        int index = userSelection.indexOf(us);
                        if (index > 0) newTarget.append(";");

                        newTarget.append(us.getSelectedObject().getID());
                        reportLink.setJEVisId(us.getSelectedObject().getID());

                        updateName(reportLink);

                        ReportAttribute reportAttribute = reportLink.getReportAttribute();
                        if (us.getSelectedAttribute() != null) {
                            reportAttribute.setAttributeName(us.getSelectedAttribute().getName());
                        } else {
                            reportAttribute.setAttributeName("Value");
                        }

                        if (us.getSelectedAttribute() != null) {
                            newTarget.append(":").append(us.getSelectedAttribute().getName());
                        } else {
                            newTarget.append(":Value");
                        }
                    }
                    targetString.set(newTarget.toString());
                }
                setButtonText(targetString.get(), targetsButton);
            });
            selectionDialog.show();
        });

        aggregationPeriodComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setReportAggregation(newValue);
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && reportLink.getLinkStatus() != ReportLink.Status.NEW) {
                    reportLink.setLinkStatus(ReportLink.Status.UPDATE);
                }

                updateName(reportLink);
            }
        });

        manipulationComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setReportManipulation(newValue);
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && reportLink.getLinkStatus() != ReportLink.Status.NEW) {
                    reportLink.setLinkStatus(ReportLink.Status.UPDATE);
                }

                updateName(reportLink);
            }
        });

        periodModeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setPeriodMode(newValue);
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && reportLink.getLinkStatus() != ReportLink.Status.NEW) {
                    reportLink.setLinkStatus(ReportLink.Status.UPDATE);
                }

                updateName(reportLink);

                if (newValue == PeriodMode.FIXED || newValue == PeriodMode.FIXED_TO_REPORT_END) {
                    showFixedPeriodComboBox(currentRow, fixedPeriodComboBox);
                } else if (oldValue == PeriodMode.FIXED || oldValue == PeriodMode.FIXED_TO_REPORT_END) {

                    ReportFixedPeriodBox box = null;
                    for (Node node : gridPane.getChildren()) {
                        if (GridPane.getRowIndex(node).equals(currentRow)) {
                            if (node instanceof ReportFixedPeriodBox) {
                                box = (ReportFixedPeriodBox) node;
                            }
                        }
                    }

                    if (box != null) {
                        ReportFixedPeriodBox finalBox = box;
                        Platform.runLater(() -> gridPane.getChildren().remove(finalBox));
                    }
                }
            }
        });

        fixedPeriodComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setFixedPeriod(newValue);
                if (reportWizardDialog.getWizardType().equals(ReportWizardDialog.UPDATE) && reportLink.getLinkStatus() != ReportLink.Status.NEW) {
                    reportLink.setLinkStatus(ReportLink.Status.UPDATE);
                }

                updateName(reportLink);
            }
        });

        copyButton.setOnAction(event -> {
            ReportSheet firstSheetWithEmptySlots = reportWizardDialog.getFirstSheetWithEmptySlots(this);

            firstSheetWithEmptySlots.createNewReportLink(true, reportLink);

        });

        Platform.runLater(() -> {
            gridPane.add(excelCellLabel, 0, currentRow);
            gridPane.add(targetsButton, 1, currentRow);
            gridPane.add(aggregationPeriodComboBox, 2, currentRow);
            gridPane.add(tbAggregation, 3, currentRow);
            gridPane.add(manipulationComboBox, 4, currentRow);
            gridPane.add(tbManipulation, 5, currentRow);
            gridPane.add(periodModeComboBox, 6, currentRow);
            gridPane.add(tbPeriod, 7, currentRow);
            gridPane.add(toggleSwitchPlus, 8, currentRow);
            gridPane.add(copyButton, 9, currentRow);
            gridPane.add(removeButton, 10, currentRow);
        });
    }

    private void moveNodesGridPane(int targetRowIndex) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex < rowIndex) {
                Platform.runLater(() -> GridPane.setRowIndex(node, rowIndex - 1));
            }
        });
    }

    private void showFixedPeriodComboBox(int currentRow, ReportFixedPeriodBox fixedPeriodComboBox) {
        Platform.runLater(() -> gridPane.add(fixedPeriodComboBox, 11, currentRow));
    }

    private void setButtonText(String targetString, JFXButton targetsButton) {
        try {
            TargetHelper th = new TargetHelper(ds, targetString);

            if (th.isValid() && th.targetObjectAccessible()) {

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

                    if (th.isAttribute()) {

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

    public void updateName(ReportLink reportLink) {
        JEVisObject object = null;
        try {
            object = ds.getObject(reportLink.getJEVisId());
        } catch (Exception e) {
            logger.error("Could not update name for object with id: {}", reportLink.getJEVisId(), e);
        }

        if (object != null) {
            try {
                if (object.getJEVisClass().getName().equals("Clean Data")) {
                    reportLink.setName(object.getParents().get(0).getName());
                } else {
                    reportLink.setName(object.getName());
                }
            } catch (Exception e) {
                logger.error("Could not set new Report Link Name for object with id: {}", reportLink.getJEVisId(), e);
            }

            try {
                if (reportWizardDialog.getReportType() == ReportType.STANDARD) {
                    if (ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.name) || ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.CleanData.name)) {
                        reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object)
                                + "_" + reportLink.getReportAttribute().getAttributeName()
                                + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation()
                                + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());
                    } else {
                        reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object));
                    }
                } else {
                    reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object));
                }
            } catch (Exception e) {
                logger.error("Could not set new Variable Name for object with id: {}", reportLink.getJEVisId(), e);
            }
        }
    }

    private ReportLink buildReportLink(JEVisObject jeVisObject, String sheet, CellAddress cellAddress) {

        ReportLink reportLink = null;
        try {
            reportLink = ReportLink.parseFromJEVisObject(jeVisObject);
            reportLink.setCellAddress(cellAddress);
            reportLink.setSheet(sheet);
            reportLink.setLinkStatus(ReportLink.Status.FALSE);
        } catch (Exception e) {
            logger.error(jeVisObject, e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.parse.title"));
            alert.setHeaderText("JEVis Object: " + jeVisObject.getName() + ": " + jeVisObject.getID() + ": " + I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.parse.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

        return reportLink;
    }


    public void createNewReportLink(Boolean copy, ReportLink oldReportLink) {
        ReportLink reportLink = null;
        if (copy) {
            reportLink = oldReportLink.clone();
        } else {
            reportLink = oldReportLink;
        }
        reportLink.setLinkStatus(ReportLink.Status.NEW);
        reportLinks.add(reportLink);
        row++;
        createBox(reportLink);
    }

    public int getRow() {
        return row;
    }

    public List<ReportLink> getReportLinks() {
        return reportLinks;
    }
}
