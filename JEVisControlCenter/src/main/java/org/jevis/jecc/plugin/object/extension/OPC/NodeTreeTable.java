package org.jevis.jecc.plugin.object.extension.OPC;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jecc.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jecc.dialog.SelectTargetDialog;
import org.jevis.jecc.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.PathReferenceDescription;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class NodeTreeTable {


    public static final String BROWSER_MODE = I18n.getInstance().getString("plugin.object.opcua.mode.browse");
    public static final String SETUP_MODE = I18n.getInstance().getString("plugin.object.opcua.mode.setupassistant");
    public static final String LOG_MODE = "trendMode";
    public static final String LOG_INTERVAL = "POLL";
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(NodeTreeTable.class);
    private static final String LOYTEC_XML_DL_DIRECTORY = "Loytec XML-DL CEA709 Channel Directory";
    private static final String LOYTEC_XML_DL_BACNET_DIRECTORY = "Loytec XML-DL Bacnet Channel Directory";
    private static final String DATA_DIRECTORY = "Data Directory";
    private static final String LOYTEC_XML_DL_CHANNEL = "Loytec XML-DL Channel";
    private static final String TREND_ID = "Trend ID";
    private static final String TARGET_ID = "Target ID";
    private static final String IMPORTED_FROM_OPC_UA = "Imported From OPC UA";
    private final VBox view = new VBox();
    private final TreeTableView<Node> opcUATreeTableView = new TreeTableView<>();
    private final OPCClient opcClient;
    private final Button importDataStructureButton;
    private final Button selectTargetButton;
    private final ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    private final Image taskIcon = ControlCenter.getImage("if_dashboard_46791.png");
    private final JEVisObject trendRoot;
    private final String opcUARootFolder;
    private final String backNetRootFolder;
    private final String mode;
    private TreeItem<Node> branchRootTreeItem;
    private TreeItem<Node> mainRootTreeItem;
    private TreeItem<Node> currentTreeItem;
    private boolean bacnet = false;
    private boolean rootSet = false;
    private Dialog setValueDialog;
    private int jevisObjectcount;
    private JEVisObject targetDataObject;
    private JEVisDataSource ds;


    public NodeTreeTable(OPCClient opcClient, JEVisObject trendRoot, String opcUaRootFolder, String bacNetRootFolder, String mode) {

        this.mode = mode;
        this.opcClient = opcClient;
        this.trendRoot = trendRoot;
        this.backNetRootFolder = bacNetRootFolder;
        this.opcUARootFolder = opcUaRootFolder;
        selectTargetButton = buildTargetButton();
        importDataStructureButton = buildImportDataStructureButton();
        try {
            ds = trendRoot.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        setRoot();


        setUpTable(mode);


        if (mode.equals(SETUP_MODE)) {
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(selectTargetButton, importDataStructureButton);
            hBox.setPadding(new Insets(10, 10, 10, 10));

            view.getChildren().add(hBox);

        } else if (mode.equals(BROWSER_MODE)) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem cMcopyNodeId = new MenuItem(I18n.getInstance().getString("plugin.object.opcua.copynodeid"));


            opcUATreeTableView.setOnKeyPressed(event -> {
                if (event.isControlDown()) {
                    if (KeyCode.C.equals(event.getCode())) {
                        copyNodeId();
                    }
                }
            });
            cMcopyNodeId.setOnAction(event -> {

                copyNodeId();

            });
            MenuItem cMsetValue = new MenuItem(I18n.getInstance().getString("plugin.object.opcua.setvalue"));
            cMsetValue.setOnAction(event -> {
                setSetValueDialog(opcClient);
            });
            contextMenu.getItems().addAll(cMcopyNodeId, cMsetValue);
            opcUATreeTableView.setContextMenu(contextMenu);
        }
        GridPane.setFillWidth(opcUATreeTableView, true);
        GridPane.setFillHeight(opcUATreeTableView, true);


        try {
            ObservableList<PathReferenceDescription> list = FXCollections.observableArrayList();
            list.addListener(new ListChangeListener<PathReferenceDescription>() {
                @Override
                public void onChanged(Change<? extends PathReferenceDescription> c) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            c.getAddedSubList().forEach(o -> {
                                logger.debug("New Des: " + o.getPath() + "  -> " + o.getReferenceDescription().getBrowseName().getName());
                                PathReferenceDescription x = o;

                                Node node = new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue());
                                if (!rootSet) {

                                    currentTreeItem = setRoot(node);

                                } else if (rootSet) {

                                    currentTreeItem = createOPCUAChildren(currentTreeItem, node);
                                }


                            });

                        }
                    }
                    if (bacnet && mode.equals(SETUP_MODE)) {
                        branchRootTreeItem.getChildren().removeIf(nodeTreeItem -> !nodeTreeItem.getValue().getName().equals("Trend"));
                    }

                }

            });


            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        opcClient.browse(list, opcUARootFolder);
                        super.done();
                    } catch (Exception ex) {
                        super.failed();
                    }
                    return null;
                }
            };
            Task task2 = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        opcClient.browse(list, bacNetRootFolder);
                        super.done();
                    } catch (Exception ex) {
                        super.failed();
                    }
                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                        if (mode.equals(SETUP_MODE)) {
                            bacnet = true;
                            ControlCenter.getStatusBar().addTask(DashBordPlugIn.class.getName(), task2, taskIcon, true);
                            rootSet = false;
                        }
                    }
            );
            ControlCenter.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, taskIcon, true);

            /**
             HashMap<String,ReferenceDescription> map = opcClient.browse();
             System.out.println("Mapsize: "+map.size());
             map.forEach((xpath, referenceDescription) -> {
             Node newNode = new Node(referenceDescription,xpath);
             list.add(newNode);
             });
             **/
        } catch (Exception ex) {
            logger.error("Error while browsing Client: ", ex);
        }
    }

    private void setUpTable(String mode) {
        TreeTableColumn<Node, String> nameCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.name"));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getName()));


        if (mode.equals(BROWSER_MODE)) {
            TreeTableColumn<Node, String> valueCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.value"));
            TreeTableColumn<Node, String> nodeIDCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.nodeid"));
            valueCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().readData()));
            nodeIDCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getNodeIdProperty().toParseableString()));
            opcUATreeTableView.getColumns().addAll(nameCol, valueCol, nodeIDCol);
        } else if (mode.equals(SETUP_MODE)) {
            TreeTableColumn<Node, Boolean> checkCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.import"));
            TreeTableColumn<Node, String> trendIdCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.trendid"));
            TreeTableColumn<Node, String> intervallIdCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.logintervall"));
            intervallIdCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getValue().getLogInterval())));
            trendIdCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getTrendID()));
            opcUATreeTableView.getColumns().addAll(nameCol, checkCol, trendIdCol, intervallIdCol);

            checkCol.setCellFactory(new Callback<TreeTableColumn<Node, Boolean>, TreeTableCell<Node, Boolean>>() {

                @Override
                public TreeTableCell<Node, Boolean> call(TreeTableColumn<Node, Boolean> param) {
                    return new TreeTableCell<Node, Boolean>() {

                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(null);
                            setGraphic(null);

                            if (!empty && getTreeTableRow().getTreeItem() != null) {
                                try {

                                    if (getTreeTableRow().getTreeItem().getValue().getDescriptionProperty().getNodeClass().getValue() == 1) {
                                        CheckBox box = new CheckBox();
                                        box.setSelected(item);
                                        box.selectedProperty().addListener((observableValue, aBoolean, t1) -> {


                                            getTreeTableRow().getTreeItem().getValue().setSelected(t1);

                                            childrenSetSelected(getTreeTableRow().getTreeItem());
                                            parentSetSelected(getTreeTableRow().getTreeItem());
                                            opcUATreeTableView.refresh();

                                        });

                                        setGraphic(new BorderPane(box));
                                    }
                                } catch (Exception e) {
                                    logger.error(e);
                                }
                            }
                        }
                    };

                }
            });

            checkCol.setCellValueFactory(param -> {
                Node node = param.getValue().getValue();

                if (node.isSelected()) {
                    return new SimpleBooleanProperty(true);
                } else {
                    return new SimpleBooleanProperty(false);
                }

            });

            checkCol.setEditable(true);
        }


        opcUATreeTableView.setEditable(true);


        opcUATreeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(opcUATreeTableView, Priority.ALWAYS);

        view.getChildren().add(opcUATreeTableView);
    }

    private void setSetValueDialog(OPCClient opcClient) {
        try {
            String dataType = opcClient.getDataType(NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID()));


            setValueDialog = new Dialog<>();
            setValueDialog.setResizable(true);
            setValueDialog.initOwner(ControlCenter.getStage());
            setValueDialog.initModality(Modality.APPLICATION_MODAL);
            Stage stage = (Stage) setValueDialog.getDialogPane().getScene().getWindow();
            TopMenu.applyActiveTheme(stage.getScene());
            stage.setAlwaysOnTop(true);

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(10);
            gridPane.setVgap(5);
            gridPane.setMinHeight(150);
            gridPane.setMinWidth(235);


            Label valueLabel = new Label(I18n.getInstance().getString("plugin.graph.table.value"));

            HBox hbox = new HBox(6);
            TextField valueField = new TextField();

            ComboBox<Boolean> valueComboBox = new ComboBox<>();
            valueComboBox.getItems().addAll(true, false);


            if (dataType.equals(Boolean.class.getName())) {
                hbox.getChildren().addAll(valueLabel, valueComboBox);
            } else {
                hbox.getChildren().addAll(valueLabel, valueField);
            }

            ButtonType okType = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            setValueDialog.getDialogPane().getButtonTypes().setAll(cancelType, okType);

            Button okButton = (Button) setValueDialog.getDialogPane().lookupButton(okType);
            okButton.setDefaultButton(true);

            Button cancelButton = (Button) setValueDialog.getDialogPane().lookupButton(cancelType);
            cancelButton.setCancelButton(true);

            cancelButton.setOnAction(event1 -> setValueDialog.close());

            okButton.setOnAction(event1 -> {
                try {


                    if (dataType.equals(Boolean.class.getName())) {
                        opcClient.writeValue(valueComboBox.getValue(), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID()));
                    } else if (dataType.equals(String.class.getName())) {
                        opcClient.writeValue(valueField.getText(), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID()));
                    } else if (dataType.equals(Double.class.getName())) {
                        opcClient.writeValue(Double.valueOf(valueField.getText()), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID()));
                    } else if (dataType.equals(Integer.class.getName())) {
                        opcClient.writeValue(Integer.valueOf(valueField.getText()), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID()));
                    } else {
                        logger.info("Datatype Mismatch");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.error.datatype.title"));
                        alert.setHeaderText(I18n.getInstance().getString("plugin.object.opcua.error.datatype.message"));
                        alert.showAndWait();
                    }

                    opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().setDataValue(opcClient.readValue(NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID())));
                    opcUATreeTableView.refresh();
                } catch (UaException | NumberFormatException e) {
                    e.printStackTrace();
                    logger.info("Datatype Mismatch");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.error.datatype.title"));
                    alert.setHeaderText(I18n.getInstance().getString("plugin.object.opcua.error.datatype.message"));
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                } finally {
                    setValueDialog.close();
                }


            });

            gridPane.add(hbox, 0, 0);

            setValueDialog.getDialogPane().setContent(gridPane);

            setValueDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * copy node ID into clipboard of Node
     */
    private void copyNodeId() {
        final ClipboardContent content = new ClipboardContent();
        content.putString(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().getStringNodeID());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public VBox getView() {
        return view;
    }


    public void setData() {

    }

    /**
     * @param node to be set as Root object for OPC-UA Browser
     * @return
     */

    private TreeItem<Node> setRoot(Node node) {
        if (bacnet) {
            node.setTrendType(Node.BACNET_TREND);
        }
        branchRootTreeItem = new TreeItem<>(node);
        rootSet = true;
        mainRootTreeItem.getChildren().add(branchRootTreeItem);
        //opcUATreeTableView.setRoot(rootTreeItem);
        return branchRootTreeItem;
    }


    private TreeItem<Node> setRoot() {
        PathReferenceDescription pd = opcClient.getRoot("/Objects/Loytec ROOT");
        mainRootTreeItem = new TreeItem<>(new Node(pd.getReferenceDescription(), pd.getPath(), pd.getDataValue()));
        opcUATreeTableView.setRoot(mainRootTreeItem);
        opcUATreeTableView.setShowRoot(false);
        return branchRootTreeItem;
    }


    /**
     * @param parent OPC-UA Parent
     * @param node   OPC-UA Node to add OPC-Browser
     * @return parent OPC-UA Node
     */

    private TreeItem<Node> createOPCUAChildren(TreeItem<Node> parent, Node node) {
        if (bacnet) {
            node.setTrendType(Node.BACNET_TREND);
        }
        if ((parent.getValue().getPathProperty() + "/" + parent.getValue().getDescriptionProperty().getBrowseName().getName()).equals(node.getPathProperty())) {
            TreeItem<Node> treeItem = new TreeItem<>(node);
            logger.info("OPC-UA children: {} added", node.getDescriptionProperty().getBrowseName());
            if (node.getDescriptionProperty().getNodeClass().getValue() == 1 || mode.equals(BROWSER_MODE)) {
                parent.getChildren().add(treeItem);
                return treeItem;
            } else {
                if (node.getDescriptionProperty().getBrowseName().getName().equals("LogHandle")) {
                    String logHandleString = node.readData().split("\\.|\\-")[node.readData().split("\\.|\\-.").length - 2];
                    parent.getValue().setTrendID(logHandleString);
                } else if (node.getDescriptionProperty().getBrowseName().getName().equals("Configuration")) {
                    try {
                        parent.getValue().setLogInterval(getLogInterval(node.readData()));
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
                return parent;
            }

        } else if (parent.getValue().getPathProperty().contains(branchRootTreeItem.getValue().getPathProperty())) {
            return createOPCUAChildren(parent.getParent(), node);
        } else {
            return parent;
        }
    }

    /**
     * @param node                  OPC-UA Node
     * @param dataSourceJEVisObject Datasource Parent
     * @param dateTime              DateTime Stamp for JEVIs Sample
     * @param dataJEVisObject       Data Parent
     * @throws JEVisException
     */

    private void createTrendDataTree(TreeItem<Node> node, JEVisObject dataSourceJEVisObject, DateTime dateTime, JEVisObject dataJEVisObject) {
        try {

            if (node.getValue().getTrendID() == null) {
                logger.info("OPC-UA Node: {} is a Folder", node.getValue().getDescriptionProperty().getDisplayName());
                if (node.getValue().isSelected()) {
                    if (node.getValue().getTrendType().equals(Node.BACNET_TREND)) {
                        dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_BACNET_DIRECTORY);
                    } else {
                        dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_DIRECTORY);
                    }

                    if (dataJEVisObject != null) {

                        dataJEVisObject = createJEVisObject(node, dataJEVisObject, DATA_DIRECTORY);
                    }

                }
            } else {
                logger.info("OPC-UA Node: {} is not Folder", node.getValue().getDescriptionProperty().getDisplayName());
                if (node.getValue().isSelected()) {
                    dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_CHANNEL);
                    JEVisAttribute jevisAttributeTarget = dataSourceJEVisObject.getAttribute(TARGET_ID);
                    if (dataJEVisObject != null && jevisAttributeTarget != null) {
                        dataJEVisObject = createJEVisObject(node, dataJEVisObject, "Data");
                        JEVisAttribute dataPeriodAttribute = dataJEVisObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
                        if (dataPeriodAttribute != null) {
                            JEVisSample sample = dataPeriodAttribute.buildSample(new DateTime(1990, 1, 1, 0, 0, 0, 0), convertInterval(node.getValue().getLogInterval()));
                            sample.commit();
                        }
                        jevisAttributeTarget.buildSample(dateTime, dataJEVisObject.getID() + ":Value").commit();
                        JEVisObject cleanDataJEVisObject = createJEVisObject(dataJEVisObject, "Clean Data", I18n.getInstance().getString("tree.treehelper.cleandata.name"));
                        cleanDataJEVisObject.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.cleandata.name"));
                        JEVisAttribute cleanDataPeriodAttribute = cleanDataJEVisObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
                        if (cleanDataPeriodAttribute != null) {
                            JEVisSample sample = cleanDataPeriodAttribute.buildSample(new DateTime(1990, 1, 1, 0, 0, 0, 0), convertInterval(node.getValue().getLogInterval()));
                            sample.commit();
                        }
                        cleanDataJEVisObject.commit();
                    }
                    setTrendIdToJEVisObject(node, dataSourceJEVisObject, dateTime);
                }
            }
            for (TreeItem<Node> nodeChild : node.getChildren()) {
                createTrendDataTree(nodeChild, dataSourceJEVisObject, dateTime, dataJEVisObject);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param node                  OPC parent node (in one of the children is the trend id stored)
     * @param dataSourceJEVisObject to add the TrendId into
     * @param dateTime              of the Sample
     * @throws JEVisException
     */
    private void setTrendIdToJEVisObject(TreeItem<Node> node, JEVisObject dataSourceJEVisObject, DateTime dateTime) throws JEVisException {
        JEVisAttribute jeVisAttribute = dataSourceJEVisObject.getAttribute(TREND_ID);
        logger.info("Trend Id: {} added in Sample", node.getValue().getTrendID());
        jeVisAttribute.buildSample(dateTime, node.getValue().getTrendID()).commit();

    }


    /**
     * @param node              of OPC-UA Node Item
     * @param jevisParentObject Parnet under which the JVEis Object will vbe created
     * @param className         Class of the new JEVis Object
     * @return
     * @throws JEVisException
     */
    private JEVisObject createJEVisObject(TreeItem<Node> node, JEVisObject jevisParentObject, String className) throws JEVisException {
        JEVisClass dataClass = jevisParentObject.getDataSource().buildClass(className);

        String name = node.getValue().getDescriptionProperty().getBrowseName().getName();
        name = name.replace("Trend_", "");


        JEVisObject newObject = jevisParentObject.buildObject(name, dataClass);
        if (newObject.isAllowedUnder(jevisParentObject)) {
            newObject.commit();
            jevisObjectcount++;
            return newObject;
        } else {
            return jevisParentObject;
        }
    }

    /**
     * @param jevisParentObject
     * @param className         Name of the Jevis Class
     * @param objectName        name of the JEVis Object in JEVis
     * @return
     * @throws JEVisException
     */
    private JEVisObject createJEVisObject(JEVisObject jevisParentObject, String className, String objectName) throws JEVisException {

        JEVisClass dataClass = jevisParentObject.getDataSource().buildClass(className);
        JEVisObject newObject = jevisParentObject.buildObject(objectName, dataClass);
        if (newObject.isAllowedUnder(jevisParentObject)) {
            newObject.commit();
        }
        return newObject;
    }

    /**
     * select children if parent is selected
     *
     * @param nodeTreeItem
     */
    private void childrenSetSelected(TreeItem<Node> nodeTreeItem) {
        for (int i = 0; i < nodeTreeItem.getChildren().size(); i++) {
            nodeTreeItem.getChildren().get(i).getValue().setSelected(nodeTreeItem.getValue().isSelected());
            if (nodeTreeItem.getChildren().get(i).getChildren().size() > 0) {
                childrenSetSelected(nodeTreeItem.getChildren().get(i));
            }
        }
    }

    private void parentSetSelected(TreeItem<Node> nodeTreeItem) {
        if (nodeTreeItem.getValue().isSelected()) {
            if (nodeTreeItem.getParent() != null) {
                nodeTreeItem.getParent().getValue().setSelected(nodeTreeItem.getValue().isSelected());
                parentSetSelected(nodeTreeItem.getParent());
            }
        }

    }

    /**
     * @param xml String of the Loytec config
     * @return String of the log Interval
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private String getLogInterval(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        if (doc.getDocumentElement().getAttribute(LOG_MODE).equals(LOG_INTERVAL)) {
            return String.valueOf(Integer.valueOf(doc.getDocumentElement().getAttribute("logInterval")) / 1000);
        } else {
            return "Asynchronous";
        }
    }

    /**
     * @param interval Log Interval in Seconds
     * @return Periode
     */
    private Period convertInterval(String interval) {

        if (interval.equals("Asynchronous")) {
            return Period.ZERO;
        } else {
            int day = (int) TimeUnit.SECONDS.toDays(Integer.valueOf(interval));
            long hours = TimeUnit.SECONDS.toHours(Integer.valueOf(interval)) - (day * 24L);
            long minute = TimeUnit.SECONDS.toMinutes(Integer.valueOf(interval)) - (TimeUnit.SECONDS.toHours(Integer.valueOf(interval)) * 60);
            long second = TimeUnit.SECONDS.toSeconds(Integer.valueOf(interval)) - (TimeUnit.SECONDS.toMinutes(Integer.valueOf(interval)) * 60);

            if (day == 0 && hours == 0 && minute == 15 && second == 0) {
                return Period.minutes(15);
            } else if (day == 0 && hours == 0 && minute == 1 && second == 0) {
                return Period.minutes(1);
            } else if (day == 1 && hours == 0 && minute == 0 && second == 0) {
                return Period.days(1);
            } else if (day == 0 && hours == 1 && minute == 0 && second == 0) {
                return Period.hours(1);
            } else if (day == 7 && hours == 0 && minute == 0 && second == 0) {
                return Period.weeks(1);
            } else {
                String stringBuilder = "PT" +
                        (hours + day * 24L) + "H" +
                        minute + "M" +
                        second + "S";
                return Period.parse(stringBuilder);


            }

        }
    }


    private Button buildTargetButton() {
        final Button button = new Button(I18n.getInstance().getString("plugin.object.attribute.target.button"), ControlCenter.getImage("folders_explorer.png", 18, 18));
        button.wrapTextProperty().setValue(true);
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {


                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter dataDirectoryFilter = SelectTargetDialog.buildClassFilter(ds, "Data Directory");
                allFilter.add(dataDirectoryFilter);
                SelectTargetDialog selectionDialog = new SelectTargetDialog(allFilter, dataDirectoryFilter, null, SelectionMode.SINGLE, ds, new ArrayList<UserSelection>());
                selectionDialog.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);

                selectionDialog.setOnCloseRequest(event -> {

                    if (selectionDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        logger.trace("Selection Done");
                        for (UserSelection us : selectionDialog.getUserSelection()) {
                            try {
                                String buttonText = "";
                                if (us.getSelectedObject() != null) {
                                    logger.trace("us: {}", us.getSelectedObject().getID());
                                    buttonText += us.getSelectedObject().getName();
                                    targetDataObject = us.getSelectedObject();
                                }
                                button.setText(buttonText);

                            } catch (Exception ex) {
                                logger.catching(ex);
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle(I18n.getInstance().getString("csv.target.error.title"));
                                alert.setHeaderText(I18n.getInstance().getString("csv.target.error.message"));
                                alert.setContentText(ex.getMessage());

                                alert.showAndWait();
                            }
                        }
                    }
                });
                selectionDialog.show();
            }
        });

        return button;
    }

    private Button buildImportDataStructureButton() {
        Button button = new Button();
        button.setText(I18n.getInstance().getString("plugin.object.opcua.button.import"));
        button.setOnAction(event -> {


            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    if (targetDataObject == null) {
                        failed();
                    } else {
                        try {
                            DateTime dateTime = DateTime.now();
                            JEVisClass trendClass = trendRoot.getDataSource().getJEVisClass(LOYTEC_XML_DL_DIRECTORY);
                            JEVisObject rootTrendObject = trendRoot.buildObject(IMPORTED_FROM_OPC_UA, trendClass);
                            if (targetDataObject != null) {

                                JEVisClass dataClass = trendRoot.getDataSource().getJEVisClass(DATA_DIRECTORY);


                                JEVisObject rootDataObject = targetDataObject.buildObject(IMPORTED_FROM_OPC_UA, dataClass);
                                if (rootTrendObject.isAllowedUnder(trendRoot) && rootDataObject.isAllowedUnder(targetDataObject)) {

                                    rootTrendObject.commit();
                                    rootDataObject.commit();
                                    createTrendDataTree(mainRootTreeItem, rootTrendObject, dateTime, rootDataObject);

                                }

                            } else {
                                logger.info("no target selected");
                                rootTrendObject.commit();
                                createTrendDataTree(mainRootTreeItem, rootTrendObject, dateTime, null);
                            }

                            succeeded();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                    return null;
                }
            };

            ControlCenter.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, taskIcon, true);

            task.setOnFailed(workerStateEvent -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(I18n.getInstance().getString("plugin.object.attribute.target.error"));
                alert.setHeaderText("");
                alert.setContentText(I18n.getInstance().getString("plugin.object.attribute.target.error.message"));
                jevisObjectcount = 0;
                alert.showAndWait();
            });

            task.setOnSucceeded(event1 -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.import.finish.title"));
                alert.setHeaderText("");
                alert.setContentText(I18n.getInstance().getString("plugin.object.opcua.import.finish.message") + jevisObjectcount);
                jevisObjectcount = 0;
                alert.showAndWait();
            });


        });
        return button;
    }


}
