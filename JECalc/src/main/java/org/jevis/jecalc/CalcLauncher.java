package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;

import java.util.*;
import java.util.concurrent.FutureTask;

/**
 * Entry point for the JECalc background service.
 * <p>
 * Execution modes:
 * <ul>
 *   <li><b>SERVICE</b> — polls on a configurable cycle; processes all enabled Calculation objects.</li>
 *   <li><b>SINGLE</b>  — processes the Calculation objects specified by their JEVis IDs on the
 *       command line, then exits.</li>
 *   <li><b>COMPLETE</b> — one-shot run of all enabled calculations, then exits.</li>
 * </ul>
 * Each job iterates up to 500 times (to handle cascading dependency recalculation) until
 * no new samples are produced. A thread pool (size configured via the service object) runs
 * jobs concurrently.
 */
public class CalcLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(CalcLauncher.class);
    private final Command commands = new Command();
    private static final String APP_INFO = "JECalc";

    public CalcLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        logger.info("-------Start JECalc-------");
        CalcLauncher app = new CalcLauncher(args, APP_INFO);
        app.execute();
    }

    @Override
    protected void runServiceHelp() {

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.isEmpty() && runningJobs.isEmpty()) {
                TaskPrinter.printJobStatus(LogTaskManager.getInstance());

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                    logger.info("Service is enabled.");
                    List<JEVisObject> dataSources = getEnabledCalcObjects();
                    this.executeCalcJobs(dataSources);
                } else {
                    logger.info("Service is disabled.");
                }
            } else {
                StringBuilder running = new StringBuilder();
                runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime).append(" "));
                logger.info("Still running queue - {}. Going to sleep again.", running.toString());
            }
        }

        sleep();
    }


    private void executeCalcJobs(List<JEVisObject> enabledCalcObject) {

        logger.info("Number of Calc Jobs: {}", enabledCalcObject.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        List<List<JEVisObject>> levels = buildDependencyLevels(enabledCalcObject);

        for (List<JEVisObject> level : levels) {
            List<FutureTask<?>> submitted = new ArrayList<>();
            for (JEVisObject object : level) {
                if (!runningJobs.containsKey(object.getID())) {
                    submitted.add(submitJob(object));
                } else {
                    logger.info("Still processing Job {}:{}", object.getName(), object.getID());
                }
            }

            // Parallelize within a level, but wait for the whole level to finish before
            // submitting the next one, so a Calc's freshly written output is visible to
            // any Calc in the next level that consumes it, within the same cycle.
            for (FutureTask<?> ft : submitted) {
                try {
                    ft.get();
                } catch (Exception e) {
                    logger.error("Error while waiting for calc job dependency level to complete", e);
                }
            }
        }
    }

    private FutureTask<?> submitJob(JEVisObject object) {
        Runnable runnable = () -> {
            try {
                Thread.currentThread().setName(object.getName() + ":" + object.getID().toString());
                runningJobs.put(object.getID(), new DateTime());
                logger.info("Starting Calc Job {} for {} @ {}", object.getName(), object.getID(), new DateTime().toString(DateTimeFormat.patternForStyle("MM", I18n.getInstance().getLocale())));

                LogTaskManager.getInstance().buildNewTask(object.getID(), object.getName());
                LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.STARTED);

                CalcJobFactory calcJobCreator = new CalcJobFactory();
                SampleHandler sampleHandler = new SampleHandler();

                boolean changed;
                int iteration = 0;

                do {
                    CalcJob calcJob = calcJobCreator.getCurrentCalcJob(sampleHandler, ds, object);
                    changed = calcJob.execute();
                    iteration++;
                } while (changed && calcJobCreator.isLastFetchTruncated() && iteration < 500);

                LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FINISHED);
            } catch (Exception e) {
                LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FAILED);

                logger.error("Failed Job: {}:{}", object.getName(), object.getID(), e);

            } finally {
                StringBuilder finished = new StringBuilder();
                finished.append(object.getID()).append(" in ");
                String length = new Period(runningJobs.get(object.getID()), new DateTime()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale()));
                removeJob(object);
                finished.append(length);

                StringBuilder running = new StringBuilder();
                runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime).append(" "));

                logger.info("Queued Jobs: {} | Finished {} | running Jobs: {}", plannedJobs.size(), finished.toString(), running.toString());

                checkLastJob();
            }
        };

        FutureTask<?> ft = new FutureTask<Void>(runnable, null);

        runnables.put(object.getID(), ft);
        executor.submit(ft);
        return ft;
    }

    /**
     * Groups enabled Calculations into dependency levels via Kahn's algorithm, based on a
     * producer→consumer graph: an edge exists from Calc A to Calc B when A's output attribute
     * feeds one of B's inputs. Jobs within a level have no relation to each other and are run in
     * parallel; {@link #executeCalcJobs} waits for a level to finish before submitting the next,
     * so same-cycle producer→consumer chains converge in one pass instead of needing a second
     * poll cycle. Falls back to a single shuffled level (today's behavior) if a cycle is detected
     * or dependency resolution fails.
     */
    private List<List<JEVisObject>> buildDependencyLevels(List<JEVisObject> enabledCalcObjects) {
        Map<Long, JEVisObject> byId = new HashMap<>();
        Map<Long, CalcJobFactory.CalcDependencyInfo> depInfo = new HashMap<>();

        for (JEVisObject object : enabledCalcObjects) {
            byId.put(object.getID(), object);
            try {
                depInfo.put(object.getID(), new CalcJobFactory().resolveDependencyInfo(object, ds));
            } catch (Exception e) {
                logger.error("Could not resolve dependencies for calc {}:{}, treating as independent", object.getName(), object.getID(), e);
                depInfo.put(object.getID(), new CalcJobFactory.CalcDependencyInfo(object.getID(), Collections.emptyList(), Collections.emptyList()));
            }
        }

        // output object id -> ids of enabled calcs that produce it
        Map<Long, Set<Long>> producersByOutputObject = new HashMap<>();
        for (CalcJobFactory.CalcDependencyInfo info : depInfo.values()) {
            for (Long outputObjectId : info.getOutputObjectIds()) {
                producersByOutputObject.computeIfAbsent(outputObjectId, k -> new HashSet<>()).add(info.getCalcObjectId());
            }
        }

        // producer calc id -> ids of calcs that consume one of its outputs
        Map<Long, Set<Long>> consumers = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Long id : byId.keySet()) {
            consumers.put(id, new HashSet<>());
            inDegree.put(id, 0);
        }
        for (CalcJobFactory.CalcDependencyInfo info : depInfo.values()) {
            Set<Long> producerIds = new HashSet<>();
            for (Long inputObjectId : info.getInputObjectIds()) {
                Set<Long> producers = producersByOutputObject.get(inputObjectId);
                if (producers != null) {
                    for (Long producerId : producers) {
                        if (!producerId.equals(info.getCalcObjectId())) {
                            producerIds.add(producerId);
                        }
                    }
                }
            }
            for (Long producerId : producerIds) {
                if (consumers.get(producerId).add(info.getCalcObjectId())) {
                    inDegree.merge(info.getCalcObjectId(), 1, Integer::sum);
                }
            }
        }

        List<List<JEVisObject>> levels = new ArrayList<>();
        Map<Long, Integer> remainingInDegree = new HashMap<>(inDegree);
        Set<Long> processed = new HashSet<>();
        int totalNodes = byId.size();

        while (processed.size() < totalNodes) {
            List<Long> currentLevelIds = new ArrayList<>();
            for (Long id : byId.keySet()) {
                if (!processed.contains(id) && remainingInDegree.get(id) == 0) {
                    currentLevelIds.add(id);
                }
            }

            if (currentLevelIds.isEmpty()) {
                logger.warn("Cycle detected in Calculation dependency graph; falling back to unordered execution for {} remaining object(s)", totalNodes - processed.size());
                List<JEVisObject> remaining = new ArrayList<>();
                for (Long id : byId.keySet()) {
                    if (!processed.contains(id)) {
                        remaining.add(byId.get(id));
                    }
                }
                Collections.shuffle(remaining);
                levels.add(remaining);
                break;
            }

            Collections.shuffle(currentLevelIds);
            List<JEVisObject> level = new ArrayList<>();
            for (Long id : currentLevelIds) {
                level.add(byId.get(id));
                processed.add(id);
                for (Long consumerId : consumers.get(id)) {
                    remainingInDegree.merge(consumerId, -1, Integer::sum);
                }
            }
            levels.add(level);
        }

        return levels;
    }

    private List<JEVisObject> getEnabledCalcObjects() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            DateTime start = new DateTime();
            ds.reloadObjects();
            logger.info("Reloaded objects in {}", new Period(new DateTime().getMillis() - start.getMillis()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale())));
            jevisObjects = ds.getObjects(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex.getMessage());
        }

        List<JEVisObject> jevisCalcObjects = jevisObjects;
        logger.info("{} calc task found", jevisCalcObjects.size());
        List<JEVisObject> enabledObjects = new ArrayList<>();
        SampleHandler sampleHandler = new SampleHandler();
        for (JEVisObject curObj : jevisCalcObjects) {
            Boolean valueAsBoolean = sampleHandler.getLastSample(curObj, CalcJobFactory.Calculation.ENABLED.getName(), false);
            if (valueAsBoolean) {
                enabledObjects.add(curObj);
                if (!plannedJobs.containsKey(curObj.getID())) {
                    plannedJobs.put(curObj.getID(), new DateTime());
                }
            }
        }

        // Ordering is now handled per-cycle in buildDependencyLevels(), which shuffles within
        // each dependency level rather than the whole flat list.
        return enabledObjects;
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JECalc";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        logger.info("Start Single Mode");
        for (Long id : ids) {
            try {
                JEVisObject object = ds.getObject(id);

                try {
                    CalcJobFactory calcJobCreator = new CalcJobFactory();
                    SampleHandler sampleHandler = new SampleHandler();

                    boolean changed;
                    int iteration = 0;

                    do {
                        CalcJob calcJob = calcJobCreator.getCurrentCalcJob(sampleHandler, ds, object);
                        changed = calcJob.execute();
                        iteration++;
                    } while (changed && calcJobCreator.isLastFetchTruncated() && iteration < 500);

                } catch (Exception e) {
                    logger.error(e);
                }
            } catch (Exception ex) {
                logger.error("JECalc: Single mode failed", ex);
            }
        }
    }

    @Override
    protected void runComplete() {
        logger.info("Start Complete Mode");
        List<JEVisObject> filterForEnabledCalcObjects = getEnabledCalcObjects();
        logger.info("{} enabled calc task found", filterForEnabledCalcObjects.size());

        executeCalcJobs(filterForEnabledCalcObjects);
    }

}