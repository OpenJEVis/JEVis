package org.jevis.jecc.plugin.object.extension.OPC;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.joda.time.DateTime;

public class DataValueRow {

    public final ObjectProperty<DataValue> dateValueroperty;
    public final StringProperty tsProperty;
    public final StringProperty valueProperty;
    public final StringProperty qualityProperty;

    public DataValueRow(DataValue dataValue) {
        this.dateValueroperty = new SimpleObjectProperty<>(dataValue);

        //tsProperty =new SimpleStringProperty(dateValueroperty.getValue().getSourceTime().toString());
        tsProperty = new SimpleStringProperty((new DateTime(dateValueroperty.getValue().getSourceTime().getJavaDate())).toString());
        valueProperty = new SimpleStringProperty(dateValueroperty.getValue().getValue().getValue().toString());
        qualityProperty = new SimpleStringProperty(dateValueroperty.getValue().getStatusCode().toString());
    }
}
