package org.jevis.jeconfig.plugin.object.extension.OPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.csv.CSVColumnHeader;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.tool.ImageConverter;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OPCBrowser {

    private static final Logger logger = LogManager.getLogger(CSVColumnHeader.class);

    public static String ICON = "Loytec XML-DL Server.png";


    private OPCClient opcClient;
    public final JEVisObject opcServerObj;

    JFXTextField port = new JFXTextField();
    JFXButton connect = new JFXButton();

    StackPane dialogContainer = new StackPane();

    JFXComboBox<String> rootFolder = new JFXComboBox();


    private JEVisDataSource ds;

    private EndpointDescription endpointDescription;


    JFXDialog opcUaBrowserDialog;


    public OPCBrowser(JEVisObject opcServerObj) {
        this.opcServerObj = opcServerObj;
        //init();


        try {
            ds = opcServerObj.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }


        final Stage stage;
        stage = new Stage();

        VBox vBox = new VBox();
        try {
            stage.initOwner(JEConfig.getStage());

            dialogContainer.getChildren().add(vBox);

            Node header = DialogHeader.getDialogHeader(ImageConverter.convertToImageView(opcServerObj.getJEVisClass().getIcon(), 64, 64), opcServerObj.getName());
            System.out.println(opcServerObj.getName());

            vBox.getChildren().add(header);

            vBox.setSpacing(10);

            Scene scene = new Scene(dialogContainer);
            TopMenu.applyActiveTheme(scene);
            stage.setScene(scene);
            //TODo better be dynamic

            stage.initStyle(StageStyle.UTILITY);
            stage.setResizable(true);
            stage.setWidth(650);
            stage.setHeight(800);
        } catch (JEVisException e) {
            e.printStackTrace();
        }


        try {


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


            FlowPane flowPane = new FlowPane();


            port.setPromptText("Port");

            connect.setText("Connect");

            connect.setOnAction(event -> {
                try {
                    ;
                    OPCUAServer opcuaServer = new OPCUAServer(this.opcServerObj);
                    opcClient = new OPCClient(opcuaServer.getURL().replace(this.opcServerObj.getAttribute("Port").getLatestSample().getValue().toString(), port.getText()));//"opc.tcp://10.1.2.128:4840");
                    endpointDescription = opcClient.autoSelectEndpoint();


                    UsernameProvider usernameProvider = new UsernameProvider(this.opcServerObj.getAttribute("User").getLatestSample().getValue().toString(), this.opcServerObj.getAttribute("Password").getLatestSample().getValue().toString());
                    opcClient.setEndpoints(endpointDescription);

                    if (!this.opcServerObj.getAttribute("User").getLatestSample().getValue().toString().isEmpty() && !this.opcServerObj.getAttribute("Password").getLatestSample().getValue().toString().isEmpty()) {
                        opcClient.setIdentification(usernameProvider);
                    }


                    opcClient.connect();


                    NodeTreeTable nodeTable = new NodeTreeTable(opcClient, opcServerObj, rootFolder.getValue(), dialogContainer);
                    if (vBox.getChildren().size() > 2) {
                        vBox.getChildren().set(2, nodeTable.getView());
                        VBox.setVgrow(nodeTable.getView(), Priority.ALWAYS);
                    } else {
                        vBox.getChildren().add(nodeTable.getView());
                        VBox.setVgrow(nodeTable.getView(), Priority.ALWAYS);
                    }


                } catch (JEVisException | ExecutionException | InterruptedException | UaException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);

                    System.out.println(I18n.getInstance().getString("plugin.object.opcua.error.title"));
                    alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.error.title"));
                    alert.setHeaderText(I18n.getInstance().getString("plugin.object.opcua.error.message"));
                    alert.setContentText(e.getMessage());

                    alert.showAndWait();


                    e.printStackTrace();
                }


            });

            rootFolder.getItems().addAll("/Objects/Loytec ROOT/Trend", "/Objects/Loytec ROOT", "/Objects/Loytec ROOT/User Registers");
            rootFolder.setValue(rootFolder.getItems().get(0));
            rootFolder.setOnAction(event -> {
                        System.out.println(rootFolder.getValue());
                    }

            );
            flowPane.getChildren().addAll(rootFolder, port, connect);
            vBox.getChildren().add(flowPane);

        } catch (Exception ex) {
            ex.printStackTrace();

        }


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

    private void init() {


    }


}
