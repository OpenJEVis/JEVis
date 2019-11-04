package org.jevis.jeconfig.application.login;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.datasource.DataSourceLoader;
import org.jevis.commons.utils.Benchmark;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeconfig.Configuration;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.ParameterHelper;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.Layouts;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import static java.util.Locale.GERMANY;
import static java.util.Locale.UK;

public class WebLogin extends Application {

    private final static Configuration config = new Configuration();
    private Stage primaryStage = null;
    private AnchorPane root = new AnchorPane();
    private JEVisDataSource ds;
    private static final Logger logger = LogManager.getLogger(WebLogin.class);
    private List<Locale> availableLang = new ArrayList<>();
//    private JEVisDataSource dataSource;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("WebLogin.start");
        Parameters parameters = getParameters();
        config.parseParameters(parameters);
        List<JEVisOption> options= ParameterHelper.ParseJEVisConfiguration(parameters);
        ds = loadDataSource(options);

        this.primaryStage = primaryStage;

        Locale.setDefault(Locale.GERMANY);

        availableLang.add(UK);
        availableLang.add(GERMANY);
        availableLang.add(Locale.forLanguageTag("ru"));
        availableLang.add(Locale.forLanguageTag("uk"));
        availableLang.add(Locale.forLanguageTag("th"));


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

    private JEVisDataSource loadDataSource(List<JEVisOption> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (JEVisOption opt : config) {
            logger.error("Option: {} {}", opt.getKey(), opt.getValue());
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {

                String host = opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue();
                System.out.println("Host: "+host);

//                DataSourceLoader dsl = new DataSourceLoader();
//                JEVisDataSource ds = dsl.getDataSource(opt);
//            config.completeWith(ds.getConfiguration());
                return new JEVisDataSourceWS(host);

//                ds.setConfiguration(config);
//                return ds;
            }
        }

        return null;
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


        ObservableList<Locale> options = FXCollections.observableArrayList(availableLang);

        JFXComboBox<Locale> comboBox = new JFXComboBox<Locale>(options);
//        ComboBox<Locale> comboBox = new ComboBox<Locale>(options);
        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = buildLangCellFactory();
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

        if (availableLang.contains(Locale.getDefault())) {
            comboBox.getSelectionModel().select(Locale.getDefault());
        }
        comboBox.minWidthProperty().bind(jfxPasswordField.widthProperty());
        Button login = new Button("Login");
        login.setDefaultButton(true);
        login.setBackground(new Background(new BackgroundFill(Paint.valueOf("#099109"), new CornerRadii(8d), new Insets(0))));
        GridPane.setHalignment(imageView, HPos.CENTER);
        GridPane.setHalignment(userNameField, HPos.CENTER);
        GridPane.setHalignment(jfxPasswordField, HPos.CENTER);
        GridPane.setHalignment(login, HPos.CENTER);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);

        gridPane.addRow(0, imageView);
        gridPane.addRow(1, userNameField);
        gridPane.addRow(2, jfxPasswordField);

        gridPane.add(comboBox, 0, 3, 2, 1);
        gridPane.addRow(4, login);

//        gridPane.setScaleX(1.2d);
//        gridPane.setScaleY(1.2d);


        EventHandler<KeyEvent> loginEvent = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
//                Platform.runLater(() -> login.setDisable(true));
//                login(userNameField.getText(), jfxPasswordField.getText(),comboBox.getSelectionModel().getSelectedItem());
//                Platform.runLater(() -> login.setDisable(false));
                login.fire();
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
                if (!login(userNameField.getText(), jfxPasswordField.getText(), comboBox.getSelectionModel().getSelectedItem())) {

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


    private boolean login(String username, String password, Locale locale) {

        try {
            System.out.println("#########################################Start JEConfig");
//            ds = new JEVisDataSourceWS("http://10.1.1.236:8000");
//            ds = new JEVisDataSourceWS("http://83.169.43.14:8000");
//            ds = dataSource;
//            ((JEVisDataSourceWS) ds).setLoginPreload(false);
//            System.out.println("Login: " + ds.connect("jsc", "EarlyBird8!"));
            Benchmark benchmark = new Benchmark();
            if (ds.connect(username, password)) {
                benchmark.printBechmark("Done login with preload");

//                ds.getJEVisClasses();
//                benchmark.printBechmark("Done preload jclasses");
//                ds.getRelationships();
//                benchmark.printBechmark("Done preload relationships");
//                ds.getObjects();
//                benchmark.printBechmark("Done preload objects");
//                ds.getAttributes();
//                benchmark.printBechmark("Done preload attributes");


                I18n.getInstance().loadAndSelectBundles(availableLang, locale);
                I18nWS.setDataSource((JEVisDataSourceWS) ds);


                //MDC.put("userid", username);
//                showLPreloadStatus();//we are to fast
                JEConfig jeConfig = new JEConfig();
                jeConfig.initJEVisCC(primaryStage, root, ds);

                return true;
            } else {
                System.out.println("login failed");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.getInstance().getString("app.login.error.title"));
                    alert.setHeaderText("");
                    alert.setContentText("Login Failed");
                    alert.show();
                });

            }


//                Scene scene2 = new Scene(new BorderPane(new TextField("test")));
//                primaryStage.setScene(scene2);

        } catch (Exception ex) {


            try {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.getInstance().getString("app.login.error.title"));
                    alert.setHeaderText("");
                    alert.setContentText(ex.getMessage());
                    alert.show();
                });


                System.out.println("WRITE error");
                logger.error("Login Faild: {}",ex.getMessage());
//                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/tmp/hmmmmm.log")));
//                writer.write(ExceptionUtils.getStackTrace(ex));
//
//                writer.close();
//                System.out.println("Done wrte errror");
            } catch (Exception ex2) {
//                ex2.printStackTrace();
            }

        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("Weblogin.main JAVA 11 JEConfig.Main: " + args);
        Application.launch(args);
//        launch(args);
    }

    private Callback<ListView<Locale>, ListCell<Locale>> buildLangCellFactory() {
        return new Callback<ListView<Locale>, ListCell<Locale>>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> param) {
                return new ListCell<Locale>() {
                    {
//                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(Locale item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            try {
                                HBox box = new HBox(5);
                                box.setAlignment(Pos.CENTER_LEFT);

                                Image img = new Image("/icons/" + item.getLanguage() + ".png");
                                ImageView iv = new ImageView(img);
                                iv.fitHeightProperty().setValue(20);
                                iv.fitWidthProperty().setValue(20);
                                iv.setSmooth(true);

                                Label name = new Label(item.getDisplayLanguage());
                                name.setTextFill(javafx.scene.paint.Color.BLACK);

                                box.getChildren().setAll(iv, name);
                                setGraphic(box);
                            } catch (Exception ex) {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }

}
