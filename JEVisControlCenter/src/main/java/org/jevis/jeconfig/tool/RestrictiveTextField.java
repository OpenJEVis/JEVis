/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A text field, which restricts the user's input.
 * <p>
 * The restriction can either be a maximal number of characters which the user
 * is allowed to input or a regular expression class, which contains allowed
 * characters.
 * </p>
 * <p/>
 * <b>Sample, which restricts the input to maximal 10 numeric characters</b>:
 * <pre>
 * {@code
 * RestrictiveTextField textField = new RestrictiveTextField();
 * textField.setMaxLength(10);
 * textField.setRestrict("[0-9]");
 * }
 * </pre>
 *
 * @author Christian Schudt
 */
public class RestrictiveTextField extends TextField {

    private IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength", -1);
    private StringProperty restrict = new SimpleStringProperty(this, "restrict");

    public RestrictiveTextField() {

        textProperty().addListener(new ChangeListener<String>() {

            private boolean ignore;

            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                if (ignore || s1 == null) {
                    return;
                }
                if (maxLength.get() > -1 && s1.length() > maxLength.get()) {
                    ignore = true;
                    setText(s1.substring(0, maxLength.get()));
                    ignore = false;
                }

                if (restrict.get() != null && !restrict.get().equals("") && !s1.matches(restrict.get() + "*")) {
                    ignore = true;
                    setText(s);
                    ignore = false;
                }
            }
        });
    }

    /**
     * The max length property.
     *
     * @return The max length property.
     */
    public IntegerProperty maxLengthProperty() {
        return maxLength;
    }

    /**
     * Gets the max length of the text field.
     *
     * @return The max length.
     */
    public int getMaxLength() {
        return maxLength.get();
    }

    /**
     * Sets the max length of the text field.
     *
     * @param maxLength The max length.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength.set(maxLength);
    }

    /**
     * The restrict property.
     *
     * @return The restrict property.
     */
    public StringProperty restrictProperty() {
        return restrict;
    }

    /**
     * Gets a regular expression character class which restricts the user input.
     *
     *
     * @return The regular expression.
     * @see #getRestrict()
     */
    public String getRestrict() {
        return restrict.get();
    }

    /**
     * Sets a regular expression character class which restricts the user input.
     *
     * E.g. [0-9] only allows numeric values.
     *
     * @param restrict The regular expression.
     */
    public void setRestrict(String restrict) {
        this.restrict.set(restrict);
    }
}
