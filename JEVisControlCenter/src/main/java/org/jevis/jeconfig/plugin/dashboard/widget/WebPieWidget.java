package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Experimental JavaScript based PieChart. Prototype for future browser based WidGets.
 * To enable this Widget add "d3.min.js" and "d3pie.js" into the resources folder '/html'
 */
public class WebPieWidget extends Widget {
    private static final Logger logger = LogManager.getLogger(WebPieWidget.class);
    public static String WIDGET_ID = "Web Pie";
    private final NumberFormat nf = NumberFormat.getInstance();

    private final ObjectMapper mapper = new ObjectMapper();
    private final BorderPane borderPane = new BorderPane();
    private final VBox legendPane = new VBox();
    private final WebView webView = new WebView();


    public WebPieWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new WebPie Widget");
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().xGridInterval * 12, control.getActiveDashboard().yGridInterval * 12));


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("WebPie.Update: {}", interval);
        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        this.borderPane.setMaxWidth(this.config.getSize().getWidth());
        this.nf.setMinimumFractionDigits(0);/** tmp solution**/
        this.nf.setMaximumFractionDigits(0);


        List<Color> colors = new ArrayList<>();

        /** data Update **/
        AtomicDouble total = new AtomicDouble(0);
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
//                chartDataModel.setAbsolute(true);
                Double dataModelTotal = DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), chartDataModel.getSamples(), chartDataModel);
                total.set(total.get() + dataModelTotal);

            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        List<PieData> pieDataList = new ArrayList<>();
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                String dataName = chartDataModel.getObject().getName();
                double value = 0;
                boolean hasNoData = chartDataModel.getSamples().isEmpty();

                double proC = 0;

                if (!hasNoData) {
                    logger.error("Samples: ({}) {}", dataName, chartDataModel.getSamples());
                    try {
                        value = DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), chartDataModel.getSamples(), chartDataModel);
                        BigDecimal bd = new BigDecimal(value);
                        bd = bd.setScale(5, RoundingMode.HALF_UP);
//                        value = bd.doubleValue();

                        proC = (bd.doubleValue() / total.get()) * 100;
                        if (Double.isInfinite(proC)) proC = 100;
                        if (Double.isNaN(proC)) proC = 0;


                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                } else {
                    logger.debug("Empty Samples for: {}", this.config.getTitle());
                }


                if (proC < 8) {
                    proC = 8;
                }

                PieData pieData = new PieData(chartDataModel.getColor()
                        , value, proC, UnitManager.getInstance().format(chartDataModel.getUnitLabel()), dataName, dataName);

                pieDataList.add(pieData);
                colors.add(pieData.getColor());

            } catch (Exception ex) {
                logger.error(ex);
            }
        });


        WebEngine webEngine = this.webView.getEngine();

        webEngine.setJavaScriptEnabled(true);


//        logger.error("\n--------------\n{}\n------------------", buildHTMLPie2(pieDataList));
        /** redrawing **/
        Platform.runLater(() -> {
            webEngine.loadContent(buildHTMLPie2(pieDataList), "text/html");
        });
    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }

    @Override
    public boolean isStatic() {
        return false;
    }


    @Override
    public List<DateTime> getMaxTimeStamps() {
        return sampleHandler.getMaxTimeStamps();
    }


    @Override
    public void init() {

        try {
            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
            this.sampleHandler.setMultiSelect(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Platform.runLater(() -> {
            this.borderPane.setCenter(this.webView);
            setGraphic(this.borderPane);
            WebEngine engine = this.webView.getEngine();
            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> o, Worker.State old, final Worker.State state) {
                    if (state == Worker.State.SUCCEEDED) {
                        try {
//                            System.out.println("Page loaded: " + engine.getLocation());
                            engine.executeScript("document.style.overflow = 'hidden';");
                        } catch (Exception ex) {
                        }
                    }
                }
            });
        });


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
        return JEConfig.getImage("widget/DonutWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    private String buildHTMLPie2(List<PieData> pieDataList) {
        StringBuilder sb = new StringBuilder();

        String unit = "";
        for (PieData pieData : pieDataList) {
            unit = pieData.getUnit();
            break;
        }


        sb.append(
                "<html>" +
                        "<head></head>" +
                        "<body>" +
                        "<div id=\"pieChart\"></div>" +
                        "<script src=\"" + getClass().getResource("/html/d3.min.js") + "\"></script>" +
                        "<script src=\"" + getClass().getResource("/html/d3pie.js") + "\"></script>" +
                        "<script>\n" +
                        "var pie = new d3pie(\"pieChart\", {" +
                        "\"header\": {" +
                        "\"title\": {" +
                        "\"text\": \"" + "" + "\"," +
                        "\"fontSize\": 18," +
                        "\"font\": \"courier\"" +
                        "},\n" +
                        "\"subtitle\": {" +
                        "\"text\": \"sub\"," +
                        "\"color\": \"#999999\"," +
                        "\"fontSize\": 8," +
                        "\"font\": \"courier\"" +
                        "},\n" +
                        "\"location\": \"pie-center\"," +
                        "\"titleSubtitlePadding\": 9" +
                        "}," +
                        "\"footer\": {" +
                        "\"color\": \"#999999\"," +
                        "\"fontSize\": 10," +
                        "\"font\": \"open sans\"," +
                        "\"location\": \"bottom-left\"" +
                        "}," +
                        "\"size\": {" +
                        "\"canvasWidth\": " + (this.borderPane.getWidth() - 20) + "," +
                        "\"canvasHeight\": " + (this.borderPane.getHeight() - 20) + "," +
//                        "\"pieInnerRadius\": \"31%\"," +
                        "\"pieOuterRadius\": \"90%\"" +
                        "}," +
                        "\"data\": {" +
                        "\"sortOrder\": \"label-desc\"," +
                        "\"smallSegmentGrouping\": {" +
                        "\"enabled\": true" +
                        "},\n" +
                        "\"content\": [");


        boolean firstElement = true;
        for (PieData pieData : pieDataList) {
            if (!firstElement) {
                sb.append(",");
            }
            sb.append(String.format("{label: '%s [%s]', value: %s, color: '%s'}"
                    , pieData.getLineName(), pieData.getUnit(), pieData.getRealValue(), toRGBCode(pieData.getColor())));
            firstElement = false;
        }


        sb.append(
                "]\n" +
                        "},\n" +
                        "\"labels\": {" +
                        "\"outer\": {\n" +
                        "\"format\": \"label-value2\"," +
                        "\"pieDistance\": 10" +
                        "},\n" +
                        "\"inner\": {\n" +
                        "\t\t\t\"hideWhenLessThanPercentage\": 2\n" +
                        "\t\t}," +

                        "\"mainLabel\": {" +
                        "\"fontSize\": 11" +
                        "},\n" +
                        "\"percentage\": {" +
                        "\"color\": \"#ffffff\"," +
                        "\"fontSize\": 11," +
                        "\"decimalPlaces\": 2" +
                        "},\n" +
                        "\"value\": {" +
                        "\"color\": \"#000000\"," +
                        "\"fontSize\": 11" +
                        "},\n" +
                        "\"lines\": {" +
                        "\"enabled\": true," +
                        "\"style\": \"straight\"," +
                        "\"color\": \"#777777\"" +
                        "},\n" +
                        "\"truncation\": {" +
                        "\"enabled\": true" +
                        "}" +
                        "}," +
                        "\"effects\": {" +
                        "\"pullOutSegmentOnClick\": {" +
                        "\"effect\": \"linear\"," +
                        "\"speed\": 400," +
                        "\"size\": 8" +
                        "}" +
                        "}," +
                        "\"misc\": {" +
                        "\"colors\": {" +
                        "\"segmentStroke\": \"#000000\"" +
                        "}" +
                        "}" +
                        "});" +
                        "</script>" +
                        "" +
                        "</body>" +
                        "</html>"
        );


        return sb.toString();
    }

    private String buildHTMLPie3(List<PieData> pieDataList) {
        StringBuilder sb = new StringBuilder();

        String unit = "";
        for (PieData pieData : pieDataList) {
            unit = pieData.getUnit();
            break;
        }

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"utf-8\">");
        sb.append("<script src=\"");
        sb.append(getClass().getResource("/html/echarts/echarts.min2.js"));
        sb.append("\"></script>");
        sb.append("</head>");
        sb.append("<body bgcolor=\"#FFFFFF\" >");
        sb.append("<div id=\"main\" style=\"width:");
        sb.append(this.borderPane.getWidth() - 20);
        sb.append("px;height:");
        sb.append(this.borderPane.getHeight() - 20);
        sb.append("px;\"></div>");
        sb.append("<script type=\"text/javascript\">");
        sb.append("var myChart = echarts.init(document.getElementById('main'));");

        sb.append("option = {" +
                "" +
                "    backgroundColor: '#FFFFFF'," +
                "    calculable : true," +
                "    series : [" +
                "" +
                "        {" +
                "            name:'Pie'," +
                "            type:'sunburst'," +
                "            radius : [10, 60]," +
                "            center : ['50%', '50%']," +
//                "            roseType : 'area'," +
                "            animation: false," +
                "            hoverAnimation: true," +
                "            label:{ " +
                "                   formatter: '{b}: \\n{@1} " + unit + " ({d}%)'," +
                "                   color: '#000000'" +
                "            }," +
                "            data:[");


        boolean firstElement = true;
        for (PieData pieData : pieDataList) {
            if (!firstElement) {
                sb.append(",");
            }
            sb.append(String.format("{value:%s, name:'%s',itemStyle: {color: '%s'}}"
                    , pieData.getRealValue(), pieData.getLineName(), toRGBCode(pieData.getColor())));
            firstElement = false;
        }
        sb.append("]");
        sb.append("}");
        sb.append("]");
        sb.append("};");


        sb.append("myChart.setOption(option)");
        sb.append(" </script>");
        sb.append("</body>");
        sb.append("</html>");


        return sb.toString();
    }


    private String buildHTMLPie(List<PieData> pieDataList) {
        StringBuilder sb = new StringBuilder();

        String unit = "";
        for (PieData pieData : pieDataList) {
            unit = pieData.getUnit();
            break;
        }

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"utf-8\">");
        sb.append("<script src=\"");
        sb.append(getClass().getResource("/html/echarts/echarts.min.js"));
        sb.append("\"></script>");
        sb.append("</head>");
        sb.append("<body bgcolor=\"#FFFFFF\" >");
        sb.append("<div id=\"main\" style=\"width:");
        sb.append(this.borderPane.getWidth() - 20);
        sb.append("px;height:");
        sb.append(this.borderPane.getHeight() - 20);
        sb.append("px;\"></div>");
        sb.append("<script type=\"text/javascript\">");
        sb.append("var myChart = echarts.init(document.getElementById('main'));");

        sb.append("option = {" +
                "" +
                "    backgroundColor: '#FFFFFF'," +
                "    calculable : true," +
                "    series : [" +
                "" +
                "        {" +
                "            name:'Pie'," +
                "            type:'pie'," +
                "            radius : [10, 60]," +
                "            center : ['50%', '50%']," +
                "            roseType : 'area'," +
                "            animation: false," +
                "            hoverAnimation: true," +
                "            label:{ " +
                "                   formatter: '{b}: \\n{@1} " + unit + " ({d}%)'," +
                "                   color: '#000000'" +
                "            }," +
                "            data:[");


        boolean firstElement = true;
        for (PieData pieData : pieDataList) {
            if (!firstElement) {
                sb.append(",");
            }
            sb.append(String.format("{value:%s, name:'%s',itemStyle: {color: '%s'}}"
                    , pieData.getRealValue(), pieData.getLineName(), toRGBCode(pieData.getColor())));
            firstElement = false;
        }
        sb.append("]");
        sb.append("}");
        sb.append("]");
        sb.append("};");


        sb.append("myChart.setOption(option)");
        sb.append(" </script>");
        sb.append("</body>");
        sb.append("</html>");


        return sb.toString();
    }


    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private class PieData {
        private double realValue, relativValue;
        private String legendName, lineName, unit;
        private Color color;

        public PieData(Color color, double realValue, double relativeValue, String unit, String legendName, String lineName) {
            this.color = color;
            this.unit = unit;
            this.realValue = realValue;
            this.relativValue = relativeValue;
            this.legendName = legendName;
            this.lineName = lineName;
        }


        public Color getColor() {
            return this.color;
        }

        public double getRealValue() {
            return this.realValue;
        }

        public void setRealValue(double realValue) {
            this.realValue = realValue;
        }

        public double getRelativValue() {
            return this.relativValue;
        }

        public void setRelativValue(double relativValue) {
            this.relativValue = relativValue;
        }

        public String getLegendName() {
            return this.legendName;
        }

        public void setLegendName(String legendName) {
            this.legendName = legendName;
        }

        public String getLineName() {
            return this.lineName;
        }

        public void setLineName(String lineName) {
            this.lineName = lineName;
        }

        public String getUnit() {
            return this.unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}
