package org.jevis.application.Chart.ChartPluginElements;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.plugin.ChartPlugin;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public interface ChartPluginColumn {
    Image imgMarkAll = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));
    SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    Tooltip tpMarkAll = new Tooltip(rb.getString("plugin.graph.dialog.changesettings.tooltip.forall"));

    default ChartDataModel getData(JEVisTreeRow row) {
        Long id = Long.parseLong(row.getID());
        if (getData() != null && getData().getSelectedData() != null && getData().containsId(id)) {
            return getData().get(id);
        } else {
            ChartDataModel newData = new ChartDataModel();
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

}
