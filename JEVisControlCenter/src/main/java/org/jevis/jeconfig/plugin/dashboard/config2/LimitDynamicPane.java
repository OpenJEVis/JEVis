package org.jevis.jeconfig.plugin.dashboard.config2;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.widget.ValueWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.Optional;

public class LimitDynamicPane extends GridPane {
    private static final Logger logger = LogManager.getLogger(LimitDynamicPane.class);
    private final Label upperColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.upper"));
    private final Label lowerColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.lower"));
    private final JFXCheckBox enableUpperBox = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.upper"));
    private final JFXCheckBox enableLowerBox = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.lower"));
    //    private ColorPicker upperColorPicker = new ColorPicker();
//    private ColorPicker lowerColorPicker = new ColorPicker();
    private final ColorPickerAdv upperColorPicker = new ColorPickerAdv();
    private final ColorPickerAdv lowerColorPicker = new ColorPickerAdv();


    private final Label upperVOffsetlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.upper"));
    private final Label lowerVOffsetlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.lower"));
    private final JFXTextField upperValueField = new JFXTextField();
    private final JFXTextField lowerValueField = new JFXTextField();

    private final Label sourceLable = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.source"));
    private final ObservableList<Widget> widgetList;
    private final Limit limit;
    private JFXComboBox<Widget> widgetBox;


    public LimitDynamicPane(Limit limit, ObservableList<Widget> widgetList) {
        this.limit = limit;
        widgetList.sort((o1, o2) -> {
            try {
                return Integer.compare(o1.getConfig().getUuid(), o2.getConfig().getUuid());
            } catch (Exception e) {
                logger.error("Could not get uuid for widgets {} and {}", o1, o2);
            }
            return 0;
        });
        this.widgetList = widgetList;

        initControls();

        setPadding(new Insets(10));
        setVgap(8);
        setHgap(8);

        addRow(0, sourceLable, widgetBox);
        add(new Separator(Orientation.HORIZONTAL), 0, 1, 2, 1);
        add(enableUpperBox, 0, 2, 2, 1);
        addRow(3, upperVOffsetlabel, upperValueField);
        addRow(4, upperColorlabel, upperColorPicker);
        add(new Separator(Orientation.HORIZONTAL), 0, 5, 2, 1);
        add(enableLowerBox, 0, 6, 2, 1);
        addRow(8, lowerVOffsetlabel, lowerValueField);
        addRow(9, lowerColorlabel, lowerColorPicker);



    }

    private void initControls() {
        enableUpperBox.setSelected(limit.hasUpperLimit);
        enableLowerBox.setSelected(limit.hasLowerLimit);

        upperColorPicker.setValue(limit.upperLimitColor);
        lowerColorPicker.setValue(limit.lowerLimitColor);
        upperColorPicker.setStyle("-fx-color-label-visible: false ;");
        lowerColorPicker.setStyle("-fx-color-label-visible: false ;");

        upperValueField.setText(limit.upperLimitOffset.toString());
        lowerValueField.setText(limit.lowerLimitOffset.toString());

        widgetBox = new JFXComboBox<>(
                widgetList.filtered(widget -> widget.typeID().equals(ValueWidget.WIDGET_ID)));
        Callback<ListView<Widget>, ListCell<Widget>> cellFactory = new Callback<javafx.scene.control.ListView<Widget>, ListCell<Widget>>() {
            @Override
            public ListCell<Widget> call(ListView<Widget> param) {
                return new ListCell<Widget>() {
                    @Override
                    protected void updateItem(Widget item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            try {
                                ValueWidget widget = ((ValueWidget) item);
                                String title = item.getConfig().getTitle().isEmpty()
                                        ? I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.list.nottitle")
                                        : item.getConfig().getTitle();

                                setText(String.format("[%d] '%s' | %.2f",
                                        item.getConfig().getUuid(),
                                        title,
                                        widget.getDisplayedSampleProperty().getValue()));
                            } catch (Exception ex) {
                                logger.error(ex);
                                setText(item.toString());
                            }
                        }
                    }
                };
            }
        };
        widgetBox.setButtonCell(cellFactory.call(null));
        widgetBox.setCellFactory(cellFactory);
        if (limit.limitWidget > 0) {
            Optional<Widget> widget = widgetList.stream()
                    .filter(widget1 -> widget1.getConfig().getUuid() == limit.limitWidget)
                    .findFirst();
            if (widget.isPresent()) {
                widgetBox.getSelectionModel().select(widget.get());
            }
        }


        enableUpperBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            upperValueField.setDisable(!newValue);
            upperColorPicker.setDisable(!newValue);
            limit.hasUpperLimit = newValue;
        });

        enableLowerBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            lowerValueField.setDisable(!newValue);
            lowerColorPicker.setDisable(!newValue);
            limit.hasLowerLimit = newValue;
        });


        upperColorPicker.setOnAction(event -> {
            limit.upperLimitColor = upperColorPicker.getValue();
        });


        lowerColorPicker.setOnAction(event -> {
            limit.lowerLimitColor = lowerColorPicker.getValue();
        });

        upperValueField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    limit.upperLimitOffset = Double.parseDouble(newValue.replaceAll(",", "."));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });

        lowerValueField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    limit.lowerLimitOffset = Double.parseDouble(newValue.replaceAll(",", "."));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });

        widgetBox.setOnAction(event -> {
            if (widgetBox.getSelectionModel().getSelectedItem() != null) {
                limit.limitWidget = widgetBox.getSelectionModel().getSelectedItem().getConfig().getUuid();
            }
        });


    }

}
