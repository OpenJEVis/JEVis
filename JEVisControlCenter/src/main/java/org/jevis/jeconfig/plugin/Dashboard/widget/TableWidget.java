package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;

public class TableWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TableWidget.class);
    public static String WIDGET_ID = "Table";
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private TableView<TableData> table;


    public TableWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }


    public TableWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }

    @Override
    public void update(Interval interval) {
        logger.debug("Table.Update: {}", interval);

        sampleHandler.setInterval(interval);
        sampleHandler.update();

        //if config changed
        if (config.hasChanged("")) {
//            Platform.runLater(() -> {
//                Background bgColor = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
//                label.setBackground(bgColor);
//                label.setTextFill(config.fontColor.getValue());
//
//                label.setContentDisplay(ContentDisplay.CENTER);
//            });

            nf.setMinimumFractionDigits(config.decimals.getValue());
            nf.setMaximumFractionDigits(config.decimals.getValue());
        }

        ObservableList<TableData> tableDatas = FXCollections.observableArrayList();
        sampleHandler.getDataModel().forEach(chartDataModel -> {
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
                            nf.format(DataModelDataHandler.getTotal(results)),
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
            table.getItems().clear();
            table.setItems(tableDatas);

        });


    }

    @Override
    public void init() {

        sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        sampleHandler.setMultiSelect(false);

        table = new TableView<TableData>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String name = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.name");
        TableColumn<TableData, String> nameCol = new TableColumn<TableData, String>(name);
        nameCol.setMinWidth(225);
        nameCol.setCellValueFactory(new PropertyValueFactory<>(name));

        String value = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value");
        TableColumn<TableData, String> valueCol = new TableColumn<TableData, String>(value);
        valueCol.setPrefWidth(150);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        valueCol.setCellValueFactory(new PropertyValueFactory<>(value));

        String unit = I18n.getInstance().getString("plugin.dashboard.tablewidget.column.unit");
        TableColumn<TableData, String> unitCol = new TableColumn<TableData, String>(unit);
        unitCol.setCellValueFactory(new PropertyValueFactory<>(unit));

        table.getColumns().setAll(nameCol, valueCol, unitCol);

        TableData dummy = new TableData("", "", "");

        table.setItems(FXCollections.observableArrayList(dummy));
        setGraphic(table);


    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", previewSize.getHeight(), previewSize.getWidth());
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
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }


}
