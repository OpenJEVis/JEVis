package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFactoryBox;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widgets;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.ScreenSize;


public class WidgetNavigator {
    private double iconSize = 20;
    final ImageView lockIcon = JEConfig.getImage("eye_visible.png", this.iconSize, this.iconSize);
    final ImageView unlockIcon = JEConfig.getImage("eye_hidden.png", this.iconSize, this.iconSize);


    private final DashboardControl control;
    private TableView<Widget> table;

    public WidgetNavigator(DashboardControl control) {
        this.control = control;
    }


    public void show() {
        final Stage stage = new Stage(StageStyle.UTILITY);


        stage.setTitle(I18n.getInstance().getString("attribute.editor.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        AnchorPane root = new AnchorPane();

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

        final Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setWidth(ScreenSize.fitScreenWidth(1100));
        stage.setHeight(800);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);

        Button finishButton = new Button(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"));
        finishButton.setDefaultButton(true);
        finishButton.setOnAction(event -> {
            stage.hide();
        });
        stage.setOnHiding(event -> {
            this.control.enableHightlightGlow(false);
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
        Label backgroundLabel = new Label(I18n.getInstance().getString("dashboard.navigator.background"));
        Label timeLabel = new Label(I18n.getInstance().getString("dashboard.navigator.timeframe"));


        TextField nameField = new TextField();
        TextField widthField = new TextField();
        TextField heightField = new TextField();
        TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
        ObservableList<TimeFrameFactory> timeFrames = FXCollections.observableArrayList(control.getAllTimeFrames().getAll());
        timeFactoryBox.getItems().addAll(timeFrames);
        timeFactoryBox.selectValue(control.getActiveTimeFrame());

        Button backgroundButton = new Button("", JEConfig.getImage("if_32_171485.png", this.iconSize, this.iconSize));

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
        gridPane.add(nameLabel, 0, 0);
        gridPane.add(sizeLabel, 0, 1);
        gridPane.add(backgroundLabel, 0, 2);
        gridPane.add(nameField, 1, 0);
        gridPane.add(sizeBox, 1, 1);
        gridPane.add(backgroundButton, 1, 2);

        gridPane.add(timeLabel, 0, 3);
        gridPane.add(timeFactoryBox, 1, 3);


        try {
            nameField.setText(this.control.getActiveDashboard().getTitle());
            widthField.setText(this.control.getActiveDashboard().getSize().getWidth() + "");
            heightField.setText(this.control.getActiveDashboard().getSize().getHeight() + "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        widthField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.control.setDashboardSize(Double.parseDouble(newValue), Double.parseDouble(widthField.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.control.setDashboardSize(Double.parseDouble(newValue), Double.parseDouble(heightField.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
//


//        widthField.setOnAction(event -> {
//            this.control.setDashboardSize(Double.parseDouble(widthField.getText()), Double.parseDouble(heightField.getText()));
//        });
//        heightField.setOnAction(event -> {
//            this.control.setDashboardSize(Double.parseDouble(widthField.getText()), Double.parseDouble(heightField.getText()));
//        });

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.control.getActiveDashboard().setName(newValue);
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

    private ToolBar buildToolbar() {
        ToolBar toolBar = new ToolBar();


        ToggleButton highlightButton = new ToggleButton("", this.unlockIcon);
//        highlightButton.selectedProperty().bindBidirectional(this.control.highlightProperty);
        highlightButton.setOnAction(event -> {
            control.enableHightlightGlow(highlightButton.isSelected());
        });

        highlightButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highlightButton.setGraphic(this.lockIcon);
            } else {
                highlightButton.setGraphic(this.unlockIcon);
            }
        });
        highlightButton.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.highlight")));

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> {
            control.removeAllWidgets(table.getSelectionModel().getSelectedItems());
            table.refresh();
        });


        NewWidgetSelector widgetSelector = new NewWidgetSelector(Widgets.getAvabableWidgets(control, new WidgetPojo()));
        widgetSelector.getSelectedWidgetProperty().addListener((observable, oldValue, newValue) -> {
            Widget newWidget = widgetSelector.getSelectedWidget();
            control.addWidget(newWidget);
//            newWidget.updateConfig();
            table.getSelectionModel().select(newWidget);
            table.scrollTo(newWidget);
            this.control.requestViewUpdate(newWidget);
        });
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        toolBar.getItems().addAll(highlightButton, delete, sep1, widgetSelector, sep2);

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
