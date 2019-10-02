package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.tool.I18n;

public class LimitStaticPane extends GridPane {
    private static final Logger logger = LogManager.getLogger(LimitStaticPane.class);
    private Label upperVlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.upper"));
    private Label lowerVlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.valuelabel.lower"));
    private Label upperColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.upper"));
    private Label lowerColorlabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.color.lower"));
    private CheckBox enableUpperBox = new CheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.upper"));
    private CheckBox enableLowerBox = new CheckBox(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.enable.lower"));
    private TextField upperValueField = new TextField();
    private TextField lowerValueField = new TextField();
    private ColorPicker upperColorPicker = new ColorPicker();
    private ColorPicker lowerColorPicker = new ColorPicker();

    private final Limit limit;

    public LimitStaticPane(Limit limit) {
        this.limit = limit;
        initControls();

        setPadding(new Insets(10));
        setVgap(8);
        setHgap(8);

        add(enableUpperBox, 0, 1, 2, 1);
        addRow(2, upperVlabel, upperValueField);
        addRow(3, upperColorlabel, upperColorPicker);
        add(new Separator(Orientation.HORIZONTAL), 0, 4, 2, 1);
        add(enableLowerBox, 0, 5, 2, 1);
        addRow(6, lowerVlabel, lowerValueField);
        addRow(7, lowerColorlabel, lowerColorPicker);
    }


    private void initControls() {
        enableUpperBox.setSelected(limit.hasUpperLimit);
        enableLowerBox.setSelected(limit.hasLowerLimit);

        upperColorPicker.setStyle("-fx-color-label-visible: false ;");
        lowerColorPicker.setStyle("-fx-color-label-visible: false ;");
        upperColorPicker.setValue(limit.upperLimitColor);
        lowerColorPicker.setValue(limit.lowerLimitColor);

        upperValueField.setText(limit.upperLimit.toString());
        lowerValueField.setText(limit.lowerLimit.toString());

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
                    limit.upperLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });

        lowerValueField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    limit.lowerLimit = Double.parseDouble(newValue.replaceAll(",", "."));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });
    }
}
