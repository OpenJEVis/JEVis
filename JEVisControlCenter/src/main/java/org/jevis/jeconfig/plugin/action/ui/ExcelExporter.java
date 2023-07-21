package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class ExcelExporter {

    private static final Logger logger = LogManager.getLogger(ExcelExporter.class);

    private final short borderColor = IndexedColors.INDIGO.getIndex();

    public ExcelExporter(ActionController actionController, List<ExportDialog.Selection> selection) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("XLSX File Destination");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
        fileChooser.getExtensionFilters().addAll(pdfFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);
        //fileChooser.setInitialFileName("Actions.xlsx");
        fileChooser.setInitialFileName(UUID.randomUUID() + ".xlsx");

        File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
        if (selectedFile != null) {
            JEConfig.setLastPath(selectedFile);
            // createExcelFile(selectedFile);
            XSSFWorkbook workbook = new XSSFWorkbook(); //create workbook
            addStyles(workbook);
            actionController.getTabPane().getTabs().forEach(tab -> {
                ActionTab actionTab = (ActionTab) tab;
                addActionPlanSheet(workbook, actionTab.getActionPlan(), actionTab.getActionTable());
            });

            actionController.getTabPane().getTabs().forEach(tab -> {
                ActionTab actionTab = (ActionTab) tab;
                if (!actionTab.getActionPlan().getName().get().equals("Übersicht")) {
                    actionTab.getActionPlan().getActionData().sorted(Comparator.comparingInt(o -> o.nr.get())).forEach(actionData -> {
                        try {
                            addActionTab(workbook, actionData);
                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }
                    });
                }
            });


            try {
                //faster delvelopment workaround, remove
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


    private void addStyles(XSSFWorkbook workbook) {
        XSSFDataFormat dataFormatDates = workbook.createDataFormat();
        dataFormatDates.putFormat((short) 165, "dd.MM.YYYY");
        XSSFDataFormat currencyFormatDates = workbook.createDataFormat();
        currencyFormatDates.putFormat((short) 166, "#,##0.00 €");
    }

    private CellStyle getDateStyle(XSSFWorkbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat((short) 165);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        return dateStyle;
    }

    private void setBoxBorder(XSSFWorkbook workbook, Sheet sheet, String name, int width) {
        CellStyle leftBorder = workbook.createCellStyle();
        leftBorder.setBorderLeft(BorderStyle.THICK);
        leftBorder.setBorderRight(BorderStyle.NONE);
        leftBorder.setLeftBorderColor(borderColor);

        CellStyle rightBorder = workbook.createCellStyle();
        rightBorder.setBorderRight(BorderStyle.THICK);
        rightBorder.setBorderLeft(BorderStyle.NONE);
        rightBorder.setRightBorderColor(borderColor);

        CellStyle bottomBorder = workbook.createCellStyle();
        bottomBorder.setBorderBottom(BorderStyle.THICK);
        bottomBorder.setBorderRight(BorderStyle.THICK);
        bottomBorder.setLeftBorderColor(borderColor);

        CellStyle topBorder = workbook.createCellStyle();
        topBorder.setBorderRight(BorderStyle.THICK);
        topBorder.setBorderLeft(BorderStyle.THICK);
        topBorder.setLeftBorderColor(borderColor);
        topBorder.setRightBorderColor(borderColor);
        topBorder.setFillBackgroundColor(borderColor);
        topBorder.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        topBorder.setFillForegroundColor(borderColor);
        topBorder.setAlignment(HorizontalAlignment.CENTER);
        topBorder.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        font.setFontHeightInPoints((short) 14);
        topBorder.setFont(font);
        Row topRow = sheet.createRow(1);
        topRow.setHeight((short) 500);


        sheet.setColumnWidth(1, 2 * 256);
        sheet.setColumnWidth(10, 2 * 256);


        Cell topCells = getOrCreateCell(sheet, 1, 1, 1, width);
        topCells.setCellStyle(topBorder);
        topCells.setCellValue(name);
        CellUtil.setFont(topCells, font);


        List<Cell> leftCells = getCellsInArea(sheet, 2, 1, 49, 1);
        leftCells.forEach(cell1 -> {
            cell1.setCellStyle(leftBorder);
        });

        List<Cell> rightCells = getCellsInArea(sheet, 2, width, 49, 1);
        rightCells.forEach(cell1 -> {
            cell1.setCellStyle(rightBorder);
        });

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

    private CellStyle getCurrencyStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat((short) 166);
        return currencyStyle;
    }

    private CellStyle getConsumptionStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#.##0 \"kWh\""));
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
        CellStyle lineBorder = workbook.createCellStyle();
        lineBorder.setBorderTop(BorderStyle.THICK);
        lineBorder.setTopBorderColor(borderColor);

        for (int i = col; i < (col + colSpan); i++) {
            if (i == col) { /* left Border */
                CellStyle leftBorder = workbook.createCellStyle();
                leftBorder.setBorderTop(BorderStyle.THICK);
                leftBorder.setBorderLeft(BorderStyle.THICK);
                leftBorder.setTopBorderColor(borderColor);
                leftBorder.setLeftBorderColor(borderColor);

                getOrCreateCell(sheet, row, i).setCellStyle(leftBorder);
            } else {
                getOrCreateCell(sheet, row, i).setCellStyle(lineBorder);
            }
            if (i == (col + colSpan - 1)) {
                CellStyle rightBorder = workbook.createCellStyle();
                rightBorder.setBorderTop(BorderStyle.THICK);
                rightBorder.setBorderRight(BorderStyle.THICK);
                rightBorder.setTopBorderColor(borderColor);
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
    private short toSize(int size) {
        return (short) (size * 256);
    }

    private Sheet addActionTab(XSSFWorkbook workbook, ActionData actionData) {
        CellStyle currencyStyle = getCurrencyStyle(workbook);//workbook.createCellStyle();;
        CellStyle dateStyle = workbook.createCellStyle();//getDateStyle(workbook);
        CellStyle stringStyle = workbook.createCellStyle();//getStringTableStyle(workbook);
        CellStyle linkStyle = workbook.createCellStyle();//workbook.createCellStyle();;
        CellStyle consumptionStyle = getConsumptionStyle(workbook);//workbook.createCellStyle();;

        dateStyle.setDataFormat((short) 165);
        stringStyle.setAlignment(HorizontalAlignment.LEFT);
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        Sheet sheet = workbook.createSheet(actionData.getNrText());
        if (true) {
            //return sheet;
        }

        int boxWidth = 7;
        int boxStartCol = 1;
        String titleName = actionData.getActionPlan().getName().get() + " #" + actionData.getActionPlan().getNextActionNr();
        setBoxBorder(workbook, sheet, titleName, boxWidth);

        ActionData names = new ActionData();


        int startRow = 3;
        int colLeftSpace = 1;
        int firstLabelCol = 2;
        int firstValueCol = 3;
        int colBetween = 4;
        int secLabelCol = 5;
        int secValueCol = 6;
        int colRightSpace = 7;

        sheet.setColumnWidth(0, toSize(2));
        sheet.setColumnWidth(firstLabelCol, toSize(20));
        sheet.setColumnWidth(secLabelCol, toSize(20));
        sheet.setColumnWidth(colBetween, toSize(3));
        sheet.setColumnWidth(colLeftSpace, toSize(3));
        sheet.setColumnWidth(colRightSpace, toSize(3));
        sheet.setColumnWidth(firstValueCol, toSize(30));
        sheet.setColumnWidth(secValueCol, toSize(30));
        sheet.setColumnWidth(boxWidth + 2, toSize(5));

        /* Print area */
        sheet.setAutobreaks(false);
        sheet.setColumnBreak(boxWidth + 2);
        sheet.setRowBreak(38);
        sheet.setDisplayGridlines(false);

        XSSFFont linkFont = workbook.createFont();
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkFont.setUnderline(Font.U_SINGLE);
        CreationHelper createHelper = workbook.getCreationHelper();
        String linkStrg = "'" + actionData.getActionPlan().getName().get() + "'!A1";
        Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        link2.setAddress(linkStrg);
        linkStyle.setFont(linkFont);


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
        //setCellBorder(textBoxStyle, BorderStyle.THIN, this.borderColor);

        getOrCreateCell(sheet, startRow + 8, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.description"));
        sheet.getRow(startRow + 9).setHeight((short) 2000);
        Cell descTextBox = getOrCreateCell(sheet, startRow + 9, firstLabelCol, 1, 2);
        descTextBox.setCellStyle(textBoxStyle);
        descTextBox.setCellValue(actionData.desciptionProperty().getValue());
        CellRangeAddress range1 = new CellRangeAddress(startRow + 9, startRow + 9, firstLabelCol, firstValueCol);
        setCellBoxBorder(sheet, range1, BorderStyle.THIN, borderColor);


        getOrCreateCell(sheet, startRow + 8, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.measureDescription"));
        Cell measureTextBox = getOrCreateCell(sheet, startRow + 9, secLabelCol, 1, 2);
        measureTextBox.setCellValue(actionData.noteEnergieflussProperty().getValue());
        measureTextBox.setCellStyle(textBoxStyle);
        CellRangeAddress range2 = new CellRangeAddress(startRow + 9, startRow + 9, secLabelCol, secValueCol);
        setCellBoxBorder(sheet, range2, BorderStyle.THIN, borderColor);

        getOrCreateCell(sheet, startRow + 10, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.noteBewertet"));
        sheet.getRow(startRow + 11).setHeight((short) 2000);
        Cell evaluationTextBox = getOrCreateCell(sheet, startRow + 11, firstLabelCol, 1, 2);
        evaluationTextBox.setCellStyle(textBoxStyle);
        evaluationTextBox.setCellValue(actionData.noteBewertetProperty().getValue());
        CellRangeAddress range3 = new CellRangeAddress(startRow + 11, startRow + 11, firstLabelCol, firstValueCol);
        setCellBoxBorder(sheet, range3, BorderStyle.THIN, borderColor);


        getOrCreateCell(sheet, startRow + 10, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.note"));
        Cell noteTextBox = getOrCreateCell(sheet, startRow + 11, secLabelCol, 1, 2);
        noteTextBox.setCellStyle(textBoxStyle);
        noteTextBox.setCellValue(actionData.noteProperty().getValue());
        CellRangeAddress range4 = new CellRangeAddress(startRow + 11, startRow + 11, secLabelCol, secValueCol);
        setCellBoxBorder(sheet, range4, BorderStyle.THIN, borderColor);


        drawHorizontalLine(workbook, sheet, startRow + 13, boxStartCol, boxWidth);

        /* ----------------------- Details ---------------------------------*/

        getOrCreateCell(sheet, startRow + 15, firstLabelCol).setCellValue(names.mediaTags.getName());
        getOrCreateCell(sheet, startRow + 16, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.before"));
        getOrCreateCell(sheet, startRow + 17, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.after"));
        getOrCreateCell(sheet, startRow + 18, firstLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.consumption.diff"));

        getOrCreateCell(sheet, startRow + 15, secLabelCol).setCellValue(names.enpi.getName());
        getOrCreateCell(sheet, startRow + 16, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiabefore"));
        getOrCreateCell(sheet, startRow + 17, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiafter"));
        getOrCreateCell(sheet, startRow + 18, secLabelCol).setCellValue(I18n.getInstance().getString("plugin.action.enpiabechange"));

        getOrCreateCell(sheet, startRow + 15, firstValueCol).setCellValue(names.mediaTags.getValue());
        getOrCreateCell(sheet, startRow + 16, firstValueCol).setCellValue(actionData.consumption.get().actualProperty().get());
        getOrCreateCell(sheet, startRow + 16, firstValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 17, firstValueCol).setCellValue(actionData.consumption.get().afterProperty().get());
        getOrCreateCell(sheet, startRow + 17, firstValueCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 18, firstValueCol).setCellValue(actionData.consumption.get().diffProperty().get());
        getOrCreateCell(sheet, startRow + 18, firstValueCol).setCellStyle(consumptionStyle);

        String enpiName = "";
        try {
            enpiName = ConsumptionData.getObjectName(actionData.getObject().getDataSource(), names.enpi.get());
        } catch (Exception ex) {
        }
        getOrCreateCell(sheet, startRow + 15, secLabelCol).setCellValue(enpiName);
        getOrCreateCell(sheet, startRow + 16, secLabelCol).setCellValue(actionData.enpi.get().actualProperty().get());
        getOrCreateCell(sheet, startRow + 16, secLabelCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 17, secLabelCol).setCellValue(actionData.enpi.get().afterProperty().get());
        getOrCreateCell(sheet, startRow + 17, secLabelCol).setCellStyle(consumptionStyle);
        getOrCreateCell(sheet, startRow + 18, secLabelCol).setCellValue(actionData.enpi.get().diffProperty().get());
        getOrCreateCell(sheet, startRow + 18, secLabelCol).setCellStyle(consumptionStyle);









/*

        Cell l_Plan = getOrCreateCell(sheet, 0, firstLabelCol);
        l_Plan.setCellValue("Standort:");

        Cell f_Plan = getOrCreateCell(sheet, 0, firstValueCol);
        f_Plan.setHyperlink(link2);
        f_Plan.setCellStyle(workbook.createCellStyle());
        f_Plan.getCellStyle().setFont(linkFont);
        f_Plan.setCellValue(actionData.getActionPlan().getName().get());

        int startRow = 2;

        Cell l_nr = getOrCreateCell(sheet, startRow, firstLabelCol);
        l_nr.setCellValue(I18n.getInstance().getString("plugin.action.nr"));
        l_nr.setCellStyle(stringStyle);
        Cell f_nr = getOrCreateCell(sheet, startRow, firstValueCol);
        f_nr.setCellValue(actionData.getNrText());
        f_nr.setCellStyle(stringStyle);


        Cell l_title = getOrCreateCell(sheet, startRow + 1, firstLabelCol);
        l_title.setCellStyle(stringStyle);
        l_title.setCellValue(names.titleProperty().getName());
        Cell f_title = getOrCreateCell(sheet, startRow + 1, firstValueCol);
        f_title.setCellStyle(stringStyle);
        f_title.setCellValue(actionData.titleProperty().get());

        Cell l_responsible = getOrCreateCell(sheet, startRow + 2, firstLabelCol);
        f_nr.setCellStyle(stringStyle);
        l_responsible.setCellValue(names.responsibleProperty().getName());
        Cell f_responsible = getOrCreateCell(sheet, startRow + 2, firstValueCol);
        f_nr.setCellStyle(stringStyle);
        f_responsible.setCellValue(actionData.responsibleProperty().get());


        Cell l_created = getOrCreateCell(sheet, startRow + 3, firstLabelCol);
        l_created.setCellStyle(stringStyle);
        l_created.setCellValue(names.createDateProperty().getName());
        Cell f_created = getOrCreateCell(sheet, startRow + 3, firstValueCol);
        try {
            f_created.setCellStyle(dateStyle);
            f_created.setCellValue(DateTimeToLocal(actionData.createDate.get()));
        } catch (Exception ex) {

        }

        Cell l_until = getOrCreateCell(sheet, startRow + 4, firstLabelCol);
        l_until.setCellStyle(stringStyle);
        l_until.setCellValue(names.plannedDate.getName());
        Cell f_until = getOrCreateCell(sheet, startRow + 4, firstValueCol);
        try {
            f_until.setCellStyle(dateStyle);
            f_until.setCellValue(DateTimeToLocal(actionData.plannedDate.get()));
        } catch (Exception ex) {

        }


        Cell l_done = getOrCreateCell(sheet, startRow + 5, firstLabelCol);
        l_done.setCellStyle(stringStyle);
        l_done.setCellValue(names.doneDateProperty().getName());
        Cell f_done = getOrCreateCell(sheet, startRow + 5, firstValueCol);
        try {
            f_done.setCellStyle(dateStyle);
            f_done.setCellValue(DateTimeToLocal(actionData.doneDateProperty().get()));
        } catch (Exception ex) {

        }


        Cell l_status = getOrCreateCell(sheet, startRow, secValueCol, names.statusTags.getName());
        l_status.setCellStyle(stringStyle);
        Cell f_status = getOrCreateCell(sheet, startRow, c_b2, actionData.statusTags.get());
        f_status.setCellStyle(stringStyle);
        Cell l_field = getOrCreateCell(sheet, startRow + 1, secValueCol, names.fieldTags.getName());
        l_field.setCellStyle(stringStyle);
        Cell f_field = getOrCreateCell(sheet, startRow + 1, c_b2, actionData.fieldTags.get());
        f_field.setCellStyle(stringStyle);
        Cell l_sue = getOrCreateCell(sheet, startRow + 2, secValueCol, I18n.getInstance().getString("actionform.editor.tab.general.seu"));
        l_sue.setCellStyle(stringStyle);
        Cell f_sue = getOrCreateCell(sheet, startRow + 2, c_b2, actionData.seuTagsProperty().get());
        f_sue.setCellStyle(stringStyle);
        Cell l_creator = getOrCreateCell(sheet, startRow + 3, secValueCol, names.fromUser.getName());
        l_creator.setCellStyle(stringStyle);
        Cell f_creator = getOrCreateCell(sheet, startRow + 3, c_b2, actionData.fromUser.get());
        f_creator.setCellStyle(stringStyle);
        Cell l_savings = getOrCreateCell(sheet, startRow + 4, secValueCol, names.npv.get().einsparung.getName());
        l_savings.setCellStyle(stringStyle);
        Cell f_savings = getOrCreateCell(sheet, startRow + 4, c_b2);
        try {
            f_savings.setCellStyle(getCurrencyStyle(workbook));
            f_savings.setCellValue(actionData.npv.get().einsparung.get());
        } catch (Exception ex) {

        }
        Cell l_invest = getOrCreateCell(sheet, startRow + 5, secValueCol, names.npv.get().investment.getName());
        l_invest.setCellStyle(stringStyle);
        Cell f_invest = getOrCreateCell(sheet, startRow + 5, c_b2);
        try {
            f_invest.setCellStyle(currencyStyle);
            f_invest.setCellValue(actionData.npv.get().investment.get());
        } catch (Exception ex) {

        }


*/



        /*--------------------------------------------------------------------------------------*/
        /*Details*/

        /*

        int detailStart = 15;
        Cell l_enpi = getOrCreateCell(sheet, detailStart, firstLabelCol);
        l_enpi.setCellValue("EnPI");
        l_enpi.setCellStyle(stringStyle);

        Cell f_enpi = getOrCreateCell(sheet, detailStart, firstValueCol);
        f_enpi.setCellStyle(stringStyle);
        try {
            f_enpi.setCellValue(actionData.enpi.get().dataObject.getValue());
        } catch (Exception ex) {
        }


        Cell l_enpiRef = getOrCreateCell(sheet, detailStart + 1, firstLabelCol);
        l_enpiRef.setCellStyle(stringStyle);
        l_enpiRef.setCellValue("Verbrauch (Referenz)");

        Cell f_enpiRef = getOrCreateCell(sheet, detailStart + 1, firstValueCol);
        try {
            f_enpiRef.setCellStyle(stringStyle);
            f_enpiRef.setCellValue(actionData.enpi.get().actualProperty().get());
        } catch (Exception ex) {
        }

        /*--------------------------------------------------------------------------------------*/
        /*Capital*/

        /*
        int capitalStart = 18;

        CellStyle stringBox = getStringTableStyle(workbook);

        Cell l_investment = getOrCreateCell(sheet, capitalStart, firstLabelCol);
        l_investment.setCellValue("Investment");
        l_investment.setCellStyle(stringBox);

        Cell f_investment = getOrCreateCell(sheet, capitalStart, firstValueCol);
        try {
            f_investment.setCellValue(actionData.npv.get().getInvestment());
            f_investment.setCellStyle(getCurrencyStyle(workbook));
        } catch (Exception ex) {
        }


        Cell l_ySavings = getOrCreateCell(sheet, capitalStart + 1, firstLabelCol);
        l_ySavings.setCellValue("Jährliche Einsparung");
        l_ySavings.setCellStyle(stringBox);

        Cell f_ySavings = getOrCreateCell(sheet, capitalStart + 1, firstValueCol);
        f_ySavings.setCellStyle(getCurrencyStyle(workbook));
        try {
            f_ySavings.setCellValue(actionData.npv.get().einsparung.get());
        } catch (Exception ex) {
        }


        Cell l_interest = getOrCreateCell(sheet, capitalStart + 2, firstLabelCol);
        l_interest.setCellValue("Zinssatz");
        l_interest.setCellStyle(stringBox);

        Cell f_interest = getOrCreateCell(sheet, capitalStart + 2, firstValueCol);
        try {
            f_interest.setCellValue(actionData.npv.get().amoutYear.get());
        } catch (Exception ex) {
        }
        f_interest.setCellStyle(getCurrencyStyle(workbook));

        Cell l_yCost = getOrCreateCell(sheet, capitalStart + 1, secValueCol);
        l_yCost.setCellValue("Jährliche Betriebskosten");
        l_yCost.setCellStyle(stringBox);

        Cell f_yCost = getOrCreateCell(sheet, capitalStart + 1, c_b2);
        f_yCost.setCellStyle(getCurrencyStyle(workbook));
        try {
            f_yCost.setCellValue(actionData.npv.get().runningCost.get());
        } catch (Exception ex) {
        }

        Cell l_runingCost = getOrCreateCell(sheet, capitalStart + 2, secValueCol);
        l_runingCost.setCellValue("Jährliche Preissteigerung");
        l_runingCost.setCellStyle(stringBox);

        Cell f_runingCost = getOrCreateCell(sheet, capitalStart + 2, c_b2);
        f_runingCost.setCellStyle(getStringTableStyle(workbook));
        try {
            f_runingCost.setCellValue(actionData.npv.get().inflation.get());
        } catch (Exception ex) {
        }

        Cell l_Years = getOrCreateCell(sheet, capitalStart + 3, firstLabelCol);
        l_Years.setCellValue("Laufzeit");
        l_Years.setCellStyle(stringBox);

        Cell f_Years = getOrCreateCell(sheet, capitalStart + 3, firstValueCol);
        f_Years.setCellStyle(getStringTableStyle(workbook));
        try {
            f_Years.setCellValue(actionData.npv.get().amoutYear.get());
        } catch (Exception ex) {
        }

        Cell l_overXYears = getOrCreateCell(sheet, capitalStart + 3, secValueCol);
        l_overXYears.setCellValue("Amortisation über");
        l_overXYears.setCellStyle(stringBox);

        Cell f_overXYears = getOrCreateCell(sheet, capitalStart + 3, c_b2);
        f_overXYears.setCellStyle(getStringTableStyle(workbook));
        try {
            f_overXYears.setCellValue(actionData.npv.get().overXYear.get());
        } catch (Exception ex) {
        }



        IntStream.rangeClosed(0, firstLabelCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, firstValueCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, secValueCol).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, c_b2).forEach(sheet::autoSizeColumn);
*/

        return null;
    }

    private LocalDate DateTimeToLocal(DateTime dateTime) {
        if (dateTime == null) return null;
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    private Sheet addActionPlanSheet(XSSFWorkbook workbook, ActionPlanData actionPlanData, ActionTable table) {

        ActionData fakeForName = new ActionData();

        CellStyle dateStyle = getDateStyle(workbook);
        CellStyle currencyStyle = getCurrencyStyle(workbook);
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        XSSFSheet sheet = workbook.createSheet(actionPlanData.getName().get());
        if (actionPlanData.getName().get().equals("Übersicht")) {
            sheet.setTabColor(new XSSFColor(new java.awt.Color(37, 150, 190)));
        } else {
            sheet.setTabColor(new XSSFColor(new java.awt.Color(55, 72, 148)));
        }


        XSSFFont headerFont = workbook.createFont();
        //font.setFontHeightInPoints((short) 10);
        //font.setFontName("Arial");
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerFont.setItalic(false);

        XSSFFont linkFont = workbook.createFont();
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkFont.setUnderline(Font.U_SINGLE);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
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

        Cell updateCell = getOrCreateCell(sheet, 0, 0);
        updateCell.setCellStyle(getStringTableStyle(workbook));
        updateCell.setCellValue("Stand: " + DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.now()));

        int firstRow = 1;
        int lastRow = 1;
        int firstCol = 1;
        int lastCol = 1;

        int colldx = -1;
        for (TableColumn<ActionData, ?> actionDataTableColumn : table.getColumns()) {
            if (actionDataTableColumn.isVisible()) {
                colldx++;
                lastCol = colldx;
                int row = firstRow;
                Cell headerCell = getOrCreateCell(sheet, row, colldx);
                headerCell.setCellValue(actionDataTableColumn.getText());
                headerCell.setCellStyle(headerStyle);

                for (ActionData data : table.getItems()) {
                    row++;
                    lastRow++;
                    //System.out.println("Data: " + data.getNrText() + " - " + data.desciption.get());
                    //System.out.println("Cell: Col: " + colldx + " row: " + row);

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

                        }

                        if (actionDataTableColumn.getText().equals(I18n.getInstance().getString("plugin.action.filter.plan"))) {
                            valueCell.setCellValue(data.getActionPlan().getName().get());
                        }


                        if (actionDataTableColumn.getText().equals(fakeForName.fromUserProperty().getName())) {
                            valueCell.setCellValue(data.fromUser.get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.desciptionProperty().getName())) {
                            valueCell.setCellValue(data.desciptionProperty().get());
                        }


                        if (actionDataTableColumn.getText().equals(fakeForName.noteProperty().getName())) {
                            valueCell.setCellValue(data.noteProperty().get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.mediaTagsProperty().getName())) {
                            valueCell.setCellValue(data.mediaTagsProperty().get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.statusTagsProperty().getName())) {
                            valueCell.setCellValue(data.statusTagsProperty().get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.title.getName())) {
                            valueCell.setCellValue(data.title.get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.responsible.getName())) {
                            valueCell.setCellValue(data.responsible.get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.createDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.createDate.get()));
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.doneDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.doneDate.get()));
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.plannedDate.getName())) {
                            valueCell.setCellStyle(dateStyle);
                            valueCell.setCellValue(DateTimeToLocal(data.plannedDate.get()));
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.npv.get().investmentProperty().getName())) {
                            valueCell.setCellStyle(currencyStyle);
                            valueCell.setCellValue(data.npv.get().investmentProperty().get());
                        }

                        if (actionDataTableColumn.getText().equals(fakeForName.npv.get().einsparung.getName())) {
                            valueCell.setCellStyle(currencyStyle);
                            valueCell.setCellValue(data.npv.get().einsparung.get());
                        }


                        sheet.autoSizeColumn(colldx);
                        if (sheet.getColumnWidth(colldx) > 8000) {
                            //System.out.println("ColW: " + sheet.getColumnWidth(colldx));
                            sheet.setColumnWidth(colldx, 8000);
                        }

                    } catch (Exception ex) {
                        logger.error("Error in cell: {}:{}", colldx, row, ex);
                    }
                }


            }

        }
        sheet.setAutoFilter(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

        IntStream.rangeClosed(0, 1).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(3, 15).forEach(sheet::autoSizeColumn);

        //IntStream.rangeClosed(0, colldx).forEach(sheet::autoSizeColumn);


        return sheet;

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
