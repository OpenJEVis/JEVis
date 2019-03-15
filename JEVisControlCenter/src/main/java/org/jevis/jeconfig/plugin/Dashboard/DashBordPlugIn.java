package org.jevis.jeconfig.plugin.Dashboard;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.Dashboard.config.DashBordModel;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.Dashboard.widget.Widgets;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class DashBordPlugIn implements Plugin {

    private static final Logger logger = LogManager.getLogger(DashBordPlugIn.class);
    public static String CLASS_ANALYSIS = "Dashboard Analysis", CLASS_ANALYSIS_DIR = "Analyses Directory", ATTRIBUTE_DATA_MODEL = "Data Model", ATTRIBUTE_BACKGROUND = "Background";
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
        return this.getClass().getName();
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

    @Override
    public void setHasFocus() {
        if (!isInitialized) {
            isInitialized = true;

            toolBar.updateToolbar(dashBoardPane.getDashBordAnalysis());
            loadAnalysis(currentAnalysis);

        }
    }

    public Widget createWidget(WidgetConfig widget) {
        System.out.println("createWidget for: " + widget.getType());
        for (Widget availableWidget : Widgets.getAvabableWidgets(getDataSource(), widget)) {
            if (availableWidget.typeID().equalsIgnoreCase(widget.getType())) {
                availableWidget.init();
                return availableWidget;
            }
        }

        return null;
    }

    public void addWidget(WidgetConfig widget) {
        logger.info("addWidget: " + widget);
        Widget newWidget = createWidget(widget);
        System.out.println("new widget##: " + newWidget);
        if (newWidget != null) {
            dashBoardPane.addNode(newWidget);
            currentAnalysis.addWidget(widget);
        }

    }

    public void toPDF() {
        try {

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            Interval interval = currentAnalysis.displayedIntervalProperty.getValue();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
            String intervalString = fmt.print(interval.getStart()) + "_" + fmt.print(interval.getEnd());
            fileChooser.setInitialFileName(currentAnalysis.getAnalysisObject().getName() + "_" + intervalString + ".pdf");

            File file = fileChooser.showSaveDialog(JEConfig.getStage());

            if (file != null) {

                final SnapshotParameters spa = new SnapshotParameters();
                final WritableImage image = new WritableImage((int) dashBoardPane.getWidth(), (int) dashBoardPane.getHeight());
                WritableImage wImage = dashBoardPane.snapshot(spa, image);
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(wImage, null), "png", byteOutput);
                com.itextpdf.text.Image graph = com.itextpdf.text.Image.getInstance(byteOutput.toByteArray());

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(graph);
                document.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
