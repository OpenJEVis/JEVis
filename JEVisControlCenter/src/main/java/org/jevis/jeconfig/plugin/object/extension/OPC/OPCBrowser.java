package org.jevis.jeconfig.plugin.object.extension.OPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.csv.CSVColumnHeader;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.tool.ImageConverter;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

import java.util.concurrent.ExecutionException;

public class OPCBrowser {

    private static final Logger logger = LogManager.getLogger(CSVColumnHeader.class);

    public static String ICON = "Loytec XML-DL Server.png";
    private OPCClient opcClient;
    public final JEVisObject opcServerObj;
    public static final String DEFAULT_OPC_PORT = "4840";
    public static final String ROOT_FOLDER_TREND = "/Objects/Loytec ROOT/Trend";
    public static final String ROOT_FOLDER_TREND_BACNET = "/Objects/Loytec ROOT/BACnet Port";

    JFXTextField port = new JFXTextField();
    JFXButton connect = new JFXButton();
    JFXComboBox<String> comboRootFolder = new JFXComboBox<>();
    JFXComboBox<String> comboMode = new JFXComboBox<>();


    private JEVisDataSource ds;

    private EndpointDescription endpointDescription;


    Dialog opcUaBrowserDialog;


    public OPCBrowser(JEVisObject opcServerObj) {
        this.opcServerObj = opcServerObj;


        try {
            ds = opcServerObj.getDataSource();
        } catch (JEVisException e) {
            e.printStackTrace();
        }


        final Stage stage;
        stage = new Stage();

        VBox vBox = new VBox();

        stage.setOnHiding(event -> {

            if (opcClient != null) {

                opcClient.close();
            }


        });
        try {
            stage.initOwner(JEConfig.getStage());

            Node header = DialogHeader.getDialogHeader(ImageConverter.convertToImageView(opcServerObj.getJEVisClass().getIcon(), 64, 64), I18n.getInstance().getString("plugin.object.opcua.mode.title"));

            vBox.getChildren().add(header);

            vBox.setSpacing(10);

            Scene scene = new Scene(vBox);
            TopMenu.applyActiveTheme(scene);
            stage.setScene(scene);
            //TODo better be dynamic

            stage.initStyle(StageStyle.UTILITY);
            stage.setResizable(true);
            stage.setWidth(650);
            stage.setHeight(800);
        } catch (Exception e) {
            logger.error(e);
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
                                setText(toUserString(item));
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


            port.setPromptText(I18n.getInstance().getString("plugin.object.opcua.port"));
            port.setText(DEFAULT_OPC_PORT);

            connect.setText(I18n.getInstance().getString("plugin.object.opcua.connect"));

            connect.setOnAction(event -> {

                try {
                    OPCUAServer opcuaServer = new OPCUAServer(this.opcServerObj);
                    opcClient = new OPCClient(opcuaServer.getURL().replace(this.opcServerObj.getAttribute("Port").getLatestSample().getValue().toString(), port.getText()));//"opc.tcp://10.1.2.128:4840");
                    endpointDescription = opcClient.autoSelectEndpoint();


                    UsernameProvider usernameProvider = new UsernameProvider(this.opcServerObj.getAttribute("User").getLatestSample().getValue().toString(), this.opcServerObj.getAttribute("Password").getLatestSample().getValue().toString());
                    opcClient.setEndpoints(endpointDescription);

                    if (!this.opcServerObj.getAttribute("User").getLatestSample().getValue().toString().isEmpty() && !this.opcServerObj.getAttribute("Password").getLatestSample().getValue().toString().isEmpty()) {
                        opcClient.setIdentification(usernameProvider);
                    }


                    opcClient.connect();


                    NodeTreeTable nodeTable = new NodeTreeTable(opcClient, opcServerObj, comboRootFolder.getValue(), ROOT_FOLDER_TREND_BACNET, comboMode.getValue());
                    if (vBox.getChildren().size() > 2) {
                        vBox.getChildren().set(2, nodeTable.getView());
                        VBox.setVgrow(nodeTable.getView(), Priority.ALWAYS);
                    } else {
                        vBox.getChildren().add(nodeTable.getView());
                        VBox.setVgrow(nodeTable.getView(), Priority.ALWAYS);
                    }


                } catch (JEVisException | ExecutionException | InterruptedException | UaException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);

                    alert.setTitle(I18n.getInstance().getString("plugin.object.opcua.error.server.title"));
                    alert.setHeaderText(I18n.getInstance().getString("plugin.object.opcua.error.server.message"));
                    alert.setContentText(e.getMessage());

                    alert.showAndWait();

                    logger.error(e);
                }


            });

            comboMode.getItems().addAll(NodeTreeTable.SETUP_MODE, NodeTreeTable.BROWSER_MODE);
            comboMode.setValue(comboMode.getItems().get(0));
            comboRootFolder.managedProperty().bind(comboRootFolder.visibleProperty());
            comboMode.setOnAction(event -> {
                if (comboMode.getValue().equals(NodeTreeTable.BROWSER_MODE)) {
                    comboRootFolder.setVisible(true);
                    comboRootFolder.setValue(comboRootFolder.getItems().get(0));
                } else if (comboMode.getValue().equals(NodeTreeTable.SETUP_MODE)) {
                    comboRootFolder.setVisible(false);
                    comboRootFolder.setValue("/Objects/Loytec ROOT/Trend");
                }

            });
            comboRootFolder.setVisible(false);
            comboRootFolder.getItems().addAll("/Objects/Loytec ROOT","/Objects/Loytec ROOT/LIOB-IP","/Objects/Loytec ROOT/LIOB","/Objects/Loytec ROOT/Local IO","/Objects/Loytec ROOT/BACnet Port","/Objects/Loytec ROOT/CEA709 Port","/Objects/Loytec ROOT/Trend","/Objects/Loytec ROOT/Scheduler","/Objects/Loytec ROOT/User Registers", "/Objects/Loytec ROOT/System Registers");
            comboRootFolder.setValue(ROOT_FOLDER_TREND);
            comboRootFolder.setOnAction(event -> {
                logger.debug(comboRootFolder.getValue());
                    }

            );
            flowPane.getChildren().setAll(comboMode,comboRootFolder, port, connect);
            flowPane.setHgap(10);
            vBox.getChildren().add(flowPane);

        } catch (Exception ex) {
            ex.printStackTrace();

        }


        stage.showAndWait();
    }


    private String toUserString(EndpointDescription ep) {
        String product = ep.getServer().getProductUri();
        String securityMode = ep.getSecurityMode().name();
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
                , product, endpoints, securityMode, security, userTokens);
        return info;
    }

    private void init() {


    }


}
