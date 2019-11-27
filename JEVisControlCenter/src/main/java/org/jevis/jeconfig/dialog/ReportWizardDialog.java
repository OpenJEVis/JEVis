package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.*;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ReportAggregationBox;
import org.jevis.jeconfig.tool.ReportPeriodBox;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReportWizardDialog extends Dialog<ButtonType> {
    private static final Logger logger = LogManager.getLogger(ReportWizardDialog.class);
    Image imgMarkAll = new Image(ChartPluginTree.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));

    Tooltip tooltipMarkAll = new Tooltip(I18n.getInstance().getString("plugin.graph.dialog.changesettings.tooltip.forall"));
    public static String ICON = "Startup Wizard_18228.png";
    private JEVisDataSource ds;
    private JEVisObject reportLinkDirectory;
    private JEVisObject emailNotification;
    private List<UserSelection> selections;
    private List<ReportLink> reportLinkList = new ArrayList<>();
    private int row = 0;
    //    private Button addButton;
    private Button addMultiple;
    private GridPane gridPane;

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
        this.initOwner(JEConfig.getStage());
        this.getDialogPane().setMinHeight(600);
        this.setResizable(true);
        this.getDialogPane().setPrefWidth(1220);

        Node header = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("plugin.object.report.dialog.header"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox vBox = new VBox();
        HBox hbox = new HBox();
//        addButton = new Button("", JEConfig.getImage("list-add_3671791.png", 16, 16));
        addMultiple = new Button("", JEConfig.getImage("list-add.png", 16, 16));

        this.setTitle(I18n.getInstance().getString("plugin.object.report.dialog.title"));

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(4);

        if (reportLinkList.isEmpty()) {
            reportLinkList.add(new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("NONE", PeriodMode.CURRENT))));
        }

        updateGridPane();

//        addButton.setOnAction(event -> {
//            createNewReportLink(false, null);
//        });

        addMultiple.setOnAction(event -> {
            openMultiSelect();
        });

        hbox.getChildren().add(gridPane);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(ok, cancel);
        vBox.getChildren().add(header);
        vBox.getChildren().add(hbox);
//        vBox.getChildren().add(addButton);
        vBox.getChildren().add(addMultiple);
        scrollPane.setContent(vBox);
        this.getDialogPane().setContent(scrollPane);
        vBox.setFillWidth(true);
    }

    private void openMultiSelect() {
        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllObjects();
        JEVisTreeFilter allAttributeFilter = SelectTargetDialog.buildAllAttributesFilter();
        allFilter.add(basicFilter);
        allFilter.add(allAttributeFilter);

        SelectTargetDialog selectionDialog = new SelectTargetDialog(allFilter, basicFilter, null, SelectionMode.MULTIPLE);

        List<UserSelection> openList = new ArrayList<>();

        if (selectionDialog.show(
                ds,
                I18n.getInstance().getString("dialog.target.data.title"),
                openList
        ) == SelectTargetDialog.Response.OK) {
            logger.trace("Selection Done");

            selections = selectionDialog.getUserSelection();
            for (UserSelection us : selections) {

                ReportLink newLink = new ReportLink("", us.getSelectedObject().getID(), false, "",
                        new ReportAttribute("Value",
                                new ReportPeriodConfiguration("NONE", PeriodMode.CURRENT)));
                if (us.getSelectedAttribute() != null) {
                    newLink.getReportAttribute().setAttributeName(us.getSelectedAttribute().getName());
                } else {
                    newLink.getReportAttribute().setAttributeName("Value");
                }

                updateName(newLink);

                createNewReportLink(true, newLink);
            }
        }
    }

    private void updateGridPane() {
        gridPane.getChildren().clear();
        Label reportVariableLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.reportlink"));
        Label aggregationLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.aggregation"));
        Label periodLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.period"));
        Label optionalLabel = new Label(I18n.getInstance().getString("plugin.object.report.dialog.header.optional"));

        gridPane.add(reportVariableLabel, 0, 0);
        gridPane.add(aggregationLabel, 1, 0);
        gridPane.add(periodLabel, 2, 0);
        gridPane.add(optionalLabel, 3, 0);
        row = 1;
        for (ReportLink reportLink : reportLinkList) {
            createBox(reportLink);
            row++;
        }
    }

    private void createNewReportLink(Boolean copy, ReportLink oldReportLink) {
        ReportLink reportLink = null;
        if (!copy) {
            reportLink = new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("NONE", PeriodMode.CURRENT)));

        } else {
            reportLink = oldReportLink.clone();
        }
        reportLinkList.add(reportLink);
        row++;
        createBox(reportLink);
    }

    private void createBox(ReportLink reportLink) {
        Button targetsButton = new Button("Select Target");
        ReportAggregationBox aggregationPeriodComboBox = new ReportAggregationBox(FXCollections.observableArrayList(ReportAggregation.values()));
        if (reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation() != null) {
            aggregationPeriodComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
        }

        ImageView imageMarkAllAggregation = new ImageView(imgMarkAll);
        imageMarkAllAggregation.fitHeightProperty().set(13);
        imageMarkAllAggregation.fitWidthProperty().set(13);

        Button tbAggregation = new Button("", imageMarkAllAggregation);
        tbAggregation.setTooltip(tooltipMarkAll);
        tbAggregation.setOnAction(event -> {
            gridPane.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == 1) {
                    if (node instanceof ReportAggregationBox) {
                        ReportAggregationBox reportAggregationBox = (ReportAggregationBox) node;
                        Platform.runLater(() -> reportAggregationBox.getSelectionModel().select(aggregationPeriodComboBox.getSelectionModel().getSelectedItem()));
                    }
                }
            });
        });

        ReportPeriodBox periodModeComboBox = new ReportPeriodBox(FXCollections.observableArrayList(PeriodMode.values()));
        if (reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode() != null) {
            periodModeComboBox.getSelectionModel().select(reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode());
        }

        ImageView imageMarkAllPeriod = new ImageView(imgMarkAll);
        imageMarkAllPeriod.fitHeightProperty().set(13);
        imageMarkAllPeriod.fitWidthProperty().set(13);

        Button tbPeriod = new Button("", imageMarkAllPeriod);
        tbPeriod.setTooltip(tooltipMarkAll);
        tbPeriod.setOnAction(event -> {
            gridPane.getChildren().forEach(node -> {
                if (GridPane.getColumnIndex(node) == 3) {
                    if (node instanceof ReportPeriodBox) {
                        ReportPeriodBox reportPeriodBox = (ReportPeriodBox) node;
                        Platform.runLater(() -> reportPeriodBox.getSelectionModel().select(periodModeComboBox.getSelectionModel().getSelectedItem()));
                    }
                }
            });
        });

        ToggleSwitchPlus toggleSwitchPlus = new ToggleSwitchPlus();
        toggleSwitchPlus.setSelected(reportLink.isOptional());
        toggleSwitchPlus.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.setOptional(newValue);
            }
        });

        Button copyButton = new Button("", JEConfig.getImage("copy_172587.png", 16, 16));

        Button removeButton = new Button("", JEConfig.getImage("list-remove.png", 16, 16));
        removeButton.setOnAction(event -> {
            if (row > 1) {
                reportLinkList.remove(reportLink);
                Platform.runLater(this::updateGridPane);
            }
        });

        AtomicReference<String> targetString = new AtomicReference<>();
        if (reportLink.getjEVisID() != null) {
            targetString.set(reportLink.getjEVisID().toString());
        }

        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllObjects();
        JEVisTreeFilter allAttributeFilter = SelectTargetDialog.buildAllAttributesFilter();
        allFilter.add(basicFilter);
        allFilter.add(allAttributeFilter);

        if (reportLink.getjEVisID() != null) {
            String target = reportLink.getjEVisID() + ":" + reportLink.getReportAttribute().getAttributeName();
            TargetHelper th = new TargetHelper(ds, target);
            if (th.isValid() && th.targetAccessible()) {
                logger.info("Target Is valid");
                setButtonText(target, targetsButton);
            }
        }

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

        aggregationPeriodComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setReportAggregation(newValue);

                Platform.runLater(() -> updateName(reportLink));
            }
        });

        periodModeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                reportLink.getReportAttribute().getReportPeriodConfiguration().setPeriodMode(newValue);

                Platform.runLater(() -> updateName(reportLink));
            }
        });

        copyButton.setOnAction(event -> {
            Platform.runLater(() -> createNewReportLink(true, reportLink));
        });

        gridPane.add(targetsButton, 0, row);
        gridPane.add(aggregationPeriodComboBox, 1, row);
        gridPane.add(tbAggregation, 2, row);
        gridPane.add(periodModeComboBox, 3, row);
        gridPane.add(tbPeriod, 4, row);
        gridPane.add(toggleSwitchPlus, 5, row);
        gridPane.add(copyButton, 6, row);
        gridPane.add(removeButton, 7, row);
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
                reportLink.setTemplateVariableName(CalculationNameFormatter.createVariableName(object)
                        + "_" + reportLink.getReportAttribute().getAttributeName()
                        + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getReportAggregation()
                        + "_" + reportLink.getReportAttribute().getReportPeriodConfiguration().getPeriodMode().toString());
            } catch (JEVisException e) {
                logger.error("Could not set new Variable Name for object with id: {}", reportLink.getjEVisID(), e);
            }
        }
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

    public JEVisFile createStandardTemplate(String templateName) throws IOException {
        if (templateName == null || templateName.equals("")) {
            templateName = "template";
        }
        DateTime now = new DateTime();
        templateName += "_template_" + now.toString(" YYYYMMdd ");
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
                cell2.setCellValue(rl.getReportAttribute().getReportPeriodConfiguration().getReportAggregation());
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

        Path templatePath = Files.createTempFile("template", "xlsx");
        File templateFile = new File(templatePath.toString());
        templateFile.deleteOnExit();
        workbook.write(new FileOutputStream(templateFile));
        workbook.close();
        return new JEVisFileImp(templateName + ".xlsx", templateFile);
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
