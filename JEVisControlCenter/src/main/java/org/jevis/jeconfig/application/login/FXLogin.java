/**
 * Copyright (C) 20156Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.login;

import com.jfoenix.controls.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.commons.application.ApplicationInfo;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.datasource.DataSourceLoader;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.ParameterHelper;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.tool.Layouts;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The FXLogin represents the common JEVis login dialog with all necessary
 * parameters like username, password, server and so on.
 *
 * @author Florian Simon
 */
public class FXLogin extends AnchorPane {
    private static final Logger logger = LogManager.getLogger(FXLogin.class);

    private final Stage mainStage;
    private final JFXButton loginButton = new JFXButton("Login");
    private final JFXButton closeButton = new JFXButton("Close");
    private final JFXTextField userName = new JFXTextField();
    private final JFXCheckBox storeConfig = new JFXCheckBox("Remember me");
    private final JFXPasswordField userPassword = new JFXPasswordField();
    private final GridPane authGrid = new GridPane();
    private final Preferences jevisPref = Preferences.userRoot().node("JEVis");
    private JEVisDataSource _ds;
    //    private final Preferences serverPref = Preferences.userRoot().node("JEVis.Server");
    private final List<JEVisObject> rootObjects = new ArrayList<>();
    private final List<JEVisClass> classes = new ArrayList<>();
    //    private final JFXComboBox<JEVisConfiguration> serverSelection = new JFXComboBox<>();
    private final String css = "";

    private final int lastServer = -1;
    private final Stage statusDialog = new Stage(StageStyle.TRANSPARENT);

    //workaround, need some coll OO implementaion
    private final List<PreloadTask> tasks = new ArrayList<>();

    private final SimpleBooleanProperty loginStatus = new SimpleBooleanProperty(false);
//    private final String URL_SYNTAX = "user:password@server:port/jevis";

    //    private final ObservableList<JEVisConfiguration> serverConfigurations = FXCollections.observableList(new ArrayList<>());
    private VBox mainHBox = new VBox();
    private final ProgressIndicator progress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

    //Workaround replace later, in the moment i have problems showing the xeporion in an thread as an Alart
    private final Exception lastExeption = null;

    private Application.Parameters parameters;

    private List<JEVisOption> configuration;
    private JEVisOption fxoptions;
    private boolean useCSSFile = false;
    private ApplicationInfo app = new ApplicationInfo("FXLogin", "");
    private Locale selectedLocale = Locale.getDefault();
    private final NotificationPane notificationPane = new NotificationPane();


    private FXLogin() {
        this.mainStage = null;
    }

    public FXLogin(Stage stage, Application.Parameters parameters, ApplicationInfo app) {
        super();
        this.mainStage = stage;
        this.app = app;
        this.parameters = parameters;

        //this.loginButton.getStyleClass().add("button-raised");
        //loginButton.setId("ok-button");


        //loginButton.setRipplerFill(javafx.scene.paint.Paint.valueOf("#FFFFFF"));
        //closeButton.getStyleClass().add("button-raised");
        //closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        //loginButton.setButtonType(JFXButton.ButtonType.RAISED);
        loginButton.setStyle("-fx-background-color: #4dadf7");
        loginButton.setTextFill(javafx.scene.paint.Paint.valueOf("#FFFFFF"));
        closeButton.setStyle("-fx-background-color: #adb5bd");
        closeButton.setTextFill(javafx.scene.paint.Paint.valueOf("#FFFFFF"));


        this.configuration = parseConfig(parameters);
        for (JEVisOption opt : this.configuration) {
            if (opt.equals(CommonOptions.FXLogin.FXLogin)) {
                this.fxoptions = opt;
            }
        }

        try {
            this._ds = loadDataSource(this.configuration);

        } catch (ClassNotFoundException ex) {
            logger.fatal(ex);
        } catch (InstantiationException ex) {
            logger.fatal(ex);
        } catch (IllegalAccessException ex) {
            logger.fatal(ex);
        }

        setStyleSheet();

        init();

        Platform.runLater(() -> {
            this.userName.setEditable(true);
        });
    }

    /**
     * Create an new Loginpanel
     *
     * @param stage      The Stage will be used of eventuell dialogs
     * @param parameters
     */
    public FXLogin(Stage stage, Application.Parameters parameters) {
        this(stage, parameters, new ApplicationInfo("FXLogin", ""));
    }

    /**
     * This function will try to connect to the JEVisDataSource with the
     * configures server settings.
     */
    private void doLogin() {

        Platform.runLater(() -> {
            this.loginButton.setDisable(true);
            this.authGrid.setDisable(true);
            this.progress.setVisible(true);
            this.progress.setVisible(true);
        });
        //start animation, todo make an own thread..
        Runnable runnable = () -> {
            try {
                if (this._ds.connect(this.userName.getText(), this.userPassword.getText())) {
                    logger.trace("Login succeeded");
                    if (this.storeConfig.isSelected()) {
                        storePreference();
                    }

                    Platform.runLater(() -> {
                        this.loginStatus.setValue(Boolean.TRUE);
                        this.statusDialog.hide();
                    });

                } else {
                    I18n.getInstance().selectBundle(getSelectedLocale());
                    throw new RuntimeException(I18n.getInstance().getString("app.login.exception.runtime"));
                }
            } catch (Exception ex) {
                I18n.getInstance().selectBundle(getSelectedLocale());
                logger.trace("{}: {}", I18n.getInstance().getString("app.login.error.message"), ex, ex);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(I18n.getInstance().getString("app.login.error.title"));
                    alert.setHeaderText("");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();

                    this.loginButton.setDisable(false);
                    this.authGrid.setDisable(false);
                    this.progress.setVisible(false);
                    this.progress.setVisible(false);
                });

            }

        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    /**
     * Load the DataSource from an configuration object.
     *
     * @param config
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private JEVisDataSource loadDataSource(List<JEVisOption> config) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (JEVisOption opt : config) {
            logger.trace("Option: {} {}", opt.getKey(), opt.getValue());
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                DataSourceLoader dsl = new DataSourceLoader();
                JEVisDataSource ds = dsl.getDataSource(opt);
//            config.completeWith(ds.getConfiguration());

                ds.setConfiguration(config);
                return ds;
            }
        }

        return null;
    }

    /**
     * Try to set the StyleSheet is the user set the parameter
     */
    private void setStyleSheet() {
        try {
            if (this.fxoptions != null) {
                if (this.fxoptions.hasOption(CommonOptions.FXLogin.URL_CSS.getKey())) {
                    JEVisOption opt = this.fxoptions.getOption(CommonOptions.FXLogin.URL_CSS.getKey());
                    this.mainStage.getScene().getStylesheets().add(opt.getValue());
                    this.useCSSFile = true;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse a JEVisConfiguration out of the Application.Parameters
     *
     * @param parameter
     * @return
     */
    private List<JEVisOption> parseConfig(Application.Parameters parameter) {
        return ParameterHelper.ParseJEVisConfiguration(parameter);
    }

    /**
     * Returns all JEVisClasses after successfull login.
     *
     * @return List of every JEVisClass on the server
     */
    public List<JEVisClass> getAllClasses() {
        return this.classes;
    }

    /**
     * Returns all root JEVisOBjects which the user has access to after an
     * successfull login.
     *
     * @return
     */
    public List<JEVisObject> getRootObjects() {
        return this.rootObjects;
    }

    /**
     * Build an language selection box
     *
     * @return
     */
    private JFXComboBox buildLanguageBox() {

        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = new Callback<ListView<Locale>, ListCell<Locale>>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> param) {
                return new ListCell<Locale>() {
                    {
                        super.setPrefWidth(260);
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

        ObservableList<Locale> options = FXCollections.observableArrayList(I18n.getInstance().getAvailableLang());

        final JFXComboBox<Locale> comboBox = new JFXComboBox<Locale>(options);
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

        if (I18n.getInstance().getAvailableLang().contains(Locale.getDefault())) {
            comboBox.getSelectionModel().select(Locale.getDefault());
        }

        comboBox.setMinWidth(250);
        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.selectedLocale = newValue;
            //TODO reload UI
        });

        return comboBox;

    }

    /**
     * returns the JEVisDataSource after an successfull login
     *
     * @return
     */
    public JEVisDataSource getDataSource() {
        return this._ds;
    }

    private void setDefaultStyle(Node node, String style) {
        if (!this.useCSSFile) {
            node.setStyle(style);
        }
    }

    /**
     * Build an header GUI element.
     *
     * @return
     */
    private Node buildHeader() {
        AnchorPane header = new AnchorPane();
        header.setId("fxlogin-header");
        setDefaultStyle(header, "-fx-background-color: " + Color.LIGHT_BLUE);

        ImageView logo = null;

        String defaultLogo = "/icons/openjevis_longlogo.png";

        if (this.fxoptions != null) {
            if (this.fxoptions.hasOption(CommonOptions.FXLogin.URL_LOGO.getKey())) {
                JEVisOption opt = this.fxoptions.getOption(CommonOptions.FXLogin.URL_LOGO.getKey());
                if (opt.getValue() != null && !opt.getValue().isEmpty()) {
                    try {
                        logo = new ImageView(new Image(opt.getValue()));
                    } catch (Exception ex) {
                        logo = new ImageView(new Image(defaultLogo));
                    }
                } else {
                    logo = new ImageView(new Image(defaultLogo));
                }
            }
        } else {
            logo = new ImageView(new Image(defaultLogo));
        }
        if (logo != null) {
            logo.setPreserveRatio(true);
        }

        logo.fitWidthProperty().bind(this.mainStage.widthProperty());

        header.getChildren().add(logo);
        AnchorPane.setBottomAnchor(logo, 0.0);
        AnchorPane.setLeftAnchor(logo, 0.0);

        logo.setId("fxlogin-header-logo");

        return header;
    }

    /**
     * Build an footer GUI element
     *
     * @return
     */
    private Node buildFooter() {
        AnchorPane footer = new AnchorPane();
        footer.setId("fx-login-footer");
        setDefaultStyle(footer, "-fx-background-color: " + Color.LIGHT_BLUE);

        Node buildInfo = buildBuildInfo();
        buildInfo.setId("fx-login-footer-info");
        AnchorPane.setBottomAnchor(buildInfo, 5.0);
        AnchorPane.setRightAnchor(buildInfo, 5.0);
        footer.getChildren().add(buildInfo);

        return footer;
    }

    /**
     * Build an bottom button bar element
     *
     * @return
     */
    private Node buildButtonsbar() {
        Region spacer = new Region();
        setDefaultStyle(spacer, "-fx-background-color: transparent;");

        HBox buttonBox = new HBox(10);
        Node link = buildLink();
        buttonBox.getChildren().setAll(link, spacer, this.closeButton, this.loginButton);
        HBox.setHgrow(link, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(this.loginButton, Priority.NEVER);
        return buttonBox;
    }

    /**
     * Build an authentification (user, passwort-..) GUI element
     *
     * @return
     */
    private Node buildAuthForm() {

        Node buttonBox = buildButtonsbar();
//        Node serverConfigBox = buildServerSelection();
        Region serverConfigBox = new Region();
        JFXComboBox langSelect = buildLanguageBox();
        loadPreference(true);

        Label userL = new Label("Username:");
        userL.setId("fxlogin-form-user-label");
        Label passwordL = new Label("Password:");
        passwordL.setId("fxlogin-form-password-label");
        Label serverL = new Label("Server:");
        serverL.setId("fxlogin-form-server-label");
        Label languageL = new Label("Language: ");
        languageL.setId("fxlogin-form-language-label");

        this.loginButton.setDefaultButton(true);
        this.closeButton.setCancelButton(true);

        langSelect.setId("fxlogin-form-language");
        //this.loginButton.setId("fxlogin-form-login");
        //this.closeButton.setId("fxlogin-form-close");
        this.userName.setId("fxlogin-form-username");
        this.userPassword.setId("fxlogin-form-password");
        this.authGrid.setId("fxlogin-form");

        this.closeButton.setOnAction(event -> {
            this.loginStatus.setValue(Boolean.FALSE);
            this.statusDialog.hide();
            System.exit(0);//Not the fine ways but ok for now
        });


        this.userName.requestFocus();

        this.loginButton.setOnAction(event -> {
            this.loginButton.setDisable(true);
            doLogin();

        });
        this.userName.setEditable(false);

        this.authGrid.setHgap(10);
        this.authGrid.setVgap(5);
        this.authGrid.setPadding(new Insets(10, 10, 10, 10));

        int columns = 2;
        //x,x...
        int row = 0;
        this.authGrid.add(userL, 0, row);
        this.authGrid.add(this.userName, 1, row);

        row++;
        this.authGrid.add(passwordL, 0, row);
        this.authGrid.add(this.userPassword, 1, row);

        row++;
        this.authGrid.add(languageL, 0, row);
        this.authGrid.add(langSelect, 1, row);

        row++;
//        authGrid.add(serverL, 0, row);
//        authGrid.add(serverConfigBox, 1, row);
        this.authGrid.add(new Region(), 0, row);
        this.authGrid.add(new Region(), 1, row);

        row++;
//        grid.add(serverL, 0, row);
        this.authGrid.add(this.storeConfig, 1, row);

        Region cTobSpacer = new Region();
        setDefaultStyle(cTobSpacer, "-fx-background-color: transparent;");
        cTobSpacer.setPrefHeight(30);

        row++;
        this.authGrid.add(cTobSpacer, 0, row, columns, 1);

        row++;
        this.authGrid.add(buttonBox, 0, row, columns, 1);

        this.storeConfig.setId("fxlogin-form-remeberme");

        return this.authGrid;
    }

    /**
     * Initilize this class, this will build most of the GUI
     */
    private void init() {
        loadPreference(true);

        //TODO load from URL/RESOURCE
        ImageView logo = new ImageView(new Image("/icons/openjevislogo_simple2.png"));
        logo.setPreserveRatio(true);

        AnchorPane leftSpacer = new AnchorPane();
        AnchorPane rightSpacer = new AnchorPane();

        leftSpacer.setId("fxlogin-body-left");
        rightSpacer.setId("fxlogin-body-right");
        this.progress.setId("fxlogin-body-progress");

        this.progress.setPrefSize(80, 80);
        this.progress.setVisible(false);
        AnchorPane.setTopAnchor(this.progress, 70d);
        AnchorPane.setLeftAnchor(this.progress, 100d);
        rightSpacer.getChildren().setAll(this.progress);

        leftSpacer.setMinWidth(200);//todo 20%

        Node authForm = buildAuthForm();

        HBox body = new HBox();
        body.setId("fxlogin-body");
        body.getChildren().setAll(leftSpacer, authForm, rightSpacer);
        HBox.setHgrow(authForm, Priority.NEVER);
        HBox.setHgrow(leftSpacer, Priority.NEVER);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Node header = buildHeader();
        Node footer = buildFooter();

        this.mainHBox = new VBox();
        this.mainHBox.getChildren().setAll(header, body, footer);
        VBox.setVgrow(body, Priority.NEVER);
        VBox.setVgrow(header, Priority.ALWAYS);
        VBox.setVgrow(footer, Priority.ALWAYS);

        setDefaultStyle(body, "-fx-background-color: white;");
        setDefaultStyle(leftSpacer, "-fx-background-color: white;");
        setDefaultStyle(rightSpacer, "-fx-background-color: white;");
        setDefaultStyle(this.mainHBox, "-fx-background-color: yellow;");

        AnchorPane.setTopAnchor(this.mainHBox, 0.0);
        AnchorPane.setRightAnchor(this.mainHBox, 0.0);
        AnchorPane.setLeftAnchor(this.mainHBox, 0.0);
        AnchorPane.setBottomAnchor(this.mainHBox, 0.0);
        notificationPane.setContent(this.mainHBox);

        AnchorPane root = new AnchorPane(notificationPane);
        Layouts.setAnchor(notificationPane, 0);
        Layouts.setAnchor(root, 0);
        getChildren().setAll(root);

    }

    public void checkVersion() {
        try {
            String serverJECCVersion = ((JEVisDataSourceWS) _ds).getJEVisCCVersion();
            if (serverJECCVersion != "0") {
                DefaultArtifactVersion thisVersion = new DefaultArtifactVersion(JEConfig.class.getPackage().getImplementationVersion());
                DefaultArtifactVersion serverVersion = new DefaultArtifactVersion(serverJECCVersion);
                if (thisVersion.compareTo(serverVersion) < 0) {
                    //notificationPane.setStyle("-fx-focus-color: transparent;");
                    //notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

                    ImageView image = new ImageView(new Image(FXLogin.class.getResourceAsStream("/icons/update.png")));
                    image.fitHeightProperty().set(32);
                    image.fitWidthProperty().set(32);


                    Action openWebAction = new Action(new Consumer<ActionEvent>() {
                        @Override
                        public void accept(ActionEvent actionEvent) {
                            try {
                                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                    Desktop.getDesktop().browse(new URI(getHost()));
                                } else {
                                    new ProcessBuilder("x-www-browser", getHost()).start();
                                }
                            } catch (Exception ex) {
                                logger.error(ex);
                            }
                        }
                    });
                    openWebAction.setText(I18n.getInstance().getString("fxlogin.update.button"));

                    Platform.runLater(() -> {
                        notificationPane.show(
                                String.format(I18n.getInstance().getString("fxlogin.update.message"), serverJECCVersion)
                                , image, openWebAction);
                    });

                    /**
                     Notifications.create()
                     .title("JEVis Control Center Update")
                     .text("New version " + serverJECCVersion + " is available")
                     .hideAfter(Duration.INDEFINITE)
                     .showInformation();
                     **/
                } else {
                    logger.debug("We are up to date");
                }
            } else {
                logger.error("Could not fetch JEVisCC Server Version");
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private String getHost() {
        for (JEVisOption opt : configuration) {
            if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                try {
                    URI uri = new URI(opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue());
                    return uri.getScheme() + "://" + uri.getHost();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                //return  opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue().split(":8000");
            }
        }
        return "http://my-jevis.com";
    }

    /**
     * This version has no function to add new servers. this may change in te
     * future again.
     *
     * @return
     * @deprecated
     */
    @Deprecated
    private Node buildServerSelection() {
        VBox root = new VBox(10);
        Label titel = new Label("Server Configuration");
        setDefaultStyle(titel, "-fx-font-weight: bold;");

        Label nameLabel = new Label("Name:");
        JFXTextField nameF = new JFXTextField();
        Label urlLabel = new Label("Server:");
        JFXTextField urlF = new JFXTextField();
        Label portLabel = new Label("Port:");
        JFXTextField portF = new JFXTextField();
        Label schema = new Label("Schema:");
        JFXTextField schemaF = new JFXTextField();
        Label userL = new Label("Username:");
        JFXTextField userF = new JFXTextField();
        Label passL = new Label("Password:");
        JFXTextField passF = new JFXTextField();

        JFXButton ok = new JFXButton("Save");
        ok.setDefaultButton(true);
        JFXButton addNewButton = new JFXButton("Save as new");
        ok.setDefaultButton(true);
        JFXButton cancel = new JFXButton("Cancel");
        cancel.setCancelButton(true);
        Region spacer = new Region();

        HBox buttons = new HBox(8);
        buttons.getChildren().setAll(spacer, ok, addNewButton, cancel);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        int columns = 2;
        //x,x...
        int row = 0;
        grid.add(nameLabel, 0, row);
        grid.add(nameF, 1, row);
        row++;
        grid.add(urlLabel, 0, row);
        grid.add(urlF, 1, row);
        row++;
        grid.add(portLabel, 0, row);
        grid.add(portF, 1, row);
        row++;
        grid.add(schema, 0, row);
        grid.add(schemaF, 1, row);
        row++;
        grid.add(userL, 0, row);
        grid.add(userF, 1, row);
        row++;
        grid.add(passL, 0, row);
        grid.add(passF, 1, row);

        VBox.setMargin(titel, new Insets(10, 30, 10, 30));
        Region bottomSpacer = new Region();
        bottomSpacer.setPrefHeight(10);

        root.getChildren().setAll(titel, grid, buttons, bottomSpacer);
        root.setPadding(new Insets(10));

//        JFXButton configureServer = new JFXButton("", JEConfig.getImage("Service Manager.png", 16, 16));
        JFXButton configureServer = new JFXButton("", ResourceLoader.getImage("Service Manager.png", 16, 16));

        PopOver serverConfigPop = new PopOver(root);
        serverConfigPop.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        serverConfigPop.setDetachable(true);
        serverConfigPop.setHideOnEscape(true);
        serverConfigPop.setAutoFix(true);

//        serverSelection.setItems(serverConfigurations);
//        serverSelection.getSelectionModel().selectFirst();
        HBox serverConfBox = new HBox(10);
//        serverConfBox.getChildren().setAll(serverSelection, configureServer);

        HBox.setHgrow(configureServer, Priority.NEVER);
//        HBox.setHgrow(serverSelection, Priority.ALWAYS);

        ok.setOnAction(event -> serverConfigPop.hide(Duration.seconds(0.3)));
        cancel.setOnAction(event -> serverConfigPop.hide(Duration.seconds(1)));

        addNewButton.setOnAction(event -> {

        });

        return serverConfBox;
    }

    /**
     * Load usersettings from Prederence.
     *
     * @param showServer
     */
    private void loadPreference(boolean showServer) {
//        logger.info("load from disk");
        if (!this.jevisPref.get("JEVisUser", "").isEmpty()) {
            this.storeConfig.setSelected(true);
//            logger.info("username: " + jevisPref.get("JEVisUser", ""));
            this.userName.setText(this.jevisPref.get("JEVisUser", ""));
            this.userPassword.setText(this.jevisPref.get("JEVisPW", ""));
        } else {
            this.storeConfig.setSelected(false);
        }
    }

    public Locale getSelectedLocale() {
        return this.selectedLocale;
    }

    /**
     * Build an remote link for the userregistration
     *
     * @return
     */
    private Node buildLink() {
        Hyperlink link = new Hyperlink();
        link.setId("fxlogin-form-register");
        link.setText("Register");

        final String url;

        if (this.fxoptions != null) {
            if (this.fxoptions.hasOption(CommonOptions.FXLogin.URL_REGISTER.getKey())) {
                JEVisOption opt = this.fxoptions.getOption(CommonOptions.FXLogin.URL_REGISTER.getKey());
                if (opt.getValue().equals("off")) {
                    link.setText("");
                    link.setVisible(false);
                    url = "";
                } else {
                    url = opt.getValue();
                }
            } else {
                url = "http://openjevis.org/account/register";
            }
        } else {
            link.setVisible(false);
            url = "";

        }

        link.setOnAction(event -> new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e1) {
                logger.fatal(e1);
            }
        }).start());

        return link;
    }

    /**
     * Build an infoBox where some information about the application are
     * displayed
     *
     * @return
     */
    public Node buildBuildInfo() {
        VBox vbox = new VBox();
        setDefaultStyle(vbox, "-fx-background-color: transparent;");
        logger.info(this.app.toString());
        Label copyLeft = new Label(this.app.getName() + " " + this.app.getVersion());//©Envidatec GmbH 2014-2016");
        Label java = new Label("Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        Label javafxlabel = new Label("JavaFX: " + System.getProperties().get("javafx.runtime.version"));


        copyLeft.setTextFill(javafx.scene.paint.Color.WHITE);
        java.setTextFill(javafx.scene.paint.Color.WHITE);
        javafxlabel.setTextFill(javafx.scene.paint.Color.WHITE);

        vbox.getChildren().setAll(copyLeft, java, javafxlabel);
        return vbox;
    }

    /**
     * The an BooleanProperty which is true if the user logged in successfully
     *
     * @return
     */
    public SimpleBooleanProperty getLoginStatus() {
        return this.loginStatus;
    }

    /**
     * Store the current setting like username and password in the java
     * preference context. Warning the password is in plaintext i guess, maye be
     * find a better solution but the JEVisDataSource needs an plaintext pw.
     */
    private void storePreference() {
        final String jevisUser = this.userName.getText();
        final String jevisPW = this.userPassword.getText();

        this.jevisPref.put("JEVisUser", jevisUser);
        this.jevisPref.put("JEVisPW", jevisPW);
        try {
            this.jevisPref.sync();
        } catch (BackingStoreException ex) {
            logger.fatal(ex);
        }

    }

    /**
     * Temporary solution the get the clear text password. The JEConfig will use this
     * to login into the ISO 50001 webservice. Will be reloaded if an better way
     * is found. Maybe the JEVisDataSource will give acces to it.
     *
     * @return
     */
    public String getUserPassword() {
        return this.userPassword.getText();
    }

    public interface Color {

        String MID_BLUE = "#005782";
        String MID_GREY = "#666666";
        String LIGHT_BLUE = "#1a719c";
        String LIGHT_BLUE2 = "#0E8CCC";
        String LIGHT_GREY = "#efefef";
        String LIGHT_GREY2 = "#f4f4f4";
    }
}
