package org.jevis.commons.dataprocessing.processor.workflow;

/**
 * A single step in a data-processing pipeline.
 *
 * <p>Implementations read inputs from the supplied {@link ResourceManager},
 * perform their transformation, and write results back into the same
 * {@link ResourceManager} so that subsequent steps can consume them.</p>
 *
 * <p>Contract for implementors:</p>
 * <ul>
 *   <li>{@link #run} must be idempotent with respect to the
 *       {@link ResourceManager} state it receives — it should not rely on
 *       side-effects from a previous iteration.</li>
 *   <li>If the step cannot proceed (e.g. no raw data available), it should
 *       throw a {@link RuntimeException} with a descriptive message so the
 *       {@link ProcessManager} can mark the job as finished.</li>
 *   <li>Steps must not swallow exceptions silently; they propagate to
 *       {@link ProcessManager#start()} which handles logging and job
 *       clean-up.</li>
 * </ul>
 */
public interface ProcessStep {

    /**
     * Executes this processing step.
     *
     * @param resourceManager shared state carrier for the current pipeline run;
     *                        read inputs from it and write outputs back into it
     * @throws Exception if the step encounters an unrecoverable error
     */
    void run(ResourceManager resourceManager) throws Exception;

}
