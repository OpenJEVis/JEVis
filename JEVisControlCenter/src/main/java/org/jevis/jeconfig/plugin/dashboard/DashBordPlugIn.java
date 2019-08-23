package org.jevis.jeconfig.plugin.dashboard;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.pdf.PdfWriter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.Layouts;


public class DashBordPlugIn implements Plugin {

    private static final Logger logger = LogManager.getLogger(DashBordPlugIn.class);
    public static String CLASS_ANALYSIS = "Dashboard Analysis", CLASS_ANALYSIS_DIR = "Analyses Directory", ATTRIBUTE_DATA_MODEL_FILE = "Data Model File", ATTRIBUTE_DATA_MODEL = "Data Model", ATTRIBUTE_BACKGROUND = "Background";
    public static String PLUGIN_NAME = "Dashboard Plugin";
    private StringProperty nameProperty = new SimpleStringProperty("Dashboard");
    private StringProperty uuidProperty = new SimpleStringProperty("Dashboard");
    private boolean isInitialized = false;
    private AnchorPane rootPane = new AnchorPane();

    public final BooleanProperty editProperty = new SimpleBooleanProperty(Boolean.class, "Enable Edit", false);
    private final DashboardControl dashboardControl;
    private JEVisDataSource jeVisDataSource;
    private final DashBoardPane dashBoardPane;
    private final DashBoardToolbar toolBar;
    private ScrollPane scrollPane = new ScrollPane();

    public DashBordPlugIn(JEVisDataSource ds, String name) {
        logger.error("init DashBordPlugIn");
        this.rootPane.setStyle("-fx-background-color: blue;");
        this.nameProperty.setValue(name);
        this.jeVisDataSource = ds;


        this.dashboardControl = new DashboardControl(this);
        this.toolBar = new DashBoardToolbar(this.dashboardControl);
        this.dashBoardPane = new DashBoardPane(this.dashboardControl);
        this.scrollPane.setContent(this.dashBoardPane);

        Layouts.setAnchor(this.scrollPane, 0d);
        Layouts.setAnchor(this.rootPane, 0d);
        this.rootPane.getChildren().setAll(this.scrollPane);


        ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                DashBordPlugIn.this.dashboardControl.setRootSizeChanged(DashBordPlugIn.this.scrollPane.getWidth(), DashBordPlugIn.this.scrollPane.getHeight());
            }
        };
        this.scrollPane.widthProperty().addListener(sizeListener);
        this.scrollPane.heightProperty().addListener(sizeListener);

    }

    public void setContentSize(double width, double height) {
        logger.debug("DashBordPlugIn init.size: {}/{} {}/{} ", this.rootPane.getWidth(), this.rootPane.getHeight(), this.scrollPane.getWidth(), this.scrollPane.getHeight());

    }

    public DashBoardToolbar getDashBoardToolbar() {
        return this.toolBar;
    }

    @Override
    public String getClassName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getName() {
        return this.nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
        this.nameProperty.setValue(name);
    }

    @Override
    public StringProperty nameProperty() {
        return this.nameProperty;
    }

    @Override
    public String getUUID() {
        return this.uuidProperty.getValue();
    }

    @Override
    public void setUUID(String id) {
        this.uuidProperty.setValue(id);
    }

    @Override
    public String getToolTip() {
        return this.nameProperty.getValue();
    }

    @Override
    public StringProperty uuidProperty() {
        return this.uuidProperty;
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
        return this.toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return this.jeVisDataSource;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.jeVisDataSource = ds;
    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        return this.rootPane;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("if_dashboard_46791.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }


    @Override
    public void setHasFocus() {
        if (!this.isInitialized) {
            this.isInitialized = true;
            this.dashboardControl.loadFirstDashboard();
            logger.debug("DashBordPlugIn focus.size: {}/{} {}/{} ", this.rootPane.getWidth(), this.rootPane.getHeight(), this.scrollPane.getWidth(), this.scrollPane.getHeight());

        }
    }

    public DashBoardPane getDashBoardPane() {
        return this.dashBoardPane;
    }


    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 1;
    }


    public void toPDF() {
        /** disabled in dependency, takes 5 mb and does not work for now because of ChartFX
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
