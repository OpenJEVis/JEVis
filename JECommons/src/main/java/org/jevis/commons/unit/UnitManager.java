/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.unit;

import org.jevis.api.JEVisUnit;
import org.jscience.economics.money.Money;

import javax.measure.quantity.*;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.*;

/**
 * This Class helps with the handling of JScince Lib. This class in not final an
 * may be chaned in the future.
 *
 * @author fs
 */
public class UnitManager {

    private final static UnitManager unitManager = new UnitManager();
    private List<Unit> prefixes;
    private List<String> prefixes2;
    private List<Unit> quantities;
    private List<JEVisUnit> _quantitiesJunit;
    private List<Unit> nonSI;
    private List<JEVisUnit> _nonSIJunit;
    private List<Unit> si;
    private List<JEVisUnit> _siJunit;
    private List<Unit> additonalUnits;
    private HashMap<Unit, String> names;
    private HashMap<JEVisUnit, String> _namesJUnit;
    private HashMap<Unit, String> dimNames;
    private HashMap<JEVisUnit, String> _dimNamesJUnit;

    public static JEVisUnit cloneUnit(JEVisUnit unit) {

        JEVisUnit clone = new JEVisUnitImp();
        clone.setFormula(unit.getFormula());
        clone.setLabel(unit.getLabel());
        clone.setPrefix(unit.getPrefix());
        return clone;
    }

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private UnitManager() {
    }

    public static UnitManager getInstance() {
        return unitManager;
    }

    public List<JEVisUnit> getQuantitiesJunit() {
        if (_quantitiesJunit != null) {
            return _quantitiesJunit;
        }
        _quantitiesJunit = new ArrayList<>();

        for (Unit unit : getQuantities()) {
            _quantitiesJunit.add(new JEVisUnitImp(unit));
        }

        return _quantitiesJunit;
    }

    public List<Unit> getQuantities() {

        if (quantities != null) {
            return quantities;
        }
        quantities = new ArrayList<>();

        quantities.add(Money.BASE_UNIT);
        quantities.add(Acceleration.UNIT);
        quantities.add(AmountOfSubstance.UNIT);
        quantities.add(Angle.UNIT);
        quantities.add(AngularAcceleration.UNIT);
        quantities.add(AngularVelocity.UNIT);
        quantities.add(Area.UNIT);
        quantities.add(CatalyticActivity.UNIT);
        quantities.add(DataAmount.UNIT);
        quantities.add(DataRate.UNIT);
        quantities.add(Dimensionless.UNIT);
        quantities.add(Duration.UNIT);
        quantities.add(DynamicViscosity.UNIT);
        quantities.add(ElectricCapacitance.UNIT);
        quantities.add(ElectricCharge.UNIT);
        quantities.add(ElectricConductance.UNIT);
        quantities.add(ElectricCurrent.UNIT);
        quantities.add(ElectricInductance.UNIT);
        quantities.add(ElectricPotential.UNIT);
        quantities.add(ElectricResistance.UNIT);
        quantities.add(Energy.UNIT);
        quantities.add(Force.UNIT);
        quantities.add(Frequency.UNIT);
        quantities.add(Illuminance.UNIT);
        quantities.add(KinematicViscosity.UNIT);
        quantities.add(Length.UNIT);
        quantities.add(LuminousFlux.UNIT);
        quantities.add(LuminousIntensity.UNIT);
        quantities.add(MagneticFlux.UNIT);
        quantities.add(MagneticFluxDensity.UNIT);
        quantities.add(Mass.UNIT);
        quantities.add(MassFlowRate.UNIT);
        quantities.add(Power.UNIT);
        quantities.add(Pressure.UNIT);
        quantities.add(RadiationDoseAbsorbed.UNIT);
        quantities.add(RadiationDoseEffective.UNIT);
        quantities.add(RadioactiveActivity.UNIT);
        quantities.add(SolidAngle.UNIT);
        quantities.add(Temperature.UNIT);
        quantities.add(Torque.UNIT);
        quantities.add(Velocity.UNIT);
        quantities.add(Volume.UNIT);
        quantities.add(VolumetricDensity.UNIT);
        quantities.add(VolumetricFlowRate.UNIT);

        return quantities;
    }

    /**
     * returns an new unit with the given Prefix
     *
     * @param pre
     * @param unit
     * @return
     */
    public Unit getWithPrefix(String pre, Unit unit) {
        if (pre.equals(PrefixName.ATTO)) {
            return SI.ATTO(unit);
        } else if (pre.equals(PrefixName.CENTI)) {
            return SI.CENTI(unit);
        } else if (pre.equals(PrefixName.DECI)) {
            return SI.DECI(unit);
        } else if (pre.equals(PrefixName.DEKA)) {
            return SI.DEKA(unit);
        } else if (pre.equals(PrefixName.EXA)) {
            return SI.EXA(unit);
        } else if (pre.equals(PrefixName.FEMTO)) {
            return SI.FEMTO(unit);
        } else if (pre.equals(PrefixName.GIGA)) {
            return SI.GIGA(unit);
        } else if (pre.equals(PrefixName.HECTO)) {
            return SI.HECTO(unit);
        } else if (pre.equals(PrefixName.KILO)) {
            return SI.KILO(unit);
        } else if (pre.equals(PrefixName.MEGA)) {
            return SI.MEGA(unit);
        } else if (pre.equals(PrefixName.MICRO)) {
            return SI.MICRO(unit);
        } else if (pre.equals(PrefixName.MILLI)) {
            return SI.MILLI(unit);
        } else if (pre.equals(PrefixName.NANO)) {
            return SI.NANO(unit);
        } else if (pre.equals(PrefixName.PETA)) {
            return SI.PETA(unit);
        } else if (pre.equals(PrefixName.PICO)) {
            return SI.PICO(unit);
        } else if (pre.equals(PrefixName.YOCTO)) {
            return SI.YOCTO(unit);
        } else if (pre.equals(PrefixName.ZEPTO)) {
            return SI.ZEPTO(unit);
        } else if (pre.equals(PrefixName.ZETTA)) {
            return SI.ZETTA(unit);
        } else {
            return unit;
        }
    }

    /**
     * This funktion will change in the future
     *
     * @return
     */
    public List<String> getPrefixes() {
        if (prefixes2 != null) {
            return prefixes2;
        }
        prefixes2 = new ArrayList<>();

        prefixes2.add("");
        prefixes2.add(PrefixName.ZETTA);
        prefixes2.add(PrefixName.EXA);
        prefixes2.add(PrefixName.PETA);
        prefixes2.add(PrefixName.TERA);
        prefixes2.add(PrefixName.GIGA);
        prefixes2.add(PrefixName.MEGA);
        prefixes2.add(PrefixName.KILO);
        prefixes2.add(PrefixName.HECTO);
        prefixes2.add(PrefixName.DEKA);
        prefixes2.add(PrefixName.DECI);
        prefixes2.add(PrefixName.CENTI);
        prefixes2.add(PrefixName.MILLI);
        prefixes2.add(PrefixName.MICRO);
        prefixes2.add(PrefixName.NANO);
        prefixes2.add(PrefixName.PICO);
        prefixes2.add(PrefixName.FEMTO);
        prefixes2.add(PrefixName.ATTO);
        prefixes2.add(PrefixName.ZEPTO);
        prefixes2.add(PrefixName.YOCTO);
        return prefixes2;
    }

    /**
     * Returns an String representaion of the Prefix
     *
     * @param prefix
     * @return
     */
    public String getPrefixChar(JEVisUnit.Prefix prefix) {

        switch (prefix) {
            case ATTO:
                return "a";
            case CENTI:
                return "c";
            case DECI:
                return "d";
            case DEKA:
                return "da";
            case EXA:
                return "E";
            case FEMTO:
                return "f";
            case GIGA:
                return "G";
            case HECTO:
                return "h";
            case KILO:
                return "k";
            case MEGA:
                return "M";
            case MICRO:
                return "µ";
            case MILLI:
                return "m";
            case NANO:
                return "n";
            case PETA:
                return "P";
            case PICO:
                return "P";
            case TERA:
                return "T";
            case YOCTO:
                return "y";
            case ZEPTO:
                return "Z";
            case ZETTA:
                return "Z";
            case NONE:
                return "";
            default:
                throw new AssertionError();
        }
    }

    //TODO is this still in use?
    public String getPrefixChar(String prefix) {

        switch (prefix) {
            case PrefixName.ATTO:
                return "a";
            case PrefixName.CENTI:
                return "c";
            case PrefixName.DECI:
                return "d";
            case PrefixName.DEKA:
                return "da";
            case PrefixName.EXA:
                return "E";
            case PrefixName.FEMTO:
                return "f";
            case PrefixName.GIGA:
                return "G";
            case PrefixName.HECTO:
                return "h";
            case PrefixName.KILO:
                return "k";
            case PrefixName.MEGA:
                return "m";
            case PrefixName.MICRO:
                return "µ";
            case PrefixName.MILLI:
                return "m";
            case PrefixName.NANO:
                return "n";
            case PrefixName.PETA:
                return "P";
            case PrefixName.PICO:
                return "P";
            case PrefixName.TERA:
                return "T";
            case PrefixName.YOCTO:
                return "y";
            case PrefixName.ZEPTO:
                return "Z";
            case PrefixName.ZETTA:
                return "Z";
            default:
                throw new AssertionError();
        }
    }

    public JEVisUnit.Prefix getPrefix(String name, Locale locale) {
//        System.out.println("getPrefix: " + name);
        if (name == null || name.isEmpty()) {
            System.out.println("emty Prefix = none");
            return JEVisUnit.Prefix.NONE;
        }

        switch (name) {
            case PrefixName.ATTO:
                return JEVisUnit.Prefix.ATTO;
            case PrefixName.CENTI:
                return JEVisUnit.Prefix.CENTI;
            case PrefixName.DECI:
                return JEVisUnit.Prefix.DECI;
            case PrefixName.DEKA:
                return JEVisUnit.Prefix.DEKA;
            case PrefixName.EXA:
                return JEVisUnit.Prefix.EXA;
            case PrefixName.FEMTO:
                return JEVisUnit.Prefix.FEMTO;
            case PrefixName.GIGA:
                return JEVisUnit.Prefix.GIGA;
            case PrefixName.HECTO:
                return JEVisUnit.Prefix.HECTO;
            case PrefixName.KILO:
                return JEVisUnit.Prefix.KILO;
            case PrefixName.MEGA:
                return JEVisUnit.Prefix.MEGA;
            case PrefixName.MICRO:
                return JEVisUnit.Prefix.MICRO;
            case PrefixName.MILLI:
                return JEVisUnit.Prefix.MILLI;
            case PrefixName.NANO:
                return JEVisUnit.Prefix.NANO;
            case PrefixName.PETA:
                return JEVisUnit.Prefix.PETA;
            case PrefixName.PICO:
                return JEVisUnit.Prefix.PICO;
            case PrefixName.TERA:
                return JEVisUnit.Prefix.TERA;
            case PrefixName.YOCTO:
                return JEVisUnit.Prefix.YOCTO;
            case PrefixName.ZEPTO:
                return JEVisUnit.Prefix.ZEPTO;
            case PrefixName.ZETTA:
                return JEVisUnit.Prefix.ZETTA;
            case PrefixName.NONE:
                return JEVisUnit.Prefix.NONE;
            default:
                System.out.println("unkown Prefix: " + name);
                return JEVisUnit.Prefix.NONE;
        }
    }

    /**
     * Returns the long name of the prefix for the given locale
     *
     * @param prefix
     * @param locale
     * @return
     */
    public String getPrefixName(JEVisUnit.Prefix prefix, Locale locale) {
        switch (prefix) {
            case ATTO:
                return PrefixName.ATTO;
            case CENTI:
                return PrefixName.CENTI;
            case DECI:
                return PrefixName.DECI;
            case DEKA:
                return PrefixName.DEKA;
            case EXA:
                return PrefixName.EXA;
            case FEMTO:
                return PrefixName.FEMTO;
            case GIGA:
                return PrefixName.GIGA;
            case HECTO:
                return PrefixName.HECTO;
            case KILO:
                return PrefixName.KILO;
            case MEGA:
                return PrefixName.MEGA;
            case MICRO:
                return PrefixName.MICRO;
            case MILLI:
                return PrefixName.MILLI;
            case NANO:
                return PrefixName.NANO;
            case PETA:
                return PrefixName.PETA;
            case PICO:
                return PrefixName.PICO;
            case TERA:
                return PrefixName.TERA;
            case YOCTO:
                return PrefixName.YOCTO;
            case ZEPTO:
                return PrefixName.ZEPTO;
            case ZETTA:
                return PrefixName.ZETTA;
            case NONE:
                return PrefixName.NONE;

            default:
                System.out.println("Waring no prefix name found for: " + prefix);
                return PrefixName.NONE;
        }
    }

    /**
     * Return an new Unit with the set Prefix
     *
     * @param unit
     * @param prefix
     * @return
     */
    public Unit getUnitWithPrefix(Unit unit, JEVisUnit.Prefix prefix) {
//        System.out.println("getUnitWithPrefix: " + prefix);
        switch (prefix) {
            case ATTO:
                return SI.ATTO(unit);
            case CENTI:
                return SI.CENTI(unit);
            case DECI:
                return SI.DECI(unit);
            case DEKA:
                return SI.DEKA(unit);
            case EXA:
                return SI.EXA(unit);
            case FEMTO:
                return SI.FEMTO(unit);
            case GIGA:
                return SI.GIGA(unit);
            case HECTO:
                return SI.HECTO(unit);
            case KILO:
                return SI.KILO(unit);
            case MEGA:
                return SI.MEGA(unit);
            case MICRO:
                return SI.MICRO(unit);
            case MILLI:
                return SI.MILLI(unit);
            case NANO:
                return SI.NANO(unit);
            case PETA:
                return SI.PETA(unit);
            case PICO:
                return SI.PICO(unit);
            case TERA:
                return SI.TERA(unit);
            case YOCTO:
                return SI.YOCTO(unit);
            case ZEPTO:
                return SI.ZEPTO(unit);
            case ZETTA:
                return SI.ZETTA(unit);
            case NONE:
                return unit;
            default:
                throw new AssertionError();
        }
    }

    public List<JEVisUnit> getNonSIJEVisUnits() {
        if (_nonSIJunit != null) {
            return _nonSIJunit;
        }
        _nonSIJunit = new ArrayList<>();

        for (Unit unit : getNonSIUnits()) {
            _nonSIJunit.add(new JEVisUnitImp(unit));
        }

        return _nonSIJunit;
    }

    public List<Unit> getNonSIUnits() {
        if (nonSI != null) {
            return nonSI;
        }
        nonSI = new ArrayList<>();
        nonSI.add(javax.measure.unit.NonSI.ANGSTROM);
        nonSI.add(javax.measure.unit.NonSI.ARE);
        nonSI.add(javax.measure.unit.NonSI.ASTRONOMICAL_UNIT);
        nonSI.add(javax.measure.unit.NonSI.ATMOSPHERE);
        nonSI.add(javax.measure.unit.NonSI.ATOM);
        nonSI.add(javax.measure.unit.NonSI.ATOMIC_MASS);
        nonSI.add(javax.measure.unit.NonSI.BAR);
        nonSI.add(javax.measure.unit.NonSI.BYTE);
        nonSI.add(javax.measure.unit.NonSI.C);
        nonSI.add(javax.measure.unit.NonSI.CENTIRADIAN);
        nonSI.add(javax.measure.unit.NonSI.COMPUTER_POINT);
        nonSI.add(javax.measure.unit.NonSI.CUBIC_INCH);
        nonSI.add(javax.measure.unit.NonSI.CURIE);
        nonSI.add(javax.measure.unit.NonSI.DAY);
        nonSI.add(javax.measure.unit.NonSI.DAY_SIDEREAL);
        nonSI.add(javax.measure.unit.NonSI.DECIBEL);
        nonSI.add(javax.measure.unit.NonSI.DEGREE_ANGLE);
        nonSI.add(javax.measure.unit.NonSI.DYNE);
        nonSI.add(javax.measure.unit.NonSI.E);
        nonSI.add(javax.measure.unit.NonSI.ELECTRON_MASS);
        nonSI.add(javax.measure.unit.NonSI.ELECTRON_VOLT);
        nonSI.add(javax.measure.unit.NonSI.ERG);
        nonSI.add(javax.measure.unit.NonSI.FAHRENHEIT);
        nonSI.add(javax.measure.unit.NonSI.FARADAY);
        nonSI.add(javax.measure.unit.NonSI.FOOT);
        nonSI.add(javax.measure.unit.NonSI.FOOT_SURVEY_US);
        nonSI.add(javax.measure.unit.NonSI.FRANKLIN);
        nonSI.add(javax.measure.unit.NonSI.G);
        nonSI.add(javax.measure.unit.NonSI.GALLON_DRY_US);
        nonSI.add(javax.measure.unit.NonSI.GALLON_LIQUID_US);
        nonSI.add(javax.measure.unit.NonSI.GALLON_UK);
        nonSI.add(javax.measure.unit.NonSI.GAUSS);
        nonSI.add(javax.measure.unit.NonSI.GILBERT);
        nonSI.add(javax.measure.unit.NonSI.GRADE);
        nonSI.add(javax.measure.unit.NonSI.HECTARE);
        nonSI.add(javax.measure.unit.NonSI.HORSEPOWER);
        nonSI.add(javax.measure.unit.NonSI.HOUR);
        nonSI.add(javax.measure.unit.NonSI.INCH);
        nonSI.add(javax.measure.unit.NonSI.INCH_OF_MERCURY);
        nonSI.add(javax.measure.unit.NonSI.KILOGRAM_FORCE);
        nonSI.add(javax.measure.unit.NonSI.KILOMETERS_PER_HOUR);
        nonSI.add(javax.measure.unit.NonSI.KNOT);
        nonSI.add(javax.measure.unit.NonSI.LAMBERT);
        nonSI.add(javax.measure.unit.NonSI.LIGHT_YEAR);
        nonSI.add(javax.measure.unit.NonSI.LITER);
        nonSI.add(javax.measure.unit.NonSI.LITRE);
        nonSI.add(javax.measure.unit.NonSI.MACH);
        nonSI.add(javax.measure.unit.NonSI.MAXWELL);
        nonSI.add(javax.measure.unit.NonSI.METRIC_TON);
        nonSI.add(javax.measure.unit.NonSI.MILE);
        nonSI.add(javax.measure.unit.NonSI.MILES_PER_HOUR);
        nonSI.add(javax.measure.unit.NonSI.MILLIMETER_OF_MERCURY);
        nonSI.add(javax.measure.unit.NonSI.MINUTE);
        nonSI.add(javax.measure.unit.NonSI.MINUTE_ANGLE);
        nonSI.add(javax.measure.unit.NonSI.MONTH);
        nonSI.add(javax.measure.unit.NonSI.NAUTICAL_MILE);
        nonSI.add(javax.measure.unit.NonSI.OCTET);
        nonSI.add(javax.measure.unit.NonSI.OUNCE);
        nonSI.add(javax.measure.unit.NonSI.OUNCE_LIQUID_UK);
        nonSI.add(javax.measure.unit.NonSI.OUNCE_LIQUID_US);
        nonSI.add(javax.measure.unit.NonSI.PARSEC);
        nonSI.add(javax.measure.unit.NonSI.PERCENT);
        nonSI.add(javax.measure.unit.NonSI.PIXEL);
        nonSI.add(javax.measure.unit.NonSI.POINT);
        nonSI.add(javax.measure.unit.NonSI.POISE);
        nonSI.add(javax.measure.unit.NonSI.POUND);
        nonSI.add(javax.measure.unit.NonSI.POUND_FORCE);
        nonSI.add(javax.measure.unit.NonSI.RAD);
        nonSI.add(javax.measure.unit.NonSI.RANKINE);
        nonSI.add(javax.measure.unit.NonSI.REM);
        nonSI.add(javax.measure.unit.NonSI.REVOLUTION);
        nonSI.add(javax.measure.unit.NonSI.ROENTGEN);
        nonSI.add(javax.measure.unit.NonSI.RUTHERFORD);
        nonSI.add(javax.measure.unit.NonSI.SECOND_ANGLE);
        nonSI.add(javax.measure.unit.NonSI.SPHERE);
        nonSI.add(javax.measure.unit.NonSI.STOKE);
        nonSI.add(javax.measure.unit.NonSI.TON_UK);
        nonSI.add(javax.measure.unit.NonSI.TON_US);
        nonSI.add(javax.measure.unit.NonSI.WEEK);
        nonSI.add(javax.measure.unit.NonSI.YARD);
        nonSI.add(javax.measure.unit.NonSI.YEAR);
        nonSI.add(javax.measure.unit.NonSI.YEAR_CALENDAR);
        nonSI.add(javax.measure.unit.NonSI.YEAR_SIDEREAL);

        return nonSI;
    }

    public String formate(JEVisUnit junit) {
        String uString = junit.getFormula().replace("·", "");
        uString = uString.replace("(", "");
        uString = uString.replace(")", "");
        uString = uString.replace("/", "");
        String withPrefix = getPrefixChar(junit.getPrefix()) + uString;
        return withPrefix;
    }

    public String formate(Unit unit) {
        return formate(unit, "");
    }

    public String formate(Unit unit, String altSymbol) {
//        System.out.println("Formate unit: " + unit + "  AltUnit: " + altSymbol);
//        String u1 = unit.getStandardUnit().toString().replace("·", "");
        String u1 = unit.toString().replace("·", "");
        u1 = u1.replace("(", "");
        u1 = u1.replace(")", "");
        u1 = u1.replace("/", "");

        return u1;

    }

    /**
     * @return
     * @TODO: this list comes from the WebServices
     */
    public List<Unit> getAdditonalUnits() {
        if (additonalUnits != null) {
            return additonalUnits;
        }
        additonalUnits = new ArrayList<>();

        additonalUnits.add(Unit.valueOf("Hz"));
        additonalUnits.add(NonSI.REVOLUTION.divide(SI.SECOND));
        additonalUnits.add(SI.WATT.times(SI.SECOND));
        additonalUnits.add(SI.WATT.times(NonSI.HOUR));

        additonalUnits.add(Money.BASE_UNIT.alternate("€"));
        additonalUnits.add(Money.BASE_UNIT.alternate("$"));
        additonalUnits.add(Money.BASE_UNIT.alternate("£"));
        additonalUnits.add(Money.BASE_UNIT.alternate("¥"));
        additonalUnits.add(Money.BASE_UNIT.alternate("₦"));
        additonalUnits.add(Money.BASE_UNIT.alternate("₹"));

        additonalUnits.add(SI.WATT.alternate("var"));
        additonalUnits.add(SI.WATT.divide(SI.SQUARE_METRE));
        additonalUnits.add(SI.METER.divide(SI.SECOND));

        additonalUnits.add(Dimensionless.UNIT.alternate("Hits").divide(SI.METER.pow(2)));
        additonalUnits.add(Unit.ONE.alternate("Hits").divide(SI.CENTIMETER.pow(2)));
        additonalUnits.add(SI.OHM.divide(SI.CENTIMETER.pow(2)));
        additonalUnits.add(SI.CENTIMETER.pow(2));
        additonalUnits.add(SI.KILOMETER.pow(2));

        additonalUnits.add(Unit.ONE.times(1E-6));//ppm

        additonalUnits.add(SI.KILO(SI.WATT).times(NonSI.HOUR.times(SI.SECOND).times(Unit.ONE)));

        //kWh/m²kWh/m²
        additonalUnits.add(((SI.KILO(SI.WATT).times(NonSI.HOUR)).divide(SI.SQUARE_METRE)));

        //Norm cubic metre
        additonalUnits.add(SI.CUBIC_METRE.alternate("Nm³"));


        try {
            additonalUnits.add(SI.WATT.alternate("va"));
            /**
             * Workaround, should be Watt.time(hour) but this somehow does not work with .alternativ
             */
            //additonalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("ws"));
            additonalUnits.add(Unit.ONE.alternate("vah"));
            additonalUnits.add(Unit.ONE.alternate("cal"));
            additonalUnits.add(Unit.ONE.alternate("vahr"));

//            additonalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("ws"));
//            System.out.println("1: " + SI.WATT.times(NonSI.HOUR).alternate("vahr"));
//            additonalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("cal"));
//            System.out.println("1: " + SI.WATT.times(NonSI.HOUR).alternate("cal"));
//
//            additonalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("vah"));
//            System.out.println("4: " + SI.WATT.times(NonSI.HOUR).alternate("vah"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }


//        additonalUnits.add(Dimensionless.UNIT.alternate("Status"));
        return additonalUnits;
    }

    public List<JEVisUnit> getCustomUnits() {
        List<JEVisUnit> customUnits = new ArrayList<>();
        for (JEVisUnit unit : getAdditonalJEVisUnits()) {
            boolean hasQuantitiy = false;
            for (JEVisUnit quantity : getQuantitiesJunit()) {
                if (quantity.isCompatible(unit)) {
                    hasQuantitiy = true;
                }
            }
            if (!hasQuantitiy) {
//                try {
//                    System.out.println("Cutom unit has NO Qulity: " + formate(unit));
//                } catch (JEVisException ex) {
//                    Logger.getLogger(UnitManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
                customUnits.add(unit);
            } else {
//                try {
//                    System.out.println("Cutom unit has Qulity: " + formate(unit));
//                } catch (JEVisException ex) {
//                    Logger.getLogger(UnitManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }

        return customUnits;
    }

    public List<JEVisUnit> getAdditonalJEVisUnits() {
//        if (additonalUnits != null) {
//            return additonalUnits;
//        }
        List<JEVisUnit> units = new ArrayList<>();

        for (Unit u : getAdditonalUnits()) {
            units.add(new JEVisUnitImp(u));
        }
        return units;

    }

    public List<Unit> getSIUnits() {
        if (si != null) {
            return si;
        }
        si = new ArrayList<>();
        si.add(javax.measure.unit.SI.AMPERE);
        si.add(javax.measure.unit.SI.BECQUEREL);
        si.add(javax.measure.unit.SI.BIT);
        si.add(javax.measure.unit.SI.CANDELA);
        si.add(javax.measure.unit.SI.CELSIUS);
        si.add(javax.measure.unit.SI.CENTIMETER);
        si.add(javax.measure.unit.SI.CENTIMETRE);
        si.add(javax.measure.unit.SI.COULOMB);
        si.add(javax.measure.unit.SI.CUBIC_METRE);
        si.add(javax.measure.unit.SI.FARAD);
        si.add(javax.measure.unit.SI.GRAM);
        si.add(javax.measure.unit.SI.GRAY);
        si.add(javax.measure.unit.SI.HENRY);
        si.add(javax.measure.unit.SI.HERTZ);
        si.add(javax.measure.unit.SI.JOULE);
        si.add(javax.measure.unit.SI.KATAL);
        si.add(javax.measure.unit.SI.KELVIN);
        si.add(javax.measure.unit.SI.KILOGRAM);
        si.add(javax.measure.unit.SI.KILOMETER);
        si.add(javax.measure.unit.SI.LUMEN);
        si.add(javax.measure.unit.SI.LUX);
        si.add(javax.measure.unit.SI.METER);
        si.add(javax.measure.unit.SI.METERS_PER_SECOND);
        si.add(javax.measure.unit.SI.METERS_PER_SQUARE_SECOND);
        si.add(javax.measure.unit.SI.METRE);
        si.add(javax.measure.unit.SI.METRES_PER_SECOND);
        si.add(javax.measure.unit.SI.METRES_PER_SQUARE_SECOND);
        si.add(javax.measure.unit.SI.MILLIMETER);
        si.add(javax.measure.unit.SI.MILLIMETRE);
        si.add(javax.measure.unit.SI.MOLE);
        si.add(javax.measure.unit.SI.NEWTON);
        si.add(javax.measure.unit.SI.OHM);
        si.add(javax.measure.unit.SI.PASCAL);
        si.add(javax.measure.unit.SI.RADIAN);
        si.add(javax.measure.unit.SI.SECOND);
        si.add(javax.measure.unit.SI.SIEMENS);
        si.add(javax.measure.unit.SI.SIEVERT);
        si.add(javax.measure.unit.SI.SQUARE_METRE);
        si.add(javax.measure.unit.SI.STERADIAN);
        si.add(javax.measure.unit.SI.TESLA);
        si.add(javax.measure.unit.SI.VOLT);
        si.add(javax.measure.unit.SI.WATT);
        si.add(javax.measure.unit.SI.WEBER);
        si.add(javax.measure.unit.SI.METRE);

        return si;
    }

    public List<JEVisUnit> getSIJEVisUnits() {
        if (_siJunit != null) {
            return _siJunit;
        }
        _siJunit = new ArrayList<>();

        for (Unit unit : getSIUnits()) {
            _siJunit.add(new JEVisUnitImp(unit));
        }

        return _siJunit;
    }

    public HashMap<JEVisUnit, String> getNameMapQuantities(Locale local) {
        if (_dimNamesJUnit != null) {
            return _dimNamesJUnit;
        }
        _dimNamesJUnit = new HashMap<>();

        for (Map.Entry<Unit, String> entry : getNameMapQuantities().entrySet()) {
            Unit unit = entry.getKey();
            String string = entry.getValue();

            _dimNamesJUnit.put(new JEVisUnitImp(unit), string);
        }

        return _dimNamesJUnit;
    }

    public HashMap<Unit, String> getNameMapQuantities() {
        if (dimNames != null) {
            return dimNames;
        }
        dimNames = new HashMap<>();

        dimNames.put(Money.BASE_UNIT, "Currency");

        dimNames.put(Acceleration.UNIT, "Acceleration");
        dimNames.put(AngularVelocity.UNIT, "Angular Velocity");
        dimNames.put(AmountOfSubstance.UNIT, "Amount Of Substance");
        dimNames.put(Angle.UNIT, "Angle");
        dimNames.put(AngularAcceleration.UNIT, "Angular Acceleration");
        dimNames.put(Area.UNIT, "Area");
        dimNames.put(CatalyticActivity.UNIT, "Catalytic Activity");
        dimNames.put(DataAmount.UNIT, "Data Amount");
        dimNames.put(DataRate.UNIT, "Data Rate");
        dimNames.put(Dimensionless.UNIT, "Dimensionless");
        dimNames.put(Duration.UNIT, "Duration");
        dimNames.put(DynamicViscosity.UNIT, "DynamicViscosity");
        dimNames.put(ElectricCapacitance.UNIT, "Electric Capacitance");
        dimNames.put(ElectricConductance.UNIT, "Electric Conductance");
        dimNames.put(ElectricCharge.UNIT, "Electric Charge");
        dimNames.put(ElectricCurrent.UNIT, "Electric Current");
        dimNames.put(ElectricInductance.UNIT, "Electric Inductance");
        dimNames.put(ElectricPotential.UNIT, "Electric Potential");
        dimNames.put(ElectricResistance.UNIT, "Electric Resistance");
        dimNames.put(Energy.UNIT, "Energy");
        dimNames.put(Force.UNIT, "Force");
        dimNames.put(Frequency.UNIT, "Frequency");
        dimNames.put(Illuminance.UNIT, "Illuminance");
        dimNames.put(KinematicViscosity.UNIT, "Kinematic Viscosity");
        dimNames.put(Length.UNIT, "Length");
        dimNames.put(LuminousFlux.UNIT, "Luminous Flux");
        dimNames.put(LuminousIntensity.UNIT, "Luminous Intensity");
        dimNames.put(Mass.UNIT, "Mass");
        dimNames.put(MagneticFlux.UNIT, "Magnetic Flux");
        dimNames.put(MagneticFluxDensity.UNIT, "Magnetic Flux Density");
        dimNames.put(MassFlowRate.UNIT, "Mass Flow Rate");
        dimNames.put(Power.UNIT, "Power");
        dimNames.put(Pressure.UNIT, "Pressure");
        dimNames.put(RadiationDoseAbsorbed.UNIT, "Radiation Dose Absorbed");
        dimNames.put(RadiationDoseEffective.UNIT, "Radiation Dose Effective");
        dimNames.put(RadioactiveActivity.UNIT, "Radioactive Activity");
        dimNames.put(SolidAngle.UNIT, "Solid Angle");
        dimNames.put(Temperature.UNIT, "Temperature");
        dimNames.put(Torque.UNIT, "Torque");
        dimNames.put(Velocity.UNIT, "Velocity");
        dimNames.put(Volume.UNIT, "Volume");
        dimNames.put(VolumetricDensity.UNIT, "Volumetric Density");
        dimNames.put(VolumetricFlowRate.UNIT, "Volumetric Flow Rate");

        return dimNames;
    }

    public HashMap<JEVisUnit, String> getNameMap(Locale locale) {
        if (_namesJUnit != null) {
            return _namesJUnit;
        }
        _namesJUnit = new HashMap<>();

        for (Map.Entry<Unit, String> entry : getNameMap().entrySet()) {
            Unit unit = entry.getKey();
            String string = entry.getValue();

            _namesJUnit.put(new JEVisUnitImp(unit), string);

        }

        return _namesJUnit;
    }

    public HashMap<Unit, String> getNameMap() {
        if (names != null) {
            return names;
        }
        names = new HashMap<>();

        names.put(SI.AMPERE, "Ampere");
        names.put(SI.BECQUEREL, "Becquerel");
        names.put(SI.BIT, "Bit");
        names.put(SI.CANDELA, "Candela");
        names.put(SI.CELSIUS, "Celsius");
        names.put(SI.CENTIMETER, "Centimeter");
        names.put(SI.CENTIMETRE, "Centimetre");
        names.put(SI.COULOMB, "Coulomb");
        names.put(SI.CUBIC_METRE, "Cubic Metre");
        names.put(SI.FARAD, "Farad");
        names.put(SI.GRAM, "Gram");
        names.put(SI.GRAY, "Gray");
        names.put(SI.HENRY, "Henry");
        names.put(SI.HERTZ, "Hertz");
        names.put(SI.JOULE, "Joule");
        names.put(SI.KATAL, "Katal");
        names.put(SI.KELVIN, "Kelvin");
        names.put(SI.KILOGRAM, "Kilogram");
        names.put(SI.KILOMETER, "Kilometer");
        names.put(SI.LUMEN, "Lumen");
        names.put(SI.LUX, "Lux");
        names.put(SI.METER, "Meter");
        names.put(SI.METERS_PER_SECOND, "Meters Per Second");
        names.put(SI.METERS_PER_SQUARE_SECOND, "Meters Per Square Second");
        names.put(SI.MILLIMETRE, "Millimetre");
        names.put(SI.MOLE, "Mole");
        names.put(SI.NEWTON, "Newton");
        names.put(SI.OHM, "Ohm");
        names.put(SI.PASCAL, "Pascal");
        names.put(SI.RADIAN, "Radian");
        names.put(SI.SECOND, "Second");
        names.put(SI.SIEMENS, "Siemens");
        names.put(SI.SIEVERT, "Sievert");
        names.put(SI.SQUARE_METRE, "Square Metre");
        names.put(SI.STERADIAN, "Steradian");
        names.put(SI.TESLA, "Tesla");
        names.put(SI.VOLT, "Volt");
        names.put(SI.WATT, "Watt");
        names.put(SI.WEBER, "Weber");
        //---- NON SI
        names.put(NonSI.ANGSTROM, "Angstrom");
        names.put(NonSI.ARE, "Are");
        names.put(NonSI.ASTRONOMICAL_UNIT, "Astronomical Unit");
        names.put(NonSI.ATMOSPHERE, "Atmosphere");
        names.put(NonSI.ATOM, "Atom");
        names.put(NonSI.ATOMIC_MASS, "Atomic Mass");
        names.put(NonSI.BAR, "Bar");
        names.put(NonSI.BYTE, "Byte");
        names.put(NonSI.C, "C");
        names.put(NonSI.CENTIRADIAN, "Centiradian");
        names.put(NonSI.COMPUTER_POINT, "Computer Point");
        names.put(NonSI.CUBIC_INCH, "Cubic_Inch");
        names.put(NonSI.CURIE, "Curie");
        names.put(NonSI.DAY, "Day");
        names.put(NonSI.DAY_SIDEREAL, "Day_Sidereal");
        names.put(NonSI.DECIBEL, "Decibel");
        names.put(NonSI.DEGREE_ANGLE, "Degree Angle");
        names.put(NonSI.DYNE, "Dyne");
        names.put(NonSI.E, "E");
        names.put(NonSI.ELECTRON_MASS, "Electron Mass");
        names.put(NonSI.ELECTRON_VOLT, "Electron Volt");
        names.put(NonSI.ERG, "Erg");
        names.put(NonSI.FAHRENHEIT, "Fahrenheit");
        names.put(NonSI.FARADAY, "Faraday");
        names.put(NonSI.FOOT, "Foot");
        names.put(NonSI.FOOT_SURVEY_US, "Foot Survey Us");
        names.put(NonSI.FRANKLIN, "Franklin");
        names.put(NonSI.G, "G");
        names.put(NonSI.GALLON_DRY_US, "Gallon Dry Us");
        names.put(NonSI.GALLON_LIQUID_US, "Gallon Liquid US");
        names.put(NonSI.GALLON_UK, "Gallon UK");
        names.put(NonSI.GAUSS, "Gauss");
        names.put(NonSI.GILBERT, "Gilbert");
        names.put(NonSI.GRADE, "Grade");
        names.put(NonSI.HECTARE, "Hectare");
        names.put(NonSI.HORSEPOWER, "Horsepower");
        names.put(NonSI.HOUR, "Hour");
        names.put(NonSI.INCH, "Inch");
        names.put(NonSI.INCH_OF_MERCURY, "Inch Of Mercury");
        names.put(NonSI.KILOGRAM_FORCE, "Kilogram Force");
        names.put(NonSI.KILOMETERS_PER_HOUR, "Kilometers Per Hour");
        names.put(NonSI.KNOT, "Knot");
        names.put(NonSI.LAMBERT, "Lambert");
        names.put(NonSI.LIGHT_YEAR, "Light Year");
        names.put(NonSI.LITER, "Liter");
        names.put(NonSI.LITRE, "Litre");
        names.put(NonSI.MACH, "Mach");
        names.put(NonSI.MAXWELL, "Maxwell");
        names.put(NonSI.METRIC_TON, "Metric Ton");
        names.put(NonSI.MILE, "Mile");
        names.put(NonSI.MILES_PER_HOUR, "Miles Per Hour");
        names.put(NonSI.MILLIMETER_OF_MERCURY, "Millimeter Of Mercury");
        names.put(NonSI.MINUTE, "Minute");
        names.put(NonSI.MINUTE_ANGLE, "Minute Angle");
        names.put(NonSI.MONTH, "Month");
        names.put(NonSI.NAUTICAL_MILE, "Nautical Mile");
        names.put(NonSI.OCTET, "Octet");
        names.put(NonSI.OUNCE, "Ounce");
        names.put(NonSI.OUNCE_LIQUID_UK, "Ounce Liquid UK");
        names.put(NonSI.PARSEC, "Parsec");
        names.put(NonSI.PERCENT, "Percent");
        names.put(NonSI.PIXEL, "Pixel");
        names.put(NonSI.POINT, "Point");
        names.put(NonSI.POISE, "Poise");
        names.put(NonSI.POUND, "Pound");
        names.put(NonSI.POUND_FORCE, "Pound_Force");
        names.put(NonSI.RAD, "Rad");
        names.put(NonSI.RANKINE, "Rankine");
        names.put(NonSI.REM, "Rem");
        names.put(NonSI.REVOLUTION, "Revolution");
        names.put(NonSI.ROENTGEN, "Roentgen");
        names.put(NonSI.RUTHERFORD, "Rutherford");
        names.put(NonSI.SECOND_ANGLE, "Second Angle");
        names.put(NonSI.SPHERE, "Sphere");
        names.put(NonSI.STOKE, "Stoke");
        names.put(NonSI.TON_UK, "Ton UK");
        names.put(NonSI.TON_US, "Ton US");
        names.put(NonSI.WEEK, "Week");
        names.put(NonSI.YARD, "Yard");
        names.put(NonSI.YEAR, "Year");
        names.put(NonSI.YEAR_CALENDAR, "Year Calendar");
        names.put(NonSI.YEAR_SIDEREAL, "Year Sidereal");
        //Prefix

        names.put(SI.ZETTA(Unit.ONE), "Zetta");
        names.put(SI.EXA(Unit.ONE), "Exa");
        names.put(SI.PETA(Unit.ONE), "Peta");
        names.put(SI.TERA(Unit.ONE), "Tera");
        names.put(SI.GIGA(Unit.ONE), "Giga");
        names.put(SI.MEGA(Unit.ONE), "Mega");
        names.put(SI.KILO(Unit.ONE), "Kilo");
        names.put(SI.HECTO(Unit.ONE), "Hecto");
        names.put(SI.DEKA(Unit.ONE), "Deka");
        names.put(SI.DECI(Unit.ONE), "Deci");
        names.put(SI.CENTI(Unit.ONE), "Centi");
        names.put(SI.MILLI(Unit.ONE), "Milli");
        names.put(SI.MICRO(Unit.ONE), "Micro");
        names.put(SI.NANO(Unit.ONE), "Nano");
        names.put(SI.PICO(Unit.ONE), "Pico");
        names.put(SI.FEMTO(Unit.ONE), "Femto");
        names.put(SI.ATTO(Unit.ONE), "Atto");
        names.put(SI.ZEPTO(Unit.ONE), "Zepto");
        names.put(SI.ATTO(Unit.ONE), "Atto");
        names.put(SI.YOCTO(Unit.ONE), "Yocto");

        //money does not work with the rest of the system. The API will store € but cannot parse it again....
        // we have to use Currency + alt symbol :(
        names.put(Money.BASE_UNIT.alternate("€"), "Euro");
        names.put(Money.BASE_UNIT.alternate("£"), "Pound");
        names.put(Money.BASE_UNIT.alternate("$"), "US-Dollar");
        names.put(Money.BASE_UNIT.alternate("£"), "Yen");
        names.put(Money.BASE_UNIT.alternate("¥"), "Yuan");
        names.put(Money.BASE_UNIT.alternate("₦"), "Naira");
        names.put(Money.BASE_UNIT.alternate("元"), "Renminbi");
        names.put(Money.BASE_UNIT.alternate("₹"), "Rupee");
        ///--additonal

//        name.put
        return names;
    }

    /**
     * Ask the JEADPI localization feature for the correct name of the unit.
     * Unit is identyfiyed by its symbole(this should be unitq?)
     * <p>
     * TODO: The functionality is hardcoded in the moment....
     *
     * @param unit
     * @param locale
     * @return
     */
    public String getUnitName(Unit unit, Locale locale) {
        String name = getNameMap().get(unit);
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return unit.toString();
        }

    }

    public String getUnitName(JEVisUnit unit, Locale locale) {
        String name = getNameMap(locale).get(unit);
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return unit.toString();
        }

    }

    public String getQuantitiesName(JEVisUnit unit, Locale locale) {

        String name = getNameMapQuantities(locale).get(unit);
        if (name != null && !name.isEmpty()) {
//            System.out.println(String.format(" get name for: %s [%s] = %s", unit.toString(), unit.getStandardUnit().toString(), name));
            return name;
        } else {
            return "Dimensionless";
        }

    }

    public String getQuantitiesName(Unit unit, Locale locale) {

        String name = getNameMapQuantities().get(unit.getStandardUnit());
        if (name != null && !name.isEmpty()) {
//            System.out.println(String.format(" get name for: %s [%s] = %s", unit.toString(), unit.getStandardUnit().toString(), name));
            return name;
        } else {
            return "Dimensionless";
        }

    }

    public List<JEVisUnit> getCompatibleNonSIUnit(JEVisUnit unit) {
        List<JEVisUnit> units = new ArrayList<>();

        for (JEVisUnit other : getNonSIJEVisUnits()) {
            if (unit.isCompatible(other)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleNonSIUnit(Unit unit) {
        List<Unit> units = new ArrayList<Unit>();

        for (Unit other : getNonSIUnits()) {
            if (unit.isCompatible(other)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleQuantityUnit(Unit unit) {
        List<Unit> units = new ArrayList<Unit>();

        for (Unit other : getQuantities()) {
            if (unit.isCompatible(other)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<JEVisUnit> getCompatibleSIUnit(JEVisUnit unit) {
        List<JEVisUnit> units = new ArrayList<>();

        for (JEVisUnit other : getSIJEVisUnits()) {
            if (unit.isCompatible(other) && !other.equals(unit)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<JEVisUnit> getCompatibleAdditionalUnit(JEVisUnit unit) {
        List<JEVisUnit> units = new ArrayList<>();

        for (JEVisUnit other : getAdditonalJEVisUnits()) {
//            System.out.print(other + " ? ...");
            if (unit.isCompatible(other) && !other.equals(unit)) {
//                System.out.println("is");
                units.add(other);
            }
//            System.out.println("NOT");
        }

        return units;
    }

    public List<Unit> getCompatibleSIUnit(Unit unit) {
        List<Unit> units = new ArrayList<Unit>();

        for (Unit other : getSIUnits()) {
            if (unit.isCompatible(other) && !other.equals(unit)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleAdditionalUnit(Unit unit) {
        List<Unit> units = new ArrayList<Unit>();
//        System.out.println("Found add units for: " + unit);

        for (Unit other : getAdditonalUnits()) {
//            System.out.print(other + " ? ...");
            if (unit.getStandardUnit().isCompatible(other) && !other.equals(unit)) {
//                System.out.println("is");
                units.add(other);
            }
//            System.out.println("NOT");
        }

        return units;
    }

    public List<Unit> getFavoriteQuantitys() {
        List<Unit> list = new ArrayList<Unit>();

        list.add(Dimensionless.UNIT);
        list.add(Energy.UNIT);
        list.add(Power.UNIT);
        list.add(Temperature.UNIT);
        list.add(Volume.UNIT);
        list.add(Area.UNIT);

        return list;
    }

    public interface PrefixName {

        //TODO: remove this
        String ZETTA = "Zetta";
        String EXA = "Exa";
        String PETA = "Peta";
        String TERA = "Tera";
        String GIGA = "Giga";
        String MEGA = "Mega";
        String KILO = "Kilo";
        String HECTO = "Hecto";
        String DEKA = "Deka";
        String DECI = "Deci";
        String CENTI = "Centi";
        String MILLI = "Milli";
        String MICRO = "Micro";
        String NANO = "Nano";
        String PICO = "Pico";
        String FEMTO = "Femto";
        String ATTO = "Atto";
        String ZEPTO = "Zepto";
        String YOCTO = "Yocto";
        String NONE = "None";

    }

}
