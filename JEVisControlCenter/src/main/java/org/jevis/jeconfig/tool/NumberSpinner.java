package org.jevis.jeconfig.tool;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * JavaFX Control that behaves like a {@link JSpinner} known in Swing. The
 * number in the textfield can be incremented or decremented by a configurable
 * stepWidth using the arrow buttons in the control or the up and down arrow
 * keys.
 *
 * @author Thomas Bolz
 */
public class NumberSpinner extends HBox {

    private static final Logger logger = LogManager.getLogger(NumberSpinner.class);
    public static final String ARROW = "NumberSpinnerArrow";
    public static final String NUMBER_FIELD = "NumberField";
    public static final String DEFAULT_STYLE_CLASS = "NumberSpinner";
    public static final String SPINNER_BUTTON_UP = "SpinnerButtonUp";
    public static final String SPINNER_BUTTON_DOWN = "SpinnerButtonDown";
    private final String BUTTONS_BOX = "ButtonsBox";
    private final NumberTextField numberField;
    private final ObjectProperty<BigDecimal> stepWidthProperty = new SimpleObjectProperty<>();
    private final double ARROW_SIZE = 4;
    private final Button incrementButton;
    private final Button decrementButton;
    private final NumberBinding buttonHeight;
    private final NumberBinding spacing;
    private BigDecimal min;
    private BigDecimal max;

    public NumberSpinner() {
        this(BigDecimal.ZERO, BigDecimal.ONE);
    }

    public NumberSpinner(BigDecimal value, BigDecimal stepWidth) {
        this(value, stepWidth, NumberFormat.getInstance(I18n.getInstance().getLocale()));
    }

    public NumberSpinner(BigDecimal value, BigDecimal stepWidth, NumberFormat nf) {
        super();
        this.setId(DEFAULT_STYLE_CLASS);
        this.stepWidthProperty.set(stepWidth);

        // JFXTextField
        numberField = new NumberTextField(value, nf);
        numberField.setId(NUMBER_FIELD);
        numberField.setPrefWidth(40);

        // Enable arrow keys for dec/inc
        numberField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.DOWN) {
                    decrement();
                    keyEvent.consume();
                }
                if (keyEvent.getCode() == KeyCode.UP) {
                    increment();
                    keyEvent.consume();
                }
            }
        });

        // the spinner buttons scale with the textfield size
        // TODO: the following approach leads to the desired result, but it is 
        // not fully understood why and obviously it is not quite elegant
        buttonHeight = numberField.heightProperty().subtract(3).divide(2);
        // give unused space in the buttons VBox to the incrementBUtton
        spacing = numberField.heightProperty().subtract(2).subtract(buttonHeight.multiply(2));

        // inc/dec buttons
        incrementButton = new Button("", JEConfig.getSVGImage(Icon.ARROW_UP, 12, 12));
//        incrementButton.setId(SPINNER_BUTTON_UP);
        incrementButton.prefWidthProperty().bind(numberField.heightProperty());
        incrementButton.minWidthProperty().bind(numberField.heightProperty());
        incrementButton.maxHeightProperty().bind(buttonHeight.add(spacing));
        incrementButton.prefHeightProperty().bind(buttonHeight.add(spacing));
        incrementButton.minHeightProperty().bind(buttonHeight.add(spacing));
//        incrementButton.setFocusTraversable(false);

        // Paint arrow path on button using a StackPane


        decrementButton = new Button("", JEConfig.getSVGImage(Icon.ARROW_DOWN, 12, 12));
//        decrementButton.setId(SPINNER_BUTTON_DOWN);
        decrementButton.prefWidthProperty().bind(numberField.heightProperty());
        decrementButton.minWidthProperty().bind(numberField.heightProperty());
        decrementButton.maxHeightProperty().bind(buttonHeight);
        decrementButton.prefHeightProperty().bind(buttonHeight);
        decrementButton.minHeightProperty().bind(buttonHeight);
//        decrementButton.setFocusTraversable(false);

        setListener();

        VBox buttons = new VBox();
        buttons.setId(BUTTONS_BOX);

        buttons.getChildren().setAll(incrementButton, decrementButton);
        getChildren().setAll(numberField, buttons);
    }

    private static String getItemText(Cell<Integer> cell) {
        if (cell.getItem() != null) {
            return String.valueOf(cell.getItem());
        } else return "";
    }

    public static NumberSpinner createNumberSpinner(final Cell<Integer> cell) {
        Integer item = cell.getItem();
        final NumberSpinner numberSpinner = new NumberSpinner(new BigDecimal(item), new BigDecimal(1));
        numberSpinner.setMin(new BigDecimal(-1));

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        numberSpinner.getNumberField().numberProperty().addListener((observableValue, bigDecimal, t1) -> {
            if (!t1.equals(bigDecimal)) {
                cell.commitEdit(t1.intValue());
            }
        });
        numberSpinner.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return numberSpinner;
    }

    public static void startEdit(final Cell<Integer> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final NumberSpinner numberSpinner) {
        if (numberSpinner != null) {
            numberSpinner.setNumber(new BigDecimal(cell.getItem()));
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, numberSpinner);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(numberSpinner.getNumberField());

            // requesting focus so that key input can immediately go into the
            // TextField (see RT-28132)
            numberSpinner.getNumberField().requestFocus();
        }
    }

    public static void cancelEdit(Cell<Integer> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<Integer> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final NumberSpinner numberSpinner) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (numberSpinner != null) {
                    numberSpinner.setNumber(new BigDecimal(cell.getItem()));
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, numberSpinner);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(numberSpinner);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }

    public HBox getBox() {
        HBox hBox = new HBox();

        VBox buttons = new VBox();
        buttons.setId(BUTTONS_BOX);

        buttons.getChildren().setAll(incrementButton, decrementButton);
        hBox.getChildren().setAll(numberField, buttons);

        return hBox;
    }

    public NumberTextField getNumberField() {
        return numberField;
    }

    public Button getIncrementButton() {
        return incrementButton;
    }

    public Button getDecrementButton() {
        return decrementButton;
    }

    /**
     * increment number value by stepWidth
     */
    private void increment() {
        BigDecimal value = numberField.getNumber();
        value = value.add(stepWidthProperty.get());
        numberField.setNumber(value);
    }

    /**
     * decrement number value by stepWidth
     */
    private void decrement() {
        //check if the value is bigger than 0
        if (numberField.getNumber().compareTo(BigDecimal.valueOf(1)) == 1) {
            BigDecimal value = numberField.getNumber();
            value = value.subtract(stepWidthProperty.get());
            numberField.setNumber(value);
        }

    }

    public final void setNumber(BigDecimal value) {
        //if (numberField.getNumber().compareTo(BigDecimal.valueOf(1)) != 0) {
        numberField.setNumber(value);
        //}

    }

    public ObjectProperty<BigDecimal> numberProperty() {
        return numberField.numberProperty();
    }

    public final BigDecimal getNumber() {
        return numberField.getNumber();
    }

    // debugging layout bounds
    public void dumpSizes() {
        logger.info("numberField (layout)=" + numberField.getLayoutBounds());
        logger.info("buttonInc (layout)=" + incrementButton.getLayoutBounds());
        logger.info("buttonDec (layout)=" + decrementButton.getLayoutBounds());
        logger.info("binding=" + buttonHeight.toString());
        logger.info("spacing=" + spacing.toString());
    }

    public void setMin(BigDecimal min) {
        this.min = min;
        setListener();
    }

    private void setListener() {
        incrementButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                if (max != null) {
                    if (numberField.getNumber().compareTo(max) <= 0)
                        increment();
                } else increment();
                ae.consume();
            }
        });

        decrementButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent ae) {
                if (min != null) {
                    if (numberField.getNumber().compareTo(min) >= 0)
                        decrement();
                } else decrement();
                ae.consume();
            }
        });
    }

    public void setMax(BigDecimal max) {
        this.max = max;
        setListener();
    }
}
