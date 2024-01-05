package org.jevis.jeconfig.plugin.action.ui;

import com.google.common.collect.Iterables;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionController;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.ConsumptionData;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.awt.Color;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExcelExporter {

    private static final Logger logger = LogManager.getLogger(ExcelExporter.class);


    private final XSSFColor backgroundColor = new XSSFColor(new Color(231, 230, 230));
    private final XSSFColor boxColor = backgroundColor;
    private final XSSFColor borderColor = backgroundColor;
    private final XSSFColor linkColor = new XSSFColor(new Color(81, 171, 165));
    private final short defaultFontSite = (short) 9;
    private final short textBoxHeight = (short) 4000;


    public ExcelExporter(ActionController actionController, List<ExportDialog.Selection> selections) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("XLSX File Destination");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
        fileChooser.getExtensionFilters().addAll(pdfFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fileChooser.setInitialFileName("Actions_" + simpleDateFormat.format(new Date()) + ".xlsx");
        //fileChooser.setInitialFileName(UUID.randomUUID() + ".xlsx");

        File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
        if (selectedFile != null) {
            JEConfig.setLastPath(selectedFile);
            // createExcelFile(selectedFile);
            XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook
            addStyles(workbook);

            selections.stream().filter(selection -> selection.exportPlan).forEach(selection -> {
                logger.debug(selection.plan);
                addActionPlanSheet(workbook, selection.plan, selection.plan.getTableView(), selection.plan.getTableView().getStatistic(), selection.exportDetail);
            });

            selections.stream().filter(selection -> selection.exportDetail).forEach(selection -> {
                logger.debug(selection.plan);
                if (!selection.plan.getName().get().equals("Übersicht")) {
                    selection.plan.getActionData().sorted(Comparator.comparingInt(o -> o.nr.get())).forEach(actionData -> {
                        try {
                            addActionDetailsSheet(workbook, actionData);
                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }
                    });
                }
            });

            /*
            actionController.getTabPane().getTabs().forEach(tab -> {
                ActionTab actionTab = (ActionTab) tab;
                addActionPlanSheet(workbook, actionTab.getActionPlan(), actionTab.getActionTable(), actionTab.getStatistics());
            });


             */
            /*
            actionController.getTabPane().getTabs().forEach(tab -> {
                ActionTab actionTab = (ActionTab) tab;
                if (!actionTab.getActionPlan().getName().get().equals("Übersicht")) {
                    System.out.println("Action to export: " + (actionTab.getActionPlan().getActionData().size() - 1));
                    actionTab.getActionPlan().getActionData().sorted(Comparator.comparingInt(o -> o.nr.get())).forEach(actionData -> {
                        try {
                            addActionDetailsSheet(workbook, actionData);
                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }
                    });
                }
            });

             */


            try {
                //faster development workaround, remove
                Desktop desktop = Desktop.getDesktop();


                FileOutputStream fileOutputStream = new FileOutputStream(selectedFile);
                workbook.write(fileOutputStream);
                workbook.close();
                fileOutputStream.close();

                desktop.open(selectedFile);
                selectedFile.deleteOnExit();

            } catch (IOException e) {
                logger.error("Could not save file {}", selectedFile, e);
            }

        }


    }

    private static int letterToAlphabetPos(char letter) {
        return Character.toUpperCase(letter) - 64;
    }

    private Sheet addActionPlanSheet(XSSFWorkbook workbook, ActionPlanData actionPlanData, ActionTable table, Statistics statistics, boolean exportDetails) {

        ActionData fakeForName = new ActionData();

        CellStyle dateStyle = getDateStyle(workbook);
        CellStyle currencyStyle = getCurrencyStyle(workbook);
        CellStyle kwhStyle = getConsumptionStyle(workbook);
        setTableBorder(dateStyle);
        setTableBorder(currencyStyle);
        setTableBorder(kwhStyle);

        /*
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

         */

        CellStyle sumCurrencyStyle = getCurrencyStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        sumCurrencyStyle.setFont(font);

        XSSFSheet sheet = workbook.createSheet(actionPlanData.getName().get());
        if (actionPlanData.getName().get().equals("Übersicht")) {
            sheet.setTabColor(new XSSFColor(new java.awt.Color(37, 150, 190)));
        } else {
            sheet.setTabColor(new XSSFColor(new java.awt.Color(55, 72, 148)));
        }


        XSSFFont headerFont = workbook.createFont();
        //font.setFontHeightInPoints((short) 10);
        //font.setFontName("Arial");
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        //headerFont.setBold(true);
        headerFont.setItalic(false);

        XSSFFont linkFont = workbook.createFont();
        linkFont.setColor(linkColor.getIndex());//IndexedColors.BLUE.getIndex());
        linkFont.setUnderline(Font.U_SINGLE);

        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(backgroundColor);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);

        CellStyle tableCellStyle = workbook.createCellStyle();
        tableCellStyle.setBorderBottom(BorderStyle.THIN);
        tableCellStyle.setBorderLeft(BorderStyle.THIN);
        tableCellStyle.setBorderRight(BorderStyle.THIN);
        tableCellStyle.setBorderTop(BorderStyle.THIN);
        tableCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        /*
        Cell updateCell = getOrCreateCell(sheet, 0, 0);
        updateCell.setCellStyle(getStringTableStyle(workbook));
        updateCell.setCellValue("Stand: " + DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.now()));
         */

        int firstRow = 4;
        int lastRow = 1;
        int firstCol = 1;
        int lastCol = 1;

        sheet.setColumnWidth(1, 5);
        sheet.getPrintSetup().setLandscape(true);

        CellStyle sheetHeaderStyle = workbook.createCellStyle();
        XSSFFont sheetHeaderFont = workbook.createFont();
        sheetHeaderFont.setFontHeightInPoints((short) 18);
        sheetHeaderStyle.setFont(sheetHeaderFont);
        Cell sheetHeader = getOrCreateCell(sheet, firstRow - 3, 1);
        sheetHeader.setCellValue(I18n.getInstance().getString("plugin.action.name") + " - " + actionPlanData.getName().get());
        sheetHeader.setCellStyle(sheetHeaderStyle);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Cell creationHeader = getOrCreateCell(sheet, firstRow - 2, 1);
        //creationHeader.setCellValue(LocalDateTime.now());
        //creationHeader.setCellStyle(dateStyle);
        creationHeader.setCellValue("Stand: " + formatter.format(LocalDateTime.now()));


        int colldx = 0;
        for (TableColumn<ActionData, ?> actionDataTableColumn : table.getVisibleLeafColumns()) {
            if (actionDataTableColumn.isVisible()) {
                System.out.println("Export Colum: " + actionDataTableColumn.getText());
                colldx++;
                lastCol = colldx;
                int row = firstRow;
                Cell headerCell = getOrCreateCell(sheet, row, colldx);
                headerCell.setCellValue(actionDataTableColumn.getText());
                headerCell.setCellStyle(headerStyle);

                ActionData lastElement = Iterables.getLast(table.getItems());

                for (ActionData data : table.getItems()) {
                    row++;
                    lastRow++;

                    try {
                        Cell valueCell = getOrCreateCell(sheet, row, colldx);
                        valueCell.setCellStyle(tableCellStyle);

                        if (actionDataTableColumn.getText().equals(fakeForName.nrProperty().getName())) {
                            valueCell.setCellValue(data.getNrText());
                            CreationHelper createHelper = workbook.getCreationHelper();
                            String linkStrg = "'" + data.getNrText() + "'!A1";
                            Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                            link2.setAddress(linkStrg);
                            valueCell.setHyperlink(link2);
                            XSSFCellStyle tableCellStyle2 = workbook.createCellStyle();

                            valueCell.setCellStyle(tableCellStyle2);
                            valueCell.getCellStyle().setFont(linkFont);

                            valueCell.getCellStyle().setBorderBottom(BorderStyle.THIN);
                            valueCell.getCellStyle().setBorderLeft(BorderStyle.THIN);
                            valueCell.getCellStyle().setBorderRight(BorderStyle.THIN);
                            valueCell.getCellStyle().setBorderTop(BorderStyle.THIN);
                            tableCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());


                            if (data.nr.get() == lastElement.nr.get()) {
                                Cell sumCell = getOrCreateCell(sheet, row + 2, colldx);
                                sumCell.setCellValue(I18n.getInstance().getString("plugin.action.export.sum"));
                            }
                            sheet.setColumnWidth(colldx, toSize(15));
                            //sheet.autoSizeColumn(colldx);

                        }

                        if (actionDataTableColumn.getText().equals(I18n.getInstance().getString("plugin.action.filter.plan"))) {
                            valueCell.setCellValue(data.getActionPlan().getName().get());
                            sheet.autoSizeColumn(colldx);

                        }


                        if (actionDataTableColumn.getText().equals(fakeForName.fromUserProperty().getName())) {
                            valueCell.setCellValue(data.fromUser.get());
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.desciptionProperty().getName())) {
                            valueCell.setCellValue(data.desciptionProperty().get());
                            sheet.autoSizeColumn(colldx);
                        }


                        if (actionDataTableColumn.getText().equals(fakeForName.noteProperty().getName())) {
                            valueCell.setCellValue(data.noteProperty().get());
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.mediaTagsProperty().getName())) {
                            valueCell.setCellValue(data.mediaTagsProperty().get());
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.statusTagsProperty().getName())) {
                            valueCell.setCellValue(data.statusTagsProperty().get());

                            CellStyle statusSumStyle = workbook.createCellStyle();
                            statusSumStyle.setAlignment(HorizontalAlignment.RIGHT);

                            if (data.nr.get() == lastElement.nr.get()) {
                                int statusIndex = 0;
                                for (String s : actionPlanData.getStatustags()) {
                                    statusIndex++;
                                    Cell statusSumCell = getOrCreateCell(sheet, row + 2 + statusIndex, colldx);
                                    //statusSumCell.setCellStyle(kwhStyle);
                                    statusSumCell.setCellValue(statistics.getStatusAmount(s).getValue());
                                    statusSumCell.setCellStyle(statusSumStyle);
                                }
                            }
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.title.getName())) {
                            valueCell.setCellValue(data.title.get());

                            if (data.nr.get() == lastElement.nr.get()) {
                                Cell sumCell = getOrCreateCell(sheet, row + 2, colldx);
                                sumCell.setCellValue(statistics.getTextSumSinceImplementation());
                                //einsparung seint umsetzung
                            }
                            //sheet.autoSizeColumn(colldx);
                            sheet.setColumnWidth(colldx, toSize(50));
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.responsible.getName())) {
                            valueCell.setCellValue(data.responsible.get());
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.createDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.createDate.get()));
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.doneDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.doneDate.get()));
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.plannedDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.plannedDate.get()));
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.npv.get().investmentProperty().getName())) {
                            valueCell.setCellStyle(currencyStyle);
                            valueCell.setCellValue(data.npv.get().investmentProperty().get());

                            if (data.nr.get() == lastElement.nr.get()) {
                                Cell sumCell = getOrCreateCell(sheet, row + 2, colldx);
                                String cellLetter = CellReference.convertNumToColString(sumCell.getColumnIndex());
                                String formula = "SUM(" +
                                        cellLetter + (firstRow + 2) +
                                        ":" + cellLetter + (row + 1) + ")";
                                sumCell.setCellFormula(formula);
                                sumCell.setCellStyle(sumCurrencyStyle);
                            }
                            sheet.autoSizeColumn(colldx);

                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.npv.get().einsparung.getName())) {
                            valueCell.setCellStyle(currencyStyle);
                            valueCell.setCellValue(data.npv.get().einsparung.get());

                            if (data.nr.get() == lastElement.nr.get()) {
                                Cell sumCell = getOrCreateCell(sheet, row + 2, colldx);
                                String cellLetter = CellReference.convertNumToColString(sumCell.getColumnIndex());
                                String formula = "SUM(" +
                                        cellLetter + (firstRow + 2) +
                                        ":" + cellLetter + (row + 1) + ")";
                                sumCell.setCellFormula(formula);
                                sumCell.setCellStyle(sumCurrencyStyle);
                            }
                            sheet.autoSizeColumn(colldx);
                        }

                        if (actionDataTableColumn.getText().equals(I18n.getInstance().getString("plugin.action.consumption.diff"))) {
                            valueCell.setCellStyle(kwhStyle);
                            valueCell.setCellValue(data.consumption.get().diff.get());
                            //System.out.println("data.enpi.get().diffProperty().get(): " + data.enpi.get().diffProperty().get());

                            CellStyle mediumSumStyle = getConsumptionStyle(workbook);
                            mediumSumStyle.setAlignment(HorizontalAlignment.RIGHT);

                            if (data.nr.get() == lastElement.nr.get()) {
                                Cell sumCell = getOrCreateCell(sheet, row + 2, colldx);
                                String cellLetter = CellReference.convertNumToColString(sumCell.getColumnIndex());
                                String formula = "SUM(" +
                                        cellLetter + (firstRow + 2) +
                                        ":" + cellLetter + (row + 1) + ")";
                                sumCell.setCellFormula(formula);
                                sumCell.setCellStyle(kwhStyle);

                                int mediumIndex = 0;
                                for (String s : actionPlanData.getMediumTags()) {
                                    mediumIndex++;
                                    Cell mediumSumCell = getOrCreateCell(sheet, row + 2 + mediumIndex, colldx);
                                    mediumSumCell.setCellStyle(mediumSumStyle);
                                    mediumSumCell.setCellValue(statistics.getMediumSum(s).getValue());
                                }
                            }
                            sheet.autoSizeColumn(colldx);

                        }

                        if (actionDataTableColumn.getText().equals(I18n.getInstance().getString("plugin.action.donedays"))) {

                            Cell daysCell = getOrCreateCell(sheet, row, colldx);
                            try {
                                System.out.println("-Export " + I18n.getInstance().getString("plugin.action.donedays") + " Date: " + data.doneDateProperty().get().withTimeAtStartOfDay());
                                int daysRunning = Days.daysBetween(data.doneDateProperty().get().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                                daysCell.setCellValue(daysRunning);
                            } catch (Exception ex) {
                            }
                            //AString cellLetter = CellReference.convertNumToColString(sumCell.getColumnIndex());
                        }

                        if (actionDataTableColumn.getText().equals(I18n.getInstance().getString("plugin.action.doneruntime"))) {
                            Cell daysCell = getOrCreateCell(sheet, row, colldx);
                            try {
                                int daysRunning = Days.daysBetween(data.doneDate.get().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                                double net = ((daysRunning) * (data.consumption.get().diff.get() / 365));
                                //setText(NumerFormating.getInstance().getDoubleFormate().format(net) + " kWh");
                                daysCell.setCellValue(net);
                                daysCell.setCellStyle(kwhStyle);
                            } catch (Exception ex) {
                            }
                            //AString cellLetter = CellReference.convertNumToColString(sumCell.getColumnIndex());
                        }



                        /*
                        sheet.autoSizeColumn(colldx);
                        if (sheet.getColumnWidth(colldx) > 8000) {
                            sheet.setColumnWidth(colldx, 8000);
                        }
                        */

                    } catch (Exception ex) {
                        logger.error("Error in cell: {}:{}", colldx, row, ex);
                    }
                }


            }

        }
        //sheet.setAutoFilter(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));


        // IntStream.rangeClosed(0, 1).forEach(sheet::autoSizeColumn);
        // IntStream.rangeClosed(3, 15).forEach(sheet::autoSizeColumn);

        //IntStream.rangeClosed(0, colldx).forEach(sheet::autoSizeColumn);


        return sheet;

    }

    private Sheet addActionDetailsSheet(XSSFWorkbook workbook, ActionData actionData) {
        logger.debug("Export Tab: " + actionData.nr.get() + " Name: " + actionData.title.get());
        CellStyle currencyStyle = getCurrencyStyle(workbook);//workbook.createCellStyle();;
        CellStyle dateStyle = workbook.createCellStyle();//getDateStyle(workbook);
        CellStyle stringStyle = workbook.createCellStyle();//getStringTableStyle(workbook);
        CellStyle linkStyle = workbook.createCellStyle();//workbook.createCellStyle();;
        CellStyle consumptionStyle = getConsumptionStyle(workbook);//workbook.createCellStyle();;
        setDefaultFont(workbook, currencyStyle);
        setDefaultFont(workbook, dateStyle);
        setDefaultFont(workbook, stringStyle);
        setDefaultFont(workbook, linkStyle);
        setDefaultFont(workbook, consumptionStyle);

        dateStyle.setDataFormat((short) 165);
        stringStyle.setAlignment(HorizontalAlignment.LEFT);
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        Sheet sheet = workbook.createSheet(actionData.getNrText());
        if (true) {
            //return sheet;
        }

        int boxWidth = 7;
        int boxStartCol = 1;
        int startRow = 4;
        int colLeftSpace = 1;
        int firstLabelCol = 2;
        int firstValueCol = 3;
        int colBetween = 4;
        int secLabelCol = 5;
        int secValueCol = 6;
        int colRightSpace = 7;

        String titleName = actionData.getActionPlan().getName().get() + " #" + actionData.nr.get();
        setBoxBorder(workbook, sheet, titleName, startRow - 2, boxWidth, 42);

        ActionData names = new ActionData();


        sheet.setColumnWidth(0, toSize(0.25));
        sheet.setColumnWidth(firstLabelCol, toSize(18));
        sheet.setColumnWidth(secLabelCol, toSize(18));
        sheet.setColumnWidth(colBetween, toSize(2));
        sheet.setColumnWidth(colLeftSpace, toSize(1));
        sheet.setColumnWidth(colRightSpace, toSize(1));
        sheet.setColumnWidth(firstValueCol, toSize(20));
        sheet.setColumnWidth(secValueCol, toSize(20));
        sheet.setColumnWidth(boxWidth + 2, toSize(5));

        /* Print area */
        sheet.setAutobreaks(false);
        sheet.setColumnBreak(boxWidth + 2);
        sheet.setRowBreak(38);
        sheet.setDisplayGridlines(false);

        XSSFFont linkFont = workbook.createFont();
        linkFont.setColor(linkColor.getIndex());//IndexedColors.BLUE.getIndex());
        linkFont.setUnderline(Font.U_SINGLE);
        CreationHelper createHelper = workbook.getCreationHelper();
        String linkStrg = "'" + actionData.getActionPlan().getName().get() + "'!A1";
        Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        if (actionData.getActionPlan().getName().get().equals(I18n.getInstance().getString("plugin.action.plan.overview"))) {
            link2.setLabel(I18n.getInstance().getString("plugin.action.plan.overview"));
        } else {
            link2.setLabel(I18n.getInstance().getString("plugin.action.plan.overview") + " - " + actionData.getActionPlan().getName().get());
        }

        link2.setAddress(linkStrg);
        linkStyle.setFont(linkFont);

        getOrCreateCell(sheet, 1, 0).setHyperlink(link2);
        getOrCreateCell(sheet, 1, 0).setCellStyle(linkStyle);


        /* Label Left Side */
        getOrCreateCell(sheet, startRow, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.nr"));
        getOrCreateCell(sheet, startRow + 1, firstLabelCol).setCellValue(names.titleProperty().getName());
        getOrCreateCell(sheet, startRow + 2, firstLabelCol).setCellValue(names.responsibleProperty().getName());
        getOrCreateCell(sheet, startRow + 3, firstLabelCol).setCellValue(names.createDateProperty().getName());
        getOrCreateCell(sheet, startRow + 4, firstLabelCol).setCellValue(names.plannedDate.getName());
        getOrCreateCell(sheet, startRow + 5, firstLabelCol).setCellValue(names.doneDate.getName());

        /* Label Right Side */
        //getOrCreateCell(sheet, startRow, secLabelCol).setCellValue("Standort");
        getOrCreateCell(sheet, startRow, secLabelCol).setCellValue(names.statusTags.getName());
        getOrCreateCell(sheet, startRow + 1, secLabelCol).setCellValue(names.fieldTags.getName());
        getOrCreateCell(sheet, startRow + 2, secLabelCol).setCellValue(I18n.getInstance().getString("actionform.editor.tab.general.seu"));
        getOrCreateCell(sheet, startRow + 3, secLabelCol).setCellValue(names.fromUser.getName());
        getOrCreateCell(sheet, startRow + 4, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.npv.saving"));
        getOrCreateCell(sheet, startRow + 5, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.npv.invest"));
        //getOrCreateCell(sheet, startRow + 5, secLabelCol).setCellValue(names.titleProperty().getName());

        /* Values Left Side */
        getOrCreateCell(sheet, startRow, firstValueCol).setCellValue(actionData.getNrText());
        getOrCreateCell(sheet, startRow, firstValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 1, firstValueCol).setCellValue(actionData.title.getValue());
        getOrCreateCell(sheet, startRow + 1, firstValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 2, firstValueCol).setCellValue(actionData.responsibleProperty().getValue());
        getOrCreateCell(sheet, startRow + 2, firstValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 3, firstValueCol).setCellValue(DateTimeToLocal(actionData.createDateProperty().get()));
        getOrCreateCell(sheet, startRow + 3, firstValueCol).setCellStyle(dateStyle);
        getOrCreateCell(sheet, startRow + 4, firstValueCol).setCellValue(DateTimeToLocal(actionData.plannedDate.get()));
        getOrCreateCell(sheet, startRow + 4, firstValueCol).setCellStyle(dateStyle);
        getOrCreateCell(sheet, startRow + 5, firstValueCol).setCellValue(DateTimeToLocal(actionData.doneDate.get()));
        getOrCreateCell(sheet, startRow + 5, firstValueCol).setCellStyle(dateStyle);

        /* Values Right Side */
        //getOrCreateCell(sheet, startRow, secValueCol).setCellValue(actionData.getActionPlan().getName().get());
        //getOrCreateCell(sheet, startRow, secValueCol).setHyperlink(link2);
        //getOrCreateCell(sheet, startRow, secValueCol).setCellStyle(linkStyle);
        getOrCreateCell(sheet, startRow, secValueCol).setCellValue(actionData.statusTags.get());
        getOrCreateCell(sheet, startRow, secValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 1, secValueCol).setCellValue(actionData.fieldTags.get().replaceAll(";", ","));
        getOrCreateCell(sheet, startRow + 1, secValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 2, secValueCol).setCellValue(actionData.seuTagsProperty().get().replaceAll(";", ","));
        getOrCreateCell(sheet, startRow + 2, secValueCol).setCellStyle(dateStyle);
        getOrCreateCell(sheet, startRow + 3, secValueCol).setCellValue(actionData.fromUser.get());
        getOrCreateCell(sheet, startRow + 3, secValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 4, secValueCol).setCellValue(actionData.npv.get().einsparung.get());
        getOrCreateCell(sheet, startRow + 4, secValueCol).setCellStyle(currencyStyle);
        getOrCreateCell(sheet, startRow + 5, secValueCol).setCellValue(actionData.npv.get().investment.get());
        getOrCreateCell(sheet, startRow + 5, secValueCol).setCellStyle(currencyStyle);


        drawHorizontalLine(workbook, sheet, startRow + 7, boxStartCol, boxWidth);


        /* TextBoxes */
        CellStyle textBoxStyle = workbook.createCellStyle();
        textBoxStyle.setAlignment(HorizontalAlignment.LEFT);
        textBoxStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        textBoxStyle.setWrapText(true);
        setDefaultFont(workbook, textBoxStyle);
        //setCellBorder(textBoxStyle, BorderStyle.THIN, this.borderColor);

        getOrCreateCell(sheet, startRow + 8, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.description"));
        getOrCreateCell(sheet, startRow + 8, firstLabelCol).setCellStyle(stringStyle);
        sheet.getRow(startRow + 9).setHeight(textBoxHeight);
        Cell descTextBox = getOrCreateCell(sheet, startRow + 9, firstLabelCol, 1, 2);
        descTextBox.setCellStyle(textBoxStyle);
        descTextBox.setCellValue(actionData.desciptionProperty().getValue());
        CellRangeAddress range1 = new CellRangeAddress(startRow + 9, startRow + 9, firstLabelCol, firstValueCol);
        setCellBoxBorder(sheet, range1, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());


        getOrCreateCell(sheet, startRow + 8, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.measureDescription"));
        getOrCreateCell(sheet, startRow + 8, secLabelCol).setCellStyle(stringStyle);
        Cell measureTextBox = getOrCreateCell(sheet, startRow + 9, secLabelCol, 1, 2);
        measureTextBox.setCellValue(actionData.noteEnergieflussProperty().getValue());
        measureTextBox.setCellStyle(textBoxStyle);
        CellRangeAddress range2 = new CellRangeAddress(startRow + 9, startRow + 9, secLabelCol, secValueCol);
        setCellBoxBorder(sheet, range2, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());

        getOrCreateCell(sheet, startRow + 10, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.noteBewertet"));
        getOrCreateCell(sheet, startRow + 10, firstLabelCol).setCellStyle(stringStyle);
        sheet.getRow(startRow + 11).setHeight(textBoxHeight);
        Cell evaluationTextBox = getOrCreateCell(sheet, startRow + 11, firstLabelCol, 1, 2);
        evaluationTextBox.setCellStyle(textBoxStyle);
        evaluationTextBox.setCellValue(actionData.noteBewertetProperty().getValue());
        CellRangeAddress range3 = new CellRangeAddress(startRow + 11, startRow + 11, firstLabelCol, firstValueCol);
        setCellBoxBorder(sheet, range3, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());


        getOrCreateCell(sheet, startRow + 10, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.note"));
        getOrCreateCell(sheet, startRow + 10, secLabelCol).setCellStyle(stringStyle);
        Cell noteTextBox = getOrCreateCell(sheet, startRow + 11, secLabelCol, 1, 2);
        noteTextBox.setCellStyle(textBoxStyle);
        noteTextBox.setCellValue(actionData.noteProperty().getValue());
        CellRangeAddress range4 = new CellRangeAddress(startRow + 11, startRow + 11, secLabelCol, secValueCol);
        setCellBoxBorder(sheet, range4, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());


        drawHorizontalLine(workbook, sheet, startRow + 13, boxStartCol, boxWidth);

        /* ----------------------- Details ---------------------------------*/

        getOrCreateCell(sheet, startRow + 15, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.editor.tab.deteils.medium"));
        getOrCreateCell(sheet, startRow + 15, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 16, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.before"));
        getOrCreateCell(sheet, startRow + 16, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 17, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.after"));
        getOrCreateCell(sheet, startRow + 17, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 18, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.diff"));
        getOrCreateCell(sheet, startRow + 18, firstLabelCol).setCellStyle(stringStyle);

        getOrCreateCell(sheet, startRow + 15, secLabelCol).setCellValue(names.enpi.getName());
        getOrCreateCell(sheet, startRow + 16, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiabefore"));
        getOrCreateCell(sheet, startRow + 16, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 17, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiafter"));
        getOrCreateCell(sheet, startRow + 17, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 18, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiabechange"));
        getOrCreateCell(sheet, startRow + 18, secLabelCol).setCellStyle(stringStyle);

        getOrCreateCell(sheet, startRow + 15, firstValueCol).setCellValue(I18n.getInstance().getString("plugin.action.enpi"));
        getOrCreateCell(sheet, startRow + 15, firstValueCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 16, firstValueCol).setCellValue(actionData.enpi.get().actualProperty().get());
        getOrCreateCell(sheet, startRow + 16, firstValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 17, firstValueCol).setCellValue(actionData.enpi.get().afterProperty().get());
        getOrCreateCell(sheet, startRow + 17, firstValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 18, firstValueCol).setCellValue(actionData.enpi.get().diffProperty().get());
        getOrCreateCell(sheet, startRow + 18, firstValueCol).setCellStyle(consumptionStyle);

        String enpiName = "";
        try {
            enpiName = ConsumptionData.getObjectName(actionData.getObject().getDataSource(), names.enpi.get());
        } catch (Exception ex) {
        }
        getOrCreateCell(sheet, startRow + 15, secValueCol).setCellValue(enpiName);
        getOrCreateCell(sheet, startRow + 16, secValueCol).setCellValue(actionData.enpi.get().actualProperty().get());
        getOrCreateCell(sheet, startRow + 16, secValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 17, secValueCol).setCellValue(actionData.enpi.get().afterProperty().get());
        getOrCreateCell(sheet, startRow + 17, secValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 18, secValueCol).setCellValue(actionData.enpi.get().diffProperty().get());
        getOrCreateCell(sheet, startRow + 18, secValueCol).setCellStyle(consumptionStyle);

        getOrCreateCell(sheet, startRow + 20, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.noteBewertet"));
        getOrCreateCell(sheet, startRow + 20, firstLabelCol).setCellStyle(stringStyle);
        sheet.getRow(startRow + 21).setHeight(textBoxHeight);
        Cell correctionTextBox = getOrCreateCell(sheet, startRow + 21, firstLabelCol, 1, 2);
        correctionTextBox.setCellStyle(textBoxStyle);
        correctionTextBox.setCellValue(actionData.noteCorrectionProperty().getValue());
        setCellBoxBorder(sheet, new CellRangeAddress(startRow + 21, startRow + 21, firstLabelCol, firstValueCol),
                BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());


        getOrCreateCell(sheet, startRow + 20, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.note"));
        getOrCreateCell(sheet, startRow + 20, secLabelCol).setCellStyle(stringStyle);
        Cell nextActionBox = getOrCreateCell(sheet, startRow + 21, secLabelCol, 1, 2);
        nextActionBox.setCellStyle(textBoxStyle);
        nextActionBox.setCellValue(actionData.noteAlternativeMeasuresProperty().getValue());
        setCellBoxBorder(sheet, new CellRangeAddress(startRow + 21, startRow + 21, secLabelCol, secValueCol)
                , BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());


        drawHorizontalLine(workbook, sheet, startRow + 23, boxStartCol, boxWidth);


        /* -----------------------  NPV  ---------------------------------------------- */

        CellStyle percentStyle = getPercentStyle(workbook);
        setDefaultFont(workbook, percentStyle);
        CellStyle yearsStyle = getYearsStyle(workbook);
        setDefaultFont(workbook, yearsStyle);

        getOrCreateCell(sheet, startRow + 25, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.interestRate"));
        getOrCreateCell(sheet, startRow + 25, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 26, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.term"));
        getOrCreateCell(sheet, startRow + 26, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 27, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.totalSavings"));
        getOrCreateCell(sheet, startRow + 27, firstLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 28, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.totalInvestment"));
        getOrCreateCell(sheet, startRow + 28, firstLabelCol).setCellStyle(stringStyle);

        getOrCreateCell(sheet, startRow + 25, secLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.annualIncrease"));
        getOrCreateCell(sheet, startRow + 25, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 26, secLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.amortization"));
        getOrCreateCell(sheet, startRow + 26, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 27, secLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.totalNet"));
        getOrCreateCell(sheet, startRow + 27, secLabelCol).setCellStyle(stringStyle);

        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellValue(actionData.npv.get().interestRate.get());
        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellStyle(percentStyle);
        getOrCreateCell(sheet, startRow + 26, firstValueCol).setCellValue(actionData.npv.get().amoutYear.get());
        getOrCreateCell(sheet, startRow + 26, firstValueCol).setCellStyle(yearsStyle);
        getOrCreateCell(sheet, startRow + 27, firstValueCol).setCellValue(actionData.npv.get().sumAuszahlung.get());
        getOrCreateCell(sheet, startRow + 27, firstValueCol).setCellStyle(currencyStyle);
        getOrCreateCell(sheet, startRow + 28, firstValueCol).setCellValue(actionData.npv.get().sumEinzahlung.get());
        getOrCreateCell(sheet, startRow + 28, firstValueCol).setCellStyle(currencyStyle);


        getOrCreateCell(sheet, startRow + 25, secValueCol).setCellValue(actionData.npv.get().inflation.get());
        getOrCreateCell(sheet, startRow + 25, secValueCol).setCellStyle(percentStyle);
        getOrCreateCell(sheet, startRow + 26, secValueCol).setCellValue(actionData.npv.get().amoutYearProperty().get());
        getOrCreateCell(sheet, startRow + 26, secValueCol).setCellStyle(yearsStyle);
        getOrCreateCell(sheet, startRow + 27, secValueCol).setCellValue(actionData.npv.get().sumNetto.get());
        getOrCreateCell(sheet, startRow + 27, secValueCol).setCellStyle(currencyStyle);


        drawHorizontalLine(workbook, sheet, startRow + 30, boxStartCol, boxWidth);

        CellStyle questionStyle = workbook.createCellStyle();
        questionStyle.setIndention((short) 2);
        setDefaultFont(workbook, questionStyle);

        /** Dependency's **/
        Cell qt1 = getOrCreateCell(sheet, startRow + 31, firstLabelCol, 1, 2);
        qt1.setCellValue(I18n.getInstance().getString("plugin.action.dependencies.title"));
        qt1.setCellStyle(stringStyle);

        Cell q1 = getOrCreateCell(sheet, startRow + 32, firstLabelCol, 1, 2);
        q1.setCellValue(I18n.getInstance().getString("plugin.action.checklist.needMeter"));
        q1.setCellStyle(questionStyle);

        Cell q2 = getOrCreateCell(sheet, startRow + 33, firstLabelCol, 1, 2);
        q2.setCellValue(I18n.getInstance().getString("plugin.action.checklist.affectOtherProcess"));
        q2.setCellStyle(questionStyle);

        getOrCreateCell(sheet, startRow + 32, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedAdditionalMeters.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 32, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 33, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isAffectsOtherProcess.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 33, secLabelCol).setCellStyle(stringStyle);

        /** Documents **/
        Cell qt2 = getOrCreateCell(sheet, startRow + 35, firstLabelCol, 1, 2);
        qt2.setCellValue(I18n.getInstance().getString("plugin.action.needdocchange.title"));
        qt2.setCellStyle(stringStyle);


        Cell qt2_1 = getOrCreateCell(sheet, startRow + 36, firstLabelCol, 1, 2);
        qt2_1.setCellValue(I18n.getInstance().getString("plugin.action.checklist.needProcessDoc"));
        qt2_1.setCellStyle(questionStyle);

        Cell qt2_2 = getOrCreateCell(sheet, startRow + 37, firstLabelCol, 1, 2);
        qt2_2.setCellValue(I18n.getInstance().getString("plugin.action.checklist.needWorkInstDoc"));
        qt2_2.setCellStyle(questionStyle);

        Cell qt2_3 = getOrCreateCell(sheet, startRow + 38, firstLabelCol, 1, 2);
        qt2_3.setCellValue(I18n.getInstance().getString("plugin.action.checklist.needtestDoc"));
        qt2_3.setCellStyle(questionStyle);

        Cell qt2_4 = getOrCreateCell(sheet, startRow + 39, firstLabelCol, 1, 2);
        qt2_4.setCellValue(I18n.getInstance().getString("plugin.action.checklist.needOtherDoc"));
        qt2_4.setCellStyle(questionStyle);

        getOrCreateCell(sheet, startRow + 36, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedProcessDocument.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 36, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 37, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedWorkInstruction.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 37, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 38, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedTestInstruction.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 38, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 39, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedDrawing.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 39, secLabelCol).setCellStyle(stringStyle);
        getOrCreateCell(sheet, startRow + 40, secLabelCol).setCellValue(actionData.checkListDataProperty().get().isNeedOther.get() ? "Ja" : "Nein");
        getOrCreateCell(sheet, startRow + 40, secLabelCol).setCellStyle(stringStyle);

        // drawHorizontalLine(workbook, sheet, startRow + 42, boxStartCol, boxWidth);

/*
        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellValue(actionData.npv.get().einsparung.get());
        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellStyle(currencyStyle);

        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellValue(actionData.npv.get().einsparung.get());
        getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellStyle(currencyStyle);




        //

        getOrCreateCell(sheet, startRow + 25, secValueCol).setCellValue(actionData.npv.get().runningCost.get());
        getOrCreateCell(sheet, startRow + 25, secValueCol).setCellStyle(currencyStyle);
        //getOrCreateCell(sheet, startRow + 25, firstValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 26, secValueCol).setCellValue(actionData.npv.get().inflation.get());
        getOrCreateCell(sheet, startRow + 25, secValueCol).setCellStyle(currencyStyle);
        //getOrCreateCell(sheet, startRow + 26, firstValueCol).setCellStyle(consumptionStyle);

        // getOrCreateCell(sheet, startRow + 27, firstValueCol).setCellStyle(consumptionStyle);

 */



            /*

        CellStyle headerStyle = createTableHeaderStyle(workbook);

        getOrCreateCell(sheet, startRow + 29, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.nvp.year"));
        getOrCreateCell(sheet, startRow + 29, firstLabelCol).setCellStyle(headerStyle);

        getOrCreateCell(sheet, startRow + 29, firstValueCol).setCellValue(I18n.getInstance().getString("plugin.action.nvp.deposit"));
        getOrCreateCell(sheet, startRow + 29, firstValueCol).setCellStyle(headerStyle);

        getOrCreateCell(sheet, startRow + 29, colBetween).setCellValue(I18n.getInstance().getString("plugin.action.nvp.investment"));
        getOrCreateCell(sheet, startRow + 29, colBetween).setCellStyle(headerStyle);

        getOrCreateCell(sheet, startRow + 29, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.nvp.netamount"));
        getOrCreateCell(sheet, startRow + 29, secLabelCol).setCellStyle(headerStyle);

        getOrCreateCell(sheet, startRow + 29, secValueCol).setCellValue(I18n.getInstance().getString("plugin.action.nvp.cdp"));
        getOrCreateCell(sheet, startRow + 29, secValueCol).setCellStyle(headerStyle);


        actionData.npv.get().update();
        actionData.npv.get().updateResults();





        getOrCreateCell(sheet, startRow + 30, firstValueCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp.label.overRuntime"));
        getOrCreateCell(sheet, startRow + 30, secLabelCol).setCellValue(
                I18n.getInstance().getString("actionform.editor.tab.captial.amortoveryear")
                        + actionData.npv.get().overXYear.get());

        getOrCreateCell(sheet, startRow + 31, firstLabelCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp"));
        //getOrCreateCell(sheet, startRow + 30, firstLabelCol).setCellStyle(headerStyle);

        getOrCreateCell(sheet, startRow + 32, firstValueCol).setCellValue(I18n.getInstance().getString("plugin.action.nvp.deposit"));
        //getOrCreateCell(sheet, startRow + 31, firstValueCol).setCellStyle(headerStyle);




         int tableRow = 30;
        CellStyle currencyTableStyle = getCurrencyStyle(workbook);
        currencyTableStyle.setBorderBottom(BorderStyle.THIN);
        currencyTableStyle.setBorderLeft(BorderStyle.THIN);
        currencyTableStyle.setBorderRight(BorderStyle.THIN);
        currencyTableStyle.setBorderTop(BorderStyle.THIN);
        for (NPVYearData npvYearData : actionData.npv.get().npvYears) {
            getOrCreateCell(sheet, startRow + tableRow, firstLabelCol).setCellValue(npvYearData.getYear());
            getOrCreateCell(sheet, startRow + tableRow, firstLabelCol).setCellStyle(currencyTableStyle);

            getOrCreateCell(sheet, startRow + tableRow, firstValueCol).setCellValue(npvYearData.getDeposit());
            getOrCreateCell(sheet, startRow + tableRow, firstValueCol).setCellStyle(currencyTableStyle);

            getOrCreateCell(sheet, startRow + tableRow, colBetween).setCellValue(npvYearData.getInvestment());
            getOrCreateCell(sheet, startRow + tableRow, colBetween).setCellStyle(currencyTableStyle);

            getOrCreateCell(sheet, startRow + tableRow, secLabelCol).setCellValue(npvYearData.getNetamount());
            getOrCreateCell(sheet, startRow + tableRow, secLabelCol).setCellStyle(currencyTableStyle);

            getOrCreateCell(sheet, startRow + tableRow, secValueCol).setCellValue(npvYearData.getDiscountedCashFlow());
            getOrCreateCell(sheet, startRow + tableRow, secValueCol).setCellStyle(currencyTableStyle);
            tableRow++;
        }

         int startUnderTable = startRow + tableRow + 1;
        getOrCreateCell(sheet, startUnderTable + 1, firstValueCol).setCellValue(I18n.getInstance().getString("actionform.action.nvp"));
        getOrCreateCell(sheet, startUnderTable + 1, secLabelCol).setCellStyle(headerStyle);

         */





/*

        IntStream.rangeClosed(0, firstLabelCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, firstValueCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, secValueCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, c_b2).forEach(sheet::autoSizeColumn);
*/

        return null;
    }

    private void addStyles(XSSFWorkbook workbook) {
        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "dd.MM.YYYY");
        XSSFDataFormat currencyFormatDates = workbook.createDataFormat();
        currencyFormatDates.putFormat((short) 166, "#,##0.00 €");
    }

    private CellStyle getDateStyle(XSSFWorkbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat((short) 165);

        return dateStyle;
    }

    private void setTableBorder(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
    }

    private void setBoxBorder(XSSFWorkbook workbook, Sheet sheet, String name, int startRow, int width, int height) {
        XSSFCellStyle leftBorder = workbook.createCellStyle();
        leftBorder.setBorderLeft(BorderStyle.THICK);
        leftBorder.setBorderRight(BorderStyle.NONE);
        leftBorder.setLeftBorderColor(boxColor);//borderColor);

        XSSFCellStyle rightBorder = workbook.createCellStyle();
        rightBorder.setBorderRight(BorderStyle.THICK);
        rightBorder.setBorderLeft(BorderStyle.NONE);
        rightBorder.setRightBorderColor(boxColor);//borderColor);

        XSSFCellStyle bottomBorder = workbook.createCellStyle();
        bottomBorder.setBorderBottom(BorderStyle.THICK);
        bottomBorder.setBorderRight(BorderStyle.THICK);
        bottomBorder.setLeftBorderColor(boxColor);//borderColor);

        XSSFCellStyle topBorder = workbook.createCellStyle();
        topBorder.setBorderRight(BorderStyle.THICK);
        topBorder.setBorderLeft(BorderStyle.THICK);
        topBorder.setLeftBorderColor(boxColor);//borderColor);
        topBorder.setRightBorderColor(boxColor);//borderColor);
        topBorder.setFillBackgroundColor(boxColor);//borderColor);
        topBorder.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        topBorder.setFillForegroundColor(boxColor);//borderColor);
        topBorder.setAlignment(HorizontalAlignment.CENTER);
        topBorder.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        font.setFontHeightInPoints((short) 14);
        topBorder.setFont(font);
        Row topRow = sheet.createRow(startRow);
        topRow.setHeight((short) 500);


        sheet.setColumnWidth(startRow, 2 * 256);
        sheet.setColumnWidth(10, 2 * 256);


        Cell topCells = getOrCreateCell(sheet, startRow, 1, 1, width);
        topCells.setCellStyle(topBorder);
        topCells.setCellValue(name);
        CellUtil.setFont(topCells, font);


        List<Cell> leftCells = getCellsInArea(sheet, startRow + 1, 1, height, 1);
        leftCells.forEach(cell1 -> {
            cell1.setCellStyle(leftBorder);
        });

        List<Cell> rightCells = getCellsInArea(sheet, startRow + 1, width, height, 1);
        rightCells.forEach(cell1 -> {
            cell1.setCellStyle(rightBorder);
        });


        drawBoxEndLine(workbook, sheet, startRow + 1 + height, 1, width);


        //Cell leftCells = getOrCreateCell(sheet, 3, 2, 49, 1);
        //leftCells.setCellStyle(leftBorder);

        // Cell rightCells = getOrCreateCell(sheet, 3, 12, 49, 1);
        // rightCells.setCellStyle(rightBorder);

        //Cell bottomCells = getOrCreateCell(sheet, 51, 2, 1, 12);
        //bottomCells.setCellStyle(bottomBorder);
    }

    private void removeAllBorders(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.NONE);
        cellStyle.setBorderLeft(BorderStyle.NONE);
        cellStyle.setBorderRight(BorderStyle.NONE);
        cellStyle.setBorderTop(BorderStyle.NONE);
    }

    private void setDefaultFont(XSSFWorkbook workbook, CellStyle cellStyle) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(defaultFontSite);//(short) 9);//(short) (9 * 20)
        cellStyle.setFont(font);
    }

    private CellStyle getCurrencyStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        //currencyStyle.setDataFormat((short) 166);
        //#.##0 /€
        currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0 €"));

        return currencyStyle;
    }

    private CellStyle getPercentStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        //currencyStyle.setDataFormat((short) 166);
        currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("0 \"%\""));

        return currencyStyle;
    }

    private CellStyle getYearsStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        //currencyStyle.setDataFormat((short) 166);
        currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("0 \"Jahre\""));

        return currencyStyle;
    }


    private CellStyle getConsumptionStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0 \"kWh\""));
        return currencyStyle;
    }

    private CellStyle getStringTableStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    private CellStyle getValueStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    private void addBorder(Cell cell) {
        cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
        cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
        cell.getCellStyle().setBorderRight(BorderStyle.THIN);
        cell.getCellStyle().setBorderTop(BorderStyle.THIN);
        cell.getCellStyle().setBottomBorderColor(IndexedColors.BLACK.getIndex());
    }

    private void drawHorizontalLine(XSSFWorkbook workbook, Sheet sheet, int row, int col, int colSpan) {
        XSSFCellStyle lineBorder = workbook.createCellStyle();
        lineBorder.setBorderTop(BorderStyle.THICK);
        lineBorder.setTopBorderColor(boxColor);

        for (int i = col; i < (col + colSpan); i++) {
            if (i == col) { /* left Border */
                XSSFCellStyle leftBorder = workbook.createCellStyle();
                leftBorder.setBorderTop(BorderStyle.THICK);
                leftBorder.setBorderLeft(BorderStyle.THICK);
                leftBorder.setTopBorderColor(borderColor);
                leftBorder.setLeftBorderColor(borderColor);

                getOrCreateCell(sheet, row, i).setCellStyle(leftBorder);
            } else {
                getOrCreateCell(sheet, row, i).setCellStyle(lineBorder);
            }
            if (i == (col + colSpan - 1)) {
                XSSFCellStyle rightBorder = workbook.createCellStyle();
                rightBorder.setBorderTop(BorderStyle.THICK);
                rightBorder.setBorderRight(BorderStyle.THICK);
                rightBorder.setTopBorderColor(borderColor);
                rightBorder.setRightBorderColor(borderColor);

                getOrCreateCell(sheet, row, i).setCellStyle(rightBorder);
            }
        }

    }

    private void drawBoxEndLine(XSSFWorkbook workbook, Sheet sheet, int row, int col, int colSpan) {
        XSSFCellStyle lineBorder = workbook.createCellStyle();
        lineBorder.setBorderBottom(BorderStyle.THICK);
        lineBorder.setBottomBorderColor(borderColor);

        for (int i = col; i < (col + colSpan); i++) {
            if (i == col) { /* left Border */
                XSSFCellStyle leftBorder = workbook.createCellStyle();
                leftBorder.setBorderBottom(BorderStyle.THICK);
                leftBorder.setBorderLeft(BorderStyle.THICK);
                leftBorder.setBottomBorderColor(borderColor);
                leftBorder.setLeftBorderColor(borderColor);

                getOrCreateCell(sheet, row, i).setCellStyle(leftBorder);
            } else {
                getOrCreateCell(sheet, row, i).setCellStyle(lineBorder);
            }
            if (i == (col + colSpan - 1)) {
                XSSFCellStyle rightBorder = workbook.createCellStyle();
                rightBorder.setBorderBottom(BorderStyle.THICK);
                rightBorder.setBorderRight(BorderStyle.THICK);
                rightBorder.setBottomBorderColor(borderColor);
                rightBorder.setRightBorderColor(borderColor);

                getOrCreateCell(sheet, row, i).setCellStyle(rightBorder);
            }
        }

    }

    private void setCellBoxBorder(Sheet sheet, CellRangeAddress rangeAddress, BorderStyle borderStyle, short borderColor) {
        RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setTopBorderColor(borderColor, rangeAddress, sheet);
        RegionUtil.setBottomBorderColor(borderColor, rangeAddress, sheet);
        RegionUtil.setLeftBorderColor(borderColor, rangeAddress, sheet);
        RegionUtil.setRightBorderColor(borderColor, rangeAddress, sheet);
    }

    /**
     * Excel has special size measurement.
     *
     * @param size you see in Excel
     * @return
     */
    private short toSize(double size) {
        return (short) (size * 256);
    }

    public CellStyle createTableHeaderStyle(XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerFont.setItalic(false);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);

        return headerStyle;
    }


    private LocalDate DateTimeToLocal(DateTime dateTime) {
        if (dateTime == null) return null;
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }


    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx, String object) {
        org.apache.poi.ss.usermodel.Cell cell = getOrCreateCell(sheet, rowIdx, colIdx);
        cell.setCellValue(object);
        return cell;
    }

    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx) {
        return getOrCreateCell(sheet, rowIdx, colIdx, 1, 1);
    }


    private org.apache.poi.ss.usermodel.Cell getOrCreateCell(Sheet sheet, int rowIdx, int colIdx, int rowSpan, int colSpan) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }

        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }

        if (rowSpan > 1 || colSpan > 1) {
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + rowSpan - 1, colIdx, colIdx + colSpan - 1));
        }

        return cell;
    }

    private List<Cell> getCellsInArea(Sheet sheet, int rowIdx, int colIdx, int rowSpan, int colSpan) {
        List<Cell> cellList = new ArrayList<>();
        int col = colIdx;
        while (col < (colIdx + colSpan)) {
            int row = rowIdx;
            while (row < (rowIdx + rowSpan)) {
                Cell cell = getOrCreateCell(sheet, row, col);
                cellList.add(cell);
                row++;
            }
            col++;
        }
        return cellList;
    }
}
