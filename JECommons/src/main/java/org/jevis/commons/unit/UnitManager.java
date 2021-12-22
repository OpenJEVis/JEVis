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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;

import javax.measure.MetricPrefix;
import javax.measure.Prefix;
import javax.measure.quantity.*;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.*;

import static javax.measure.MetricPrefix.*;

/**
 * This Class helps with the handling of JScince Lib. This class in not final an
 * may be chaned in the future.
 *
 * @author fs
 */
public class UnitManager {
    private static final Logger logger = LogManager.getLogger(UnitManager.class);

    private final static UnitManager unitManager = new UnitManager();
    private List<Unit<Quantity>> prefixes;
    private List<String> prefixes2;
    private List<Unit> quantities;
    private List<JEVisUnit> _quantitiesJunit;
    private List<Unit> nonSI;
    private List<JEVisUnit> _nonSIJunit;
    private List<Unit> si;
    private List<JEVisUnit> _siJunit;
    private List<Unit> additionalUnits;
    private HashMap<Unit, String> names;
    private HashMap<JEVisUnit, String> _namesJUnit;
    private HashMap<Unit, String> dimNames;
    private HashMap<JEVisUnit, String> _dimNamesJUnit;
    private List<JEVisUnit> favUnits;

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private UnitManager() {
    }

    public static JEVisUnit cloneUnit(JEVisUnit unit) {

        JEVisUnit clone = new JEVisUnitImp();
        clone.setFormula(unit.getFormula());
        clone.setLabel(unit.getLabel());
        clone.setPrefix(unit.getPrefix());
        return clone;
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
     * Returns the long name of the prefix for the given locale
     *
     * @param prefix
     * @return
     */
    public Prefix getPrefix(JEVisUnit.Prefix prefix) {
        switch (prefix) {
            case ATTO:
                return ATTO;
            case CENTI:
                return CENTI;
            case DECI:
                return DECI;
            case DEKA:
                return DEKA;
            case EXA:
                return EXA;
            case FEMTO:
                return FEMTO;
            case GIGA:
                return GIGA;
            case HECTO:
                return HECTO;
            case KILO:
                return KILO;
            case MEGA:
                return MEGA;
            case MICRO:
                return MICRO;
            case MILLI:
                return MILLI;
            case NANO:
                return NANO;
            case PETA:
                return PETA;
            case PICO:
                return PICO;
            case TERA:
                return TERA;
            case YOCTO:
                return YOCTO;
            case ZEPTO:
                return ZEPTO;
            case ZETTA:
                return ZETTA;
            case NONE:
            default:
                return CustomPrefix.NONE;
        }
    }

    /**
     * Returns the long name of the prefix for the given locale
     *
     * @param prefix
     * @return
     */
    public Prefix getPrefix(String prefix) {
        prefix = prefix.toUpperCase();
        switch (prefix) {
            case "ATTO":
                return ATTO;
            case "CENTI":
                return CENTI;
            case "DECI":
                return DECI;
            case "DEKA":
                return DEKA;
            case "EXA":
                return EXA;
            case "FEMTO":
                return FEMTO;
            case "GIGA":
                return GIGA;
            case "HECTO":
                return HECTO;
            case "KILO":
                return KILO;
            case "MEGA":
                return MEGA;
            case "MICRO":
                return MICRO;
            case "MILLI":
                return MILLI;
            case "NANO":
                return NANO;
            case "PETA":
                return PETA;
            case "PICO":
                return PICO;
            case "TERA":
                return TERA;
            case "YOCTO":
                return YOCTO;
            case "ZEPTO":
                return ZEPTO;
            case "ZETTA":
                return ZETTA;
            case "NONE":
            case "":
            default:
                return CustomPrefix.NONE;
        }
    }

    public JEVisUnit.Prefix getPrefix(Prefix prefix) {
        if (prefix != null) {
            if (prefix instanceof MetricPrefix) {
                final MetricPrefix p = (MetricPrefix) prefix;

                switch (p) {
                    case ATTO:
                        return JEVisUnit.Prefix.ATTO;
                    case CENTI:
                        return JEVisUnit.Prefix.CENTI;
                    case DECI:
                        return JEVisUnit.Prefix.DECI;
                    case DEKA:
                        return JEVisUnit.Prefix.DEKA;
                    case EXA:
                        return JEVisUnit.Prefix.EXA;
                    case FEMTO:
                        return JEVisUnit.Prefix.FEMTO;
                    case GIGA:
                        return JEVisUnit.Prefix.GIGA;
                    case HECTO:
                        return JEVisUnit.Prefix.HECTO;
                    case KILO:
                        return JEVisUnit.Prefix.KILO;
                    case MEGA:
                        return JEVisUnit.Prefix.MEGA;
                    case MICRO:
                        return JEVisUnit.Prefix.MICRO;
                    case MILLI:
                        return JEVisUnit.Prefix.MILLI;
                    case NANO:
                        return JEVisUnit.Prefix.NANO;
                    case PETA:
                        return JEVisUnit.Prefix.PETA;
                    case PICO:
                        return JEVisUnit.Prefix.PICO;
                    case TERA:
                        return JEVisUnit.Prefix.TERA;
                    case YOCTO:
                        return JEVisUnit.Prefix.YOCTO;
                    case ZEPTO:
                        return JEVisUnit.Prefix.ZEPTO;
                    case ZETTA:
                        return JEVisUnit.Prefix.ZETTA;
                    case YOTTA:
                        return JEVisUnit.Prefix.YOTTA;
                }
            } else if (prefix instanceof CustomPrefix) {
                final CustomPrefix p = (CustomPrefix) prefix;

                if (p == CustomPrefix.NONE) {
                    return JEVisUnit.Prefix.NONE;
                }
            }
        }
        return JEVisUnit.Prefix.NONE;
    }

    public JEVisUnit.Prefix getJEVisUnitPrefix(String prefix) {
        if (prefix != null) {
            switch (prefix) {
                case "ATTO":
                    return JEVisUnit.Prefix.ATTO;
                case "CENTI":
                    return JEVisUnit.Prefix.CENTI;
                case "DECI":
                    return JEVisUnit.Prefix.DECI;
                case "DEKA":
                    return JEVisUnit.Prefix.DEKA;
                case "EXA":
                    return JEVisUnit.Prefix.EXA;
                case "FEMTO":
                    return JEVisUnit.Prefix.FEMTO;
                case "GIGA":
                    return JEVisUnit.Prefix.GIGA;
                case "HECTO":
                    return JEVisUnit.Prefix.HECTO;
                case "KILO":
                    return JEVisUnit.Prefix.KILO;
                case "MEGA":
                    return JEVisUnit.Prefix.MEGA;
                case "MICRO":
                    return JEVisUnit.Prefix.MICRO;
                case "MILLI":
                    return JEVisUnit.Prefix.MILLI;
                case "NANO":
                    return JEVisUnit.Prefix.NANO;
                case "PETA":
                    return JEVisUnit.Prefix.PETA;
                case "PICO":
                    return JEVisUnit.Prefix.PICO;
                case "TERA":
                    return JEVisUnit.Prefix.TERA;
                case "YOCTO":
                    return JEVisUnit.Prefix.YOCTO;
                case "ZEPTO":
                    return JEVisUnit.Prefix.ZEPTO;
                case "ZETTA":
                    return JEVisUnit.Prefix.ZETTA;
                case "YOTTA":
                    return JEVisUnit.Prefix.YOTTA;
            }
        }

        return JEVisUnit.Prefix.NONE;
    }

    /**
     * Return an new Unit with the set Prefix
     *
     * @param unit
     * @param prefix
     * @return
     */
    public Unit getUnitWithPrefix(Unit unit, MetricPrefix prefix) {
        if (prefix != null) {
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
                default:
                    return unit;
            }
        } else return unit;
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
        nonSI.add(SI.WATT.alternate("va").times(NonSI.HOUR));
        nonSI.add(SI.WATT.alternate("var").times(NonSI.HOUR));
        nonSI.add(javax.measure.unit.NonSI.WEEK);
        nonSI.add(javax.measure.unit.NonSI.YARD);
        nonSI.add(javax.measure.unit.NonSI.YEAR);
        nonSI.add(javax.measure.unit.NonSI.YEAR_CALENDAR);
        nonSI.add(javax.measure.unit.NonSI.YEAR_SIDEREAL);

        return nonSI;
    }

    public String format(String uString) {
        uString = uString.replace("(", "");
        uString = uString.replace(")", "");
        uString = uString.replace("·", "");
        return uString;
    }

    public String format(JEVisUnit junit) {
        if (junit != null && junit.getLabel() != null) {
//            if (junit != null && junit.getFormula() != null) {
//            String uString = junit.getFormula();
//            uString = format(uString);
//            uString = uString.replace("(", "");
//            uString = uString.replace(")", "");
//            uString = uString.replace("·", "");
//            return getPrefixChar(junit.getPrefix()) + uString;
            return junit.getLabel().replace("·", "");
        } else {
            logger.warn("No unit for format");
            return "";
        }
    }

    public String format(Unit unit) {
        return format(unit, "");
    }

    public String format(Unit unit, String altSymbol) {
//        logger.info("Formate unit: " + unit + "  AltUnit: " + altSymbol);
//        String u1 = unit.getStandardUnit().toString().replace("·", "");
        String u1 = unit.toString();
        //.replace("·", "");
        u1 = u1.replace("(", "");
        u1 = u1.replace(")", "");
        //u1 = u1.replace("/", "");

        return u1;

    }

    /**
     * @return
     * @TODO: this list comes from the WebServices
     */
    public List<Unit> getAdditionalUnits() {
        if (additionalUnits != null) {
            return additionalUnits;
        }
        additionalUnits = new ArrayList<>();

        additionalUnits.add(Unit.valueOf("Hz"));
        additionalUnits.add(NonSI.REVOLUTION.divide(SI.SECOND));
        additionalUnits.add(SI.WATT.times(SI.SECOND));
        additionalUnits.add(SI.WATT.times(NonSI.HOUR));

        additionalUnits.add(Currency.EUR);
        additionalUnits.add(Currency.USD);
        additionalUnits.add(Currency.GBP);
        additionalUnits.add(Currency.JPY);
        additionalUnits.add(Currency.AUD);
        additionalUnits.add(Currency.CAD);
        additionalUnits.add(Currency.CNY);
        additionalUnits.add(Currency.KRW);
        additionalUnits.add(Currency.TWD);


        additionalUnits.add(SI.WATT.divide(SI.SQUARE_METRE));
        additionalUnits.add(SI.METER.divide(SI.SECOND));

        additionalUnits.add(Dimensionless.UNIT.alternate("Hits").divide(SI.METER.pow(2)));
        additionalUnits.add(Unit.ONE.alternate("Hits").divide(SI.CENTIMETER.pow(2)));
        additionalUnits.add(SI.OHM.divide(SI.CENTIMETER.pow(2)));
        additionalUnits.add(SI.CENTIMETER.pow(2));
        additionalUnits.add(SI.KILOMETER.pow(2));

        additionalUnits.add(Unit.ONE.times(1E-6));//ppm

        additionalUnits.add(SI.KILO(SI.WATT).times(NonSI.HOUR.times(SI.SECOND).times(Unit.ONE)));

        //kWh/m²kWh/m²
        additionalUnits.add(((SI.KILO(SI.WATT).times(NonSI.HOUR)).divide(SI.SQUARE_METRE)));

        //Norm cubic metre
        additionalUnits.add(SI.CUBIC_METRE.alternate("Nm³"));


        try {
            /**
             * Workaround, should be Watt.time(hour) but this somehow does not work with .alternativ
             * doesnt work properly either, use dimension
             */
            //additionalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("ws"));

//            additionalUnits.add(SI.WATT.alternate("va"));
//            additionalUnits.add(SI.WATT.alternate("var"));
//
//            additionalUnits.add(SI.JOULE.alternate("vah"));
//            additionalUnits.add(SI.JOULE.alternate("vahr"));
//
//            additionalUnits.add(SI.JOULE.alternate("cal"));

//            additionalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("ws"));
//            logger.info("1: " + SI.WATT.times(NonSI.HOUR).alternate("vahr"));
//            additionalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("cal"));
//            logger.info("1: " + SI.WATT.times(NonSI.HOUR).alternate("cal"));
//
//            additionalUnits.add(SI.WATT.times(NonSI.HOUR).alternate("vah"));
//            logger.info("4: " + SI.WATT.times(NonSI.HOUR).alternate("vah"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }


//        additionalUnits.add(Dimensionless.UNIT.alternate("Status"));
        return additionalUnits;
    }

    public List<JEVisUnit> getCustomUnits() {
        List<JEVisUnit> customUnits = new ArrayList<>();
        for (JEVisUnit unit : getAdditionalJEVisUnits()) {
            boolean hasQuantitiy = false;
            for (JEVisUnit quantity : getQuantitiesJunit()) {
                if (quantity.isCompatible(unit)) {
                    hasQuantitiy = true;
                }
            }
            if (!hasQuantitiy) {
//                try {
//                    logger.info("Cutom unit has NO Qulity: " + format(unit));
//                } catch (JEVisException ex) {
//                    Logger.getLogger(UnitManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
                customUnits.add(unit);
            } else {
//                try {
//                    logger.info("Cutom unit has Qulity: " + format(unit));
//                } catch (JEVisException ex) {
//                    Logger.getLogger(UnitManager.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }

        return customUnits;
    }

    public List<JEVisUnit> getAdditionalJEVisUnits() {
//        if (additionalUnits != null) {
//            return additionalUnits;
//        }
        List<JEVisUnit> units = new ArrayList<>();

        for (Unit u : getAdditionalUnits()) {
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
        si.add(SI.WATT.alternate("va"));
        si.add(SI.WATT.alternate("var"));
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

    public HashMap<JEVisUnit, String> getNameMapQuantities(Locale locale) {
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

        dimNames.put(Money.BASE_UNIT, I18n.getInstance().getString("units.quantities.currency"));

        dimNames.put(Acceleration.UNIT, I18n.getInstance().getString("units.quantities.acceleration"));
        dimNames.put(AngularVelocity.UNIT, I18n.getInstance().getString("units.quantities.angularvelocity"));
        dimNames.put(AmountOfSubstance.UNIT, I18n.getInstance().getString("units.quantities.amountofsubstance"));
        dimNames.put(Angle.UNIT, I18n.getInstance().getString("units.quantities.angle"));
        dimNames.put(AngularAcceleration.UNIT, I18n.getInstance().getString("units.quantities.angularacceleration"));
        dimNames.put(Area.UNIT, I18n.getInstance().getString("units.quantities.area"));
        dimNames.put(CatalyticActivity.UNIT, I18n.getInstance().getString("units.quantities.catalyticactivity"));
        dimNames.put(DataAmount.UNIT, I18n.getInstance().getString("units.quantities.dataamount"));
        dimNames.put(DataRate.UNIT, I18n.getInstance().getString("units.quantities.datarate"));
        dimNames.put(Dimensionless.UNIT, I18n.getInstance().getString("units.quantities.dimensionless"));
        dimNames.put(Duration.UNIT, I18n.getInstance().getString("units.quantities.duration"));
        dimNames.put(DynamicViscosity.UNIT, I18n.getInstance().getString("units.quantities.dynamicviscosity"));
        dimNames.put(ElectricCapacitance.UNIT, I18n.getInstance().getString("units.quantities.electriccapacitance"));
        dimNames.put(ElectricConductance.UNIT, I18n.getInstance().getString("units.quantities.electricconductance"));
        dimNames.put(ElectricCharge.UNIT, I18n.getInstance().getString("units.quantities.electriccharge"));
        dimNames.put(ElectricCurrent.UNIT, I18n.getInstance().getString("units.quantities.electriccurrent"));
        dimNames.put(ElectricInductance.UNIT, I18n.getInstance().getString("units.quantities.electricinductance"));
        dimNames.put(ElectricPotential.UNIT, I18n.getInstance().getString("units.quantities.electricpotential"));
        dimNames.put(ElectricResistance.UNIT, I18n.getInstance().getString("units.quantities.electricresistance"));
        dimNames.put(Energy.UNIT, I18n.getInstance().getString("units.quantities.energy"));
        dimNames.put(Force.UNIT, I18n.getInstance().getString("units.quantities.force"));
        dimNames.put(Frequency.UNIT, I18n.getInstance().getString("units.quantities.frequency"));
        dimNames.put(Illuminance.UNIT, I18n.getInstance().getString("units.quantities.illuminance"));
        dimNames.put(KinematicViscosity.UNIT, I18n.getInstance().getString("units.quantities.kinematicviscosity"));
        dimNames.put(Length.UNIT, I18n.getInstance().getString("units.quantities.length"));
        dimNames.put(LuminousFlux.UNIT, I18n.getInstance().getString("units.quantities.luminousflux"));
        dimNames.put(LuminousIntensity.UNIT, I18n.getInstance().getString("units.quantities.luminousintensity"));
        dimNames.put(Mass.UNIT, I18n.getInstance().getString("units.quantities.mass"));
        dimNames.put(MagneticFlux.UNIT, I18n.getInstance().getString("units.quantities.magneticflux"));
        dimNames.put(MagneticFluxDensity.UNIT, I18n.getInstance().getString("units.quantities.magneticfluxdensity"));
        dimNames.put(MassFlowRate.UNIT, I18n.getInstance().getString("units.quantities.massflowrate"));
        dimNames.put(Power.UNIT, I18n.getInstance().getString("units.quantities.power"));
        dimNames.put(Pressure.UNIT, I18n.getInstance().getString("units.quantities.pressure"));
        dimNames.put(RadiationDoseAbsorbed.UNIT, I18n.getInstance().getString("units.quantities.radiationdoseabsorbed"));
        dimNames.put(RadiationDoseEffective.UNIT, I18n.getInstance().getString("units.quantities.radiationdoseeffective"));
        dimNames.put(RadioactiveActivity.UNIT, I18n.getInstance().getString("units.quantities.radioactiveactivity"));
        dimNames.put(SolidAngle.UNIT, I18n.getInstance().getString("units.quantities.solidangle"));
        dimNames.put(Temperature.UNIT, I18n.getInstance().getString("units.quantities.temperature"));
        dimNames.put(Torque.UNIT, I18n.getInstance().getString("units.quantities.torque"));
        dimNames.put(Velocity.UNIT, I18n.getInstance().getString("units.quantities.velocity"));
        dimNames.put(Volume.UNIT, I18n.getInstance().getString("units.quantities.volume"));
        dimNames.put(VolumetricDensity.UNIT, I18n.getInstance().getString("units.quantities.volumetricdensity"));
        dimNames.put(VolumetricFlowRate.UNIT, I18n.getInstance().getString("units.quantities.volumetricflowrate"));

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

        names.put(SI.AMPERE, I18n.getInstance().getString("units.name.Ampere"));
        names.put(SI.BECQUEREL, I18n.getInstance().getString("units.name.Becquerel"));
        names.put(SI.BIT, I18n.getInstance().getString("units.name.Bit"));
        names.put(SI.CANDELA, I18n.getInstance().getString("units.name.Candela"));
        names.put(SI.CELSIUS, I18n.getInstance().getString("units.name.Celsius"));
        names.put(SI.CENTIMETER, I18n.getInstance().getString("units.name.Centimeter"));
        names.put(SI.CENTIMETRE, I18n.getInstance().getString("units.name.Centimetre"));
        names.put(SI.COULOMB, I18n.getInstance().getString("units.name.Coulomb"));
        names.put(SI.CUBIC_METRE, I18n.getInstance().getString("units.name.CubicMetre"));
        names.put(SI.FARAD, I18n.getInstance().getString("units.name.Farad"));
        names.put(SI.GRAM, I18n.getInstance().getString("units.name.Gram"));
        names.put(SI.GRAY, I18n.getInstance().getString("units.name.Gray"));
        names.put(SI.HENRY, I18n.getInstance().getString("units.name.Henry"));
        names.put(SI.HERTZ, I18n.getInstance().getString("units.name.Hertz"));
        names.put(SI.JOULE, I18n.getInstance().getString("units.name.Joule"));
        names.put(SI.KATAL, I18n.getInstance().getString("units.name.Katal"));
        names.put(SI.KELVIN, I18n.getInstance().getString("units.name.Kelvin"));
        names.put(SI.KILOGRAM, I18n.getInstance().getString("units.name.Kilogram"));
        names.put(SI.KILOMETER, I18n.getInstance().getString("units.name.Kilometer"));
        names.put(SI.LUMEN, I18n.getInstance().getString("units.name.Lumen"));
        names.put(SI.LUX, I18n.getInstance().getString("units.name.Lux"));
        names.put(SI.METER, I18n.getInstance().getString("units.name.Meter"));
        names.put(SI.METERS_PER_SECOND, I18n.getInstance().getString("units.name.MetersPerSecond"));
        names.put(SI.METERS_PER_SQUARE_SECOND, I18n.getInstance().getString("units.name.MetersPerSquareSecond"));
        names.put(SI.MILLIMETRE, I18n.getInstance().getString("units.name.Millimetre"));
        names.put(SI.MOLE, I18n.getInstance().getString("units.name.Mole"));
        names.put(SI.NEWTON, I18n.getInstance().getString("units.name.Newton"));
        names.put(SI.OHM, I18n.getInstance().getString("units.name.Ohm"));
        names.put(SI.PASCAL, I18n.getInstance().getString("units.name.Pascal"));
        names.put(SI.RADIAN, I18n.getInstance().getString("units.name.Radian"));
        names.put(SI.SECOND, I18n.getInstance().getString("units.name.Second"));
        names.put(SI.SIEMENS, I18n.getInstance().getString("units.name.Siemens"));
        names.put(SI.SIEVERT, I18n.getInstance().getString("units.name.Sievert"));
        names.put(SI.SQUARE_METRE, I18n.getInstance().getString("units.name.SquareMetre"));
        names.put(SI.STERADIAN, I18n.getInstance().getString("units.name.Steradian"));
        names.put(SI.TESLA, I18n.getInstance().getString("units.name.Tesla"));
        names.put(SI.VOLT, I18n.getInstance().getString("units.name.Volt"));
        names.put(SI.WATT, I18n.getInstance().getString("units.name.Watt"));
        names.put(SI.WEBER, I18n.getInstance().getString("units.name.Weber"));
//----NONSI
        names.put(NonSI.ANGSTROM, I18n.getInstance().getString("units.name.Angstrom"));
        names.put(NonSI.ARE, I18n.getInstance().getString("units.name.Are"));
        names.put(NonSI.ASTRONOMICAL_UNIT, I18n.getInstance().getString("units.name.AstronomicalUnit"));
        names.put(NonSI.ATMOSPHERE, I18n.getInstance().getString("units.name.Atmosphere"));
        names.put(NonSI.ATOM, I18n.getInstance().getString("units.name.Atom"));
        names.put(NonSI.ATOMIC_MASS, I18n.getInstance().getString("units.name.AtomicMass"));
        names.put(NonSI.BAR, I18n.getInstance().getString("units.name.Bar"));
        names.put(NonSI.BYTE, I18n.getInstance().getString("units.name.Byte"));
        names.put(NonSI.C, I18n.getInstance().getString("units.name.C"));
        names.put(NonSI.CENTIRADIAN, I18n.getInstance().getString("units.name.Centiradian"));
        names.put(NonSI.COMPUTER_POINT, I18n.getInstance().getString("units.name.ComputerPoint"));
        names.put(NonSI.CUBIC_INCH, I18n.getInstance().getString("units.name.CubicInch"));
        names.put(NonSI.CURIE, I18n.getInstance().getString("units.name.Curie"));
        names.put(NonSI.DAY, I18n.getInstance().getString("units.name.Day"));
        names.put(NonSI.DAY_SIDEREAL, I18n.getInstance().getString("units.name.Day_Sidereal"));
        names.put(NonSI.DECIBEL, I18n.getInstance().getString("units.name.Decibel"));
        names.put(NonSI.DEGREE_ANGLE, I18n.getInstance().getString("units.name.DegreeAngle"));
        names.put(NonSI.DYNE, I18n.getInstance().getString("units.name.Dyne"));
        names.put(NonSI.E, I18n.getInstance().getString("units.name.E"));
        names.put(NonSI.ELECTRON_MASS, I18n.getInstance().getString("units.name.ElectronMass"));
        names.put(NonSI.ELECTRON_VOLT, I18n.getInstance().getString("units.name.ElectronVolt"));
        names.put(NonSI.ERG, I18n.getInstance().getString("units.name.Erg"));
        names.put(NonSI.FAHRENHEIT, I18n.getInstance().getString("units.name.Fahrenheit"));
        names.put(NonSI.FARADAY, I18n.getInstance().getString("units.name.Faraday"));
        names.put(NonSI.FOOT, I18n.getInstance().getString("units.name.Foot"));
        names.put(NonSI.FOOT_SURVEY_US, I18n.getInstance().getString("units.name.FootSurveyUs"));
        names.put(NonSI.FRANKLIN, I18n.getInstance().getString("units.name.Franklin"));
        names.put(NonSI.G, I18n.getInstance().getString("units.name.G"));
        names.put(NonSI.GALLON_DRY_US, I18n.getInstance().getString("units.name.GallonDryUs"));
        names.put(NonSI.GALLON_LIQUID_US, I18n.getInstance().getString("units.name.GallonLiquidUS"));
        names.put(NonSI.GALLON_UK, I18n.getInstance().getString("units.name.GallonUK"));
        names.put(NonSI.GAUSS, I18n.getInstance().getString("units.name.Gauss"));
        names.put(NonSI.GILBERT, I18n.getInstance().getString("units.name.Gilbert"));
        names.put(NonSI.GRADE, I18n.getInstance().getString("units.name.Grade"));
        names.put(NonSI.HECTARE, I18n.getInstance().getString("units.name.Hectare"));
        names.put(NonSI.HORSEPOWER, I18n.getInstance().getString("units.name.Horsepower"));
        names.put(NonSI.HOUR, I18n.getInstance().getString("units.name.Hour"));
        names.put(NonSI.INCH, I18n.getInstance().getString("units.name.Inch"));
        names.put(NonSI.INCH_OF_MERCURY, I18n.getInstance().getString("units.name.InchOfMercury"));
        names.put(NonSI.KILOGRAM_FORCE, I18n.getInstance().getString("units.name.KilogramForce"));
        names.put(NonSI.KILOMETERS_PER_HOUR, I18n.getInstance().getString("units.name.KilometersPerHour"));
        names.put(NonSI.KNOT, I18n.getInstance().getString("units.name.Knot"));
        names.put(NonSI.LAMBERT, I18n.getInstance().getString("units.name.Lambert"));
        names.put(NonSI.LIGHT_YEAR, I18n.getInstance().getString("units.name.LightYear"));
        names.put(NonSI.LITER, I18n.getInstance().getString("units.name.Liter"));
        names.put(NonSI.LITRE, I18n.getInstance().getString("units.name.Litre"));
        names.put(NonSI.MACH, I18n.getInstance().getString("units.name.Mach"));
        names.put(NonSI.MAXWELL, I18n.getInstance().getString("units.name.Maxwell"));
        names.put(NonSI.METRIC_TON, I18n.getInstance().getString("units.name.MetricTon"));
        names.put(NonSI.MILE, I18n.getInstance().getString("units.name.Mile"));
        names.put(NonSI.MILES_PER_HOUR, I18n.getInstance().getString("units.name.MilesPerHour"));
        names.put(NonSI.MILLIMETER_OF_MERCURY, I18n.getInstance().getString("units.name.MillimeterOfMercury"));
        names.put(NonSI.MINUTE, I18n.getInstance().getString("units.name.Minute"));
        names.put(NonSI.MINUTE_ANGLE, I18n.getInstance().getString("units.name.MinuteAngle"));
        names.put(NonSI.MONTH, I18n.getInstance().getString("units.name.Month"));
        names.put(NonSI.NAUTICAL_MILE, I18n.getInstance().getString("units.name.NauticalMile"));
        names.put(NonSI.OCTET, I18n.getInstance().getString("units.name.Octet"));
        names.put(NonSI.OUNCE, I18n.getInstance().getString("units.name.Ounce"));
        names.put(NonSI.OUNCE_LIQUID_UK, I18n.getInstance().getString("units.name.OunceLiquidUK"));
        names.put(NonSI.PARSEC, I18n.getInstance().getString("units.name.Parsec"));
        names.put(NonSI.PERCENT, I18n.getInstance().getString("units.name.Percent"));
        names.put(NonSI.PIXEL, I18n.getInstance().getString("units.name.Pixel"));
        names.put(NonSI.POINT, I18n.getInstance().getString("units.name.Point"));
        names.put(NonSI.POISE, I18n.getInstance().getString("units.name.Poise"));
        names.put(NonSI.POUND, I18n.getInstance().getString("units.name.Pound"));
        names.put(NonSI.POUND_FORCE, I18n.getInstance().getString("units.name.PoundForce"));
        names.put(NonSI.RAD, I18n.getInstance().getString("units.name.Rad"));
        names.put(NonSI.RANKINE, I18n.getInstance().getString("units.name.Rankine"));
        names.put(NonSI.REM, I18n.getInstance().getString("units.name.Rem"));
        names.put(NonSI.REVOLUTION, I18n.getInstance().getString("units.name.Revolution"));
        names.put(NonSI.ROENTGEN, I18n.getInstance().getString("units.name.Roentgen"));
        names.put(NonSI.RUTHERFORD, I18n.getInstance().getString("units.name.Rutherford"));
        names.put(NonSI.SECOND_ANGLE, I18n.getInstance().getString("units.name.SecondAngle"));
        names.put(NonSI.SPHERE, I18n.getInstance().getString("units.name.Sphere"));
        names.put(NonSI.STOKE, I18n.getInstance().getString("units.name.Stoke"));
        names.put(NonSI.TON_UK, I18n.getInstance().getString("units.name.TonUK"));
        names.put(NonSI.TON_US, I18n.getInstance().getString("units.name.TonUS"));
        names.put(NonSI.WEEK, I18n.getInstance().getString("units.name.Week"));
        names.put(NonSI.YARD, I18n.getInstance().getString("units.name.Yard"));
        names.put(NonSI.YEAR, I18n.getInstance().getString("units.name.Year"));
        names.put(NonSI.YEAR_CALENDAR, I18n.getInstance().getString("units.name.YearCalendar"));
        names.put(NonSI.YEAR_SIDEREAL, I18n.getInstance().getString("units.name.YearSidereal"));
        //Prefix

        names.put(SI.ZETTA(Unit.ONE), I18n.getInstance().getString("units.name.Zetta"));
        names.put(SI.EXA(Unit.ONE), I18n.getInstance().getString("units.name.Exa"));
        names.put(SI.PETA(Unit.ONE), I18n.getInstance().getString("units.name.Peta"));
        names.put(SI.TERA(Unit.ONE), I18n.getInstance().getString("units.name.Tera"));
        names.put(SI.GIGA(Unit.ONE), I18n.getInstance().getString("units.name.Giga"));
        names.put(SI.MEGA(Unit.ONE), I18n.getInstance().getString("units.name.Mega"));
        names.put(SI.KILO(Unit.ONE), I18n.getInstance().getString("units.name.Kilo"));
        names.put(SI.HECTO(Unit.ONE), I18n.getInstance().getString("units.name.Hecto"));
        names.put(SI.DEKA(Unit.ONE), I18n.getInstance().getString("units.name.Deka"));
        names.put(SI.DECI(Unit.ONE), I18n.getInstance().getString("units.name.Deci"));
        names.put(SI.CENTI(Unit.ONE), I18n.getInstance().getString("units.name.Centi"));
        names.put(SI.MILLI(Unit.ONE), I18n.getInstance().getString("units.name.Milli"));
        names.put(SI.MICRO(Unit.ONE), I18n.getInstance().getString("units.name.Micro"));
        names.put(SI.NANO(Unit.ONE), I18n.getInstance().getString("units.name.Nano"));
        names.put(SI.PICO(Unit.ONE), I18n.getInstance().getString("units.name.Pico"));
        names.put(SI.FEMTO(Unit.ONE), I18n.getInstance().getString("units.name.Femto"));
        names.put(SI.ATTO(Unit.ONE), I18n.getInstance().getString("units.name.Atto"));
        names.put(SI.ZEPTO(Unit.ONE), I18n.getInstance().getString("units.name.Zepto"));
        names.put(SI.YOCTO(Unit.ONE), I18n.getInstance().getString("units.name.Yocto"));

        //money does not work with the rest of the system. The API will store € but cannot parse it again....
        // we have to use Currency + alt symbol :(
        names.put(Money.BASE_UNIT.alternate("€"), I18n.getInstance().getString("units.currency.name.Euro"));
        names.put(Money.BASE_UNIT.alternate("£"), I18n.getInstance().getString("units.currency.name.Pound"));
        names.put(Money.BASE_UNIT.alternate("$"), I18n.getInstance().getString("units.currency.name.US-Dollar"));
        names.put(Money.BASE_UNIT.alternate("£"), I18n.getInstance().getString("units.currency.name.Yen"));
        names.put(Money.BASE_UNIT.alternate("¥"), I18n.getInstance().getString("units.currency.name.Yuan"));
        names.put(Money.BASE_UNIT.alternate("₦"), I18n.getInstance().getString("units.currency.name.Naira"));
        names.put(Money.BASE_UNIT.alternate("元"), I18n.getInstance().getString("units.currency.name.Renminbi"));
        names.put(Money.BASE_UNIT.alternate("₹"), I18n.getInstance().getString("units.currency.name.Rupee"));
        ///--additonal

//        name.put
        return names;
    }

    /**
     * Ask the JEAPI localization feature for the correct name of the unit.
     * Unit is identified by its symbol(this should be unitq?)
     * <p>.getStandardUnit().
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
//            logger.info(String.format(" get name for: %s [%s] = %s", unit.toString(), unit.getStandardUnit().toString(), name));
            return name;
        } else {
            return "Dimensionless";
        }

    }

    public String getQuantitiesName(Unit<Quantity> unit, Locale locale) {

        String name = getNameMapQuantities().get(unit.getStandardUnit());
        if (name != null && !name.isEmpty()) {
//            logger.info(String.format(" get name for: %s [%s] = %s", unit.toString(), unit.getStandardUnit().toString(), name));
            return name;
        } else {
            return "Dimensionless";
        }

    }

    public List<JEVisUnit> getCompatibleNonSIUnit(JEVisUnit unit) {
        List<JEVisUnit> units = new ArrayList<>();

        for (JEVisUnit other : getNonSIJEVisUnits()) {
            if (other.getUnit().getStandardUnit().isCompatible(unit.getUnit().getStandardUnit()) && !other.equals(unit)) {

//            if (unit.isCompatible(other)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleNonSIUnit(Unit<Quantity> unit) {
        List<Unit> units = new ArrayList<Unit>();

        for (Unit other : getNonSIUnits()) {
            if (unit.isCompatible(other)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleQuantityUnit(Unit<Quantity> unit) {
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
//            if (unit.isCompatible(other) && !other.equals(unit)) {
//                units.add(other);
//            }
            if (other.getUnit().getStandardUnit().isCompatible(unit.getUnit().getStandardUnit()) && !other.equals(unit)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<JEVisUnit> getCompatibleAdditionalUnit(JEVisUnit unit) {
        List<JEVisUnit> units = new ArrayList<>();

        for (JEVisUnit other : getAdditionalJEVisUnits()) {
//            System.out.print(other + " ? ...");
//            if (unit.isCompatible(other) && !other.equals(unit)) {
            if (other.getUnit().getStandardUnit().isCompatible(unit.getUnit().getStandardUnit()) && !other.equals(unit)) {
//                logger.info("is");
                units.add(other);
            }
//            logger.info("NOT");
        }

        return units;
    }

    public List<Unit> getCompatibleSIUnit(Unit<Quantity> unit) {
        List<Unit> units = new ArrayList<Unit>();

        for (Unit other : getSIUnits()) {
            if (unit.isCompatible(other) && !other.equals(unit)) {
                units.add(other);
            }
        }

        return units;
    }

    public List<Unit> getCompatibleAdditionalUnit(Unit<Quantity> unit) {
        List<Unit> units = new ArrayList<Unit>();
//        logger.info("Found add units for: " + unit);

        for (Unit other : getAdditionalUnits()) {
//            System.out.print(other + " ? ...");
            if (unit.getStandardUnit().isCompatible(other) && !other.equals(unit)) {
//                logger.info("is");
                units.add(other);
            }
//            logger.info("NOT");
        }

        return units;
    }

    public List<Unit> getFavoriteQuantitys() {
        List<Unit> list = new ArrayList<Unit>();

        list.add(Dimensionless.UNIT);
        list.add(Energy.UNIT);
        list.add(Power.UNIT);
        list.add(SI.WATT.times(NonSI.HOUR));
        list.add(Temperature.UNIT);
        list.add(Volume.UNIT);
        list.add(Area.UNIT);

        return list;
    }

    public List<JEVisUnit> getFavoriteJUnits() {
        if (favUnits != null) {
            return favUnits;
        }

        favUnits = new ArrayList<>();

        Unit _l = NonSI.LITER;
        Unit _W = SI.WATT;
        Unit _kW = SI.KILO(SI.WATT);
        Unit _MW = SI.MEGA(SI.WATT);
        Unit _GW = SI.GIGA(SI.WATT);
        Unit _Wh = SI.WATT.times(NonSI.HOUR);
        Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
        Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);
        Unit _m3 = SI.CUBIC_METRE;
        Unit _m2 = SI.SQUARE_METRE;
        Unit _t = NonSI.METRIC_TON;
        Unit _C = SI.CELSIUS;
        Unit _cubicMeterPerHour = SI.CUBIC_METRE.divide(NonSI.HOUR);
        Unit _kgh = SI.KILOGRAM.divide(NonSI.HOUR);
        Unit _per100 = NonSI.PERCENT;


        final JEVisUnit t = new JEVisUnitImp(_t);
        final JEVisUnit W = new JEVisUnitImp(_W);
        final JEVisUnit kW = new JEVisUnitImp(_kW);
        final JEVisUnit MW = new JEVisUnitImp(_MW);
        final JEVisUnit GW = new JEVisUnitImp(_GW);
        final JEVisUnit Wh = new JEVisUnitImp(_Wh);
        final JEVisUnit kWh = new JEVisUnitImp(_kWh);
        final JEVisUnit MWh = new JEVisUnitImp(_MWh);
        final JEVisUnit GWh = new JEVisUnitImp(_GWh);
        final JEVisUnit l = new JEVisUnitImp(_l);
        final JEVisUnit m2 = new JEVisUnitImp(_m2);
        final JEVisUnit m3 = new JEVisUnitImp(_m3);
        final JEVisUnit C = new JEVisUnitImp(_C);
        final JEVisUnit cubicMeterPerHour = new JEVisUnitImp(_cubicMeterPerHour);
        final JEVisUnit kgh = new JEVisUnitImp(_kgh);
        final JEVisUnit per100 = new JEVisUnitImp(_per100);

        favUnits.add(kW);
        favUnits.add(kWh);

        favUnits.add(C);
        favUnits.add(l);
        favUnits.add(t);
        favUnits.add(m2);
        favUnits.add(m3);
        favUnits.add(kgh);
        favUnits.add(cubicMeterPerHour);
        favUnits.add(per100);


        //favUnits.add(MWh);
        //favUnits.add(MW);
        //favUnits.add(GW);
        //favUnits.add(Wh);
        //favUnits.add(GWh);

        return favUnits;
    }


    public MetricPrefix getPrefixFromShort(String sub) {
        switch (sub) {
            case "Y":
                return YOTTA;
            case "a":
                return ATTO;
            case "c":
                return CENTI;
            case "d":
                return DECI;
            case "da":
                return DEKA;
            case "E":
                return EXA;
            case "f":
                return FEMTO;
            case "G":
                return GIGA;
            case "h":
                return HECTO;
            case "k":
                return KILO;
            case "M":
                return MEGA;
            case "µ":
                return MICRO;
            case "m":
                return MILLI;
            case "n":
                return NANO;
            case "P":
                return PETA;
            case "p":
                return PICO;
            case "T":
                return TERA;
            case "y":
                return YOCTO;
            case "z":
                return ZEPTO;
            case "Z":
                return ZETTA;
            default:
                return null;
        }
    }
}
