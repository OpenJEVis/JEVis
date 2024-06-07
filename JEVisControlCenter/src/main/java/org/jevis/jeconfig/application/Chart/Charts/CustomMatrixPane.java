package org.jevis.jeconfig.application.Chart.Charts;

import eu.hansolo.fx.charts.*;
import eu.hansolo.fx.charts.data.MatrixItem;
import eu.hansolo.fx.charts.series.MatrixItemSeries;
import eu.hansolo.fx.charts.tools.ColorMapping;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

public class CustomMatrixPane<T extends MatrixItem> extends Region implements ChartArea {
    private static final Logger logger = LogManager.getLogger(CustomMatrixPane.class);
    private static final double PREFERRED_WIDTH = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH = 0;
    private static final double MINIMUM_HEIGHT = 0;
    private static final double MAXIMUM_WIDTH = 4096;
    private static final double MAXIMUM_HEIGHT = 4096;
    private static double aspectRatio;
    private final Tooltip tp = new Tooltip("");
    private final boolean keepAspect;
    private double size;
    private double width;
    private double height;
    private Pane pane;
    private Paint _chartBackground;
    private ObjectProperty<Paint> chartBackground;
    private final MatrixItemSeries<T> series;
    private PixelMatrix matrix;
    private LinearGradient matrixGradient;
    private double minZ;
    private double maxZ;
    private double rangeZ;
    private double scaleX;
    private double scaleY;
    private final double scaleZ;
    private double _lowerBoundX;
    private DoubleProperty lowerBoundX;
    private double _upperBoundX;
    private DoubleProperty upperBoundX;
    private double _lowerBoundY;
    private DoubleProperty lowerBoundY;
    private double _upperBoundY;
    private DoubleProperty upperBoundY;
    private double _lowerBoundZ;
    private DoubleProperty lowerBoundZ;
    private double _upperBoundZ;
    private DoubleProperty upperBoundZ;
    private List<DateTime> XAxisList;
    private String XFormat;
    private Map<MatrixXY, Double> matrixData;
    private String unit;
    private GridPane leftAxis;
    private GridPane rightAxis;
    private Canvas bottomXAxis;


    // ******************** Constructors **************************************
    public CustomMatrixPane(final MatrixItemSeries<T> SERIES) {
        this(Color.WHITE, SERIES);
    }

    public CustomMatrixPane(final Paint BACKGROUND, final MatrixItemSeries<T> SERIES) {
        getStylesheets().add(XYPane.class.getResource("chart.css").toExternalForm());
        aspectRatio = PREFERRED_HEIGHT / PREFERRED_WIDTH;
        keepAspect = false;
        _chartBackground = BACKGROUND;
        series = SERIES;
        matrixGradient = ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED.getGradient();
        scaleX = 1;
        scaleY = 1;
        scaleZ = 1;
        _lowerBoundX = 0;
        _upperBoundX = 100;
        _lowerBoundY = 0;
        _upperBoundY = 100;
        _lowerBoundZ = 0;
        _upperBoundZ = 100;
        tp.setAutoHide(true);

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
                Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().setAll("chart", "matrix-chart");

        matrix = PixelMatrixBuilder.create()
                .prefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT)
                .pixelShape(PixelMatrix.PixelShape.SQUARE)
                .useSpacer(true)
                .squarePixels(false)
                .pixelOnColor(Color.BLACK)
                .pixelOffColor(Color.TRANSPARENT)
                .build();

        pane = new Pane(matrix);
        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());

        series.setOnSeriesEvent(seriesEvent -> redraw());
    }


    // ******************** Methods *******************************************
    @Override
    protected double computeMinWidth(final double HEIGHT) {
        return MINIMUM_WIDTH;
    }

    @Override
    protected double computeMinHeight(final double WIDTH) {
        return MINIMUM_HEIGHT;
    }

    @Override
    protected double computePrefWidth(final double HEIGHT) {
        return super.computePrefWidth(HEIGHT);
    }

    @Override
    protected double computePrefHeight(final double WIDTH) {
        return super.computePrefHeight(WIDTH);
    }

    @Override
    protected double computeMaxWidth(final double HEIGHT) {
        return MAXIMUM_WIDTH;
    }

    @Override
    protected double computeMaxHeight(final double WIDTH) {
        return MAXIMUM_HEIGHT;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public Paint getChartBackground() {
        return null == chartBackground ? _chartBackground : chartBackground.get();
    }

    public void setChartBackground(final Paint PAINT) {
        if (null == chartBackground) {
            _chartBackground = PAINT;
            redraw();
        } else {
            chartBackground.set(PAINT);
        }
    }

    public ObjectProperty<Paint> chartBackgroundProperty() {
        if (null == chartBackground) {
            chartBackground = new ObjectPropertyBase<Paint>(_chartBackground) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "chartBackground";
                }
            };
            _chartBackground = null;
        }
        return chartBackground;
    }

    public double getLowerBoundX() {
        return null == lowerBoundX ? _lowerBoundX : lowerBoundX.get();
    }

    public void setLowerBoundX(final double VALUE) {
        if (null == lowerBoundX) {
            _lowerBoundX = VALUE;
            redraw();
        } else {
            lowerBoundX.set(VALUE);
        }
    }

    public DoubleProperty lowerBoundXProperty() {
        if (null == lowerBoundX) {
            lowerBoundX = new DoublePropertyBase(_lowerBoundX) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "lowerBoundX";
                }
            };
        }
        return lowerBoundX;
    }

    public double getUpperBoundX() {
        return null == upperBoundX ? _upperBoundX : upperBoundX.get();
    }

    public void setUpperBoundX(final double VALUE) {
        if (null == upperBoundX) {
            _upperBoundX = VALUE;
            redraw();
        } else {
            upperBoundX.set(VALUE);
        }
    }

    public DoubleProperty upperBoundXProperty() {
        if (null == upperBoundX) {
            upperBoundX = new DoublePropertyBase(_upperBoundX) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "upperBoundX";
                }
            };
        }
        return upperBoundX;
    }

    public double getLowerBoundY() {
        return null == lowerBoundY ? _lowerBoundY : lowerBoundY.get();
    }

    public void setLowerBoundY(final double VALUE) {
        if (null == lowerBoundY) {
            _lowerBoundY = VALUE;
            redraw();
        } else {
            lowerBoundY.set(VALUE);
        }
    }

    public DoubleProperty lowerBoundYProperty() {
        if (null == lowerBoundY) {
            lowerBoundY = new DoublePropertyBase(_lowerBoundY) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "lowerBoundY";
                }
            };
        }
        return lowerBoundY;
    }

    public double getUpperBoundY() {
        return null == upperBoundY ? _upperBoundY : upperBoundY.get();
    }

    public void setUpperBoundY(final double VALUE) {
        if (null == upperBoundY) {
            _upperBoundY = VALUE;
            redraw();
        } else {
            upperBoundY.set(VALUE);
        }
    }

    public DoubleProperty upperBoundYProperty() {
        if (null == upperBoundY) {
            upperBoundY = new DoublePropertyBase(_upperBoundY) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "upperBoundY";
                }
            };
        }
        return upperBoundY;
    }

    public double getLowerBoundZ() {
        return null == lowerBoundZ ? _lowerBoundZ : lowerBoundZ.get();
    }

    public void setLowerBoundZ(final double VALUE) {
        if (null == lowerBoundZ) {
            _lowerBoundZ = VALUE;
            redraw();
        } else {
            lowerBoundZ.set(VALUE);
        }
    }

    public DoubleProperty lowerBoundZProperty() {
        if (null == lowerBoundZ) {
            lowerBoundZ = new DoublePropertyBase(_lowerBoundZ) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "lowerBoundZ";
                }
            };
        }
        return lowerBoundZ;
    }

    public double getUpperBoundZ() {
        return null == upperBoundZ ? _upperBoundZ : upperBoundZ.get();
    }

    public void setUpperBoundZ(final double VALUE) {
        if (null == upperBoundZ) {
            _upperBoundZ = VALUE;
            redraw();
        } else {
            upperBoundZ.set(VALUE);
        }
    }

    public DoubleProperty upperBoundZProperty() {
        if (null == upperBoundZ) {
            upperBoundZ = new DoublePropertyBase(_upperBoundZ) {
                @Override
                protected void invalidated() {
                    redraw();
                }

                @Override
                public Object getBean() {
                    return CustomMatrixPane.this;
                }

                @Override
                public String getName() {
                    return "upperBoundZ";
                }
            };
        }
        return upperBoundZ;
    }

    public double getRangeX() {
        return getUpperBoundX() - getLowerBoundX();
    }

    public double getRangeY() {
        return getUpperBoundY() - getLowerBoundY();
    }

    public double getRangeZ() {
        return getUpperBoundZ() - getLowerBoundZ();
    }

    public double getValueAt(final int X, final int Y) throws Exception {
        if (null == getSeries()) {
            throw new Exception("Series is null");
        }
        if (getSeries().getItems().isEmpty()) {
            throw new Exception("Series is empty");
        }
        return getSeries().getAt(X, Y);
    }

    public void setValueAt(final int X, final int Y, final double Z) {
        if (null != getSeries()) {
            minZ = Math.min(minZ, Z);
            maxZ = Math.max(maxZ, Z);
            rangeZ = maxZ - minZ;

            Color color = Helper.getColorAt(matrixGradient, Z / rangeZ);
            matrix.setPixel(X, Y, color);
        }
    }

    public MatrixItemSeries<T> getSeries() {
        return series;
    }

    public PixelMatrix getMatrix() {
        return matrix;
    }

    public void setColorMapping(final ColorMapping MAPPING) {
        setMatrixGradient(MAPPING.getGradient());
    }

    public LinearGradient getMatrixGradient() {
        return matrixGradient;
    }

    public void setMatrixGradient(final LinearGradient GRADIENT) {
        matrixGradient = GRADIENT;
        drawChart();
    }


    // ******************** Draw Chart ****************************************
    private void drawChart() {
        if (null == series || series.getItems().isEmpty()) return;

        final ChartType TYPE = series.getChartType();

        switch (TYPE) {
            case MATRIX_HEATMAP:
                drawMatrixHeatMap(series);
                break;
        }
    }

    private void drawMatrixHeatMap(final MatrixItemSeries<T> SERIES) {
        minZ = SERIES.getItems().stream().mapToDouble(MatrixItem::getZ).min().getAsDouble();
        maxZ = SERIES.getItems().stream().mapToDouble(MatrixItem::getZ).max().getAsDouble();
        rangeZ = maxZ - minZ;

        SERIES.getItems().forEach(data -> {
            Color color = Helper.getColorAt(matrixGradient, data.getZ() / rangeZ);
            matrix.setPixel(data.getX(), data.getY(), color);
        });
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (keepAspect) {
            if (aspectRatio * width > height) {
                width = 1 / (aspectRatio / height);
            } else if (1 / (aspectRatio / height) > width) {
                height = aspectRatio * width;
            }
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            matrix.setPrefSize(width, height);

            scaleX = width / getRangeX();
            scaleY = height / getRangeY();

            redraw();
            resizeAdditionalStuff();
        }
    }

    private void resizeAdditionalStuff() {

        double pixelHeight = height / getMatrix().getRows();
        double pixelWidth = width / getMatrix().getCols();
        double spacerSizeFactor = getMatrix().getSpacerSizeFactor();
        double pixelSize = Math.min(pixelWidth, pixelHeight);
        double spacer = pixelSize * spacerSizeFactor;

        double leftAxisWidth = 0;
        double rightAxisWidth = 0;

        for (Node node2 : getLeftAxis().getChildren()) {
            if (node2 instanceof Label) {
                boolean isOk = false;
                double newHeight = pixelHeight - 2;
                Font font = ((Label) node2).getFont();
                if (newHeight > 0 && newHeight < 13) {
                    final Label test = new Label(((Label) node2).getText());
                    test.setFont(font);
                    while (!isOk) {
                        double height1 = test.getLayoutBounds().getHeight();
                        if (height1 > pixelHeight - 2) {
                            newHeight = newHeight - 0.05;
                            test.setFont(new Font(font.getName(), newHeight));
                        } else {
                            isOk = true;
                        }
                    }
                }

                if (newHeight < 12) {
                    ((Label) node2).setFont(new Font(font.getName(), newHeight));
                }

                ((Label) node2).setPrefHeight(pixelHeight);

                final Label test = new Label(((Label) node2).getText());
                test.setFont(((Label) node2).getFont());
                double newWidth = test.getLayoutBounds().getWidth();

                leftAxisWidth = Math.max(newWidth, getLeftAxis().getLayoutBounds().getWidth());
            }
        }

        for (Node node2 : getRightAxis().getChildren()) {
            if (node2 instanceof Label) {
                boolean isOk = false;
                double newHeight = pixelHeight - 2;
                Font font = ((Label) node2).getFont();
                if (newHeight > 0 && newHeight < 13) {
                    final Label test = new Label(((Label) node2).getText());
                    test.setFont(font);
                    while (!isOk) {
                        double height1 = test.getLayoutBounds().getHeight();
                        if (height1 > pixelHeight - 2) {
                            newHeight = newHeight - 0.05;
                            test.setFont(new Font(font.getName(), newHeight));
                        } else {
                            isOk = true;
                        }
                    }
                }

                if (newHeight < 12) {
                    ((Label) node2).setFont(new Font(font.getName(), newHeight));
                }

                ((Label) node2).setPrefHeight(pixelHeight);

                final Label test = new Label(((Label) node2).getText());
                test.setFont(((Label) node2).getFont());
                double newWidth = test.getLayoutBounds().getWidth();

                rightAxisWidth = Math.max(newWidth, getRightAxis().getLayoutBounds().getWidth());
            }
        }

        List<DateTime> xAxisList = getXAxisList();
        String X_FORMAT = getXFormat();

        bottomXAxis.setWidth(leftAxisWidth + width + rightAxisWidth);
        GraphicsContext gc = bottomXAxis.getGraphicsContext2D();
        gc.clearRect(0, 0, bottomXAxis.getWidth(), bottomXAxis.getHeight());
        double x = leftAxisWidth + 4 + spacer + pixelWidth / 2;

        for (DateTime dateTime : xAxisList) {
            String ts = "";
            if (!X_FORMAT.isEmpty()) {
                ts = dateTime.toString(X_FORMAT);
            }
            Text text = new Text(ts);
            Font helvetica = Font.font("Helvetica", 12);
            text.setFont(helvetica);

            final double textWidth = text.getLayoutBounds().getWidth();
            final double textHeight = text.getLayoutBounds().getHeight();

            gc.setFont(helvetica);

            if (dateTime.getMinuteOfHour() == 0) {

                gc.fillRect(x, 0, 2, 10);
                gc.fillText(ts, x - textWidth / 2, 10 + textHeight + 2);

            } else if (dateTime.getMinuteOfHour() % 15 == 0) {
                gc.fillRect(x, 0, 1, 7);
            }

            x += pixelWidth;
        }

        setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                Node node = (Node) t.getSource();
                NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                for (Node node1 : getMatrix().getChildren()) {
                    if (node1 instanceof Canvas) {
                        Canvas canvas = (Canvas) node1;
                        // listen to only events within the canvas
                        final Point2D mouseLoc = new Point2D(t.getScreenX(), t.getScreenY());
                        final Bounds screenBounds = canvas.localToScreen(canvas.getBoundsInLocal());

                        double pixelHeight = getMatrix().getPixelHeight();
                        double pixelWidth = getMatrix().getPixelWidth();
                        double spacerSizeFactor = getMatrix().getSpacerSizeFactor();
                        double width = getMatrix().getWidth() - getMatrix().getInsets().getLeft() - getMatrix().getInsets().getRight();
                        double height = getMatrix().getHeight() - getMatrix().getInsets().getTop() - getMatrix().getInsets().getBottom();
                        double pixelSize = Math.min((width / getMatrix().getCols()), (height / getMatrix().getRows()));
                        double spacer = pixelSize * spacerSizeFactor;
                        double pixelWidthMinusDoubleSpacer = pixelWidth - spacer * 2;
                        double pixelHeightMinusDoubleSpacer = pixelHeight - spacer * 2;

                        double spacerPlusPixelWidthMinusDoubleSpacer = spacer + pixelWidthMinusDoubleSpacer;
                        double spacerPlusPixelHeightMinusDoubleSpacer = spacer + pixelHeightMinusDoubleSpacer;

                        if (screenBounds.contains(mouseLoc)) {
                            for (int y = 0; y < getMatrix().getRows(); y++) {
                                for (int x = 0; x < getMatrix().getCols(); x++) {
                                    if (Helper.isInRectangle(t.getX(), t.getY(), x * pixelWidth + spacer, y * pixelHeight + spacer, x * pixelWidth + spacerPlusPixelWidthMinusDoubleSpacer, y * pixelHeight + spacerPlusPixelHeightMinusDoubleSpacer)) {
                                        Double value = null;
                                        for (Map.Entry<MatrixXY, Double> entry : getMatrixData().entrySet()) {
                                            MatrixXY matrixXY = entry.getKey();
                                            if (matrixXY.getY() == y && matrixXY.getX() == x) {
                                                value = entry.getValue();
                                                break;
                                            }
                                        }

                                        if (value != null) {
                                            Double finalValue = value;
                                            Platform.runLater(() -> {
                                                try {
                                                    tp.setText(nf.format(finalValue) + " " + getUnit());

                                                    tp.show(node, getScene().getWindow().getX() + t.getSceneX(), getScene().getWindow().getY() + t.getSceneY());
                                                } catch (Exception np) {
                                                    logger.warn(np);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        } else if (tp.isShowing()) {
                            Platform.runLater(tp::hide);
                        }
                    }
                }
            }
        });
    }

    private void redraw() {
        drawChart();
    }

    public List<DateTime> getXAxisList() {
        return XAxisList;
    }

    public void setXAxisList(List<DateTime> xAxisList) {
        this.XAxisList = xAxisList;
    }

    public String getXFormat() {
        return XFormat;
    }

    public void setXFormat(String xFormat) {
        this.XFormat = xFormat;
    }

    public Map<MatrixXY, Double> getMatrixData() {
        return matrixData;
    }

    public void setMatrixData(Map<MatrixXY, Double> matrixData) {
        this.matrixData = matrixData;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public GridPane getLeftAxis() {
        return leftAxis;
    }

    public void setLeftAxis(GridPane leftAxis) {
        this.leftAxis = leftAxis;
    }

    public GridPane getRightAxis() {
        return rightAxis;
    }

    public void setRightAxis(GridPane rightAxis) {
        this.rightAxis = rightAxis;
    }

    public Canvas getBottomXAxis() {
        return bottomXAxis;
    }

    public void setBottomXAxis(Canvas bottomXAxis) {
        this.bottomXAxis = bottomXAxis;
    }
}
