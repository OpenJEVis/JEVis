package org.jevis.jeconfig.tool.jevis2;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import org.jevis.ws.sql.SQLtoJsonFactory;

public class JEVis2DB {


    private static final Logger logger = LogManager.getLogger(JEVis2DB.class);
    private BasicDataSource ds;
    //    private String dataSQL = "select *,CONVERT_TZ('TIMESTAMP', @@session.time_zone, '+00:00') AS 'utc_datetime'" +
//            " from REGISTRY_DATA where REGISTRY_ID=? and REGISTRY_DATA.TIMESTAMP> ? ";
//    private String dataSQL = "select REGISTRY_ID,VALUE,REGISTRY_DATA.TIMESTAMP,CONVERT_TZ(REGISTRY_DATA.TIMESTAMP, @@session.time_zone, '+00:00') AS 'utc_datetime'" +
//            ", UNIX_TIMESTAMP(REGISTRY_DATA.TIMESTAMP) as unixtime,DATE_FORMAT(REGISTRY_DATA.TIMESTAMP, '%Y-%m-%dT%TZ') AS date_formatted, NOTE" +
//            " from REGISTRY_DATA where REGISTRY_ID=? and REGISTRY_DATA.TIMESTAMP> ?";
//    private String dataSQL = "select VALUE,NOTE,REGISTRY_DATA.TIMESTAMP,CONVERT_TZ(REGISTRY_DATA.TIMESTAMP, @@session.time_zone, '+00:00') AS 'utc_datetime'" +
//            " from REGISTRY_DATA where REGISTRY_ID=? and REGISTRY_DATA.TIMESTAMP> ?";
    private String dataSQL = "select VALUE,NOTE,REGISTRY_DATA.TIMESTAMP" +
            " from REGISTRY_DATA where REGISTRY_ID=? and REGISTRY_DATA.TIMESTAMP> ?";

    private JEVisDataSource jevis3ds;
    private PrintWriter pw;

    public JEVis2DB(JEVisDataSource ds) throws IOException {
        this.jevis3ds = ds;
        registerMySQLDriver("localhost", "4406", "JEVIS", "exporter", "ienga4Gu");

        this.pw = new PrintWriter(new FileWriter("/home/fs/plus_export.txt", true));

    }

    DateTime dateTime = new DateTime(2019, 8, 15, 8, 0);

    public void match() {

        String jevis2SQL = "select a.name as j2name,b.name,c.name as cleanobj,c.ID as cleanID from REGISTRY a \n" +
                "left join REGISTRY b on a.ID=b.PARENT_ID\n" +
                "left join REGISTRY c on b.ID=c.PARENT_ID\n" +
                "where a.TYPE_ID = 9 and b.TYPE_ID= 99 and c.TYPE_ID= 96 and c.DELETE_TS is null;";

        try {
            List<JEVisObject> objectsJEVis3 = this.jevis3ds.getObjects(this.jevis3ds.getJEVisClass("Data"), true);
            PreparedStatement ps = getConnection().prepareStatement(jevis2SQL);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    String jevis3id = "";
                    for (JEVisObject obj : objectsJEVis3) {
                        if (obj.getName().toLowerCase().equals(rs.getString("j2name").toLowerCase())) {
                            jevis3id = obj.getID().toString();
                        }
                    }

                    System.out.println(String.format("%s;%s;%s", rs.getString("j2name"), rs.getLong("cleanID"), jevis3id));


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void batchCopy(File file) throws IOException, JEVisException {


        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine();

                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                System.out.println("Export: " + Long.parseLong(parts[0]) + " -> " + Long.parseLong(parts[1]));
                export(Long.parseLong(parts[0]), Long.parseLong(parts[1]));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void export(long jevis2ID, long jevis3id) {
        this.pw.write("Start Export: " + jevis2ID + "->" + jevis3id);

        System.out.println("==================Export===============");
        PreparedStatement ps = null;
        String pattern2 = "yyyy-MM-dd HH:mm:ss Z";
        try {
            JEVisObject jevis3Obj = this.jevis3ds.getObject(jevis3id);
            JEVisAttribute value = jevis3Obj.getAttribute("Value");
//            value.deleteAllSample();


            ps = getConnection().prepareStatement(this.dataSQL);
            ps.setLong(1, jevis2ID);//73797l
            ps.setString(2, "2019-08-08");
            System.out.println("SQL2: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            List<JEVisSample> sampleList = new ArrayList<>();
            while (rs.next()) {
                try {

                    String tsString = rs.getString("TIMESTAMP").substring(0, 19) + " +00:00";//2007-08-31T16:47+00:00
                    DateTime ts = DateTime.parse(tsString, DateTimeFormat.forPattern(pattern2));

                    Double aDouble = rs.getDouble("VALUE");
                    if (ts.isBefore(this.dateTime)) {
                        sampleList.add(value.buildSample(ts, aDouble, rs.getString("NOTE")));
                    }


                } catch (Exception ex) {
                    logger.error("Could not load Object: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            this.pw.write("   Import:  " + sampleList.size());
            System.out.println("Import " + sampleList.size() + " sample from JEVis2: " + jevis2ID + "into JEVis3: " + jevis3id);
            value.addSamples(sampleList);
            this.pw.write("   OK DONE  ");
            sampleList.clear();

        } catch (Exception ex) {
            this.pw.write("   ERROR DONE:  " + ex);
            logger.error(ex);
            logger.error("Error while selecting Object: {} ", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /*ignored*/
                }
            }

            this.pw.write("\n");
            this.pw.flush();
        }
    }


//            DateTimeZone.setDefault(DateTimeZone.UTC);
//            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//                    DateTime unixdate = new DateTime(rs.getLong("unixtime"));
//                    DateTime ts = DateTime.parse(rs.getString("utc_datetime").substring(0, 19), DateTimeFormat.forPattern(pattern).withZoneUTC());

//                    Timestamp sqlTS = rs.getTimestamp("TIMESTAMP");
//                    DateTime ts = (new DateTime(sqlTS.getTime())).withZone(DateTimeZone.forID("Europe/Berlin"));

//                    DateTime ts = DateTime.parse(rs.getString("TIMESTAMP").substring(0, 19), DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID("Europe/Berlin")));
//                    DateTime ts = new DateTime(rs.getString("utc_datetime").substring(0, 19)).withZone(DateTimeZone.getDefault());
//                    System.out.println("----------");
//                    System.out.println(debufSample);
//                    System.out.println("ts0: " + rs.getString("utc_datetime"));
//                    System.out.println("ts1: " + ts + "  -> " + ts.getMillis());
//                    System.out.println("ts3: " + rs.getTimestamp(3));
//                    System.out.println("ts4: " + new DateTime(rs.getTimestamp(3).getTime()));
//                    System.out.println("ts5:" + ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(rs.getString("date_formatted")));

    //                    sampleList.add(value.buildSample(ts, aDouble, rs.getString("NOTE")));
    //                    if (rs.getString("NOTE") == null || !rs.getString("NOTE").equals("Timezone test")) {
//                        continue;
//                    }
//                    String debufSample = String.format("TS=%s|Obj=%s|Value:%s|UnixTS:%s|ISODATE:%s", rs.getString(3), rs.getString(1), rs.getString(2), rs.getString(5), rs.getString(6));


    public void registerMySQLDriver(String host, String port, String schema, String dbUser, String dbPW) {


        if (this.ds == null) {
            String conSring = "jdbc:mysql://" + host + ":" + port + "/" + schema + "?"
                    + "characterEncoding=UTF-8&useUnicode=true&character_set_client=UTF-8&character_set_connection=UTF-8&character_set_results=UTF-8";
//                    + "characterEncoding=UTF-8&amp;useUnicode=true";
//                    + "user=" + dbUser + "&password=" + dbPW;

            this.ds = new BasicDataSource();
            this.ds.setDriverClassName("com.mysql.jdbc.Driver");
            this.ds.setUrl(conSring);
            this.ds.setUsername(dbUser);
            this.ds.setPassword(dbPW);
            this.ds.setMaxTotal(100);
        }

    }

    public Connection getConnection() throws SQLException {
        if (this.ds != null) {
            return this.ds.getConnection();
        } else {
            throw new SQLException("No database driver registered");
        }

    }
}
