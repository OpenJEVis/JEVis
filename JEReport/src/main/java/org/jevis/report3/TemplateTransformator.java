/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jxls.common.Context;
import org.jxls.formula.StandardFormulaProcessor;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author broder
 */
public class TemplateTransformator {

    private byte[] outputBytes;

    public void transform(byte[] templateBytes, Context context) throws IOException {
        InputStream input = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.createTransformer(input, output);
        JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        jxlsHelper.setUseFastFormulaProcessor(false);
        jxlsHelper.setFormulaProcessor(new StandardFormulaProcessor());
        jxlsHelper.setProcessFormulas(true);

        jxlsHelper.processTemplate(context, transformer);

        //jxlsHelper.processTemplate(input, output, context);
////        Workbook workbook;
////        try {
////            workbook = Workbook.getWorkbook(input);
////            int numberOfSheets = workbook.getNumberOfSheets();
////            logger.info(numberOfSheets);
////        } catch (BiffException ex) {
////            Logger.getLogger(TemplateTransformator.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        Transformer transformer = TransformerFactory.createTransformer(input, output);
//        AreaBuilder areaBuilder = new XlsCommentAreaBuilder();
//        areaBuilder.setTransformer(transformer);
//        List<Area> xlsAreaList = areaBuilder.build();
//
//        logger.info(xlsAreaList.size() + " areas found");
//        for (Area xlsArea : xlsAreaList) {
//            String cellName = xlsArea.getStartCellRef().getCellName();
//            String sheetName = xlsArea.getStartCellRef().getSheetName();
//            logger.info(cellName + "," + sheetName);
//        }
//        JxlsHelper.getInstance().processTemplate(context, transformer);
        outputBytes = output.toByteArray();
    }

    public byte[] getOutputBytes() {
        return outputBytes;
    }
}
