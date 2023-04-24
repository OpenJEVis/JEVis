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
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author broder
 */
public class TemplateTransformator {

    private byte[] outputBytes;

    public void transform(byte[] templateBytes, Context context) throws Exception {
        InputStream input = new ByteArrayInputStream(templateBytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.createTransformer(input, output);
        JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        jxlsHelper.setUseFastFormulaProcessor(false);
        jxlsHelper.setProcessFormulas(true);

        jxlsHelper.processTemplate(context, transformer);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(output.toByteArray());

        XSSFWorkbook workbook = new XSSFWorkbook(byteArrayInputStream);
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
        } finally {
            bos.close();
            workbook.close();
            byteArrayInputStream.close();
            input.close();
            output.close();
        }
        outputBytes = bos.toByteArray();
    }

    public byte[] getOutputBytes() {
        return outputBytes;
    }
}
