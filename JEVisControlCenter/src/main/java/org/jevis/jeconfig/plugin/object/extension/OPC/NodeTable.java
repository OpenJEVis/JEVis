package org.jevis.jeconfig.plugin.object.extension.OPC;


import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
    private final GridPane view = new GridPane();
    private final TableView<Node> tableView = new TableView<>();
    private final OPCClient opcClient;
    private final HiddenSidesPane hiddenSidesPane = new HiddenSidesPane();
    private final ObservableList<Node> list = FXCollections.observableArrayList();
    private final ObservableList<Node> filteredList = FXCollections.observableArrayList();
    private final JFXCheckBox filterTrends = new JFXCheckBox();
    private final JFXTextField filterFieldGroup = new JFXTextField();
    private final ObservableList<Node> nodeObservableList = FXCollections.observableArrayList();
    private final Image taskIcon = JEConfig.getImage("if_dashboard_46791.png");
    private DataValueTable dataValueTable;


    public NodeTable(OPCClient opcClient) {
        this.opcClient = opcClient;
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

        filterTrends.setText("Trend Filter");

        tableView.getColumns().addAll(pathCol, idCol, nodeIDCol, nodeClassCol);
        //tableView.setPrefSize(Double.MAX_VALUE,Double.MAX_VALUE);
        tableView.getSortOrder().add(pathCol);
        tableView.setMinSize(1200, 900);
        tableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        //hiddenSidesPane.setContent(tableView);
        view.addRow(0, filterTrends, filterFieldGroup);
        //view.setStyle("-fx-background-color:orangered;");


        //view.setGridLinesVisible(true);
        view.setPadding(new Insets(8));
        view.setHgap(8);
        view.add(tableView, 0, 1, 3, 1);
        GridPane.setFillWidth(tableView, true);
        GridPane.setFillHeight(tableView, true);
        //_view.setPinnedSide(Side.RIGHT)
        //_view.setRight(help);


        //readCol.setCellFactory(param -> new JFXCheckBoxTableCell<>());


        try {
            ObservableList<PathReferenceDescription> testList = FXCollections.observableArrayList();
            testList.addListener(new ListChangeListener<PathReferenceDescription>() {
                @Override
                public void onChanged(Change<? extends PathReferenceDescription> c) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            c.getAddedSubList().forEach(o -> {
                                list.add(new Node(o.getReferenceDescription(), o.getPath()));
                                updateFilteredData();
                                //System.out.println("New Des: "+o.getPath()+"  -> "+o.getReferenceDescription().getBrowseName().getName());
                            });

                        }
                    }

                }
            });
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        opcClient.browse(testList);

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
            System.out.println("List: " + list.size());
        } catch (Exception ex) {
            logger.error("error while browsing Client: {}", ex);
        }

        filteredList.addAll(list);
        list.addListener((ListChangeListener<Node>) change -> updateFilteredData());
        System.out.println("filteredList; " + filteredList.size());

        tableView.setItems(filteredList);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Show History");
        MenuItem copyNodeID = new MenuItem("Copy NodeID");
        contextMenu.getItems().addAll(menuItem, copyNodeID);
        menuItem.setOnAction(event -> {
            TableView.TableViewSelectionModel model = tableView.getSelectionModel();
            System.out.println("model: " + model);
            System.out.println("index: " + model.getSelectedIndex());
            System.out.println("getSelectedItem: " + tableView.getSelectionModel().getSelectedItem());
            System.out.println("Selected Row: " + tableView.getSelectionModel().getSelectedItem().nodeIdProperty.get());
            dataValueTable.updateTable(tableView.getSelectionModel().getSelectedItem().nodeIdProperty.get());
        });
        copyNodeID.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(tableView.getSelectionModel().getSelectedItem().nodeIdProperty.get().toParseableString());
            clipboard.setContent(content);
        });

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                    System.out.println("event.getSource(): " + event.getSource());
                }
            }
        });


        filterTrends.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateFilteredData();
        });
        filterTrends.setSelected(true);


        updateFilteredData();

    }

    public GridPane getView() {
        return view;
    }

    public void setDataValueTable(DataValueTable dataValueTable) {
        this.dataValueTable = dataValueTable;
    }

    private boolean isLoytecTrend(Node p) {
        return p.idStringProperty.get().toLowerCase().indexOf("(trend") != -1 && p.descriptionProperty.get().getNodeClass().equals(NodeClass.Variable);
    }

    private void updateFilteredData() {
        filteredList.clear();

        for (Node p : list) {
            boolean isFilerMatch = matchesFilter(p);
            ReferenceDescription ref = p.descriptionProperty.get();

            //System.out.println("updateFilteredData: s:" + filterTrends.isSelected() + "  l:" + isLoytecTrend(p) + "  f:" + isFilerMatch);
            if (filterTrends.isSelected() && isLoytecTrend(p) && isFilerMatch) {
                filteredList.add(p);
            }

            if (!filterTrends.isSelected() && isFilerMatch) {
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
        } else return node.stringNodeID.toString().toLowerCase().indexOf(lowerCaseFilterString) != -1;// Does not match
    }

    public void setData() {

    }

}
