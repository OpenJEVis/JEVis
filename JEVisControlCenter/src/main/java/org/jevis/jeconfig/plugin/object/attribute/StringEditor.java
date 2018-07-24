package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import javafx.scene.shape.VLineTo;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class StringEditor extends BasicEditor {

    private JEVisAttribute attribute;
    public StringEditor(JEVisAttribute att) {
        super(att);
        this.attribute=att;
    }

    @Override
    public ValidatorBase getValidator() {
        ValidatorBase validator = new LocalDoubleValidator();
        validator.setMessage("Value must be a rational number");
        return validator;

    }

    @Override
    public Object parseValue(String value) throws ParseException {
        try {
            return value;

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;

    }

    @Override
    public String formatSample(JEVisSample value) throws ParseException,JEVisException {
        return value.getValueAsString();
    }

    @Override
    public String formatValue(Object value) throws ParseException, JEVisException {
        return value.toString();
    }

    @DefaultProperty(value = "icon")
    private class LocalDoubleValidator extends ValidatorBase {


        public LocalDoubleValidator() {
            super();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void eval() {
            if (srcControl.get() instanceof TextInputControl) {
                evalTextInputField();
            }
        }

        private void evalTextInputField() {
            TextInputControl textField = (TextInputControl) srcControl.get();
            try {
                textField.getText();
                hasErrors.set(false);
            } catch (Exception e) {
                hasErrors.set(true);
            }
        }
    }





}
