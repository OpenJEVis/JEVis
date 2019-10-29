package org.jevis.jeconfig.tool;

//import com.sun.javafx.scene.control.skin.CustomColorDialog;
import com.sun.javafx.scene.control.CustomColorDialog;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import org.jevis.jeconfig.JEConfig;

import java.lang.reflect.Field;

public class ColorPickerFixer {

    public ColorPickerFixer(ColorPicker colorPicker) {

        try {

//            ColorPickerSkin skin = (ColorPickerSkin) upperColorPicker.getSkin();

//            List<Field> privateFields = new ArrayList<>();

            Field[] fields = CheckBox.class.getDeclaredFields();
            for (Field field : fields) {
//                if (Modifier.isPrivate(field.getModifiers())) {
//                    field.setAccessible(true);
                field.setAccessible(true);
                System.out.println(field.getName() + " : " + field.get(colorPicker));
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


            Button buttonShowColorPicker = new Button("Show custom color dialog!");
            buttonShowColorPicker.setOnAction(actionEvent -> {
                CustomColorDialog customColorDialog = new CustomColorDialog(JEConfig.getStage());
                customColorDialog.show();
            });

            CustomColorDialog customColorDialog = new CustomColorDialog(JEConfig.getStage());
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
