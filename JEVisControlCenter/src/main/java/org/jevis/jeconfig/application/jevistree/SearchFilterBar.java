package org.jevis.jeconfig.application.jevistree;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;

import java.util.List;

public class SearchFilterBar extends VBox {
    private static final Logger logger = LogManager.getLogger(SearchFilterBar.class);
    private final Finder finder;
    private Label labelFilter = new Label(I18n.getInstance().getString("searchbar.filter"));
    private Label labelSearch = new Label(I18n.getInstance().getString("searchbar.search"));
    //    private TextField searchField = new TextField();
    private Spinner<String> searchField = new Spinner<>();
    private ComboBox<JEVisTreeFilter> filterBox;
    private final HBox replacementHbox;
    boolean replaceMode = false;

    private void filter(JEVisTree tree, String newValue, Background originalBackground) {
        if (finder.findMatch(newValue)) {
            searchField.getEditor().setBackground(originalBackground);
        } else {
            searchField.getEditor().setBackground(new Background(new BackgroundFill(Color.ORANGERED, new CornerRadii(2), new Insets(2))));
        }

    }

    public void requestCursor() {
        Platform.runLater(() -> {
            searchField.requestFocus();
        });

    }

    public void goPrevious() {
        finder.goPrevious();
    }

    public void goNext() {
        finder.goNext();
    }

    public void hideFilter(boolean hide) {
        labelFilter.setVisible(!hide);
        filterBox.setVisible(!hide);
    }

    public void hideSearchField(boolean hide) {
        labelSearch.setVisible(!hide);
        searchField.setVisible(!hide);
    }

    public void showObject(JEVisObject object) {
        finder.showObject(object);
    }

    public SearchFilterBar(JEVisTree tree, List<JEVisTreeFilter> filter, Finder finder) {
        super(4);
        setPadding(new Insets(8));

        this.finder = finder;

        ObservableList<JEVisTreeFilter> filterList = FXCollections.observableArrayList(filter);
        filterBox = new ComboBox<>(filterList);
        filterBox.setButtonCell(new ListCell<JEVisTreeFilter>() {
            @Override
            protected void updateItem(JEVisTreeFilter item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getName());
                }
            }
        });
        filterBox.setCellFactory(param -> new ListCell<JEVisTreeFilter>() {
            @Override
            protected void updateItem(JEVisTreeFilter item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getName());
                }
            }
        });
        filterBox.getSelectionModel().selectFirst();
        filterBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue) && newValue != null) {
                tree.setFilter(newValue);
            }
        });


        searchField.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        Background originalBackground = searchField.getEditor().getBackground();
        searchField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue) || newValue.length() < 4) {
                return;
            }
            filter(tree, newValue, originalBackground);


//            if (!newValue.isEmpty() && newValue.length() > 0) {
//                if (finder.findMatch(newValue)) {
//                    searchField.getEditor().setBackground(originalBackground);
//                } else {
//                    searchField.getEditor().setBackground(new Background(new BackgroundFill(Color.ORANGERED, new CornerRadii(2), new Insets(2))));
//                }
//            } else {
//                searchField.getEditor().setBackground(originalBackground);
//                tree.getHighlighterList().clear();
//            }
        });


        searchField.getEditor().setOnKeyReleased(event -> {
            logger.info("key typed: " + event.getCode());
            if (event.getCode() == KeyCode.ENTER) {
                filter(tree, searchField.getEditor().getText(), originalBackground);
            }
        });

        SpinnerValueFactory<String> spinnerFactory = new SpinnerValueFactory<String>() {
            @Override
            public void decrement(int steps) {
                finder.goPrevious();
            }

            @Override
            public void increment(int steps) {
                finder.goNext();
            }
        };
        searchField.setValueFactory(spinnerFactory);
        searchField.setEditable(true);


        this.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(labelFilter, filterBox, labelSearch, searchField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BASELINE_LEFT);
        this.getChildren().add(hBox);

//        Platform.runLater(() -> {
//            filterBox.getSelectionModel().selectLast();
//            System.out.println("select Filter: " + filterBox.getSelectionModel().getSelectedItem().getName());
//            filterBox.getSelectionModel().selectFirst();
//            if (!filter.isEmpty()) {
//                tree.setFilter(filter.get(0));
//            }

//        });

        Label replaceLabel = new Label(I18n.getInstance().getString("searchbar.label.replacewith"));
        TextField replaceField = new TextField();
        Button replace = new Button(I18n.getInstance().getString("searchbar.button.replace"));
        Button replaceAll = new Button(I18n.getInstance().getString("searchbar.button.alltreedown"));

        replacementHbox = new HBox(replaceLabel, replaceField, replace, replaceAll);
        replacementHbox.setSpacing(10);
        replacementHbox.setAlignment(Pos.BASELINE_LEFT);

        replace.setOnAction(event -> {
            final JEVisObject selectedObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
            replaceInObjectName(selectedObject, searchField.getEditor().getText(), replaceField.getText(), false);
        });
        replaceAll.setOnAction(event -> {
            final JEVisObject selectedObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
            replaceInObjectName(selectedObject, searchField.getEditor().getText(), replaceField.getText(), true);
        });
    }

    public void enableReplaceMode() {

        if (!replaceMode) {
            replaceMode = true;
            Platform.runLater(() -> {
//                setPrefHeight(2 * getHeight());
                getChildren().add(replacementHbox);
            });
        } else {
            replaceMode = false;
            Platform.runLater(() -> {
                getChildren().remove(replacementHbox);
//                setPrefHeight(getHeight() / 2);
            });
        }
    }

    private void replaceInObjectName(JEVisObject object, String replacementValue, String newValue, boolean recursive) {
        if (object != null) {
            try {
                String oldName = object.getName();
                String newName = oldName.replace(replacementValue, newValue);

                object.setName(newName);
                object.commit();
            } catch (Exception e) {
                logger.error(e);
            }

            if (recursive) {
                try {
                    for (JEVisObject child : object.getChildren()) {
                        replaceInObjectName(child, replacementValue, newValue, true);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }
}
