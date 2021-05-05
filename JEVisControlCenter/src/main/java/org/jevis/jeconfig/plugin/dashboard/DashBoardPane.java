package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.ScrollPanes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class DashBoardPane extends Pane {


    private static final Logger logger = LogManager.getLogger(DashBoardPane.class);
    private DashboardPojo analysis;
    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();
    private List<Line> visibleGrid = new ArrayList<>();
    private final JEVisDataSource jeVisDataSource;
    private final DashboardControl control;
    private final Background defaultBackground;
    private boolean gridIsVisible = false;

    /**
     * Dummy Pane fif no Dashboard is loaded
     **/
    public DashBoardPane() {
        jeVisDataSource = null;
        control = null;
        defaultBackground = null;
        setBorder(null);
    }

    public DashBoardPane(DashboardControl control) {
        super();
        this.defaultBackground = getBackground();
        this.setStyle("-fx-focus-color: transparent;");
//        setStyle("-fx-background-color: orange;");
        logger.debug("Start DashBoardPane: {}", control.getActiveDashboard());
        this.jeVisDataSource = control.getDataSource();
        this.control = control;
        this.analysis = control.getActiveDashboard();
        getTransforms().add(this.scale);
        this.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() < 0) {
                    this.control.zoomOut();
                } else {
                    this.control.zoomIn();
                }
            }
        });
        addPopUpFunctions();

        setOnMouseClicked(event -> {
            control.setSelectedWidgets(new ArrayList<>());
        });

//        addMouseSelectionGesture();
    }

    private void addMouseSelectionGesture() {
        final Rectangle selectionRect = new Rectangle(20, 20, Color.TRANSPARENT);
        selectionRect.setStroke(Color.BLACK);

        EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (Node shape : DashBoardPane.this.getChildren()) {
                    if (selectionRect.getBoundsInParent().intersects(shape.getBoundsInParent())) {
//                        shape.setFill(Color.RED);
//                        if(!this.selected.contains(shape))
//                        System.out.println("Selected: " + shape);
//                            this.selected.add(shape);
                    } else {
//                        shape.setFill(Color.BLACK);
//                        this.selected.remove(shape);
                    }

                }
            }
        };

        /** does not work with the current java/jfxtras version **/
        //MouseControlUtil.addSelectionRectangleGesture(this, selectionRect, null, null, mouseReleaseHandler);

    }


    public void clearView() {
        getChildren().clear();
        setBackground(this.defaultBackground);
        Region filler = new Region();
        Layouts.setAnchor(filler, 0);
        getChildren().add(filler);
        setZoom(1d);
        requestLayout();

    }

    public boolean getSnapToGrid() {
        return this.control.showGridProperty.getValue();
    }

    public void updateView() {
        Platform.runLater(() -> {
            try {
                loadSetting(this.control.getActiveDashboard());
                ScrollPanes.resetParentScrollView(this);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    public void loadSetting(DashboardPojo analysis) {
        if (analysis == null) {
            logger.error("setSize=enty analysis");
            return;
        }
        logger.debug("Load dashboard: {}", analysis);
        this.analysis = analysis;

        setSize(analysis.getSize());
    }


    public void redrawWidgets(List<Widget> widgetList) {

        Platform.runLater(() -> {
            getChildren().clear();
        });

        sortWidgets(widgetList);
        widgetList.forEach(widget -> {
            addWidget(widget);
        });


    }

    private void sortWidgets(List<Widget> widgetList) {
        widgetList.sort(new Comparator<Widget>() {
            @Override
            public int compare(Widget o1, Widget o2) {
                try {
                    int layerC = o1.getConfig().getLayer().compareTo(o2.config.getLayer());
                    if (layerC == 0) {
                        return Integer.compare(o1.getConfig().getUuid(), o2.getConfig().getUuid());
                    } else {
                        return layerC;
                    }
                } catch (Exception ex) {
                    return 1;
                }
            }
        });
    }


    public void addWidget(Widget widget) {
        Platform.runLater(() -> {
            try {
                if (!getChildren().contains(widget)) {
                    getChildren().add(widget);
                    widget.setVisible(true);

                    if (gridIsVisible) {
                        visibleGrid.forEach(line -> {
                            line.toFront();
                        });
                    }

                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

    }

    public void removeWidget(Widget widget) {
        this.getChildren().remove(widget);
    }

    public synchronized void removeAllWidgets(Collection<Widget> elements) {
        this.getChildren().remove(elements);
    }


    public void setScale(double x, double y) {
        logger.debug("Set Zoom: x:{} y:{}", x, y);
        Platform.runLater(() -> {
            getTransforms().removeAll(this.scale);
            this.scale.setY(y);
            this.scale.setX(x);
            getTransforms().addAll(this.scale);
        });
    }


    public void setZoom(double zoom) {
        setScale(zoom, zoom);
    }


    private void addPopUpFunctions() {
        Popup popup = new Popup();
        Popup linePopup = new Popup();

        final DoubleProperty lastAltX = new SimpleDoubleProperty(0d);
        final DoubleProperty lastAltY = new SimpleDoubleProperty(0d);

        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isAltDown()) {
                    String text;
                    if (popup.isShowing()) {
                        text = String.format("x: %s -> %s [%s]\ny: %s -> %s [%s]"
                                , lastAltX.get(), event.getX(), (lastAltX.get() - event.getX())
                                , lastAltY.get(), event.getY(), (lastAltY.get() - event.getY()));

                    } else {
                        text = String.format("x: %s\ny: %s", event.getX(), event.getY());
                    }


                    Label label = new Label(text);
                    popup.getContent().setAll(label);
                    lastAltX.setValue(event.getX());
                    lastAltY.setValue(event.getY());

                    popup.show(DashBoardPane.this, event.getScreenX(), event.getScreenY() - 20);
                } else {
                    popup.hide();
                }
            }
        });
    }


    public void setSize(Size newValue) {
        logger.debug("setSize: {}/{}", newValue.getWidth(), newValue.getHeight());
        this.setMaxWidth(newValue.getWidth());
        this.setMinWidth(newValue.getWidth());
        this.setPrefWidth(newValue.getWidth());
        this.setMaxHeight(newValue.getHeight());
        this.setMinHeight(newValue.getHeight());
        this.setPrefHeight(newValue.getHeight());
        createGrid(analysis.xGridInterval, analysis.yGridInterval);
    }

//    /**
//     * TODo support diffreent modes like repeat , stretch
//     *
//     * @param image
//     */
//    public void setBackgroundImage(Image image) {
//        final BackgroundSize backgroundSize = new BackgroundSize(image.getWidth(), image.getHeight(), false, false, false, false);
//
//        final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
//                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
//        final Background background = new Background(backgroundImage);
//        logger.debug("SetBackground: {}/{} {}/{}", image.getWidth(), image.getHeight(), this.getWidth(), this.getHeight());
//        setBackground(background);
//    }


    public void showGrid(boolean show) {
        gridIsVisible = show;
        Platform.runLater(() -> {
            if (show) {
                if (!DashBoardPane.this.getChildren().contains(visibleGrid.get(0))) {
                    DashBoardPane.this.getChildren().addAll(this.visibleGrid);
                }
            } else {
                DashBoardPane.this.getChildren().removeAll(this.visibleGrid);
            }

        });

    }

    /**
     * Add an grid to the pane.
     * <p>
     * TODO: make the grid bigger than the visible view, so the user can zoom out an still has an grid
     *
     * @param xGridInterval
     * @param yGridInterval
     */
    public void createGrid(double xGridInterval, double yGridInterval) {
        logger.debug("createGrid: {},{}", xGridInterval, yGridInterval);
        getChildren().removeAll(visibleGrid);


        double totalHeight = analysis.getSize().getHeight();
        double totalWidth = analysis.getSize().getWidth();
        int maxColumns = Double.valueOf(totalWidth / xGridInterval).intValue() + 1;
        int maxRows = Double.valueOf(totalHeight / yGridInterval).intValue() + 1;
        double opacity = 0.4;
        Double[] strokeDashArray = new Double[]{4d};

        this.xGrids.clear();
        this.yGrids.clear();
        this.visibleGrid.clear();

        /** rows **/
        for (int i = 0; i < maxColumns; i++) {
            double xPos = i * xGridInterval;
            this.xGrids.add(xPos);

            Line line = new Line();
            line.setId("grid");
            line.setStartX(xPos);
            line.setStartY(0.0f);
            line.setEndX(xPos);
            line.setEndY(totalHeight);
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }

        /** columns **/
        for (int i = 0; i < maxRows; i++) {
            double yPos = i * yGridInterval;
            this.yGrids.add(yPos);

            Line line = new Line();
            line.setId("grid");
            line.setStartX(0);
            line.setStartY(yPos);
            line.setEndX(totalWidth);
            line.setEndY(yPos);
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }
    }


    public double getNextGridX(double xPos) {
        if (!this.control.snapToGridProperty.get()) {
            return xPos;
        }
        double c = this.xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElse(xPos);
        return c;
    }

    public double getNextGridY(double yPos) {
        if (!this.control.snapToGridProperty.get()) {
            return yPos;
        }
        double c = this.yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElse(yPos);
        return c;
    }


}
