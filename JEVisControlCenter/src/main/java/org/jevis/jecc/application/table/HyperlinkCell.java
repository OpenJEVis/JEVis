package org.jevis.jecc.application.table;


import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.awt.*;
import java.net.URI;


public class HyperlinkCell<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                Hyperlink hyperlink = new Hyperlink();
                hyperlink.setText(item);
                hyperlink.setOnAction(actionEvent -> {
                            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                                try {
                                    desktop.browse(new URI(item));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                );

                if (item == null || empty) {
                    setText(null);
                } else {
                    setGraphic(hyperlink);
                }
            }
        };
    }
};



