/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.itextpdf.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author broder
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException {

//        FileInputStream input_document = new FileInputStream(new File("/Users/broder/NetBeansProjects_env/JEReport/attributes_stresstest.xls"));
        Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
        CreationHelper factory = wb.getCreationHelper();
        Sheet sheet1 = wb.createSheet();
        // When the comment box is visible, have it show in a 1x3 space

        Row firstRow = sheet1.createRow(0);
        for (int i = 0; i < 40; i++) {
            Cell cell = firstRow.createCell(i);
            cell.setCellValue("timestamp" + i);

            if (i == 0) {
                String value = "jx:area(lastCell=\"CZ2\")";
                setCellComment(factory, sheet1, firstRow, cell, value);
            }
        }

        Row secondRow = sheet1.createRow(1);
        for (int i = 0; i < 40; i++) {
            String name = "var" + i;
            Cell cell = secondRow.createCell(i);
            cell.setCellValue("${value.timestamp}");

            String cellAddress = cell.getAddress().formatAsString();

            String value = "jx:each(items=\"" + name + ".value\" var=\"value\" lastCell=\"" + cellAddress + "\")";
            setCellComment(factory, sheet1, secondRow, cell, value);
        }
        

        String fname = "attributes_stresstest.xls";
        if (wb instanceof XSSFWorkbook) {
            fname += "x";
        }
        FileOutputStream out = new FileOutputStream(fname);
        wb.write(out);
        out.close();
    }
//    public static void main(String[] args) throws ConnectException {
//        File inputFile = new File("/Users/broder/env/report/Envidatec Weekly Report_07_02_2016.xls");
//        File outputFile = new File("/Users/broder/env/report/document.pdf");
//
//// connect to an OpenOffice.org instance running on port 8100
//        OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
//        connection.connect();
//
//// convert
//        DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
//        converter.convert(inputFile, outputFile);
//
//// close the connection
//        connection.disconnect();
//    }

    private static void setCellComment(CreationHelper factory, Sheet sheet, Row row, Cell cell, String value) {

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 3);
        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(value);
        comment.setString(str);
        comment.setAuthor("Apache POI");
    }
}
