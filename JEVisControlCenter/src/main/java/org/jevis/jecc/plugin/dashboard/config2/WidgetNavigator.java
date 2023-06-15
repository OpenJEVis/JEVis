package org.jevis.jecc.plugin.dashboard.config2;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.control.ColorPickerAdv;
import org.jevis.jecc.dialog.DialogHeader;
import org.jevis.jecc.plugin.dashboard.DashBoardToolbar;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.config.BackgroundMode;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jecc.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jecc.plugin.dashboard.widget.Widget;
import org.jevis.jecc.tool.Layouts;
import org.jevis.jecc.tool.ScreenSize;


public class WidgetNavigator {
    private final double iconSize = 16;
    final Region lockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_ON, this.iconSize, this.iconSize);
    final Region unlockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_OFF, this.iconSize, this.iconSize);
    final Region copyWidget = ControlCenter.getSVGImage(Icon.COPY, this.iconSize, this.iconSize);


    private final DashboardControl control;
    private TableView<Widget> table;

    public WidgetNavigator(DashboardControl control) {
        this.control = control;
    }


    public void show() {
        final Stage stage = new Stage(StageStyle.UTILITY);


        stage.setTitle(I18n.getInstance().getString("attribute.editor.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(ControlCenter.getStage());

        AnchorPane root = new AnchorPane();


        final Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);

        stage.setWidth(ScreenSize.fitScreenWidth(1100));
        stage.setHeight(800);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);

        Control table = buildTable();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(table);

        Node header = DialogHeader.getDialogHeader("if_table_gear_64761.png", I18n.getInstance().getString("dashboard.navigator.setting.title"));
        Node contentPane = buildGeneralSetting();
        BorderPane topBorderPane = new BorderPane();
        topBorderPane.setTop(header);
        topBorderPane.setCenter(contentPane);

        AnchorPane anchorPane = new AnchorPane(topBorderPane);
        Layouts.setAnchor(topBorderPane, 0);
        borderPane.setTop(anchorPane);


        Layouts.setAnchor(borderPane, 0d);
        root.getChildren().add(borderPane);

        Button finishButton = new Button(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"));
        finishButton.setDefaultButton(true);
        finishButton.setOnAction(event -> {
            stage.hide();
        });
        stage.setOnHiding(event -> {
            this.control.enableHighlightGlow(false);
        });


        HBox buttonBox = new HBox(18);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(finishButton);
        buttonBox.setPadding(new Insets(12));
        borderPane.setBottom(buttonBox);
        stage.show();
    }


    private Node buildGeneralSetting() {
        Label nameLabel = new Label(I18n.getInstance().getString("dashboard.navigator.namelabel"));
        Label sizeLabel = new Label(I18n.getInstance().getString("dashboard.navigator.sizelabel"));
        Label backgroundIconLabel = new Label(I18n.getInstance().getString("dashboard.navigator.background"));
        Label backgroundColorLabel = new Label(I18n.getInstance().getString("dashboard.navigator.backgroundColor"));
        Label timeLabel = new Label(I18n.getInstance().getString("dashboard.navigator.timeframe"));
        Label defaultZoomLabel = new Label(I18n.getInstance().getString("dashboard.navigator.defaultzoom"));

        Region spacer = new Region();
        spacer.setPrefWidth(20d);

        MFXTextField nameField = new MFXTextField();
        MFXTextField widthField = new MFXTextField();
        MFXTextField heightField = new MFXTextField();
        MFXComboBox<Double> listZoomLevel = DashBoardToolbar.buildZoomLevelListView();


        TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
        ObservableList<TimeFrame> timeFrames = FXCollections.observableArrayList(control.getAllTimeFrames().getAll());
        timeFactoryBox.getItems().addAll(timeFrames);
        timeFactoryBox.selectValue(control.getActiveDashboard().getTimeFrame());
        //timeFactoryBox.selectValue(control.getActiveTimeFrame());

        MFXButton backgroundButton = new MFXButton("", ControlCenter.getImage("if_32_171485.png", this.iconSize, this.iconSize));
        MFXButton removeBGIcon = new MFXButton("", ControlCenter.getImage("if_trash_(delete)_16x16_10030.gif", this.iconSize, this.iconSize));
        ColorPickerAdv pickerAdv = new ColorPickerAdv();
        pickerAdv.setValue(control.getActiveDashboard().getBackgroundColor());
        pickerAdv.setMinHeight(backgroundButton.getHeight());

        MFXComboBox<String> bhModeBox = buildBGMOdeBox();

        HBox imageBox = new HBox();
        imageBox.setSpacing(5);
        imageBox.getChildren().addAll(backgroundButton, removeBGIcon, bhModeBox);

        listZoomLevel.setMaxWidth(Double.MAX_VALUE);
        defaultZoomLabel.setMaxWidth(Double.MAX_VALUE);

        widthField.setPrefWidth(75d);
        heightField.setPrefWidth(75d);
        HBox sizeBox = new HBox(8d);
        Label xLabel = new Label("x");
        sizeBox.getChildren().setAll(widthField, heightField);
        HBox.setMargin(xLabel, new Insets(3));
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(8d));
        gridPane.setHgap(8);
        gridPane.setVgap(8);

        gridPane.addColumn(0, nameLabel, backgroundColorLabel, backgroundIconLabel);
        gridPane.addColumn(1, nameField, pickerAdv, imageBox);
        gridPane.add(new Separator(Orientation.VERTICAL), 2, 0, 1, 3);
        gridPane.addColumn(4, sizeLabel, defaultZoomLabel, timeLabel);
        gridPane.addColumn(5, sizeBox, listZoomLevel, timeFactoryBox);
        gridPane.addColumn(6, new Region());

        try {
            nameField.setText(this.control.getActiveDashboard().getTitle());
            widthField.setText(this.control.getActiveDashboard().getSize().getWidth() + "");
            heightField.setText(this.control.getActiveDashboard().getSize().getHeight() + "");
            listZoomLevel.setValue(this.control.getActiveDashboard().getZoomFactor());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        widthField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.control.setDashboardSize(Double.parseDouble(newValue), Double.parseDouble(heightField.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.control.setDashboardSize(Double.parseDouble(widthField.getText()), Double.parseDouble(newValue));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        bhModeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            control.getActiveDashboard().setBackgroundMode(newValue);
            control.updateBackground();
        });

        pickerAdv.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            control.getActiveDashboard().setBackgroundColor(newValue);
            control.updateBackground();
        });

//
        backgroundButton.setOnAction(event -> {
            control.startWallpaperSelection();
        });


        listZoomLevel.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                control.setDefaultZoom(newValue);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            //this.control.getActiveDashboard().setName(newValue);
            this.control.getActiveDashboard().setTitle(newValue);
        });

        timeFactoryBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (timeFactoryBox.isOffValue()) {
                this.control.getActiveDashboard().setTimeFrame(null);
            } else {
                this.control.getActiveDashboard().setTimeFrame(newValue);
            }
        });

        backgroundButton.setOnAction(event -> {
            control.startWallpaperSelection();
        });

        return gridPane;
    }


    private MFXComboBox<String> buildBGMOdeBox() {
        ObservableList<String> modeList = FXCollections.observableArrayList();
        modeList.addAll(BackgroundMode.defaultMode, BackgroundMode.repeat, BackgroundMode.stretch);
        MFXComboBox<String> comboBox = new MFXComboBox<>(modeList);
        comboBox.setFloatMode(FloatMode.DISABLED);
        comboBox.setValue(control.getActiveDashboard().backgroundMode);


        Callback<ListView<String>, ListCell<String>> factory = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        String text = item;

                        if (item == BackgroundMode.defaultMode) {
                            text = I18n.getInstance().getString("dashboard.navigator.bgmode.default");
                        } else if (item == BackgroundMode.repeat) {
                            text = I18n.getInstance().getString("dashboard.navigator.bgmode.repeat");
                        } else if (item == BackgroundMode.stretch) {
                            text = I18n.getInstance().getString("dashboard.navigator.bgmode.stretch");
                        }

                        setText(text);

                    }
                };
            }
        };

        //TODO JFX17
        comboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                String text = object;

                if (object.equals(BackgroundMode.defaultMode)) {
                    text = I18n.getInstance().getString("dashboard.navigator.bgmode.default");
                } else if (object.equals(BackgroundMode.repeat)) {
                    text = I18n.getInstance().getString("dashboard.navigator.bgmode.repeat");
                } else if (object.equals(BackgroundMode.stretch)) {
                    text = I18n.getInstance().getString("dashboard.navigator.bgmode.stretch");
                }

                return (text);
            }

            @Override
            public String fromString(String string) {
                return comboBox.getItems().get(comboBox.getSelectedIndex());
            }
        });

        return comboBox;

    }

    private ToolBar buildToolbar() {
        ToolBar toolBar = new ToolBar();


        ToggleButton highlightButton = new ToggleButton("", this.unlockIcon);

//        highlightButton.selectedProperty().bindBidirectional(this.control.highlightProperty);
        highlightButton.setOnAction(event -> {
            control.enableHighlightGlow(highlightButton.isSelected());
        });

        highlightButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highlightButton.setGraphic(this.lockIcon);
            } else {
                highlightButton.setGraphic(this.unlockIcon);
            }
        });
        highlightButton.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.highlight")));

        ToggleButton delete = new ToggleButton("", ControlCenter.getSVGImage(Icon.DELETE, this.iconSize, this.iconSize));
        delete.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.delete")));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> {
            control.removeAllWidgets(table.getSelectionModel().getSelectedItems());
            table.refresh();
        });

        Button copyButton = new Button("", this.copyWidget);
        copyButton.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.copy")));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(copyButton);
        copyButton.setOnAction(event -> {
            try {
                Widget newWidget = table.getSelectionModel().getSelectedItem().clone();
                newWidget.getConfig().setUuid(control.getNextFreeUUID());
                newWidget.getConfig().setTitle(newWidget.getConfig().getTitle() + " " + I18n.getInstance().getString("dashboard.navigator.copy.name"));
                double newXPos = newWidget.getConfig().getxPosition() + newWidget.getConfig().getSize().getWidth() + 50;
                if (newXPos > control.getDashboardPane().getWidth()) {
                    newWidget.getConfig().setxPosition(newWidget.getConfig().getxPosition() + 50);
                } else {
                    newWidget.getConfig().setxPosition(newXPos);
                }

                control.addWidget(newWidget);
                newWidget.setEditable(true);
                table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
                table.scrollTo(newWidget);
                this.control.requestViewUpdate(newWidget);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        NewWidgetSelector widgetSelector = new NewWidgetSelector(control);
        widgetSelector.getSelectedWidgetProperty().addListener((observable, oldValue, newValue) -> {
            Widget newWidget = widgetSelector.getSelectedWidget();
            //newWidget.getConfig().setUuid(control.getNextFreeUUID());
            control.addWidget(newWidget);
            newWidget.setEditable(true);
            table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
            table.scrollTo(newWidget);
            this.control.requestViewUpdate(newWidget);
        });
        Separator sep1 = new Separator();
        toolBar.getItems().addAll(highlightButton, delete, sep1, widgetSelector, copyButton);

        return toolBar;
    }

    private Control buildTable() {
//        gridPane.setStyle("-fx-background-color: orange;");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

//        List<Widget> widgetList = this.control.getWidgets();
//        System.out.println("Widgets: " + widgetList.size());
        WidgetColumnFactory widgetColumnFactory = new WidgetColumnFactory(this.control);
        table = widgetColumnFactory.buildTable(this.control.getWidgetList());

//        table.getItems().addListener((ListChangeListener<Widget>) (c -> {
//            c.next();
//            final int size = table.getItems().size();
//            if (size > 0) {
//                table.scrollTo(size - 1);
//            }
//        }));

//        VBox vBox = new VBox(, table);
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(buildToolbar());
        borderPane.setCenter(table);

        scrollPane.setContent(borderPane);
//        table.getItems().setAll(this.control.getWidgets());

        return scrollPane;

    }


}
