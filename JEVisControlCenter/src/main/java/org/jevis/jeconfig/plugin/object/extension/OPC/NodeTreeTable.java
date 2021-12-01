package org.jevis.jeconfig.plugin.object.extension.OPC;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
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


    private final TreeTableView<Node> treeTableView = new TreeTableView<>();
    private final OPCClient opcClient;
    private TreeItem<Node> rootTreeItem;
    private TreeItem<Node> currentTreeItem;
    private boolean rootSet = false;
    private final JFXButton createTrendObject = new JFXButton();
    private final JFXButton selectTarget;

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


    public NodeTreeTable(OPCClient opcClient, JEVisObject object, String opcUaRootFolder, StackPane dialogContainer) {

        this.opcClient = opcClient;
        this.object = object;
        this.opcUARootFolder = opcUaRootFolder;
        this.dialogContainer = dialogContainer;
        selectTarget = buildTargetButton();
        try {
            ds = object.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }


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

                        if (!empty && getTreeTableRow().getTreeItem()!=null) {
                            try {

                                if (getTreeTableRow().getTreeItem().getValue().descriptionProperty.get().getNodeClass().getValue() == 1) {
                                    JFXCheckBox box = new JFXCheckBox();
                                    box.setSelected(item);
                                    box.setOnAction(event -> {


                                        getTreeTableRow().getTreeItem().getValue().setSelected(box.isSelected());

                                        childrenSetSelected(getTreeTableRow().getTreeItem());
                                        treeTableView.refresh();

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


        treeTableView.setEditable(true);
        checkCol.setEditable(true);

        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().descriptionProperty.get().getBrowseName().getName()));
        valueCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().readData()));
        nodeIDCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().nodeIdProperty.get().toParseableString()));


        nameCol.setPrefWidth(200);
        valueCol.setPrefWidth(80);
        checkCol.setPrefWidth(150);
        treeTableView.getColumns().addAll(nameCol, checkCol, valueCol,nodeIDCol);


        //view.setPadding(new Insets(8));
        view.getChildren().add(treeTableView);


        createTrendObject.setText("Create Trend Objects");
        createTrendObject.setOnAction(event -> {


            try {
                DateTime dateTime = DateTime.now();


                JEVisClass trendClass = object.getDataSource().getJEVisClass(LOYTEC_XML_DL_DIRECTORY);
                JEVisClass dataClass = object.getDataSource().getJEVisClass(DATA_DIRECTORY);

                JEVisObject rootTrendObject = object.buildObject(IMPORTED_FROM_OPC_UA, trendClass);
                JEVisObject rootDataObject = targetDataObject.buildObject(IMPORTED_FROM_OPC_UA, dataClass);
                if (rootTrendObject.isAllowedUnder(object)&& rootDataObject.isAllowedUnder(targetDataObject)) {

                    rootTrendObject.commit();
                    rootDataObject.commit();
                    createTrendDataTree(rootTreeItem, rootTrendObject, dateTime, rootDataObject);
                }

            } catch (JEVisException e) {
                e.printStackTrace();
            }


        });

        view.getChildren().addAll(createTrendObject,selectTarget);
        GridPane.setFillWidth(treeTableView, true);
        GridPane.setFillHeight(treeTableView, true);


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
                                if (o.getReferenceDescription().getBrowseName().getName().equals(opcUaRootFolder)) {
                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
                                } else if (rootSet == true) {
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
                        opcClient.browse(list, opcUaRootFolder);
                        list.forEach(System.out::println);

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

    public VBox getView() {
        return view;
    }

    public void setData() {

    }

    private TreeItem<Node> setRoot(Node node) {
        System.out.println("set Root");
        rootTreeItem = new TreeItem<>(node);
        rootSet = true;
        treeTableView.setRoot(rootTreeItem);

        return rootTreeItem;

    }


    private TreeItem<Node> createOPCUAChildren(TreeItem<Node> parent, Node node) {

        if ((parent.getValue().pathProperty.get() + "/" + parent.getValue().descriptionProperty.get().getBrowseName().getName()).equals(node.pathProperty.get())) {
            TreeItem<Node> treeItem = new TreeItem<>(node);
            parent.getChildren().add(treeItem);
            return treeItem;
        } else if (parent.getValue().pathProperty.get().contains(rootTreeItem.getValue().pathProperty.get())) {
            return createOPCUAChildren(parent.getParent(), node);
        } else {
            return parent;
        }
    }

    private void createTrendDataTree(TreeItem<Node> node, JEVisObject trendObject, DateTime dateTime, JEVisObject dataObject) throws JEVisException {

        System.out.println(node.getValue().descriptionProperty.get().getBrowseName().getName());

        if (node.getChildren().size() > 0) {


            if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 1) {
                if (node.getValue().isSelected()) {
                    trendObject = createJEVisObject(node, trendObject, LOYTEC_XML_DL_DIRECTORY);
                    dataObject = createJEVisObject(node, dataObject, DATA_DIRECTORY);

                }
            } else if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 2) {
                System.out.println("2");
                if (node.getValue().isSelected()) {
                    trendObject = createJEVisObject(node, trendObject, LOYTEC_XML_DL_CHANNEL);
                    dataObject = createJEVisObject(node, dataObject, "Data");
                    JEVisAttribute jeVisAttributeTarget = trendObject.getAttribute(TARGET_ID);
                  jeVisAttributeTarget.buildSample(dateTime,dataObject.getID()+":Value").commit();



                    if (node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).count() > 0) {
                        Node csvNode = node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).findFirst().get();

                        if (trendObject.getAttribute(TREND_ID) != null) {
                            JEVisAttribute jeVisAttribute = trendObject.getAttribute(TREND_ID);
                            System.out.println(csvNode.readData());
                            String csvString = csvNode.readData().split("/")[csvNode.readData().split("/").length - 1].substring(0, 4);

                            JEVisSample jeVisSample = jeVisAttribute.buildSample(dateTime, csvString);
                            jeVisSample.commit();
                        }
                    }


                }
            }


            for (TreeItem<Node> nodeChild : node.getChildren()) {
                createTrendDataTree(nodeChild, trendObject, dateTime, dataObject);
            }
        }


    }

    private JEVisObject createJEVisObject(TreeItem<Node> node, JEVisObject object, String className) throws JEVisException {

        JEVisClass dataClass = object.getDataSource().buildClass(className);
        JEVisObject newObject = object.buildObject(node.getValue().descriptionProperty.get().getBrowseName().getName(), dataClass);
        if (newObject.isAllowedUnder(object)) {
            newObject.commit();
            return newObject;
        } else {
            return object;
        }

    }


    public void childrenSetSelected(TreeItem<Node> nodeTreeItem) {
        for (int i = 0; i < nodeTreeItem.getChildren().size(); i++) {
            nodeTreeItem.getChildren().get(i).getValue().setSelected(nodeTreeItem.getValue().isSelected());
            if (nodeTreeItem.getChildren().get(i).getChildren().size() > 0) {
                childrenSetSelected(nodeTreeItem.getChildren().get(i));
            }
        }


    }

    private JFXButton buildTargetButton() {
        final JFXButton button = new JFXButton(I18n.getInstance().getString("csv.import_target"));//, JEConfig.getImage("1404843819_node-tree.png", 15, 15));
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
