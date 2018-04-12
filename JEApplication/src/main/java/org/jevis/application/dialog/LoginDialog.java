/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.dialog;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static java.util.Locale.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.sql.JEVisDataSourceSQL;
import org.jevis.application.resource.ResourceLoader;

/**
 * Simple Login Dialog. This will be replaced if the real one is working
 *
 * @author fs
 */
public class LoginDialog {

    //https://www.iconfinder.com/icons/54330/login_icon#size=32
    //public static String ICON_LOGIN = "1401219513_login.png";
    public static String ICON_LOGIN = "1401219513_login.png";
    private JEVisDataSourceSQL ds;
    private final Button ok = new Button("Login");
    private final Button cancel = new Button("Cancel");
    private final PasswordField passwordF = new PasswordField();
    private final TextField loginF = new TextField("");
    private final ComboBox serverSQLBox = new ComboBox();
    private final HBox waitPane = new HBox();
    private Label loginButtonL = new Label("Login");//dirty space for GUI...
    private final Stage stage = new Stage();
    private final CheckBox storeConfig = new CheckBox("Remember me");
    private Preferences pref = Preferences.userRoot().node("JEVis");
    private final String URL_SYNTAX = "user:password@server:port/jevis";
//    private Thread load = new Thread();

    public static enum Response {

        NO, YES, CANCEL
    };

    final ProgressBar progress = new ProgressBar(0);

    public JEVisDataSource showSQL(Stage owner) {
        return showSQL(owner, "/icons/openjevislogo_simple2.png");
    }

    public JEVisDataSource showSQL(Stage owner, String banner) {
        return showSQL(owner, banner, false, true, "");
    }

    public JEVisDataSource showSQL(Stage owner, String banner, final boolean ssl, final boolean showServer, String defaultServer) {
//        System.out.println("defaultServer: " + defaultServer);

        //JAVA FX for JAVA 1.7  worka round. Javafx will exit the thred after closing the first stage and cannot be reopend
        //see http://stackoverflow.com/questions/25193198/prevent-javafx-thread-from-dying-with-jfxpanel-swing-interop
        //TODo: can be removed with java 1.8
//        Platform.setImplicitExit(false);
        stage.setTitle("JEVis Login");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        waitPane.setAlignment(Pos.CENTER);
        waitPane.getChildren().setAll(loginButtonL);
        progress.setMaxWidth(65);
        progress.setStyle("-fx-box-border: transparent;");
        ok.setText("");
        ok.setGraphic(waitPane);
        ok.setMaxWidth(70);

        final VBox root = new VBox();
//        root.setStyle("-fx-background-color: #E2E2E2");//#F4F4F4
//        root.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(410);
        stage.setHeight(345);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        HBox imageBox = new HBox();
//         ImageView logo = new ImageView(new Image("/icons/logo_coffee_klein_login.png"));
        ImageView logo = new ImageView(new Image(banner));
        logo.setPreserveRatio(true);
        logo.setFitWidth(stage.getWidth());
        logo.setFitWidth(300);
//        logo.setFitHeight(60);

        imageBox.getChildren().setAll(logo);
        imageBox.setAlignment(Pos.CENTER);

        ImageView imageView = ResourceLoader.getImage(ICON_LOGIN, 65, 65);

        stage.getIcons().add(imageView.getImage());

        HBox buttonPanel = new HBox();

        ok.setDefaultButton(true);
        cancel.setCancelButton(true);

        Region spacer = new Region();

        Hyperlink link = new Hyperlink();
        link.setText("Register");
        link.setVisited(true);
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    URI uri = new URI("http://openjevis.org/account/register");
                    if (Desktop.isDesktopSupported()) {
                        System.out.println("Desktop is supportet");
                        Desktop.getDesktop().browse(uri);
                    } else {
                        //TODO: maybe disable send button if not suppotret at all
                        System.out.println("Desktop is not Supportet");
                        if (System.getProperty("os.name").equals("Linux")) {
                            System.out.println("is limux using xdg-open");
                            Runtime.getRuntime().exec("xdg-open " + uri);
                        }
                    }
                } catch (URISyntaxException ex) {
                    System.out.println("ex: " + ex);
                    Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ex: " + ex);
                }
            }
        });

        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonPanel.getChildren().addAll(link, spacer, ok, cancel);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);

        GridPane content = new GridPane();
//        content.setStyle("-fx-background-color: linear-gradient(#ffffff,#1a719c);");
        content.setHgap(10);
        content.setVgap(5);
        content.setPadding(new Insets(10, 10, 10, 10));

        Label loginL = new Label("Login:");
        loginF.setPromptText("Username");
        loginL.setMinWidth(80);
        Label passwordL = new Label("Password:");
        Label serverSQLL = new Label("Server:");

        if (defaultServer == null || defaultServer.isEmpty()) {
            defaultServer = URL_SYNTAX;
        }
        serverSQLBox.getItems().addAll(
                defaultServer
        );

        serverSQLBox.setEditable(true);
        serverSQLBox.setMaxWidth(300);
        serverSQLBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                serverSQLBox.getItems().add(t1);
                serverSQLBox.getSelectionModel().select(t1);
            }
        });
        serverSQLBox.getSelectionModel().selectFirst();

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        //TODO read the allowd Languages from strt paramete or ask the JEVis Translation service
        List<Locale> availableLang = new ArrayList<>();
        availableLang.add(UK);
//        availableLang.add(GERMAN);

        Label lLang = new Label("Language: ");
        ComboBox langSelect = buildLanguageBox(availableLang);

        //spalte , zeile bei 0 starten
        int x = 0;

        content.add(loginL, 0, x);
        content.add(loginF, 1, x);
        content.add(passwordL, 0, ++x);
        content.add(passwordF, 1, x);
        content.add(lLang, 0, ++x);
        content.add(langSelect, 1, x);

        if (showServer) {
            content.add(serverSQLL, 0, ++x);
            content.add(serverSQLBox, 1, x);
        } else {
            content.add(new Label(), 0, ++x);
        }

        content.add(storeConfig, 1, ++x);
//        content.add(buttonPanel, 0, ++x, 2, 1);

        GridPane.setHalignment(storeConfig, HPos.LEFT);
        GridPane.setHgrow(loginF, Priority.ALWAYS);
        GridPane.setHgrow(passwordF, Priority.ALWAYS);
        GridPane.setHgrow(serverSQLBox, Priority.ALWAYS);
        GridPane.setHgrow(langSelect, Priority.ALWAYS);

        //new Separator(Orientation.HORIZONTAL),
        root.getChildren().addAll(imageBox, content, buttonPanel);
        VBox.setVgrow(content, Priority.NEVER);
        VBox.setVgrow(buttonPanel, Priority.NEVER);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                login(serverSQLBox.getSelectionModel().getSelectedItem().toString(), ssl);
            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                if(load.isAlive()){
//                    load.interrupt();
//                }
                stage.close();
            }
        });

        loadPreference(showServer, defaultServer);
        stage.showAndWait();

        return ds;
    }

    private void loadPreference(boolean showServer, String defaultServer) {

        if (!pref.get("JEVisUser", "").isEmpty()) {
            storeConfig.setSelected(true);
            loginF.setText(pref.get("JEVisUser", ""));
            passwordF.setText(pref.get("JEVisPW", ""));

            if (showServer) {
                serverSQLBox.getItems().add(pref.get("LastServer", ""));
                serverSQLBox.getSelectionModel().selectLast();
            }

            if (!defaultServer.equals(URL_SYNTAX)) {
                serverSQLBox.getSelectionModel().select(defaultServer);
            }

        } else {
            storeConfig.setSelected(false);
        }
    }

    private ComboBox buildLanguageBox(List<Locale> locales) {
        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = new Callback<ListView<Locale>, ListCell<Locale>>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> param) {
                final ListCell<Locale> cell = new ListCell<Locale>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(Locale item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {

                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);

//                            if (item.getLanguage().equals(US.getLanguage())) {
//
//                            } else if (item.getLanguage().equals(GERMAN.getLanguage())) {
//
//                            }
                            Image img = new Image("/icons/" + item.getLanguage() + ".png");
                            ImageView iv = new ImageView(img);
                            iv.fitHeightProperty().setValue(20);
                            iv.fitWidthProperty().setValue(20);
                            iv.setSmooth(true);

                            Label name = new Label(item.getDisplayLanguage());
                            name.setTextFill(Color.BLACK);

                            box.getChildren().setAll(iv, name);
                            setGraphic(box);

                        }
                    }
                };
                return cell;
            }
        };

        ObservableList<Locale> options = FXCollections.observableArrayList(locales);

        final ComboBox<Locale> comboBox = new ComboBox<Locale>(options);
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));

        //TODO: load default lange from Configfile or so
        comboBox.getSelectionModel().select(UK);//Default

        comboBox.setMinWidth(250);
        comboBox.setMaxWidth(Integer.MAX_VALUE);//workaround

        return comboBox;

    }

    private void storePreference() {
        final String jevisUser = loginF.getText();
        final String jevisPW = passwordF.getText();

        final String server1 = serverSQLBox.getSelectionModel().getSelectedItem().toString();

        pref.put("LastServer", server1);
        pref.put("JEVisUser", jevisUser);
        pref.put("JEVisPW", jevisPW);

    }

    private void login(final String serverURL, final boolean ssl) {
//        ok.setDisable(true);

//        waitPane.getChildren().setAll(loginButtonL, progress);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                passwordF.setDisable(true);
                serverSQLBox.setDisable(true);
                loginF.setDisable(true);
                progress.setVisible(true);
//                waitPane.getChildren().setAll(loginButtonL, progress);
                ok.setGraphic(progress);
//                ok.setPrefWidth(90);
                progress.setProgress(-1);
            }
        });

        final Thread load = new Thread() {

            @Override
            public void run() {
                //super simple server settings parser
                //TODO implement URi parser for JEVisDataSourceConnection
                //example "jevis:jevis@alpha.openjevis.org:3306/jevis"
//                StringTokenizer tokenizer = new StringTokenizer(serverSQLBox.getSelectionModel().getSelectedItem().toString(), ":/@");
                System.out.println("ServerURL: " + serverURL);
                StringTokenizer tokenizer = new StringTokenizer(serverURL, ":/@");

                //Dump way to parse the string
                List<String> para = new ArrayList<String>();
                while (tokenizer.hasMoreTokens()) {
                    para.add(tokenizer.nextToken());
                }

                final String server = para.get(2);
                final String port = para.get(3);
                final String schema = para.get(4);
                final String dbuser = para.get(0);
                final String dbpw = para.get(1);
                final String jevisUser = loginF.getText();
                final String jevisPW = passwordF.getText();
//                System.out.println("Server: '" + server + "' Port: '" + port
//                        + "' Schema: '" + schema + "' dbuser: '" + dbuser
//                        + "' dbpw: '" + dbpw + "' jevisuser: '"
//                        + jevisUser + "' jevispw: '" + jevisPW + "'");

                if (para.size() >= 5) {
                    try {

                        ds = new JEVisDataSourceSQL(server, port, schema, dbuser, dbpw);
                        ds.enableSSL(ssl);

                        if (ds.connect(jevisUser, jevisPW)) {
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    if (storeConfig.isSelected()) {
                                        storePreference();
                                    }
                                    stage.close();
                                }
                            });

                        } else {
                            waitEnd();
                        }

                    } catch (JEVisException ex) {
                        //TODO: implement an error handling to let the user know what happend
                        Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);

                        waitEnd();
                    }
                } else {
                    waitEnd();
                }
            }

            @Override
            public void interrupt() {
                ds = null;
                waitEnd();
            }

        };
        load.start();

        //Timeout handler
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
                load.interrupt();

            }
        }.start();

    }

    private void waitEnd() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                progress.setVisible(false);
                passwordF.setDisable(false);
                serverSQLBox.setDisable(false);
                loginF.setDisable(false);
                ok.setGraphic(waitPane);
            }
        });
    }

    public static ImageView convertToImageView(BufferedImage icon, double w, double h) throws JEVisException {
        if (icon == null) {
            return new ImageView();
        }

        Image image = SwingFXUtils.toFXImage(icon, null);
        ImageView iv = new ImageView(image);
        iv.fitHeightProperty().setValue(h);
        iv.fitWidthProperty().setValue(w);
        iv.setSmooth(true);
        return iv;
    }

}
