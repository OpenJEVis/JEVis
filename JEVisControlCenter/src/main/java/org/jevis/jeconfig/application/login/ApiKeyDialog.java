package org.jevis.jeconfig.application.login;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.ws.json.JsonApiKey;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sys Admin dialog: API keys of the service accounts (create, list, revoke) and the 2FA reset of a user.
 */
public class ApiKeyDialog {

    private static final Logger logger = LogManager.getLogger(ApiKeyDialog.class);

    private ApiKeyDialog() {
    }

    private static String t(String key) {
        return I18n.getInstance().getString(key);
    }

    public static void show(Window owner, JEVisDataSourceWS ds) {
        ObservableList<KeyRow> rows = FXCollections.observableArrayList();
        TableView<KeyRow> table = new TableView<>(rows);
        table.getColumns().add(column(t("app.login.apikeys.name"), "name", 200));
        table.getColumns().add(column("User", "userName", 150));
        table.getColumns().add(column("ID", "id", 140));
        table.getColumns().add(column(t("app.login.apikeys.created"), "created", 140));
        table.getColumns().add(column(t("app.login.apikeys.lastused"), "lastUsed", 140));
        table.getColumns().add(column("Status", "status", 80));
        table.setPrefHeight(300);

        Runnable reload = () -> {
            try {
                rows.clear();
                for (JsonApiKey key : ds.listApiKeys()) {
                    rows.add(new KeyRow(key));
                }
            } catch (JEVisException ex) {
                logger.error("Could not load API keys", ex);
                TwoFactorDialogs.showError(owner, ex.getMessage());
            }
        };

        Button create = new Button(t("app.login.apikeys.create"));
        create.setOnAction(event -> {
            createKey(owner, ds);
            reload.run();
        });
        Button revoke = new Button(t("app.login.apikeys.revoke"));
        revoke.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        revoke.setOnAction(event -> {
            KeyRow row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, t("app.login.apikeys.revoke") + ": " + row.getName() + "?", ButtonType.YES, ButtonType.NO);
            confirm.initOwner(owner);
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    ds.revokeApiKey(row.getId());
                } catch (JEVisException ex) {
                    TwoFactorDialogs.showError(owner, ex.getMessage());
                }
                reload.run();
            }
        });
        Button resetMfa = new Button(t("app.login.mfa.reset"));
        resetMfa.setOnAction(event -> resetMfa(owner, ds));

        HBox buttons = new HBox(10, create, revoke, new Separator(), resetMfa);
        VBox content = new VBox(10, table, buttons);
        content.setPadding(new Insets(10));

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(t("app.login.apikeys.title"));
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        reload.run();
        dialog.showAndWait();
    }

    private static void createKey(Window owner, JEVisDataSourceWS ds) {
        ComboBox<JEVisObject> user = new ComboBox<>(FXCollections.observableArrayList(loadUsers(ds)));
        user.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public JEVisObject fromString(String string) {
                return null;
            }
        });
        user.setUserData("User");
        TextField name = new TextField();
        name.setUserData(t("app.login.apikeys.name"));
        name.setPromptText("JEDataCollector@server");
        if (!TwoFactorDialogs.showFields(owner, t("app.login.apikeys.create"), null, user, name)) return;
        if (user.getValue() == null || name.getText().trim().isEmpty()) return;

        try {
            JsonApiKey key = ds.createApiKey(user.getValue().getID(), name.getText().trim(), null);
            TextArea area = new TextArea(key.getKey());
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefRowCount(3);
            ButtonType copy = new ButtonType(t("app.login.mfa.copy"), ButtonBar.ButtonData.LEFT);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", copy, ButtonType.OK);
            alert.initOwner(owner);
            alert.setTitle(t("app.login.apikeys.title"));
            alert.setHeaderText(t("app.login.apikeys.newkey"));
            alert.getDialogPane().setContent(area);
            alert.getDialogPane().lookupButton(copy).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(key.getKey());
                Clipboard.getSystemClipboard().setContent(content);
                event.consume();
            });
            alert.showAndWait();
        } catch (JEVisException ex) {
            logger.error("Could not create API key", ex);
            TwoFactorDialogs.showError(owner, ex.getMessage());
        }
    }

    private static void resetMfa(Window owner, JEVisDataSourceWS ds) {
        ComboBox<JEVisObject> user = new ComboBox<>(FXCollections.observableArrayList(loadUsers(ds)));
        user.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public JEVisObject fromString(String string) {
                return null;
            }
        });
        user.setUserData("User");
        if (!TwoFactorDialogs.showFields(owner, t("app.login.mfa.reset"), t("app.login.mfa.reset.confirm"), user))
            return;
        if (user.getValue() == null) return;
        try {
            if (!ds.resetMfa(user.getValue().getID())) {
                TwoFactorDialogs.showError(owner, t("app.login.mfa.notsupported"));
            }
        } catch (JEVisException ex) {
            TwoFactorDialogs.showError(owner, ex.getMessage());
        }
    }

    private static List<JEVisObject> loadUsers(JEVisDataSourceWS ds) {
        List<JEVisObject> users = new ArrayList<>();
        try {
            JEVisClass userClass = ds.getJEVisClass("User");
            users.addAll(ds.getObjects(userClass, false));
            users.sort(Comparator.comparing(JEVisObject::getName, String.CASE_INSENSITIVE_ORDER));
        } catch (JEVisException ex) {
            logger.error("Could not load users", ex);
        }
        return users;
    }

    private static TableColumn<KeyRow, String> column(String title, String property, double width) {
        TableColumn<KeyRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Table row, public getters for the {@link PropertyValueFactory}.
     */
    public static class KeyRow {
        private final JsonApiKey key;

        KeyRow(JsonApiKey key) {
            this.key = key;
        }

        private static String date(Long millis) {
            return millis == null ? "" : new DateTime(millis).toString("yyyy-MM-dd HH:mm");
        }

        public String getId() {
            return key.getId();
        }

        public String getName() {
            return key.getName();
        }

        public String getUserName() {
            return key.getUserName() != null ? key.getUserName() : String.valueOf(key.getUserId());
        }

        public String getCreated() {
            return date(key.getCreated());
        }

        public String getLastUsed() {
            return date(key.getLastUsed());
        }

        public String getStatus() {
            if (Boolean.TRUE.equals(key.getRevoked())) return "revoked";
            if (key.getExpires() != null && key.getExpires() < System.currentTimeMillis()) return "expired";
            return "active";
        }
    }
}
