package org.jevis.jeconfig.application.table;


import javafx.scene.control.*;
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
                        System.out.println(item);
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
                            setGraphic(null);
                        } else {
                            setGraphic(hyperlink);
                        }
                    }
                };
            }
        };



