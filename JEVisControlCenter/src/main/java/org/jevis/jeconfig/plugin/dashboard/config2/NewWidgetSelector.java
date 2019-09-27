package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.I18n;

import java.util.List;

public class NewWidgetSelector extends GridPane {

    public enum SIZE {
        SMALL, LARGE, MEDIUM
    }

    final ComboBox<Widget> widgetComboBox;
    final ComboBox<SIZE> sizeComboBox;
    final ObjectProperty<Widget> selectedWidgetProperty = new SimpleObjectProperty<>();

    public NewWidgetSelector(List<Widget> widgets) {


        ObservableList<Widget> options = FXCollections.observableArrayList(widgets);
        widgetComboBox = new ComboBox<>(options);

        Callback<ListView<Widget>, ListCell<Widget>> cellFactory = buildCellFactory();
        widgetComboBox.setCellFactory(cellFactory);
        widgetComboBox.setButtonCell(buildButtonCellFactory().call(null));
        widgetComboBox.getSelectionModel().selectFirst();

        Label labelType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.type"));
        Label sizeType = new Label(I18n.getInstance().getString("plugin.dashboard.toolbar.new.size"));


        ObservableList<SIZE> sizeOptions = FXCollections.observableArrayList(SIZE.SMALL, SIZE.MEDIUM, SIZE.LARGE);
        sizeComboBox = new ComboBox<>(sizeOptions);
        sizeComboBox.getSelectionModel().selectFirst();
        sizeComboBox.setCellFactory(buildSizeCellFactory());
        sizeComboBox.setButtonCell(buildSizeCellFactory().call(null));

        ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);


        newB.setOnAction(event -> {
            selectedWidgetProperty.setValue(getSelectedWidget());
        });


        this.setHgap(8d);
        this.add(labelType, 0, 0);
        this.add(widgetComboBox, 1, 0);
        this.add(sizeType, 2, 0);
        this.add(sizeComboBox, 3, 0);
        this.add(newB, 4, 0);
    }

    public ObjectProperty<Widget> getSelectedWidgetProperty() {
        return selectedWidgetProperty;
    }

    public Widget getSelectedWidget() {
        Widget selectedWidget = widgetComboBox.getSelectionModel().getSelectedItem();
        Size size = getSize(sizeComboBox.getSelectionModel().getSelectedItem());

        Widget newWidget = selectedWidget.clone();
        WidgetPojo widgetPojo = newWidget.getConfig();
        widgetPojo.setSize(size);

//        newWidget.setNodeSize(size.getWidth(), size.getHeight());
//        newWidget.getConfig().setTitle("New Widget");
        newWidget.updateConfig(widgetPojo);

        return newWidget;
    }


    private Size getSize(SIZE size) {
        switch (size) {
            case SMALL:
                return new Size(100, 100);
            case MEDIUM:
                return new Size(300, 600);
            case LARGE:
                return new Size(500, 1000);
            default:
                return new Size(100, 100);
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
                            if (item.equals(SIZE.SMALL)) {
                                text = I18n.getInstance().getString("plugin.dashboard.toolbar.size.small");
                            } else if (item.equals(SIZE.MEDIUM)) {
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
