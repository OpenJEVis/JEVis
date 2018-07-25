package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.validation.base.ValidatorBase;
import org.apache.commons.validator.routines.LongValidator;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;
import java.text.ParseException;

public class LongEditor extends BasicEditor {

    private JEVisAttribute attribute;
    private  LongValidator validator = LongValidator.getInstance();

    public LongEditor(JEVisAttribute att) {
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
            long newValue = validator.validate(value,I18n.getInstance().getLocale());

            if (attribute.getInputUnit() != null && attribute.getDisplayUnit() != null) {
                Double doubleWithUnit= Double.valueOf(attribute.getDisplayUnit().converteTo(attribute.getInputUnit(),newValue));
                return doubleWithUnit.longValue();
            }else{
                return newValue;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;

    }

    @Override
    public String formatSample(JEVisSample value) throws ParseException,JEVisException {
        if(attribute.getInputUnit()!=null && attribute.getDisplayUnit()!=null){
            return formatValue(value.getValueAsLong(attribute.getDisplayUnit()));
        }else{
            return formatValue(value.getValueAsLong());
        }

    }

    @Override
    public String formatValue(Object value) throws ParseException, JEVisException {
        return validator.validate(value.toString(),I18n.getInstance().getLocale()).toString();
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

                Long.valueOf(validator.validate(textField.getText(),I18n.getInstance().getLocale())).longValue();

                hasErrors.set(false);
            } catch (Exception e) {
                hasErrors.set(true);
            }
        }
    }





}
