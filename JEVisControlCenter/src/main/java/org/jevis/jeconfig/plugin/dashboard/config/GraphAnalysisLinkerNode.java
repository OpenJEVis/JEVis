package org.jevis.jeconfig.plugin.dashboard.config;

/**
 * @deprecated Legacy V1 dashboard graph analysis linker node format. This class exists solely for
 * backward-compatibility when reading old dashboard JSON files.
 * Use {@link org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo} and the
 * {@code config2} package instead.
 */
@Deprecated
public class GraphAnalysisLinkerNode {


    private Long graphAnalysisObject = 0L;

    public Long getGraphAnalysisObject() {
        return this.graphAnalysisObject;
    }

    public void setGraphAnalysisObject(Long graphAnalysisObject) {
        this.graphAnalysisObject = graphAnalysisObject;
    }

}
