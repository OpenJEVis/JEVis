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

public class OPCBrowser {

    public OPCBrowser(JEVisObject server) {

        final Stage stage = new Stage();

        stage.setTitle("");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(JEConfig.getStage());

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setWidth(365);
        stage.setHeight(500);
        //Workaround to set a dynamic size
//        stage.setHeight(460 + ((gy - 3) * 17));




        /**
        Dialog<ButtonType> dialog = new Dialog();
        dialog.initOwner(JEConfig.getStage());
        dialog.setTitle(I18n.getInstance().getString("jevistree.dialog.new.title"));
        dialog.setHeaderText(I18n.getInstance().getString("jevistree.dialog.new.header"));
        dialog.getDialogPane().getButtonTypes().setAll();
        dialog.setResizable(true);
        String ICON = "1403104602_brick_add.png";
        dialog.setGraphic(ResourceLoader.getImage(ICON, 50, 50));

*/
        OPCClient opcClient = new OPCClient("opc.tcp://10.1.2.128:4840");
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
        }catch ( Exception ex){
            ex.printStackTrace();
            //opcClient.close();
        }

        stage.sizeToScene();
        stage.showAndWait();
        /**
        dialog.onCloseRequestProperty().addListener(observable -> dialog.close());
        dialog.getDialogPane().setContent(borderPane);

        dialog.show();
         **/
    }
}
