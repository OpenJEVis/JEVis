
package org.jevis.jecc.application.table;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class SummeryData {
    private final ObservableMap<TableColumn, StringProperty> summeryList;
    private final ObservableMap<TableColumn, StringProperty> cellCSSList = FXCollections.observableHashMap();

    public SummeryData(ObservableMap<TableColumn, StringProperty> summeryList) {
        this.summeryList = summeryList;
    }

    public StringProperty getProperty(TableColumn column) {
        return summeryList.getOrDefault(column, null);
    }

    public ObservableMap<TableColumn, StringProperty> getSummeryList() {
        return summeryList;
    }

    public ObservableMap<TableColumn, StringProperty> getCellCss() {
        return cellCSSList;
    }

    public interface SummeryFunction {

        TableColumn getColumn();

        StringProperty getValue();

        default TableCell<SummeryData, String> getCellFactory() {
            return null;
        }

    }
}
