package org.jevis.jeconfig.plugin.dtrc.dialogs;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dtrc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TemplateCalculationInputDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(TemplateCalculationInputDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private Response response = Response.CANCEL;

    public TemplateCalculationInputDialog(JEVisDataSource ds, RCTemplate rcTemplate, TemplateInput templateInput, List<TimeFrame> allowedTimeFrames) {
        super();

        setTitle(I18n.getInstance().getString("plugin.trc.inputdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.trc.inputdialog.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(6));
        gridPane.setVgap(4);
        gridPane.setHgap(4);

        ColumnConstraints labelWidth = new ColumnConstraints(80);
        gridPane.getColumnConstraints().add(0, labelWidth);
        ColumnConstraints fieldWidth = new ColumnConstraints(150);
        gridPane.getColumnConstraints().add(1, fieldWidth);

        Label classesLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.classlabel"));
        GridPane.setHgrow(classesLabel, Priority.ALWAYS);

        Label attributeLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.attributelabel"));
        GridPane.setHgrow(attributeLabel, Priority.ALWAYS);

        Label variableNameLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.variablenamelabel"));
        GridPane.setHgrow(variableNameLabel, Priority.ALWAYS);

        Label typeLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.typelabel"));
        GridPane.setHgrow(typeLabel, Priority.ALWAYS);

        boolean oldNameFound = templateInput.getVariableName() != null;
        String oldName = templateInput.getVariableName();
        AtomicBoolean changedName = new AtomicBoolean(false);
        JFXTextField variableNameField = new JFXTextField(templateInput.getVariableName());
        GridPane.setHgrow(variableNameField, Priority.ALWAYS);

        variableNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            templateInput.setVariableName(newValue);
            if (oldNameFound) {
                changedName.set(true);
            }
        });

        JFXComboBox<InputVariableType> inputVariableTypeJFXComboBox = new JFXComboBox<>(FXCollections.observableArrayList(InputVariableType.values()));
        Callback<ListView<InputVariableType>, ListCell<InputVariableType>> inputVariableTypeJFXComboBoxCellFactory = new Callback<ListView<InputVariableType>, ListCell<InputVariableType>>() {
            @Override
            public ListCell<InputVariableType> call(ListView<InputVariableType> param) {
                return new JFXListCell<InputVariableType>() {
                    @Override
                    protected void updateItem(InputVariableType obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            switch (obj) {
                                case SUM:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.sum"));
                                    break;
                                case AVG:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.avg"));
                                    break;
                                case MIN:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.min"));
                                    break;
                                case MAX:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.max"));
                                    break;
                                case YEARLY_VALUE:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.yearlyvalue"));
                                    break;
                                case LAST:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.last"));
                                    break;
                                case NON_PERIODIC:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.nonperiodic"));
                                    break;
                                case STRING:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.type.string"));
                                    break;
                                case FORMULA:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
                                    break;
                                case RANGING_VALUE:
                                    setText(I18n.getInstance().getString("plugin.dtrc.dialog.rangingvalue"));
                                    break;
                            }
                        }
                    }
                };
            }
        };

        inputVariableTypeJFXComboBox.setCellFactory(inputVariableTypeJFXComboBoxCellFactory);
        inputVariableTypeJFXComboBox.setButtonCell(inputVariableTypeJFXComboBoxCellFactory.call(null));
        GridPane.setHgrow(inputVariableTypeJFXComboBox, Priority.ALWAYS);

        Label isQuantitylabel = new Label("Quantity");
        JFXCheckBox isQuantityCheckBox = new JFXCheckBox();
        isQuantityCheckBox.setSelected(templateInput.isQuantity());
        isQuantityCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> templateInput.setQuantity(t1));

        Label limiterLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.limiterlabel"));
        GridPane.setHgrow(limiterLabel, Priority.ALWAYS);

        JFXTextField filterField = new JFXTextField();
        filterField.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
        GridPane.setHgrow(filterField, Priority.ALWAYS);

        if (templateInput.getFilter() != null) {
            filterField.setText(templateInput.getFilter());
        }

        JFXCheckBox groupCheckBox = new JFXCheckBox(I18n.getInstance().getString("plugin.dtrc.dialog.grouplabel"));
        GridPane.setHgrow(groupCheckBox, Priority.ALWAYS);

        if (templateInput.getGroup() != null) {
            groupCheckBox.setSelected(templateInput.getGroup());
        }

        groupCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> templateInput.setGroup(newValue));

        JFXComboBox<JEVisClass> classSelector = new JFXComboBox<>();
        JFXComboBox<JEVisType> attributeSelector = new JFXComboBox<>();

        JFXListView<JEVisObject> listView = new JFXListView<>();
        listView.setMinSize(450, 550);
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> listViewCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new JFXListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            try {
                                if (classSelector.getSelectionModel().getSelectedItem() != null &&
                                        classSelector.getSelectionModel().getSelectedItem().getName().equals("Clean Data")) {
                                    setText(CommonMethods.getFirstParentalDataObject(obj).getName() + " : " + obj.getID());
                                } else {
                                    setText(obj.getName() + " : " + obj.getID());
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        };
        listView.setCellFactory(listViewCellFactory);
        GridPane.setHgrow(listView, Priority.ALWAYS);

        try {
            List<JEVisClass> allClasses = ds.getJEVisClasses();
            List<JEVisClass> classesWithoutDirectories = new ArrayList<>();
            JEVisClass dirClass = ds.getJEVisClass("Directory");
            for (JEVisClass jeVisClass : allClasses) {
                boolean isDir = false;
                for (JEVisClassRelationship jeVisClassRelationship : jeVisClass.getRelationships()) {
                    if (jeVisClassRelationship.getType() == 0 && jeVisClassRelationship.getEnd() != null && jeVisClassRelationship.getEnd().equals(dirClass)) {
                        isDir = true;
                        break;
                    }
                }
                if (!isDir) classesWithoutDirectories.add(jeVisClass);
            }

            ObservableList<JEVisClass> jeVisClasses = FXCollections.observableArrayList(classesWithoutDirectories);
            jeVisClasses.sort((o1, o2) -> {
                try {
                    return ac.compare(I18nWS.getInstance().getClassName(o1.getName()), I18nWS.getInstance().getClassName(o2.getName()));
                } catch (JEVisException e) {
                    logger.error("Could not sort object {} and object {}", o1, o2, e);
                }
                return 0;
            });

            classSelector.setItems(jeVisClasses);

            Callback<ListView<JEVisClass>, ListCell<JEVisClass>> classCellFactory = new Callback<ListView<JEVisClass>, ListCell<JEVisClass>>() {
                @Override
                public ListCell<JEVisClass> call(ListView<JEVisClass> param) {
                    return new JFXListCell<JEVisClass>() {
                        @Override
                        protected void updateItem(JEVisClass obj, boolean empty) {
                            super.updateItem(obj, empty);
                            if (obj == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                try {
                                    setText(I18nWS.getInstance().getClassName(obj.getName()));
                                } catch (JEVisException e) {
                                    logger.error("Could not get class name", e);
                                }
                            }
                        }
                    };
                }
            };

            classSelector.setCellFactory(classCellFactory);
            classSelector.setButtonCell(classCellFactory.call(null));

            if (templateInput.getObjectClass() != null) {
                JEVisClass selectedClass = ds.getJEVisClass(templateInput.getObjectClass());
                classSelector.getSelectionModel().select(selectedClass);
            } else {
                classSelector.getSelectionModel().selectFirst();
            }

            JEVisClass firstClass = classSelector.getSelectionModel().getSelectedItem();
            List<JEVisObject> objectsOfFirstClass = ds.getObjects(firstClass, false);
            objectsOfFirstClass.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

            ObservableList<JEVisObject> objects = FXCollections.observableArrayList(objectsOfFirstClass);
            createFilterList(templateInput, filterField, listView, objects);

            List<JEVisType> types = firstClass.getTypes();
            types.sort((o1, o2) -> {
                try {
                    return ac.compare(o1.getName(), o2.getName());
                } catch (JEVisException e) {
                    logger.error("Could not sort object {} and object {}", o1, o2, e);
                }
                return 0;
            });
            types.add(new JEVisNameType(ds, firstClass));

            attributeSelector.getItems().setAll(types);
            Callback<ListView<JEVisType>, ListCell<JEVisType>> attributeCellFactory = new Callback<ListView<JEVisType>, ListCell<JEVisType>>() {
                @Override
                public ListCell<JEVisType> call(ListView<JEVisType> param) {
                    return new JFXListCell<JEVisType>() {
                        @Override
                        protected void updateItem(JEVisType obj, boolean empty) {
                            super.updateItem(obj, empty);
                            if (obj == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                try {
                                    if (!obj.getName().equals("name")) {
                                        setText(I18nWS.getInstance().getTypeName(classSelector.getSelectionModel().getSelectedItem().getName(), obj.getName()));
                                    } else {
                                        setText(I18n.getInstance().getString("plugin.graph.table.name"));
                                    }
                                } catch (JEVisException e) {
                                    logger.error("Could not get type name", e);
                                }
                            }
                        }
                    };
                }
            };

            attributeSelector.setCellFactory(attributeCellFactory);
            attributeSelector.setButtonCell(attributeCellFactory.call(null));

            if (templateInput.getAttributeName() != null) {
                JEVisType selectedType = firstClass.getType(templateInput.getAttributeName());
                attributeSelector.getSelectionModel().select(selectedType);
            } else {
                attributeSelector.getSelectionModel().selectFirst();
            }

            classSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    try {
                        templateInput.setObjectClass(newValue.getName());
                        attributeSelector.getSelectionModel().selectFirst();

                        List<JEVisType> newTypes = newValue.getTypes();
                        newTypes.sort((o1, o2) -> {
                            try {
                                return ac.compare(o1.getName(), o2.getName());
                            } catch (JEVisException e) {
                                logger.error("Could not sort object {} and object {}", o1, o2, e);
                            }
                            return 0;
                        });
                        newTypes.add(new JEVisNameType(ds, newValue));

                        List<JEVisObject> newObjects = ds.getObjects(newValue, false);

                        if (newValue.getName().equals("Clean Data")) {
                            newObjects.sort((o1, o2) -> {
                                JEVisObject firstParentalDataObjectO1 = null;
                                try {
                                    firstParentalDataObjectO1 = CommonMethods.getFirstParentalDataObject(o1);
                                    JEVisObject firstParentalDataObjectO2 = CommonMethods.getFirstParentalDataObject(o2);
                                    return ac.compare(firstParentalDataObjectO1.getName(), firstParentalDataObjectO2.getName());
                                } catch (JEVisException e) {
                                    e.printStackTrace();
                                }
                                return -1;
                            });
                        } else {
                            newObjects.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                        }

                        Platform.runLater(() -> {
                            createFilterList(templateInput, filterField, listView, FXCollections.observableArrayList(newObjects));
                            attributeSelector.setItems(FXCollections.observableArrayList(newTypes));
                            attributeSelector.getSelectionModel().selectFirst();
                        });
                    } catch (JEVisException e) {
                        logger.error("Could not set new class name", e);
                    }
                }
            });

            attributeSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    try {
                        templateInput.setAttributeName(newValue.getName());
                        templateInput.buildVariableName(classSelector.getSelectionModel().getSelectedItem(), newValue);
                        variableNameField.setText(templateInput.getVariableName());
                    } catch (JEVisException e) {
                        logger.error("Could not set new attribute name", e);
                    }
                }
            });

        } catch (JEVisException e) {
            logger.error("Could not load JEVisClasses", e);
        }

        Label formulaLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
        JFXComboBox<TemplateFormula> formulaBox = new JFXComboBox<>(FXCollections.observableArrayList(rcTemplate.getTemplateFormulas()));
        TemplateFormula none = new TemplateFormula();
        none.setName(I18n.getInstance().getString("dialog.regression.type.none"));
        formulaBox.getItems().add(0, none);
        Callback<ListView<TemplateFormula>, ListCell<TemplateFormula>> formulaCellFactory = new Callback<ListView<TemplateFormula>, ListCell<TemplateFormula>>() {
            @Override
            public ListCell<TemplateFormula> call(ListView<TemplateFormula> param) {
                return new JFXListCell<TemplateFormula>() {
                    @Override
                    protected void updateItem(TemplateFormula obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getName());
                        }
                    }
                };
            }
        };

        formulaBox.setCellFactory(formulaCellFactory);
        formulaBox.setButtonCell(formulaCellFactory.call(null));

        Label dependencyLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.dependencylabel"));
        JFXComboBox<TemplateInput> dependencyBox = new JFXComboBox<>(FXCollections.observableArrayList(rcTemplate.getTemplateInputs()));
        TemplateInput noneInput = new TemplateInput();
        noneInput.setVariableName(I18n.getInstance().getString("dialog.regression.type.none"));
        dependencyBox.getItems().add(0, noneInput);
        Callback<ListView<TemplateInput>, ListCell<TemplateInput>> dependencyCellFactory = new Callback<ListView<TemplateInput>, ListCell<TemplateInput>>() {
            @Override
            public ListCell<TemplateInput> call(ListView<TemplateInput> param) {
                return new JFXListCell<TemplateInput>() {
                    @Override
                    protected void updateItem(TemplateInput obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getVariableName());
                        }
                    }
                };
            }
        };

        dependencyBox.setCellFactory(dependencyCellFactory);
        dependencyBox.setButtonCell(dependencyCellFactory.call(null));

        Label timeRestrictionsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.timerestictions"));
        JFXCheckBox timeRestrictionEnabledCheckBox = new JFXCheckBox(I18n.getInstance().getString("jevistree.dialog.enable.title.enable"));
        timeRestrictionEnabledCheckBox.setSelected(templateInput.getTimeRestrictionEnabled());
        timeRestrictionEnabledCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> templateInput.setTimeRestrictionEnabled(t1));

        JFXComboBox<TimeFrame> fixedTimeFrameBox = new JFXComboBox<>();
        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> fixedTimeFrameJFXComboBoxCellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                return new JFXListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getListName());
                        }
                    }
                };
            }
        };

        fixedTimeFrameBox.setCellFactory(fixedTimeFrameJFXComboBoxCellFactory);
        fixedTimeFrameBox.setButtonCell(fixedTimeFrameJFXComboBoxCellFactory.call(null));

        fixedTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateInput.getFixedTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateInput.getFixedTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            fixedTimeFrameBox.getSelectionModel().select(selectedTimeFrame);
        }
        fixedTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateInput.setFixedTimeFrame(t1.getID()));

        JFXComboBox<TimeFrame> reducingTimeFrameBox = new JFXComboBox<>();
        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> reducingTimeFrameJFXComboBoxCellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                return new JFXListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getListName());
                        }
                    }
                };
            }
        };

        reducingTimeFrameBox.setCellFactory(reducingTimeFrameJFXComboBoxCellFactory);
        reducingTimeFrameBox.setButtonCell(reducingTimeFrameJFXComboBoxCellFactory.call(null));

        reducingTimeFrameBox.getItems().setAll(allowedTimeFrames);
        if (templateInput.getReducingTimeFrame() != null) {
            TimeFrame selectedTimeFrame = allowedTimeFrames.stream().filter(timeFrame -> templateInput.getReducingTimeFrame().equals(timeFrame.getID())).findFirst().orElse(null);
            reducingTimeFrameBox.getSelectionModel().select(selectedTimeFrame);
        }
        reducingTimeFrameBox.getSelectionModel().selectedItemProperty().addListener((observableValue, timeFrame, t1) -> templateInput.setReducingTimeFrame(t1.getID()));

        HBox timeRestrictionsBox = new HBox(6, timeRestrictionEnabledCheckBox, fixedTimeFrameBox, reducingTimeFrameBox);

        if (templateInput.getVariableType() != null && templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
            inputVariableTypeJFXComboBox.getSelectionModel().select(InputVariableType.valueOf(templateInput.getVariableType()));
            dependencyLabel.setVisible(false);
            dependencyBox.setVisible(false);
            formulaLabel.setVisible(true);
            formulaBox.setVisible(true);
        } else if (templateInput.getVariableType() != null && templateInput.getVariableType().equals(InputVariableType.RANGING_VALUE.toString())) {
            inputVariableTypeJFXComboBox.getSelectionModel().select(InputVariableType.valueOf(templateInput.getVariableType()));
            dependencyLabel.setVisible(true);
            dependencyBox.setVisible(true);
            formulaLabel.setVisible(false);
            formulaBox.setVisible(false);
        } else if (templateInput.getVariableType() != null) {
            inputVariableTypeJFXComboBox.getSelectionModel().select(InputVariableType.valueOf(templateInput.getVariableType()));
            dependencyLabel.setVisible(false);
            dependencyBox.setVisible(false);
            formulaLabel.setVisible(false);
            formulaBox.setVisible(false);
        } else {
            inputVariableTypeJFXComboBox.getSelectionModel().selectFirst();
        }

        inputVariableTypeJFXComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            templateInput.setVariableType(newValue.toString());
            if (newValue.equals(InputVariableType.FORMULA)) {
                Platform.runLater(() -> {
                    formulaLabel.setVisible(true);
                    formulaBox.setVisible(true);
                    dependencyLabel.setVisible(false);
                    dependencyBox.setVisible(false);
                });
            } else if (newValue.equals(InputVariableType.RANGING_VALUE)) {
                Platform.runLater(() -> {
                    formulaLabel.setVisible(false);
                    formulaBox.setVisible(false);
                    dependencyLabel.setVisible(true);
                    dependencyBox.setVisible(true);
                });
            } else {
                Platform.runLater(() -> {
                    formulaLabel.setVisible(false);
                    formulaBox.setVisible(false);
                    dependencyLabel.setVisible(false);
                    dependencyBox.setVisible(false);
                });
            }
        });

        if (templateInput.getTemplateFormula() != null) {
            TemplateFormula selectedFormula = formulaBox.getItems().stream().filter(templateFormula -> templateFormula.getId().equals(templateInput.getTemplateFormula())).findFirst().orElse(null);
            if (selectedFormula != null)
                formulaBox.getSelectionModel().select(selectedFormula);
            else {
                formulaBox.getSelectionModel().selectFirst();
            }
        } else if (templateInput.getDependency() != null) {
            TemplateInput selectedInput = dependencyBox.getItems().stream().filter(ti -> ti.getId().equals(templateInput.getDependency())).findFirst().orElse(null);
            if (selectedInput != null)
                dependencyBox.getSelectionModel().select(selectedInput);
            else {
                dependencyBox.getSelectionModel().selectFirst();
            }
        } else {
            formulaBox.getSelectionModel().selectFirst();
        }

        formulaBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.getName().equals(none.getId())) {
                templateInput.setTemplateFormula(newValue.getId());
            } else {
                templateInput.setTemplateFormula(null);
            }
        });

        dependencyBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.getId().equals(none.getId())) {
                templateInput.setDependency(newValue.getId());
            } else {
                templateInput.setDependency(null);
            }
        });

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteType = new ButtonType(I18n.getInstance().getString("jevistree.menu.delete"), ButtonBar.ButtonData.OTHER);

        this.getDialogPane().getButtonTypes().addAll(deleteType, cancelType, okType);

        Button deleteButton = (Button) this.getDialogPane().lookupButton(deleteType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            response = Response.OK;
            if (changedName.get() && oldName != null) {
                rcTemplate.getTemplateFormulas().forEach(templateFormula -> {
                    if (templateFormula.getFormula().contains(oldName)) {
                        logger.debug("Detected variable name change. Found formula {}, replacing old name \"{}\" with \"{}\"", templateFormula.getName(), oldName, variableNameField.getText());
                        templateFormula.setFormula(templateFormula.getFormula().replace(oldName, variableNameField.getText()));
                    }
                });
            }
            this.close();
        });

        cancelButton.setOnAction(event -> this.close());

        deleteButton.setOnAction(event -> {
            response = Response.DELETE;
            this.close();
        });

        int row = 0;
        gridPane.add(classesLabel, 0, row);
        gridPane.add(classSelector, 1, row);
        gridPane.add(listView, 2, row, 1, 8);
        row++;

        gridPane.add(attributeLabel, 0, row);
        gridPane.add(attributeSelector, 1, row);
        row++;

        gridPane.add(variableNameLabel, 0, row);
        gridPane.add(variableNameField, 1, row);
        row++;

        gridPane.add(typeLabel, 0, row);
        gridPane.add(inputVariableTypeJFXComboBox, 1, row);
        row++;

        gridPane.add(isQuantitylabel, 0, row);
        gridPane.add(isQuantityCheckBox, 1, row);
        row++;

        gridPane.add(formulaLabel, 0, row);
        gridPane.add(formulaBox, 1, row);
        row++;

        gridPane.add(dependencyLabel, 0, row);
        gridPane.add(dependencyBox, 1, row);
        row++;

        gridPane.add(limiterLabel, 0, row);
        gridPane.add(filterField, 1, row);
        row++;

        gridPane.add(groupCheckBox, 0, row, 2, 1);
        row++;

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));
        gridPane.add(separator2, 0, row, 3, 1);
        row++;

        gridPane.add(timeRestrictionsLabel, 0, row, 1, 1);
        gridPane.add(timeRestrictionsBox, 1, row, 2, 1);
        row++;

        gridPane.add(new Label("UUID: " + templateInput.getId()), 0, row, 3, 1);
        row++;

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));
        gridPane.add(separator3, 0, row, 3, 1);
        row++;

        getDialogPane().setContent(gridPane);
    }

    private void createFilterList(TemplateInput templateInput, JFXTextField limiterField, JFXListView<JEVisObject> listView, ObservableList<JEVisObject> objects) {
        FilteredList<JEVisObject> filteredList = new FilteredList<>(objects, s -> true);

        limiterField.textProperty().addListener(obs -> {
            String filter = limiterField.getText();
            templateInput.setFilter(filter);
            if (filter == null || filter.length() == 0) {
                filteredList.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredList.setPredicate(s -> {
                        String name = TRCPlugin.getRealName(s);
                        boolean match = false;
                        String string = name.toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredList.setPredicate(s -> {
                        String name = TRCPlugin.getRealName(s);

                        String string = name.toLowerCase();
                        return string.contains(filter.toLowerCase());
                    });
                }
            }
        });

        listView.setItems(filteredList);
    }

    public Response getResponse() {
        return response;
    }
}
