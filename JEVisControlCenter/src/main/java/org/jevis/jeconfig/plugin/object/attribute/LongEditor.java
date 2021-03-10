package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.StackPane;
import org.apache.commons.validator.routines.LongValidator;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;

public class LongEditor extends BasicEditor {

    private final JEVisAttribute attribute;
    private final LongValidator validator = LongValidator.getInstance();

    public LongEditor(StackPane dialogContainer, JEVisAttribute att) {
        super(dialogContainer, att);
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
            long newValue = validator.validate(value, I18n.getInstance().getLocale());

            if (attribute.getInputUnit() != null && attribute.getDisplayUnit() != null) {
                Double doubleWithUnit = attribute.getDisplayUnit().convertTo(attribute.getInputUnit(), newValue);
                return doubleWithUnit.longValue();
            } else {
                return newValue;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    @Override
    public String formatSample(JEVisSample value) throws JEVisException {
        if (attribute.getInputUnit() != null && attribute.getDisplayUnit() != null) {
            return formatValue(value.getValueAsLong(attribute.getDisplayUnit()));
        } else {
            return formatValue(value.getValueAsLong());
        }

    }

    @Override
    public String formatValue(Object value) {
        return validator.validate(value.toString(), I18n.getInstance().getLocale()).toString();
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
                validator.validate(textField.getText(), I18n.getInstance().getLocale());

                hasErrors.set(false);
            } catch (Exception e) {
                hasErrors.set(true);
            }
        }
    }


}
