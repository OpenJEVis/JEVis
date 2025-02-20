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
package org.jevis.jecc.application.login;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.NotificationPane;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.commons.application.ApplicationInfo;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.datasource.DataSourceLoader;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.ParameterHelper;
import org.jevis.jecc.tool.Layouts;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    public static String checkMarkSymbol = "\uD83D\uDDF8";
    private final Stage mainStage;
    private final Button loginButton = new Button("Login");
    private final Button closeButton = new Button("Close");
    private final TextField userName = new TextField();
    private final CheckBox storeConfig = new CheckBox("Remember me");
    private final PasswordField userPassword = new PasswordField();
    private final VBox authGrid = new VBox();
    private final Preferences jevisPref = Preferences.userRoot().node("JEVis");
    private final TextArea messageBox = new TextArea("");
    //    private final Preferences serverPref = Preferences.userRoot().node("JEVis.Server");
    private final List<JEVisObject> rootObjects = new ArrayList<>();
    private final List<JEVisClass> classes = new ArrayList<>();
    //    private final ComboBox <JEVisConfiguration> serverSelection = new ComboBox <>();
    private final String css = "";
    private final int lastServer = -1;
    private final Stage statusDialog = new Stage(StageStyle.TRANSPARENT);

    //workaround, need some coll OO implementation
    private final List<PreloadTask> tasks = new ArrayList<>();

    private final SimpleBooleanProperty loginStatus = new SimpleBooleanProperty(false);
    //    private final String URL_SYNTAX = "user:password@server:port/jevis";
    private final ProgressIndicator progress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
    //Workaround replace later, at the moment I have problems showing the exception in a thread as an Alert
    private final Exception lastException = null;
    private final NotificationPane notificationPane = new NotificationPane();
    private final StringBuilder messageText = new StringBuilder();
    private JEVisDataSource _ds;
    //    private final ObservableList<JEVisConfiguration> serverConfigurations = FXCollections.observableList(new ArrayList<>());
    private VBox mainHBox = new VBox();
    private Application.Parameters parameters;
    private List<JEVisOption> configuration;
    private JEVisOption fxOptions;
    private boolean useCSSFile = false;
    private ApplicationInfo app = new ApplicationInfo("FXLogin", "");
    private Locale selectedLocale = Locale.getDefault();
    private ComboBox<Locale> langSelect;
    private boolean hasCredentials = false;

    private FXLogin() {
        this.mainStage = null;
    }

    public FXLogin(Stage stage, Application.Parameters parameters, ApplicationInfo app) {
        super();
        this.mainStage = stage;
        this.app = app;

        this.messageBox.getStylesheets().add(FXLogin.class.getResource("/styles/LoginMessageBox.css").toExternalForm());
//
//        this.messageBox.setId("LoginMessageBox");
        this.messageBox.setBorder(null);
        this.messageBox.setPadding(new Insets(0));
        this.messageBox.setEditable(false);
        this.messageBox.setMouseTransparent(true);
        this.messageBox.setFocusTraversable(false);


        loginButton.setStyle("-fx-background-color: #4dadf7");
        loginButton.setTextFill(javafx.scene.paint.Paint.valueOf("#FFFFFF"));
        closeButton.setStyle("-fx-background-color: #adb5bd");
        closeButton.setTextFill(javafx.scene.paint.Paint.valueOf("#FFFFFF"));


        this.configuration = parseConfig(parameters);
        for (JEVisOption opt : this.configuration) {
            if (opt.equals(CommonOptions.FXLogin.FXLogin)) {
                this.fxOptions = opt;
            }
        }

        setStyleSheet();

        try {
            this._ds = loadDataSource(this.configuration);


        } catch (ClassNotFoundException ex) {
            logger.fatal(ex);
        } catch (InstantiationException ex) {
            logger.fatal(ex);
        } catch (IllegalAccessException ex) {
            logger.fatal(ex);
        }

        if (hasLoginCredentials(this.configuration)) {
            initSlim();

            hasCredentials = true;
        } else {
            init();

            Platform.runLater(() -> {
                this.userName.setEditable(true);
                this.mainStage.setMaximized(false);
                this.mainStage.setMaximized(true);
            });
        }
    }

    /**
     * Create a new Login panel
     *
     * @param stage      The Stage will be used of eventual dialogs
     * @param parameters
     */
    public FXLogin(Stage stage, Application.Parameters parameters) {
        this(stage, parameters, new ApplicationInfo("FXLogin", ""));
    }

    private boolean hasLoginCredentials(List<JEVisOption> configuration) {
        String userName = null;
        String password = null;
        String locale = null;
        for (JEVisOption opt : configuration) {
            logger.trace("Option: {} {}", opt.getKey(), opt.getValue());
            if (opt.getKey().equals(CommonOptions.DataSource.USERNAME.getKey())) {
                userName = opt.getValue();
            } else if (opt.getKey().equals(CommonOptions.DataSource.PASSWORD.getKey())) {
                password = opt.getValue();
            } else if (opt.getKey().equals(CommonOptions.DataSource.LOCALE.getKey())) {
                locale = opt.getValue();
            }
        }
        if (userName != null && password != null && locale != null) {
            this.userName.setText(userName);
            this.userPassword.setText(password);
            this.selectedLocale = Locale.forLanguageTag(locale);
            logger.debug("locale set to {} from {}", selectedLocale, locale);
            return true;
        } else if (userName != null && password != null) {
            this.userName.setText(userName);
            this.userPassword.setText(password);
            return true;
        } else return false;
    }

    /**
     * This function will try to connect to the JEVisDataSource with the
     * configures server settings.
     */
    public void doLogin() {

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
     * Load the DataSource from a configuration object.
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
            if (this.fxOptions != null) {
                if (this.fxOptions.hasOption(CommonOptions.FXLogin.URL_CSS.getKey())) {
                    JEVisOption opt = this.fxOptions.getOption(CommonOptions.FXLogin.URL_CSS.getKey());
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
     * Build a language selection box
     *
     * @return
     */
    private ComboBox<Locale> buildLanguageBox() {

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

                                Image img = new Image("/icons/flags2/" + item.getLanguage() + ".png");
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

        final ComboBox<Locale> comboBox = new ComboBox<>(options);
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
     * returns the JEVisDataSource after a successfull login
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
    private AnchorPane buildHeader() {
        AnchorPane header = new AnchorPane();
//        header.setId("fxlogin-header");
        setDefaultStyle(header, "-fx-background-color: " + Color.MID_GREY);

        ImageView logo = null;

        String defaultLogo = "/icons/openjevis_longlogo.png";

        if (this.fxOptions != null) {
            if (this.fxOptions.hasOption(CommonOptions.FXLogin.URL_LOGO.getKey())) {
                JEVisOption opt = this.fxOptions.getOption(CommonOptions.FXLogin.URL_LOGO.getKey());
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
            logo.fitWidthProperty().bind(this.mainStage.widthProperty());
        }

        header.getChildren().add(logo);
        AnchorPane.setBottomAnchor(logo, 0.0);
        AnchorPane.setLeftAnchor(logo, 0.0);

//        logo.setId("fxlogin-header-logo");

        return header;
    }

    /**
     * Build a footer GUI element
     *
     * @return
     */
    private AnchorPane buildFooter() {
        AnchorPane footer = new AnchorPane();
//        footer.setId("fx-login-footer");
        setDefaultStyle(footer, "-fx-background-color: " + Color.MID_GREY);

        Node buildInfo = buildBuildInfo();
//        buildInfo.setId("fx-login-footer-info");
        AnchorPane.setBottomAnchor(buildInfo, 5.0);
        AnchorPane.setRightAnchor(buildInfo, 5.0);
        footer.getChildren().add(buildInfo);

        return footer;
    }

    /**
     * Build a bottom button bar element
     *
     * @return
     */
    private HBox buildButtonBar() {
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
     * Build an authentication (user, password, ...) GUI element
     *
     * @return
     */
    private VBox buildAuthForm() {

        HBox buttonBox = buildButtonBar();
//        Node serverConfigBox = buildServerSelection();
        Region serverConfigBox = new Region();
        this.langSelect = buildLanguageBox();

        this.loginButton.setDefaultButton(true);
        this.closeButton.setCancelButton(true);

        //this.loginButton.setId("fxlogin-form-login");
        //this.closeButton.setId("fxlogin-form-close");
        this.userName.setId("fxlogin-form-username");
        this.userName.setPromptText("Username:");

        this.userPassword.setId("fxlogin-form-password");
        this.userPassword.setPromptText("Password:");

        this.langSelect.setId("fxlogin-form-language");
        this.langSelect.setPromptText("Language:");

//        this.authGrid.setId("fxlogin-form");

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

        this.authGrid.setSpacing(5);
        this.authGrid.setPadding(new Insets(10, 10, 10, 10));

        this.authGrid.getChildren().add(this.userName);

        this.authGrid.getChildren().add(this.userPassword);

        this.authGrid.getChildren().add(langSelect);

        this.authGrid.getChildren().add(new Region());

        this.authGrid.getChildren().add(this.storeConfig);

        Region cTobSpacer = new Region();
        setDefaultStyle(cTobSpacer, "-fx-background-color: transparent;");
        cTobSpacer.setPrefHeight(30);
        this.authGrid.getChildren().add(cTobSpacer);

        this.authGrid.getChildren().add(buttonBox);

        //this.storeConfig.setId("fxlogin-form-rememberme");

        return this.authGrid;
    }

    private void initSlim() {

        //TODO load from URL/RESOURCE
        ImageView logo = new ImageView(new Image("/icons/openjevislogo_simple2.png"));
        logo.setPreserveRatio(true);

        AnchorPane leftSpacer = new AnchorPane();
        AnchorPane rightSpacer = new AnchorPane();

//        leftSpacer.setId("fxlogin-body-left");
//        rightSpacer.setId("fxlogin-body-right");

        leftSpacer.setMinWidth(200);//todo 20%

        HBox body = new HBox();
//        body.setId("fxlogin-body");
        body.getChildren().setAll(leftSpacer, messageBox, rightSpacer);
        HBox.setHgrow(messageBox, Priority.NEVER);
        HBox.setHgrow(leftSpacer, Priority.NEVER);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        AnchorPane header = buildHeader();
        AnchorPane footer = buildFooter();

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
     * Load usersettings from Prederence.
     *
     * @param showServer
     */
    private void loadPreference(boolean showServer) {
//        logger.info("load from disk");
        if (!this.jevisPref.get("JEVisUser", "").isEmpty() && !hasCredentials) {
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
//        link.setId("fxlogin-form-register");
        link.setText("Register");

        final String url;

        if (this.fxOptions != null) {
            if (this.fxOptions.hasOption(CommonOptions.FXLogin.URL_REGISTER.getKey())) {
                JEVisOption opt = this.fxOptions.getOption(CommonOptions.FXLogin.URL_REGISTER.getKey());
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
     * Initilize this class, this will build most of the GUI
     */
    private void init() {
        loadPreference(true);

        //TODO load from URL/RESOURCE
        ImageView logo = new ImageView(new Image("/icons/openjevislogo_simple2.png"));
        logo.setPreserveRatio(true);

        AnchorPane leftSpacer = new AnchorPane();
        AnchorPane rightSpacer = new AnchorPane();

//        leftSpacer.setId("fxlogin-body-left");
//        rightSpacer.setId("fxlogin-body-right");
//        this.progress.setId("fxlogin-body-progress");

        this.progress.setPrefSize(80, 80);
        this.progress.setVisible(false);
        AnchorPane.setTopAnchor(this.progress, 70d);
        AnchorPane.setLeftAnchor(this.progress, 100d);
        rightSpacer.getChildren().setAll(this.progress);

        leftSpacer.setMinWidth(200);//todo 20%

        VBox authForm = buildAuthForm();

        HBox body = new HBox();
//        body.setId("fxlogin-body");
        body.getChildren().setAll(leftSpacer, authForm, rightSpacer, messageBox);
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

    public void addLoginMessage(String message, boolean newLIne) {
        messageText.append(message);
        if (messageText.length() > 0 && newLIne) {
            messageText.append(System.getProperty("line.separator"));
        }

        Platform.runLater(() -> {
            messageBox.setText(messageText.toString());
            messageBox.setScrollTop(Double.MAX_VALUE);
        });
    }

    /**
     * The BooleanProperty which is true if the user logged in successfully
     *
     * @return
     */
    public SimpleBooleanProperty getLoginStatus() {
        return this.loginStatus;
    }

    /**
     * Store the current setting like username and password in the java
     * preference context. Warning the password is in plaintext I guess, maybe
     * find a better solution but the JEVisDataSource needs a plaintext pw.
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
     * to login into the ISO 50001 webservice. Will be reloaded if a better way
     * is found. Maybe the JEVisDataSource will give access to it.
     *
     * @return
     */
    public String getUserPassword() {
        return this.userPassword.getText();
    }

    public boolean hasCredentials() {
        return hasCredentials;
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
