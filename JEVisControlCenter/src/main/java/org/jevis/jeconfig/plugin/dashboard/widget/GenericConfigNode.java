package org.jevis.jeconfig.plugin.dashboard.widget;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.DoubleValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.config2.ConfigTab;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;

public class GenericConfigNode extends Tab implements ConfigTab {

    private final Label nameLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.title"));
    private final Label tooltipLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.tooltip"));
    private final Label bgColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.color"));
    private final Label fColorLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontcolor"));
    private final Label idLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.id"));
    private final Label idField = new Label("");
    private final Label shadowLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.shadow"));
    private final Label yPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.ypos"));
    private final Label xPosLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.xpos"));
    private final Label timeFrameLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.forcedtime"));
    private final Label fontSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontsize"));
    private final Label borderSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.bordersize"));


    private final JFXTextField nameField = new JFXTextField();
    private final JFXTextField tooltipField = new JFXTextField();
    private final JFXTextField yPosField = new JFXTextField();
    private final JFXTextField xPosField = new JFXTextField();
    private final CheckBox showShadowField = new CheckBox();
    private final Spinner<Integer> fontSizeSpinner = new Spinner<Integer>(5, 50, 12);
    private final Spinner<Integer> borderSizeSpinner = new Spinner<Integer>(0, 20, 0);
    private final ColorPickerAdv bgColorPicker = new ColorPickerAdv();
    private final ColorPickerAdv fColorPicker = new ColorPickerAdv();
    private final TimeFactoryBox timeFrameBox;
    private final Widget widget;
    private final DataModelDataHandler dataModelDataHandler;

    public GenericConfigNode(JEVisDataSource ds, Widget widget, DataModelDataHandler dataModelDataHandler) {
        super(I18n.getInstance().getString("plugin.dashboard.edit.general.tab"));
        this.widget = widget;
        this.dataModelDataHandler = dataModelDataHandler;

        ObservableList<TimeFrameFactory> timeFrames = FXCollections.observableArrayList(widget.getControl().getAllTimeFrames().getAll());
        timeFrameBox = new TimeFactoryBox(true);
        timeFrameBox.setPrefWidth(200);
        timeFrameBox.setMinWidth(200);
        timeFrameBox.getItems().addAll(timeFrames);


        bgColorPicker.setStyle("-fx-color-label-visible: false ;");
        fColorPicker.setStyle("-fx-color-label-visible: false ;");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        gridPane.addColumn(0, idLabel, nameLabel, tooltipLabel, bgColorLabel, fColorLabel, yPosLabel, xPosLabel,
                shadowLabel, borderSizeLabel, fontSizeLabel, timeFrameLabel);
        gridPane.addColumn(1, idField, nameField, tooltipField, bgColorPicker, fColorPicker, yPosField, xPosField,
                showShadowField, borderSizeSpinner, fontSizeSpinner, timeFrameBox);

        setContent(gridPane);

        updateData();

    }

    public void updateData() {
        /** ----------------------------------- set Values ---------------------------------------------**/
        nameField.setText(widget.getConfig().getTitle());
        tooltipField.setText(widget.getConfig().getTooltip());
        idField.setText(widget.getConfig().getUuid() + "");
        bgColorPicker.setValue(widget.getConfig().getBackgroundColor());
        fColorPicker.setValue(widget.getConfig().getFontColor());
        yPosField.setText(widget.getConfig().getyPosition() + "");
        xPosField.setText(widget.getConfig().getxPosition() + "");

        showShadowField.setSelected(widget.getConfig().getShowShadow());


        if (dataModelDataHandler == null) {
            timeFrameBox.setDisable(true);
        } else if (dataModelDataHandler.isForcedInterval()) {
            timeFrameBox.setDisable(false);
            timeFrameBox.selectValue(dataModelDataHandler.getTimeFrameFactory());
        }

        DoubleValidator validator = new DoubleValidator();
        yPosField.getValidators().add(validator);

        fontSizeSpinner.getValueFactory().setValue(widget.getConfig().getFontSize().intValue());
        borderSizeSpinner.getValueFactory().setValue((int) widget.getConfig().getBorderSize().getTop());
    }

    @Override
    public void commitChanges() {
        try {
            widget.getConfig().setShowShadow(showShadowField.isSelected());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setTitle(nameField.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setTooltip(tooltipField.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setxPosition(Double.parseDouble(xPosField.getText()));
            widget.getConfig().setyPosition(Double.parseDouble(yPosField.getText()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setBackgroundColor(bgColorPicker.getValue());
            widget.getConfig().setFontColor(fColorPicker.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setBorderSize(new BorderWidths(borderSizeSpinner.getValue()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setFontSize(fontSizeSpinner.getValueFactory().getValue().doubleValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        try {
            TimeFrameFactory timeFrameFactory = timeFrameBox.getValue();
            if (widget instanceof DataModelWidget) {
                DataModelDataHandler dataModelDataHandler = ((DataModelWidget) widget).getDataHandler();

                if (timeFrameBox.isOffValue()) {
                    dataModelDataHandler.setForcedInterval(false);
                } else {
                    dataModelDataHandler.setForcedPeriod(timeFrameFactory);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
