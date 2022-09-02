package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(TableWidget.class);
    public static String WIDGET_ID = "Table";
    private NumberFormat nf = NumberFormat.getInstance();
    //private DataModelDataHandler sampleHandler;
    private TableView<TableData> table;
    private Interval lastInterval = null;
    private Boolean customWorkday = true;


    public TableWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public void debug() {
        sampleHandler.debug();
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.tablewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 20, control.getActiveDashboard().xGridInterval * 20));

        return widgetPojo;
    }

    @Override
    public void updateData(Interval interval) {
        logger.debug("Table.Update: {}", interval);

        this.lastInterval = interval;
        showAlertOverview(false, "");

        if (sampleHandler == null) {
            return;
        } else {
            showProgressIndicator(true);
        }

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        ObservableList<TableData> tableDatas = FXCollections.observableArrayList();
        List<String> alerts = new ArrayList();
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {

            try {
                chartDataModel.setCustomWorkDay(customWorkday);
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
                            this.nf.format(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, chartDataModel)),
                            chartDataModel.getUnitLabel()));


                } else {
                    tableDatas.add(new TableData(
                            chartDataModel.getObject().getName(),
                            "n.a.",
                            chartDataModel.getUnitLabel()));
                    alerts.add(chartDataModel.getObject().getName() + " missing date in selected interval");
                }
            } catch (Exception ex) {
                logger.error(ex);
//                ex.printStackTrace();
                tableDatas.add(new TableData("", "", ""));
            }

            if (!alerts.isEmpty()) {
                String alertMessage = "";
                for (String message : alerts) {
                    alertMessage += message + "\n";
                }

                showAlertOverview(true, alertMessage);
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
    public boolean isStatic() {
        return false;
    }



    @Override
    public List<DateTime> getMaxTimeStamps() {
        if (sampleHandler != null) {
            return sampleHandler.getMaxTimeStamps();
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public void init() {
        nf = NumberFormat.getInstance();
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), WIDGET_ID);
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
    public DataModelDataHandler getDataHandler() {
        return this.sampleHandler;
    }

    @Override
    public void setDataHandler(DataModelDataHandler dataHandler) {
        this.sampleHandler = dataHandler;
    }

    @Override
    public void setCustomWorkday(Boolean customWorkday) {
        this.customWorkday = customWorkday;
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
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
            } catch (Exception ex) {
                logger.error(ex);
            }
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
