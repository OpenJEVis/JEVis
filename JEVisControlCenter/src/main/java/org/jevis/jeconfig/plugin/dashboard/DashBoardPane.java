package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.*;
import java.util.stream.Collectors;

public class DashBoardPane extends Pane {


    private static final Logger logger = LogManager.getLogger(DashBoardPane.class);
    private final DashboardPojo analysis;
//    private ObservableList<Widget> widgetList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

//    private List<Widget> widgetList = new ArrayList<>();

    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();

    private List<Line> visibleGrid = new ArrayList<>();

    private final JEVisDataSource jeVisDataSource;
    private final DashboardControl control;
    private final Background defaultBackground;

    public DashBoardPane(DashboardControl control) {
        super();
        this.defaultBackground = getBackground();

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
                        System.out.println("Selected: " + shape);
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


    private <T> Set<T> findDuplicates(Collection<T> collection) {
        Set<T> uniques = new HashSet<>();
        return collection.stream()
                .filter(e -> !uniques.add(e))
                .collect(Collectors.toSet());
    }

    public void clearView() {

        getChildren().clear();
        setBackground(this.defaultBackground);

        setZoom(1d);
        requestLayout();

    }

    public boolean getSnapToGrid() {
        return this.control.showSnapToGridProperty.getValue();
    }

    public void updateView() {
        Platform.runLater(() -> {
            try {
                loadSetting(this.control.getActiveDashboard());
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
        logger.error("Load dashboard: {}", analysis);


        setSize(analysis.getSize());
        createGrid(this.analysis.xGridInterval, this.analysis.yGridInterval);

//        this.scale.setX(analysis.getZoomFactor());
//        this.scale.setY(analysis.getZoomFactor());
    }

    public void addWidget(Widget widget) {
        Platform.runLater(() -> {
            try {
                if (!getChildren().contains(widget)) {
                    getChildren().add(widget);
                    widget.setVisible(true);
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


    public void zoomToParent(double parentWidth, double parentHeight) {
        double scaleFactorWidth = parentWidth / getWidth();
        double scaleFactorHeight = parentHeight / getHeight();

        if (Double.isFinite(scaleFactorWidth) && Double.isFinite(scaleFactorHeight)) {
//            System.out.println("w/h " + parentWidth + "/" + getWidth() + "    " + parentHeight + "/" + getHeight());
//            System.out.println("Scale: " + scaleFactorWidth + " / " + scaleFactorHeight);
            this.scale.setX(scaleFactorWidth);
            this.scale.setY(scaleFactorHeight);
        }

    }

    public void setZoom(double zoom) {
        logger.debug("Set Zoom: {}", zoom);
        this.scale.setX(zoom);
        this.scale.setY(zoom);
//        getTransforms().add(this.scale);
//        requestLayout();
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
                    Double lineStartX;
                    Double lineStartY;
                    double lenght = 0;
                    double height = 0;

                    if (popup.isShowing()) {
                        text = String.format("x: %s -> %s [%s]\ny: %s -> %s [%s]"
                                , lastAltX.get(), event.getX(), (lastAltX.get() - event.getX())
                                , lastAltY.get(), event.getY(), (lastAltY.get() - event.getY()));

                        /**
                         * Not working red line
                         */
//                        if(event.getX()>lastAltX.get()){
//                            lenght=event.getX()-lastAltX.get();
//                            lineStartX=lastAltX.get();
//                        }else{
//                            lenght=lastAltX.get()-event.getX();
//                            lineStartX=event.getX();
//                        }
//
//                        if(event.getY()>lastAltY.get()){
//                            height=event.getY()-lastAltY.get();
//                            lineStartY=lastAltY.get();
//                        }else{
//                            height=lastAltY.get()-event.getY();
//                            lineStartY=event.getY();
//                        }
//
//
//                        Line line = new Line();
//                        line.setStartX(0);
//                        line.setStartY(0);
//                        line.setEndX(lenght);
//                        line.setEndY(height);
//                        line.setFill(Color.RED);
//
//                        linePopup.getContent().setAll(line);
//                        popup.show(DashBoardPane.this,event.getScreenX(),event.getScreenY()-20);

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


    private void setSize(Size newValue) {
        logger.error("setSize: {}/{}", newValue.getWidth(), newValue.getHeight());
        this.setMaxWidth(newValue.getWidth());
        this.setMinWidth(newValue.getWidth());
        this.setPrefWidth(newValue.getWidth());
        this.setMaxHeight(newValue.getHeight());
        this.setMinHeight(newValue.getHeight());
        this.setPrefHeight(newValue.getHeight());
    }

    /**
     * TODo support diffreent modes like repeat , stretch
     *
     * @param image
     */
    public void setBackgroundImage(Image image) {
        final BackgroundSize backgroundSize = new BackgroundSize(image.getWidth(), image.getHeight(), false, false, false, false);

        final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        final Background background = new Background(backgroundImage);
        logger.error("SetBackground: {}/{} {}/{}", image.getWidth(), image.getHeight(), this.getWidth(), this.getHeight());
        setBackground(background);
    }


    public void activateGrid(boolean show) {
//        if (show) {
//            createGrid(this.analysis.xGridInterval, this.analysis.yGridInterval);
////            DashBoardPane.this.getChildren().addAll(this.visibleGrid);
//        } else {
////            DashBoardPane.this.getChildren().removeAll(this.visibleGrid);
//            visibleGrid.clear();
//        }
    }

    public void showGrid(boolean show) {

        if (show) {
//            activateGrid(true);
//            createGrid(this.analysis.xGridInterval, this.analysis.yGridInterval);
            DashBoardPane.this.getChildren().addAll(this.visibleGrid);
        } else {
            DashBoardPane.this.getChildren().removeAll(this.visibleGrid);
//            visibleGrid.clear();
        }
    }

    /**
     * Add an grid to the pane.
     * <p>
     * TODO: make the grid bigger than the visible view, so the user can zoom out an still has an grid
     *
     * @param xWidth
     * @param height
     */
    public void createGrid(double xWidth, double height) {
        int maxColumns = Double.valueOf(DashBoardPane.this.getWidth() / xWidth).intValue() + 1;
        int maxRows = Double.valueOf(DashBoardPane.this.getHeight() / height).intValue() + 1;
        double opacity = 0.4;
        Double[] strokeDashArray = new Double[]{4d};
        this.xGrids.clear();
        this.yGrids.clear();
        this.visibleGrid.clear();

        /** rows **/
        for (int i = 0; i < maxColumns; i++) {
            double xPos = i * xWidth;
            this.xGrids.add(xPos);


            Line line = new Line();
            line.setStartX(xPos);
            line.setStartY(0.0f);
            line.setEndX(xPos);
            line.setEndY(DashBoardPane.this.getHeight());
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }

        /** columns **/
        for (int i = 0; i < maxRows; i++) {
            double yPos = i * height;
            this.yGrids.add(yPos);

            Line line = new Line();
            line.setStartX(0);
            line.setStartY(yPos);
            line.setEndX(DashBoardPane.this.getWidth());
            line.setEndY(yPos);
            line.getStrokeDashArray().addAll(strokeDashArray);
            line.setOpacity(opacity);
            this.visibleGrid.add(line);

        }
    }


    public double getNextGridX(double xPos) {
        if (!this.analysis.getSnapToGrid()) {
            return xPos;
        }
        double c = this.xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElse(xPos);

//        System.out.println("Next xPos: " + c);
        return c;
    }

    public double getNextGridY(double yPos) {
        if (!this.analysis.getSnapToGrid()) {
            return yPos;
        }
        double c = this.yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElse(yPos);

//        System.out.println("Next yPos: " + c);
        return c;
    }


    private void printChildren() {
//        System.out.println("---");
//        for (Widget node : this.widgetList) {
//            String id = "";
//
//            if (node instanceof Widget) {
//                id = ((Widget) node).getId();
//            }
//
//            System.out.println("Child: " + id + "  " + node.getClass() + " " + node);
//        }
    }


}
