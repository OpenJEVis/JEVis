package com.envidatec.jevis.jereport;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import envidatec.jevis.capi.data.JevCalendar;
import envidatec.jevis.capi.definitions.FixedReg;
import envidatec.jevis.capi.definitions.FixedReg.PropertyID;
import envidatec.jevis.capi.nodes.NodeManager;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.Configuration;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author broder
 */
public class ReportGenerator {

    private static final String TEMPLATE_FILE = "template.xls";
    private static final Logger _logger = Logger.getLogger(ReportHandler.class.getName());
//    Property property;

    public void generateNormalReport(RegTreeNode reportNode, Property property, List<Datasource> datasourceList, String folder) {
        Map beans = fillBeans(datasourceList);
        generateReport(reportNode, property, beans, folder);
    }

    private void generateReport(RegTreeNode reportNode, Property property, Map beans, String folder) {


        _logger.info("phase1----------begin generating report");
//        property = prop;

        FileManager fileManager = new FileManager();
        RegTreeNode templateFile = (RegTreeNode) reportNode.getPropertyNode(FixedReg.XLS_TEMPLATE_FILE);
        String reportFileName = reportNode.getName() + "_" + new JevCalendar().toFormattedString(JevCalendar.YYYYMMDDHHMMSS) + ".xls";


        if (templateFile != null) {
            byte[] templateFileAsByte = (byte[]) templateFile.getCurrentValue().getVal();
            FileManager.writeBytesToFile(
                    templateFileAsByte, folder + TEMPLATE_FILE);

            fileManager.setTemplateFileName(folder + TEMPLATE_FILE);
            fileManager.setDestFileName(folder + reportFileName);
        }

        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer(config);


        try {
            _logger.info("phase2-----------transforming excel file");
//            transformer.groupCollection("datasource.name");
//            transformer.transformXLS(template, beans, output);
            _logger.log(Level.FINER, "FilenameInput :" + fileManager.getTemplateFileName());
            _logger.log(Level.FINER, "FilenameOutput :" + fileManager.getDestFileName());
            transformer.transformXLS(fileManager.getTemplateFileName(), beans, fileManager.getDestFileName());  //TODO Output muss noch hochgeladen werden


        } catch (IOException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParsePropertyException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidFormatException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }



        _logger.info("phase3-----Uploading report into DB...");
        try {
            byte[] reportFileAsByte = FileManager.getBytesFromFile(new File(fileManager.getDestFileName()));

            RegTreeNode reportFile = (RegTreeNode) reportNode.getPropertyNode(FixedReg.XLS_REPORT_FILE);
            if (reportFile == null) {
                reportFile = NodeManager.getInstance().createRegistryNode(FixedReg.XLS_REPORT_FILE.getID(), reportNode.getID(), FixedReg.XLS_REPORT_FILE.getDefaultName());
            }
//            PropertyID testFile = FixedReg.XLS_REPORT_FILE;
//            System.out.println("DefaultName "+testFile.getDefaultName());
//            System.out.println("ID "+testFile.getID());
//            Class datatype = testFile.getDatatype();
//            System.out.println("Datatype "+datatype.getName());
            reportFile.addNewValue(reportFileAsByte, new JevCalendar(), byte[].class);
        } catch (IOException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }


        _logger.info("phase4-----send e-mail");
        try {
            NodeManager nm = NodeManager.getInstance();
            Long tmp = reportNode.getPropertyNode("Format").getLinkID();
            String format = nm.getDefinitionNode(tmp).getName();
            Mail mail = new Mail(property.getHost(), property.getPort(), property.getUser(), property.getPassword(), property.getSubject());
            if (format.equals("pdf")) {
                String pdfFile = convertToPdf(fileManager.getDestFileName());
                int i = Integer.parseInt(reportNode.getPropertyNode("pdf- number of pages").getCurrentValue().getVal().toString());
                String outputFile = splitPDF(pdfFile, i);
                fileManager.deleteFiles();
                fileManager.setDestFileName(outputFile);
            }
            mail.sendMail(property.getMails(), fileManager.getDestFileName());

        } catch (GeneralSecurityException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AddressException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
//            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            _logger.throwing("ReportGenerator", "generateReport", ex);
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }


        _logger.info("phase5-----change dates");
        JevCalendar tmpNextCreation = property.getNextCreationDate().clone();

        JevCalendar nextCreation = new JevCalendar(new Date(tmpNextCreation.getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(tmpNextCreation.clone().getTimeInMillis())));

        nextCreation.add(property.getScheduleAsInt(), 1);

        nextCreation.setTimeZone(TimeZone.getTimeZone("UTC"));

//        reportNode.getPropertyNode("Next Creation Date").addNewValue(nextCreation.toString(), new JevCalendar(), FixedReg.NEXT_CREATION_DATE.getDatatype());
//        JevCalendar tmpNextCreation = new JevCalendar(new Date(nextCreation.getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(nextCreation.clone().getTimeInMillis())));
        _logger.info("NextCreation Date" + nextCreation);
        reportNode.getPropertyNode("Next Creation Date").addNewValue(nextCreation.toString(), new JevCalendar(), String.class);

        JevCalendar tmpStartRecord = property.getStartRecord().clone();
        JevCalendar startRecord = new JevCalendar(new Date(tmpStartRecord.getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(tmpStartRecord.clone().getTimeInMillis())));

        JevCalendar dynamicStartRecord = startRecord.clone();
        dynamicStartRecord.add(property.getScheduleAsInt(), 1);

        JevCalendar staticStartRecord = startRecord.clone();
        staticStartRecord.add(property.getScheduleAsInt(), property.getPeriod());

//        System.out.println("winterzeitoffset " + Calendar.getInstance().getTimeZone().getOffset(tmpStartRecord.clone().getTimeInMillis()));
//        System.out.println("somemrzeitoffset " + Calendar.getInstance().getTimeZone().getOffset(JevCalendar.getInstance().getTimeInMillis()));
//        System.out.println("rawoffset " + Calendar.getInstance().getTimeZone().getRawOffset());
//        System.out.println("tmpStartRecord " + tmpStartRecord);
//        System.out.println("StartRecord " + startRecord);

        if (property.isDynamic() & (dynamicStartRecord.before(new JevCalendar(TimeZone.getTimeZone("UTC"))) || dynamicStartRecord.equals(new JevCalendar(TimeZone.getTimeZone("UTC"))))) {
            reportNode.getPropertyNode("Start record").addNewValue(new JevCalendar(), dynamicStartRecord.toString());
            _logger.log(Level.FINER, "NextDynamicStartRecord Date" + dynamicStartRecord);
        } else if (!property.isDynamic() & (staticStartRecord.before(new JevCalendar(TimeZone.getTimeZone("UTC"))) || staticStartRecord.equals(new JevCalendar(TimeZone.getTimeZone("UTC"))))) {
            reportNode.getPropertyNode("Start record").addNewValue(new JevCalendar(), staticStartRecord.toString());
            _logger.log(Level.FINER, "NextStaticStartRecord Date" + staticStartRecord);
        }



        _logger.info("phase6------delete files in " + folder);
        fileManager.deleteFiles();
        _logger.info("------finished-----");
    }

//    public Property getProperty() {
//        return property;
//    }
    private String convertToPdf(String destFileName) {
//        System.out.println("FILEN " + destFileName);
        String tmpString = null;
//        System.out.println("--bishier--");
        DefaultOfficeManagerConfiguration defaultOfficeManagerConfiguration = new DefaultOfficeManagerConfiguration();
//        System.out.println("--bishier--");
        OfficeManager officeManager = defaultOfficeManagerConfiguration.buildOfficeManager();

        try {
//            System.out.println("--bishier--");
            officeManager.start();
//            System.out.println("--bishier--");
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
//            System.out.println("--bishier--");
            DocumentFormat format = new DocumentFormat();
//            System.out.println("--bishier--");
            tmpString = destFileName.replace(".xls", "tmp.pdf");
//            System.out.println("DESTFILE " + destFileName);
//            System.out.println("OUTPUTFILE " + tmpString);
            File destfile = new File(destFileName);
            _logger.log(Level.FINEST, "absoluter pfad{0}", destfile.getAbsolutePath());
            _logger.log(Level.FINEST, "writeable {0}", destfile.canWrite());
            _logger.log(Level.FINEST, "readable {0}", destfile.canRead());
            File outputFile = new File(tmpString);
            _logger.info("-----laden der Files geklappt------");
            converter.convert(destfile, outputFile);
        } catch (Exception e) {
            _logger.throwing("ReportGenerator", "convertToPdf", e);
//            e.printStackTrace();
        } finally {
            officeManager.stop();
        }
        return tmpString;
    }

    public String splitPDF(String filename, int pageNumber) {
        String inFile = filename;
        String outFile = inFile.replace("tmp.pdf", ".pdf");
        PdfCopy writer = null;
        Document document = null;
        try {

            _logger.log(Level.FINEST, "Reading {0}", inFile);
            PdfReader reader = new PdfReader(inFile);
//            int n = reader.getNumberOfPages();

            int n;
            if (reader.getNumberOfPages() < pageNumber) {
                n = reader.getNumberOfPages();
            } else {
                n = pageNumber;
            }
            _logger.log(Level.FINEST, "Number of pages : {0}", n);
            int i = 0;

//            String outFile = output.substring(0, output.indexOf(".pdf"))
//                    + ".pdf";
            _logger.log(Level.FINEST, "Writing {0}", outFile);
            document = new Document(reader.getPageSizeWithRotation(1));
            writer = new PdfCopy(document, new FileOutputStream(outFile));
            document.open();
            while (i < n) {


                PdfImportedPage page = writer.getImportedPage(reader, ++i);

                writer.addPage(page);

            }

        } catch (Exception e) {
            _logger.throwing("ReportGenerator", "splitpdf", e);
//            e.printStackTrace();
        } finally {
            document.close();
            writer.close();
        }

        return outFile;
        /*
         * example : java SplitPDFFile d:\temp\x\tx.pdf
         *
         * Reading d:\temp\x\tx.pdf Number of pages : 3 Writing
         * d:\temp\x\tx-001.pdf Writing d:\temp\x\tx-002.pdf Writing
         * d:\temp\x\tx-003.pdf
         */

    }
//
//    private void deleteFiles(String parafolder) {
//        File dir = new File(parafolder);
//
//        File[] files = dir.listFiles();
//        if (files != null) { // Erforderliche Berechtigungen etc. sind vorhanden
//            for (int i = 0; i < files.length; i++) {
//                files[i].delete();
//            }
//        }
//    }

    public void generateAlarmReport(RegTreeNode reportNode, Property prop, AlarmData alarmData, String parafolder) {
        Map beans = fillAlarmBeans(alarmData);
        generateReport(reportNode, prop, beans, parafolder);
    }

    private Map fillAlarmBeans(AlarmData alarmData) {
        Map beans = new HashMap();
        beans.put("adfRaise.timestamps", alarmData.getADFRaiseTimestamps());
        beans.put("adfRaise.names", alarmData.getADFRaiseNames());
        beans.put("adfRaise.ids", alarmData.getADFRaiseIds());
        beans.put("adfRaise.reasons", alarmData.getADFRaiseExplanations());

        beans.put("adfAcknowledge.timestamps", alarmData.getADFAcknowlegdeTimestamps());
        beans.put("adfAcknowledge.names", alarmData.getADFAcknowlegdeNames());
        beans.put("adfAcknowledge.ids", alarmData.getADFAcknowlegdeIds());
        beans.put("adfAcknowledge.reasons", alarmData.getADFAcknowlegdeExplanations());
        return beans;
    }

    private Map fillBeans(List<Datasource> datasourceList) {
        Map beans = new HashMap();
        for (Datasource data : datasourceList) {
            _logger.log(Level.FINER, "Name {0}", data.getName());
            _logger.log(Level.FINER, "Identifier {0}", data.getIdentifier());
            _logger.log(Level.FINEST, "Timestamps size {0}", data.getTimestamp().getall().size());
            _logger.log(Level.FINEST, "first Timestamp {0}", data.getTimestamp().getFirst());
            _logger.log(Level.FINEST, "last Timestamp {0}", data.getTimestamp().getLast());
            _logger.log(Level.FINER, "Values size {0}", data.getValue().getall().size());

            beans.put(data.getIdentifier() + ".values", data.getValue().getall());  //allSamples
            beans.put(data.getIdentifier() + ".timestamps", data.getTimestamp().getall());  //allTimestamps
            beans.put(data.getIdentifier() + ".inkw", data.getValue().getValuesInkW());
//            beans.put(data.getIdentifier() + ".sortedvalues", data.getValue().getValuesInkW());
            beans.put(data.getIdentifier() + ".oldvalues", data.getValueOld().getall());  //allOldSamples
            beans.put(data.getIdentifier() + ".oldtimestamps", data.getTimestampOld().getall());  //allOldTimestamps

            beans.put(data.getIdentifier() + ".oldoldvalues", data.getValueOldOld().getall());
            beans.put(data.getIdentifier() + ".oldoldtimestamps", data.getTimestampOldOld().getall());

            beans.put(data.getIdentifier() + ".lastyearvalues", data.getValueLastYear().getall());
            beans.put(data.getIdentifier() + ".lastyeartimestamps", data.getTimestampLastYear().getall());

            beans.put(data.getIdentifier() + ".oldlastyearvalues", data.getValueOldLastYear().getall());
            beans.put(data.getIdentifier() + ".oldlastyeartimestamps", data.getTimestampOldLastYear().getall());

            beans.put(data.getIdentifier(), data);
//            System.out.println("IDENT " + data.getIdentifier());
        }
        return beans;
    }
}
