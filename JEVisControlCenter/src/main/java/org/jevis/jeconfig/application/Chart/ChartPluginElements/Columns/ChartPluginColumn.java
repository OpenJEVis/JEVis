package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.jevis.jeconfig.tool.I18n;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public interface ChartPluginColumn {
    Image imgMarkAll = new Image(ChartPluginTree.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));

    Tooltip tooltipMarkAll = new Tooltip(I18n.getInstance().getString("plugin.graph.dialog.changesettings.tooltip.forall"));

    default ChartDataModel getData(JEVisTreeRow row) {
        Long id = Long.parseLong(row.getID());
        if (getData() != null && getData().getSelectedData() != null && getData().containsId(id)) {
            return getData().get(id);
        } else {
            ChartDataModel newData = new ChartDataModel(getDataSource());
            newData.setObject(row.getJEVisObject());
            try {
                for (JEVisObject obj : row.getJEVisObject().getChildren()) {
                    if (obj.getJEVisClassName().equals("Clean Data"))
                        newData.setDataProcessor(obj);
                    break;
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            newData.setAttribute(row.getJEVisAttribute());

            getData().getSelectedData().add(newData);

            return newData;
        }
    }

    void setGraphDataModel(GraphDataModel graphDataModel);

    void buildColumn();

    default void update() {
        buildColumn();
    }

    GraphDataModel getData();

    JEVisDataSource getDataSource();

}
