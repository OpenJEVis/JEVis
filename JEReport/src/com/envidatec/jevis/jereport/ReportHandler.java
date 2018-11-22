/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevCalendar;
import envidatec.jevis.capi.handler.login.JevLoginHandler;
import envidatec.jevis.capi.nodes.INode;
import envidatec.jevis.capi.nodes.NodeManager;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Handler;



/**
 *
 * @author broder
 */
public class ReportHandler //implements Pluggable //{
//public class ReportHandler
{  //zum testen

    private static final Logger _logger = Logger.getLogger(ReportHandler.class.getName());
    private static NodeManager nm = NodeManager.getInstance();
    public static String HOST;
    public static String USER;
    public static String PASS;
    public static int PORT;
    public static String SUBJECT;
    public boolean _running = true;
    List<Datasource> _datasourceList;
    //zum testen
    private static final ArrayList<Long> include = new ArrayList<Long>() {

        {
            add(1854l);
        }
    };
    private static final ArrayList<Long> exclude = new ArrayList<Long>() {

        {
        }
    };
    private static final ArrayList<Long> includeAlarm = new ArrayList<Long>() {

        {
            add(1422l);
        }
    };
    private static final ArrayList<Long> excludeAlarm = new ArrayList<Long>() {

        {
        }
    };

//    Methode zum testen
    public static void main(String[] args) {
        ReportHandler handler = new ReportHandler();
//        String user = args[0];
//        String pw = args[1];
//        String server = args[2];
//        String folder = args[3];
//        Long period = Long.parseLong(args[4]);
//        Long period = 21600000l;
//        handler.startTask(user, pw, server, folder, period);
//        handler.startTask("Admin", "eeS1esee", "http://192.168.2.21/axis2/services/JEWebService", "broder/tmp", 200000l);
//        handler.startTask("demo", "omed", "http://192.168.2.55/axis2/services/JEWebService", "broder/tmp", period);
        handler.startTask();
    }

//    public boolean startTask(String user, String pw, String server, String folder, Long period) {
    public void startTask() {


        ConfigProperties cp;
//        cp = new ConfigProperties();
        cp = new ConfigProperties("/home/broder/tmp/", "/home/broder/tmp/logfile", "http://192.168.2.202/axis2/services/JEWebService", "Admin", "Emooko4i", 0, "FINEST");
        
        JevLoginHandler.createDirectLogin(cp.getUser(), cp.getPassword(), cp.getServer());

        _logger.setLevel(Level.parse(cp.getLogLevel()));
        String folder = cp.getFolder();
        try {
            FileHandler fileHandler = new FileHandler(cp.getLogfile(), true);
            fileHandler.setFormatter(new LogFormatter());
            _logger.addHandler(fileHandler);
        } catch (IOException ex) {
            Logger.getLogger(ReportHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ReportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }


//        folder = "/tmp/";
//                    String folder = "/home/broder/tmp/";
//                    String folder = JevConfigManager.getProperty(parafolder).toString();


        _logger.info("starte Report Version r30");
//        while (_running) //läuft nachher in endlosschleife
//        {
        Date lastStart = new Date();

        nm.deleteHistory();
        List<RegTreeNode> registryFiltered = nm.getRegistryFiltered(include, true, exclude, true);

        _logger.info("Loglevel: " + cp.getLogLevel());
        logger.info("loglevel " + cp.getLogLevel());
        _logger.log(Level.FINER, "Reportlistsize: " + registryFiltered.size());
        ReportGenerator rg;
        Property prop;

        for (RegTreeNode reportHeadNode : registryFiltered) {
            try {
                //TODO: checken ob ein Report gemacht werden muss
                _logger.info("-----------------handel Node " + reportHeadNode.getID() + "--------");
                RegTreeNode reportNode = (RegTreeNode) reportHeadNode.getChildrenByType(916l).get(0);


                rg = new ReportGenerator();
                prop = new Property(reportNode);

                boolean newReport = newReportNecessary(prop);
                if (!newReport) {
                    _logger.info("--no Report necessary - creationDate not reached--");
                    continue;
                }
                _logger.info("--Report necessary - creationDate reached--");
                if (isAlarmReport(reportNode)) {

                    _logger.log(Level.FINER, "--AlarmReport--");
                    List<RegTreeNode> alarmNodes = nm.getRegistryFiltered(includeAlarm, true, excludeAlarm, true);
                    AlarmData alarmData = new AlarmData(alarmNodes, nm);
                    rg.generateAlarmReport(reportNode, prop, alarmData, folder);

                } else {
                    _logger.log(Level.FINER, "--NormalReport--");
                    _datasourceList = new ArrayList();

                    //checks, if there is data in all datasources    
                    boolean allNodesWithData = setDatasource(reportHeadNode, prop);
                    if (allNodesWithData) {
                        _logger.info("-- all Nodes with Data--");
                        //if there is data, init all datasources
                        for (Datasource s : _datasourceList) {
//                            if(!s.ignoreTimestamp()){
                            s.init();
//                            }
                        }

                        reportHeadNode.loadChildren();
                        rg.generateNormalReport(reportNode, prop, _datasourceList, folder);
                    } else {
                        _logger.info("-- not all Nodes with Data --");
                    }
                }

            } catch (Exception e) {
                _logger.throwing("ReportHandler", "startTask", e);
            }

        }

//            try {
//                _logger.info("--Report schläft--");
//                Thread.sleep(waitUntilPeriodEnd(lastStart, cp.getPeriod()));//ms
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
    }

    private boolean setDatasource(RegTreeNode reportHeadNode, Property property) {
        NodeManager nm = NodeManager.getInstance();

        List<INode> linklist = reportHeadNode.getChildrenByType(1801l);

        for (INode n : linklist) {
            Long id = Long.parseLong(n.getPropertyNode("LinkdID").getCurrentValue().getVal().toString());
            String test = n.getPropertyNode("Ignore Timestamps").getCurrentValue().getVal().toString();

            boolean bool = false;
            if (test.equals("1")) {
                bool = true;
            }

            JevCalendar c = new JevCalendar(new Date(property.getNextCreationDate().getTimeInMillis() - 1));

            c.setTimeZone(TimeZone.getTimeZone("UTC"));
            _logger.log(Level.INFO, "Nodeid " + id);
            if (!nm.getRegistryNode(id).isPropertyNode() & !bool) {
//                logger.info("MAXTS CreationDate" + c);
                if (getMaxTSOfOnlineNode(
                        nm.getRegistryNode(id)).before(c)) {

                    _logger.log(Level.INFO, "keine Daten f\u00fcr Node {0}", nm.getRegistryNode(id).getID() + "vor Datum " + c.getTime().toString());

                    return false;
                }
//                _datasourceList.add(new Datasource(nm.getRegistryNode(id), n.getPropertyNode("Variable name").getCurrentValue().getVal().toString(), property.getTimeSet(), property.getOldTimeSet(), property.getOldOldTimeSet(), property.getLastYearTimeSet(), property.getOldLastYearTimeSet()));
                _datasourceList.add(new Datasource(nm.getRegistryNode(id), n.getPropertyNode("Variable name").getCurrentValue().getVal().toString(), property));

            } else {
                _datasourceList.add(new Datasource(nm.getRegistryNode(id), n.getPropertyNode("Variable name").getCurrentValue().getVal().toString()));
            }

        }
        return true;

    }

//    public boolean isReport(Property prop) {
//        JevCalendar c = prop.getNextCreationDate();
//        logger.info(c.before(new JevCalendar()));
//        return c.before(new JevCalendar());
//    }
    /**
     * Checks when the next Period starts
     *
     * @param lastStart
     * @return time in long until the next start, 1 if period is allready over
     */
    private long waitUntilPeriodEnd(Date lastStart, long period) {
//        Date periodEnd = new Date(lastStart.getTime()
//                + Long.parseLong(JevConfigManager.getProperty(paraPeriod)));  //TODO die Config benutzen!

        Date periodEnd = new Date(lastStart.getTime() + period);

        Date now = new Date();

        _logger.log(Level.FINER, "Startzeit Report " + now);
        logger.info("Endzeit Report " + periodEnd);
        if (periodEnd.getTime() > now.getTime()) {
            _logger.log(Level.INFO, "next cycle starts in {0}"
                    + " msec" + " at {1}", new Object[]{
                        periodEnd.getTime() - now.getTime(), periodEnd
                    });
            return periodEnd.getTime() - now.getTime();
        } else {
            _logger.info("Period is already over start next cycle now ");
            return 10000;
        }
    }

    /**
     * Stop the instance of this JEReport
     *
     * @return
     */
    public boolean stopTask() {
        _logger.info("oh ... Ok i will stop if u wish");
        _running = false;
        return true;
    }

    public boolean isRunning() {
        return _running;
    }

    public boolean setLoggerAddHandler(Handler handler) {
        _logger.addHandler(handler);
        return true;
    }

    public boolean setLogLevel(Level level) {
        _logger.setLevel(level);
        return true;
    }

    private JevCalendar getMaxTSOfOnlineNode(RegTreeNode r) {
        if (r.getTypeID() == 96 || r.getTypeID() == 99) {
            return r.getMaxTS();
        } else {
            RegTreeNode tmp = (RegTreeNode) r.getParent();
            while (r.getRoot().getID() != tmp.getID()) {
                if (tmp.getTypeID() == 96) {
//                    logger.info("MAXTS onlineNode" + tmp.getMaxTS());
                    return tmp.getMaxTS();
                } else {
                    tmp = (RegTreeNode) tmp.getParent();
                }
            }
        }
        return null;
    }

    private boolean isAlarmReport(RegTreeNode reportNode) {
        boolean alarmReport = false;
        if (reportNode.getPropertyNode("AlarmReport") != null) {
            if (reportNode.getPropertyNode("AlarmReport").getCurrentValue().getVal().toString().equals("1")) {
                alarmReport = true;
            }
        }
        return alarmReport;
    }

    private boolean newReportNecessary(Property prop) {
        JevCalendar c = prop.getNextCreationDate();
//        logger.info(c.before(new JevCalendar()));
        _logger.log(Level.FINER, "Creation Data: " + c);
        return c.before(new JevCalendar());
    }
}
