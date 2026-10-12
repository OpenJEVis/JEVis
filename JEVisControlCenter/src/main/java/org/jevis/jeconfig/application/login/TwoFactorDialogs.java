package org.jevis.jeconfig.application.login;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.ws.json.JsonLoginResult;
import org.jevis.commons.ws.json.JsonMfaStatus;
import org.jevis.jeapi.ws.JEVisDataSourceWS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Dialogs for the two-factor authentication: code input during login, TOTP setup with QR code, recovery codes and
 * the management dialog of the user menu. All methods must be called on the JavaFX application thread.
 */
public class TwoFactorDialogs {

    private static final Logger logger = LogManager.getLogger(TwoFactorDialogs.class);
    private static final int MAX_ATTEMPTS = 3;

    private TwoFactorDialogs() {
    }

    private static String t(String key) {
        return I18n.getInstance().getString(key);
    }

    /**
     * Asks for the code of the authenticator app (or a recovery code).
     *
     * @param message optional error of the previous attempt
     * @return the code, or {@code null} if cancelled
     */
    public static String askCode(Window owner, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle(t("app.login.mfa.title"));
        dialog.setHeaderText(message == null ? t("app.login.mfa.code") : message + "\n" + t("app.login.mfa.code"));
        dialog.setContentText("Code:");
        Optional<String> result = dialog.showAndWait();
        return result.map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    }

    /**
     * Mandatory 2FA setup during login (policy requires 2FA but the user has none yet).
     *
     * @return {@code true} if 2FA was activated and the data source is logged in
     */
    public static boolean enrollDuringLogin(Window owner, JEVisDataSourceWS ds, String mfaToken) throws JEVisException {
        JsonMfaStatus setup = ds.setupTotpForEnrollment(mfaToken);
        return confirmSetup(owner, ds, setup, mfaToken, t("app.login.mfa.setup.required"));
    }

    /**
     * Dialog of the user menu: shows the state and enables or disables 2FA for the logged-in user.
     */
    public static void showManageDialog(Window owner, JEVisDataSourceWS ds) {
        JsonMfaStatus status;
        try {
            status = ds.getMfaStatus();
        } catch (JEVisException ex) {
            logger.error("Could not read 2FA status", ex);
            showError(owner, t("app.login.mfa.notsupported"));
            return;
        }

        boolean enabled = Boolean.TRUE.equals(status.getEnabled());
        ButtonType toggle = new ButtonType(enabled ? t("app.login.mfa.disable") : t("app.login.mfa.enable"), ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", toggle, ButtonType.CLOSE);
        alert.initOwner(owner);
        alert.setTitle(t("app.login.mfa.title"));
        alert.setHeaderText(enabled ? t("app.login.mfa.enabled") : t("app.login.mfa.disabled"));
        if (enabled && status.getRecoveryCodesLeft() != null) {
            alert.setContentText(t("app.login.mfa.recoveryleft") + " " + status.getRecoveryCodesLeft());
        }
        if (enabled && Boolean.TRUE.equals(status.getRequired())) {
            alert.getDialogPane().lookupButton(toggle).setDisable(true);
        }
        if (alert.showAndWait().orElse(ButtonType.CLOSE) != toggle) return;

        try {
            if (enabled) {
                disable(owner, ds);
            } else {
                enable(owner, ds);
            }
        } catch (JEVisException ex) {
            logger.error("2FA change failed", ex);
            showError(owner, ex.getMessage());
        }
    }

    private static void enable(Window owner, JEVisDataSourceWS ds) throws JEVisException {
        PasswordField password = new PasswordField();
        if (!showFields(owner, t("app.login.mfa.title"), t("app.login.mfa.password"), password)) return;
        JsonMfaStatus setup = ds.setupTotp(password.getText());
        confirmSetup(owner, ds, setup, null, null);
    }

    private static void disable(Window owner, JEVisDataSourceWS ds) throws JEVisException {
        PasswordField password = new PasswordField();
        TextField code = new TextField();
        code.setPromptText("123456");
        if (!showFields(owner, t("app.login.mfa.disable"), t("app.login.mfa.code"), password, code)) return;
        if (ds.disableMfa(password.getText(), code.getText().trim())) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, t("app.login.mfa.disabled"), ButtonType.OK);
            info.initOwner(owner);
            info.setHeaderText(null);
            info.showAndWait();
        } else {
            showError(owner, t("app.login.mfa.wrongcode"));
        }
    }

    /**
     * Shows QR code and secret, asks for the first code and activates 2FA.
     */
    private static boolean confirmSetup(Window owner, JEVisDataSourceWS ds, JsonMfaStatus setup, String mfaToken, String extraHeader) throws JEVisException {
        String message = extraHeader;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = showSetupDialog(owner, setup, message);
            if (code == null) return false;
            JsonLoginResult result = ds.confirmTotp(code, mfaToken);
            if (result.getStatus() == JsonLoginResult.Status.SUCCESS) {
                showRecoveryCodes(owner, result.getRecoveryCodes() != null ? result.getRecoveryCodes() : Collections.<String>emptyList());
                return true;
            }
            message = result.getMessage() != null ? result.getMessage() : t("app.login.mfa.wrongcode");
        }
        showError(owner, message);
        return false;
    }

    private static String showSetupDialog(Window owner, JsonMfaStatus setup, String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(t("app.login.mfa.title"));
        dialog.setHeaderText((message != null ? message + "\n" : "") + t("app.login.mfa.setup.header"));

        ImageView qr = new ImageView(qrImage(setup.getOtpAuthUri(), 220));
        TextField secret = new TextField(formatSecret(setup.getSecret()));
        secret.setEditable(false);
        TextField code = new TextField();
        code.setPromptText("123456");

        VBox box = new VBox(8, qr, new Label(t("app.login.mfa.secret")), secret, new Label("Code:"), code);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK ? code.getText().trim() : null);
        javafx.application.Platform.runLater(code::requestFocus);
        return dialog.showAndWait().filter(s -> !s.isEmpty()).orElse(null);
    }

    /**
     * Shows the recovery codes once, with a copy button.
     */
    public static void showRecoveryCodes(Window owner, List<String> codes) {
        TextArea area = new TextArea(String.join("\n", codes));
        area.setEditable(false);
        area.setPrefRowCount(Math.max(codes.size(), 4));
        area.setPrefColumnCount(16);
        ButtonType copy = new ButtonType(t("app.login.mfa.copy"), ButtonBar.ButtonData.LEFT);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", copy, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle(t("app.login.mfa.title"));
        alert.setHeaderText(t("app.login.mfa.recovery.header"));
        alert.getDialogPane().setContent(area);
        /* the copy button must not close the dialog */
        alert.getDialogPane().lookupButton(copy).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(area.getText());
            Clipboard.getSystemClipboard().setContent(content);
            event.consume();
        });
        alert.showAndWait();
    }

    /**
     * Shows a dialog with labeled input fields.
     *
     * @return {@code true} if confirmed with OK
     */
    static boolean showFields(Window owner, String title, String header, Control... fields) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        for (int i = 0; i < fields.length; i++) {
            String label = fields[i] instanceof PasswordField ? t("app.login.mfa.password") : "Code";
            if (fields[i].getUserData() instanceof String) label = (String) fields[i].getUserData();
            grid.add(new Label(label + ":"), 0, i);
            grid.add(fields[i], 1, i);
        }
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        if (fields.length > 0) javafx.application.Platform.runLater(fields[0]::requestFocus);
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    static void showError(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle(t("app.login.mfa.title"));
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static String formatSecret(String secret) {
        if (secret == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(secret.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Renders a QR code into a JavaFX image (ZXing core only, no AWT/Swing needed).
     */
    static Image qrImage(String text, int size) {
        try {
            java.util.Map<EncodeHintType, Object> hints = new java.util.EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            WritableImage image = new WritableImage(matrix.getWidth(), matrix.getHeight());
            PixelWriter writer = image.getPixelWriter();
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    writer.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return image;
        } catch (Exception ex) {
            logger.error("Could not create QR code", ex);
            return null;
        }
    }
}
