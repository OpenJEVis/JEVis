package org.jevis.commons.unit;

import org.jevis.commons.unit.dimensions.Currency;
import tech.units.indriya.AbstractUnit;

import javax.measure.Unit;
import javax.measure.quantity.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jevis.commons.unit.Currencies.EUR;
import static tech.units.indriya.unit.Units.*;

public class DefaultQuantities {


    private static final Map<Class, Unit> CLASS_TO_SYSTEM_UNIT = new ConcurrentHashMap<>();

    static {
        CLASS_TO_SYSTEM_UNIT.put(Dimensionless.class, AbstractUnit.ONE);
        CLASS_TO_SYSTEM_UNIT.put(ElectricCurrent.class, AMPERE);
        CLASS_TO_SYSTEM_UNIT.put(LuminousIntensity.class, CANDELA);
        CLASS_TO_SYSTEM_UNIT.put(Temperature.class, KELVIN);
        CLASS_TO_SYSTEM_UNIT.put(Mass.class, KILOGRAM);
        CLASS_TO_SYSTEM_UNIT.put(Length.class, METRE);
        CLASS_TO_SYSTEM_UNIT.put(AmountOfSubstance.class, MOLE);
        CLASS_TO_SYSTEM_UNIT.put(Time.class, SECOND);
        CLASS_TO_SYSTEM_UNIT.put(Angle.class, RADIAN);
        CLASS_TO_SYSTEM_UNIT.put(SolidAngle.class, STERADIAN);
        CLASS_TO_SYSTEM_UNIT.put(Frequency.class, HERTZ);
        CLASS_TO_SYSTEM_UNIT.put(Force.class, NEWTON);
        CLASS_TO_SYSTEM_UNIT.put(Pressure.class, PASCAL);
        CLASS_TO_SYSTEM_UNIT.put(Energy.class, JOULE);
        CLASS_TO_SYSTEM_UNIT.put(Power.class, WATT);
        CLASS_TO_SYSTEM_UNIT.put(ElectricCharge.class, COULOMB);
        CLASS_TO_SYSTEM_UNIT.put(ElectricPotential.class, VOLT);
        CLASS_TO_SYSTEM_UNIT.put(ElectricCapacitance.class, FARAD);
        CLASS_TO_SYSTEM_UNIT.put(ElectricResistance.class, OHM);
        CLASS_TO_SYSTEM_UNIT.put(ElectricConductance.class, SIEMENS);
        CLASS_TO_SYSTEM_UNIT.put(MagneticFlux.class, WEBER);
        CLASS_TO_SYSTEM_UNIT.put(MagneticFluxDensity.class, TESLA);
        CLASS_TO_SYSTEM_UNIT.put(ElectricInductance.class, HENRY);
        CLASS_TO_SYSTEM_UNIT.put(LuminousFlux.class, LUMEN);
        CLASS_TO_SYSTEM_UNIT.put(Illuminance.class, LUX);
        CLASS_TO_SYSTEM_UNIT.put(Radioactivity.class, BECQUEREL);
        CLASS_TO_SYSTEM_UNIT.put(RadiationDoseAbsorbed.class, GRAY);
        CLASS_TO_SYSTEM_UNIT.put(RadiationDoseEffective.class, SIEVERT);
        CLASS_TO_SYSTEM_UNIT.put(CatalyticActivity.class, KATAL);
        CLASS_TO_SYSTEM_UNIT.put(Speed.class, METRE_PER_SECOND);
        CLASS_TO_SYSTEM_UNIT.put(Acceleration.class, METRE_PER_SQUARE_SECOND);
        CLASS_TO_SYSTEM_UNIT.put(Area.class, SQUARE_METRE);
        CLASS_TO_SYSTEM_UNIT.put(Volume.class, CUBIC_METRE);
        CLASS_TO_SYSTEM_UNIT.put(Currency.class, EUR);
    }

    public static Map<Class, Unit> getClassToSystemUnit() {
        return CLASS_TO_SYSTEM_UNIT;
    }

    public static Unit getUnitForClass(Class type) {
        return CLASS_TO_SYSTEM_UNIT.get(type);
    }

}
