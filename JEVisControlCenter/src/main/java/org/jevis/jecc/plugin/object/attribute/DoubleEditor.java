package org.jevis.jecc.plugin.object.attribute;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.apache.commons.validator.routines.DoubleValidator;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;

public class DoubleEditor extends BasicEditor {

    private final JEVisAttribute attribute;
    private final DoubleValidator validator = DoubleValidator.getInstance();

    public DoubleEditor(JEVisAttribute att) {
        super(att);
        this.attribute = att;
    }

    @Override
    public ValidatorBase getValidator() {
        ValidatorBase validator = new LocalDoubleValidator();
        validator.setMessage("Value must be a number");
        return validator;

    }

    @Override
    public Object parseValue(String value) {
        try {

            double newVal = validator.validate(value, I18n.getInstance().getLocale());

            if (attribute.getInputUnit() != null && attribute.getDisplayUnit() != null) {
                return attribute.getDisplayUnit().convertTo(attribute.getInputUnit(), newVal);
            } else {
                return newVal;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    @Override
    public String formatSample(JEVisSample value) throws JEVisException {
        if (attribute.getInputUnit() != null && attribute.getDisplayUnit() != null) {
            return formatValue(value.getValueAsDouble(attribute.getDisplayUnit()));
        } else {
            return formatValue(value.getValueAsDouble());
        }

    }

    @Override
    public String formatValue(Object value) {
        return validator.format(value, I18n.getInstance().getLocale());

    }

    @Override
    public boolean validateEmptyValue() {
        return false;
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
                double test = validator.validate(textField.getText(), I18n.getInstance().getLocale()) + 1;

                hasErrors.set(false);
            } catch (Exception e) {
                hasErrors.set(true);
            }
        }
    }
}
