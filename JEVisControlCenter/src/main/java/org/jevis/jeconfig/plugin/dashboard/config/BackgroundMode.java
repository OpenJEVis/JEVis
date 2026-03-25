package org.jevis.jeconfig.plugin.dashboard.config;

/**
 * @deprecated Legacy V1 dashboard background mode constants. This interface exists solely for
 * backward-compatibility when reading old dashboard JSON files.
 * Use {@link org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo} and the
 * {@code config2} package instead.
 */
@Deprecated
public interface BackgroundMode {

    String defaultMode="default";
    String repeat ="repeat";
    String stretch= "stretch";
}
