package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import tech.units.indriya.AbstractUnit;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class MoneyUnits {
    private final ArrayList<Unit> unitArrayList = new ArrayList<>();

    private final ArrayList<String> stringArrayList = new ArrayList<>();
    private final List<JEVisUnit> moneyUnits = new ArrayList<>();

    public MoneyUnits() {

        Currency.getAvailableCurrencies().forEach(currency -> {
            Unit one = AbstractUnit.ONE.alternate(currency.getSymbol());

            JEVisUnit jeVisUnit = new JEVisUnitImp(one);
            moneyUnits.add(jeVisUnit);
            stringArrayList.add(currency.getSymbol());
            unitArrayList.add(one);
        });
    }

    public boolean isMoneyUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : moneyUnits) {
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
