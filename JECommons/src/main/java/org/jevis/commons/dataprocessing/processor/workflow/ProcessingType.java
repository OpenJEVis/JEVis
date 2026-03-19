package org.jevis.commons.dataprocessing.processor.workflow;

/**
 * Discriminates which type of data-processing pipeline is active for a given
 * {@link ProcessManager} run.
 *
 * <p>Having a single enum field instead of three separate boolean flags makes
 * impossible states (e.g. {@code isClean=true} AND {@code isForecast=true})
 * unrepresentable at compile time.</p>
 */
public enum ProcessingType {

    /**
     * Standard clean-data pipeline: PrepareStep → alignment → gap fill → diff → scaling → ImportStep.
     */
    CLEAN,

    /**
     * Forecast pipeline: PrepareForecast → ForecastStep → ImportStep.
     */
    FORECAST,

    /**
     * Math/formula pipeline: PrepareMath → MathStep → ImportStep.
     */
    MATH
}
