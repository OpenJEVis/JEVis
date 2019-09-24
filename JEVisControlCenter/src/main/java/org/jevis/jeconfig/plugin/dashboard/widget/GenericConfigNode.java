package org.jevis.jeconfig.plugin.dashboard.widget;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrames;

public class GenericConfigNode extends Tab {

    Label nameLabel = new Label("Title");
    Label bgColorLabel = new Label("Color");
    Label fColorLabel = new Label("Font");
    Label idLabel = new Label("ID");
    Label idField = new Label("");
    Label shadowLabel = new Label("Show sahdow");

    Label yPosLabel = new Label("xPos");
    Label xPosLabel = new Label("yPos");
    Label timeFrameLabel = new Label("Fix TimeFrame");


    TextField nameField = new TextField();
    TextField yPosField = new TextField();
    TextField xPosField = new TextField();
    CheckBox showShadowField = new CheckBox();

    ColorPicker bgColorPicker = new ColorPicker();
    ColorPicker fColorPicker = new ColorPicker();
    ComboBox<TimeFrameFactory> timeFrameBox = new ComboBox();
    TimeFrames timeFrames;
    private Widget widget;
    private DataModelDataHandler dataModelDataHandler;

    public GenericConfigNode(JEVisDataSource ds, Widget widget, DataModelDataHandler dataModelDataHandler) {
        super("General");
        this.widget = widget;
        this.dataModelDataHandler = dataModelDataHandler;

        timeFrames = new TimeFrames(ds);
        bgColorPicker.setStyle("-fx-color-label-visible: false ;");
        fColorPicker.setStyle("-fx-color-label-visible: false ;");
        timeFrameBox.setPrefWidth(200);
        timeFrameBox.setMinWidth(200);

        Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>> cellFactory = new Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>>() {
            @Override
            public ListCell<TimeFrameFactory> call(ListView<TimeFrameFactory> param) {
                final ListCell<TimeFrameFactory> cell = new ListCell<TimeFrameFactory>() {

//                    {
//                        super.setPrefWidth(300);
//                    }

                    @Override
                    protected void updateItem(TimeFrameFactory item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getListName());
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        };
        timeFrameBox.setCellFactory(cellFactory);
        timeFrameBox.setButtonCell(cellFactory.call(null));
        timeFrames.setWorkdays(widget.control.getActiveDashboard().getDashboardObject());
        ObservableList<TimeFrameFactory> timeFrameFactories = FXCollections.observableArrayList();
        timeFrameFactories.add(timeFrames.emptyTimeFrame());
        timeFrameFactories.addAll(timeFrames.getAll());
        timeFrameBox.setItems(timeFrameFactories);


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        gridPane.addColumn(0, idLabel, nameLabel, bgColorLabel, fColorLabel, yPosLabel, xPosLabel, shadowLabel, timeFrameLabel);
        gridPane.addColumn(1, idField, nameField, bgColorPicker, fColorPicker, yPosField, xPosField, showShadowField, timeFrameBox);

        setContent(gridPane);

        updateData();

    }

    public void updateData() {
        /** ----------------------------------- set Values ---------------------------------------------**/
        nameField.setText(widget.getConfig().getTitle());
        idField.setText(widget.getConfig().getUuid() + "");
        bgColorPicker.setValue(widget.getConfig().getBackgroundColor());
        fColorPicker.setValue(widget.getConfig().getFontColor());
        yPosField.setText(widget.getConfig().getyPosition() + "");
        xPosField.setText(widget.getConfig().getxPosition() + "");

        showShadowField.setSelected(widget.getConfig().getShowShadow());
        if (dataModelDataHandler == null) {
            timeFrameBox.setDisable(true);
        } else if (dataModelDataHandler.isForcedInterval()) {
            timeFrameBox.getSelectionModel().select(dataModelDataHandler.getTimeFrameFactory());

            timeFrameBox.setOnAction(event -> {
                TimeFrameFactory timeFrameFactory = timeFrameBox.getSelectionModel().getSelectedItem();
                if (timeFrameFactory != null) {
                    if (timeFrameFactory.getID().equals(timeFrames.emptyTimeFrame().getID())) {
                        dataModelDataHandler.setForcedInterval(false);
                    } else {
                        dataModelDataHandler.setForcedInterval(true);
                        dataModelDataHandler.setForcedPeriod(timeFrameFactory.getID());
                    }

                }

            });

        } else {
            timeFrameBox.getSelectionModel().selectFirst();
        }

        /** --------------------- Add listeners----------------------------------------------------------*/
        showShadowField.setOnAction(event -> {
            widget.getConfig().setShowShadow(showShadowField.isSelected());
        });
    }
}
