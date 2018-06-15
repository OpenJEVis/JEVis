package org.jevis.jeconfig.plugin.object.extension.calculation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;

public class CalculationViewController {

    private final org.apache.logging.log4j.Logger log = LogManager.getLogger(CalculationViewController.class);

    @FXML
    private Button buttonCeil;

    @FXML
    private Button buttonLog;

    @FXML
    private Button buttonDot;

    @FXML
    private Button buttonSeven;

    @FXML
    private Button buttonTwo;

    @FXML
    private Button buttonBracketOpen;

    @FXML
    private Button buttonThree;

    @FXML
    private Button buttonMultiply;

    @FXML
    private Button buttonAcos;

    @FXML
    private Button buttonNotEquals;

    @FXML
    private Button buttonRound;

    @FXML
    private TextField expressionField;

    @FXML
    private Button buttonZero;

    @FXML
    private Button buttonEquals;

    @FXML
    private Button buttonFour;

    @FXML
    private Button buttonDivide;

    @FXML
    private Button buttonAtan;

    @FXML
    private Button buttonGreater;

    @FXML
    private Button buttonClear;

    @FXML
    private Button buttonBracketClose;

    @FXML
    private Button buttonSqrt;

    @FXML
    private Button buttonE;

    @FXML
    private Button buttonRad;

    @FXML
    private Button buttonGreaterEquals;

    @FXML
    private Button buttonExp;

    @FXML
    private Button buttonPi;

    @FXML
    private Button buttonTan;

    @FXML
    private Button buttonSmaller;

    @FXML
    private Button buttonMod;

    @FXML
    private Button buttonOr;

    @FXML
    private Button buttonMinus;

    @FXML
    private Button buttonDeg;

    @FXML
    private Button buttonCos;

    @FXML
    private Button buttonFloor;

    @FXML
    private Button buttonAbs;

    @FXML
    private Button buttonSmallerEquals;

    @FXML
    private Button buttonEight;

    @FXML
    private Button buttonNine;

    @FXML
    private Button buttonFive;

    @FXML
    private Button buttonSix;

    @FXML
    private Button buttonMax;

    @FXML
    private Button buttonOne;

    @FXML
    private Button buttonNegate;

    @FXML
    private Button buttonBack;

    @FXML
    private Button buttonMin;

    @FXML
    private Button buttonAnd;

    @FXML
    private Button buttonPow;

    @FXML
    private Button buttonSin;

    @FXML
    private Button buttonAtan2;

    @FXML
    private ListView<?> listInputs;

    @FXML
    private Button buttonAsin;

    @FXML
    void onSeven(ActionEvent event) {

    }

    @FXML
    void onEight(ActionEvent event) {

    }

    @FXML
    void onNine(ActionEvent event) {

    }

    @FXML
    void onFour(ActionEvent event) {

    }

    @FXML
    void onSix(ActionEvent event) {

    }

    @FXML
    void onFive(ActionEvent event) {

    }

    @FXML
    void onThree(ActionEvent event) {

    }

    @FXML
    void onTwo(ActionEvent event) {

    }

    @FXML
    void onOne(ActionEvent event) {

    }

    @FXML
    void onZero(ActionEvent event) {

    }

    @FXML
    void onMod(ActionEvent event) {

    }

    @FXML
    void onModulo(ActionEvent event) {

    }

    @FXML
    void onDivide(ActionEvent event) {

    }

    @FXML
    void onMultiply(ActionEvent event) {

    }

    @FXML
    void onMinus(ActionEvent event) {

    }

    @FXML
    void onDot(ActionEvent event) {

    }

    @FXML
    void onBracketOpen(ActionEvent event) {

    }

    @FXML
    void onPi(ActionEvent event) {

    }

    @FXML
    void onBracketClose(ActionEvent event) {

    }

    @FXML
    void onE(ActionEvent event) {

    }

    @FXML
    void onDeg(ActionEvent event) {

    }

    @FXML
    void onClear(ActionEvent event) {

    }

    @FXML
    void onBack(ActionEvent event) {

    }

    @FXML
    void onRad(ActionEvent event) {

    }

    @FXML
    void onPow(ActionEvent event) {

    }

    @FXML
    void onSqrt(ActionEvent event) {

    }

    @FXML
    void onLog(ActionEvent event) {

    }

    @FXML
    void onMin(ActionEvent event) {

    }

    @FXML
    void onCeil(ActionEvent event) {

    }

    @FXML
    void onAbs(ActionEvent event) {

    }

    @FXML
    void onMax(ActionEvent event) {

    }

    @FXML
    void onRound(ActionEvent event) {

    }

    @FXML
    void onExp(ActionEvent event) {

    }

    @FXML
    void onFloor(ActionEvent event) {

    }

    @FXML
    void onSin(ActionEvent event) {

    }

    @FXML
    void onCos(ActionEvent event) {

    }

    @FXML
    void onTan(ActionEvent event) {

    }

    @FXML
    void onAsin(ActionEvent event) {

    }

    @FXML
    void onAcos(ActionEvent event) {

    }

    @FXML
    void onAtan2(ActionEvent event) {

    }

    @FXML
    void onAtan(ActionEvent event) {

    }

    @FXML
    void onNegate(ActionEvent event) {

    }

    @FXML
    void onSmaller(ActionEvent event) {

    }

    @FXML
    void onSmallerEquals(ActionEvent event) {

    }

    @FXML
    void onGreaterEquals(ActionEvent event) {

    }

    @FXML
    void onGreater(ActionEvent event) {

    }

    @FXML
    void onEquals(ActionEvent event) {
        log.info("SRC: " + event.getSource().toString());
        log.info("TAR: " + event.getTarget().toString());
        log.info("TYPE: " + event.getEventType().toString());
    }

    @FXML
    void onNotEquals(ActionEvent event) {

    }

    @FXML
    void onOr(ActionEvent event) {

    }

    @FXML
    void onAnd(ActionEvent event) {

    }

    @FXML
    void onExpressionField(ActionEvent event) {

    }

    @FXML
    void onValidate(ActionEvent event) {

        log.info("you clicked the validate button!");

        buttonSqrt.setText("\u221a");
        buttonSqrt.textProperty().setValue("\u221a");
    }

}