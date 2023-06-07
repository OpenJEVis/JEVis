package org.jevis.jecc.plugin.object.attribute;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;

public class StringEditor extends BasicEditor {

    private final JEVisAttribute attribute;

    public StringEditor(JEVisAttribute att) {
        super(att);
        this.attribute = att;
    }

    @Override
    public ValidatorBase getValidator() {
        ValidatorBase validator = new LocalDoubleValidator();
        validator.setMessage("Value must be a rational number");
        return validator;

    }

    @Override
    public Object parseValue(String value) {
        try {
            return value;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }


    @Override
    public String formatSample(JEVisSample value) throws JEVisException {
        return value.getValueAsString();
    }

    @Override
    public String formatValue(Object value) {
        return value.toString();
    }

    @Override
    public boolean validateEmptyValue() {
        return true;
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
