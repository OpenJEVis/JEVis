/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jevis.api.JEVisFile;
import org.jevis.commons.JEVisFileImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author broder
 */
public class PdfFileSplitter {

    private static final Logger logger = LoggerFactory.getLogger(PdfFileSplitter.class);

    private String outputPath;
    private String inputPath;
    private int nrOfPages;
    private File outputFile;

    public PdfFileSplitter(Long nrOfPdfPages, File wholePdfFile) {
        try {
            inputPath = wholePdfFile.getAbsolutePath();
            String pdfFileName = wholePdfFile.getName();
            Path pdfSplittedFilePath = Files.createTempFile(pdfFileName.replace(".pdf", "_split"), ".pdf");

            outputPath = pdfSplittedFilePath.toString();
            System.out.println(outputPath);
            outputFile = pdfSplittedFilePath.toFile();
            nrOfPages = nrOfPdfPages.intValue();
        } catch (IOException ex) {
            System.out.println(ex);
            logger.error("error while creating pdf splitter", ex);
        }
    }

    public void splitPDF() {
        PdfCopy writer = null;
        Document document = null;
        try {

            PdfReader reader = new PdfReader(inputPath);
//            int n = reader.getNumberOfPages();

            int n;
            if (reader.getNumberOfPages() < nrOfPages) {
                n = reader.getNumberOfPages();
            } else {
                n = nrOfPages;
            }
            int i = 0;

//            String outFile = output.substring(0, output.indexOf(".pdf"))
//                    + ".pdf";
            document = new Document(reader.getPageSizeWithRotation(1));
            writer = new PdfCopy(document, new FileOutputStream(outputPath));
            document.open();
            while (i < n) {

                PdfImportedPage page = writer.getImportedPage(reader, ++i);

                writer.addPage(page);

            }

        } catch (DocumentException | IOException ex) {
            System.out.println(ex);
            logger.error("error while splitting pdf file", ex);
        } finally {
            document.close();
            writer.close();
        }

        /*
         * example : java SplitPDFFile d:\temp\x\tx.pdf
         *
         * Reading d:\temp\x\tx.pdf Number of pages : 3 Writing
         * d:\temp\x\tx-001.pdf Writing d:\temp\x\tx-002.pdf Writing
         * d:\temp\x\tx-003.pdf
         */
    }

    public File getOutputFile() {
        return outputFile;
    }
    
    public static void main(String[] args){
        
        PdfFileSplitter start = new PdfFileSplitter(2l, new File("/Users/broder/2006_Lothar-Sachs_Angewandte Statistik.pdf"));
        start.splitPDF();
    }
}
