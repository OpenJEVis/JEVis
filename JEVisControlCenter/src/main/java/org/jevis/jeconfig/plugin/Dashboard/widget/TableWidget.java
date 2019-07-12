package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.Dashboard.config.DataModelNode;
import org.jevis.jeconfig.plugin.Dashboard.config.DataPointNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.Dashboard.wizzard.ExampleConverter;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;

public class TableWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TableWidget.class);
    public static String WIDGET_ID = "Table";
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private TableView<TableData> table;


    public TableWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void updateData(Interval interval) {
        logger.debug("Table.Update: {}", interval);

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());


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

        });


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }

    @Override
    public void init() {

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(false);

        this.table = new TableView<TableData>();
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TableData, String> nameCol = new TableColumn<TableData, String>("Name");
        nameCol.setMinWidth(225);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));


        TableColumn<TableData, String> valueCol = new TableColumn<TableData, String>("Wert");
        valueCol.setPrefWidth(150);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));


        TableColumn<TableData, String> unitCol = new TableColumn<TableData, String>("Einheit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        this.table.getColumns().setAll(nameCol, valueCol, unitCol);

        TableData dummy = new TableData("", "", "");

        this.table.setItems(FXCollections.observableArrayList(dummy));
        setGraphic(this.table);

        try {
            ObjectMapper mapper = new ObjectMapper();

            DataModelNode dataModelNode = mapper.treeToValue(this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), DataModelNode.class);
//            System.out.println("Json: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataModelNode));

            boolean isStickstoff = false;
            for (DataPointNode dataPointNode : dataModelNode.getData()) {
                if (dataPointNode.getObjectID().equals(1076)) {
                    isStickstoff = true;
                }
            }

            if (!isStickstoff) {
                ExampleConverter exampleConverter = new ExampleConverter();
                exampleConverter.sampleHandlerToValue(dataModelNode);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
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
