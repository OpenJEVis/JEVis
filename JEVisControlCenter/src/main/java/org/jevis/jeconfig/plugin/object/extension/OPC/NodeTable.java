package org.jevis.jeconfig.plugin.object.extension.OPC;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.controlsfx.control.HiddenSidesPane;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.PathReferenceDescription;

import java.util.ArrayList;


public class NodeTable {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(NodeTable.class);
    private GridPane view = new GridPane();
    private TableView<Node> tableView = new TableView<>();
    private OPCClient opcClient;
    private HiddenSidesPane hiddenSidesPane = new HiddenSidesPane();
    private ObservableList<Node> list = FXCollections.observableArrayList();
    private ObservableList<Node> filteredList = FXCollections.observableArrayList();
    private CheckBox filterTrends = new CheckBox();
    private TextField filterFieldGroup= new TextField();
    private ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    private final Image taskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private DataValueTable dataValueTable;


    public NodeTable(OPCClient opcClient) {
        this.opcClient= opcClient;
        filterFieldGroup.setPromptText(I18n.getInstance().getString("plugin.object.role.filterprompt"));
        filterFieldGroup.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                updateFilteredData();
            }
        });

        TableColumn<Node, String> idCol = new TableColumn("Name");//I18n.getInstance().getString("plugin.object.role.table.read"
        TableColumn<Node, String> pathCol = new TableColumn("path");
        TableColumn<Node, String> nodeIDCol = new TableColumn("NodeID");
        TableColumn<Node, String> nodeClassCol = new TableColumn("Class");
        idCol.setCellValueFactory(param -> param.getValue().idStringProperty);
        pathCol.setCellValueFactory(param -> param.getValue().pathProperty);
        nodeIDCol.setCellValueFactory(param -> param.getValue().stringNodeID);
        nodeClassCol.setCellValueFactory(param -> param.getValue().typeProperty);


        idCol.setPrefWidth(200);
        pathCol.setPrefWidth(400);
        nodeIDCol.setPrefWidth(100);


        tableView.getColumns().addAll(pathCol,idCol,nodeIDCol,nodeClassCol);
        //tableView.setPrefSize(Double.MAX_VALUE,Double.MAX_VALUE);
        tableView.getSortOrder().add(pathCol);
        tableView.setMinSize(1200,900);
        tableView.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        //hiddenSidesPane.setContent(tableView);
        view.addRow(0,filterTrends,filterFieldGroup);
        //view.setStyle("-fx-background-color:orangered;");


        //view.setGridLinesVisible(true);
        view.setPadding(new Insets(8));
        view.setHgap(8);
        view.add(tableView,0,1,3,1);
        GridPane.setFillWidth(tableView,true);
        GridPane.setFillHeight(tableView,true);
        //_view.setPinnedSide(Side.RIGHT)
        //_view.setRight(help);


        //readCol.setCellFactory(param -> new CheckBoxTableCell<>());



        try{
            ObservableList<PathReferenceDescription> testList = FXCollections.observableArrayList();
            testList.addListener(new ListChangeListener<PathReferenceDescription>() {
                @Override
                public void onChanged(Change<? extends PathReferenceDescription> c) {
                    while (c.next()) {
                        if(c.wasAdded()){
                            c.getAddedSubList().forEach(o -> {
                                list.add(new Node(o.getReferenceDescription(),o.getPath()));
                                //System.out.println("New Des: "+o.getPath()+"  -> "+o.getReferenceDescription().getBrowseName().getName());
                            });

                        }
                    }

                }
            });
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    opcClient.browse(testList);

                    super.done();
                    return null;
                }
            };
            JEConfig.getStatusBar().addTask(DashBordPlugIn.class.getName(),task, taskIcon,true);

            /**
            HashMap<String,ReferenceDescription> map = opcClient.browse();
            System.out.println("Mapsize: "+map.size());
            map.forEach((xpath, referenceDescription) -> {
                Node newNode = new Node(referenceDescription,xpath);
                list.add(newNode);
            });
             **/
            System.out.println("List: "+list.size());
        }catch (Exception ex){
            logger.error("error while browsing Client: {}",ex);
        }

        filteredList.addAll(list);
        System.out.println("filteredList; "+filteredList.size());

        tableView.setItems(filteredList);
        /**
        tableView.setRowFactory(param -> {
            TableRow<Node> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                System.out.println("Row clicked: "+event);
                if(event.getClickCount()==2 && dataValueTable!=null){
                    System.out.println("is double: "+row.getItem());
                    dataValueTable.updateTable(row.getItem().getNodeIdProperty());
                }
            });
            return row;
        });
**/

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Show History");
        contextMenu.getItems().add(menuItem);
        menuItem.setOnAction(event -> {
            TableView.TableViewSelectionModel model = tableView.getSelectionModel();
            System.out.println("model: "+model);
            System.out.println("index: "+model.getSelectedIndex());
            System.out.println("getSelectedItem: "+tableView.getSelectionModel().getSelectedItem());
            System.out.println("Selected Row: "+tableView.getSelectionModel().getSelectedItem().nodeIdProperty.get());
            dataValueTable.updateTable(tableView.getSelectionModel().getSelectedItem().nodeIdProperty.get());
        });

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton()== MouseButton.SECONDARY){
                    contextMenu.show(tableView, event.getScreenX(),event.getScreenY());
                    System.out.println("event.getSource(): "+event.getSource());
                }
            }
        });

        list.addListener((ListChangeListener<Node>) change -> updateFilteredData());


        filterTrends.selectedProperty().addListener((observable, oldValue, newValue) -> {updateFilteredData();});
        filterTrends.setSelected(true);


        updateFilteredData();

    }

    public GridPane getView(){
        return view;
    }

    public void setDataValueTable(DataValueTable dataValueTable){
        this.dataValueTable=dataValueTable;
    }

    private void updateFilteredData() {
        filteredList.clear();

        for (Node p : list) {
            boolean isFilerMatch = matchesFilter(p);
            ReferenceDescription ref = p.descriptionProperty.get();

            if (filterTrends.isSelected() &&( ref.getNodeClass().equals(NodeClass.Variable)
                    &&( p.idStringProperty.get().toLowerCase().indexOf("(trend") != -1 || p.pathProperty.get().toLowerCase().indexOf("(trend") != -1   ))) {
                filteredList.add(p);

                /**
                if (isFilerMatch  ) {
                    filteredList.add(p);
                }**/

            } else if (isFilerMatch) {
                filteredList.add(p);
            }

        }

        // Must re-sort table after items changed
        reapplyTableSortOrder();
    }

    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<Node, ?>> sortOrder = new ArrayList<>(tableView.getSortOrder());
        tableView.getSortOrder().clear();
        tableView.getSortOrder().addAll(sortOrder);
    }

    private boolean matchesFilter(Node node) {
        String filterString = filterFieldGroup.getText();
        if (filterString == null || filterString.isEmpty()) {
            // No filter --> Add all.
            return true;
        }
        String lowerCaseFilterString = filterString.toLowerCase();


        if (node.idStringProperty.get().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        } else if (node.pathProperty.get().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        }else if (node.stringNodeID.toString().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        }


        return false; // Does not match
    }

    public void setData(){

    }

}
