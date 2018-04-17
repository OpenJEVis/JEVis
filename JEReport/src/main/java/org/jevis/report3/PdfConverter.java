/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

/**
 *
 * @author broder
 */
public class PdfConverter {

    private File xlsFile;
    private File pdfFile;

    public PdfConverter(String filePrefix, byte[] fileContent) {
        try {
            Path xlsFilePath = Files.createTempFile(filePrefix, ".xls");
            xlsFile = xlsFilePath.toFile();
            Files.write(xlsFilePath, fileContent);
            xlsFile.deleteOnExit();

            Path pdfFilePath = Files.createTempFile(filePrefix, ".pdf");
            pdfFile = pdfFilePath.toFile();
            pdfFile.deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(PdfConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File runPdfConverter() {
        DefaultOfficeManagerConfiguration defaultOfficeManagerConfiguration = new DefaultOfficeManagerConfiguration();
        OfficeManager officeManager = defaultOfficeManagerConfiguration.buildOfficeManager();
        try {
            officeManager.start();
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
            converter.convert(xlsFile, pdfFile);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            officeManager.stop();
        }
        return pdfFile;
    }

    public static void main(String[] args) throws IOException {
        Locale aDefault = Locale.getDefault();
        Charset defaultCharset = Charset.defaultCharset();
        String displayName = defaultCharset.displayName();
        String displayLanguage = aDefault.getDisplayLanguage();
        Path fileLocation = Paths.get("");
        byte[] data = Files.readAllBytes(fileLocation);
        PdfConverter converter = new PdfConverter("test", data);
        converter.runPdfConverter();
    }
}
