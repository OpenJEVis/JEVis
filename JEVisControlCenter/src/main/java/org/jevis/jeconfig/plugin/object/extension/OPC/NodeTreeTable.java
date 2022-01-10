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
import org.apache.logging.log4j.core.filter.RegexFilter;
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

import java.util.ArrayList;
import java.util.List;


public class NodeTreeTable {


    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(NodeTreeTable.class);
    private final VBox view = new VBox();


    private final TreeTableView<Node> opcUATreeTableView = new TreeTableView<>();
    private final OPCClient opcClient;
    private TreeItem<Node> rootTreeItem;
    private TreeItem<Node> currentTreeItem;
    private boolean rootSet = false;
    private final JFXButton createTrendObject = new JFXButton();
    private final JFXButton selectTarget;
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


    private JEVisObject object;
    private final String opcUARootFolder;


    public NodeTreeTable(OPCClient opcClient, JEVisObject trendRoot, String opcUaRootFolder, StackPane dialogContainer) {


        this.opcClient = opcClient;
        this.object = trendRoot;
        this.opcUARootFolder = opcUaRootFolder;
        this.dialogContainer = dialogContainer;
        selectTarget = buildTargetButton();
        try {
            ds = trendRoot.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }


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


        TreeTableColumn<Node, String> nameCol = new TreeTableColumn<>("Name");
        TreeTableColumn<Node, String> valueCol = new TreeTableColumn<>("Value");
        TreeTableColumn<Node, Boolean> checkCol = new TreeTableColumn<>("Trend");
        TreeTableColumn<Node, String> nodeIDCol = new TreeTableColumn<>("NodeID");


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


        opcUATreeTableView.setEditable(true);
        checkCol.setEditable(true);

        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().descriptionProperty.get().getBrowseName().getName()));
        valueCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().readData()));
        nodeIDCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().nodeIdProperty.get().toParseableString()));

        opcUATreeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(opcUATreeTableView, Priority.ALWAYS);
        checkCol.setPrefWidth(80);
        opcUATreeTableView.getColumns().addAll(nameCol, checkCol, valueCol, nodeIDCol);


        view.getChildren().add(opcUATreeTableView);


        createTrendObject.setText("Create Trend Objects");
        createTrendObject.setOnAction(event -> {


            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {


                    try {
                        DateTime dateTime = DateTime.now();

                        if (targetDataObject != null) {
                            JEVisClass trendClass = trendRoot.getDataSource().getJEVisClass(LOYTEC_XML_DL_DIRECTORY);
                            JEVisClass dataClass = trendRoot.getDataSource().getJEVisClass(DATA_DIRECTORY);

                            JEVisObject rootTrendObject = trendRoot.buildObject(IMPORTED_FROM_OPC_UA, trendClass);
                            JEVisObject rootDataObject = targetDataObject.buildObject(IMPORTED_FROM_OPC_UA, dataClass);
                            if (rootTrendObject.isAllowedUnder(trendRoot) && rootDataObject.isAllowedUnder(targetDataObject)) {

                                rootTrendObject.commit();
                                rootDataObject.commit();
                                createTrendDataTree(rootTreeItem, rootTrendObject, dateTime, rootDataObject);

                            }

                        } else {
                            logger.info("no target selected");
                        }


                    } catch (JEVisException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("OPC-UA Trend");
                        //alert.setHeaderText("Look, an Information Dialog");
                        alert.setContentText(count + " Trend Object with Data Objects have been created ");
                        alert.showAndWait();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(), task, taskIcon, true);

            task.setOnSucceeded(event1 -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("OPC-UA Trend");
                //alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText(count + " Trend Object with Data Objects have been created ");
                alert.showAndWait();
            });







        });

        HBox hBox = new HBox(10);

        hBox.getChildren().addAll(selectTarget, createTrendObject);

        hBox.setPadding(new Insets(10, 10, 10, 10));

        view.getChildren().add(hBox);
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
                                System.out.println("New Des: " + o.getPath() + "  -> " + o.getReferenceDescription().getBrowseName().getName());
                                PathReferenceDescription x = o;

                                if (rootSet == false) {

                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));

                                }
//                                if (o.getReferenceDescription().getBrowseName().getName().equals(opcUaRootFolder)) {
//                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
//                                }
                                else if (rootSet == true) {
                                    currentTreeItem = createOPCUAChildren(currentTreeItem, new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
                                    System.out.println(rootTreeItem.getChildren());


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
            //System.out.println("List: " + list.size());
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
                } catch (UaException e) {
                    e.printStackTrace();
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
     * copy node into clipboard of Node
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
        System.out.println("set Root");
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

        //System.out.println(node.pathProperty.get());
        //System.out.println(parent.getValue().pathProperty.get() + "/" + parent.getValue().descriptionProperty.get().getBrowseName().getName());

        if ((parent.getValue().pathProperty.get() + "/" + parent.getValue().descriptionProperty.get().getBrowseName().getName()).equals(node.pathProperty.get())) {
            TreeItem<Node> treeItem = new TreeItem<>(node);
            logger.info("OPC-UA children: {} added", node.descriptionProperty.get().getBrowseName());
            parent.getChildren().add(treeItem);
            return treeItem;
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

        //System.out.println(node.getValue().descriptionProperty.get().getBrowseName().getName());

        if (node.getChildren().size() > 0) {


            if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 1) {
                logger.info("OPC-UA Node: {} is a Folder", node.getChildren().get(0).getValue().descriptionProperty.get().getDisplayName());
                if (node.getValue().isSelected()) {
                    dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_DIRECTORY);
                    dataJEVisObject = createJEVisObject(node, dataJEVisObject, DATA_DIRECTORY);

                }
            } else if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 2) {
                logger.info("OPC-UA Node: {} is not Folder", node.getChildren().get(0).getValue().descriptionProperty.get().getDisplayName());
                if (node.getValue().isSelected()) {
                    dataSourceJEVisObject = createJEVisObject(node, dataSourceJEVisObject, LOYTEC_XML_DL_CHANNEL);
                    dataJEVisObject = createJEVisObject(node, dataJEVisObject, "Data");
                    JEVisAttribute jeVisAttributeTarget = dataSourceJEVisObject.getAttribute(TARGET_ID);
                    jeVisAttributeTarget.buildSample(dateTime, dataJEVisObject.getID() + ":Value").commit();
                    createJEVisObject(dataJEVisObject, "Clean Data", I18n.getInstance().getString("tree.treehelper.cleandata.name"));


                    if (node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).count() > 0) {
                        logger.info("OPC-UA Node: {} is trend Object", node.getChildren().get(0).getValue().descriptionProperty.get().getDisplayName());
                        Node csvNode = node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).findFirst().get();

                        if (dataSourceJEVisObject.getAttribute(TREND_ID) != null) {
                            System.out.println(csvNode.readData());
                            JEVisAttribute jeVisAttribute = dataSourceJEVisObject.getAttribute(TREND_ID);
                            String csvString = csvNode.readData().split("\\.|\\/")[csvNode.readData().split("\\.|\\/.").length - 2];
                            logger.info("Trend Id: {} added in Sample", csvString);

                            JEVisSample jeVisSample = jeVisAttribute.buildSample(dateTime, csvString);
                            jeVisSample.commit();
                        }
                    }


                }
            }


            for (TreeItem<Node> nodeChild : node.getChildren()) {
                createTrendDataTree(nodeChild, dataSourceJEVisObject, dateTime, dataJEVisObject);
            }
        }


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


        JEVisObject newObject = jevisParentObject.buildObject(node.getValue().descriptionProperty.get().getBrowseName().getName(), dataClass);
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
            System.out.println(newObject);
            newObject.commit();
        }

        return newObject;

    }

    /**
     * select children if parent is selected
     *
     * @param nodeTreeItem
     */
    public void childrenSetSelected(TreeItem<Node> nodeTreeItem) {
        for (int i = 0; i < nodeTreeItem.getChildren().size(); i++) {
            nodeTreeItem.getChildren().get(i).getValue().setSelected(nodeTreeItem.getValue().isSelected());
            if (nodeTreeItem.getChildren().get(i).getChildren().size() > 0) {
                childrenSetSelected(nodeTreeItem.getChildren().get(i));
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
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllObjects();


                SelectTargetDialog selectionDialog = new SelectTargetDialog(dialogContainer, allFilter, allDataFilter, null, SelectionMode.SINGLE, ds, new ArrayList<UserSelection>());
                //selectionDialog.show();
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
//                                if (us.getSelectedAttribute() != null) {
//                                    logger.trace("att: {}", us.getSelectedAttribute().getName());
//                                    target = us.getSelectedObject();
//                                    buttonText += "." + target.getName();
//                                }

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


}
