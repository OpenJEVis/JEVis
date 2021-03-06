package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.List;

public class NewWidgetSelector extends GridPane {

    public enum SIZE {
        S_1x4, LARGE, S_16x16, S_16x24, S_1x6
    }

    final JFXComboBox<Widget> widgetComboBox;
    //final JFXComboBox<SIZE> sizeComboBox;
    final ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();

    public NewWidgetSelector(List<Widget> widgets) {


        ObservableList<Widget> options = FXCollections.observableArrayList(widgets);
        widgetComboBox = new JFXComboBox<>(options);

        Callback<ListView<Widget>, ListCell<Widget>> cellFactory = buildCellFactory();
        widgetComboBox.setCellFactory(cellFactory);
        widgetComboBox.setButtonCell(buildButtonCellFactory().call(null));
        widgetComboBox.getSelectionModel().selectFirst();

        Label labelType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.type"));
        /**
        Label sizeType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.size"));


         ObservableList<SIZE> sizeOptions = FXCollections.observableArrayList(SIZE.S_1x4, SIZE.S_16x16, SIZE.LARGE);
         sizeComboBox = new JFXComboBox<>(sizeOptions);
         sizeComboBox.getSelectionModel().selectFirst();
        sizeComboBox.setCellFactory(buildSizeCellFactory());
        sizeComboBox.setButtonCell(buildSizeCellFactory().call(null));
        **/
        ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);


        newB.setOnAction(event -> {
            selectedWidgetProperty.setValue(getSelectedWidget());
        });


        this.setHgap(8d);
        this.add(labelType, 0, 0);
        this.add(widgetComboBox, 1, 0);
        //this.add(sizeType, 2, 0);
        //this.add(sizeComboBox, 3, 0);
        this.add(newB, 4, 0);
    }

    public ObjectProperty<Widget> getSelectedWidgetProperty() {
        return selectedWidgetProperty;
    }

    public Widget getSelectedWidget() {
        Widget selectedWidget = widgetComboBox.getSelectionModel().getSelectedItem();
        //Size size = getSize(sizeComboBox.getSelectionModel().getSelectedItem());

        Widget newWidget = selectedWidget.getControl().createNewWidget(selectedWidget.createDefaultConfig());
        //newWidget.getConfig().setSize(size);

        return newWidget;
    }


    private Size getSize(SIZE size) {
        switch (size) {
            case S_1x4:
                return new Size(25, 100);  //1x4
            case S_16x16:
                return new Size(400, 400); //16x16
            case S_16x24:
                return new Size(400, 600); //16x16
            case S_1x6:
                return new Size(25, 150); //16x16
            case LARGE:
                return new Size(500, 1000); //20x40
            default:
                return new Size(400, 400); //16x16
        }
    }


    private Callback<ListView<SIZE>, ListCell<SIZE>> buildSizeCellFactory() {
        return new Callback<ListView<SIZE>, ListCell<SIZE>>() {
            @Override
            public ListCell<SIZE> call(ListView<SIZE> param) {
                return new ListCell<SIZE>() {
                    @Override
                    protected void updateItem(SIZE item, boolean empty) {
                        super.updateItem(item, empty);
                        String text = "";

                        if (item != null && !empty) {
                            if (item.equals(SIZE.S_1x4)) {
                                text = I18n.getInstance().getString("plugin.dashboard.toolbar.size.small");
                            } else if (item.equals(SIZE.S_16x16)) {
                                text = I18n.getInstance().getString("plugin.dashboard.toolbar.size.medium");
                            } else if (item.equals(SIZE.LARGE)) {
                                text = I18n.getInstance().getString("plugin.dashboard.toolbar.size.large");
                            }
                        }
                        setText(text);

                    }
                };
            }
        };
    }

    private Callback<ListView<Widget>, ListCell<Widget>> buildButtonCellFactory() {
        return new Callback<ListView<Widget>, ListCell<Widget>>() {
            @Override
            public ListCell<Widget> call(ListView<Widget> param) {
                return new ListCell<Widget>() {
                    @Override
                    protected void updateItem(Widget widget, boolean empty) {
                        super.updateItem(widget, empty);

                        if (widget == null) return;

                        setText(widget.typeID());

                    }
                };
            }
        };
    }

    private Callback<ListView<Widget>, ListCell<Widget>> buildCellFactory() {
        return new Callback<ListView<Widget>, ListCell<Widget>>() {
            @Override
            public ListCell<Widget> call(ListView<Widget> param) {
                return new ListCell<Widget>() {
                    @Override
                    protected void updateItem(Widget widget, boolean empty) {
                        super.updateItem(widget, empty);

                        if (widget == null) return;

                        setText(widget.typeID());

                        try {
                            ImageView preview = widget.getImagePreview();
                            preview.setPreserveRatio(false);
                            preview.setFitWidth(40);
                            preview.setFitHeight(40);
                            setGraphic(preview);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                };
            }
        };
    }


}
