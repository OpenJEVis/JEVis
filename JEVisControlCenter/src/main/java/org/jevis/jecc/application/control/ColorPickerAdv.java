package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jecc.application.Chart.ChartElements.ColorTable;

import java.util.List;

public class ColorPickerAdv extends HBox {

    private static final Logger logger = LogManager.getLogger(ColorPickerAdv.class);
    private final ObjectProperty<Color> selectColorProperty = new SimpleObjectProperty<>(ColorTable.color_list[0]);
    private final ObjectProperty<Color> finalColor = new SimpleObjectProperty<>(ColorTable.color_list[0]);
    private final MFXButton button = new MFXButton();
    private Window owner;
    private Color initColor = ColorTable.color_list[0];

    public ColorPickerAdv() {
        this(null, null);
    }

    public ColorPickerAdv(Window owner, Color selectedColor) {
        super();
        if (selectedColor != null) {
            initColor = selectedColor;
            finalColor.set(initColor);
        }

        finalColor.addListener((observable, oldValue, newValue) -> {
            initColor = newValue;
            setButtonColor(newValue);
        });

        this.owner = owner;
//        this.setOnAction(event -> show());
//        Node colorRect = colorRectPane(selectColorProperty.get(), 9d);//
//        button.setGraphic(colorRect);
        setButtonColor(initColor);
        button.setOnAction(event -> show());
        button.setAlignment(Pos.CENTER);
        HBox.setHgrow(button, Priority.ALWAYS);
        this.getChildren().setAll(button);
        this.setAlignment(Pos.CENTER);
//        this.setGraphic(colorRect);
    }

    public static Node colorRectPane(Color color, Double size) {

        Pane colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.setMinHeight(size);
        colorRectOverlayTwo.setMinWidth(size);
        colorRectOverlayTwo.setMaxHeight(size);
        colorRectOverlayTwo.setMaxWidth(size);

        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        return colorRectOverlayTwo;

    }

    public static ColorPickerAdv createColorPicker(final Cell<Color> cell) {
        Color item = cell.getItem();
        final ColorPickerAdv colorPicker = new ColorPickerAdv();
        colorPicker.setValue(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        colorPicker.setOnAction(event -> {

            cell.commitEdit(colorPicker.getValue());
            event.consume();
        });
        colorPicker.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return colorPicker;
    }

    public static void startEdit(final Cell<Color> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ColorPickerAdv colorPicker) {
        if (colorPicker != null) {
            colorPicker.setValue(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, colorPicker);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(colorPicker);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        colorPicker.requestFocus();
    }

    public static void cancelEdit(Cell<Color> cell, Node graphic) {
        cell.setText(null);
        if (cell.getItem() != null) {
            cell.setGraphic(colorRectPane(cell.getItem(), 12d));
        } else {
            cell.setGraphic(graphic);
        }
    }

    public static void updateItem(final Cell<Color> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ColorPickerAdv colorPicker) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (colorPicker != null) {
                    colorPicker.setValue(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, colorPicker);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(colorPicker);
                }
            } else {
                if (cell.getItem() != null) {
                    cell.setGraphic(colorRectPane(cell.getItem(), 12d));
                }
            }
        }
    }

    public ObjectProperty<Color> selectColorProperty() {
        return selectColorProperty;
    }

    private VBox favoriteColorsBox(List<Color> colors) {
        VBox vBox = new VBox();
        vBox.setSpacing(2);
//        vBox.setPadding(new Insets(4));

        colors.forEach(color -> {
            vBox.getChildren().add(colorRectPane(color, 12d));
        });

        return vBox;
    }

    private void setButtonColor(Color color) {
        Node colorRect = colorRectPane(selectColorProperty.get(), 9d);
        button.setGraphic(colorRect);
    }

    public Color getValue() {
        return finalColor.getValue();
    }

    public void setValue(Color color) {
        selectColorProperty.setValue(color);
        finalColor.setValue(color);

    }

    public void show() {
        try {
//            System.out.println("Show color");
//            System.out.println("this.getScene(): "+this.getScene());
            if (owner == null) {
//                System.out.println("this.getScene().getWindow(): "+this.getScene().getWindow());
                owner = this.getScene().getWindow();
            }

            //TODO JFX17 : Standard Color Picker into Dialog
//            final CustomColorDialog customColorDialog = new CustomColorDialog(owner);
//            customColorDialog.setCurrentColor(initColor);
//
//            // remove save button
//            VBox controlBox = (VBox) customColorDialog.getChildren().get(1);
//            HBox buttonBox = (HBox) controlBox.getChildren().get(2);
//            buttonBox.getChildren().remove(0);
//
//
//            VBox vBox = new VBox();
//            vBox.setSpacing(2);
//            vBox.setPadding(new Insets(4));
//            Runnable saveUseRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Field customColorPropertyField = CustomColorDialog.class
//                                .getDeclaredField("customColorProperty"); //$NON-NLS-1$
//                        customColorPropertyField.setAccessible(true);
//                        @SuppressWarnings("unchecked")
//                        ObjectProperty<Color> customColorPropertyValue = (ObjectProperty<Color>) customColorPropertyField
//                                .get(customColorDialog);
//
//                        customColorPropertyValue.addListener((observable, oldValue, newValue) -> {
//                            selectColorProperty.setValue(newValue);
//                        });
//
//
//                        Lists.newArrayList(ColorTable.color_list).forEach(color -> {
//                            Node colorRect = colorRectPane(color, 12d);
//                            vBox.getChildren().add(colorRect);
//                            colorRect.setOnMouseClicked(event -> {
//                                customColorPropertyValue.setValue(color);
//                            });
//                        });
//
//
//                    } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
//                        logger.error(e);
//                    }
//                }
//            };
//
//            saveUseRunnable.run();
//
//            customColorDialog.setOnUse(saveUseRunnable);
//
//            customColorDialog.setOnHidden(new EventHandler<WindowEvent>() {
//                @Override
//                public void handle(WindowEvent event) {
//
////                countDownLatch.countDown();
//                }
//            });
//
//            Field dialogField = CustomColorDialog.class.getDeclaredField("dialog"); //$NON-NLS-1$
//            dialogField.setAccessible(true);
//
//            customColorDialog.getChildren().add(0, vBox);
//            customColorDialog.setOnCancel(() -> {
//                selectColorProperty.set(initColor);
//                finalColor.setValue(initColor);
//            });
//            customColorDialog.setOnUse(() -> {
//                Node colorRect = colorRectPane(selectColorProperty.get(), 10d);
////                this.setGraphic(colorRect);
//                button.setGraphic(colorRect);
//                finalColor.setValue(selectColorProperty.getValue());
//            });
//
//
//            Stage dialog = (Stage) dialogField.get(customColorDialog);
//
//            dialog.setTitle(ControlResources.getString("ColorPicker.customColorDialogTitle"));
////            dialog.setTitle("Farbe Cool Picker");
//            customColorDialog.show();
//            dialog.centerOnScreen();
        } catch (Exception ex) {
//            logger.error(ex);
//            ex.printStackTrace();
        }
    }

    public void setOnAction(EventHandler<ActionEvent> value) {

//        show();
        finalColor.addListener((observable, oldValue, newValue) -> {
            value.handle(new ActionEvent());
        });

    }
}
