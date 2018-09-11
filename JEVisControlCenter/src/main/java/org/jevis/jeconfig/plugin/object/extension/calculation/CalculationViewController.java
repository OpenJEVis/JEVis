package org.jevis.jeconfig.plugin.object.extension.calculation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisObject;


public class CalculationViewController {

    private final org.apache.logging.log4j.Logger log = LogManager.getLogger(CalculationViewController.class);

    @FXML
    public Button atan2;
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
    private Button buttonOutput;
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
    private FormelBox formelBox;
    @FXML
    private VariablesBox variablesBox;
    @FXML
    private Button buttonVerify;

    @FXML
    public void initialize() {
    }


    @FXML
    void onSeven(ActionEvent event) {
        formelBox.addExpression("7");
    }

    @FXML
    void onEight(ActionEvent event) {
        formelBox.addExpression("8");
    }

    @FXML
    void onOutput(ActionEvent event) {
        formelBox.setOnOutputAction();
    }

    @FXML
    void onNine(ActionEvent event) {

        formelBox.addExpression("9");
    }

    @FXML
    void onFour(ActionEvent event) {
        formelBox.addExpression("4");
    }

    @FXML
    void onSix(ActionEvent event) {
        formelBox.addExpression("6");
    }

    @FXML
    void onFive(ActionEvent event) {
        formelBox.addExpression("5");
    }

    @FXML
    void onThree(ActionEvent event) {
        formelBox.addExpression("3");
    }

    @FXML
    void onTwo(ActionEvent event) {
        formelBox.addExpression("2");
    }

    @FXML
    void onOne(ActionEvent event) {
        formelBox.addExpression("1");
    }

    @FXML
    void onZero(ActionEvent event) {
        formelBox.addExpression("0");
    }

    @FXML
    void onMod(ActionEvent event) {
        formelBox.addExpression("");
    }

    @FXML
    void onModulo(ActionEvent event) {
        formelBox.addExpression("");
    }

    @FXML
    void onDivide(ActionEvent event) {

        formelBox.addExpression("/");
    }

    @FXML
    void onMultiply(ActionEvent event) {

        formelBox.addExpression("*");
    }

    @FXML
    void onMinus(ActionEvent event) {
        formelBox.addExpression("-");
    }

    @FXML
    void onPlus(ActionEvent event) {
        formelBox.addExpression("+");
    }

    @FXML
    void onDot(ActionEvent event) {
        formelBox.addExpression(".");
    }

    @FXML
    void onBracketOpen(ActionEvent event) {
        formelBox.addExpression("(");
    }

    @FXML
    void onPi(ActionEvent event) {
        formelBox.addExpression("#{Pi}");
    }

    @FXML
    void onBracketClose(ActionEvent event) {
        formelBox.addExpression(")");
    }

    @FXML
    void onE(ActionEvent event) {
        formelBox.addExpression("#{E}");
    }

    @FXML
    void onDeg(ActionEvent event) {
        formelBox.addExpression("toDegrees()");//TODO
    }

    @FXML
    void onClear(ActionEvent event) {
        //TODO
    }

    @FXML
    void onBack(ActionEvent event) {
        formelBox.backspaceExpression();
    }

    @FXML
    void onRad(ActionEvent event) {
        formelBox.addExpression("toRadians()");//TODO
    }

    @FXML
    void onPow(ActionEvent event) {
        formelBox.addExpression("pow()");
    }

    @FXML
    void onSqrt(ActionEvent event) {
        formelBox.addExpression("sqrt()");
    }

    @FXML
    void onLog(ActionEvent event) {
        formelBox.addExpression("log()");
    }

    @FXML
    void onMin(ActionEvent event) {
        formelBox.addExpression("min()");
    }

    @FXML
    void onCeil(ActionEvent event) {
        formelBox.addExpression("ceil()");
    }

    @FXML
    void onAbs(ActionEvent event) {
        formelBox.addExpression("abs()");
    }

    @FXML
    void onMax(ActionEvent event) {
        formelBox.addExpression("max()");
    }

    @FXML
    void onRound(ActionEvent event) {
        formelBox.addExpression("round()");
    }

    @FXML
    void onExp(ActionEvent event) {
        formelBox.addExpression("exp()");
    }

    @FXML
    void onFloor(ActionEvent event) {
        formelBox.addExpression("foor()");
    }

    @FXML
    void onSin(ActionEvent event) {
        formelBox.addExpression("sin()");
    }

    @FXML
    void onCos(ActionEvent event) {
        formelBox.addExpression("cos()");
    }

    @FXML
    void onTan(ActionEvent event) {
        formelBox.addExpression("tan()");
    }

    @FXML
    void onAsin(ActionEvent event) {
        formelBox.addExpression("asin()");
    }

    @FXML
    void onAcos(ActionEvent event) {
        formelBox.addExpression("acos()");
    }

    @FXML
    void onAtan2(ActionEvent event) {
        formelBox.addExpression("atan2()");
    }

    @FXML
    void onAtan(ActionEvent event) {
        formelBox.addExpression("atan()");
    }

    @FXML
    void onSmaller(ActionEvent event) {
        formelBox.addExpression("<");
    }

    @FXML
    void onSmallerEquals(ActionEvent event) {
        formelBox.addExpression("<=");
    }

    @FXML
    void onGreaterEquals(ActionEvent event) {
        formelBox.addExpression(">=");
    }

    @FXML
    void onGreater(ActionEvent event) {
        formelBox.addExpression(">");
    }

    @FXML
    void onEquals(ActionEvent event) {
        formelBox.addExpression("==");
    }

    @FXML
    void onNotEquals(ActionEvent event) {
        formelBox.addExpression("!=");
    }

    @FXML
    void onOr(ActionEvent event) {
        formelBox.addExpression("||");
    }

    @FXML
    void onAnd(ActionEvent event) {
        formelBox.addExpression("&&");
    }

    @FXML
    void onExpressionField(ActionEvent event) {

    }

    @FXML
    void onVerify(ActionEvent event) {
        formelBox.eval();
    }


    public void setData(JEVisObject obj) {
        System.out.println("setData: " + buttonOutput);
        formelBox.setCalculation(obj);


        formelBox.setOutputButton(buttonOutput);

        variablesBox.bindVaribaleBox(formelBox, obj);

    }

    public String getFormel() {
        return formelBox.getExpression();
    }

}