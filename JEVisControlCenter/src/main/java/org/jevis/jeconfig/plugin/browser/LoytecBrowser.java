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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.I18n;
import org.w3c.dom.Document;

/**
 *
 * @author fs
 */
public class LoytecBrowser implements Plugin {

    private StringProperty nameProperty = new SimpleStringProperty(I18n.getInstance().getString("plugin.loytec.title"));
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane contentPane = new BorderPane();
    private WebEngine webEngine;
    private ImageView icon = new ImageView();

    public LoytecBrowser(JEVisDataSource ds) {
        this.ds = ds;
        icon = JEConfig.getImage("if_50_2315874.png", 20, 20);
        try {
//            final String username = ds.getCurrentUser().getAccountName();
//            final String password = JEConfig.userpassword;
            final String username = "Sys Admin";
            final String password = "nordhorn2.0";

            final BooleanProperty logedIn = new SimpleBooleanProperty(false);
            final WebView page = new WebView();

            contentPane.setCenter(page);
            webEngine = page.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            webEngine.confirmHandlerProperty().addListener(new ChangeListener<Callback<String, Boolean>>() {
                @Override
                public void changed(ObservableValue<? extends Callback<String, Boolean>> observable, Callback<String, Boolean> oldValue, Callback<String, Boolean> newValue) {
                    System.out.println("confirmHandlerProperty: " + newValue);
                }
            });
            webEngine.onErrorProperty().addListener(new ChangeListener<EventHandler<WebErrorEvent>>() {
                @Override
                public void changed(ObservableValue<? extends EventHandler<WebErrorEvent>> observable, EventHandler<WebErrorEvent> oldValue, EventHandler<WebErrorEvent> newValue) {
                    System.out.println("onErrorProperty: " + newValue);
                }
            });
            webEngine.setOnAlert(event -> showAlert(event.getData()));
            webEngine.setConfirmHandler(message -> showConfirm(message));
            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document doc) {
                    if (doc != null && !logedIn.getValue()) {
                        System.out.println("do login");
//                        HTMLFormElement form = (HTMLFormElement) doc.getElementById("form-login");
//
//                        NodeList nodes = form.getElementsByTagName("input");
//                        System.out.println("form: " + form);
//                        for (int i = 0; i < form.getElements().getLength(); i++) {
//                            try {
//                                org.w3c.dom.Node node = nodes.item(i);
//                                if (node != null) {
//                                    System.out.println("Node.name: " + "  - " + node);
//                                    HTMLInputElement input = (HTMLInputElement) node;
//                                    if (input.getId() != null) {
//                                        switch (input.getId()) {
//                                            case "input-username":
//                                                input.setValue(username);
//                                                break;
//                                            case "input-password":
//                                                input.setValue(password);
//                                                break;
//                                        }
//
//                                    }
//                                }
//
//                            } catch (NullPointerException nex) {
//                                nex.printStackTrace();
//                            }
//                        }
//
//                        webEngine.executeScript("setLanguage(\"german\")");
//                        NodeList nodesSelect = form.getElementsByTagName("select");
//                        try {
//                            org.w3c.dom.Node node = nodesSelect.item(0);
//                            HTMLSelectElement select = (HTMLSelectElement) node;
//                            select.setSelectedIndex(1);
//
//                        } catch (NullPointerException nex) {
//                            nex.printStackTrace();
//                        }
//
//                        logedIn.setValue(Boolean.TRUE);
////                        form.setTarget(null);
////                        form.submit();
                        Object ob = webEngine.executeScript("retryReload()");
                        System.out.println();
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
        return "Loytec Browser";
    }

    private void showAlert(String message) {
        System.out.println("http error: " +message);
    }

    private boolean showConfirm(String message) {
         System.out.println("http error: " +message);
         return true;
    }

    @Override
    public void setHasFocus() {
        webEngine.load("http://www.loytec.com/lweb802/?project=visu_project.lweb2&address=10.1.2.46&port=80#lvisPage");
    }

    @Override
    public String getName() {
        return nameProperty.getValue();
    }

    @Override
    public void setName(String name) {
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
    public void setUUID(String id) {
        this.id.setValue(id);
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
    public boolean supportsRequest(int cmdType) {
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
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public void handelRequest(int cmdType) {

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
