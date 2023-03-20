package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.report.*;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.ReportSheet;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ReportWizardDialog {
    private static final Logger logger = LogManager.getLogger(ReportWizardDialog.class);

    public static final Image taskImage = JEConfig.getImage("Report.png");

    //public static String ICON = "Startup Wizard_18228.png";
    private JEVisDataSource ds;
    private JEVisObject reportLinkDirectory;
    //private int row = 0;
    private XSSFWorkbook workbook;
    private final List<String> sheetList = new ArrayList<>();
    private ReportType reportType = ReportType.STANDARD;
    private final TabPane tabPane = new TabPane();
    private Dialog reportWizardDialog;
    private Map<String, Map<CellAddress, JEVisObject>> cellJevisMap;
    private JEVisObject allAttributesRootObject;
    public static final String NEW = "NEW";
    public static final String UPDATE = "UPDATE";
    private final String wizardType;
    private Integer columnIndex = 0;
    private Integer rowIndex = 0;
    private Integer maxColumnIndex = 0;
    private Integer maxRowIndex = 0;
    private Result result;

    public ReportWizardDialog(JEVisObject newObject, String wizardType) {
        this.wizardType = wizardType;
        try {
            ds = newObject.getDataSource();
            JEVisClass reportLinkDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass reportLinkClass = ds.getJEVisClass("Report Link");

            List<JEVisObject> listDirectory = CommonMethods.getChildrenRecursive(newObject, reportLinkDirectoryClass);
            List<JEVisObject> listReportLinkObjects = new ArrayList<>();
            for (JEVisObject object : listDirectory) {
                listReportLinkObjects.addAll(object.getChildren(reportLinkClass, false));
            }
            if ((newObject.getAttribute("Template").hasSample())) {
                JEVisFile templateFile = newObject.getAttribute("Template").getLatestSample().getValueAsFile();
                loadTemplate(templateFile, listReportLinkObjects);
            }
        } catch (Exception ex) {
            logger.error(ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.template"));
            alert.setHeaderText(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.template.header"));
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return;
        }

        JEVisClass reportLinkClass = null;
        JEVisClass reportAttributeClass = null;
        JEVisClass reportPeriodConfigurationClass = null;
        try {
            JEVisClass reportLinksDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass emailNotificationClass = ds.getJEVisClass("E-Mail Notification");
            reportLinkClass = newObject.getDataSource().getJEVisClass("Report Link");
            reportAttributeClass = newObject.getDataSource().getJEVisClass("Report Attribute");
            reportPeriodConfigurationClass = newObject.getDataSource().getJEVisClass("Report Period Configuration");
            if (newObject.getChildren().size() > 0) {
                reportLinkDirectory = newObject.getChildren(reportLinksDirectoryClass, true).stream().findFirst().orElse(null);

                if (reportLinkDirectory == null) {
                    reportLinkDirectory = newObject.buildObject(I18n.getInstance().getString("tree.treehelper.reportlinkdirectory.name"), reportLinksDirectoryClass);
                    reportLinkDirectory.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportlinkdirectory.name"));
                    reportLinkDirectory.commit();
                }

                JEVisObject emailNotificationObject = newObject.getChildren(emailNotificationClass, true).stream().findFirst().orElse(null);

                if (emailNotificationObject == null) {
                    JEVisObject emailNotification = newObject.buildObject(I18n.getInstance().getString("tree.treehelper.emailnotification.name"), emailNotificationClass);
                    emailNotification.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.emailnotification.name"));
                    emailNotification.commit();
                }
            } else {
                reportLinkDirectory = newObject.buildObject(I18n.getInstance().getString("tree.treehelper.reportlinkdirectory.name"), reportLinksDirectoryClass);
                reportLinkDirectory.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportlinkdirectory.name"));
                reportLinkDirectory.commit();
                JEVisObject emailNotification = newObject.buildObject(I18n.getInstance().getString("tree.treehelper.emailnotification.name"), emailNotificationClass);
                emailNotification.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.emailnotification.name"));
                emailNotification.commit();
            }
        } catch (Exception e) {
            logger.error("Could not create basic folder structure", e);
        }

        init();

        JEVisClass finalReportLinkClass = reportLinkClass;
        JEVisClass finalReportAttributeClass = reportAttributeClass;
        JEVisClass finalReportPeriodConfigurationClass = reportPeriodConfigurationClass;
        reportWizardDialog.setOnCloseRequest(event -> {
            logger.debug("wizard");
            logger.debug(event.toString());
            if (getResult() == Result.OK) {
                onWizardClose(newObject, wizardType, finalReportLinkClass, finalReportAttributeClass, finalReportPeriodConfigurationClass);
            }
        });

        reportWizardDialog.show();
    }

    @NotNull
    private static Task getUpdateTask(List<ReportLink> updateList) {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                updateList.forEach(reportLink -> {
                    try {
                        reportLink.update();
                    } catch (Exception e) {
                        logger.error(e);
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.update.title"));
                        alert.setHeaderText("JEVis Object: " + reportLink.getName() + " : " + I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.update.header"));
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    } finally {
                        JEConfig.getStatusBar().progressProgressJob("reportlinks", 1, I18n.getInstance().getString("plugin.object.report.message.finishedlink") + " " + reportLink.getName());
                        succeeded();
                    }
                });
                return null;

            }

        };
    }

    private void onWizardClose(JEVisObject newObject, String wizardTyp, JEVisClass finalReportLinkClass, JEVisClass finalReportAttributeClass, JEVisClass finalReportPeriodConfigurationClass) {
        try {
            if (wizardTyp.equals(ReportWizardDialog.NEW)) {
                createStandardTemplate();
                //JEVisAttribute templateAttribute = newObject.getAttribute("Template");
                //JEVisSample templateSample = templateAttribute.buildSample(new DateTime(), template);
                //templateSample.commit();
            }

            List<ReportLink> allLinks = new ArrayList<>();
            for (Tab tab : tabPane.getTabs()) {
                ReportSheet reportSheet = (ReportSheet) tab;
                for (ReportLink link : reportSheet.getReportLinks()) {
                    allLinks.add(link);
                }
            }

            List<ReportLink> deleteList = allLinks.stream().filter(reportLink -> reportLink.getLinkStatus().equals(ReportLink.Status.DELETE)).collect(Collectors.toList());
            List<ReportLink> newList = allLinks.stream().filter(reportLink -> reportLink.getLinkStatus().equals(ReportLink.Status.NEW)).collect(Collectors.toList());
            List<ReportLink> updateList = allLinks.stream().filter(reportLink -> reportLink.getLinkStatus().equals(ReportLink.Status.UPDATE)).collect(Collectors.toList());
            Task updateTask = getUpdateTask(updateList);
            JEConfig.getStatusBar().addTask(ReportWizardDialog.class.getName(), updateTask, taskImage, true);
            delete(deleteList);
            for (ReportLink reportLink : newList) {
                if (newObject.getChildren().size() > 1) {
                    if (reportLinkDirectory != null) {
                        if (reportLink.getJeVisObject() == null) {
                            Task task = new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    try {
                                        String variableName = reportLink.getTemplateVariableName();

                                        JEVisObject object = reportLinkDirectory.buildObject(variableName, finalReportLinkClass);
                                        object.commit();

                                        JEVisAttribute jeVis_id = object.getAttribute("JEVis ID");
                                        JEVisSample jevisIdSample = jeVis_id.buildSample(new DateTime(), reportLink.getJEVisId());
                                        jevisIdSample.commit();

                                        JEVisAttribute optionalAttribute = object.getAttribute("Optional");
                                        JEVisSample sampleOptional = optionalAttribute.buildSample(new DateTime(), reportLink.isOptional());
                                        sampleOptional.commit();

                                        JEVisAttribute templateVariableName = object.getAttribute("Template Variable Name");
                                        JEVisSample templateVariableSample = templateVariableName.buildSample(new DateTime(), variableName);
                                        templateVariableSample.commit();

                                        if (reportType == ReportType.STANDARD && (ds.getObject(jevisIdSample.getValueAsLong()).getJEVisClass().getName().equals(JC.Data.name) || ds.getObject(jevisIdSample.getValueAsLong()).getJEVisClass().getName().equals(JC.Data.CleanData.name))) {
                                            JEVisObject reportAttribute = object.buildObject(I18n.getInstance().getString("tree.treehelper.reportattribute.name"), finalReportAttributeClass);
                                            reportAttribute.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportattribute.name"));
                                            reportAttribute.commit();
                                            JEVisAttribute attribute_name = reportAttribute.getAttribute("Attribute Name");

                                            JEVisSample attributeNameSample = attribute_name.buildSample(new DateTime(), reportLink.getReportAttribute().getAttributeName());
                                            attributeNameSample.commit();

                                            JEVisObject reportPeriodConfiguration = reportAttribute.buildObject(I18n.getInstance().getString("tree.treehelper.reportperiodconfiguration.name"), finalReportPeriodConfigurationClass);
                                            reportPeriodConfiguration.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportperiodconfiguration.name"));
                                            reportPeriodConfiguration.commit();

                                            JEVisAttribute aggregationAttribute = reportPeriodConfiguration.getAttribute("Aggregation");
                                            JEVisSample aggregationSample = aggregationAttribute.buildSample(new DateTime(), reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
                                            aggregationSample.commit();

                                            JEVisAttribute manipulationAttribute = reportPeriodConfiguration.getAttribute("Manipulation");
                                            JEVisSample manipulationSample = manipulationAttribute.buildSample(new DateTime(), reportLink.getReportAttribute().getReportPeriodConfiguration().getReportManipulation());
                                            manipulationSample.commit();

                                            JEVisAttribute periodAttribute = reportPeriodConfiguration.getAttribute("Period");
                                            JEVisSample periodSample = periodAttribute.buildSample(new DateTime(), reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());
                                            periodSample.commit();

                                            JEVisAttribute fixedPeriodAttribute = reportPeriodConfiguration.getAttribute("Fixed Period");
                                            JEVisSample fixedPeriodSample = fixedPeriodAttribute.buildSample(new DateTime(), reportLink.getReportAttribute().getReportPeriodConfiguration().getFixedPeriod().toString());
                                            fixedPeriodSample.commit();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        failed();
                                    } finally {
                                        JEConfig.getStatusBar().progressProgressJob("reportlinks", 1, I18n.getInstance().getString("plugin.object.report.message.finishedlink") + " " + reportLink.getName());
                                        succeeded();
                                    }
                                    return null;
                                }
                            };

                            JEConfig.getStatusBar().addTask(ReportWizardDialog.class.getName(), task, taskImage, true);
                        }
                    }
                }
            }
            addData(newList);

        } catch (Exception e) {
            logger.error(e);
        }

        if (workbook != null) {
            try {
                JEVisFile template = writeTemplate(newObject.getName());
                JEVisAttribute jeVisAttribute = newObject.getAttribute("Template");
                JEVisSample templateSample = jeVisAttribute.buildSample(new DateTime(), template);
                templateSample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private void addData(List<ReportLink> newList) {
        Optional<Integer> optional_maxDataSheetNumber = sheetList.stream().filter(s -> s.contains("Data")).map(s -> {
            s = s.replaceAll("\\D+", "");
            if (!s.isEmpty()) {
                return Integer.valueOf(s);
            } else {
                return 0;
            }
        }).max(Integer::compareTo);
        int maxDataSheetnumber = 0;
        if (optional_maxDataSheetNumber.isPresent()) {
            maxDataSheetnumber = optional_maxDataSheetNumber.get();
        }
        Optional<Integer> optional_maxTextSheetNumber = sheetList.stream().filter(s -> s.contains("Text")).map(s -> {
            s = s.replaceAll("\\D+", "");
            if (!s.isEmpty()) {
                return Integer.valueOf(s);
            } else {
                return 0;
            }
        }).max(Integer::compareTo);
        int maxTextSheetnumber = 0;
        if (optional_maxTextSheetNumber.isPresent()) {
            maxTextSheetnumber = optional_maxTextSheetNumber.get();
        }


        List<ReportLink> newDataLinks = filterReportDataLinks(newList);
        List<ReportLink> newTextLinks = filterReportTextLinks(newList);
        logger.debug("textlinks");
        logger.debug(newTextLinks);

        if (newDataLinks != null) {
            if (newDataLinks.size() > 0) {
                for (List<ReportLink> partition : Lists.partition(newDataLinks, 17)) {
                    maxDataSheetnumber++;
                    addDataContent(partition, maxDataSheetnumber);

                }
            }
        }
        if (newTextLinks != null) {
            if (newTextLinks.size() > 0) {
                for (List<ReportLink> partition : Lists.partition(newTextLinks, 17)) {
                    maxTextSheetnumber++;
                    addTextContent(partition, maxTextSheetnumber);

                }
            }
        }
    }

    private JEVisFile writeTemplate(String templateName) throws IOException {
        DateTime now = new DateTime();
        templateName += "_template_" + now.toString("YYYYMMdd");
        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(Files.newOutputStream(templateFile.toPath()));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
    }

    private void delete(List<ReportLink> deleteReportLinks) {

        List<ReportLink> deleteDataLinks = filterReportDataLinks(deleteReportLinks);
        List<ReportLink> deleteTextLinks = filterReportTextLinks(deleteReportLinks);

        for (ReportLink rl : deleteDataLinks) {
            removeDataCell(rl);
        }
        for (ReportLink rl : deleteTextLinks) {
            removeTextCell(rl);
        }
    }

    private void removeDataCell(ReportLink reportLink) {
        logger.debug("Delete: {}", reportLink);
        Sheet sheet = workbook.getSheet(reportLink.getSheet());
        Cell cell = sheet.getRow(reportLink.getCellAddress().getRow()).getCell(reportLink.getCellAddress().getColumn());
        cell.removeCellComment();
        removeDataCellContent(sheet, reportLink.getCellAddress());
        try {
            reportLink.getJeVisObject().delete();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void removeTextCell(ReportLink reportLink) {
        logger.debug("Delete: {}", reportLink);
        Sheet sheet = workbook.getSheet(reportLink.getSheet());
        Cell cell = sheet.getRow(reportLink.getCellAddress().getRow()).getCell(reportLink.getCellAddress().getColumn());
        cell.removeCellComment();
        removeTextCellContent(sheet, reportLink.getCellAddress());
        try {
            reportLink.getJeVisObject().delete();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void removeDataCellContent(Sheet sheet, CellAddress cellAddress) {
        getOrCreateCell(sheet, cellAddress.getRow(), cellAddress.getColumn()).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 1, cellAddress.getColumn()).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 2, cellAddress.getColumn()).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow(), cellAddress.getColumn() + 1).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 1, cellAddress.getColumn() + 1).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 2, cellAddress.getColumn() + 1).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow(), cellAddress.getColumn() + 2).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 1, cellAddress.getColumn() + 2).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() - 2, cellAddress.getColumn() + 2).setBlank();
    }

    private void removeTextCellContent(Sheet sheet, CellAddress cellAddress) {
        getOrCreateCell(sheet, cellAddress.getRow(), cellAddress.getColumn()).setBlank();
        getOrCreateCell(sheet, cellAddress.getRow() + 1, cellAddress.getColumn()).setBlank();

    }

    private void createEmptyReportSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(I18n.getInstance().getString("plugin.object.report.dialog.report.titlepage"));
        sheet.setDefaultColumnWidth(3);
    }


    private void init() {
        reportWizardDialog = new Dialog();
        reportWizardDialog.setTitle(I18n.getInstance().getString("plugin.configuration.reportwizard.title"));
        reportWizardDialog.setHeaderText(I18n.getInstance().getString("plugin.configuration.reportwizard.header"));
        reportWizardDialog.setResizable(true);
        reportWizardDialog.initOwner(JEConfig.getStage());
        reportWizardDialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) reportWizardDialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Node header = DialogHeader.getDialogHeader(JEConfig.getSVGImage(Icon.WIZARD_HAT, 32, 32), I18n.getInstance().getString("plugin.object.report.dialog.header"));

        VBox vBox = new VBox(6);
        vBox.setFillWidth(true);

        JFXButton addMultiple = new JFXButton("", JEConfig.getSVGImage(Icon.PLUS, 16, 16));

        JFXComboBox<ReportType> reportTypeComboBox = getReportTypeComboBox();
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                reportType = newValue;
            }
        });

        updateTabPane();

        addMultiple.setOnAction(event -> {
            if (reportType == ReportType.STANDARD) {
                openMultiSelect();
            } else {
                openSingleSelect();
            }
        });


        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        reportWizardDialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) reportWizardDialog.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) reportWizardDialog.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            this.result = Result.OK;
            reportWizardDialog.close();
        });

        cancelButton.setOnAction(event -> {
            result = Result.CANCEL;
            reportWizardDialog.close();
        });

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        vBox.getChildren().add(header);
        vBox.getChildren().add(tabPane);
        vBox.getChildren().add(reportTypeComboBox);
        vBox.getChildren().add(addMultiple);
        vBox.getChildren().add(separator);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        reportWizardDialog.getDialogPane().setContent(vBox);
    }

    private void updateTabPane() {
        if (cellJevisMap != null && !cellJevisMap.isEmpty()) {
            for (Map.Entry<String, Map<CellAddress, JEVisObject>> entry : cellJevisMap.entrySet()) {
                String s = entry.getKey();
                Map<CellAddress, JEVisObject> cellAddressJEVisObjectMap = entry.getValue();

                ReportSheet reportSheet = new ReportSheet(ds, this);
                reportSheet.setText(s);

                reportSheet.updateGridPane(cellAddressJEVisObjectMap);

                tabPane.getTabs().add(reportSheet);
            }
        } else {
            ReportSheet firstSheet = new ReportSheet(ds, this);
            firstSheet.setText("Data 0");

            tabPane.getTabs().add(firstSheet);
        }
    }

    private JFXComboBox<ReportType> getReportTypeComboBox() {
        JFXComboBox<ReportType> box = new JFXComboBox<>();

        box.setItems(FXCollections.observableArrayList(ReportType.values()));

        box.getSelectionModel().select(ReportType.STANDARD);

        final String keyStandard = I18n.getInstance().getString("plugin.object.report.dialog.typ.standard");
        final String keyAllAttributes = I18n.getInstance().getString("plugin.object.report.dialog.typ.allattributes");

        Callback<ListView<ReportType>, ListCell<ReportType>> cellFactory = new Callback<javafx.scene.control.ListView<ReportType>, ListCell<ReportType>>() {
            @Override
            public ListCell<ReportType> call(javafx.scene.control.ListView<ReportType> param) {
                return new ListCell<ReportType>() {
                    @Override
                    protected void updateItem(ReportType reportType, boolean empty) {
                        super.updateItem(reportType, empty);
                        if (empty || reportType == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (reportType) {
                                case STANDARD:
                                    text = keyStandard;
                                    break;
                                case ALL_ATTRIBUTES:
                                    text = keyAllAttributes;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        box.setCellFactory(cellFactory);
        box.setButtonCell(cellFactory.call(null));

        return box;
    }

    private void openMultiSelect() {

        TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, new ArrayList<>(), SelectionMode.MULTIPLE, new ArrayList<>(), true);

        selectionDialog.setOnCloseRequest(event -> {
            if (selectionDialog.getResponse() == Response.OK) {
                logger.trace("Selection Done");

                List<UserSelection> userSelection = selectionDialog.getUserSelection();
                ReportSheet currentSheet = (ReportSheet) tabPane.getSelectionModel().getSelectedItem();

                for (UserSelection us : userSelection) {
                    currentSheet = getFirstSheetWithEmptySlots(currentSheet);

                    ReportSheet finalCurrentSheet = currentSheet;
                    Platform.runLater(() -> tabPane.getSelectionModel().select(finalCurrentSheet));

                    ReportLink newLink = new ReportLink("", us.getSelectedObject().getID(), false, "",
                            new ReportAttribute("Value",
                                    new ReportPeriodConfiguration(AggregationPeriod.NONE, ManipulationMode.NONE, PeriodMode.CURRENT, FixedPeriod.NONE)));
                    if (us.getSelectedAttribute() != null) {
                        newLink.getReportAttribute().setAttributeName(us.getSelectedAttribute().getName());
                    } else {
                        newLink.getReportAttribute().setAttributeName("Value");
                    }

                    currentSheet.updateName(newLink);

                    currentSheet.createNewReportLink(false, newLink);
                }
            }
        });
        selectionDialog.show();
    }

    public ReportSheet getFirstSheetWithEmptySlots(ReportSheet currentSheet) {
        if (currentSheet.getRow() < 50 / 3) {
            return currentSheet;
        } else if (tabPane.getTabs().size() > tabPane.getTabs().indexOf(currentSheet) + 1) {
            ReportSheet nextSheet = (ReportSheet) tabPane.getTabs().get(tabPane.getTabs().indexOf(currentSheet) + 1);
            return getFirstSheetWithEmptySlots(nextSheet);
        } else {
            ReportSheet newSheet = new ReportSheet(ds, this);
            newSheet.setText("Data " + tabPane.getTabs().size());
            tabPane.getTabs().add(newSheet);
            return getFirstSheetWithEmptySlots(newSheet);
        }
    }

    private void openSingleSelect() {
        List<JEVisClass> classes = new ArrayList<>();

        for (String className : TreeSelectionDialog.allData) {
            try {
                classes.add(ds.getJEVisClass(className));
            } catch (Exception e) {
                logger.error("Could not get JEVisClass for {}", className, e);
            }
        }

        TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, classes, SelectionMode.SINGLE, new ArrayList<>(), true);

        selectionDialog.setOnCloseRequest(event -> {
            if (selectionDialog.getResponse() == Response.OK) {
                logger.trace("Selection Done");

                List<UserSelection> userSelection = selectionDialog.getUserSelection();

                for (UserSelection us : userSelection) {

                    allAttributesRootObject = us.getSelectedObject();

                    JEVisClass directoryClass = null;
                    try {
                        directoryClass = ds.getJEVisClass("Directory");
                        createAllAttributeLinks(directoryClass, us.getSelectedObject());
                    } catch (Exception e) {
                        logger.error(e);
                    }

                }
            }
        });
        selectionDialog.show();
    }

    private void createAllAttributeLinks(JEVisClass directoryClass, JEVisObject selectedObject) throws JEVisException {

        if (!directoryClass.getHeirs().contains(selectedObject.getJEVisClass())) {
            ReportLink newLink = new ReportLink("", selectedObject.getID(), false, "", null);
            //TODO enable again
//            updateName(newLink);
//            createNewReportLink(false, newLink);
        }

        try {
            for (JEVisObject jeVisObject : selectedObject.getChildren()) {
                createAllAttributeLinks(directoryClass, jeVisObject);
            }
        } catch (Exception e) {
            logger.error("Could not create attribute link for object {}:{}", selectedObject.getName(), selectedObject.getID());
        }
    }

    public void loadTemplate(JEVisFile jeVisFile, List<JEVisObject> listReportLinkObjects) throws IOException, NullPointerException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jeVisFile.getBytes());
        workbook = new XSSFWorkbook(byteArrayInputStream);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetList.add(workbook.getSheetAt(i).getSheetName());
        }
        cellJevisMap = new TreeMap<>();
        for (String sheetName : sheetList) {
            if (sheetName.contains("Data")) {
                logger.debug("load Excel sheet: {}", sheetName);
                XSSFSheet sheet = workbook.getSheet(sheetName);
                cellJevisMap.put(sheet.getSheetName(), new TreeMap<>());
                for (Map.Entry<CellAddress, XSSFComment> entry : sheet.getCellComments().entrySet()) {
                    String variable = getVariableFromComment(entry.getValue());
                    Optional<JEVisObject> jevisobject = listReportLinkObjects.stream().filter(jeVisObject -> jeVisObject.getName().equals(variable)).findFirst();
                    if (jevisobject.isPresent()) {
                        cellJevisMap.get(sheet.getSheetName()).put(entry.getKey(), jevisobject.get());
                        logger.debug("load DP key {} with object name {}", entry.getKey(), jevisobject.get().getName());
                    }
                }
            } else if (sheetName.contains("Text")) {
                XSSFSheet sheet = workbook.getSheet(sheetName);
                cellJevisMap.put(sheet.getSheetName(), new TreeMap<>());
                Iterator<Cell> cellIterator = sheet.getRow(0).cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    logger.debug(cell.getStringCellValue());
                    if (!cell.getStringCellValue().isEmpty()) {
                        Optional<JEVisObject> jevisobject = listReportLinkObjects.stream().filter(jeVisObject -> jeVisObject.getName().equals(cell.getStringCellValue())).findFirst();
                        if (jevisobject.isPresent()) {
                            cellJevisMap.get(sheet.getSheetName()).put(cell.getAddress(), jevisobject.get());
                            logger.debug(cellJevisMap.get(sheet.getSheetName()));
                            logger.debug("load DP with cell address {} and object name {}", cell.getAddress(), jevisobject.get().getName());
                        }
                    }
                }
            }
        }
        logger.debug(cellJevisMap);
    }

    private String getVariableFromComment(XSSFComment xssfComment) {
        String first = "items=";
        String commentString = xssfComment.getString().getString();
        if (commentString.contains("jx:each")) {
            return commentString.substring(commentString.indexOf(first) + first.length(), commentString.indexOf("var=")).replace("\"", "").trim().replace(".value", "");
        } else {
            return null;
        }
    }

    public void createStandardTemplate() {

        workbook = new XSSFWorkbook();
        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);

        createEmptyReportSheet(workbook);
    }

    private JEVisFile createAllAttributesTemplate(String templateName) throws IOException, JEVisException {
        if (templateName == null || templateName.equals("")) {
            templateName = "template";
        }
        DateTime now = new DateTime();
        templateName += "_template_" + now.toString("YYYYMMdd");
        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFFont defaultFont = workbook.createFont();
        defaultFont.setFontHeightInPoints((short) 10);
        defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        XSSFFont boldFont = workbook.createFont();
        boldFont.setFontHeightInPoints((short) 10);
        boldFont.setFontName("Arial");
        boldFont.setColor(IndexedColors.BLACK.getIndex());
        boldFont.setBold(true);
        boldFont.setItalic(false);

        XSSFFont boldHeaderFont = workbook.createFont();
        boldHeaderFont.setFontHeightInPoints((short) 12);
        boldHeaderFont.setFontName("Arial");
        boldHeaderFont.setColor(IndexedColors.BLACK.getIndex());
        boldHeaderFont.setBold(true);
        boldHeaderFont.setItalic(false);

        CellStyle defaultStyle = workbook.createCellStyle();
        defaultStyle.setFont(defaultFont);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        CellStyle boldHeaderStyle = workbook.createCellStyle();
        boldHeaderStyle.setFont(boldHeaderFont);

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);
        cellStyleDates.setFont(defaultFont);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);
        cellStyleValues.setFont(defaultFont);

        JEVisClass directoryClass = ds.getJEVisClass(JC.Directory.name);

        if (directoryClass.getHeirs().contains(allAttributesRootObject.getJEVisClass())) {
            for (JEVisObject sheetObject : allAttributesRootObject.getChildren()) {

                Sheet sheet = workbook.createSheet(sheetObject.getName()); //create sheet
                Cell firstCell = getOrCreateCell(sheet, 0, 0);

                columnIndex = 0;
                maxColumnIndex = 0;
                rowIndex = 0;
                maxRowIndex = 0;

                createCellsForChildren(directoryClass, sheet, boldHeaderStyle, defaultStyle, boldStyle, sheetObject);

                String lastCellColumnName = CellReference.convertNumToColString(maxColumnIndex + 2);
                String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + (maxRowIndex + 1) + "\")";
                addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);
            }

        }

        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
    }

    private void addDataContent(List<ReportLink> links, int number) {
        logger.debug("Create Sheet Data{} with {} links", number, links);

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);


        setExcelDataContent(links, workbook, number, cellStyleDates, cellStyleValues);
    }

    private void addTextContent(List<ReportLink> links, int number) {
        logger.debug("Create Sheet Data{} with {} links", number, links);

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);


        setExcelTextContent(links, workbook, number, cellStyleDates, cellStyleValues);
    }

    private void setExcelTextContent(List<ReportLink> links, XSSFWorkbook workbook, int number, CellStyle cellStyleDates, CellStyle cellStyleValues) {


        int sheetWidth = links.size();
        Sheet sheet = workbook.createSheet("Text" + number); //create sheet
        Cell firstCell = getOrCreateCell(sheet, 0, 0);
        String lastCellColumnName = CellReference.convertNumToColString(sheetWidth);
        String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + 3 + "\")";
        addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);


        for (int i = 0; i < links.size(); i++) {
            ReportLink rl = links.get(i);
            Cell cell = getOrCreateCell(sheet, 0, (i + 1));

            cell.setCellValue(rl.getTemplateVariableName());

            Cell cell2 = getOrCreateCell(sheet, 1, (i + 1));
            String string = "${" +
                    rl.getTemplateVariableName() +
                    ".Value.value" +
                    "}";
            cell2.setCellValue(string);

        }

    }


    private void setExcelDataContent(List<ReportLink> links, XSSFWorkbook workbook, int number, CellStyle cellStyleDates, CellStyle cellStyleValues) {

        List<ReportLink> dataLinks = links.stream().filter(reportLink -> {
            try {
                return ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.name) || ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.CleanData.name);
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        logger.debug(dataLinks);

        int sheetWidth = links.size() * 3;
        XSSFSheet sheet = workbook.createSheet("Data" + number); //create sheet
        Cell firstCell = getOrCreateCell(sheet, 0, 0);
        String lastCellColumnName = CellReference.convertNumToColString(sheetWidth);
        String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + 3 + "\")";
        addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);

        int counter = 2;

        for (int i = 0; i < links.size(); i++) {
            ReportLink rl = links.get(i);
            Cell cell = getOrCreateCell(sheet, 0, ((i + 1) + (counter - 2)));
            cell.setCellValue(rl.getName());
            Cell cell2 = getOrCreateCell(sheet, 0, ((i + 1) + counter - 1));
            cell2.setCellValue(rl.getReportAttribute().getReportPeriodConfiguration().getReportAggregation() + "_" +
                    rl.getReportAttribute().getReportPeriodConfiguration().getReportManipulation());
            Cell cell3 = getOrCreateCell(sheet, 0, ((i + 1) + counter));
            cell3.setCellValue(rl.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());

            counter = counter + 2;
        }

        counter = 2;
        for (int i = 0; i < links.size(); i++) {
            ReportLink rl = links.get(i);
            Cell cell = getOrCreateCell(sheet, 1, ((i + 1) + (counter - 2)));
            cell.setCellValue("Date");
            Cell cell2 = getOrCreateCell(sheet, 1, ((i + 1) + counter - 1));
            cell2.setCellValue("Value");
            Cell cell3 = getOrCreateCell(sheet, 1, ((i + 1) + counter));
            cell3.setCellValue("Unit");

            counter = counter + 2;
        }

        counter = 2;
        for (int i = 0; i < links.size(); i++) {
            ReportLink rl = links.get(i);
            Cell cell = getOrCreateCell(sheet, 2, ((i + 1) + (counter - 2)));
            cell.setCellValue("${data.timestamp}");
            cell.setCellStyle(cellStyleDates);
            sheet.getColumnHelper().setColDefaultStyle(((i + 1) + (counter - 2)), cellStyleDates);
            Cell cell2 = getOrCreateCell(sheet, 2, ((i + 1) + counter - 1));
            cell2.setCellValue("${data.value}");
            cell2.setCellStyle(cellStyleValues);
            sheet.getColumnHelper().setColDefaultStyle(((i + 1) + (counter - 1)), cellStyleValues);
            Cell cell3 = getOrCreateCell(sheet, 2, ((i + 1) + counter));
            cell3.setCellValue("${data.unit}");
            String columnName = CellReference.convertNumToColString((i + 1) + counter);
            String commentText = "jx:each(items=\"" + rl.getTemplateVariableName() + ".value\" var=\"data\" lastCell=\"" + columnName + 3 + "\")";
            addComment(workbook, sheet, 2, ((i + 1) + (counter - 2)), "JEVis", commentText);

            counter = counter + 2;
        }
    }

    private void createCellsForChildren(JEVisClass directoryClass, Sheet sheet, CellStyle boldHeader, CellStyle defaultStyle, CellStyle boldStyle, JEVisObject sheetObject) throws JEVisException {

        for (JEVisObject jeVisObject : sheetObject.getChildren()) {
            if (directoryClass.getHeirs().contains(jeVisObject.getJEVisClass())) {
                Cell cell = getOrCreateCell(sheet, rowIndex, columnIndex);
                cell.setCellValue(jeVisObject.getName());
                cell.setCellStyle(boldHeader);

                columnIndex++;
                rowIndex++;
                maxColumnIndex = Math.max(maxColumnIndex, columnIndex);
                maxRowIndex = Math.max(maxRowIndex, rowIndex);

                createCellsForChildren(directoryClass, sheet, boldHeader, defaultStyle, boldStyle, jeVisObject);

                rowIndex++;
                maxColumnIndex = Math.max(maxColumnIndex, columnIndex);
                maxRowIndex = Math.max(maxRowIndex, rowIndex);
                columnIndex--;
            } else {
                Cell cell = getOrCreateCell(sheet, rowIndex, columnIndex);
                cell.setCellValue(jeVisObject.getName());
                cell.setCellStyle(boldStyle);

                rowIndex++;

                for (JEVisAttribute jeVisAttribute : jeVisObject.getAttributes()) {

                    String attributeName = I18nWS.getInstance().getAttributeName(jeVisAttribute);
                    Cell attributeCell = getOrCreateCell(sheet, rowIndex, columnIndex + 1);
                    attributeCell.setCellValue(attributeName);
                    attributeCell.setCellStyle(boldStyle);

                    Cell attributeCommand = getOrCreateCell(sheet, rowIndex, columnIndex + 2);
                    String templateVariableName = CalculationNameFormatter.createVariableName(jeVisObject);
                    attributeCommand.setCellValue("${" + templateVariableName + "." + jeVisAttribute.getName() + ".value}");
                    attributeCommand.setCellStyle(defaultStyle);

                    rowIndex++;
                }

                maxColumnIndex = Math.max(maxColumnIndex, columnIndex);
                maxRowIndex = Math.max(maxRowIndex, rowIndex);

                createCellsForChildren(directoryClass, sheet, boldHeader, defaultStyle, boldStyle, jeVisObject);

                rowIndex++;

                maxColumnIndex = Math.max(maxColumnIndex, columnIndex);
                maxRowIndex = Math.max(maxRowIndex, rowIndex);
            }
        }
    }

    private Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        return cell;
    }

    private void addComment(Workbook workbook, Sheet sheet, int rowIdx, int colIdx, String author, String commentText) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:
        Cell cell = getOrCreateCell(sheet, rowIdx, colIdx);

        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        anchor.setCol2(cell.getColumnIndex() + 3); //...and ends at that given column
        anchor.setRow1(rowIdx + 1); //one row below the cell...
        anchor.setRow2(rowIdx + 5); //...and 4 rows high

        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        cell.setCellComment(comment);
    }

    private List<ReportLink> filterReportDataLinks(List<ReportLink> reportLinkList) {
        return reportLinkList.stream().filter(reportLink -> {
            try {
                return ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.name) || ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.Data.CleanData.name);
            } catch (Exception e) {
                logger.error(e);
            }
            return false;
        }).collect(Collectors.toList());
    }


    private List<ReportLink> filterReportTextLinks(List<ReportLink> reportLinkList) {
        return reportLinkList.stream().filter(reportLink -> {
            try {
                return ds.getObject(reportLink.getJEVisId()).getJEVisClass().getName().equals(JC.StringData.name);
            } catch (Exception e) {
                logger.error(e);
            }
            return false;
        }).collect(Collectors.toList());
    }

    public String getWizardType() {
        return wizardType;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public Result getResult() {
        return result;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    private enum Result {
        OK, CANCEL;
    }
}
