package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.i18n.I18n;

public class ReferencePeriodsBox extends JFXComboBox<GapFillingReferencePeriod> {
    private final ObservableList<GapFillingReferencePeriod> options = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
            GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR, GapFillingReferencePeriod.ALL);

    public ReferencePeriodsBox() {
        super();

        Callback<ListView<GapFillingReferencePeriod>, ListCell<GapFillingReferencePeriod>> cellFactoryReferencePeriodBox = new Callback<javafx.scene.control.ListView<GapFillingReferencePeriod>, ListCell<GapFillingReferencePeriod>>() {
            @Override
            public ListCell<GapFillingReferencePeriod> call(javafx.scene.control.ListView<GapFillingReferencePeriod> param) {
                return new ListCell<GapFillingReferencePeriod>() {
                    @Override
                    protected void updateItem(GapFillingReferencePeriod referencePeriod, boolean empty) {
                        super.updateItem(referencePeriod, empty);
                        if (empty || referencePeriod == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (referencePeriod) {
                                case DAY:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.day");
                                    break;
                                case WEEK:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.week");
                                    break;
                                case MONTH:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.month");
                                    break;
                                case YEAR:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.year");
                                    break;
                                case ALL:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.all");
                                    break;
                                case NONE:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.none");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactoryReferencePeriodBox);
        setButtonCell(cellFactoryReferencePeriodBox.call(null));
        setItems(options);
    }

    public ObservableList<GapFillingReferencePeriod> getOptions() {
        return options;
    }
}
