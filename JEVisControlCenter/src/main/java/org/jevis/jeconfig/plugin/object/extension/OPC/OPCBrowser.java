package org.jevis.jeconfig.plugin.object.extension.OPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OPCBrowser {

    private JEVisObject opcServerObj = null;
    private OPCClient opcClient;
    JFXTextField port = new JFXTextField();
    JFXButton connect = new JFXButton();
    JFXComboBox<String> rootFolder = new JFXComboBox();
    private List<UserSelection> userSelections = new ArrayList<>();
    JFXButton button = new JFXButton();

    private EndpointDescription endpointDescription;

    public OPCBrowser(JEVisObject server) {




        this.opcServerObj = server;
        final Stage stage = new Stage();


        stage.setTitle("");
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        StackPane stackPane = new StackPane();
        VBox vBox = new VBox();
        stackPane.getChildren().add(vBox);

        Scene scene = new Scene(stackPane);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setWidth(650);
        stage.setHeight(500);
        stage.setAlwaysOnTop(true);





        button.setOnAction(event -> {
            try {
                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllObjects();
                JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
                allFilter.add(allDataFilter);
                allFilter.add(allAttributesFilter);


                SelectTargetDialog selectTargetDialog = new SelectTargetDialog(stackPane, allFilter, allDataFilter, null, SelectionMode.SINGLE, opcServerObj.getDataSource(), userSelections);
                selectTargetDialog.show();
                selectTargetDialog.setOnDialogClosed(event1 -> {
                    System.out.println(selectTargetDialog.getResponse());

                    if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        System.out.println(selectTargetDialog.getUserSelection().get(0).getSelectedObject().getID());
                    } else if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.CANCEL) {

                    }



                });



            } catch (JEVisException e) {
                e.printStackTrace();
            }


        });

        //OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
        //OPCClient opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
        try {

            /**
             EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
             opcClient.setEndpoints(endpointDescription);
             opcClient.connect();
             **/
            //System.out.println("Connect");

            //JFXComboBox<EndpointDescription> alignmentBox = new JFXComboBox<>();
            //alignmentBox.setPrefWidth(1000);
            //alignmentBox.setMinWidth(100);

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
            //alignmentBox.setCellFactory(cellFactory);
            //alignmentBox.setButtonCell(cellFactory.call(null));
/*            alignmentBox.valueProperty().addListener((observable, oldValue, newValue) -> {
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
            });*/
            //alignmentBox.getSelectionModel().selectFirst();

            FlowPane flowPane = new FlowPane();


            //username.setPromptText("Username");
            //password.setPromptText("Password");

            port.setPromptText("Port");

            connect.setText("Connect");

            connect.setOnAction(event -> {
                try {
                   ;
                    OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
                    opcClient = new OPCClient(opcuaServer.getURL().replace(opcServerObj.getAttribute("Port").getLatestSample().getValue().toString(),port.getText()));//"opc.tcp://10.1.2.128:4840");
                    endpointDescription = opcClient.autoSelectEndpoint();


                        UsernameProvider usernameProvider = new UsernameProvider(opcServerObj.getAttribute("User").getLatestSample().getValue().toString(), opcServerObj.getAttribute("Password").getLatestSample().getValue().toString());
                        opcClient.setEndpoints(endpointDescription);

                        if (!opcServerObj.getAttribute("User").getLatestSample().getValue().toString().isEmpty() && !opcServerObj.getAttribute("Password").getLatestSample().getValue().toString().isEmpty()) {
                            opcClient.setIdentification(usernameProvider);
                        }



                        opcClient.connect();


                        NodeTreeTable nodeTable = new NodeTreeTable(opcClient,server,rootFolder.getValue());


                        vBox.getChildren().add(nodeTable.getView());






                    //alignmentBox.getItems().addAll(endpoints);
                } catch (JEVisException | ExecutionException | InterruptedException | UaException e) {
                    e.printStackTrace();
                }


            });

            rootFolder.getItems().addAll("Loytec ROOT","Trend");
            rootFolder.setValue(rootFolder.getItems().get(0));
            rootFolder.setOnAction(event ->{
                System.out.println(rootFolder.getValue());
                    }

            );
            button.setText("Root");
            //flowPane.getChildren().addAll(alignmentBox, rootFolder, port, getEndpoints);
            flowPane.getChildren().addAll(rootFolder, port, connect, button);
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
