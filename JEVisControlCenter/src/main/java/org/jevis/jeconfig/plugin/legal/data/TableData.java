package org.jevis.jeconfig.plugin.legal.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleIntegerProperty;
import org.jevis.commons.i18n.I18n;

public interface TableData {

    @Expose
    @SerializedName("Nr")
    final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.Legalcadastre.legislation.nr"), 0);


}
