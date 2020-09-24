package org.jevis.jeconfig.plugin.object.extension.OPC;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

public class OPCBrowser {

    private JEVisObject opcServerObj = null;

    public OPCBrowser(JEVisObject server) {
        this.opcServerObj = server;
        final Stage stage = new Stage();


        stage.setTitle("");
        stage.initModality(Modality.NONE);
        stage.initOwner(JEConfig.getStage());

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setWidth(365);
        stage.setHeight(500);

        OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);
        OPCClient opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
        try {

            EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
            opcClient.setEndpoints(endpointDescription);
            opcClient.connect();

            NodeTable nodeTable = new NodeTable(opcClient);
            DataValueTable dataValueTable = new DataValueTable(opcClient);
            nodeTable.setDataValueTable(dataValueTable);

            borderPane.setCenter(nodeTable.getView());
            borderPane.setRight(dataValueTable.getView());
            //borderPane.setStyle("-fx-background-color:blue;");
            //opcClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //opcClient.close();
        }

        stage.sizeToScene();
        stage.showAndWait();
    }
}
