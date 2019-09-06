package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class TableWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TableWidget.class);
    public static String WIDGET_ID = "Table";
    private static Button testB = new Button();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private TableView<TableData> table;
    private Interval lastInterval = null;


    public TableWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public TableWidget(DashboardControl control) {
        super(control);
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.tablewidget.newname"));
        widgetPojo.setType(typeID());

        return widgetPojo;
    }

    @Override
    public void updateData(Interval interval) {
        logger.debug("Table.Update: {}", interval);
        showProgressIndicator(true);
        this.lastInterval = interval;

        if (this.sampleHandler == null) {
            return;
        }

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        ObservableList<TableData> tableDatas = FXCollections.observableArrayList();
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                List<JEVisSample> results;
                if (chartDataModel.getEnPI()) {
                    CalcJobFactory calcJobCreator = new CalcJobFactory();

                    CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(
                            new SampleHandler(), chartDataModel.getObject().getDataSource(),
                            chartDataModel.getCalculationObject(), chartDataModel.getSelectedStart(),
                            chartDataModel.getSelectedEnd(), true);

                    results = calcJob.getResults();

                } else {
                    results = chartDataModel.getSamples();
                }

                if (!results.isEmpty()) {


                    tableDatas.add(new TableData(
                            chartDataModel.getObject().getName(),
                            this.nf.format(DataModelDataHandler.getTotal(results)),
                            chartDataModel.getUnitLabel()));


                } else {
                    tableDatas.add(new TableData(
                            chartDataModel.getObject().getName(),
                            "n.a.",
                            chartDataModel.getUnitLabel()));
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
                tableDatas.add(new TableData("", "", ""));
            }
        });

        Platform.runLater(() -> {
            this.table.getItems().clear();
            this.table.setItems(tableDatas);
            showProgressIndicator(false);
        });


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());
        Platform.runLater(() -> {
            table.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        });


    }

    @Override
    public void init() {
        nf = NumberFormat.getInstance();
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(false);

        this.table = new TableView<>();
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPickOnBounds(false);

        String name = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.name");
        TableColumn<TableData, String> nameCol = new TableColumn<TableData, String>(name);
        nameCol.setMinWidth(225);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        String value = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value");
        TableColumn<TableData, String> valueCol = new TableColumn<TableData, String>(value);
        valueCol.setPrefWidth(150);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));


        String unit = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.unit");
        TableColumn<TableData, String> unitCol = new TableColumn<TableData, String>(unit);
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        this.table.getColumns().setAll(nameCol, valueCol, unitCol);

        TableData dummy = new TableData("", "", "");

        this.table.setItems(FXCollections.observableArrayList(dummy));
        setGraphic(this.table);


    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ObjectNode toNode() {
        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/TableWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    @Override
    public void openConfig() {

        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(null);
        widgetConfigDialog.addDataModel(this.sampleHandler);
        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println("Update data config");
            widgetConfigDialog.updateDataModel();
            updateData(this.lastInterval);
        }
    }

    /**
     * Pojo container for the table data
     */
    public class TableData {
        String name = "";
        String value = "";
        String unit = "";

        public TableData(String name, String value, String unit) {
            this.name = name;
            this.value = value;
            this.unit = unit;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUnit() {
            return this.unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }


}
