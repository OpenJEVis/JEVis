package org.jevis.jeconfig.plugin.Dashboard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.Interval;

public class ToolBarIntervalSelector extends HBox {


    public ToolBarIntervalSelector(Double iconSize) {

        ComboBox<IntervalPreset> listAnalysesComboBox = new ComboBox();
        listAnalysesComboBox.setPrefWidth(200);
        listAnalysesComboBox.setMinWidth(200);

        Callback<ListView<IntervalPreset>, ListCell<IntervalPreset>> cellFactory = new Callback<ListView<IntervalPreset>, ListCell<IntervalPreset>>() {
            @Override
            public ListCell<IntervalPreset> call(ListView<IntervalPreset> param) {
                final ListCell<IntervalPreset> cell = new ListCell<IntervalPreset>() {

//                    {
//                        super.setPrefWidth(300);
//                    }

                    @Override
                    protected void updateItem(IntervalPreset item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                            setGraphic(null);
                        }


                    }
                };

                return cell;
            }
        };

        listAnalysesComboBox.setCellFactory(cellFactory);
        listAnalysesComboBox.setButtonCell(cellFactory.call(null));
        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        ToggleButton dateButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(dateButton);

        ToggleButton prevButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);

        ToggleButton nextButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);


    }

    private ObservableList<IntervalPreset> getPresetInterval() {
        ObservableList<IntervalPreset> list = FXCollections.observableArrayList();


//        list.add(new IntervalPreset(I18n.getInstance().getString("plugin.graph.interval.preset"), Interval.parse()));

        return list;

    }


    public class IntervalPreset {

        private String name = "";
        private Interval interval = Interval.parse("P1D");

        public IntervalPreset(String name, Interval interval) {
            this.name = name;
            this.interval = interval;
        }

        public String getName() {
            return name;
        }


        public Interval getInterval() {
            return interval;
        }


    }
}
