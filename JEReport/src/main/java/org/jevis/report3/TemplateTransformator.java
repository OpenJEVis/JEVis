/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.Context;
import org.jxls.common.SheetData;
import org.jxls.transform.Transformer;
import org.jxls.transform.jexcel.JexcelSheetData;
import org.jxls.transform.jexcel.JexcelTransformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;

/**
 *
 * @author broder
 */
public class TemplateTransformator {

    private byte[] outputBytes;

    public void transfrom(byte[] templateBytes, Context context) throws IOException {
        InputStream input = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JxlsHelper.getInstance().processTemplate(input, output, context);
////        Workbook workbook;
////        try {
////            workbook = Workbook.getWorkbook(input);
////            int numberOfSheets = workbook.getNumberOfSheets();
////            System.out.println(numberOfSheets);
////        } catch (BiffException ex) {
////            Logger.getLogger(TemplateTransformator.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        Transformer transformer = TransformerFactory.createTransformer(input, output);
//        AreaBuilder areaBuilder = new XlsCommentAreaBuilder();
//        areaBuilder.setTransformer(transformer);
//        List<Area> xlsAreaList = areaBuilder.build();
//
//        System.out.println(xlsAreaList.size() + " areas found");
//        for (Area xlsArea : xlsAreaList) {
//            String cellName = xlsArea.getStartCellRef().getCellName();
//            String sheetName = xlsArea.getStartCellRef().getSheetName();
//            System.out.println(cellName + "," + sheetName);
//        }
//        JxlsHelper.getInstance().processTemplate(context, transformer);
        outputBytes = output.toByteArray();
    }

    public byte[] getOutputBytes() {
        return outputBytes;
    }
}
