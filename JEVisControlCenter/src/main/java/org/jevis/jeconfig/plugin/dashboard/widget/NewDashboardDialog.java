package org.jevis.jeconfig.plugin.dashboard.widget;

import javafx.scene.control.Alert;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ScreenSize;

public class NewDashboardDialog extends Alert {


    public NewDashboardDialog(AlertType alertType) {
        super(Alert.AlertType.CONFIRMATION);

        setTitle(I18n.getInstance().getString("dashboard.widget.editor.title"));
        setHeaderText(I18n.getInstance().getString("dashboard.widget.editor.header"));
        setResizable(true);
        getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1400));


    }


}
