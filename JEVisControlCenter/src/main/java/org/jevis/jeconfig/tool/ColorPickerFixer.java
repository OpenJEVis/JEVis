package org.jevis.jeconfig.tool;

import com.jfoenix.controls.JFXCheckBox;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.control.ColorPicker;
import org.jevis.jeconfig.TopMenu;

import java.lang.reflect.Field;

public class ColorPickerFixer {

    public ColorPickerFixer(ColorPicker colorPicker) {

        try {

//            ColorPickerSkin skin = (ColorPickerSkin) upperColorPicker.getSkin();

//            List<Field> privateFields = new ArrayList<>();

            Field[] fields = JFXCheckBox.class.getDeclaredFields();
            for (Field field : fields) {
//                if (Modifier.isPrivate(field.getModifiers())) {
//                    field.setAccessible(true);
                field.setAccessible(true);
//                }
            }
//
//            Field pField = skin.getClass().getField("popupContent");
//            System.out.println("Is accessable: " + pField.isAccessible());
//            pField.setAccessible(true);
//            System.out.println("pField: " + pField.get(skin));
//
//
//            Field linkField = ColorPalette.class.getField("customColorLink");
//
//
//            ColorPalette colorPalette = (ColorPalette) pField.get(skin);
//            Hyperlink hyperlink = (Hyperlink) linkField.get(colorPalette);
//            hyperlink.setOnAction(event -> {
//                System.out.println("hyperlink: " + hyperlink);
//            });

//            ColorPalette colorPalette = (ColorPalette) skin.getClass().getField("popupContent");

//            Field[] allFields = skin.getClass().getDeclaredFields();
//
//            for (Field field : allFields) {
////                if (Modifier.isPrivate(field.getModifiers())) {
//                privateFields.add(field);
////                }
//            }


            //TODO JFX17 Testen
            MFXButton buttonShowColorPicker = new MFXButton("Show custom color dialog!");
            buttonShowColorPicker.setOnAction(actionEvent -> {
                ColorPicker customColorDialog = new ColorPicker();
                customColorDialog.show();
            });

            ColorPicker customColorDialog = new ColorPicker();
            customColorDialog.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    TopMenu.applyActiveTheme(customColorDialog.getScene());
                }
            });
            customColorDialog.show();

//            Stage newStage = new Stage();
//            Scene scene = new Scene(new StackPane(buttonShowColorPicker));
//            newStage.setScene(scene);
//            newStage.show();


//            List<Field> privateFields = new ArrayList<>();
//            Field[] allFields = upperColorPicker.getSkin().getClass().getDeclaredFields();
//            for (Field field : allFields) {
////                if (Modifier.isPrivate(field.getModifiers())) {
//                privateFields.add(field);
////                }
//            }
//            privateFields.forEach(field -> {
//                System.out.println("field: " + field);
//            });

//            Field dialogField = CustomColorDialog.class.getDeclaredField("dialog");
//            dialogField.setAccessible(true);
//            Stage dialog = (Stage) dialogField.get(upperColorPicker);
//            dialog.setAlwaysOnTop(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
