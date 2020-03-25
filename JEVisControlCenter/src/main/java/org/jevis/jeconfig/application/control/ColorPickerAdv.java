package org.jevis.jeconfig.application.control;

import com.google.common.collect.Lists;
//import com.sun.javafx.scene.control.skin.CustomColorDialog;
//import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;

import java.lang.reflect.Field;
import java.util.List;

public class ColorPickerAdv extends HBox {

    private static final Logger logger = LogManager.getLogger(ColorPickerAdv.class);
    private ObjectProperty<Color> selectColorProperty = new SimpleObjectProperty<>(ColorColumn.color_list[0]);
    private Window owner;
    private Color initColor = ColorColumn.color_list[0];
    private ObjectProperty<Color> finalColor = new SimpleObjectProperty<>(ColorColumn.color_list[0]);
    private Button button = new Button();

    public ColorPickerAdv() {
        this(null,null);
    }

    public ColorPickerAdv(Window owner, Color selectedColor) {
        super();
        if (selectedColor != null) {
            initColor= selectedColor;
            finalColor.set(initColor);
        }

        finalColor.addListener((observable, oldValue, newValue) -> {
            initColor=newValue;
            setButtonColor(newValue);
        });

        this.owner = owner;
//        this.setOnAction(event -> show());
//        Node colorRect = colorRectPane(selectColorProperty.get(), 9d);//
//        button.setGraphic(colorRect);
        setButtonColor(initColor);
        button.setOnAction(event -> show());
        HBox.setHgrow(button,Priority.ALWAYS);
        this.getChildren().setAll(button);
//        this.setGraphic(colorRect);
    }

    private void setButtonColor(Color color){
        Node colorRect = colorRectPane(selectColorProperty.get(), 9d);
        button.setGraphic(colorRect);
    }

    public void setValue(Color color){
        selectColorProperty.setValue(color);
        finalColor.setValue(color);

    }
    public Color getValue(){
        return finalColor.getValue();
    }


    public void show() {
        try {
//            System.out.println("Show color");
//            System.out.println("this.getScene(): "+this.getScene());

            /** JFX11

            if(owner==null){
//                System.out.println("this.getScene().getWindow(): "+this.getScene().getWindow());
                owner=this.getScene().getWindow();
            }

            final CustomColorDialog customColorDialog = new CustomColorDialog(owner);
            customColorDialog.setCurrentColor(initColor);

            // remove save button
            VBox controlBox = (VBox) customColorDialog.getChildren().get(1);
            HBox buttonBox = (HBox) controlBox.getChildren().get(2);
            buttonBox.getChildren().remove(0);


            VBox vBox = new VBox();
            vBox.setSpacing(2);
            vBox.setPadding(new Insets(4));
            Runnable saveUseRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Field customColorPropertyField = CustomColorDialog.class
                                .getDeclaredField("customColorProperty"); //$NON-NLS-1$
                        customColorPropertyField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        ObjectProperty<Color> customColorPropertyValue = (ObjectProperty<Color>) customColorPropertyField
                                .get(customColorDialog);

                        customColorPropertyValue.addListener((observable, oldValue, newValue) -> {
                            selectColorProperty.setValue(newValue);
                        });


                        Lists.newArrayList(ColorColumn.color_list).forEach(color -> {
                            Node colorRect = colorRectPane(color, 12d);
                            vBox.getChildren().add(colorRect);
                            colorRect.setOnMouseClicked(event -> {
                                customColorPropertyValue.setValue(color);
                            });
                        });


                    } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                        logger.error(e);
                    }
                }
            };

            saveUseRunnable.run();

            customColorDialog.setOnUse(saveUseRunnable);

            customColorDialog.setOnHidden(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {

//                countDownLatch.countDown();
                }
            });

            Field dialogField = CustomColorDialog.class.getDeclaredField("dialog"); //$NON-NLS-1$
            dialogField.setAccessible(true);

            customColorDialog.getChildren().add(0, vBox);
            customColorDialog.setOnCancel(() -> {
                selectColorProperty.set(initColor);
                finalColor.setValue(initColor);
            });
            customColorDialog.setOnUse(() -> {
                Node colorRect = colorRectPane(selectColorProperty.get(), 10d);
//                this.setGraphic(colorRect);
                button.setGraphic(colorRect);
                finalColor.setValue(selectColorProperty.getValue());
            });


            Stage dialog = (Stage) dialogField.get(customColorDialog);

            dialog.setTitle(ControlResources.getString("ColorPicker.customColorDialogTitle"));
//            dialog.setTitle("Farbe Cool Picker");
            customColorDialog.show();
            dialog.centerOnScreen();
             **/
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }



    public void setOnAction(EventHandler<ActionEvent> value){

//        show();
        finalColor.addListener((observable, oldValue, newValue) -> {
            value.handle(new ActionEvent());
        });

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

    public Node colorRectPane(Color color, Double size) {

        Pane colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.setMinHeight(size);
        colorRectOverlayTwo.setMinWidth(size);
        colorRectOverlayTwo.setMaxHeight(size);
        colorRectOverlayTwo.setMaxWidth(size);

        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0, 0, 0, 0))));
        return colorRectOverlayTwo;

    }
}
