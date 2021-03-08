package org.jevis.jeconfig.plugin.object.extension.OPC;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

public class OPCBrowser {

    private JEVisObject opcServerObj = null;
    JFXTextField username = new JFXTextField();
    JFXTextField password = new JFXTextField();

    public OPCBrowser(JEVisObject server) {
        this.opcServerObj = server;
        final Stage stage = new Stage();


        stage.setTitle("");
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setWidth(365);
        stage.setHeight(500);

        OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
        OPCClient opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
        try {

            /**
             EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
             opcClient.setEndpoints(endpointDescription);
             opcClient.connect();
             **/
            System.out.println("Get Endpoints:");
            ObservableList<EndpointDescription> endpoints = FXCollections.observableArrayList(opcClient.getEndpoints());
            endpoints.forEach(endpointDescription -> {
                System.out.println("Endpoint: " + endpointDescription);
            });

            JFXComboBox<EndpointDescription> alignmentBox = new JFXComboBox<>(endpoints);
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
                    UsernameProvider usernameProvider = new UsernameProvider(username.getText(), password.getText());
                    opcClient.setEndpoints(newValue);
                    if (!username.getText().isEmpty() && !password.getText().isEmpty()) {
                        System.out.println("Use usernamen and password");
                        opcClient.setIdentification(usernameProvider);
                    }
                    opcClient.connect();


                    NodeTable nodeTable = new NodeTable(opcClient);
                    DataValueTable dataValueTable = new DataValueTable(opcClient);
                    nodeTable.setDataValueTable(dataValueTable);

                    borderPane.setCenter(nodeTable.getView());
                    borderPane.setRight(dataValueTable.getView());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            alignmentBox.getSelectionModel().selectFirst();

            FlowPane flowPane = new FlowPane();


            username.setPromptText("Username");

            password.setPromptText("Password");

            flowPane.getChildren().addAll(alignmentBox, username, password);
            borderPane.setTop(flowPane);

            //borderPane.setStyle("-fx-background-color:blue;");
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
