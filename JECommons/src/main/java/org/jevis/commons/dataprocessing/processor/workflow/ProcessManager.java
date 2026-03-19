package org.jevis.commons.dataprocessing.processor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.delta.DeltaStep;
import org.jevis.commons.dataprocessing.processor.limits.LimitsStep;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareForecast;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareMath;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareStep;
import org.jevis.commons.dataprocessing.processor.steps.*;
import org.jevis.commons.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates a single data-processing run for one JEVis object.
 *
 * <p>A {@code ProcessManager} is instantiated per object, determines the
 * {@link ProcessingType} (CLEAN / FORECAST / MATH) from the object's JEVis
 * class, builds the appropriate {@link ProcessStep} pipeline, and calls
 * {@link #start()} to execute it.</p>
 *
 * <p>For CLEAN objects the pipeline loops (via {@code do…while (!isFinished)})
 * to process data in {@code processingSize}-bounded batches.  For FORECAST and
 * MATH objects the loop continues while the domain object reports
 * {@code isReady()}.</p>
 *
 * <p>Between batches all transient state is cleared via
 * {@link #clearResourceManagerState()} so the next iteration starts fresh.</p>
 */
public class ProcessManager {

    private static final Logger logger = LogManager.getLogger(ProcessManager.class);
    private final ResourceManager resourceManager;
    private final ObjectHandler objectHandler;
    private final int processingSize;
    private String name;
    private Long id;
    private List<ProcessStep> processSteps = new ArrayList<>();
    /**
     * Discriminates which pipeline this manager runs.
     */
    private ProcessingType processingType = ProcessingType.CLEAN;
    private boolean isFinished = false;

    /**
     * Creates a new manager for {@code cleanObject}.
     *
     * @param cleanObject    the JEVis object to process (CleanData, ForecastData, or MathData)
     * @param objectHandler  helper for loading related objects from the data source
     * @param processingSize maximum number of intervals to process per batch
     */
    public ProcessManager(JEVisObject cleanObject, ObjectHandler objectHandler, int processingSize) {
        this.resourceManager = new ResourceManager();

        this.name = cleanObject.getName();
        this.id = cleanObject.getID();
        this.objectHandler = objectHandler;
        this.processingSize = processingSize;

        JEVisClass cleanDataClass;
        JEVisClass forecastDataClass;
        JEVisClass mathDataClass;
        try {
            cleanDataClass = cleanObject.getDataSource().getJEVisClass(CleanDataObject.CLASS_NAME);
            forecastDataClass = cleanObject.getDataSource().getJEVisClass(ForecastDataObject.CLASS_NAME);
            mathDataClass = cleanObject.getDataSource().getJEVisClass(MathDataObject.CLASS_NAME);
            resourceManager.setTimeZone(CommonMethods.getTimeZone(cleanObject));

            if (cleanObject.getJEVisClass().equals(cleanDataClass)) {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                processSteps.clear();
                addDefaultSteps();
            } else if (cleanObject.getJEVisClass().equals(forecastDataClass)) {
                this.resourceManager.setForecastDataObject(new ForecastDataObject(cleanObject, objectHandler));
                this.resourceManager.getForecastDataObject().setProcessingSize(processingSize);
                this.name = resourceManager.getForecastDataObject().getForecastDataObject().getName();
                this.id = resourceManager.getForecastDataObject().getForecastDataObject().getID();
                processSteps.clear();
                addForecastSteps();
                processingType = ProcessingType.FORECAST;
                resourceManager.setProcessingType(ProcessingType.FORECAST);
            } else if (cleanObject.getJEVisClass().equals(mathDataClass)) {
                this.resourceManager.setMathDataObject(new MathDataObject(cleanObject, objectHandler));
                this.resourceManager.getMathDataObject().setProcessingSize(processingSize);
                this.name = this.resourceManager.getMathDataObject().getMathDataObject().getName();
                this.id = this.resourceManager.getMathDataObject().getMathDataObject().getID();
                processSteps.clear();
                addMathSteps();
                processingType = ProcessingType.MATH;
                resourceManager.setProcessingType(ProcessingType.MATH);
            } else {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                processSteps.clear();
                addDefaultSteps();
            }
        } catch (Exception e) {
            logger.error("Could not determine object", e);
        }
    }

    private void addDefaultSteps() {

        ProcessStep preparation = new PrepareStep(this);
        processSteps.add(preparation);

        ProcessStep alignmentStep = new PeriodAlignmentStep();
        processSteps.add(alignmentStep);

        ProcessStep gapStep = new FillGapStep();
        processSteps.add(gapStep);

        ProcessStep diffStep = new DifferentialStep();
        processSteps.add(diffStep);

        ProcessStep multiStep = new ScalingStep();
        processSteps.add(multiStep);

        ProcessStep aggregationAlignmentStep = new AggregationAlignmentStep();
        processSteps.add(aggregationAlignmentStep);

        ProcessStep limitsStep = new LimitsStep();
        processSteps.add(limitsStep);

        ProcessStep deltaStep = new DeltaStep();
        processSteps.add(deltaStep);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    private void addForecastSteps() {

        ProcessStep preparation = new PrepareForecast();
        processSteps.add(preparation);

        ProcessStep forecast = new ForecastStep();
        processSteps.add(forecast);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    private void addMathSteps() {

        ProcessStep preparation = new PrepareMath();
        processSteps.add(preparation);

        ProcessStep math = new MathStep();
        processSteps.add(math);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

    /**
     * Runs the full processing pipeline for the configured object.
     *
     * <p>CLEAN objects are processed in batches until {@link #isFinished} is
     * {@code true}.  FORECAST and MATH objects loop while the domain object
     * reports {@code isReady()}.</p>
     *
     * @throws Exception if any pipeline step throws an unrecoverable error
     */
    public void start() throws Exception {
        if (processingType == ProcessingType.CLEAN) {
            do {
                logger.info("[{}:{}] Starting Process", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

                if (resourceManager.getCleanDataObject().checkConfig()) {
                    reRun();
                } else setFinished(true);

                logger.info("[{}:{}] Finished", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

                reinitializeCleanData();
            } while (!isFinished);
        } else if (processingType == ProcessingType.FORECAST) {
            logger.info("[{}:{}] Starting Process", resourceManager.getForecastDataObject().getForecastDataObject().getName(), resourceManager.getID());

            while (resourceManager.getForecastDataObject().isReady(resourceManager.getForecastDataObject().getForecastDataObject())) {
                reRun();
                resourceManager.getForecastDataObject().finishCurrentRun(resourceManager.getForecastDataObject().getForecastDataObject());

                reinitializeForecastData();
            }

            logger.info("[{}:{}] Finished", resourceManager.getForecastDataObject().getForecastDataObject().getName(), resourceManager.getID());
        } else if (processingType == ProcessingType.MATH) {
            logger.info("[{}:{}] Starting Process", resourceManager.getMathDataObject().getMathDataObject().getName(), resourceManager.getID());

            while (resourceManager.getMathDataObject().isReady()) {
                reRun();
                resourceManager.getMathDataObject().finishCurrentRun(resourceManager.getMathDataObject().getMathDataObject());

                reinitializeMathData();
            }

            logger.info("[{}:{}] Finished", resourceManager.getMathDataObject().getMathDataObject().getName(), resourceManager.getID());
        }
    }

    /**
     * Clears all transient per-batch state from the {@link ResourceManager}.
     * Called before each new batch iteration so stale data from the previous
     * batch cannot bleed into the next one.
     */
    private void clearResourceManagerState() {
        resourceManager.setIntervals(null);
        resourceManager.setNotesMap(null);
        resourceManager.setUserDataMap(null);
        resourceManager.setRawSamplesDown(null);
        resourceManager.setSampleCache(null);
        resourceManager.setRawIntervals(null);
    }

    private void reinitializeCleanData() {
        processSteps.clear();

        JEVisObject cleanObject = resourceManager.getCleanDataObject().getCleanObject();

        clearResourceManagerState();
        resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
        resourceManager.getCleanDataObject().setProcessingSize(processingSize);

        addDefaultSteps();
    }

    private void reinitializeForecastData() {
        processSteps.clear();

        JEVisObject forecastObject = resourceManager.getForecastDataObject().getForecastDataObject();

        clearResourceManagerState();
        resourceManager.setForecastDataObject(new ForecastDataObject(forecastObject, objectHandler));
        resourceManager.getForecastDataObject().setProcessingSize(processingSize);

        addForecastSteps();
    }

    private void reinitializeMathData() {
        processSteps.clear();

        JEVisObject mathObject = resourceManager.getMathDataObject().getMathDataObject();

        clearResourceManagerState();
        resourceManager.setMathDataObject(new MathDataObject(mathObject, objectHandler));
        resourceManager.getMathDataObject().setProcessingSize(processingSize);

        addMathSteps();
    }

    private void reRun() throws Exception {

        if (processingType == ProcessingType.CLEAN) {
            resourceManager.getCleanDataObject().reloadAttributes();
        } else if (processingType == ProcessingType.FORECAST) {
            resourceManager.getForecastDataObject().reloadAttributes();
        } else if (processingType == ProcessingType.MATH) {
            resourceManager.getMathDataObject().reloadAttributes();
        }

        for (ProcessStep ps : processSteps) {
            try {
                ps.run(resourceManager);
            } catch (Exception e) {
                setFinished(true);
                if (ps instanceof PrepareStep || ps instanceof PrepareForecast || ps instanceof PrepareMath) {
                    logger.info("Error in step {} of object {}:{}", ps, this.getName(), this.getId(), e);
                } else {
                    logger.error("Error in step {} of object {}:{}", ps, this.getName(), this.getId(), e);
                }
                throw e;
            }
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

}
