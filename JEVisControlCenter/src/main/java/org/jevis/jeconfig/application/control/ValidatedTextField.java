package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Pos;
import org.apache.commons.validator.routines.AbstractFormatValidator;
import org.jevis.commons.i18n.I18n;

import java.util.regex.Pattern;

public class ValidatedTextField extends JFXTextField {

    public static Pattern integerPattern = Pattern.compile("[0-9]*");

    private final AbstractFormatValidator validator;
    private final String originalText;


    public ValidatedTextField(String text, AbstractFormatValidator validator) {
        super(text);
        this.validator = validator;
        this.originalText = text;
        this.setAlignment(Pos.CENTER_RIGHT);
        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("-- observable: " + observable.getValue() + "  oldValue: " + oldValue + "  newValue: " + newValue);
            if (!validate(getText())) {
                setText(originalText);
            }
        });
    }


//    @Override
//    public void replaceText(IndexRange range, String text) {
//        if (validate(text)) {
//            super.replaceText(range, text);
//        }
//    }
//
//    @Override
//    public void replaceText(int start, int end, String text) {
//        if (validate(text)) {
//            super.replaceText(start, end, text);
//        }
//    }


    private boolean validate(String text) {
        try {
//            /** backspace is ok **/
            if (text.isEmpty() || validator == null) {
                return true;
            }
            System.out.println("Validate: '" + text + "'");
            return validator.isValid(text, I18n.getInstance().getLocale());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }
}
