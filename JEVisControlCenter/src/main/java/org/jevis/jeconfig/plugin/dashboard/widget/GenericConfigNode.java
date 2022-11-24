package org.jevis.jeconfig.plugin.dashboard.widget;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.DoubleValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.config2.ConfigTab;
import org.jevis.jeconfig.plugin.dashboard.controls.FontPostureBox;
import org.jevis.jeconfig.plugin.dashboard.controls.FontWeightBox;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;

public class GenericConfigNode extends Tab implements ConfigTab {
    private static final Logger logger = LogManager.getLogger(GenericConfigNode.class);
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
    private final Label fontWeightLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontweight"));
    private final Label fontPostureLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.fontposture"));
    private final Label borderSizeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.bordersize"));
    private final Label alignmentLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.alignment"));
    private final Label precisionLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.precision"));
    private final Label showValueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.edit.general.showvalue"));


    private final JFXTextField nameField = new JFXTextField();
    private final JFXTextField tooltipField = new JFXTextField();
    private final JFXTextField yPosField = new JFXTextField();
    private final JFXTextField xPosField = new JFXTextField();
    private final JFXCheckBox showShadowField = new JFXCheckBox();
    private final JFXCheckBox showValueField = new JFXCheckBox();
    private final Spinner<Integer> fontSizeSpinner = new Spinner<Integer>(5, 50, 12);
    private final FontWeightBox fontWeightBox = new FontWeightBox();
    private final FontPostureBox fontPostureBox = new FontPostureBox();
    private final JFXCheckBox fontUnderlined = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.controls.underline"));
    private final Spinner<Integer> borderSizeSpinner = new Spinner<Integer>(0, 20, 0);
    private final Spinner<Integer> precisionSpinner = new Spinner<Integer>(0, 20, 2);
    private final ColorPickerAdv bgColorPicker = new ColorPickerAdv();
    private final ColorPickerAdv fColorPicker = new ColorPickerAdv();
    private final TimeFactoryBox timeFrameBox;
    private final Widget widget;
    private final DataModelDataHandler dataModelDataHandler;
    private final JFXComboBox<Pos> alignmentBox;

    public GenericConfigNode(JEVisDataSource ds, Widget widget, DataModelDataHandler dataModelDataHandler) {
        super(I18n.getInstance().getString("plugin.dashboard.edit.general.tab"));
        this.widget = widget;
        this.dataModelDataHandler = dataModelDataHandler;

        ObservableList<TimeFrame> timeFrames = FXCollections.observableArrayList(widget.getControl().getAllTimeFrames().getAll());
        timeFrameBox = new TimeFactoryBox(true);
        timeFrameBox.setPrefWidth(200);
        timeFrameBox.setMinWidth(200);
        timeFrameBox.getItems().addAll(timeFrames);

        alignmentBox = new JFXComboBox<>(FXCollections.observableArrayList(Pos.TOP_LEFT, Pos.TOP_CENTER, Pos.TOP_RIGHT, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_CENTER, Pos.BOTTOM_RIGHT));
        alignmentBox.setPrefWidth(200);
        alignmentBox.setMinWidth(200);

        Callback<ListView<Pos>, ListCell<Pos>> cellFactory = new Callback<ListView<Pos>, ListCell<Pos>>() {
            @Override
            public ListCell<Pos> call(ListView<Pos> param) {
                final ListCell<Pos> cell = new ListCell<Pos>() {

                    @Override
                    protected void updateItem(Pos item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            switch (item) {
                                case CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.center"));
                                    break;
                                case CENTER_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.centerleft"));
                                    break;
                                case CENTER_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.centerright"));
                                    break;
                                case BOTTOM_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomright"));
                                    break;
                                case BOTTOM_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomleft"));
                                    break;
                                case BOTTOM_CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.bottomcenter"));
                                    break;
                                /**
                                 case BASELINE_LEFT:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 case BASELINE_RIGHT:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 case BASELINE_CENTER:
                                 setText(I18n.getInstance().getString("javafx.pos.center"));
                                 break;
                                 **/
                                case TOP_LEFT:
                                    setText(I18n.getInstance().getString("javafx.pos.topleft"));
                                    break;
                                case TOP_RIGHT:
                                    setText(I18n.getInstance().getString("javafx.pos.topright"));
                                    break;
                                case TOP_CENTER:
                                    setText(I18n.getInstance().getString("javafx.pos.topcenter"));
                                    break;
                                default:
                                    setText(item.toString());


                            }


                        } else {
                            setText(null);
                        }
                    }
                };

                return cell;
            }
        };

        alignmentBox.setCellFactory(cellFactory);
        alignmentBox.setButtonCell(cellFactory.call(null));
        alignmentBox.getSelectionModel().select(Pos.CENTER);

        bgColorPicker.setStyle("-fx-color-label-visible: false ;");
        fColorPicker.setStyle("-fx-color-label-visible: false ;");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        fontSizeSpinner.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                fontSizeSpinner.getValueFactory().setValue(fontSizeSpinner.valueProperty().getValue() + 1);
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                fontSizeSpinner.getValueFactory().setValue(fontSizeSpinner.valueProperty().getValue() - 1);
                event.consume();
            }
        });
        borderSizeSpinner.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                borderSizeSpinner.getValueFactory().setValue(borderSizeSpinner.valueProperty().getValue() + 1);
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                borderSizeSpinner.getValueFactory().setValue(borderSizeSpinner.valueProperty().getValue() - 1);
                event.consume();
            }
        });

        precisionSpinner.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                precisionSpinner.getValueFactory().setValue(precisionSpinner.valueProperty().getValue() + 1);
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                precisionSpinner.getValueFactory().setValue(precisionSpinner.valueProperty().getValue() - 1);
                event.consume();
            }
        });

        gridPane.addColumn(0, idLabel, nameLabel, tooltipLabel, bgColorLabel, fColorLabel, yPosLabel, xPosLabel,
                shadowLabel, showValueLabel, borderSizeLabel, precisionLabel, fontSizeLabel, fontWeightLabel, fontPostureLabel, fontUnderlined, alignmentLabel, timeFrameLabel);
        gridPane.addColumn(1, idField, nameField, tooltipField, bgColorPicker, fColorPicker, yPosField, xPosField,
                showShadowField, showValueField, borderSizeSpinner, precisionSpinner, fontSizeSpinner, fontWeightBox, fontPostureBox, new Region(), alignmentBox, timeFrameBox);

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
        showValueField.setSelected(widget.getConfig().getShowValue());
        //showShadowField.setSelected(widget.getConfig().getShowShadow());


        if (dataModelDataHandler == null) {
            timeFrameBox.setDisable(true);
        } else if (dataModelDataHandler.isForcedInterval()) {
            timeFrameBox.setDisable(false);
            timeFrameBox.selectValue(dataModelDataHandler.getTimeFrameFactory());
        }

        DoubleValidator validator = new DoubleValidator();
        yPosField.getValidators().add(validator);

        fontSizeSpinner.getValueFactory().setValue(widget.getConfig().getFontSize().intValue());
        fontWeightBox.getSelectionModel().select(widget.getConfig().getFontWeight());
        fontPostureBox.getSelectionModel().select(widget.getConfig().getFontPosture());
        fontUnderlined.selectedProperty().setValue(widget.getConfig().getFontUnderlined());
        borderSizeSpinner.getValueFactory().setValue((int) widget.getConfig().getBorderSize().getTop());
        alignmentBox.getSelectionModel().select(widget.getConfig().getTitlePosition());
        precisionSpinner.getValueFactory().setValue(widget.getConfig().getDecimals().intValue());
    }

    @Override
    public void commitChanges() {
        try {
            widget.getConfig().setShowShadow(showShadowField.isSelected());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setShowValue(showValueField.isSelected());
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
            widget.getConfig().setDecimals(precisionSpinner.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        try {
            widget.getConfig().setFontSize(fontSizeSpinner.getValueFactory().getValue().doubleValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setFontWeight(fontWeightBox.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setFontPosture(fontPostureBox.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            widget.getConfig().setFontUnderlined(fontUnderlined.isSelected());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            TimeFrame timeFrame = timeFrameBox.getValue();
            if (widget instanceof DataModelWidget) {
                DataModelDataHandler dataModelDataHandler = ((DataModelWidget) widget).getDataHandler();

                if (timeFrameBox.isOffValue()) {
                    dataModelDataHandler.setForcedInterval(false);
                } else {
                    dataModelDataHandler.setForcedPeriod(timeFrame);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            widget.getConfig().setTitlePosition(alignmentBox.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
