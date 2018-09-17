/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import com.beust.jcommander.Parameter;
import org.apache.log4j.*;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.DataSourceFactory;
import org.jevis.commons.driver.DriverHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 * @author ai
 */
public class Launcher extends AbstractCliApp {

    public static final String APP_INFO = "JEDataCollector 2018-02-21";
    public static String KEY = "process-id";
    private static Logger logger = Logger.getRootLogger();
    private int cycleTime = 900000;
    private final Command commands = new Command();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Appender appender = Logger.getRootLogger().getAppender("FILE");
            appender.addFilter(new ThreadFilter("-1"));
        } catch (Exception ex) {
            logger.error("Error loading log Appender 'FILE'", ex);
        }

        MDC.put(Launcher.KEY, "-1");
        Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "-------Start JEDataCollector 2018-02-01-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    public Launcher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * Run all datasources in Threads. The maximum number of threads is defined
     * in the JEVis System.
     * <p>
     * PROBLEM: with the new structure. Probably not all data sources are able
     * to handle multiple queries. Any solutions? Maybe without threads?
     *
     * @param dataSources
     */
    private void excecuteDataSources(List<JEVisObject> dataSources) {
        Logger.getLogger(
                this.getClass().getName()).log(Level.INFO, "Number of Requests: " + dataSources.size());

//        Long startTime = System.currentTimeMillis();
//
//        long maxNumberThreads = getNumberOfMaxThreads();
//        int identifier = 1;
//        ThreadHandler threadReqHandler = new ThreadHandler(dataSources);
//        MDC.remove(Launcher.KEY);
//        while (threadReqHandler.hasRequest()) {
//            int activeCount = threadReqHandler.getNumberActiveRequests();
//            if (activeCount < maxNumberThreads) {
//                JEVisObject currentDataSourceJevis = threadReqHandler.getNextDataSource();
//                initNewAppender("" + identifier, currentDataSourceJevis.getID() + "_" + currentDataSourceJevis.getName().replace(" ", "_") + ".log");
//                MDC.put(Launcher.KEY, "" + identifier);
        for (JEVisObject object : dataSources) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "----------------Execute DataSource " + object.getName() + "-----------------");
            DataSource dataSource = DataSourceFactory.getDataSource(object);

            dataSource.initialize(object);
            dataSource.run();
        }


//                try {
//                    DataSource dataSource = DataSourceFactory.getDataSource(currentDataSourceJevis);
//                    dataSource.initialize(currentDataSourceJevis);
//                    Thread dataCollectionThread = new Thread(dataSource, currentDataSourceJevis.getName());
//                    long threadid = dataCollectionThread.getId();
////                System.out.println("start equip:" + currentDataSourceJevis.getName() + "id." + threadid);
//                    threadReqHandler.addActiveThread(threadid);
//                    //start the data source in a new thread
//                    dataCollectionThread.start();
//                } catch (Exception ex) {
//                    Logger.getLogger(Launcher.class.getName()).log(Level.ERROR, ex.getMessage());
//                }
//                identifier++;
//                MDC.remove(Launcher.KEY);
//            } else {
//                try {
//                    boolean foundFinishedThread = false;
//                    List<Long> finishedThreads = new ArrayList<Long>();
//                    Set<Long> currentThreadIds = new HashSet<Long>();
//                    for (Thread t : Thread.getAllStackTraces().keySet()) {
//                        currentThreadIds.add(t.getId());
////                        System.out.println("thread_id:" + t.getId());
//                    }
//                    for (Long id : threadReqHandler.getActiveThreads()) {
//                        if (!currentThreadIds.contains(id)) {
//                            finishedThreads.add(id);
//                            foundFinishedThread = true;
//                        }
//                    }
//                    if (foundFinishedThread) {
//                        for (Long id : finishedThreads) {
////                            System.out.println("Remove equip id: " + id);
//                            threadReqHandler.removeActiveRequest(id);
//                        }
//                    } else {
//                        Thread.sleep(10000);
////                        System.out.println("thread sleeps");
//                    }
//                } catch (InterruptedException ie) {
//                    Logger.getLogger(Launcher.class.getName()).log(Level.ERROR, ie.getMessage());
//                }
//            }
//        }
//        //if runtime reached, then remove all threads and end the programm
//        Long maxRuntime = getMaxRunTime() * 1000l;
//
//        while (threadReqHandler.getNumberActiveRequests() != 0) {
//            Long currentRuntime = startTime - System.currentTimeMillis();
//            if (currentRuntime > maxRuntime) {
//                List<Long> finishedThreads = new ArrayList<Long>();
//                List<Long> abortThreads = new ArrayList<Long>();
//                Map<Long, Thread> currentThreadIds = new HashMap<Long, Thread>();
//                for (Thread t : Thread.getAllStackTraces().keySet()) {
//                    currentThreadIds.put(t.getId(), t);
//                }
//                for (Long id : threadReqHandler.getActiveThreads()) {
//                    if (!currentThreadIds.containsKey(id)) {
//                        finishedThreads.add(id);
//                    } else {
//                        Thread curThread = currentThreadIds.get(id);
//                        curThread.interrupt();
//                        abortThreads.add(id);
//                    }
//                }
//                for (Long id : finishedThreads) {
////                    System.out.println("Remove equip id: " + id);
//                    threadReqHandler.removeActiveRequest(id);
//                }
//                for (Long id : abortThreads) {
////                    System.out.println("Abort equip id: " + id);
//                    threadReqHandler.removeActiveRequest(id);
//                }
//            }
//            try {
//                boolean foundFinishedThread = false;
//                List<Long> finishedThreads = new ArrayList<Long>();
//                Set<Long> currentThreadIds = new HashSet<Long>();
//                for (Thread t : Thread.getAllStackTraces().keySet()) {
//                    currentThreadIds.add(t.getId());
////                    System.out.println("thread_id:" + t.getId());
//                }
//                for (Long id : threadReqHandler.getActiveThreads()) {
//                    if (!currentThreadIds.contains(id)) {
//                        finishedThreads.add(id);
//                        foundFinishedThread = true;
//                    }
//                }
//                if (foundFinishedThread) {
//                    for (Long id : finishedThreads) {
////                        System.out.println("Remove equip id: " + id);
//                        threadReqHandler.removeActiveRequest(id);
//                    }
//                } else {
//                    Thread.sleep(10000);
////                    System.out.println("thread sleeps");
//                }
//            } catch (InterruptedException ie) {
//                Logger.getLogger(Launcher.class.getName()).log(Level.ERROR, ie.getMessage());
//            }
//        }
//        try {
//            ds.disconnect();
//        } catch (JEVisException ex) {
//            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        Logger.getLogger(Launcher.class.getName()).log(Level.INFO, "---------------------finish------------------------");

    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        DriverHelper.loadDriver(ds, commands.driverFolder);
    }

    @Override
    protected void runSingle(Long id) {
        java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, "Start Single Mode");

        List<JEVisObject> dataSources = new ArrayList<JEVisObject>();

        try {
            logger.info("Try adding Single Mode for ID " + id);
            dataSources.add(ds.getObject(id));

            excecuteDataSources(dataSources);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    protected void runService(Integer cycle_time) {
        java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, "Start Service Mode");

        Thread service = new Thread(() -> runServiceHelp());
        Runtime.getRuntime().addShutdownHook(
                new JEDataCollectorShutdownHookThread(service)
        );

        if (cycle_time != null) cycleTime = cycle_time;

        try {

            service.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runServiceHelp() {
        List<JEVisObject> dataSources = new ArrayList<>();
        dataSources = getEnabledDataSources(ds);
        excecuteDataSources(dataSources);
        try {
            logger.error("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);
            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    @Override
    protected void runComplete() {
        java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, "Start Compete Mode");
        List<JEVisObject> dataSources = new ArrayList<JEVisObject>();
        dataSources = getEnabledDataSources(ds);
        excecuteDataSources(dataSources);
    }

    protected class Command {

        @Parameter(names = {"--driver-folder", "-df"}, description = "Sets the root folder for the driver structure")
        private String driverFolder;
    }

    private int getNumberOfMaxThreads() {
        try {
            JEVisClass collectorClass = ds.getJEVisClass(DataCollectorTypes.JEDataCollector.NAME);
            JEVisType numberThreadsType = collectorClass.getType(DataCollectorTypes.JEDataCollector.MAX_NUMBER_THREADS);
            List<JEVisObject> dataCollector = ds.getObjects(collectorClass, false);
            if (dataCollector.size() == 1) {
                return (int) (long) dataCollector.get(0).getAttribute(numberThreadsType).getLatestSample().getValueAsLong();
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return 1;
    }

    private long getMaxRunTime() {
        try {
            JEVisClass collectorClass = ds.getJEVisClass(DataCollectorTypes.JEDataCollector.NAME);
            JEVisType runTimeType = collectorClass.getType(DataCollectorTypes.JEDataCollector.DATA_SOURCE_TIMEOUT);
            List<JEVisObject> dataCollector = ds.getObjects(collectorClass, false);
            if (dataCollector.size() == 1) {
                return dataCollector.get(0).getAttribute(runTimeType).getLatestSample().getValueAsLong();
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return 3600l;
    }

    private void initNewAppender(String NameForAppender, String Name4LogFile) {
        FileAppender appender = new FileAppender();
        appender.setLayout(new PatternLayout("[%d{yyyy-MM--dd HH:mm:ss}][%c{2}]: %-10m%n"));
        appender.setFile(Name4LogFile);
        appender.setAppend(true);
        appender.setImmediateFlush(true);
        appender.activateOptions();
        appender.setName(NameForAppender);
        appender.addFilter(new ThreadFilter(NameForAppender));
        logger.setAdditivity(false);    //<--do not use default root logger
        logger.addAppender(appender);
    }

    private List<JEVisObject> getEnabledDataSources(JEVisDataSource client) {
        List<JEVisObject> enabledDataSources = new ArrayList<JEVisObject>();
        try {
            JEVisClass dataSourceClass = client.getJEVisClass(DataCollectorTypes.DataSource.NAME);
            JEVisType enabledType = dataSourceClass.getType(DataCollectorTypes.DataSource.ENABLE);
            List<JEVisObject> allDataSources = client.getObjects(dataSourceClass, true);
            for (JEVisObject dataSource : allDataSources) {
                try {
                    Boolean enabled = DatabaseHelper.getObjectAsBoolean(dataSource, enabledType);
                    if (enabled && DataSourceFactory.containDataSource(dataSource)) {
                        enabledDataSources.add(dataSource);
                    }
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, "DataSource failed while checking enabled status:", ex);
                }
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return enabledDataSources;
    }

}
