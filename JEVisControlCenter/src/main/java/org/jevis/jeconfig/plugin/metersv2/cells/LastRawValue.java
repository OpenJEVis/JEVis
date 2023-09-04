package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LastRawValue extends TableColumn<MeterData, String> {







    public LastRawValue(String s, JEVisDataSource ds, JEVisType jeVisType, int width) {
        super(s);
          this.setCellValueFactory(meterDataJEVisSampleCellDataFeatures -> {

              try {
                  TargetHelper th = new TargetHelper(ds,meterDataJEVisSampleCellDataFeatures.getValue().getJeVisObject().getAttribute(jeVisType));
                  if(th.getAttribute().size() == 0) return new SimpleStringProperty("");
                  if(!th.getAttribute().get(0).hasSample())  return new SimpleStringProperty("");
                  return  new SimpleStringProperty(th.getAttribute().get(0).getLatestSample().getValueAsString()+" "+th.getAttribute().get(0).getLatestSample().getUnit().getLabel());
              } catch (Exception e) {

              }


              return new SimpleStringProperty("");
          });

        this.setCellFactory(new ShortColumnCell<MeterData>());
        this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(true);


    }
}
