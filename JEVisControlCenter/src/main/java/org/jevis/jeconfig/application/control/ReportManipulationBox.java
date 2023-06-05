package org.jevis.jeconfig.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;

public class ReportManipulationBox extends MFXComboBox<ManipulationMode> {

    public ReportManipulationBox() {
        super();

        final String keyNone = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.none");
        final String keyAverage = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.average");
        final String keySortedMin = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.sortedmin");
        final String keySortedMax = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.sortedmax");
        final String keyMin = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.min");
        final String keyMax = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.max");
        final String keyMedian = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.median");
        final String keyRunningMean = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.runningmean");
        final String keyCentricRunningMean = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.centricrunningmean");

        setItems(FXCollections.observableArrayList(ManipulationMode.values()));

        //TODO JFX17
        setConverter(new StringConverter<ManipulationMode>() {
            @Override
            public String toString(ManipulationMode object) {

                String text = "";
                switch (object) {
                    case AVERAGE:
                        text += keyAverage;
                        break;
                    case MIN:
                        text += keyMin;
                        break;
                    case MAX:
                        text += keyMax;
                        break;
                    case MEDIAN:
                        text += keyMedian;
                        break;
                    case RUNNING_MEAN:
                        text += keyRunningMean;
                        break;
                    case CENTRIC_RUNNING_MEAN:
                        text += keyCentricRunningMean;
                        break;
                    case SORTED_MIN:
                        text += keySortedMin;
                        break;
                    case SORTED_MAX:
                        text += keySortedMax;
                        break;
                    case CUMULATE:
                        break;
                    default:
                    case NONE:
                        text = keyNone;
                        break;
                }

                return text;
            }

            @Override
            public ManipulationMode fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

        getSelectionModel().selectFirst();
    }
}
