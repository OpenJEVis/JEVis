package org.jevis.jecc.plugin.dashboard.datahandler;

public interface DataModelWidget {

    DataModelDataHandler getDataHandler();

    void setDataHandler(DataModelDataHandler dataHandler);

    void setCustomWorkday(Boolean customWorkday);
}
