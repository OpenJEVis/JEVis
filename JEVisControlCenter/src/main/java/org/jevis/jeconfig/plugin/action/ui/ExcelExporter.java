package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionController;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.joda.time.DateTime;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

public class ExcelExporter {

    private static final Logger logger = LogManager.getLogger(ExcelExporter.class);

    public ExcelExporter(ActionController actionController, List<ExportDialog.Selection> selection) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("XLSX File Destination");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
        fileChooser.getExtensionFilters().addAll(pdfFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);
        fileChooser.setInitialFileName("Actions.xlsx");

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
                    actionTab.getActionPlan().getActionData().forEach(actionData -> {
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

    private CellStyle getCurrencyStyle(XSSFWorkbook workbook) {
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat((short) 166);
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
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

    private void addBorder(Cell cell) {
        cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
        cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
        cell.getCellStyle().setBorderRight(BorderStyle.THIN);
        cell.getCellStyle().setBorderTop(BorderStyle.THIN);
        cell.getCellStyle().setBottomBorderColor(IndexedColors.BLACK.getIndex());
    }

    private Sheet addActionTab(XSSFWorkbook workbook, ActionData actionData) {
        CellStyle currencyStyle = getCurrencyStyle(workbook);
        CellStyle dateStyle = getDateStyle(workbook);
        CellStyle stringStyle = getStringTableStyle(workbook);
        Sheet sheet = workbook.createSheet(actionData.getNrText());
        if (true) {
            //return sheet;
        }


        ActionData names = new ActionData();


        int c_a1 = 1;
        int c_a2 = 2;
        int c_b1 = 4;
        int c_b2 = 5;

/*
        Cell erstelltVonCell = getOrCreateCell(sheet, 1, 0);
        erstelltVonCell.setCellValue("Erstellt von: " + actionData.fromUser.get());


        Cell dateCell = getOrCreateCell(sheet, 1, 4);
        dateCell.setCellValue("Datum:");
        dateCell.setCellStyle(workbook.createCellStyle());
        dateCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);

        Cell dateValueCell = getOrCreateCell(sheet, 1, 4);
        dateValueCell.setCellValue(new Date());
        dateCell.setCellStyle(workbook.createCellStyle());
        dateCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);

 */

        XSSFFont linkFont = workbook.createFont();
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkFont.setUnderline(Font.U_SINGLE);
        CreationHelper createHelper = workbook.getCreationHelper();
        String linkStrg = "'" + actionData.getActionPlan().getName().get() + "'!A1";
        Hyperlink link2 = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        link2.setAddress(linkStrg);


        Cell l_Plan = getOrCreateCell(sheet, 0, c_a1);
        l_Plan.setCellValue("Actionsplan:");

        Cell f_Plan = getOrCreateCell(sheet, 0, c_a2);
        f_Plan.setHyperlink(link2);
        f_Plan.setCellStyle(workbook.createCellStyle());
        f_Plan.getCellStyle().setFont(linkFont);
        f_Plan.setCellValue(actionData.getActionPlan().getName().get());

        int startRow = 2;

        Cell l_nr = getOrCreateCell(sheet, startRow, c_a1);
        l_nr.setCellValue(I18n.getInstance().getString("plugin.action.nr"));
        l_nr.setCellStyle(stringStyle);
        Cell f_nr = getOrCreateCell(sheet, startRow, c_a2);
        f_nr.setCellValue(actionData.getNrText());
        f_nr.setCellStyle(stringStyle);


        Cell l_title = getOrCreateCell(sheet, startRow + 1, c_a1);
        l_title.setCellStyle(stringStyle);
        l_title.setCellValue(names.titleProperty().getName());
        Cell f_title = getOrCreateCell(sheet, startRow + 1, c_a2);
        f_title.setCellStyle(stringStyle);
        f_title.setCellValue(actionData.titleProperty().get());

        Cell l_responsible = getOrCreateCell(sheet, startRow + 2, c_a1);
        f_nr.setCellStyle(stringStyle);
        l_responsible.setCellValue(names.responsibleProperty().getName());
        Cell f_responsible = getOrCreateCell(sheet, startRow + 2, c_a2);
        f_nr.setCellStyle(stringStyle);
        f_responsible.setCellValue(actionData.responsibleProperty().get());


        Cell l_created = getOrCreateCell(sheet, startRow + 3, c_a1);
        l_created.setCellStyle(stringStyle);
        l_created.setCellValue(names.createDateProperty().getName());
        Cell f_created = getOrCreateCell(sheet, startRow + 3, c_a2);
        try {
            f_created.setCellStyle(dateStyle);
            f_created.setCellValue(DateTimeToLocal(actionData.createDate.get()));
        } catch (Exception ex) {

        }

        Cell l_until = getOrCreateCell(sheet, startRow + 4, c_a1);
        l_until.setCellStyle(stringStyle);
        l_until.setCellValue(names.plannedDate.getName());
        Cell f_until = getOrCreateCell(sheet, startRow + 4, c_a2);
        try {
            f_until.setCellStyle(dateStyle);
            f_until.setCellValue(DateTimeToLocal(actionData.plannedDate.get()));
        } catch (Exception ex) {

        }


        Cell l_done = getOrCreateCell(sheet, startRow + 5, c_a1);
        l_done.setCellStyle(stringStyle);
        l_done.setCellValue(names.doneDateProperty().getName());
        Cell f_done = getOrCreateCell(sheet, startRow + 5, c_a2);
        try {
            f_done.setCellStyle(dateStyle);
            f_done.setCellValue(DateTimeToLocal(actionData.doneDateProperty().get()));
        } catch (Exception ex) {

        }


        //Cell processDes = getOrCreateCell(sheet, startRow + 6, c_a1);
        // processDes.setCellStyle(stringStyle);
        //processDes.setCellValue(names.not().getName());
        //Cell evalDes = getOrCreateCell(sheet, startRow + 7, c_a1);
        //evalDes.setCellStyle(stringStyle);

        Cell l_status = getOrCreateCell(sheet, startRow, c_b1, names.statusTags.getName());
        l_status.setCellStyle(stringStyle);
        Cell f_status = getOrCreateCell(sheet, startRow, c_b2, actionData.statusTags.get());
        f_status.setCellStyle(stringStyle);
        Cell l_field = getOrCreateCell(sheet, startRow + 1, c_b1, names.fieldTags.getName());
        l_field.setCellStyle(stringStyle);
        Cell f_field = getOrCreateCell(sheet, startRow + 1, c_b2, actionData.fieldTags.get());
        f_field.setCellStyle(stringStyle);
        Cell l_sue = getOrCreateCell(sheet, startRow + 2, c_b1, I18n.getInstance().getString("actionform.editor.tab.general.seu"));
        l_sue.setCellStyle(stringStyle);
        Cell f_sue = getOrCreateCell(sheet, startRow + 2, c_b2, actionData.seuTagsProperty().get());
        f_sue.setCellStyle(stringStyle);
        Cell l_creator = getOrCreateCell(sheet, startRow + 3, c_b1, names.fromUser.getName());
        l_creator.setCellStyle(stringStyle);
        Cell f_creator = getOrCreateCell(sheet, startRow + 3, c_b2, actionData.fromUser.get());
        f_creator.setCellStyle(stringStyle);
        Cell l_savings = getOrCreateCell(sheet, startRow + 4, c_b1, names.npv.get().einsparung.getName());
        l_savings.setCellStyle(stringStyle);
        Cell f_savings = getOrCreateCell(sheet, startRow + 4, c_b2);
        try {
            f_savings.setCellStyle(getCurrencyStyle(workbook));
            f_savings.setCellValue(actionData.npv.get().einsparung.get());
        } catch (Exception ex) {

        }
        Cell l_invest = getOrCreateCell(sheet, startRow + 5, c_b1, names.npv.get().investment.getName());
        l_invest.setCellStyle(stringStyle);
        Cell f_invest = getOrCreateCell(sheet, startRow + 5, c_b2);
        try {
            f_invest.setCellStyle(currencyStyle);
            f_invest.setCellValue(actionData.npv.get().investment.get());
        } catch (Exception ex) {

        }

        //Cell actionDes = getOrCreateCell(sheet, startRow + 7, c_b1);
        //Cell noteDes = getOrCreateCell(sheet, startRow + 8, c_b1);


        /*--------------------------------------------------------------------------------------*/
        /*Details*/


        int detailStart = 15;
        Cell l_enpi = getOrCreateCell(sheet, detailStart, c_a1);
        l_enpi.setCellValue("EnPI");
        l_enpi.setCellStyle(stringStyle);

        Cell f_enpi = getOrCreateCell(sheet, detailStart, c_a2);
        f_enpi.setCellStyle(stringStyle);
        try {
            f_enpi.setCellValue(actionData.enpi.get().dataObject.getValue());
        } catch (Exception ex) {
        }


        Cell l_enpiRef = getOrCreateCell(sheet, detailStart + 1, c_a1);
        l_enpiRef.setCellStyle(stringStyle);
        l_enpiRef.setCellValue("Verbrauch (Referenz)");

        Cell f_enpiRef = getOrCreateCell(sheet, detailStart + 1, c_a2);
        try {
            f_enpiRef.setCellStyle(stringStyle);
            f_enpiRef.setCellValue(actionData.enpi.get().actualProperty().get());
        } catch (Exception ex) {
        }

        /*--------------------------------------------------------------------------------------*/
        /*Capital*/
        int capitalStart = 18;

        CellStyle stringBox = getStringTableStyle(workbook);

        Cell l_investment = getOrCreateCell(sheet, capitalStart, c_a1);
        l_investment.setCellValue("Investment");
        l_investment.setCellStyle(stringBox);

        Cell f_investment = getOrCreateCell(sheet, capitalStart, c_a2);
        try {
            f_investment.setCellValue(actionData.npv.get().getInvestment());
            f_investment.setCellStyle(getCurrencyStyle(workbook));
        } catch (Exception ex) {
        }


        Cell l_ySavings = getOrCreateCell(sheet, capitalStart + 1, c_a1);
        l_ySavings.setCellValue("Jährliche Einsparung");
        l_ySavings.setCellStyle(stringBox);

        Cell f_ySavings = getOrCreateCell(sheet, capitalStart + 1, c_a2);
        f_ySavings.setCellStyle(getCurrencyStyle(workbook));
        try {
            f_ySavings.setCellValue(actionData.npv.get().einsparung.get());
        } catch (Exception ex) {
        }


        Cell l_interest = getOrCreateCell(sheet, capitalStart + 2, c_a1);
        l_interest.setCellValue("Zinssatz");
        l_interest.setCellStyle(stringBox);

        Cell f_interest = getOrCreateCell(sheet, capitalStart + 2, c_a2);
        try {
            f_interest.setCellValue(actionData.npv.get().amoutYear.get());
        } catch (Exception ex) {
        }
        f_interest.setCellStyle(getCurrencyStyle(workbook));

        Cell l_yCost = getOrCreateCell(sheet, capitalStart + 1, c_b1);
        l_yCost.setCellValue("Jährliche Betriebskosten");
        l_yCost.setCellStyle(stringBox);

        Cell f_yCost = getOrCreateCell(sheet, capitalStart + 1, c_b2);
        f_yCost.setCellStyle(getCurrencyStyle(workbook));
        try {
            f_yCost.setCellValue(actionData.npv.get().runningCost.get());
        } catch (Exception ex) {
        }

        Cell l_runingCost = getOrCreateCell(sheet, capitalStart + 2, c_b1);
        l_runingCost.setCellValue("Jährliche Preissteigerung");
        l_runingCost.setCellStyle(stringBox);

        Cell f_runingCost = getOrCreateCell(sheet, capitalStart + 2, c_b2);
        f_runingCost.setCellStyle(getStringTableStyle(workbook));
        try {
            f_runingCost.setCellValue(actionData.npv.get().inflation.get());
        } catch (Exception ex) {
        }

        Cell l_Years = getOrCreateCell(sheet, capitalStart + 3, c_a1);
        l_Years.setCellValue("Laufzeit");
        l_Years.setCellStyle(stringBox);

        Cell f_Years = getOrCreateCell(sheet, capitalStart + 3, c_a2);
        f_Years.setCellStyle(getStringTableStyle(workbook));
        try {
            f_Years.setCellValue(actionData.npv.get().amoutYear.get());
        } catch (Exception ex) {
        }

        Cell l_overXYears = getOrCreateCell(sheet, capitalStart + 3, c_b1);
        l_overXYears.setCellValue("Amortisation über");
        l_overXYears.setCellStyle(stringBox);

        Cell f_overXYears = getOrCreateCell(sheet, capitalStart + 3, c_b2);
        f_overXYears.setCellStyle(getStringTableStyle(workbook));
        try {
            f_overXYears.setCellValue(actionData.npv.get().overXYear.get());
        } catch (Exception ex) {
        }

        IntStream.rangeClosed(0, c_a1).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, c_a2).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, c_b1).forEach(sheet::autoSizeColumn);
        IntStream.rangeClosed(0, c_b2).forEach(sheet::autoSizeColumn);


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
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
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

        int colldx = -1;
        for (TableColumn<ActionData, ?> actionDataTableColumn : table.getColumns()) {
            if (actionDataTableColumn.isVisible()) {
                colldx++;
                int row = 1;
                Cell headerCell = getOrCreateCell(sheet, row, colldx);
                headerCell.setCellValue(actionDataTableColumn.getText());
                headerCell.setCellStyle(headerStyle);

                for (ActionData data : table.getItems()) {
                    row++;
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
}