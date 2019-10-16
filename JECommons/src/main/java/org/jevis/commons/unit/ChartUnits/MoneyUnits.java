package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jscience.economics.money.Currency;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoneyUnits {

    private final Unit _eur = Currency.EUR;
    private final Unit _usd = Currency.USD;
    private final Unit _gbp = Currency.GBP;
    private final Unit _jpy = Currency.JPY;
    private final Unit _aud = Currency.AUD;
    private final Unit _cad = Currency.CAD;
    private final Unit _cny = Currency.CNY;
    private final Unit _krw = Currency.KRW;
    private final Unit _twd = Currency.TWD;

    private final JEVisUnit eur = new JEVisUnitImp(_eur);
    private final JEVisUnit usd = new JEVisUnitImp(_usd);
    private final JEVisUnit gbp = new JEVisUnitImp(_gbp);
    private final JEVisUnit jpy = new JEVisUnitImp(_jpy);
    private final JEVisUnit aud = new JEVisUnitImp(_aud);
    private final JEVisUnit cad = new JEVisUnitImp(_cad);
    private final JEVisUnit cny = new JEVisUnitImp(_cny);
    private final JEVisUnit krw = new JEVisUnitImp(_krw);
    private final JEVisUnit twd = new JEVisUnitImp(_twd);

    private final ArrayList<JEVisUnit> jeVisUnitArrayList;

    private final ArrayList<Unit> unitArrayList;

    private final ArrayList<String> stringArrayList;
    private final List<JEVisUnit> moneyUnits;

    public MoneyUnits() {
        moneyUnits = new ArrayList<>(Arrays.asList(eur, usd, gbp, jpy, aud, cad, cny, krw, twd));

        stringArrayList = new ArrayList<>(Arrays.asList(
                eur.getLabel(), usd.getLabel(),
                gbp.getLabel(), jpy.getLabel(), aud.getLabel(),
                cad.getLabel(), cny.getLabel(),
                krw.getLabel(), twd.getLabel()
        ));

        unitArrayList = new ArrayList<>(Arrays.asList(
                _eur, _usd, _gbp, _jpy, _aud, _cad, _cny, _krw, _twd)
        );

        jeVisUnitArrayList = new ArrayList<>(Arrays.asList(
                eur, usd, gbp, jpy, aud, cad, cny, krw, twd)
        );
    }

    public boolean isMoneyUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : jeVisUnitArrayList) {
            try {
                if (jeVisUnit.equals(unit)) return true;
            } catch (Exception e) {
            }
            try {
                if (jeVisUnit.getLabel().equals(unit.getLabel())) return true;
            } catch (Exception e) {
            }
            try {
                if (jeVisUnit.getFormula().equals(unit.getFormula())) return true;
            } catch (Exception e) {
            }
            try {
                if (UnitManager.getInstance().format(jeVisUnit).equals(UnitManager.getInstance().format(unit)))
                    return true;
            } catch (Exception e) {
            }
        }
        return false;
    }


}
