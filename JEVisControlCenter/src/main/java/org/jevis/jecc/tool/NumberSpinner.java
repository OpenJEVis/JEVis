package org.jevis.jecc.tool;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static final String ARROW = "NumberSpinnerArrow";
    public static final String NUMBER_FIELD = "NumberField";
    public static final String NUMBER_SPINNER = "NumberSpinner";
    public static final String SPINNER_BUTTON_UP = "SpinnerButtonUp";
    public static final String SPINNER_BUTTON_DOWN = "SpinnerButtonDown";
    private static final Logger logger = LogManager.getLogger(NumberSpinner.class);
    private final String BUTTONS_BOX = "ButtonsBox";
    private final NumberTextField numberField;
    private final ObjectProperty<BigDecimal> stepWidthProperty = new SimpleObjectProperty<>();
    private final double ARROW_SIZE = 4;
    private final MFXButton incrementButton;
    private final MFXButton decrementButton;
    private final NumberBinding buttonHeight;
    private final NumberBinding spacing;
    private BigDecimal min;
    private BigDecimal max;

    public NumberSpinner() {
        this(BigDecimal.ZERO, BigDecimal.ONE);
    }

    public NumberSpinner(BigDecimal value, BigDecimal stepWidth) {
        this(value, stepWidth, NumberFormat.getInstance());
    }

    public NumberSpinner(BigDecimal value, BigDecimal stepWidth, NumberFormat nf) {
        super();
        this.setId(NUMBER_SPINNER);
        this.stepWidthProperty.set(stepWidth);

        // MFXTextField
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

        // Painting the up and down arrows
        Path arrowUp = new Path();
        arrowUp.setId(ARROW);
        arrowUp.getElements().addAll(new MoveTo(-ARROW_SIZE, 0), new LineTo(ARROW_SIZE, 0),
                new LineTo(0, -ARROW_SIZE), new LineTo(-ARROW_SIZE, 0));
        // mouse clicks should be forwarded to the underlying button
        arrowUp.setMouseTransparent(true);

        Path arrowDown = new Path();
        arrowDown.setId(ARROW);
        arrowDown.getElements().addAll(new MoveTo(-ARROW_SIZE, 0), new LineTo(ARROW_SIZE, 0),
                new LineTo(0, ARROW_SIZE), new LineTo(-ARROW_SIZE, 0));
        arrowDown.setMouseTransparent(true);

        // the spinner buttons scale with the textfield size
        // TODO: the following approach leads to the desired result, but it is 
        // not fully understood why and obviously it is not quite elegant
        buttonHeight = numberField.heightProperty().subtract(3).divide(2);
        // give unused space in the buttons VBox to the incrementBUtton
        spacing = numberField.heightProperty().subtract(2).subtract(buttonHeight.multiply(2));

        // inc/dec buttons
        VBox buttons = new VBox();
        buttons.setId(BUTTONS_BOX);
        incrementButton = new MFXButton();
        incrementButton.setId(SPINNER_BUTTON_UP);
        incrementButton.prefWidthProperty().bind(numberField.heightProperty());
        incrementButton.minWidthProperty().bind(numberField.heightProperty());
        incrementButton.maxHeightProperty().bind(buttonHeight.add(spacing));
        incrementButton.prefHeightProperty().bind(buttonHeight.add(spacing));
        incrementButton.minHeightProperty().bind(buttonHeight.add(spacing));
        incrementButton.setFocusTraversable(false);

        // Paint arrow path on button using a StackPane
        StackPane incPane = new StackPane();
        incPane.getChildren().addAll(incrementButton, arrowUp);
        incPane.setAlignment(Pos.CENTER);

        decrementButton = new MFXButton();
        decrementButton.setId(SPINNER_BUTTON_DOWN);
        decrementButton.prefWidthProperty().bind(numberField.heightProperty());
        decrementButton.minWidthProperty().bind(numberField.heightProperty());
        decrementButton.maxHeightProperty().bind(buttonHeight);
        decrementButton.prefHeightProperty().bind(buttonHeight);
        decrementButton.minHeightProperty().bind(buttonHeight);

        decrementButton.setFocusTraversable(false);

        setListener();

        StackPane decPane = new StackPane();
        decPane.getChildren().addAll(decrementButton, arrowDown);
        decPane.setAlignment(Pos.CENTER);

        buttons.getChildren().addAll(incPane, decPane);
        this.getChildren().addAll(numberField, buttons);
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

    public ObjectProperty<BigDecimal> numberProperty() {
        return numberField.numberProperty();
    }

    public final BigDecimal getNumber() {
        return numberField.getNumber();
    }

    public final void setNumber(BigDecimal value) {
        //if (numberField.getNumber().compareTo(BigDecimal.valueOf(1)) != 0) {
        numberField.setNumber(value);
        //}

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