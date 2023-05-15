package org.jevis.jeconfig.application.table;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ShortColumnCell<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

            @Override
            public TableCell<T, String> call(TableColumn<T, String> param) {
                return new TableCell<T, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setTextOverrun(OverrunStyle.ELLIPSIS);
                        setWrapText(false);
                        setGraphic(null);
                        if (item != null) {
                            if (!empty) {
                                if (item.contains("\n")) {
                                    setText(item.substring(0, item.indexOf("\n")));
                                } else {
                                    setText(item);
                                }
                            }
                        }
                        else {
                            setText(null);
                        }

                    }
                };
            }
        };



