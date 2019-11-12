package org.jevis.jenotifier.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.utils.PrettyError;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CSVExport extends Export {

    private static final Logger logger = LogManager.getLogger(CSVExport.class);
    public static String TYPE_SEPARATOR = "Separator";
    public static String TYPE_ENCLOSED = "Enclosed";
    public static String TYPE_FILENAME = "File Name";
    public static String TYPE_HEADER = "Header";
    public static String TYPE_TIMESTAMP_FORMAT = "Timestamp Format";
    public static String TYPE_LAST_DATE = "Export Date";
    public static String TYPE_STATUS = "Export Status";
    public static String TYPE_PERIOD_OFFSET = "Start Period Offset";

//    public static String TYPE_EXPORTET_LOG = "Export Status";

    public static String STATUS_SUCESS = "Success";
    public static String STATUS_IDEL = "Idel";
    public static String STATUS_ERROR = "Error";


    private JEVisAttribute attSeperator;
    private JEVisAttribute attEnclosed;
    private JEVisAttribute attFilename;
    private JEVisAttribute attHeader;
    private JEVisAttribute attTimestampFormat;
    private JEVisAttribute attTimeZone;
    private JEVisAttribute attLastExport;
    private JEVisAttribute attLastExportStatus;
    protected JEVisAttribute attStartPeriodOffset;

    //    protected JEVisAttribute attExportStatus;
    private int exportCount = 0;

    private List<File> exportFiles = new ArrayList<>();
    private DateTime lastUpdate;
    private String enclosed = "";
    protected String seperator = ";";
    protected String filename = "export";
    protected String header = "";
    private DateTime logDate = null;
    protected long startOffset = 1;

    private boolean hasNewData = false;
    //    private DateTime lastTimeStamp;
    protected DateTimeZone dateTimeZone = DateTimeZone.getDefault();
    private List<ExportEvent> exportEventList = new ArrayList<>();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM.dd HH:mm:ss");
    private List<ExportLink> exportLinkList = new ArrayList<>();
    private static Comparator<JEVisSample> jeVisSampleComparator = new Comparator<JEVisSample>() {
        @Override
        public int compare(JEVisSample o1, JEVisSample o2) {
            try {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            } catch (Exception ex) {

            }
            return 0;
        }
    };

    public CSVExport(JENotifierConfig jeNotifierConfig, JEVisObject object) {
        super(jeNotifierConfig, object);
        super.init();
    }


    @Override
    void initSettings() throws Exception {
        try {
            attSeperator = exportObject.getAttribute(TYPE_SEPARATOR);
            seperator = attSeperator.getLatestSample().getValueAsString();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Separator Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }


        try {
            attEnclosed = exportObject.getAttribute(TYPE_ENCLOSED);
            enclosed = attEnclosed.getLatestSample().getValueAsString();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Enclosed Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }
        try {
            attFilename = exportObject.getAttribute(TYPE_FILENAME);
            filename = attFilename.getLatestSample().getValueAsString();
            filename = filename.replaceAll(".csv", "");
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Filename Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }
        try {
            attHeader = exportObject.getAttribute(TYPE_HEADER);
            header = attHeader.getLatestSample().getValueAsString();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Header Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }

        try {
            attLastExport = exportObject.getAttribute(TYPE_LAST_DATE);
            lastUpdate = new DateTime(attLastExport.getLatestSample().getValueAsString());
//            lastUpdate = attLastExport.getLatestSample().getTimestamp();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Last Export Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }

        try {
            attLastExportStatus = exportObject.getAttribute(TYPE_STATUS);
//            lastUpdate = new DateTime(attLastExport.getLatestSample().getValueAsString());
//            lastUpdate = attLastExport.getLatestSample().getTimestamp();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Export Status Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }

//        try {
//            attLastExport = exportObject.getAttribute(TYPE_LAST_DATE);
//            lastUpdate = attLastExport.getLatestSample().getTimestamp();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            attTimestampFormat = exportObject.getAttribute(TYPE_TIMESTAMP_FORMAT);
            dateTimeFormatter = DateTimeFormat.forPattern(attTimestampFormat.getLatestSample().getValueAsString());
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Timestamp Format Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }

        try {
            attStartPeriodOffset = exportObject.getAttribute(TYPE_PERIOD_OFFSET);
            startOffset = attStartPeriodOffset.getLatestSample().getValueAsLong();

            /** 0 makes no sense in a multiplication **/
            if (startOffset == 0) {
                startOffset = 1;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Start Period Offset Format Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }

        try {
            attTimeZone = exportObject.getAttribute(TYPE_TIMEZONE);
            dateTimeZone = DateTimeZone.forID(attTimeZone.getLatestSample().getValueAsString());
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Error in Timezone Attribute: {}", PrettyError.getJEVisLineFilter(e));
        }


        JEVisClass exportLInkClass = exportObject.getDataSource().getJEVisClass(ExportLink.CLASS_NAME);
        exportObject.getChildren(exportLInkClass, true).forEach(child -> {
            /** TODO: load the different types dynamic **/
            try {
                if (child.getJEVisClassName().equals(CSVExportLink.CLASS_NAME)) {
                    ExportLink exportLink = new CSVExportLink(child);
                    exportLinkList.add(exportLink);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JEVisClass eventClass = exportObject.getDataSource().getJEVisClass(ExportEvent.CLASS_NAME);
        exportObject.getChildren(eventClass, true).forEach(child -> {
            /** TODO: load the different types dynamic **/
            try {
                if (child.getJEVisClassName().equals(ExportEvent.CLASS_NAME)) {
                    ExportEvent exportEvent = new ExportDataEvent(child);
                    exportEventList.add(exportEvent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


    }

    @Override
    List<File> getExportFiles() {
        return exportFiles;
    }


    @Override
    public void executeExport() throws Exception {
//        logger.error("executeExport()");
        System.out.println("Export from: " + lastUpdate);

        logDate = new DateTime();
        AtomicBoolean export = new AtomicBoolean(false);
        if (lastUpdate == null) { /** if there was never an export export all data **/
            export.set(true);
        } else if (exportEventList.isEmpty()) {
//            System.out.println("No Event configured using simple new Value Event");
            export.set(true);
        } else {
            exportEventList.forEach(exportEvent -> {
                try {
                    if (exportEvent.isTriggered(lastUpdate)) {
                        export.set(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });
        }

        boolean newData = false;
        if (export.get()) {
            int maxColoum = 0;
            DateTime now = DateTime.now();
            Set<DateTime> timeStampList = new TreeSet<>();

            Map<ExportLink, Map<DateTime, JEVisSample>> exportLinkListMap = new HashMap<>();
            List<DateTime> shareMax = new LinkedList<>();
            for (ExportLink exportLink : exportLinkList) {
                try {
                    DateTime maxInSource = null;

                    /** Also fetch x previous periods add add one second to not have the same sample because >=**/
                    DateTime startTime = lastUpdate.minus(
                            exportLink.targetAttribute.getInputSampleRate().toStandardDuration().getMillis() * startOffset)
                            .plusSeconds(1);

                    Map<DateTime, JEVisSample> sampleList = exportLink.getSamples(startTime, now);
//                    Map<DateTime, JEVisSample> sampleList = exportLink.getSamples(lastUpdate.plusSeconds(1), now);


                    exportLinkListMap.put(exportLink, sampleList);
                    maxColoum = exportLink.getColumn() > maxColoum ? exportLink.getColumn() : maxColoum;
                    for (JEVisSample jeVisSample : sampleList.values()) {
                        timeStampList.add(jeVisSample.getTimestamp());
                        exportCount++;
                        if (maxInSource == null || maxInSource.isBefore(jeVisSample.getTimestamp())) {
                            maxInSource = jeVisSample.getTimestamp();
                        }
                    }
//                    System.out.println("Exported: " + exportLink.getTargetAttribute().getObject().getName() + " " + sampleList.values().size() + "");
                    shareMax.add(maxInSource);

                    if (maxInSource.isAfter(lastUpdate)) {
                        newData = true;
                    }

                } catch (Exception ex) {
                    logger.error(PrettyError.getJEVisLineFilter(ex));
                }
            }

            DateTime minMax = null;

            for (DateTime max : shareMax) {
                if (minMax == null || max.isBefore(minMax)) {
                    minMax = max;
                }
            }
            if (minMax != null) {
                lastUpdate = minMax;
            }


            /** TODO: support timestamp position and sometimes Time and Date are split **/

            /** nothing todo return**/
            if (!newData || exportCount <= 0) {
                try {
                    JEVisSample status = attLastExportStatus.buildSample(logDate, STATUS_IDEL + "; " + exportCount);
                    status.commit();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                return;
            }

            hasNewData = true;
            StringBuilder stringBuilder = new StringBuilder();
            if (!header.isEmpty()) {
                stringBuilder.append(header);
                stringBuilder.append(System.lineSeparator());
            }
            
            for (DateTime timeStamp : timeStampList) {
                stringBuilder.append(enclosed);
                stringBuilder.append(dateTimeFormatter.print(timeStamp.withZone(dateTimeZone)));
                stringBuilder.append(enclosed);
                stringBuilder.append(seperator);
                for (int i = 1; i <= maxColoum; i++) {
                    for (ExportLink exportLink : exportLinkListMap.keySet()) {
                        if (exportLink.getColumn() == i) {
                            JEVisSample sample = exportLinkListMap.get(exportLink).get(timeStamp);
                            stringBuilder.append(enclosed);
                            if (sample != null) {
                                try {
                                    stringBuilder.append(exportLink.formatValue(sample.getValue()));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            stringBuilder.append(enclosed);

                            if (i + 1 <= maxColoum) {
                                stringBuilder.append(seperator);
                            }

                        }
                    }

                }
                stringBuilder.append(System.lineSeparator());
            }

            exportFiles.clear();
            File tmpCSVFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".csv");
            if (tmpCSVFile.exists()) {
                tmpCSVFile.delete();
            }

            tmpCSVFile.createNewFile();
            tmpCSVFile.deleteOnExit();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpCSVFile))) {
                writer.write(stringBuilder.toString());
            }
//            System.out.println("Exporterd file: " + tmpCSVFile.getName());
            exportFiles.add(tmpCSVFile);

        } else {
            setIdleStatus();
        }
    }

    private void setIdleStatus() {
        try {
            JEVisSample status = attLastExportStatus.buildSample(logDate, STATUS_IDEL);
            status.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean hasNewData() {
        return hasNewData;
    }

    @Override
    public void setOnSuccess() {
        try {
            JEVisSample sample = attLastExport.buildSample(logDate, lastUpdate.toString());
            sample.commit();
            JEVisSample status = attLastExportStatus.buildSample(logDate, STATUS_SUCESS + "; " + exportCount);
            status.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void cleanUp() {
        for (File exportFile : exportFiles) {
            try {
                exportFile.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
