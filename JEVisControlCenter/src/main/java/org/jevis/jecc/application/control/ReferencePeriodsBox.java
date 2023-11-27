package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.i18n.I18n;

public class ReferencePeriodsBox extends ComboBox<GapFillingReferencePeriod> {
    private final ObservableList<GapFillingReferencePeriod> options = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
            GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR, GapFillingReferencePeriod.ALL);

    public ReferencePeriodsBox() {
        super();

        //TODO JFX17
        setConverter(new StringConverter<GapFillingReferencePeriod>() {
            @Override
            public String toString(GapFillingReferencePeriod object) {
                String text = "";
                if (object != null) {
                    switch (object) {
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
                }

                return text;
            }

            @Override
            public GapFillingReferencePeriod fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });

        setItems(options);
    }

    public ObservableList<GapFillingReferencePeriod> getOptions() {
        return options;
    }
}
