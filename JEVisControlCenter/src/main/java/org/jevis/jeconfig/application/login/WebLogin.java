package org.jevis.jeconfig.application.login;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.utils.Benchmark;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.ScreenSizeManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebLogin extends Application {

    private Stage primaryStage = null;
    private AnchorPane root = new AnchorPane();
    private JEVisDataSource ds;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("WebLogin.start");
        this.primaryStage = primaryStage;
        Locale.setDefault(Locale.GERMANY);

        root.setStyle("-fx-background-color: yellow;");
        Layouts.setAnchor(root, 0);

        Pane loginForm = getLoginForm();

        root.setStyle("-fx-background-color: linear-gradient(to top, #1a719c, #7abdde);");

        setCenterPane(loginForm);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMaximized(true);

        Platform.runLater(() -> {
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("/styles/charts.css");
        });

        /**Does not work jet because the Screen does not update**/
        //ScreenSizeManager.getInstance().bindToScreenSize(primaryStage);


        Platform.runLater(() -> {
            Screen screen = Screen.getPrimary();
            primaryStage.setWidth(screen.getBounds().getWidth());
            primaryStage.setHeight(screen.getBounds().getHeight());

            primaryStage.show();

            primaryStage.setMaximized(true);
        });

    }

    private void setCenterPane(Pane pane) {
        HBox hBox = new HBox();
        VBox vBox = new VBox();
        hBox.setFillHeight(true);
        vBox.setFillWidth(true);

        Region lSpacer = new Region();
        Region rSpacer = new Region();
        Region bSpacer = new Region();
        Region tSpacer = new Region();
        HBox.setHgrow(lSpacer, Priority.ALWAYS);
        HBox.setHgrow(rSpacer, Priority.ALWAYS);
        VBox.setVgrow(tSpacer, Priority.ALWAYS);
        VBox.setVgrow(bSpacer, Priority.ALWAYS);


        vBox.getChildren().addAll(tSpacer, pane, bSpacer);

        hBox.getChildren().addAll(lSpacer, vBox, rSpacer);

        root.getChildren().setAll(hBox);
        Layouts.setAnchor(hBox, 0);
    }

    private Parent getLoginFormFXML() {
        try {

            System.out.println("URI: " + WebLogin.class.getResource("/fxml/WebLogin.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(WebLogin.class.getResource("/fxml/WebLogin.fxml"));


            Parent root = fxmlLoader.load();

//            Parent root = FXMLLoader.load(getClass().getResource("WebLogin.fxml"));
            return root;

        } catch (Exception ex) {
            ex.printStackTrace();
            return new Pane();
        }
    }

    private void showLPreloadStatus() {
        System.out.println("##showLPreloadStatus");
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        gridPane.setStyle("-fx-background-color: WHITE; -fx-effect: dropshadow(gaussian, rgb(0.0, 0.0, 0.0, 0.15), 6.0, 0.7, 0.0,1.5); -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 8;");


        ProgressIndicator progressIndicatorClass = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ProgressIndicator progressIndicatorAttribute = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ProgressIndicator progressIndicatorRelations = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ProgressIndicator progressIndicatorObject = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

        Label labelClasses = new Label("Load Classes");
        Label labelAttributes = new Label("Load Classes");
        Label labelRelationships = new Label("Load Classes");
        Label labelObjects = new Label("Load Classes");

        gridPane.addRow(0, labelClasses, progressIndicatorClass);
        gridPane.addRow(1, labelRelationships, progressIndicatorRelations);
        gridPane.addRow(2, labelObjects, progressIndicatorObject);
        gridPane.addRow(3, labelAttributes, progressIndicatorAttribute);

        Platform.runLater(() -> setCenterPane(gridPane));


        try {
            System.out.println("##Start preload");
            Benchmark benchmark = new Benchmark();
            ds.getJEVisClasses();
            Platform.runLater(() -> progressIndicatorClass.setProgress(1));

            ds.getRelationships();
            Platform.runLater(() -> progressIndicatorRelations.setProgress(1));

            ds.getObjects();
            Platform.runLater(() -> progressIndicatorObject.setProgress(1));

            ds.getAttributes();
            Platform.runLater(() -> progressIndicatorAttribute.setProgress(1));

            benchmark.printBechmark("Done preload");
//            JEConfig jeConfig = new JEConfig();
//            jeConfig.initJEVisCC(primaryStage, root, ds);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Pane getLoginForm() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(25));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        gridPane.setStyle("-fx-background-color: WHITE; -fx-effect: dropshadow(gaussian, rgb(0.0, 0.0, 0.0, 0.15), 6.0, 0.7, 0.0,1.5); -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 8;");
        JFXTextField userNameField = new JFXTextField("Username");
        userNameField.setLabelFloat(true);
//        userNameField.setPromptText("Username");
        userNameField.setAlignment(Pos.CENTER);
        JFXPasswordField jfxPasswordField = new JFXPasswordField();
        jfxPasswordField.setLabelFloat(true);
        jfxPasswordField.setPromptText("Password");

        userNameField.setAlignment(Pos.CENTER);
        jfxPasswordField.setAlignment(Pos.CENTER);

//        ImageView imageView = JEConfig.getImage("User.png",75,75);
        ImageView imageView = JEConfig.getImage("openjevislogo_simple2.png", 99, 230);


        Button login = new Button("Login");
        login.setDefaultButton(true);
        login.setBackground(new Background(new BackgroundFill(Paint.valueOf("#099109"), new CornerRadii(8d), new Insets(0))));
        GridPane.setHalignment(imageView, HPos.CENTER);
        GridPane.setHalignment(userNameField, HPos.CENTER);
        GridPane.setHalignment(jfxPasswordField, HPos.CENTER);
        GridPane.setHalignment(login, HPos.CENTER);

        gridPane.addRow(0, imageView);
        gridPane.addRow(1, userNameField);
        gridPane.addRow(2, jfxPasswordField);
        gridPane.addRow(3, login);

//        gridPane.setScaleX(1.2d);
//        gridPane.setScaleY(1.2d);


        EventHandler<KeyEvent> loginEvent = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                Platform.runLater(() -> login.setDisable(true));
                login(userNameField.getText(), jfxPasswordField.getText());
                Platform.runLater(() -> login.setDisable(false));
            }
        };

//        userNameField.setOnKeyPressed(keyEvent -> {
//            if (!jfxPasswordField.getText().isEmpty()) {
//                loginEvent.handle(keyEvent);
//            } else {
//                Platform.runLater(() -> {
//                    jfxPasswordField.requestFocus();
//                });
//            }
//        });


        login.minWidthProperty().bind(jfxPasswordField.widthProperty());
        login.setOnKeyPressed(loginEvent);
//        login.setDisable(true);
        login.setOnAction(actionEvent -> {
            System.out.println("LoginButton Action");
            login.setDisable(true);
            login.setText("Lade.....");

            System.out.println("login button done");

            Executors.newFixedThreadPool(1).execute(() -> {
                System.out.println("Start loogin");
                if (!login(userNameField.getText(), jfxPasswordField.getText())) {
                    Platform.runLater(() -> {
                        login.setDisable(false);
                        login.setText("Login");
                    });
                }
            });

        });
        jfxPasswordField.setOnKeyPressed(loginEvent);

        return gridPane;
    }

    private boolean login(String username, String password) {

        try {
            System.out.println("\n\n\n\n\n\n#########################################Start JEConfig");
            ds = new JEVisDataSourceWS("http://10.1.1.236:8000");
            ((JEVisDataSourceWS) ds).setLoginPreload(true);
//            System.out.println("Login: " + ds.connect("jsc", "EarlyBird8!"));
            Benchmark benchmark = new Benchmark();
            if (ds.connect(username, password)) {
                benchmark.printBechmark("Done login with preload");
//                showLPreloadStatus();//we are to fast
                JEConfig jeConfig = new JEConfig();
                jeConfig.initJEVisCC(primaryStage, root, ds);

                return true;
            }else{
                System.out.println("login failed");
            }


//                Scene scene2 = new Scene(new BorderPane(new TextField("test")));
//                primaryStage.setScene(scene2);

        } catch (Exception ex) {
            try {
                System.out.println("WRITE error");
                ex.printStackTrace();
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/tmp/hmmmmm.log")));
                writer.write(ExceptionUtils.getStackTrace(ex));

                writer.close();
                System.out.println("Done wrte errror");
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }

        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Weblogin.main JAVA 11 JEConfig.Main: " + args);
        Application.launch(args);
//        launch(args);
    }

}
