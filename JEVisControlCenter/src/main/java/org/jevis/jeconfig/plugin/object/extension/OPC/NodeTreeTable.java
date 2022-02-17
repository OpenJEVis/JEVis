package org.jevis.jeconfig.plugin.object.extension.OPC;


import com.jfoenix.controls.*;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.PathReferenceDescription;
import org.joda.time.DateTime;

import org.joda.time.Period;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class NodeTreeTable {


    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(NodeTreeTable.class);
    private final VBox view = new VBox();


    private final TreeTableView<Node> opcUATreeTableView = new TreeTableView<>();
    private final OPCClient opcClient;
    private TreeItem<Node> rootTreeItem;
    private TreeItem<Node> currentTreeItem;
    private boolean rootSet = false;
    private final JFXButton importDataStructureJFXButton;
    private final JFXButton selectTargetJFXButton;
    private JFXDialog setValueDialog;

    private int count;

    private JEVisObject targetDataObject;
    private JEVisDataSource ds;
    private final ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    private final Image taskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private StackPane dialogContainer;
    private static final String LOYTEC_XML_DL_DIRECTORY = "Loytec XML-DL CEA709 Channel Directory";
    private static final String DATA_DIRECTORY = "Data Directory";
    private static final String LOYTEC_XML_DL_CHANNEL = "Loytec XML-DL Channel";
    private static final String TREND_ID = "Trend ID";
    private static final String TARGET_ID = "Target ID";
    private static final String IMPORTED_FROM_OPC_UA = "Imported From OPC UA";


    private JEVisObject trendRoot;
    private final String opcUARootFolder;
    public static final String BROWSER_MODE = I18n.getInstance().getString("plugin.object.opcua.mode.browse");
    public static final String SETUP_MODE = I18n.getInstance().getString("plugin.object.opcua.mode.setupassistant");
    public static final String LOG_MODE = "trendMode";
    public static final String LOG_INTERVAL = "POLL";
    private final String mode;


    public NodeTreeTable(OPCClient opcClient, JEVisObject trendRoot, String opcUaRootFolder, StackPane dialogContainer, String mode) {

        this.mode = mode;
        this.opcClient = opcClient;
        this.trendRoot = trendRoot;
        this.opcUARootFolder = opcUaRootFolder;
        this.dialogContainer = dialogContainer;
        selectTargetJFXButton = buildTargetButton();
        importDataStructureJFXButton = buildImportDataStructureButton();
        try {
            ds = trendRoot.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }





        TreeTableColumn<Node, String> nameCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.name"));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().descriptionProperty.get().getBrowseName().getName()));


        if (mode.equals(BROWSER_MODE)) {
            TreeTableColumn<Node, String> valueCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.value"));
            TreeTableColumn<Node, String> nodeIDCol = new TreeTableColumn<>(I18n.getInstance().getString("plugin.object.opcua.column.nodeid"));
            valueCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().readData()));
            nodeIDCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().nodeIdProperty.get().toParseableString()));
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

                                    if (getTreeTableRow().getTreeItem().getValue().descriptionProperty.get().getNodeClass().getValue() == 1) {
                                        JFXCheckBox box = new JFXCheckBox();
                                        box.setSelected(item);
                                        box.setOnAction(event -> {


                                            getTreeTableRow().getTreeItem().getValue().setSelected(box.isSelected());

                                            childrenSetSelected(getTreeTableRow().getTreeItem());
                                            opcUATreeTableView.refresh();

                                        });

                                        setGraphic(new BorderPane(box));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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


        if (mode.equals(SETUP_MODE)) {
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(selectTargetJFXButton, importDataStructureJFXButton);
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
                setSetValueDialog(opcClient, dialogContainer);
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

                                if (rootSet == false) {

                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));

                                }
//                                if (o.getReferenceDescription().getBrowseName().getName().equals(opcUaRootFolder)) {
//                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
//                                }
                                else if (rootSet == true) {
                                    currentTreeItem = createOPCUAChildren(currentTreeItem, new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
                                }

                            });

                        }
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
            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, taskIcon, true);

            /**
             HashMap<String,ReferenceDescription> map = opcClient.browse();
             System.out.println("Mapsize: "+map.size());
             map.forEach((xpath, referenceDescription) -> {
             Node newNode = new Node(referenceDescription,xpath);
             list.add(newNode);
             });
             **/
        } catch (Exception ex) {
            logger.error("error while browsing Client: {}", ex);
        }
    }

    private void setSetValueDialog(OPCClient opcClient, StackPane dialogContainer) {
        try {
            String dataType = opcClient.getDataType(NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));


            setValueDialog = new JFXDialog();
            setValueDialog.setDialogContainer(dialogContainer);
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(10);
            gridPane.setVgap(5);
            gridPane.setMinHeight(150);
            gridPane.setMinWidth(235);


            Label valueLabel = new Label(I18n.getInstance().getString("plugin.graph.table.value"));

            HBox hbox = new HBox(6);
            JFXTextField valueField = new JFXTextField();

            JFXComboBox<Boolean> valueComboBox = new JFXComboBox();
            valueComboBox.getItems().addAll(true, false);


            if (dataType.equals(Boolean.class.getName())) {
                hbox.getChildren().addAll(valueLabel, valueComboBox);
            } else {
                hbox.getChildren().addAll(valueLabel, valueField);
            }


            final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
            ok.setDefaultButton(true);
            final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
            cancel.setCancelButton(true);

            HBox buttonBar = new HBox(20, cancel, ok);

            cancel.setOnAction(event1 -> setValueDialog.close());


            ok.setOnAction(event1 -> {
                try {


                    if (dataType.equals(Boolean.class.getName())) {
                        opcClient.writeValue(valueComboBox.getValue(), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));
                    } else if (dataType.equals(String.class.getName())) {
                        opcClient.writeValue(valueField.getText(), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));
                    } else if (dataType.equals(Double.class.getName())) {
                        opcClient.writeValue(Double.valueOf(valueField.getText()), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));
                    } else if (dataType.equals(Integer.class.getName())) {
                        opcClient.writeValue(Integer.valueOf(valueField.getText()), NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));
                    } else {
                        logger.info("Datatype Mismatch");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.error.datatype.title"));
                        alert.setHeaderText(I18n.getInstance().getString("plugin.object.opcua.error.datatype.message"));
                        alert.showAndWait();
                    }

                    opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().dataValue = opcClient.readValue(NodeId.parse(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get()));
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
            gridPane.add(buttonBar, 0, 3);

            setValueDialog.setContent(gridPane);

            setValueDialog.show();

        } catch (UaException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy node ID into clipboard of Node
     */
    private void copyNodeId() {
        final ClipboardContent content = new ClipboardContent();
        content.putString(opcUATreeTableView.getSelectionModel().getSelectedItem().getValue().stringNodeID.get());
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
        rootTreeItem = new TreeItem<>(node);
        rootSet = true;
        opcUATreeTableView.setRoot(rootTreeItem);
        return rootTreeItem;
    }

    /**
     * @param parent OPC-UA Parent
     * @param node   OPC-UA Node to add OPC-Browser
     * @return parent OPC-UA Node
     */

    private TreeItem<Node> createOPCUAChildren(TreeItem<Node> parent, Node node) {
        if ((parent.getValue().pathProperty.get() + "/" + parent.getValue().descriptionProperty.get().getBrowseName().getName()).equals(node.pathProperty.get())) {
            TreeItem<Node> treeItem = new TreeItem<>(node);
            logger.info("OPC-UA children: {} added", node.descriptionProperty.get().getBrowseName());
            if (node.descriptionProperty.get().getNodeClass().getValue() == 1 || mode.equals(BROWSER_MODE)) {
                parent.getChildren().add(treeItem);
                return treeItem;
            } else {
                if (node.descriptionProperty.get().getBrowseName().getName().equals("CsvFile")) {
                    String csvString = node.readData().split("\\.|\\/")[node.readData().split("\\.|\\/.").length - 2];
                    parent.getValue().setTrendID(csvString);
                } else if (node.descriptionProperty.get().getBrowseName().getName().equals("Configuration")) {
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

        } else if (parent.getValue().pathProperty.get().contains(rootTreeItem.getValue().pathProperty.get())) {
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

    private void createTrendDataTree(TreeItem<Node> node, JEVisObject dataSourceJEVisObject, DateTime dateTime, JEVisObject dataJEVisObject) throws JEVisException {

        if (node.getValue().getTrendID() == null) {
            logger.info("OPC-UA Node: {} is a Folder", node.getValue().descriptionProperty.get().getDisplayName());
            if (node.getValue().isSelected()) {
                dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_DIRECTORY);
                if (dataJEVisObject != null) {

                    dataJEVisObject = createJEVisObject(node, dataJEVisObject, DATA_DIRECTORY);
                }

            }
        } else {
            logger.info("OPC-UA Node: {} is not Folder", node.getValue().descriptionProperty.get().getDisplayName());
            if (node.getValue().isSelected()) {
                dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_CHANNEL);
                JEVisAttribute jevisAttributeTarget = dataSourceJEVisObject.getAttribute(TARGET_ID);
                if (dataJEVisObject != null) {
                    dataJEVisObject = createJEVisObject(node, dataJEVisObject, "Data");
                    jevisAttributeTarget.buildSample(dateTime, dataJEVisObject.getID() + ":Value").commit();
                    setLogIntervalToJEVisObject(node, dataJEVisObject);
                    JEVisObject cleanDataJEVisObject = createJEVisObject(dataJEVisObject, "Clean Data", I18n.getInstance().getString("tree.treehelper.cleandata.name"));
                    setLogIntervalToJEVisObject(node, cleanDataJEVisObject);
                }
                setTrendIdToJEVisObject(node, dataSourceJEVisObject, dateTime);
            }
        }
        for (TreeItem<Node> nodeChild : node.getChildren()) {
            createTrendDataTree(nodeChild, dataSourceJEVisObject, dateTime, dataJEVisObject);
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

        String name = node.getValue().descriptionProperty.get().getBrowseName().getName();
        name = name.replace("Trend_", "");


        JEVisObject newObject = jevisParentObject.buildObject(name, dataClass);
        if (newObject.isAllowedUnder(jevisParentObject)) {
            newObject.commit();
            count++;
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
     * @param node            OPC UA node to get the interval from
     * @param dataJEVisObject JEVis data object where to set up input sample rate
     * @throws JEVisException
     */
    private void setLogIntervalToJEVisObject(TreeItem<Node> node, JEVisObject dataJEVisObject) throws JEVisException {
        JEVisAttribute jeVisAttribute = dataJEVisObject.getAttribute("Value");
        jeVisAttribute.setInputSampleRate(convertInterval(node.getValue().getLogInterval()));
        logger.debug(jeVisAttribute);
        jeVisAttribute.commit();


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
            long hours = TimeUnit.SECONDS.toHours(Integer.valueOf(interval)) - (day * 24);
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
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PT");
                stringBuilder.append(hours + day * 24).append("H");
                stringBuilder.append(minute).append("M");
                stringBuilder.append(second).append("S");
                return Period.parse(stringBuilder.toString());


            }

        }
    }


    private JFXButton buildTargetButton() {
        final JFXButton button = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.button"), JEConfig.getImage("folders_explorer.png", 18, 18));
        button.wrapTextProperty().setValue(true);
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {


                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter dataDirectoryFilter = SelectTargetDialog.buildClassFilter(ds, "Data Directory");
                allFilter.add(dataDirectoryFilter);
                SelectTargetDialog selectionDialog = new SelectTargetDialog(dialogContainer, allFilter, dataDirectoryFilter, null, SelectionMode.SINGLE, ds, new ArrayList<UserSelection>());
                selectionDialog.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);

                selectionDialog.setOnDialogClosed(event -> {

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

    private JFXButton buildImportDataStructureButton() {
        JFXButton jfxButton = new JFXButton();
        jfxButton.setText(I18n.getInstance().getString("plugin.object.opcua.button.import"));
        jfxButton.setOnAction(event -> {


            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {


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
                                createTrendDataTree(rootTreeItem, rootTrendObject, dateTime, rootDataObject);

                            }

                        } else {
                            logger.info("no target selected");
                            rootTrendObject.commit();
                            createTrendDataTree(rootTreeItem, rootTrendObject, dateTime, null);
                        }


                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, taskIcon, true);

            task.setOnSucceeded(event1 -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.import.finish.title"));
                alert.setHeaderText("");
                alert.setContentText(I18n.getInstance().getString("plugin.object.opcua.import.finish.message") + "" + count);
                count = 0;
                alert.showAndWait();
            });


        });
        return jfxButton;
    }


}
