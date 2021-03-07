package org.jevis.jeconfig.plugin.object.extension.calculation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;


public class CalculationViewController {

    private static final Logger logger = LogManager.getLogger(CalculationViewController.class);

    @FXML
    public JFXButton atan2;
    @FXML
    public ScrollPane variablesScrollPane;
    @FXML
    private JFXButton buttonCeil;
    @FXML
    private JFXButton buttonLog;
    @FXML
    private JFXButton buttonDot;
    @FXML
    private JFXButton buttonSeven;
    @FXML
    private JFXButton buttonTwo;
    @FXML
    private JFXButton buttonBracketOpen;
    @FXML
    private JFXButton buttonThree;
    @FXML
    private JFXButton buttonMultiply;
    @FXML
    private JFXButton buttonAcos;
    @FXML
    private JFXButton buttonNotEquals;
    @FXML
    private JFXButton buttonRound;
    @FXML
    private JFXTextField expressionField;
    @FXML
    private JFXButton buttonZero;
    @FXML
    private JFXButton buttonEquals;
    @FXML
    private JFXButton buttonFour;
    @FXML
    private JFXButton buttonDivide;
    @FXML
    private JFXButton buttonAtan;
    @FXML
    private JFXButton buttonGreater;
    @FXML
    private JFXButton buttonClear;
    @FXML
    private JFXButton buttonBracketClose;
    @FXML
    private JFXButton buttonSqrt;
    @FXML
    private JFXButton buttonE;
    @FXML
    private JFXButton buttonRad;
    @FXML
    private JFXButton buttonGreaterEquals;
    @FXML
    private JFXButton buttonExp;
    @FXML
    private JFXButton buttonPi;
    @FXML
    private JFXButton buttonTan;
    @FXML
    private JFXButton buttonSmaller;
    @FXML
    private JFXButton buttonMod;
    @FXML
    private JFXButton buttonOr;
    @FXML
    private JFXButton buttonOutput;
    @FXML
    private JFXButton buttonMinus;
    @FXML
    private JFXButton buttonDeg;
    @FXML
    private JFXButton buttonCos;
    @FXML
    private JFXButton buttonFloor;
    @FXML
    private JFXButton buttonAbs;
    @FXML
    private JFXButton buttonSmallerEquals;
    @FXML
    private JFXButton buttonEight;
    @FXML
    private JFXButton buttonNine;
    @FXML
    private JFXButton buttonFive;
    @FXML
    private JFXButton buttonSix;
    @FXML
    private JFXButton buttonMax;
    @FXML
    private JFXButton buttonOne;
    @FXML
    private JFXButton buttonBack;
    @FXML
    private JFXButton buttonMin;
    @FXML
    private JFXButton buttonAnd;
    @FXML
    private JFXButton buttonPow;
    @FXML
    private JFXButton buttonSin;
    @FXML
    private JFXButton buttonAtan2;
    @FXML
    private ListView<?> listInputs;
    @FXML
    private JFXButton buttonAsin;
    @FXML
    private FormulaBox formulaBox;
    @FXML
    private VariablesBox variablesBox;
    @FXML
    private JFXButton buttonVerify;
    private StackPane dialogContainer;

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


    public void setData(JEVisObject obj, JFXButton buttonOutput) {
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

    public void setDialogContainer(StackPane dialogContainer) {
        this.dialogContainer = dialogContainer;
        this.formulaBox.setDialogContainer(dialogContainer);
        this.variablesBox.setDialogContainer(dialogContainer);
    }
}