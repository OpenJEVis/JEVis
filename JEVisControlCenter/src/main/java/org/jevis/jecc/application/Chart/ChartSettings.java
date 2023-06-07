package org.jevis.jecc.application.Chart;

import java.util.ArrayList;
import java.util.List;

public class ChartSettings {

    private List<ChartSetting> listSettings = new ArrayList<>();
    private Boolean isAutoSize = true;

    public ChartSettings() {
    }

    public List<ChartSetting> getListSettings() {
        return listSettings;
    }

    public void setListSettings(List<ChartSetting> listSettings) {
        this.listSettings = listSettings;
    }

    public Boolean getAutoSize() {
        return isAutoSize;
    }

    public void setAutoSize(Boolean autoSize) {
        isAutoSize = autoSize;
    }

    public void clear() {
        listSettings.clear();
    }
}
