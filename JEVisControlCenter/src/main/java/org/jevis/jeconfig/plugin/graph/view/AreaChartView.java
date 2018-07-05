/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.application.jevistree.plugin.TableEntry;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

/**
 * @author broder
 */
public class AreaChartView implements Observer {

    private final GraphDataModel dataModel;
    private AreaChart<Number, Number> areaChart;
    private VBox vbox;
    private Region areaChartRegion;
    private final TableView table;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();

    public AreaChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;
        dataModel.addObserver(this);

        table = new TableView();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
//        table.setFixedCellSize(25);
//        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(30));
        TableColumn name = new TableColumn(I18n.getInstance().getString("plugin.graph.table.name"));
        name.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));

//        TableColumn colorCol = new TableColumn("Color333");
//        colorCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("color"));
        TableColumn colorCol = buildColorColumn(I18n.getInstance().getString("plugin.graph.table.color"));

        TableColumn value = new TableColumn(I18n.getInstance().getString("plugin.graph.table.value"));
        value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));

        TableColumn dateCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.date"));
        dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, Color>("date"));

        TableColumn minCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.min"));
        minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("min"));

        TableColumn maxCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.max"));
        maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("max"));

        TableColumn avgCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.avg"));
        avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));

        TableColumn sumCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.sum"));
        sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("testeintrag");
        tableData.add(tableEntry);
        table.setItems(tableData);

        table.getColumns().addAll(name, colorCol, value, dateCol, minCol, maxCol, avgCol, sumCol);
    }

    private TableColumn<TableEntry, Color> buildColorColumn(String columnName) {
        TableColumn<TableEntry, Color> column = new TableColumn(columnName);
        column.setPrefWidth(100);
        column.setMaxWidth(100);
        column.setMinWidth(100);

        column.setCellValueFactory(param -> {
            System.out.println("CellFactory: " + param);
//                return new Simpleob<Color>

//                Color newColor = Color.valueOf(param.getValue().colorProperty().getName());
//                ObservableValue<Color> obColor = new SimpleObjectProperty<Color>(newColor);
            return new SimpleObjectProperty<>(param.getValue().getColor());

//                return obColor;
//                return param.getValue().colorProperty();
        });
        column.setCellFactory(new Callback<TableColumn<TableEntry, Color>, TableCell<TableEntry, Color>>() {
            @Override
            public TableCell<TableEntry, Color> call(TableColumn<TableEntry, Color> param) {
                TableCell<TableEntry, Color> cell = new TableCell<TableEntry, Color>() {
                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        System.out.println("Update: " + item + "  " + empty);
                        if (!empty && item != null) {
                            StackPane hbox = new StackPane();
                            hbox.setBackground(new Background(new BackgroundFill(item.deriveColor(1, 1, 50, 0.3), CornerRadii.EMPTY, Insets.EMPTY)));
//                            ColorPicker colorPicker = new ColorPicker();
//
//                            StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
//                            colorPicker.setValue(item);
//                            colorPicker.setStyle("-fx-color-label-visible: false ;");

//                            colorPicker.setDisable(true);
//                            hbox.getChildren().setAll(colorPicker);
                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };
                return cell;
            }
        });

        return column;

    }

    public XYChart getAreaChart() {
        return areaChart;
    }

    public void drawDefaultAreaChart() {
        ObservableList<XYChart.Series<Number, Number>> series = FXCollections.observableArrayList();

        ObservableList<XYChart.Data<Number, Number>> series1Data = FXCollections.observableArrayList();
        XYChart.Data<Number, Number> data1 = new XYChart.Data<Number, Number>(new GregorianCalendar(2012, 11, 15).getTime().getTime(), 2);
        Rectangle rect = new Rectangle(0, 0);
        rect.setVisible(false);
        data1.setNode(rect);
        series1Data.add(data1);
        series1Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 5, 3).getTime().getTime(), 4));

        ObservableList<XYChart.Data<Number, Number>> series2Data = FXCollections.observableArrayList();
        series2Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 0, 13).getTime().getTime(), 8));
        series2Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 7, 27).getTime().getTime(), 4));

        series.add(new XYChart.Series<>("Series1", series1Data));
        series.add(new XYChart.Series<>("Series2", series2Data));

        NumberAxis numberAxis = new NumberAxis();
//        DateAxis dateAxis = new DateAxis();
        Axis dateAxis = new DateValueAxis();
        areaChart = new AreaChart<>(dateAxis, numberAxis, series);
        areaChart.setTitle("default");
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            System.out.println("update chart view");
            this.drawAreaChart();
        } catch (JEVisException ex) {
            Logger.getLogger(AreaChartView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void drawAreaChart() throws JEVisException {
        tableData.clear();
        String unit = "";
        Set<BarChartDataModel> selectedData = dataModel.getSelectedData();

        ObservableList<XYChart.Series<Number, Number>> series = FXCollections.observableArrayList();
        List<Color> hexColors = new ArrayList<>();

        String title = I18n.getInstance().getString("plugin.graph.chart.title1");
        for (BarChartDataModel singleRow : selectedData) {
            hexColors.add(singleRow.getColor());
            System.out.println("curTitle:" + singleRow.getTitle());
            title = singleRow.getTitle();

            //-----------------------------------------
            List<JEVisSample> samples = singleRow.getSamples();
            ObservableList<XYChart.Data<Number, Number>> series1Data = FXCollections.observableArrayList();
            TreeMap<Double, JEVisSample> sampleMap = new TreeMap();

            TableEntry tableEntry = new TableEntry(singleRow.getObject().getName());
//            tableEntry.setColor(toRGBCode(singleRow.getColor()));
            tableEntry.setColor(singleRow.getColor());

            singleRow.setTableEntry(tableEntry);
            tableData.add(tableEntry);
            Boolean isQuantitiy = false;
            Double min = 0.0;
            Double max = 0.0;
            Double avg;
            Double sum = 0.0;

            for (JEVisSample sample : samples) {
                if (Objects.nonNull(sample.getAttribute().getObject().getAttribute("Value is a Quantity"))) {
                    if (Objects.nonNull(sample.getAttribute().getObject().getAttribute("Value is a Quantity").getLatestSample())) {
                        if (sample.getAttribute().getObject().getAttribute("Value is a Quantity").getLatestSample().getValueAsBoolean()) {

                            isQuantitiy = true;
                        }
                    }
                }
                unit = sample.getUnit().getLabel();
                sampleMap.put((double) sample.getTimestamp().getMillis(), sample);
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();
                if (isQuantitiy) {
                    min = Math.min(value, min);
                    max = Math.max(value, max);
                    sum += value;
                }
//                Date date = dateTime.toGregorianCalendar().getTime();
                Long timestamp = dateTime.getMillis();
                XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(timestamp, value);

                //dot for the chart
                Rectangle rect = new Rectangle(0, 0);
                rect.setVisible(false);
                data.setNode(rect);
                series1Data.add(data);
            }
            if (isQuantitiy) {
                avg = sum / samples.size();
                NumberFormat nf_out = NumberFormat.getNumberInstance();
                nf_out.setMaximumFractionDigits(2);
                nf_out.setMinimumFractionDigits(2);

                tableEntry.setMin(nf_out.format(min) + " " + unit);
                tableEntry.setMax(nf_out.format(max) + " " + unit);
                tableEntry.setAvg(nf_out.format(avg) + " " + unit);
                tableEntry.setSum(nf_out.format(sum) + " " + unit);
            }

            XYChart.Series<Number, Number> currentSerie = new XYChart.Series<>(singleRow.getObject().getName(), series1Data);
            currentSerie.setName("test");
            singleRow.setSampleMap(sampleMap);
            series.add(currentSerie);
        }

        table.setItems(tableData);
        table.setFixedCellSize(25);
        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(30));
        NumberAxis numberAxis = new NumberAxis();
        Axis dateAxis = new DateValueAxis();

        areaChart = new AreaChart<>(dateAxis, numberAxis, series);
        areaChart.applyCss();
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            System.out.println("cirght" + currentColor.getBrightness());
            Color brighter = currentColor.deriveColor(1, 1, 50, 0.3);
            String hexColor = toRGBCode(currentColor) + "55";
            String hexBrighter = toRGBCode(brighter) + "55";
            String preIdent = ".default-color" + i;
            Node node = areaChart.lookup(preIdent + ".chart-series-area-fill");
            node.setStyle("-fx-fill: linear-gradient(" + hexBrighter + "," + hexBrighter + ");"
                    + "  -fx-background-insets: 0 0 -1 0, 0, 1, 2;"
                    + "  -fx-background-radius: 3px, 3px, 2px, 1px;");

            Node nodew = areaChart.lookup(preIdent + ".chart-series-area-line");
            // Set the first series fill to translucent pale green
            nodew.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
        }
//        int i = 0;
//        for (Node n : areaChart.lookupAll(".series0")) {
//            n.setStyle("-fx-fill: blue,white;");
//            i++;
//            if (i > 20) {
//                break;
//            }
//        }
        System.out.println("Title:" + title);
        areaChart.setTitle(title);
        areaChart.setLegendVisible(false);
        areaChart.setCreateSymbols(false);
        areaChart.layout();

        areaChart.getXAxis().setAutoRanging(true);
        areaChart.getYAxis().setAutoRanging(true);

        String finalUnit = unit;
        areaChart.setOnMouseMoved(mouseEvent -> {
            Double valueForDisplay = (Double) areaChart.getXAxis().getValueForDisplay(mouseEvent.getX());
            tableData.clear();
            for (BarChartDataModel singleRow : selectedData) {
                try {
                    Double higherKey = singleRow.getSampleMap().higherKey(valueForDisplay);
                    Double lowerKey = singleRow.getSampleMap().lowerKey(valueForDisplay);
                    Double nearest = higherKey;

                    if (Objects.nonNull(higherKey) && Objects.nonNull(lowerKey)) {
                        if (lowerKey - valueForDisplay < higherKey - valueForDisplay) {
                            nearest = lowerKey;
                        }
                    } else if (Objects.isNull(higherKey)) nearest = lowerKey;
                    else if (Objects.isNull(lowerKey)) nearest = higherKey;

                    Double valueAsDouble = singleRow.getSampleMap().get(nearest).getValueAsDouble();
                    TableEntry tableEntry = singleRow.getTableEntry();
                    DateTime dateTime = new DateTime(Math.round(nearest));
                    tableEntry.setDate(dateTime.toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
                    tableEntry.setValue(valueAsDouble.toString() + finalUnit);
                    tableData.add(tableEntry);

                    table.layout();
                } catch (JEVisException ex) {
//                        Logger.getLogger(AreaChartView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        ChartPanManager panner = new ChartPanManager(areaChart);
        panner.setMouseFilter(mouseEvent -> {
            System.out.println("mouse event");
            if (mouseEvent.getButton() == MouseButton.SECONDARY
                    || (mouseEvent.getButton() == MouseButton.PRIMARY
                    && mouseEvent.isShortcutDown())) {
                //let it through
            } else {
                mouseEvent.consume();
            }
        });
        panner.start();
        areaChartRegion = JFXChartUtil.setupZooming(areaChart, mouseEvent -> {
            System.out.println("zooming");
            if (mouseEvent.getButton() != MouseButton.PRIMARY
                    || mouseEvent.isShortcutDown()) {
                mouseEvent.consume();
            }
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(areaChart);
    }

    public Region getAreaChartRegion() {
        return areaChartRegion;
    }

    public VBox getVbox() {
        return vbox;
    }

    public TableView getLegend() {
        return table;
    }

    public String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

}
