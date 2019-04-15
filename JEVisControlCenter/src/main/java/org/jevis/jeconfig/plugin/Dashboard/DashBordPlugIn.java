package org.jevis.jeconfig.plugin.Dashboard;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widgets;


public class DashBordPlugIn implements Plugin {

    private static final Logger logger = LogManager.getLogger(DashBordPlugIn.class);
    public static String CLASS_ANALYSIS = "Dashboard Analysis", CLASS_ANALYSIS_DIR = "Analyses Directory", ATTRIBUTE_DATA_MODEL_FILE = "Data Model File", ATTRIBUTE_DATA_MODEL = "Data Model", ATTRIBUTE_BACKGROUND = "Background";
    public static String PLUGIN_NAME = "Dashboard Plugin";
    private final DashBoardToolbar toolBar;
    private StringProperty nameProperty = new SimpleStringProperty("Dashboard");
    private StringProperty uuidProperty = new SimpleStringProperty("Dashboard");
    private JEVisDataSource jeVisDataSource;
    private boolean isInitialized = false;
    private AnchorPane rootPane = new AnchorPane();
    private DashBordModel currentAnalysis;
    private DashBoardPane dashBoardPane;


    public DashBordPlugIn(JEVisDataSource ds, String name) {
        nameProperty.setValue(name);
        this.jeVisDataSource = ds;
        this.currentAnalysis = new DashBordModel(ds);
        this.dashBoardPane = new DashBoardPane(currentAnalysis);
        this.toolBar = new DashBoardToolbar(ds, this);
    }


    public void loadAnalysis(DashBordModel currentAnalysis) {
        this.currentAnalysis = currentAnalysis;
        this.dashBoardPane = new DashBoardPane(currentAnalysis);
        ScrollPane scrollPane = new ScrollPane(dashBoardPane);

        AnchorPane.setTopAnchor(scrollPane, 0d);
        AnchorPane.setBottomAnchor(scrollPane, 0d);
        AnchorPane.setLeftAnchor(scrollPane, 0d);
        AnchorPane.setRightAnchor(scrollPane, 0d);

        rootPane.getChildren().setAll(scrollPane);
        toolBar.updateToolbar(currentAnalysis);
        dashBoardPane.getDashBordAnalysis().editProperty.setValue(false);

    }

    @Override
    public String getClassName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        nameProperty.setValue(name);
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public String getUUID() {
        return uuidProperty.getValue();
    }

    @Override
    public void setUUID(String id) {
        uuidProperty.setValue(id);
    }

    @Override
    public String getToolTip() {
        return nameProperty.getValue();
    }

    @Override
    public StringProperty uuidProperty() {
        return uuidProperty;
    }

    @Override
    public Node getMenu() {
        return new Pane();
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
    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        jeVisDataSource = ds;
    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        return rootPane;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("if_dashboard_46791.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }


    private JEVisObject getUserSelectedDashboard() {
        JEVisObject currentUserObject = null;
        try {
            currentUserObject = jeVisDataSource.getCurrentUser().getUserObject();
            JEVisAttribute userSelectedDashboard = currentUserObject.getAttribute("Start Dashboard");
            if (userSelectedDashboard != null) {
                TargetHelper th = new TargetHelper(jeVisDataSource, userSelectedDashboard);
                if (th.getObject() != null && !th.getObject().isEmpty()) {
                    return th.getObject().get(0);
                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get Start Dashboard from user.");
        }

        return null;
    }

    @Override
    public void setHasFocus() {
        if (!isInitialized) {
            isInitialized = true;

            toolBar.updateToolbar(dashBoardPane.getDashBordAnalysis());

            Platform.runLater(() -> {

                JEVisObject userSelectedDashboard = getUserSelectedDashboard();
                if (userSelectedDashboard != null) {
                    toolBar.getListAnalysesComboBox().getSelectionModel().select(userSelectedDashboard);
                } else {
                    toolBar.getListAnalysesComboBox().getSelectionModel().selectFirst();
                }
            });

        }
    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 1;
    }

    public Widget createWidget(WidgetConfig widget) {
        for (Widget availableWidget : Widgets.getAvabableWidgets(getDataSource(), widget)) {
            if (availableWidget.typeID().equalsIgnoreCase(widget.getType())) {
                availableWidget.init();
                return availableWidget;
            }
        }

        return null;
    }

    public void addWidget(WidgetConfig widget) {
        Widget newWidget = createWidget(widget);
        if (newWidget != null) {
            dashBoardPane.addNode(newWidget);
            currentAnalysis.addWidget(widget);
        }

    }

    private Node getView() {
        return dashBoardPane;
    }

    public void toPDF() {
        /** disabled in dependency, takes 5 mb an d does not work for now
         try {
         logger.info("start- converting to pdf");

         FileChooser fileChooser = new FileChooser();
         FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
         fileChooser.getExtensionFilters().add(extFilter);
         Interval interval = currentAnalysis.displayedIntervalProperty.getValue();
         DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
         String intervalString = fmt.print(interval.getStart()) + "_" + fmt.print(interval.getEnd());
         fileChooser.setInitialFileName(currentAnalysis.getAnalysisObject().getName() + "_" + intervalString + ".pdf");

         File file = fileChooser.showSaveDialog(JEConfig.getStage());

         if (file != null) {
         logger.info("target file: {}", file);
         final SnapshotParameters spa = new SnapshotParameters();
         //                final WritableImage image = new WritableImage((int) dashBoardPane.getWidth(), (int) dashBoardPane.getHeight());

         logger.info("Start writing screenshot");
         //                WritableImage wImage = dashBoardPane.snapshot(spa, image);
         WritableImage wImage = getView().snapshot(new SnapshotParameters(), null);
         logger.info("Done screenshot");
         ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
         ImageIO.write(SwingFXUtils.fromFXImage(wImage, null), "png", byteOutput);
         logger.info("Convert 1 Done");
         com.itextpdf.text.Image graph = com.itextpdf.text.Image.getInstance(byteOutput.toByteArray());
         logger.info("Convert 2 Done");
         Document document = new Document();
         logger.info("Document start");
         PdfWriter.getInstance(document, new FileOutputStream(file));
         document.open();
         logger.info("doc open");
         document.add(graph);
         logger.info("doc screenshot add done");
         document.close();
         logger.info("doc done done");
         }

         } catch (Exception ex) {
         ex.printStackTrace();
         }
         **/
    }
}
