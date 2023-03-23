package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.tool.Layouts;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class LocalNameDialog {

    private static final Logger logger = LogManager.getLogger(NewObjectDialog.class);
    public static String ICON = "translate.png";

    private Response response = Response.CANCEL;
    private JEVisObject object = null;
    private final ObservableList<TranslationRow> translationRows = FXCollections.observableArrayList();
    private String newName = "";

    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public LocalNameDialog(JEVisObject object) {
        this.object = object;
        this.newName = object.getLocalName(I18n.getInstance().getLocale().getLanguage());
    }

    public Response show() {
        Dialog<ButtonType> dialog = new Dialog();
        dialog.initOwner(JEConfig.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);
        dialog.setTitle(I18n.getInstance().getString("jevistree.dialog.translate.title"));
        dialog.setHeaderText(I18n.getInstance().getString("jevistree.dialog.translate.header"));
        dialog.setGraphic(ResourceLoader.getImage(ICON, 50, 50));
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(300);

        /** build form **/


        Label objLocalNameLabel = new Label(I18n.getInstance().getString("jevistree.dialog.new.name"));
        JFXTextField objectLocalNameTest = new JFXTextField();
        Image img = new Image("/icons/flags2/" + I18n.getInstance().getLocale().getLanguage() + ".png");
        ImageView imageViewFlag = new ImageView(img);
        imageViewFlag.fitHeightProperty().setValue(20);
        imageViewFlag.fitWidthProperty().setValue(20);
        imageViewFlag.setSmooth(true);
        objectLocalNameTest.setText(object.getLocalName(I18n.getInstance().getLocale().getLanguage()));
        objectLocalNameTest.textProperty().addListener((observable, oldValue, newValue) -> {
            newName = newValue;
        });
        objectLocalNameTest.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean t, Boolean t1) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (objectLocalNameTest.isFocused() && !objectLocalNameTest.getText().isEmpty()) {
                            objectLocalNameTest.selectAll();
                        }
                    }
                });
            }
        });
        Platform.runLater(() -> objectLocalNameTest.requestFocus());


        object.getLocalNameList().forEach((s, s2) -> {
            translationRows.add(new TranslationRow(s, s2));
        });

        /** add new language row **/
        translationRows.add(new TranslationRow(null, ""));

        TableView<TranslationRow> table = new TableView(translationRows);
        TableColumn firstNameCol = new TableColumn(I18n.getInstance().getString("jevistree.dialog.translate.table.language"));
        firstNameCol.setPrefWidth(200);
        firstNameCol.setMinWidth(200);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<TranslationRow, String>("language"));
        firstNameCol.setCellFactory(param -> new TableCell<TranslationRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        if (item == null) {
                            TranslationRow rowItem = (TranslationRow) getTableRow().getItem();
                            JFXComboBox<Locale> langBox = buildLangBox(null);
                            langBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    rowItem.setLanguage(newValue.getLanguage());
                                    translationRows.add(new TranslationRow(null, ""));
                                } catch (Exception ex) {
                                    ex.printStackTrace();

                                }
                            });
                            setGraphic(langBox);
                        } else {
                            TranslationRow rowItem = (TranslationRow) getTableRow().getItem();
                            JFXComboBox<Locale> langBox = buildLangBox(new Locale(item));
                            langBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    rowItem.setLanguage(newValue.getLanguage());
                                } catch (Exception ex) {
                                    ex.printStackTrace();

                                }
                            });


                            setGraphic(langBox);

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });

        TableColumn lastNameCol = new TableColumn(I18n.getInstance().getString("jevistree.dialog.translate.table.name"));
        lastNameCol.setPrefWidth(220);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<TranslationRow, String>("name"));
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setEditable(true);

        table.getColumns().addAll(firstNameCol, lastNameCol);

        table.setEditable(true);
        GridPane gridPane = new GridPane();
        AnchorPane root = new AnchorPane(gridPane);


        gridPane.setVgap(8);
        gridPane.setHgap(8);
        Layouts.setAnchor(gridPane, 5);

        gridPane.addRow(0, objLocalNameLabel, imageViewFlag, objectLocalNameTest);
        gridPane.add(table, 0, 1, 3, 1);

        GridPane.setHgrow(objectLocalNameTest, Priority.ALWAYS);
        GridPane.setHgrow(table, Priority.ALWAYS);

        dialog.getDialogPane().setContent(root);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(ok, cancel);

        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.FINISH.getButtonData().getTypeCode())) {
                        this.response = Response.YES;
                        try {
                            Map<String, String> commitLangMap = new HashMap<>();
                            translationRows.forEach(translationRow -> {
                                if (translationRow != null && !translationRow.getName().isEmpty()) {
                                    commitLangMap.put(translationRow.getLanguage(), translationRow.getName());

                                }

                            });
                            if (commitLangMap.containsKey(I18n.getInstance().getLocale().getLanguage())) {
                                commitLangMap.replace(I18n.getInstance().getLocale().getLanguage(), newName);
                            } else {

                                commitLangMap.put(I18n.getInstance().getLocale().getLanguage(), newName);
                            }
                            object.setLocalNames(commitLangMap);
                            if (!object.getLocalName("en").isEmpty()) {
                                object.setName(object.getLocalName("en"));
                            } else if (!object.getLocalName("de").isEmpty()) {
                                object.setName(object.getLocalName("de"));
                            } else {
                                Optional<String> firstKey = object.getLocalNameList().keySet().stream().findFirst();
                                if (firstKey.isPresent()) {
                                    object.setName(object.getLocalName(firstKey.get()));
                                }
                            }
                            object.commit();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        this.response = Response.CANCEL;
                    }
                });


        return response;
    }


    public enum Response {
        NO, YES, CANCEL
    }

    private JFXComboBox<Locale> buildLangBox(Locale selected) {
        ObservableList<Locale> langList = FXCollections.observableArrayList(I18n.getInstance().getAvailableLang());
        JFXComboBox picker = new JFXComboBox(langList);

        Callback<ListView<Locale>, ListCell<Locale>> cellFactory = new Callback<ListView<Locale>, ListCell<Locale>>() {
            @Override
            public ListCell<Locale> call(ListView<Locale> param) {
                return new ListCell<Locale>() {
                    @Override
                    protected void updateItem(Locale item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            try {
                                Image img = new Image("/icons/flags2/" + item.getLanguage() + ".png");
                                ImageView iv = new ImageView(img);
                                iv.fitHeightProperty().setValue(20);
                                iv.fitWidthProperty().setValue(20);
                                iv.setSmooth(true);
                                setGraphic(iv);
                            } catch (Exception ex) {
                                /** warning we have missing flags for some Languages **/
                                logger.trace(ex);
                            }
                            setText(item.getDisplayName());
                        }
                    }

                };
            }

        };
        picker.setCellFactory(cellFactory);
        picker.setButtonCell(cellFactory.call(null));

        if (selected != null && !selected.equals("empty")) {
            int index = langList.indexOf(langList.stream().filter(locale -> locale.getLanguage().equals(selected.getLanguage())).findAny().get());
            picker.getSelectionModel().select(index);
        }

        return picker;
    }


    public class TranslationRow {
        private final SimpleStringProperty language;
        private final SimpleStringProperty name;


        public TranslationRow(String language, String name) {
            this.language = new SimpleStringProperty(language);
            this.name = new SimpleStringProperty(name);
        }

        public String getLanguage() {
            return language.get();
        }

        public SimpleStringProperty languageProperty() {
            return language;
        }

        public void setLanguage(String language) {
            this.language.set(language);
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        @Override
        public String toString() {
            return "TranslationRow{" +
                    "language=" + language +
                    ", name=" + name +
                    '}';
        }
    }

}
