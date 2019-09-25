package org.jevis.jeconfig.plugin.reports;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.*;

public class ReportPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(ReportPlugin.class);
    public static String PLUGIN_NAME = "Report Plugin";
    public static String REPORT_CLASS = "Periodic Report";
    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane;
    private final ObjectRelations objectRelations;
    private final ToolBar toolBar;
    private boolean initialized = false;

    public ReportPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.borderPane = new BorderPane();
        this.toolBar = new ToolBar();

        initToolBar();

        this.objectRelations = new ObjectRelations(ds);
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", 20, 20));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {
            initialized = false;
            ds.clearCache();
            try {
                ds.preload();
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            getContentNode();
        });

        toolBar.getItems().add(reload);
    }

    @Override
    public String getClassName() {
        return "Report Plugin";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public StringProperty uuidProperty() {
        return null;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public Node getToolbar() {
        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {

    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        if (!initialized) {
            init();
        }
        return borderPane;
    }

    private void init() {
        SplitPane sp = new SplitPane();
        sp.setDividerPositions(.3d);
        sp.setOrientation(Orientation.HORIZONTAL);
        sp.setId("mainsplitpane");
        sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        ObservableList<JEVisObject> reports = FXCollections.observableArrayList(getAllReports());
        AlphanumComparator ac = new AlphanumComparator();
        reports.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        ListView<JEVisObject> listView = new ListView<>(reports);
        listView.setPrefWidth(250);
        setupCellFactory(listView);

        BorderPane view = new BorderPane();
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                loadReport(view, newValue);
            }
        });

        sp.getItems().setAll(listView, view);

        GridPane.setFillWidth(view, true);
        GridPane.setFillHeight(listView, true);
        GridPane.setFillHeight(view, true);

        borderPane.setCenter(sp);

        initialized = true;
    }

    private void loadReport(BorderPane view, JEVisObject reportObject) {
        JEVisAttribute lastReportPDFAttribute = null;
        try {
            lastReportPDFAttribute = reportObject.getAttribute("Last Report PDF");
        } catch (JEVisException e) {
            logger.error("Could not get 'Last Report' Attribute from object {}:{}", reportObject.getName(), reportObject.getID(), e);
        }

        if (lastReportPDFAttribute != null) {
            List<JEVisSample> allSamples = lastReportPDFAttribute.getAllSamples();
            if (allSamples.size() > 0) {
                JEVisSample lastSample = allSamples.get(allSamples.size() - 1);
                Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                List<DateTime> dateTimeList = new ArrayList<>();
                for (JEVisSample jeVisSample : allSamples) {
                    try {
                        dateTimeList.add(jeVisSample.getTimestamp());
                        sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                    } catch (JEVisException e) {
                        logger.error("Could not add date to dat list.");
                    }
                }
                ComboBox<DateTime> dateTimeComboBox = new ComboBox<>(FXCollections.observableList(dateTimeList));
                Callback<ListView<DateTime>, ListCell<DateTime>> cellFactory = new Callback<ListView<DateTime>, ListCell<DateTime>>() {
                    @Override
                    public ListCell<DateTime> call(ListView<DateTime> param) {
                        return new ListCell<DateTime>() {
                            @Override
                            protected void updateItem(DateTime obj, boolean empty) {
                                super.updateItem(obj, empty);
                                if (obj == null || empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    setText(obj.toString("yyyy-MM-dd HH:mm"));
                                }
                            }
                        };
                    }
                };

                dateTimeComboBox.setCellFactory(cellFactory);
                dateTimeComboBox.setButtonCell(cellFactory.call(null));

                try {
                    dateTimeComboBox.getSelectionModel().select(lastSample.getTimestamp());
                } catch (JEVisException e) {
                    logger.error("Could not get Time Stamp of last sample.");
                    dateTimeComboBox.getSelectionModel().select(dateTimeList.size() - 1);
                }

                HBox hBox = new HBox();
                hBox.setPadding(new Insets(4, 4, 4, 4));
                ImageView leftImage = JEConfig.getImage("left.png", 20, 20);
                ImageView rightImage = JEConfig.getImage("right.png", 20, 20);

                leftImage.setOnMouseClicked(event -> {
                    int i = dateTimeComboBox.getSelectionModel().getSelectedIndex();
                    if (i > 0) {
                        dateTimeComboBox.getSelectionModel().select(i - 1);
                    }
                });

                rightImage.setOnMouseClicked(event -> {
                    int i = dateTimeComboBox.getSelectionModel().getSelectedIndex();
                    if (i < sampleMap.size()) {
                        dateTimeComboBox.getSelectionModel().select(i + 1);
                    }
                });

                Separator sep1 = new Separator(Orientation.VERTICAL);
                Separator sep2 = new Separator(Orientation.VERTICAL);

                Label labelDateTimeComboBox = new Label(I18n.getInstance().getString("plugin.reports.selectionbox.label"));

                hBox.getChildren().addAll(labelDateTimeComboBox, leftImage, sep1, dateTimeComboBox, sep2, rightImage);

                view.setTop(hBox);

                WebView web = new WebView();
                WebEngine engine = web.getEngine();
                String url = JEConfig.class.getResource("/web/viewer.html").toExternalForm();

                // connect CSS styles to customize pdf.js appearance
                engine.setUserStyleSheetLocation(JEConfig.class.getResource("/web/web.css").toExternalForm());

                engine.setJavaScriptEnabled(true);
                engine.load(url);

                view.setCenter(web);

                dateTimeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        try {
//                            byte[] data = FileUtils.readFileToByteArray(new File("/path/to/another/file"));

                            byte[] bytes = sampleMap.get(newValue).getValueAsFile().getBytes();
                            String base64 = Base64.getEncoder().encodeToString(bytes);
                            engine.executeScript("openFileFromBase64('" + base64 + "')");
                        } catch (Exception e) {
                            logger.error("Could not load report for {}:{} for ts {}", reportObject.getName(), reportObject.getID(), newValue.toString(), e);
                        }
                    }
                });

                engine.getLoadWorker()
                        .stateProperty()
                        .addListener((observable, oldValue, newValue) -> {
                            if (newValue == Worker.State.SUCCEEDED) {
                                try {

                                    byte[] bytes = sampleMap.get(lastSample.getTimestamp()).getValueAsFile().getBytes();
                                    String base64 = Base64.getEncoder().encodeToString(bytes);
                                    // call JS function from Java code
                                    engine.executeScript("openFileFromBase64('" + base64 + "')");
                                } catch (Exception e) {
                                    logger.error("Could not load latest report for {}:{}", reportObject.getName(), reportObject.getID(), e);
                                }
                            }
                        });
            }
        }
    }

    private void setupCellFactory(ListView<JEVisObject> listView) {
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String prefix = objectRelations.getObjectPath(obj);

                            setText(prefix + obj.getName());
                        }

                    }
                };
            }
        };

        listView.setCellFactory(cellFactory);
    }

    private List<JEVisObject> getAllReports() {
        List<JEVisObject> list = new ArrayList<>();
        JEVisClass reportClass = null;
        try {
            reportClass = ds.getJEVisClass(REPORT_CLASS);
            list = ds.getObjects(reportClass, true);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return null;
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 3;
    }
}
