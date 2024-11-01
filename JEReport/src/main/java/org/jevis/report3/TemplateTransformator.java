/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.JxlsHelper;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * @author broder
 */
public class TemplateTransformator {

    private byte[] outputBytes;

    public void transform(File templateFile, Context context) throws Exception {
//        InputStream input = new ByteArrayInputStream(templateFile);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

//        Transformer transformer = TransformerFactory.createTransformer(input, output);
        JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        jxlsHelper.setUseFastFormulaProcessor(false);
        jxlsHelper.setProcessFormulas(true);

        XSSFWorkbook template = new XSSFWorkbook(templateFile);
        PoiTransformer poiTransformer = PoiTransformer.createTransformer(template);
        poiTransformer.setOutputStream(output);
        jxlsHelper.processTemplate(context, poiTransformer);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(byteArrayInputStream);
        workbook.setForceFormulaRecalculation(true);
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
        } finally {
            bos.close();
            workbook.close();
            byteArrayInputStream.close();
//            input.close();
            output.close();
        }
        outputBytes = bos.toByteArray();
    }

    public byte[] getOutputBytes() {
        return outputBytes;
    }
}
