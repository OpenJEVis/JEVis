package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.List;

public class TableWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TableWidget.class);
    public static String WIDGET_ID = "Table";
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private TableView<ChartDataModel> table;


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

        Platform.runLater(() -> {
            table.getItems().clear();
            table.setItems(FXCollections.observableArrayList(sampleHandler.getDataModel()));

        });


    }


    @Override
    public void init() {

        sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        sampleHandler.setMultiSelect(false);

        table = new TableView<ChartDataModel>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChartDataModel, String> nameCol = new TableColumn<ChartDataModel, String>("Name");
        nameCol.setMinWidth(225);
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ChartDataModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ChartDataModel, String> p) {
                try {
                    ChartDataModel chartDataModel = p.getValue();
                    if (chartDataModel.getObject() != null) {
                        return new SimpleStringProperty(chartDataModel.getObject().getName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return new SimpleStringProperty("");
            }
        });

        TableColumn<ChartDataModel, String> valueCol = new TableColumn<ChartDataModel, String>("Wert");
        valueCol.setPrefWidth(150);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        valueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ChartDataModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ChartDataModel, String> p) {
                try {

                    ChartDataModel chartDataModel = p.getValue();
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
                        JEVisSample sample = results.get(results.size() - 1);
                        return new SimpleStringProperty(nf.format(sample.getValueAsDouble()));

                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
                return new SimpleStringProperty("");
            }
        });


        TableColumn<ChartDataModel, String> unitCol = new TableColumn<ChartDataModel, String>("Einheit");
        unitCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ChartDataModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ChartDataModel, String> p) {
                try {
                    ChartDataModel chartDataModel = p.getValue();
                    if (chartDataModel.getUnitLabel() != null) {
                        return new SimpleStringProperty(chartDataModel.getUnitLabel());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return new SimpleStringProperty("");
            }
        });


        table.getColumns().setAll(nameCol, valueCol, unitCol);

        ChartDataModel emtyModel = new ChartDataModel(getDataSource());
        table.setItems(FXCollections.observableArrayList(emtyModel));
        setGraphic(table);


    }

//    private void autoreiszeColumn(TableView tableView, TableColumn tableColumn) {
//        try {
//
////            Class clazz = column.getClass();
//            Method columnToFitMethod = TableColumnHeader.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
//            columnToFitMethod.setAccessible(true);
//
//
//            columnToFitMethod.invoke(tableView.getSkin(), tableColumn, -1);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    private ReadOnlyObjectWrapper buildValueColumn() {
        return new ReadOnlyObjectWrapper<String>() {
            @Override
            public ReadOnlyObjectProperty<String> getReadOnlyProperty() {


                return super.getReadOnlyProperty();
            }
        };
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }
}
