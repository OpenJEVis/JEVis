package org.jevis.jeconfig.plugin.object.extension.OPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

import java.util.concurrent.ExecutionException;

public class OPCBrowser {

    private JEVisObject opcServerObj = null;
    private OPCClient opcClient;
    //JFXTextField username = new JFXTextField();
    //JFXTextField password = new JFXTextField();
    JFXTextField port = new JFXTextField();
    JFXButton getEndpoints = new JFXButton();
    JFXComboBox<String> rootFolder = new JFXComboBox();

    public OPCBrowser(JEVisObject server) {
        this.opcServerObj = server;
        final Stage stage = new Stage();


        stage.setTitle("");
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setWidth(450);
        stage.setHeight(500);
        stage.setAlwaysOnTop(true);

        //OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
        //OPCClient opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
        try {

            /**
             EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
             opcClient.setEndpoints(endpointDescription);
             opcClient.connect();
             **/
            System.out.println("Get Endpoints:");

            JFXComboBox<EndpointDescription> alignmentBox = new JFXComboBox<>();
            alignmentBox.setPrefWidth(1000);
            alignmentBox.setMinWidth(100);

            Callback<ListView<EndpointDescription>, ListCell<EndpointDescription>> cellFactory = new Callback<ListView<EndpointDescription>, ListCell<EndpointDescription>>() {
                @Override
                public ListCell<EndpointDescription> call(ListView<EndpointDescription> param) {
                    final ListCell<EndpointDescription> cell = new ListCell<EndpointDescription>() {

                        @Override
                        protected void updateItem(EndpointDescription item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null && !empty) {
                                //String security = item.getSecurityMode() + " " + item.getSecurityLevel();
                                String security = item.getSecurityPolicyUri().split("#")[1];
                                setText(tuUserString(item));
                                setTooltip(new Tooltip(item.toString()));

                            } else {
                                setText(null);
                            }
                        }
                    };

                    return cell;
                }
            };
            alignmentBox.setCellFactory(cellFactory);
            alignmentBox.setButtonCell(cellFactory.call(null));
            alignmentBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                //System.out.println("Select Endpoint: " + observable.toString().replace(",", "\n"));
                try {
                    UsernameProvider usernameProvider = new UsernameProvider(opcServerObj.getAttribute("User").getLatestSample().getValue().toString(), opcServerObj.getAttribute("Password").getLatestSample().getValue().toString());
                    opcClient.setEndpoints(newValue);

                    if (!opcServerObj.getAttribute("User").getLatestSample().getValue().toString().isEmpty() && !opcServerObj.getAttribute("Password").getLatestSample().getValue().toString().isEmpty()) {
                        opcClient.setIdentification(usernameProvider);
                    }



                    opcClient.connect();


                    NodeTreeTable nodeTable = new NodeTreeTable(opcClient,server,rootFolder.getValue());


                    vBox.getChildren().add(nodeTable.getView());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            alignmentBox.getSelectionModel().selectFirst();

            FlowPane flowPane = new FlowPane();


            //username.setPromptText("Username");
            //password.setPromptText("Password");

            port.setPromptText("Port");

            getEndpoints.setText("get Endpoints");

            getEndpoints.setOnAction(event -> {
                try {
                   ;
                    OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
                    opcClient = new OPCClient(opcuaServer.getURL().replace(opcServerObj.getAttribute("Port").getLatestSample().getValue().toString(),port.getText()));//"opc.tcp://10.1.2.128:4840");
                    ObservableList<EndpointDescription> endpoints = FXCollections.observableArrayList(opcClient.getEndpoints());
                    endpoints.forEach(endpointDescription -> {
                        System.out.println("Endpoint: " + endpointDescription);
                    });
                    alignmentBox.getItems().addAll(endpoints);
                } catch (JEVisException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }


            });

            rootFolder.getItems().addAll("Loytec ROOT","Trend");
            rootFolder.setValue(rootFolder.getItems().get(0));
            rootFolder.setOnAction(event ->{
                System.out.println(rootFolder.getValue());
                    }

            );

            flowPane.getChildren().addAll(alignmentBox, rootFolder, port, getEndpoints);
            vBox.getChildren().add(flowPane);

            //vBox.setStyle("-fx-background-color:blue;");
            //opcClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //opcClient.close();
        }

        //stage.sizeToScene();
        stage.showAndWait();
    }


    private String tuUserString(EndpointDescription ep) {
        String product = ep.getServer().getProductUri();
        String securtyMode = ep.getSecurityMode().name();
        String security = ep.getSecurityPolicyUri().split("#")[1];
        String userTokens = "{";
        for (UserTokenPolicy userIdentityToken : ep.getUserIdentityTokens()) {
            try {
                userTokens += userIdentityToken.getPolicyId();
                userTokens += " " + userIdentityToken.getSecurityPolicyUri().split("#")[1] + " | ";
            } catch (Exception ex) {

            }
        }
        userTokens += "} ";

        String endpoints = "{";
        for (String discoveryUrl : ep.getServer().getDiscoveryUrls()) {
            endpoints += discoveryUrl + ", ";
        }
        endpoints += "} ";

        String info = String.format("Endpoints: %s | Product: %s | security: %s{%s} | userTokens: %s "
                , product, endpoints, securtyMode, security, userTokens);
        return info;
    }
}
