package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import eu.hansolo.fx.charts.SankeyPlot;
import eu.hansolo.fx.charts.SankeyPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetLegend;
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
import java.util.*;

public class SankeyWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(SankeyWidget.class);
    public static String WIDGET_ID = "Sankey";
    private final NumberFormat nf = NumberFormat.getInstance();
    private SankeyPlot sankeyPlot;
    private DataModelDataHandler sampleHandler;
    private final WidgetLegend legend = new WidgetLegend();
    private final BorderPane borderPane = new BorderPane();
    private Interval lastInterval = null;
    private final BorderPane bottomBorderPane = new BorderPane();
    private List<JEVisPlotItem> plotItems = new ArrayList<>();
    private Boolean customWorkday = true;

    public SankeyWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
    }

    public SankeyWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public void debug() {
        sampleHandler.debug();
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new Chart Widget");
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 12, control.getActiveDashboard().xGridInterval * 20));

        return widgetPojo;
    }

    @Override
    public void updateData(Interval interval) {
        logger.error("Sankey.Update: {}", interval);
        this.lastInterval = interval;

        if (sampleHandler == null) {
            showProgressIndicator(false);
            return;
        } else {
            showProgressIndicator(true);
        }

        showProgressIndicator(true);
        showAlertOverview(false, "");

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        try {


            this.sampleHandler.getDataModel().forEach(dataModel -> {
                try {
                    dataModel.setCustomWorkDay(customWorkday);
                    System.out.println("sampleHandler.getDataModel().forEach: " + dataModel.getObject().getID());
                    System.out.println("Plot Items: " + plotItems);
                    String dataName = dataModel.getObject().getName();
                    AtomicDouble total = new AtomicDouble(0);

                    plotItems.forEach(jeVisPlotItem -> {
                        System.out.println("jeVisPlotItem: " + jeVisPlotItem.getObject().getID());
                        if (jeVisPlotItem.getObject().getID().equals(dataModel.getObject().getID())) {
                            System.out.println("is equal");
                            List<JEVisSample> results = dataModel.getSamples();
                            System.out.println("Sample: " + results);
                            if (!results.isEmpty()) {

                                total.set(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, dataModel));
                                String valueText = this.nf.format(total.get()) + " " + dataModel.getUnitLabel();
                                jeVisPlotItem.setValue(total.get());
                                System.out.println("Total: " + total + "  " + valueText);
                                jeVisPlotItem.setDescription("Wo ist die Description");
                                jeVisPlotItem.setName(jeVisPlotItem.getName() + " " + valueText);
                            }
                        }
                    });

                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            /**
             * LineChart does not support updateData so we need to create an new one every time;
             */

            /**
             AnalysisDataModel model = new AnalysisDataModel(getDataSource(), null);
             model.setHideShowIconsNO_EVENT(false);
             ChartSetting chartSetting = new ChartSetting(0, "");
             chartSetting.setChartType(null);
             model.getCharts().setListSettings(Collections.singletonList(chartSetting));


             PlotItem brazil = new PlotItem("Brazil", 9, "Beschreibung", Color.LIGHTBLUE);
             PlotItem mexico = new PlotItem("Mexico", Color.ORANGE);
             PlotItem usa = new PlotItem("USA", Color.ORANGE);
             PlotItem canada = new PlotItem("Canada", Color.LIGHTGOLDENRODYELLOW);

             PlotItem germany = new PlotItem("Germany", Color.web("#FF48C6"));

             PlotItem portugal = new PlotItem("Portugal", Color.LIGHTSKYBLUE);
             PlotItem spain = new PlotItem("Spain", Color.LIGHTCORAL);
             PlotItem england = new PlotItem("England", Color.LIGHTSLATEGREY);
             PlotItem france = new PlotItem("France", Color.LIGHTGREEN);

             PlotItem southAfrica = new PlotItem("South Africa", Color.YELLOW);
             PlotItem angola = new PlotItem("Angola", Color.VIOLET);
             PlotItem morocco = new PlotItem("Morocco", Color.YELLOW);
             PlotItem senegal = new PlotItem("Senegal", Color.PURPLE);
             PlotItem mali = new PlotItem("Mali", Color.BLUE);

             PlotItem china = new PlotItem("China", Color.BLUE);
             PlotItem japan = new PlotItem("Japan", Color.GREEN);
             PlotItem india = new PlotItem("India", Color.GREEN);

             brazil.addToOutgoing(portugal, 5);
             brazil.addToOutgoing(france, 1);
             brazil.addToOutgoing(spain, 1);
             brazil.addToOutgoing(england, 1);
             canada.addToOutgoing(portugal, 1);
             canada.addToOutgoing(france, 5);
             canada.addToOutgoing(england, 1);
             mexico.addToOutgoing(portugal, 1);
             mexico.addToOutgoing(france, 1);
             mexico.addToOutgoing(spain, 5);
             mexico.addToOutgoing(england, 1);
             usa.addToOutgoing(portugal, 1);
             usa.addToOutgoing(france, 1);
             usa.addToOutgoing(spain, 1);
             usa.addToOutgoing(england, 5);

             portugal.addToOutgoing(germany, 12);
             portugal.addToOutgoing(southAfrica, 12);

             france.addToOutgoing(angola, 6);
             france.addToOutgoing(japan, 6);
             france.addToOutgoing(india, 6);

             spain.addToOutgoing(morocco, 6);
             spain.addToOutgoing(mali, 6);
             spain.addToOutgoing(china, 6);
             spain.addToOutgoing(senegal, 6);
             **/


            Platform.runLater(() -> {
                this.borderPane.setCenter(null);
                borderPane.setPadding(new Insets(9));

                Size configSize = getConfig().getSize();
                this.sankeyPlot = SankeyPlotBuilder.create()
                        .prefSize(configSize.getWidth(), configSize.getHeight())
                        .items(plotItems)
                        .streamFillMode(SankeyPlot.StreamFillMode.GRADIENT)
                        .build();


                Label titleLabel = new Label(getConfig().getTitle());
                titleLabel.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
                titleLabel.setAlignment(Pos.CENTER);
                HBox hBox = new HBox(titleLabel);
                hBox.setAlignment(Pos.CENTER);

//                    model.setShowSum_NOEVENT(true);

                this.borderPane.setTop(hBox);
                this.borderPane.setCenter(sankeyPlot);
                this.legend.getItems().clear();


                //sankeyPlot.setPrefSize(configSize.getWidth() - 20, configSize.getHeight());
                updateConfig();
            });
            /** workaround because we make a new chart every time**/
        } catch (Exception ex) {
            logger.error(ex);
        }

        showProgressIndicator(false);
    }


    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                this.setBackground(bgColorTrans);
                this.legend.setBackground(bgColorTrans);
                this.borderPane.setBackground(bgColor);

                try {
                    if (sankeyPlot != null) {/**
                     String cssBGColor = ColorHelper.toRGBCode(Color.TRANSPARENT);
                     sankeyPlot.getChart().getPlotBackground().setStyle("-fx-background-color: " + cssBGColor + ";");

                     sankeyPlot.getChart().setStyle("-fx-background-color: " + cssBGColor + ";");
                     this.sankeyPlot.getChart().getAxes().forEach(axis -> {
                     if (axis instanceof DefaultNumericAxis) {
                     DefaultNumericAxis defaultNumericAxis = (DefaultNumericAxis) axis;
                     defaultNumericAxis.getAxisLabel().setVisible(false);
                     defaultNumericAxis.setStyle("-fx-text-color: " + ColorHelper.toRGBCode(this.config.getFontColor()) + ";");
                     }
                     });
                     sankeyPlot.getChart().requestLayout();
                     **/
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
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


    private void updatePlotList() {
        List<JEVisPlotItem> newList = new ArrayList<>();
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
            System.out.println("getDataModel: " + chartDataModel.getAttribute());
            boolean exists = false;


            for (JEVisPlotItem visPlotItem : plotItems) {
                if (visPlotItem.getObject().getID().equals(chartDataModel.getObject().getID())) {
                    newList.add(visPlotItem);
                    exists = true;
                }
            }
            if (!exists) {
                JEVisPlotItem plotItem = new JEVisPlotItem(chartDataModel.getObject().getName(), 10, "Des1", getRandomColor(), chartDataModel.getObject());
                newList.add(plotItem);
            }


        });

        plotItems = newList;
    }

    private void updateTab(Tab tab) {
        System.out.println("has focus");
        plotItems = new ArrayList<>();
        updatePlotList();

        /**
         this.sampleHandler.getDataModel().forEach(chartDataModel -> {
         System.out.println("getDataModel: " + chartDataModel.getAttribute());
         boolean exists = false;
         for (JEVisPlotItem jeVisPlotItem : plotItems) {
         if (jeVisPlotItem.getObject().getID() == jeVisPlotItem.getObject().getID()) {
         exists = true;
         }
         }

         if (!exists) {
         JEVisPlotItem plotItem = new JEVisPlotItem(chartDataModel.getObject().getName(), 10, "Des1", getRandomColor(), chartDataModel.getObject());
         plotItems.add(plotItem);
         }

         });
         **/

        ListView<JEVisPlotItem> leftList = new ListView<>();
        leftList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        leftList.setCellFactory(new Callback<ListView<JEVisPlotItem>, ListCell<JEVisPlotItem>>() {
            @Override
            public ListCell<JEVisPlotItem> call(ListView<JEVisPlotItem> param) {
                return new ListCell<JEVisPlotItem>() {
                    @Override
                    protected void updateItem(JEVisPlotItem obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            setText(obj.getName());
                            ColorPickerAdv bgColorPicker = new ColorPickerAdv();
                            bgColorPicker.setValue(obj.getFill());
                            bgColorPicker.setMaxHeight(8);
                            bgColorPicker.setOnAction(event -> {
                                obj.setFill(bgColorPicker.getValue());
                            });
                            setGraphic(bgColorPicker);
                        }
                    }
                };
            }
        });

        ListView<JEVisPlotItem> subList = new ListView<>();
        subList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        subList.setCellFactory(new Callback<ListView<JEVisPlotItem>, ListCell<JEVisPlotItem>>() {
            @Override
            public ListCell<JEVisPlotItem> call(ListView<JEVisPlotItem> param) {
                return new ListCell<JEVisPlotItem>() {
                    @Override
                    protected void updateItem(JEVisPlotItem obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            System.out.println("---- Update cell -----");
                            setText(obj.getName());
                            CheckBox selectedBox = new CheckBox();

                            try {

                                JEVisPlotItem selectedLeftItem = leftList.getSelectionModel().getSelectedItem();
                                System.out.println("Left selected item: " + selectedLeftItem.getObject().getName());


                                if (selectedLeftItem != null) {

                                    obj.getIncoming().forEach((plotItem, aDouble) -> {

                                        if (plotItem.equals(selectedLeftItem)) {
                                            System.out.println("Is intcoming from: " + selectedLeftItem);
                                            selectedBox.setSelected(true);
                                        }
                                    });

                                    /**
                                     if (SankeyWidget.this.isSelected(selectedLeftItem, obj)) {
                                     selected.setSelected(true);
                                     }
                                     **/

                                    selectedBox.setOnAction(event1 -> {
                                        if (SankeyWidget.this.isSelected(selectedLeftItem, obj)) {
                                            System.out.println("Remove: " + obj.getObject().getName() + "  from: " + selectedLeftItem.getObject().getName());
                                            selectedLeftItem.removeFromOutgoing(obj);
                                        } else {
                                            System.out.println("Add: " + obj.getObject().getName() + "  from: " + selectedLeftItem.getObject().getName());
                                            selectedLeftItem.addToOutgoing(obj, 10);
                                        }
                                    });
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            //HBox hBox = new HBox(selectedBox);
                            setGraphic(selectedBox);
                        }
                    }
                };
            }
        });

        leftList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("leftList.getSelectionModel(): " + newValue);
            subList.getSelectionModel().clearSelection();

            /**
             for (Map.Entry<PlotItem, Double> entry : newValue.getOutgoing().entrySet()) {
             JEVisPlotItem plotItem = (JEVisPlotItem) entry.getKey();
             subList.getSelectionModel().select(plotItem);

             }
             **/

            subList.refresh();
            subList.layout();
        });
        /**
         subList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
         JEVisPlotItem selectedLeftItem = leftList.getSelectionModel().getSelectedItem();
         System.out.println(newValue);


         });
         **/


        leftList.setItems(FXCollections.observableArrayList(plotItems));
        subList.setItems(FXCollections.observableArrayList(plotItems));
        leftList.getSelectionModel().selectFirst();

        Label headerLeft = new Label("Datenpunkt");
        Label headerRight = new Label("Fließt in datenpunk");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(headerLeft, 0, 0);
        gridPane.add(headerRight, 1, 0);
        gridPane.add(leftList, 0, 1);
        gridPane.add(subList, 1, 1);
        tab.setContent(gridPane);
    }

    private Tab makeConfigTab() {
        Tab tab = new Tab("Sankey");

        tab.setOnSelectionChanged(event -> {
            System.out.println("Tap changed");
            if (tab.isSelected()) {
                updateTab(tab);
            }
        });
        updateTab(tab);

        return tab;
    }

    private boolean isSelected(JEVisPlotItem parent, JEVisPlotItem item) {
        System.out.println("isSelected: " + parent + " item: " + item);
        for (Map.Entry<PlotItem, Double> entry : parent.getOutgoing().entrySet()) {
            JEVisPlotItem plotItem = (JEVisPlotItem) entry.getKey();
            if (plotItem.getObject().getID().equals(item.getObject().getID())) {
                return true;
            }
        }
        return false;
    }


    private Color getRandomColor() {
        Random rand = new Random();
        double r = rand.nextFloat() / 2f + 0.5;
        double g = rand.nextFloat() / 2f + 0.5;
        double b = rand.nextFloat() / 2f + 0.5;
        final Color color = new Color(r, g, b, 0.9);
        return color;
    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);


        widgetConfigDialog.addTab(makeConfigTab());

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

    @Override
    public void init() {
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
        this.sampleHandler.setMultiSelect(true);

        this.legend.setAlignment(Pos.CENTER);


//        bottomBorderPane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("bottomBorderPane: " + newValue);
//        });
        this.borderPane.setBottom(bottomBorderPane);
        this.borderPane.setBottom(this.legend);
        setGraphic(this.borderPane);


        /** Dummy chart **/
        //this.lineChart = new LineChart(new AnalysisDataModel(getDataSource(),new GraphPluginView(getDataSource(),"dummy")) , this.sampleHandler.getDataModel(), 0, "");
        //this.borderPane.setCenter(lineChart.getChart());
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
        return JEConfig.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
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

    public class JEVisPlotItem extends PlotItem {

        private JEVisObject object;

        public JEVisPlotItem(String NAME, double VALUE, String DESCRIPTION, Color FILL, JEVisObject object) {
            super(NAME, VALUE, DESCRIPTION, FILL);
            this.object = object;
        }

        public JEVisObject getObject() {
            return object;
        }

        public void setObject(JEVisObject object) {
            this.object = object;
        }
    }

}
