/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.plugin.browser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.Plugin;
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
public class ISO50001Plugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(ISO50001Plugin.class);
    public static String PLUGIN_NAME = "ISO 50001 Plugin";
    protected final BorderPane borderPane = new BorderPane();
    protected final String title;
    private final StringProperty nameProperty = new SimpleStringProperty(I18n.getInstance().getString("plugin.isobrowser"));
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private final StringProperty urlProperty = new SimpleStringProperty();
    private final String tooltip = I18n.getInstance().getString("pluginmanager.iso50001.tooltip");
    private JEVisDataSource ds;
    private WebEngine webEngine;
    private Region icon = new Region();

    public ISO50001Plugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.icon = ControlCenter.getSVGImage(Icon.LOYTEC_BROWSER, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);


    }

    @Override
    public String getClassName() {
        return "ISO5001 Browser Plugin";
    }

    @Override
    public void setHasFocus() {
        try {
            final String username = ds.getCurrentUser().getAccountName();
            final String password = ControlCenter.userpassword;

            final BooleanProperty loggedIn = new SimpleBooleanProperty(false);
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

            borderPane.setCenter(page);
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

            webEngine.documentProperty().addListener((observable, oldValue, doc) -> {
                        if (doc != null && !loggedIn.getValue()) {

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

                            loggedIn.setValue(Boolean.TRUE);
                            webEngine.executeScript("login()");
                        }
                    }
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        webEngine.load(urlProperty.getValue());
    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 8;
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
    public void updateToolbar() {

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
        return borderPane;
    }

    @Override
    public Region getIcon() {
        return icon;
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void lostFocus() {

    }
}
