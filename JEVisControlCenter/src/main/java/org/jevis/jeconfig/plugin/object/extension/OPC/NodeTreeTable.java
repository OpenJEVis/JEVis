package org.jevis.jeconfig.plugin.object.extension.OPC;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.jevis.api.*;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.PathReferenceDescription;
import org.joda.time.DateTime;


public class NodeTreeTable {


    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(NodeTreeTable.class);
    private final VBox view = new VBox();


    private final TreeTableView<Node> treeTableView = new TreeTableView<>();
    private final OPCClient opcClient;
    private TreeItem<Node> rootTreeItem;
    private TreeItem<Node> currentTreeItem;
    private boolean rootSet = false;
    private final JFXButton createTrendObject = new JFXButton();

    private final ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    private final Image taskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private JEVisObject object;
    private final String rootFolder;


    public NodeTreeTable(OPCClient opcClient, JEVisObject object, String rootFolder) {

        this.opcClient = opcClient;
        this.object = object;
        this.rootFolder = rootFolder;


        TreeTableColumn<Node, String> nameCol = new TreeTableColumn<>("Name");
        TreeTableColumn<Node, String> valueCol = new TreeTableColumn<>("Value");
        TreeTableColumn<Node, Boolean> checkCol = new TreeTableColumn<>("Trend");


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


        nameCol.setPrefWidth(200);
        valueCol.setPrefWidth(80);
        checkCol.setPrefWidth(150);
        treeTableView.getColumns().addAll(nameCol, checkCol, valueCol);


        //view.setPadding(new Insets(8));
        view.getChildren().add(treeTableView);


        createTrendObject.setText("Create Trend Objects");
        createTrendObject.setOnAction(event -> {


            try {
                DateTime dateTime = DateTime.now();


                JEVisClass dataClass = object.getDataSource().getJEVisClass("Loytec XML-DL CEA709 Channel Directory");

                JEVisObject newObj = object.buildObject("Imported From OPC UA", dataClass);
                if (newObj.isAllowedUnder(object)) {

                    newObj.commit();
                    createTrendTree(rootTreeItem, newObj, dateTime);
                }

            } catch (JEVisException e) {
                e.printStackTrace();
            }


        });

        view.getChildren().add(createTrendObject);
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
                                if (o.getReferenceDescription().getBrowseName().getName().equals(rootFolder)) {
                                    currentTreeItem = setRoot(new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
                                } else if (rootSet == true) {
                                    currentTreeItem = createChildren(currentTreeItem, new Node(o.getReferenceDescription(), o.getPath(), o.getDataValue()));
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
                        opcClient.browse(list, rootFolder);
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


    private TreeItem<Node> createChildren(TreeItem<Node> parent, Node node) {

        if ((parent.getValue().pathProperty.get() + "/" + parent.getValue().descriptionProperty.get().getBrowseName().getName()).equals(node.pathProperty.get())) {
            TreeItem<Node> treeItem = new TreeItem<>(node);
            parent.getChildren().add(treeItem);
            return treeItem;
        } else if (parent.getValue().pathProperty.get().contains(rootTreeItem.getValue().pathProperty.get())) {
            return createChildren(parent.getParent(), node);
        } else {
            return parent;
        }
    }

    private void createTrendTree(TreeItem<Node> node, JEVisObject object, DateTime dateTime) throws JEVisException {

        System.out.println(node.getValue().descriptionProperty.get().getBrowseName().getName());

        if (node.getChildren().size() > 0) {


            if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 1) {
                if (node.getValue().isSelected()) {
                    object = createJEVisObject(node, object, "Loytec XML-DL CEA709 Channel Directory");

                }
            } else if (node.getChildren().get(0).getValue().descriptionProperty.getValue().getNodeClass().getValue() == 2) {
                System.out.println("2");
                if (node.getValue().isSelected()) {
                    object = createJEVisObject(node, object, "Loytec XML-DL Channel");
                    if (node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).count() > 0) {
                        Node csvNode = node.getChildren().stream().filter(nodeTreeItem -> nodeTreeItem.getValue().descriptionProperty.get().getBrowseName().getName().equals("CsvFile")).map(nodeTreeItem -> nodeTreeItem.getValue()).findFirst().get();

                        if (object.getAttribute("Trend ID") != null) {
                            JEVisAttribute jeVisAttribute = object.getAttribute("Trend ID");
                            JEVisSample jeVisSample = jeVisAttribute.buildSample(dateTime, csvNode.readData().split("/")[csvNode.readData().split("/").length - 1].replace(".csv", ""));
                            jeVisSample.commit();
                        }
                    }


                }
            }


            for (TreeItem<Node> nodeChild : node.getChildren()) {
                createTrendTree(nodeChild, object, dateTime);
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


}
