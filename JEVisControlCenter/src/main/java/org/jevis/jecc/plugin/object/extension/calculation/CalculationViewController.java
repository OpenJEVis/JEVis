package org.jevis.jecc.plugin.object.extension.calculation;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;


public class CalculationViewController {

    private static final Logger logger = LogManager.getLogger(CalculationViewController.class);

    @FXML
    public MFXButton atan2;
    @FXML
    public ScrollPane variablesScrollPane;
    @FXML
    private MFXButton buttonCeil;
    @FXML
    private MFXButton buttonLog;
    @FXML
    private MFXButton buttonDot;

    @FXML
    private MFXButton buttonSeven;
    @FXML
    private MFXButton buttonTwo;
    @FXML
    private MFXButton buttonBracketOpen;
    @FXML
    private MFXButton buttonThree;
    @FXML
    private MFXButton buttonMultiply;
    @FXML
    private MFXButton buttonAcos;
    @FXML
    private MFXButton buttonNotEquals;
    @FXML
    private MFXButton buttonRound;
    @FXML
    private MFXTextField expressionField;
    @FXML
    private MFXButton buttonZero;
    @FXML
    private MFXButton buttonEquals;
    @FXML
    private MFXButton buttonFour;
    @FXML
    private MFXButton buttonDivide;
    @FXML
    private MFXButton buttonAtan;
    @FXML
    private MFXButton buttonGreater;
    @FXML
    private MFXButton buttonClear;
    @FXML
    private MFXButton buttonBracketClose;
    @FXML
    private MFXButton buttonSqrt;
    @FXML
    private MFXButton buttonE;
    @FXML
    private MFXButton buttonRad;
    @FXML
    private MFXButton buttonGreaterEquals;
    @FXML
    private MFXButton buttonExp;
    @FXML
    private MFXButton buttonPi;
    @FXML
    private MFXButton buttonTan;
    @FXML
    private MFXButton buttonSmaller;
    @FXML
    private MFXButton buttonMod;
    @FXML
    private MFXButton buttonOr;
    @FXML
    private MFXButton buttonOutput;
    @FXML
    private MFXButton buttonMinus;
    @FXML
    private MFXButton buttonDeg;
    @FXML
    private MFXButton buttonCos;
    @FXML
    private MFXButton buttonFloor;
    @FXML
    private MFXButton buttonAbs;
    @FXML
    private MFXButton buttonSmallerEquals;
    @FXML
    private MFXButton buttonEight;
    @FXML
    private MFXButton buttonNine;
    @FXML
    private MFXButton buttonFive;
    @FXML
    private MFXButton buttonSix;
    @FXML
    private MFXButton buttonMax;
    @FXML
    private MFXButton buttonOne;
    @FXML
    private MFXButton buttonBack;
    @FXML
    private MFXButton buttonMin;
    @FXML
    private MFXButton buttonAnd;
    @FXML
    private MFXButton buttonPow;
    @FXML
    private MFXButton buttonSin;
    @FXML
    private MFXButton buttonAtan2;
    @FXML
    private ListView<?> listInputs;
    @FXML
    private MFXButton buttonAsin;
    @FXML
    private FormulaBox formulaBox;
    @FXML
    private VariablesBox variablesBox;
    @FXML
    private MFXButton buttonVerify;

    @FXML
    public void initialize() {
    }


    @FXML
    void onSeven(ActionEvent event) {
        formulaBox.addExpression("7");
    }

    @FXML
    void onEight(ActionEvent event) {
        formulaBox.addExpression("8");
    }

    @FXML
    void onOutput(ActionEvent event) {
        formulaBox.setOnOutputAction();
    }

    @FXML
    void onNine(ActionEvent event) {

        formulaBox.addExpression("9");
    }

    @FXML
    void onFour(ActionEvent event) {
        formulaBox.addExpression("4");
    }

    @FXML
    void onSix(ActionEvent event) {
        formulaBox.addExpression("6");
    }

    @FXML
    void onFive(ActionEvent event) {
        formulaBox.addExpression("5");
    }

    @FXML
    void onThree(ActionEvent event) {
        formulaBox.addExpression("3");
    }

    @FXML
    void onTwo(ActionEvent event) {
        formulaBox.addExpression("2");
    }

    @FXML
    void onOne(ActionEvent event) {
        formulaBox.addExpression("1");
    }

    @FXML
    void onZero(ActionEvent event) {
        formulaBox.addExpression("0");
    }

    @FXML
    void onMod(ActionEvent event) {
        formulaBox.addExpression("");
    }

    @FXML
    void onModulo(ActionEvent event) {
        formulaBox.addExpression("");
    }

    @FXML
    void onDivide(ActionEvent event) {

        formulaBox.addExpression("/");
    }

    @FXML
    void onMultiply(ActionEvent event) {

        formulaBox.addExpression("*");
    }

    @FXML
    void onMinus(ActionEvent event) {
        formulaBox.addExpression("-");
    }

    @FXML
    void onPlus(ActionEvent event) {
        formulaBox.addExpression("+");
    }

    @FXML
    void onDot(ActionEvent event) {
        formulaBox.addExpression(".");
    }

    @FXML
    void onBracketOpen(ActionEvent event) {
        formulaBox.addExpression("(");
    }

    @FXML
    void onPi(ActionEvent event) {
        formulaBox.addExpression("#{Pi}");
    }

    @FXML
    void onBracketClose(ActionEvent event) {
        formulaBox.addExpression(")");
    }

    @FXML
    void onE(ActionEvent event) {
        formulaBox.addExpression("#{E}");
    }

    @FXML
    void onDeg(ActionEvent event) {
        formulaBox.addExpression("toDegrees()");//TODO
    }

    @FXML
    void onClear(ActionEvent event) {
        //TODO
    }

    @FXML
    void onBack(ActionEvent event) {
        formulaBox.backspaceExpression();
    }

    @FXML
    void onRad(ActionEvent event) {
        formulaBox.addExpression("toRadians()");//TODO
    }

    @FXML
    void onPow(ActionEvent event) {
        formulaBox.addExpression("pow()");
    }

    @FXML
    void onSqrt(ActionEvent event) {
        formulaBox.addExpression("sqrt()");
    }

    @FXML
    void onLog(ActionEvent event) {
        formulaBox.addExpression("log()");
    }

    @FXML
    void onMin(ActionEvent event) {
        formulaBox.addExpression("min()");
    }

    @FXML
    void onCeil(ActionEvent event) {
        formulaBox.addExpression("ceil()");
    }

    @FXML
    void onAbs(ActionEvent event) {
        formulaBox.addExpression("abs()");
    }

    @FXML
    void onMax(ActionEvent event) {
        formulaBox.addExpression("max()");
    }

    @FXML
    void onRound(ActionEvent event) {
        formulaBox.addExpression("round()");
    }

    @FXML
    void onExp(ActionEvent event) {
        formulaBox.addExpression("exp()");
    }

    @FXML
    void onFloor(ActionEvent event) {
        formulaBox.addExpression("foor()");
    }

    @FXML
    void onSin(ActionEvent event) {
        formulaBox.addExpression("sin()");
    }

    @FXML
    void onCos(ActionEvent event) {
        formulaBox.addExpression("cos()");
    }

    @FXML
    void onTan(ActionEvent event) {
        formulaBox.addExpression("tan()");
    }

    @FXML
    void onAsin(ActionEvent event) {
        formulaBox.addExpression("asin()");
    }

    @FXML
    void onAcos(ActionEvent event) {
        formulaBox.addExpression("acos()");
    }

    @FXML
    void onAtan2(ActionEvent event) {
        formulaBox.addExpression("atan2()");
    }

    @FXML
    void onAtan(ActionEvent event) {
        formulaBox.addExpression("atan()");
    }

    @FXML
    void onSmaller(ActionEvent event) {
        formulaBox.addExpression("<");
    }

    @FXML
    void onSmallerEquals(ActionEvent event) {
        formulaBox.addExpression("<=");
    }

    @FXML
    void onGreaterEquals(ActionEvent event) {
        formulaBox.addExpression(">=");
    }

    @FXML
    void onGreater(ActionEvent event) {
        formulaBox.addExpression(">");
    }

    @FXML
    void onEquals(ActionEvent event) {
        formulaBox.addExpression("==");
    }

    @FXML
    void onNotEquals(ActionEvent event) {
        formulaBox.addExpression("!=");
    }

    @FXML
    void onOr(ActionEvent event) {
        formulaBox.addExpression("||");
    }

    @FXML
    void onAnd(ActionEvent event) {
        formulaBox.addExpression("&&");
    }

    @FXML
    void onExpressionField(ActionEvent event) {

    }

    @FXML
    void onVerify(ActionEvent event) {
        formulaBox.eval();
    }


    public void setData(JEVisObject obj, MFXButton buttonOutput) {
        logger.info("setData: {}", buttonOutput);
        formulaBox.setCalculation(obj);

        formulaBox.setOutputButton(buttonOutput);
        variablesBox.bindVariableBox(formulaBox, obj);

        variablesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        variablesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }


    public String getFormula() {
        return formulaBox.getExpression();
    }

}