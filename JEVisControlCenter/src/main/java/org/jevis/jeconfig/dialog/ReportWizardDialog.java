package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.*;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.ReportAggregationBox;
import org.jevis.jeconfig.application.control.ReportFixedPeriodBox;
import org.jevis.jeconfig.application.control.ReportManipulationBox;
import org.jevis.jeconfig.application.control.ReportPeriodBox;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReportWizardDialog {
    private static final Logger logger = LogManager.getLogger(ReportWizardDialog.class);
    Image imgMarkAll = ResourceLoader.getImage("jetxee-check-sign-and-cross-sign-3.png");
    Tooltip tooltipMarkAll = new Tooltip(I18n.getInstance().getString("plugin.graph.dialog.changesettings.tooltip.forall"));
    public static final Image taskImage = JEConfig.getImage("Report.png");

    //public static String ICON = "Startup Wizard_18228.png";
    private JEVisDataSource ds;
    private JEVisObject reportLinkDirectory;
    private List<UserSelection> selections;
    private final List<ReportLink> reportLinkList = new ArrayList<>();
    //private int row = 0;

    private int row = 1;

    private XSSFWorkbook workbook;

    private final List<String> sheetList = new ArrayList<>();
    private ReportType reportType = ReportType.STANDARD;

    private GridPane gridPane;
    private JFXDialog reportWizardDialog;

    private Map<String, Map<CellAddress, JEVisObject>> map;
    private JEVisObject allAttributesRootObject;
    public static final String NEW = "NEW";
    public static final String UPDATE = "UPDATE";

    private final String wizardTyp;

    //                String lastCellColumnName = CellReference.convertNumToColString(sheetWidth);
//                String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + 3 + "\")";
//                addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);

    private Integer columnIndex = 0;
    private Integer rowIndex = 0;
    private Integer maxColumnIndex = 0;
    private Integer maxRowIndex = 0;

    public ReportWizardDialog(JEVisObject newObject, String wizardTyp) {
        this.wizardTyp = wizardTyp;
        try {
            ds = newObject.getDataSource();
            JEVisClass reportLinkDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass reportLinkClass = ds.getJEVisClass("Report Link");
            List<JEVisObject> listDirectory = newObject.getChildren(reportLinkDirectoryClass, false);
            List<JEVisObject> listReportLinkObjects = new ArrayList<>();
            for (int i = 0; i < listDirectory.size(); i++) {
                listReportLinkObjects.addAll(listDirectory.get(i).getChildren(reportLinkClass, false));
            }
            if ((newObject.getAttribute("Template").hasSample())) {
                JEVisFile templateFile = newObject.getAttribute("Template").getLatestSample().getValueAsFile();
                loadTemplate(templateFile, listReportLinkObjects);
            }
        } catch (JEVisException e) {
            throw new RuntimeException(e);
        } catch (IOException | NullPointerException ex) {
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
            //ds = newObject.getDataSource();

            JEVisClass reportLinksDirectoryClass = ds.getJEVisClass("Report Link Directory");
            JEVisClass emailNotificationClass = ds.getJEVisClass("E-Mail Notification");
            reportLinkClass = newObject.getDataSource().getJEVisClass("Report Link");
            reportAttributeClass = newObject.getDataSource().getJEVisClass("Report Attribute");
            reportPeriodConfigurationClass = newObject.getDataSource().getJEVisClass("Report Period Configuration");
            if (newObject.getChildren().size() > 0) {
                if (newObject.getChildren().stream().filter(jeVisObject -> {
                    try {
                        return jeVisObject.getJEVisClass().equals(reportLinksDirectoryClass);
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }).count() == 0) {
                    reportLinkDirectory = newObject.buildObject(I18n.getInstance().getString("tree.treehelper.reportlinkdirectory.name"), reportLinksDirectoryClass);
                    reportLinkDirectory.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportlinkdirectory.name"));
                    reportLinkDirectory.commit();
                }
                if (newObject.getChildren().stream().filter(jeVisObject -> {
                    try {
                        return jeVisObject.getJEVisClass().equals(emailNotificationClass);
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }).count() == 0) {
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


        } catch (JEVisException e) {
            e.printStackTrace();
        }

        init();

        JEVisClass finalReportLinkClass = reportLinkClass;
        JEVisClass finalReportAttributeClass = reportAttributeClass;
        JEVisClass finalReportPeriodConfigurationClass = reportPeriodConfigurationClass;
        reportWizardDialog.setOnDialogClosed(event -> {
            try {
                if (wizardTyp.equals(ReportWizardDialog.UPDATE)) {
                    List<ReportLink> deleteList = reportLinkList.stream().filter(reportLink -> reportLink.getLinkeStaus().equals(ReportLink.Status.DELETE)).collect(Collectors.toList());
                    List<ReportLink> newList = reportLinkList.stream().filter(reportLink -> reportLink.getLinkeStaus().equals(ReportLink.Status.NEW)).collect(Collectors.toList());
                    List<ReportLink> updateList = reportLinkList.stream().filter(reportLink -> reportLink.getLinkeStaus().equals(ReportLink.Status.UPDATE)).collect(Collectors.toList());
                    Task updateTask = new Task() {
                        @Override
                        protected Object call() throws Exception {

                            updateList.forEach(reportLink -> {
                                try {
                                    reportLink.update();
                                } catch (Exception e) {
                                    logger.error(e);
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.update.title"));
                                    alert.setHeaderText("JEVis Obejct: " + reportLink.getName() + " : " + I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.update.header"));
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
                    JEConfig.getStatusBar().addTask(ReportWizardDialog.class.getName(), updateTask, taskImage, true);

                    for (ReportLink reportLink : deleteList) {
                        delete(reportLink);
                    }
                    ConcurrentHashMap<ReportLink, Boolean> completed = new ConcurrentHashMap<>();
                    for (ReportLink reportLink : newList) {
                        if (newObject.getChildren().size() > 1) {
                            Optional<JEVisObject> linkDirectory = newObject.getChildren().stream().filter(jeVisObject -> {
                                try {
                                    return jeVisObject.getJEVisClassName().equals("Report Link Directory");
                                } catch (JEVisException e) {
                                    throw new RuntimeException(e);
                                }
                            }).findAny();
                            if (linkDirectory.isPresent()) {
                                if (reportLink.getJeVisObject() == null) {
                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            try {
                                                String variableName = reportLink.getTemplateVariableName();

                                                JEVisObject object = linkDirectory.get().buildObject(variableName, finalReportLinkClass);
                                                object.commit();

                                                JEVisAttribute jeVis_id = object.getAttribute("JEVis ID");
                                                JEVisSample jevisIdSample = jeVis_id.buildSample(new DateTime(), reportLink.getjEVisID());
                                                jevisIdSample.commit();

                                                JEVisAttribute optionalAttribute = object.getAttribute("Optional");
                                                JEVisSample sampleOptional = optionalAttribute.buildSample(new DateTime(), reportLink.isOptional());
                                                sampleOptional.commit();

                                                JEVisAttribute templateVariableName = object.getAttribute("Template Variable Name");
                                                JEVisSample templateVariableSample = templateVariableName.buildSample(new DateTime(), variableName);
                                                templateVariableSample.commit();

                                                if (reportType == ReportType.STANDARD) {
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
                                            } catch (JEVisException e) {
                                                e.printStackTrace();
                                                failed();
                                            } finally {
                                                completed.put(reportLink, true);
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
                    Integer maxDataSheetnumber = sheetList.stream().filter(s -> s.contains("Data")).map(s -> {
                        s = s.replaceAll("\\D+", "");
                        if (!s.isEmpty()) {
                            return Integer.valueOf(s);
                        } else {
                            return 0;
                        }
                    }).max(Integer::compareTo).get();
                    for (List<ReportLink> partition : Lists.partition(newList, 17)) {
                        maxDataSheetnumber++;
                        addContent(partition, maxDataSheetnumber);

                    }

                }
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }


            if (getSelections() != null && wizardTyp.equals(ReportWizardDialog.NEW)) {

                JEVisObject reportLinkDirectory = getReportLinkDirectory();
                ConcurrentHashMap<ReportLink, Boolean> completed = new ConcurrentHashMap<>();

                JEConfig.getStatusBar().startProgressJob("reportlinks", getReportLinkList().size() + 1, I18n.getInstance().getString("plugin.object.report.message.creatinglinks") + " ");

                getReportLinkList().forEach(rl -> {
                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            try {
                                String variableName = rl.getTemplateVariableName();

                                JEVisObject object = reportLinkDirectory.buildObject(variableName, finalReportLinkClass);
                                object.commit();

                                JEVisAttribute jeVis_id = object.getAttribute(JC.ReportLink.a_JEVisID);
                                JEVisSample jevisIdSample = jeVis_id.buildSample(new DateTime(), rl.getjEVisID());
                                jevisIdSample.commit();

                                JEVisAttribute optionalAttribute = object.getAttribute(JC.ReportLink.a_Optional);
                                JEVisSample sampleOptional = optionalAttribute.buildSample(new DateTime(), rl.isOptional());
                                sampleOptional.commit();

                                JEVisAttribute templateVariableName = object.getAttribute(JC.ReportLink.a_TemplateVariableName);
                                JEVisSample templateVariableSample = templateVariableName.buildSample(new DateTime(), variableName);
                                templateVariableSample.commit();

                                if (reportType == ReportType.STANDARD) {
                                    JEVisObject reportAttribute = object.buildObject(I18n.getInstance().getString("tree.treehelper.reportattribute.name"), finalReportAttributeClass);
                                    reportAttribute.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportattribute.name"));
                                    reportAttribute.commit();
                                    JEVisAttribute attribute_name = reportAttribute.getAttribute(JC.ReportAttribute.a_AttributeName);

                                    JEVisSample attributeNameSample = attribute_name.buildSample(new DateTime(), rl.getReportAttribute().getAttributeName());
                                    attributeNameSample.commit();

                                    JEVisObject reportPeriodConfiguration = reportAttribute.buildObject(I18n.getInstance().getString("tree.treehelper.reportperiodconfiguration.name"), finalReportPeriodConfigurationClass);
                                    reportPeriodConfiguration.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.reportperiodconfiguration.name"));
                                    reportPeriodConfiguration.commit();

                                    JEVisAttribute aggregationAttribute = reportPeriodConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Aggregation);
                                    JEVisSample aggregationSample = aggregationAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
                                    aggregationSample.commit();

                                    JEVisAttribute manipulationAttribute = reportPeriodConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Manipulation);
                                    JEVisSample manipulationSample = manipulationAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getReportManipulation());
                                    manipulationSample.commit();

                                    JEVisAttribute periodAttribute = reportPeriodConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_Period);
                                    JEVisSample periodSample = periodAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());
                                    periodSample.commit();

                                    JEVisAttribute fixedPeriodAttribute = reportPeriodConfiguration.getAttribute(JC.ReportConfiguration.ReportPeriodConfiguration.a_FixedPeriod);
                                    JEVisSample fixedPeriodSample = fixedPeriodAttribute.buildSample(new DateTime(), rl.getReportAttribute().getReportPeriodConfiguration().getFixedPeriod().toString());
                                    fixedPeriodSample.commit();
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                                failed();
                            } finally {
                                completed.put(rl, true);
                                JEConfig.getStatusBar().progressProgressJob("reportlinks", 1, I18n.getInstance().getString("plugin.object.report.message.finishedlink") + " " + rl.getName());
                                succeeded();
                            }
                            return null;
                        }
                    };

                    JEConfig.getStatusBar().addTask(ReportWizardDialog.class.getName(), task, taskImage, true);
                });

                Task<Void> waitTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (completed.size() == getReportLinkList().size()) {
                            try {
                                JEVisFile template = null;
                                if (reportType == ReportType.STANDARD) {
                                    template = createStandardTemplate(newObject.getName());
                                } else {
                                    template = createAllAttributesTemplate(newObject.getName());
                                }
                                JEVisAttribute templateAttribute = newObject.getAttribute("Template");
                                JEVisSample templateSample = templateAttribute.buildSample(new DateTime(), template);
                                templateSample.commit();

                                JEConfig.getStatusBar().progressProgressJob("reportlinks", 1, I18n.getInstance().getString("plugin.object.report.message.finishedtemplate"));
                                JEConfig.getStatusBar().finishProgressJob("reportlinks", I18n.getInstance().getString("plugin.object.report.message.finishedprocess"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JEVisException ex) {
                                logger.error(ex);
                            }
                        } else {
                            Thread.sleep(1000);
                            call();
                        }

                        return null;
                    }
                };

                JEConfig.getStatusBar().addTask(ReportWizardDialog.class.getName(), waitTask, taskImage, true);
            }
            if (workbook != null) {
                try {
                    JEVisFile template = writeTemplate(newObject.getName());
                    JEVisAttribute jeVisAttribute = newObject.getAttribute("Template");
                    JEVisSample templateSample = jeVisAttribute.buildSample(new DateTime(), template);
                    templateSample.commit();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JEVisException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        reportWizardDialog.show();
    }

    private JEVisFile writeTemplate(String templateName) throws IOException {
        DateTime now = new DateTime();
        templateName += "_template_" + now.toString("YYYYMMdd");
        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
    }

    private void delete(ReportLink reportLink) {
        logger.debug("Delete: ", reportLink);
        Sheet sheet = workbook.getSheet(reportLink.getSheet());
        Cell cell = sheet.getRow(reportLink.getCellAddress().getRow()).getCell(reportLink.getCellAddress().getColumn());
        cell.removeCellComment();
        removeCellContent(sheet, reportLink.getCellAddress());
        try {
            reportLink.getJeVisObject().delete();
        } catch (JEVisException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeCellContent(Sheet sheet, CellAddress cellAddress) {
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

    private XSSFWorkbook createEmptyReportSheet(XSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet(I18n.getInstance().getString("plugin.object.report.dialog.report.titlepage"));
        sheet.setDefaultColumnWidth(3);

        return workbook;

    }


    private void init() {
        reportWizardDialog = new JFXDialog();
        reportWizardDialog.setDialogContainer(JEConfig.getStackPane());
        reportWizardDialog.setOverlayClose(false);
        reportWizardDialog.setMinSize(1000, 800);

        Node header = DialogHeader.getDialogHeader(JEConfig.getSVGImage(Icon.WIZARD_HAT, 32, 32), I18n.getInstance().getString("plugin.object.report.dialog.header"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox vBox = new VBox(6);
        HBox hbox = new HBox(6);

        JFXButton addMultiple = new JFXButton("", JEConfig.getSVGImage(Icon.PLUS, 16, 16));

        JFXComboBox<ReportType> reportTypeComboBox = getReportTypeComboBox();
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                reportType = newValue;
            }
        });

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(4);

//        if (reportLinkList.isEmpty()) {
//            reportLinkList.add(new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("NONE", PeriodMode.CURRENT))));
//        }

        updateGridPane();


        addMultiple.setOnAction(event -> {
            if (reportType == ReportType.STANDARD) {
                openMultiSelect();
            } else {
                openSingleSelect();
            }
        });

        hbox.getChildren().add(gridPane);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        ok.setOnAction(event -> reportWizardDialog.close());

        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> reportWizardDialog.close());

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        vBox.getChildren().add(header);
        vBox.getChildren().add(hbox);
        vBox.getChildren().add(reportTypeComboBox);
        vBox.getChildren().add(addMultiple);
        vBox.getChildren().add(separator);
        vBox.getChildren().add(buttonBar);


        scrollPane.setContent(vBox);

        reportWizardDialog.setContent(scrollPane);

        vBox.setFillWidth(true);
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

        TreeSelectionDialog selectionDialog = new TreeSelectionDialog(reportWizardDialog.getDialogContainer(), ds, new ArrayList<>(), SelectionMode.MULTIPLE, new ArrayList<>(), true);

        selectionDialog.setOnDialogClosed(event -> {
            if (selectionDialog.getResponse() == Response.OK) {
                logger.trace("Selection Done");

                selections = selectionDialog.getUserSelection();
                for (UserSelection us : selections) {

                    ReportLink newLink = new ReportLink("", us.getSelectedObject().getID(), false, "",
                            new ReportAttribute("Value",
                                    new ReportPeriodConfiguration(AggregationPeriod.NONE, ManipulationMode.NONE, PeriodMode.CURRENT, FixedPeriod.NONE)));
                    if (us.getSelectedAttribute() != null) {
                        newLink.getReportAttribute().setAttributeName(us.getSelectedAttribute().getName());
                    } else {
                        newLink.getReportAttribute().setAttributeName("Value");
                    }

                    updateName(newLink);

                    createNewReportLink(true, newLink);
                }
            }
        });
        selectionDialog.show();
    }

    private void openSingleSelect() {

        TreeSelectionDialog selectionDialog = new TreeSelectionDialog(reportWizardDialog.getDialogContainer(), ds, new ArrayList<>(), SelectionMode.SINGLE, new ArrayList<>(), true);

        selectionDialog.setOnDialogClosed(event -> {
            if (selectionDialog.getResponse() == Response.OK) {
                logger.trace("Selection Done");

                selections = selectionDialog.getUserSelection();
                for (UserSelection us : selections) {
                    allAttributesRootObject = us.getSelectedObject();

                    JEVisClass directoryClass = null;
                    try {
                        directoryClass = ds.getJEVisClass("Directory");
                        createAllAttributeLinks(directoryClass, us.getSelectedObject());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        selectionDialog.show();
    }

    private void createAllAttributeLinks(JEVisClass directoryClass, JEVisObject selectedObject) throws JEVisException {

        if (!directoryClass.getHeirs().contains(selectedObject.getJEVisClass())) {
            ReportLink newLink = new ReportLink("", selectedObject.getID(), false, "", null);
            updateName(newLink);
            createNewReportLink(true, newLink);
        }

        try {
            for (JEVisObject jeVisObject : selectedObject.getChildren()) {
                createAllAttributeLinks(directoryClass, jeVisObject);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }


    private void updateGridPane() {
        gridPane.getChildren().clear();
        Label reportExcelCell = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.excelcolumn"));
        Label reportVariableLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.reportlink"));
        Label aggregationLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.aggregation"));
        Label manipulationLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.manipulation"));
        Label periodLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.period"));
        Label optionalLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.optional"));

        gridPane.add(reportExcelCell, 0, 0);
        gridPane.add(reportVariableLabel, 1, 0);
        gridPane.add(aggregationLabel, 2, 0);
        gridPane.add(manipulationLabel, 4, 0);
        gridPane.add(periodLabel, 6, 0);
        gridPane.add(optionalLabel, 8, 0);
        row = 1;

        if (wizardTyp.equals(ReportWizardDialog.UPDATE) && map != null) {
            for (Map.Entry<String, Map<CellAddress, JEVisObject>> sheetSet : map.entrySet())
                for (Map.Entry<CellAddress, JEVisObject> dataSet : sheetSet.getValue().entrySet()) {
                    ReportLink reportLink = buildReportLink(dataSet.getValue(), sheetSet.getKey(), dataSet.getKey());
                    if (reportLink != null) {
                        reportLinkList.add(reportLink);
                    }
                }
        }
        for (ReportLink reportLink : reportLinkList) {
            createBox(reportLink);
            row++;
        }


    }

    private ReportLink buildReportLink(JEVisObject jeVisObject, String sheet, CellAddress cellAddress) {

        ReportLink reportLink = null;
        try {
            reportLink = ReportLink.parseFromJEVisObject(jeVisObject);
            reportLink.setCellAddress(cellAddress);
            reportLink.setSheet(sheet);
            reportLink.setLinkeStaus(ReportLink.Status.FALSE);
        } catch (RuntimeException e) {
            logger.error(jeVisObject, e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.parse.title"));
            alert.setHeaderText("JEVis Obejct: " + jeVisObject.getName() + ": " + jeVisObject.getID() + ": " + I18n.getInstance().getString("plugin.object.report.dialog.wizard.error.parse.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();

        }


        return reportLink;
    }

    private void createNewReportLink(Boolean copy, ReportLink oldReportLink) {
        ReportLink reportLink = null;
        if (!copy) {
            reportLink = new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration(AggregationPeriod.NONE, ManipulationMode.NONE, PeriodMode.CURRENT, FixedPeriod.NONE)));

        } else {
            reportLink = oldReportLink.clone();
        }
        reportLinkList.add(reportLink);
        row++;
        createBox(reportLink);
    }

    private void createBox(ReportLink reportLink) {
        Label excelCellLabel = new Label();
        StringBuilder stringBuilder = new StringBuilder();
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
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation() != null) {
            aggregationPeriodComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
        }

        ReportManipulationBox manipulationComboBox = new ReportManipulationBox();
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
        if (reportLink.getReportAttribute() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration() != null
                && reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode() != null) {
            periodModeComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode());
            if (reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().equals(PeriodMode.FIXED) || reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().equals(PeriodMode.FIXED_TO_REPORT_END)) {
                showFixedPeriodeComboBox(currentRow, fixedPeriodComboBox);
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
                reportLink.setLinkeStaus(ReportLink.Status.UPDATE);
                reportLink.setOptional(newValue);
            }
        });

        JFXButton copyButton = new JFXButton("", JEConfig.getSVGImage(Icon.COPY, 16, 16));

        JFXButton removeButton = new JFXButton("", JEConfig.getSVGImage(Icon.MINUS_CIRCLE, 16, 16));
        removeButton.setOnAction(event -> {
            if (row > 1) {
                if (wizardTyp.equals(ReportWizardDialog.UPDATE)) {
                    reportLink.setLinkeStaus(ReportLink.Status.DELETE);
                    moveNodesGridpane(GridPane.getRowIndex(removeButton));
                    gridPane.getChildren().removeAll(removeButton, excelCellLabel, targetsButton, aggregationPeriodComboBox, manipulationComboBox, tbManipulation, tbPeriod, periodModeComboBox, toggleSwitchPlus, copyButton, tbAggregation);
                    row--;
                } else if (wizardTyp.equals(ReportWizardDialog.NEW)) {
                    reportLinkList.remove(reportLink);
                    Platform.runLater(this::updateGridPane);
                }
            }
        });

        AtomicReference<String> targetString = new AtomicReference<>();
        if (reportLink.getjEVisID() != null) {
            targetString.set(reportLink.getjEVisID().toString());
        }

        if (reportLink.getjEVisID() != null) {
            String target = "";
            if (reportLink.getReportAttribute() != null) {
                target = reportLink.getjEVisID() + ":" + reportLink.getReportAttribute().getAttributeName();
            } else {
                target = reportLink.getjEVisID().toString();
            }
            TargetHelper th = new TargetHelper(ds, target);
            if (th.isValid() && th.targetAccessible()) {
                logger.info("Target Is valid");
                setButtonText(target, targetsButton);
            }
        }

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

            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(reportWizardDialog.getDialogContainer(), ds, new ArrayList<>(), SelectionMode.SINGLE, openList, true);

            selectionDialog.setOnDialogClosed(event1 -> {
                if (selectionDialog.getResponse() == Response.OK) {
                    logger.trace("Selection Done");

                    String newTarget = "";
                    selections = selectionDialog.getUserSelection();
                    for (UserSelection us : selections) {
                        int index = selections.indexOf(us);
                        if (index > 0) newTarget += ";";

                        newTarget += us.getSelectedObject().getID();
                        reportLink.setjEVisID(us.getSelectedObject().getID());

                        Platform.runLater(() -> updateName(reportLink));

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
            selectionDialog.show();
        });

        aggregationPeriodComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setReportAggregation(newValue);
                reportLink.setLinkeStaus(ReportLink.Status.UPDATE);

                Platform.runLater(() -> updateName(reportLink));
            }
        });

        manipulationComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setReportManipulation(newValue);
                reportLink.setLinkeStaus(ReportLink.Status.UPDATE);

                Platform.runLater(() -> updateName(reportLink));
            }
        });

        periodModeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setPeriodMode(newValue);
                reportLink.setLinkeStaus(ReportLink.Status.UPDATE);

                Platform.runLater(() -> updateName(reportLink));

                if (newValue == PeriodMode.FIXED || newValue == PeriodMode.FIXED_TO_REPORT_END) {
                    showFixedPeriodeComboBox(currentRow, fixedPeriodComboBox);
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
                reportLink.setLinkeStaus(ReportLink.Status.UPDATE);

                Platform.runLater(() -> updateName(reportLink));
            }
        });

        copyButton.setOnAction(event -> {
            Platform.runLater(() -> createNewReportLink(true, reportLink));
        });

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


    }

    private void showFixedPeriodeComboBox(int currentRow, ReportFixedPeriodBox fixedPeriodComboBox) {
        Platform.runLater(() -> {
            gridPane.add(fixedPeriodComboBox, 11, currentRow);
        });
    }

    private void moveNodesGridpane(int targetRowIndex) {
        gridPane.getChildren().forEach(node -> {
            final int rowIndex = GridPane.getRowIndex(node);
            if (targetRowIndex < rowIndex) {
                GridPane.setRowIndex(node, rowIndex - 1);
            }
        });
    }

    private void updateName(ReportLink reportLink) {
        JEVisObject object = null;
        try {
            object = ds.getObject(reportLink.getjEVisID());
        } catch (JEVisException e) {
            logger.error("Could not update name for object with id: {}", reportLink.getjEVisID(), e);
        }

        if (object != null) {
            try {
                if (object.getJEVisClass().getName().equals("Clean Data")) {
                    reportLink.setName(object.getParents().get(0).getName());
                } else {
                    reportLink.setName(object.getName());
                }
            } catch (JEVisException e) {
                logger.error("Could not set new Report Link Name for object with id: {}", reportLink.getjEVisID(), e);
            }

            try {
                if (reportType == ReportType.STANDARD) {
                    reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object)
                            + "_" + reportLink.getReportAttribute().getAttributeName()
                            + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation()
                            + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());

                } else {
                    reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object));
                }
            } catch (JEVisException e) {
                logger.error("Could not set new Variable Name for object with id: {}", reportLink.getjEVisID(), e);
            }
        }
    }


    private void setButtonText(String targetString, JFXButton targetsButton) {
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


    public JEVisFile loadTemplate(JEVisFile jeVisFile, List<JEVisObject> listReportLinkObjects) throws IOException, NullPointerException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jeVisFile.getBytes());
        workbook = new XSSFWorkbook(byteArrayInputStream);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetList.add(workbook.getSheetAt(i).getSheetName());
        }
        map = new TreeMap<>();
        for (String sheetName : sheetList) {
            if (sheetName.contains("Data")) {
                logger.debug("load Excel sheet: ", sheetName);
                XSSFSheet sheet = workbook.getSheet(sheetName);
                map.put(sheet.getSheetName(), new TreeMap<>());
                for (Map.Entry<CellAddress, XSSFComment> entry : sheet.getCellComments().entrySet()) {
                    String variable = getVariableFromComment(entry.getValue());
                    Optional<JEVisObject> jevisobject = listReportLinkObjects.stream().filter(jeVisObject -> jeVisObject.getName().equals(variable)).findFirst();
                    if (jevisobject.isPresent()) {
                        map.get(sheet.getSheetName()).put(entry.getKey(), jevisobject.get());
                        logger.debug("load DP: ", entry.getKey(), jevisobject.get().getName());
                    }
                }
            }
        }
        logger.debug(map);

        return null;
    }

    private String getVariableFromComment(XSSFComment xssfComment) {
        String first = "items=";
        String commentString = xssfComment.getString().getString();
        if (commentString.contains("jx:each")) {
            String variable = commentString.substring(commentString.indexOf(first) + first.length(), commentString.indexOf("var=")).replace("\"", "").trim().replace(".value", "");
            return variable;
        } else {
            return null;
        }
    }

    public JEVisFile createStandardTemplate(String templateName) throws IOException {
        if (templateName == null || templateName.equals("")) {
            templateName = "template";
        }
        DateTime now = new DateTime();
        templateName += "_template_" + now.toString("YYYYMMdd");
        XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);

        int width = reportLinkList.size() * 3;
        List<List<ReportLink>> lists = new ArrayList<>();
        if (width > 50) {
            int noOfSubLists = width / 50 + 1;
            for (int i = 0; i < noOfSubLists; i++) {
                List<ReportLink> subList = new ArrayList<>();
                lists.add(subList);
            }

            int i = 0;
            for (int j = 0; j < lists.size(); j++) {
                while (i < reportLinkList.size() && (i * 3) < (50 * (j + 1))) {
                    List<ReportLink> links = lists.get(j);
                    ReportLink reportLink = reportLinkList.get(i);
                    links.add(reportLink);
                    i++;
                }
            }

        } else {
            lists.add(reportLinkList);
        }

        for (List<ReportLink> links : lists) {
            int number = lists.indexOf(links);
            setExcelContent(links, workbook, number, cellStyleDates, cellStyleValues);
        }

        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook = createEmptyReportSheet(workbook);
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
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

        } else {

//            Sheet sheet = workbook.createSheet(allAttributesRootObject.getName()); //create sheet
//            Cell firstCell = getOrCreateCell(sheet, 0, 0);
//            String lastCellColumnName = CellReference.convertNumToColString(sheetWidth);
//            String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + 3 + "\")";
//            addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);

//            columnIndex = 0;
//            maxColumnIndex = 0;
//            rowIndex = 0;
//            maxRowIndex = 0;
//
//            createCellsForChildren(directoryClass, sheet, boldHeaderStyle, defaultStyle, boldStyle, );
//
//            String lastCellColumnName = CellReference.convertNumToColString(maxColumnIndex + 2);
//            String lastCellCommentText = "jx:area(lastCell=\"" + lastCellColumnName + maxRowIndex + "\")";
//            addComment(workbook, sheet, 0, 0, "JEVis", lastCellCommentText);
        }

        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
    }

    private void addContent(List<ReportLink> links, int number) {
        logger.debug("Create Sheet Data" + number, links);

        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "YYYY-MM-dd HH:MM:ss");
        CellStyle cellStyleDates = workbook.createCellStyle();
        cellStyleDates.setDataFormat((short) 165);

        CellStyle cellStyleValues = workbook.createCellStyle();
        cellStyleValues.setDataFormat((short) 4);


        setExcelContent(links, workbook, number, cellStyleDates, cellStyleValues);
    }

    private void setExcelContent(List<ReportLink> links, XSSFWorkbook workbook, int number, CellStyle cellStyleDates, CellStyle cellStyleValues) {
        int sheetWidth = links.size() * 3;
        Sheet sheet = workbook.createSheet("Data" + number); //create sheet
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
            ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(((i + 1) + (counter - 2)), cellStyleDates);
            Cell cell2 = getOrCreateCell(sheet, 2, ((i + 1) + counter - 1));
            cell2.setCellValue("${data.value}");
            cell2.setCellStyle(cellStyleValues);
            ((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(((i + 1) + (counter - 1)), cellStyleValues);
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

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        cell.setCellComment(comment);
    }
}
