/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.browser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.I18n;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.net.URI;

/**
 * @author fs
 */
public class ISO50001Browser implements Plugin {
    private static final Logger logger = LogManager.getLogger(ISO50001Browser.class);
    private StringProperty nameProperty = new SimpleStringProperty(I18n.getInstance().getString("plugin.isobrowser"));
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane contentPane = new BorderPane();
    private WebEngine webEngine;
    private ImageView icon = new ImageView();
    private StringProperty urlProperty = new SimpleStringProperty();
    private String tooltip = I18n.getInstance().getString("pluginmanager.iso50001.tooltip");

    public ISO50001Browser(JEVisDataSource ds) {
        this.ds = ds;
        icon = JEConfig.getImage("if_50_2315874.png", 20, 20);
        try {
            final String username = ds.getCurrentUser().getAccountName();
            final String password = JEConfig.userpassword;

            final BooleanProperty logedIn = new SimpleBooleanProperty(false);
            final WebView page = new WebView();

            try {
                for (JEVisOption opt : ds.getConfiguration()) {
                    if (opt.getKey().equals(CommonOptions.DataSource.DataSource.getKey())) {
                        URI jewebseriveURI = new URI(opt.getOption(CommonOptions.DataSource.HOST.getKey()).getValue());

                        urlProperty.setValue(
                                jewebseriveURI.getScheme() + "://"
                                        + jewebseriveURI.getHost() + ":" + jewebseriveURI.getPort()
                                        + "/JEWebService/v1/login"
                        );
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            contentPane.setCenter(page);
            webEngine = page.getEngine();
            webEngine.setJavaScriptEnabled(true);

            //Hide the login window for now
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    if (newValue == Worker.State.SUCCEEDED) {
                        Document doc = webEngine.getDocument();
                        Element styleNode = doc.createElement("style");
                        Text styleContent = doc.createTextNode(".center-login{visibility:hidden;}");
                        styleNode.appendChild(styleContent);
                        doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);

                    }
                }
            });

            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                                                         @Override
                                                         public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document doc) {
                                                             if (doc != null && !logedIn.getValue()) {

                                                                 HTMLFormElement form = (HTMLFormElement) doc.getElementById("form-login");

                                                                 NodeList nodes = form.getElementsByTagName("input");
                                                                 logger.info("form: " + form);
                                                                 for (int i = 0; i < form.getElements().getLength(); i++) {
                                                                     try {
                                                                         org.w3c.dom.Node node = nodes.item(i);
                                                                         if (node != null) {
                                                                             logger.info("Node.name: " + "  - " + node);
                                                                             HTMLInputElement input = (HTMLInputElement) node;
                                                                             if (input.getId() != null) {
                                                                                 switch (input.getId()) {
                                                                                     case "input-username":
                                                                                         input.setValue(username);
                                                                                         break;
                                                                                     case "input-password":
                                                                                         input.setValue(password);
                                                                                         break;
                                                                                 }

                                                                             }
                                                                         }

                                                                     } catch (NullPointerException nex) {
                                                                         nex.printStackTrace();
                                                                     }
                                                                 }

                                                                 webEngine.executeScript("setLanguage(\"german\")");
                                                                 NodeList nodesSelect = form.getElementsByTagName("select");
                                                                 try {
                                                                     org.w3c.dom.Node node = nodesSelect.item(0);
                                                                     HTMLSelectElement select = (HTMLSelectElement) node;
                                                                     select.setSelectedIndex(1);

                                                                 } catch (NullPointerException nex) {
                                                                     nex.printStackTrace();
                                                                 }

                                                                 logedIn.setValue(Boolean.TRUE);
                                                                 webEngine.executeScript("login()");
                                                             }

                                                         }
                                                     }
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public String getClassName() {
        return "ISO5001 Browser Plugin";
    }

    @Override
    public void setHasFocus() {
//        webEngine.load("http://10.1.1.55:6735/JEWebService/v1/login");
        webEngine.load(urlProperty.getValue());
    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name
    ) {
        nameProperty.setValue(name);
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public String getUUID() {
        return id.getValue();
    }

    @Override
    public void setUUID(String id
    ) {
        this.id.setValue(id);
    }

    @Override
    public String getToolTip() {
        return tooltip;
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getMenu() {
        return new Region();
    }

    @Override
    public boolean supportsRequest(int cmdType
    ) {
        return false;
    }

    @Override
    public Node getToolbar() {
        return new Region();
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds
    ) {
        this.ds = ds;
    }

    @Override
    public void handleRequest(int cmdType
    ) {

    }

    @Override
    public Node getContentNode() {

        return contentPane;
    }

    @Override
    public ImageView getIcon() {
        return icon;
    }

    @Override
    public void fireCloseEvent() {

    }

}
