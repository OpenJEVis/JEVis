package org.jevis.jeconfig.plugin.Dashboard;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordAnalysis;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.WidgetData;

import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class DashBoardPane extends Pane {

    //    private GridLayer gridLayer = new GridLayer();
    private static final Logger logger = LogManager.getLogger(DashBoardPane.class);


    private DashBordAnalysis analysis = new DashBordAnalysis();

    private ObservableList<Widget> widgetList = FXCollections.observableArrayList();
    private List<Double> xGrids = new ArrayList<>();
    private List<Double> yGrids = new ArrayList<>();
    private Scale scale = new Scale();
    private Thread updateThread;


    public DashBoardPane() {
        super();

//        setStyle("-fx-background-color : lightblue;");
//        setBackground();//TODO: in load config
        addConfigListener();
        ChangeListener sizeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateChildren();
            }
        };
        this.heightProperty().addListener(sizeListener);
        this.widthProperty().addListener(sizeListener);


        getTransforms().add(scale);
        setOnScroll(event -> {
            if (event.isControlDown()) {

                if (event.getDeltaY() < 0) {
                    analysis.zoomOut();
                } else {
                    analysis.zoomIn();
                }
            }
        });

    }

    private void addConfigListener() {
        analysis.zoomFactor.addListener((observable, oldValue, newValue) -> {
            System.out.println("Change zoom to: " + newValue);
            scale.setX(newValue.doubleValue());
            scale.setY(newValue.doubleValue());
            requestLayout();
        });
        analysis.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Config changed update ui");
                updateChildren();
            }
        });

        analysis.imageBoardBackground.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || !oldValue.equals(newValue))) {
                setBackground();
            }
        });

        analysis.updateRate.addListener((observable, oldValue, newValue) -> {
            restartUpdater();
        });

        analysis.updateIsRunning.addListener((observable, oldValue, newValue) -> {
            if (newValue && updateThread != null && updateThread.isAlive()) {
                //nothing to do
            } else if (newValue && updateThread != null && updateThread.isInterrupted()) {
                //restart existing
                updateThread.start();
            } else if (!newValue) {
                if (updateThread != null && updateThread.isAlive()) {
                    //stop running
                    updateThread.interrupt();
                }
            }
        });

//        analysis.

//        analysis.showGridProperty.addListener((observable, oldValue, newValue) -> {
//            if (!oldValue.equals(newValue)) {
//                updateChildren();
//            }
//        });
//        analysis.showGridProperty.addListener((observable, oldValue, newValue) -> {
//            if (!oldValue.equals(newValue)) {
//                updateChildren();
//            }
//        });
//        analysis.xGridInterval.addListener((observable, oldValue, newValue) -> {
//            if (!oldValue.equals(newValue)) {
//                updateChildren();
//            }
//        });
//        analysis.yGridInterval.addListener((observable, oldValue, newValue) -> {
//            if (!oldValue.equals(newValue)) {
//                updateChildren();
//            }
//        });

    }

    private void setBackground() {
        Background colorBackground = new Background(new BackgroundFill(analysis.colorDashBoardBackground.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
//        analysis.colorDashBoardBackground.addListener((observable, oldValue, newValue) -> {
//            setBackground(new Background(new BackgroundFill(newValue, CornerRadii.EMPTY, Insets.EMPTY)));
//        });
//        final Image overlay = overlayImages.computeIfAbsent(
//                imageName,
//                image -> {
//                    return this.load(OverlayLoader.class, image, width, height);
//                });

//        final Image image = JEConfig.getImage("Dashbord.jpg");
        try {
            final Image image = analysis.imageBoardBackground.getValue();

            final BackgroundSize backgroundSize = new BackgroundSize(image.getWidth(), image.getHeight(), false, false, false, false);
            final BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            final Background background = new Background(backgroundImage);
            setBackground(background);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void restartUpdater() {
        stopUpdater();
        startUpdater();
    }

    public void stopUpdater() {
        if (updateThread != null && updateThread.isAlive()) {
            try {
                updateThread.interrupt();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    public void startUpdater() {


        Runnable runnable = () -> {
            try {
                logger.info("Starting Update");
                String name = Thread.currentThread().getName();
                /**
                 * TODO: for widgetList -> update
                 */
                logger.info("Update finished waiting {} sec", analysis.updateRate.getValue());
                TimeUnit.SECONDS.sleep(analysis.updateRate.getValue());


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        updateThread = thread;
    }

    public void setGrid(double xWidth, double height) {
        int maxColumns = Double.valueOf(DashBoardPane.this.getWidth() / xWidth).intValue() + 1;
        int maxRows = Double.valueOf(DashBoardPane.this.getHeight() / height).intValue() + 1;
        double opacity = 0.8;
        Double[] strokeDashArray = new Double[]{4d};
        xGrids.clear();
        yGrids.clear();

        for (int i = 0; i < maxColumns; i++) {
            double xPos = i * xWidth;
            xGrids.add(xPos);
            if (analysis.showGridProperty.getValue()) {
                Line line = new Line();
                line.setStartX(xPos);
                line.setStartY(0.0f);
                line.setEndX(xPos);
                line.setEndY(DashBoardPane.this.getHeight());
                line.getStrokeDashArray().addAll(strokeDashArray);
                DashBoardPane.this.getChildren().add(line);
                line.setOpacity(opacity);
            }

        }

        for (int i = 0; i < maxRows; i++) {
            double yPos = i * height;
            yGrids.add(yPos);
            if (analysis.showGridProperty.getValue()) {
                Line line = new Line();
                line.setStartX(0);
                line.setStartY(yPos);
                line.setEndX(DashBoardPane.this.getWidth());
                line.setEndY(yPos);
                line.getStrokeDashArray().addAll(strokeDashArray);
                line.setOpacity(opacity);
                DashBoardPane.this.getChildren().add(line);
            }
        }

    }


    public void addNode(Widget widget, WidgetConfig config) {
        System.out.println("Add widget: " + config.getType());
        widgetList.add(widget);
        widget.setDashBoard(this);
        widget.init();
        widget.update(new WidgetData(), true);

        getChildren().add(widget);
    }

    public double getNextGridX(double xPos) {
        double c = xGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - xPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        System.out.println("Next xPos: " + c);
        return c;
    }

    public double getNextGridY(double yPos) {
        double c = yGrids.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - yPos)))
                .orElseThrow(() -> new NoSuchElementException("No value present"));
        System.out.println("Next yPos: " + c);
        return c;
    }

    public DashBordAnalysis getDashBordAnalysis() {
        return analysis;
    }

    public void updateChildren() {
        System.out.println("Update");
        getChildren().clear();
        setGrid(analysis.xGridInterval.get(), analysis.yGridInterval.get());


        widgetList.forEach(node -> {
            getChildren().add(node);
        });

//        getChildren().clear();s
//        getChildren().add(gridLayer);
//        widgetList.forEach(node -> {
//            getChildren().add(node);
//        });
    }

}
