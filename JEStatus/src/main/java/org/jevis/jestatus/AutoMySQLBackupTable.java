package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.io.File;
import java.util.Arrays;

public class AutoMySQLBackupTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AutoMySQLBackupTable.class);
    private final JEVisDataSource ds;
    private final DateTime latestReported;
    private File default_dir = new File("/var/lib/automysqlbackup");
    //private File default_dir = new File("C:/Users/Ich/Documents/automysql");

    public AutoMySQLBackupTable(JEVisDataSource ds, DateTime latestReported) {
        super(ds);
        this.ds = ds;
        this.latestReported = latestReported;

        try {
            if (default_dir.exists() && default_dir.canRead()) {
                createTableString();
            } else {
                setTableString("");
                logger.error("AutoMySQLBackup cannot access '{}'", default_dir.toString());
            }


        } catch (Exception e) {
            logger.error("Could not initialize.", e, e);
        }
    }

    private void createTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();
        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append("AutoMySQLBackup Status").append("</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.file")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.file.size")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.backup.type")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.file.biggerthenbefore")).append("</th>");
        sb.append("  </tr>");//border=\"0\"

        String errorColorCSS = "background-color: #FF5050";
        String messageError = I18n.getInstance().getString("status.table.captions.backup.error01");


        boolean odd = false;
        for (File file : default_dir.listFiles()) {

            /** databse level **/
            for (File dbFolder : file.listFiles()) {

                if (dbFolder.isDirectory() && dbFolder.getName().equals("jevis")) {
                    long prefFileSize = 0;
                    File[] subFiles = dbFolder.listFiles();
                    Arrays.sort(subFiles);
                    for (File subFile : subFiles) {
                        try {
                            long sizeInMb = subFile.length() / (1024 * 1024);

                            String css = rowCss;
                            if (odd) {
                                css += highlight;
                            }
                            odd = !odd;

                            String isOK = messageError;
                            if (prefFileSize < subFile.length()) {
                                isOK = "OK";
                            } else {
                                css = errorColorCSS;
                            }
                            prefFileSize = subFile.length();

                            sb.append("<tr>");

                            /** File name column */
                            sb.append("<td style=\"");
                            sb.append(css);
                            sb.append("\">");
                            sb.append(subFile.getName());
                            sb.append("</td>");

                            /** File size column */
                            sb.append("<td style=\"");
                            sb.append(css);
                            sb.append("\">");
                            sb.append(sizeInMb + " MB");
                            sb.append("</td>");

                            /** backup type column */
                            sb.append("<td style=\"");
                            sb.append(css);
                            sb.append("\">");
                            sb.append(file.getName());
                            sb.append("</td>");


                            /** File OK column */
                            sb.append("<td style=\"");
                            sb.append(css);
                            sb.append("\">");
                            sb.append(isOK);
                            sb.append("</td>");

                            sb.append("</tr>");


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }


        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<br>");

        setTableString(sb.toString());
        logger.debug("AutoMySQL table:\n{}", sb.toString());
    }


}
