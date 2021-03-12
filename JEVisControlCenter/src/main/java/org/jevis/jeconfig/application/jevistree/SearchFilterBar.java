package org.jevis.jeconfig.application.jevistree;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;

import java.util.List;

public class SearchFilterBar extends GridPane {
    private static final Logger logger = LogManager.getLogger(SearchFilterBar.class);
    private final Finder finder;
    private final Label labelFilter = new Label(I18n.getInstance().getString("searchbar.filter"));
    private final Label labelSearch = new Label(I18n.getInstance().getString("searchbar.search"));
    private final Spinner<String> searchField = new Spinner<>();
    private final JFXComboBox<JEVisTreeFilter> filterBox;
    public final BooleanProperty showReplace = new SimpleBooleanProperty(false);
    boolean replaceMode = true;

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


    public SearchFilterBar(JEVisTree tree, List<JEVisTreeFilter> filter, Finder finder, boolean showReplaceBar) {
        //super(4);
        setPadding(new Insets(8));
        this.setBackground(new Background(new BackgroundFill(Color.web("#f4f4f4"), CornerRadii.EMPTY, new Insets(0))));
        this.finder = finder;

        ObservableList<JEVisTreeFilter> filterList = FXCollections.observableArrayList(filter);
        filterBox = new JFXComboBox<>(filterList);
        filterBox.setMaxWidth(Double.MAX_VALUE);
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


        Label replaceLabel = new Label(I18n.getInstance().getString("searchbar.label.replacewith"));
        TextField replaceField = new TextField();
        JFXButton replace = new JFXButton(I18n.getInstance().getString("searchbar.button.replace"));
        JFXButton replaceAll = new JFXButton(I18n.getInstance().getString("searchbar.button.alltreedown"));

        this.setHgap(5);
        this.setVgap(12);
        this.setPadding(new Insets(8));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        HBox replaceButtons = new HBox(replace, replaceAll);
        replaceButtons.setSpacing(5);

        Region spacer = new Region();
        spacer.setPrefWidth(10);

        this.add(labelSearch, 0, 0);
        this.add(searchField, 1, 0);
        this.add(spacer, 2, 0);
        this.add(labelFilter, 3, 0);
        this.add(filterBox, 4, 0);

        showReplace.addListener((observable, oldValue, newValue) -> {
            System.out.println("Show replacement gui: " + newValue);
            if (newValue) {
                SearchFilterBar.this.add(replaceLabel, 0, 1);
                SearchFilterBar.this.add(replaceField, 1, 1);
                SearchFilterBar.this.add(replaceButtons, 3, 1, 2, 1);

            } else {
                SearchFilterBar.this.getChildren().remove(replaceLabel);
                SearchFilterBar.this.getChildren().remove(replaceField);
                SearchFilterBar.this.getChildren().remove(replaceButtons);
            }
        });

        replaceMode = JEConfig.getExpert() && showReplaceBar;
        Platform.runLater(() -> {
            showReplace.setValue(replaceMode);
        });

        replace.setOnAction(event -> {
            final JEVisObject selectedObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
            replaceInObjectName(selectedObject, searchField.getEditor().getText(), replaceField.getText(), false);
        });
        replaceAll.setOnAction(event -> {
            final JEVisObject selectedObject = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();
            replaceInObjectName(selectedObject, searchField.getEditor().getText(), replaceField.getText(), true);
        });
    }

    public SearchFilterBar(JEVisTree tree, List<JEVisTreeFilter> filter, Finder finder) {
        this(tree, filter, finder, true);
    }


    public void enableReplaceMode() {
        if (!replaceMode) {
            replaceMode = true;
        } else {
            replaceMode = false;
        }
        Platform.runLater(() -> {
            showReplace.setValue(replaceMode);
        });

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
