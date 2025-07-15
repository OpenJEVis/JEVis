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
import org.jevis.commons.unit.dimensions.Currency;
import si.uom.NonSI;
import si.uom.SI;
import systems.uom.common.Imperial;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.spi.DefaultServiceProvider;
import tech.units.indriya.unit.TransformedUnit;
import tech.units.indriya.unit.Units;

import javax.measure.*;
import javax.measure.quantity.*;
import javax.measure.spi.ServiceProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.measure.BinaryPrefix.*;
import static javax.measure.MetricPrefix.*;
import static org.jevis.commons.unit.Currencies.*;
import static systems.uom.common.Imperial.*;
import static systems.uom.common.USCustomary.*;
import static systems.uom.common.USCustomary.CUBIC_INCH;
import static tech.units.indriya.unit.Units.*;
import static tech.units.indriya.unit.Units.HOUR;

/**
 * This Class helps with the handling of JScince Lib. This class in not final an
 * may be chaned in the future.
 *
 * @author fs
 */
public class UnitManager {
    private static final Logger logger = LogManager.getLogger(UnitManager.class);
    static final String[] METRIC_PREFIX_SYMBOLS =
            Stream.of(MetricPrefix.values())
                    .map(Prefix::getSymbol)
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
    private final static UnitManager unitManager = new UnitManager();

    private List<Unit> quantities;
    private List<JEVisUnit> _quantitiesJunit;
    private List<Unit> nonSI;
    private List<JEVisUnit> _nonSIJunit;
    private List<Unit> si;
    private List<JEVisUnit> _siJunit;
    private List<Unit> additionalUnits;
    private HashMap<Unit, String> names;
    private HashMap<JEVisUnit, String> _namesJUnit;
    static final UnitConverter[] METRIC_PREFIX_CONVERTERS =
            Stream.of(MetricPrefix.values())
                    .map(MultiplyConverter::ofPrefix)
                    .collect(Collectors.toList())
                    .toArray(new UnitConverter[]{});
    private HashMap<JEVisUnit, String> _dimNamesJUnit;
    private List<JEVisUnit> favUnits;

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private UnitManager() {
    }

    public static UnitManager getInstance() {
        return unitManager;
    }
    static final String[] BINARY_PREFIX_SYMBOLS =
            Stream.of(BinaryPrefix.values())
                    .map(Prefix::getSymbol)
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
    static final UnitConverter[] BINARY_PREFIX_CONVERTERS =
            Stream.of(BinaryPrefix.values())
                    .map(MultiplyConverter::ofPrefix)
                    .collect(Collectors.toList())
                    .toArray(new UnitConverter[]{});
    private static final ServiceProvider serviceProvider = new DefaultServiceProvider();
    private HashMap<Class, String> dimNames;

    public List<JEVisUnit> getQuantitiesJunit() {
        if (_quantitiesJunit != null) {
            return _quantitiesJunit;
        }
        _quantitiesJunit = new ArrayList<>();

        for (Unit unit : ServiceProvider.current().getSystemOfUnitsService().getSystemOfUnits().getUnits()) {
            Quantity<?> result = Quantities.getQuantity(unit.getName());
            //TODO check
            _quantitiesJunit.add(new JEVisUnitImp(unit));
        }

        return _quantitiesJunit;
    }

    public List<JEVisUnit> getNonSIJEVisUnits() {
        if (_nonSIJunit != null) {
            return _nonSIJunit;
        }
        _nonSIJunit = new ArrayList<>();

        for (Unit unit : ServiceProvider.current().getSystemOfUnitsService().getSystemOfUnits("Imperial").getUnits()) {
            _nonSIJunit.add(new JEVisUnitImp(unit));
        }

        return _nonSIJunit;
    }

    protected String prefixFor(UnitConverter converter) {
        for (int i = 0; i < METRIC_PREFIX_CONVERTERS.length; i++) {
            if (METRIC_PREFIX_CONVERTERS[i].equals(converter)) {
                return METRIC_PREFIX_SYMBOLS[i];
            }
        }
        for (int j = 0; j < BINARY_PREFIX_CONVERTERS.length; j++) {
            if (BINARY_PREFIX_CONVERTERS[j].equals(converter)) {
                return BINARY_PREFIX_SYMBOLS[j];
            }
        }
        return null; // TODO or return blank?
    }

    public Prefix prefixForUnit(Unit unit) {
        if (unit instanceof TransformedUnit) {
            TransformedUnit<?> tfmUnit = (TransformedUnit<?>) unit;
            UnitConverter cvtr = tfmUnit.getConverter();
            String prefix = prefixFor(cvtr);

            for (MetricPrefix value : MetricPrefix.values()) {
                if (value.getName().equals(prefix)) return value;
            }

            for (BinaryPrefix value : BinaryPrefix.values()) {
                if (value.getName().equals(prefix)) return value;
            }

        }
        return CustomPrefix.NONE;
    }

    public List<Unit> getNonSIUnits() {
        if (nonSI != null) {
            return nonSI;
        }
        nonSI = new ArrayList<>();
        nonSI.add(NonSI.ANGSTROM);
        nonSI.add(ARE);
        nonSI.add(NonSI.ASTRONOMICAL_UNIT);
        nonSI.add(NonSI.ATOM);
        nonSI.add(NonSI.ATOMIC_MASS);
        nonSI.add(NonSI.BAR);
        nonSI.add(NonSI.C);
        nonSI.add(CENTIRADIAN);
        nonSI.add(CUBIC_INCH);
        nonSI.add(NonSI.CURIE);
        nonSI.add(NonSI.DAY_SIDEREAL);
        nonSI.add(NonSI.DEGREE_ANGLE);
        nonSI.add(NonSI.DYNE);
        nonSI.add(NonSI.ELECTRON_MASS);
        nonSI.add(NonSI.ELECTRON_VOLT);
        nonSI.add(NonSI.ERG);
        nonSI.add(USCustomary.FAHRENHEIT);
        nonSI.add(NonSI.FARADAY);
        nonSI.add(USCustomary.FOOT);
        nonSI.add(NonSI.FRANKLIN);
        nonSI.add(USCustomary.GALLON_DRY);
        nonSI.add(USCustomary.GALLON_LIQUID);
        nonSI.add(GALLON_UK);
        nonSI.add(NonSI.GAUSS);
        nonSI.add(NonSI.GILBERT);
        nonSI.add(USCustomary.GRADE);
        nonSI.add(NonSI.HECTARE);
        nonSI.add(NonSI.HORSEPOWER);
        nonSI.add(USCustomary.HOUR);
        nonSI.add(USCustomary.INCH);
        nonSI.add(NonSI.INCH_OF_MERCURY);
        nonSI.add(NonSI.KILOGRAM_FORCE);
        nonSI.add(NonSI.KNOT);
        nonSI.add(NonSI.LAMBERT);
        nonSI.add(NonSI.LIGHT_YEAR);
        nonSI.add(USCustomary.LITER);
        nonSI.add(Imperial.LITRE);
        nonSI.add(NonSI.MAXWELL);
        nonSI.add(METRIC_TON);
        nonSI.add(USCustomary.MILE);
        nonSI.add(USCustomary.MINUTE);
        nonSI.add(NonSI.MINUTE_ANGLE);
        nonSI.add(NonSI.NAUTICAL_MILE);
        nonSI.add(USCustomary.OUNCE);
        nonSI.add(NonSI.PARSEC);
        nonSI.add(NonSI.POISE);
        nonSI.add(USCustomary.POUND);
        nonSI.add(POUND_FORCE);
        nonSI.add(NonSI.RAD);
        nonSI.add(NonSI.RANKINE);
        nonSI.add(NonSI.REM);
        nonSI.add(NonSI.REVOLUTION);
        nonSI.add(NonSI.ROENTGEN);
        nonSI.add(NonSI.SECOND_ANGLE);
        nonSI.add(Imperial.TON_UK);
        nonSI.add(USCustomary.TON);
        nonSI.add(SI.WATT.alternate("va").multiply(HOUR));
        nonSI.add(SI.WATT.alternate("var").multiply(HOUR));
        nonSI.add(USCustomary.YARD);
        nonSI.add(NonSI.YEAR_CALENDAR);
        nonSI.add(NonSI.YEAR_SIDEREAL);

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

    public List<Unit> getAdditionalUnits() {
        if (additionalUnits != null) {
            return additionalUnits;
        }
        additionalUnits = new ArrayList<>();

        additionalUnits.add(NonSI.REVOLUTION.divide(SI.SECOND));
        additionalUnits.add(SI.WATT.multiply(SI.SECOND));
        additionalUnits.add(SI.WATT.multiply(HOUR));

        additionalUnits.add(EUR);
        additionalUnits.add(GBP);
        additionalUnits.add(USD);
        additionalUnits.add(CAD);
        additionalUnits.add(AUD);
        additionalUnits.add(YEN);
        additionalUnits.add(CNY);
        additionalUnits.add(KRW);
        additionalUnits.add(TWD);
        additionalUnits.add(INR);

        additionalUnits.add(SI.WATT_PER_SQUARE_METRE);
        additionalUnits.add(SI.METRE_PER_SECOND);

        additionalUnits.add(SI.OHM.divide(CENTI(SI.METRE).pow(2)));
        additionalUnits.add(CENTI(SI.METRE).pow(2));
        additionalUnits.add(KILO(SI.METRE).pow(2));
        additionalUnits.add(KILO(SI.WATT).multiply(HOUR.multiply(SI.SECOND).multiply(AbstractUnit.ONE)));

        //kWh/m²kWh/m²
        additionalUnits.add(((KILO(SI.WATT).multiply(HOUR)).divide(SI.SQUARE_METRE)));

        //Norm cubic metre
        additionalUnits.add(SI.CUBIC_METRE.alternate("Nm³"));

        Unit<Mass> kkg = new TransformedUnit<>("kkg", "Kiloton", KILOGRAM, MultiplyConverter.ofRational(1000, 1));
        additionalUnits.add(kkg);

        return additionalUnits;
    }

    public HashMap<Unit, String> getNameMap() {
        if (names != null) {
            return names;
        }
        names = new HashMap<>();

        names.put(SI.AMPERE, I18n.getInstance().getString("units.name.Ampere"));
        names.put(SI.BECQUEREL, I18n.getInstance().getString("units.name.Becquerel"));
        names.put(SI.CANDELA, I18n.getInstance().getString("units.name.Candela"));
        names.put(SI.CELSIUS, I18n.getInstance().getString("units.name.Celsius"));
        names.put(CENTI(METRE), I18n.getInstance().getString("units.name.Centimeter"));
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
        names.put(KILO(METRE), I18n.getInstance().getString("units.name.Kilometer"));
        names.put(SI.LUMEN, I18n.getInstance().getString("units.name.Lumen"));
        names.put(SI.LUX, I18n.getInstance().getString("units.name.Lux"));
        names.put(METRE, I18n.getInstance().getString("units.name.Meter"));
        names.put(METRE_PER_SECOND, I18n.getInstance().getString("units.name.MetersPerSecond"));
        names.put(METRE_PER_SQUARE_SECOND, I18n.getInstance().getString("units.name.MetersPerSquareSecond"));
        names.put(MILLI(METRE), I18n.getInstance().getString("units.name.Millimetre"));
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
        names.put(ARE, I18n.getInstance().getString("units.name.Are"));
        names.put(NonSI.ASTRONOMICAL_UNIT, I18n.getInstance().getString("units.name.AstronomicalUnit"));
        names.put(NonSI.ATOM, I18n.getInstance().getString("units.name.Atom"));
        names.put(NonSI.ATOMIC_MASS, I18n.getInstance().getString("units.name.AtomicMass"));
        names.put(NonSI.BAR, I18n.getInstance().getString("units.name.Bar"));
        names.put(NonSI.C, I18n.getInstance().getString("units.name.C"));
        names.put(CENTIRADIAN, I18n.getInstance().getString("units.name.Centiradian"));
        names.put(CUBIC_INCH, I18n.getInstance().getString("units.name.CubicInch"));
        names.put(NonSI.CURIE, I18n.getInstance().getString("units.name.Curie"));
        names.put(Units.DAY, I18n.getInstance().getString("units.name.Day"));
        names.put(NonSI.DAY_SIDEREAL, I18n.getInstance().getString("units.name.Day_Sidereal"));

        names.put(NonSI.DEGREE_ANGLE, I18n.getInstance().getString("units.name.DegreeAngle"));
        names.put(NonSI.DYNE, I18n.getInstance().getString("units.name.Dyne"));

        names.put(NonSI.ELECTRON_MASS, I18n.getInstance().getString("units.name.ElectronMass"));
        names.put(NonSI.ELECTRON_VOLT, I18n.getInstance().getString("units.name.ElectronVolt"));
        names.put(NonSI.ERG, I18n.getInstance().getString("units.name.Erg"));
        names.put(USCustomary.FAHRENHEIT, I18n.getInstance().getString("units.name.Fahrenheit"));
        names.put(NonSI.FARADAY, I18n.getInstance().getString("units.name.Faraday"));
        names.put(USCustomary.FOOT, I18n.getInstance().getString("units.name.Foot"));
        names.put(USCustomary.FOOT_SURVEY, I18n.getInstance().getString("units.name.FootSurveyUs"));
        names.put(NonSI.FRANKLIN, I18n.getInstance().getString("units.name.Franklin"));
        names.put(USCustomary.GALLON_DRY, I18n.getInstance().getString("units.name.GallonDryUs"));
        names.put(USCustomary.GALLON_LIQUID, I18n.getInstance().getString("units.name.GallonLiquidUS"));
        names.put(Imperial.GALLON_UK, I18n.getInstance().getString("units.name.GallonUK"));
        names.put(NonSI.GAUSS, I18n.getInstance().getString("units.name.Gauss"));
        names.put(NonSI.GILBERT, I18n.getInstance().getString("units.name.Gilbert"));
        names.put(USCustomary.GRADE, I18n.getInstance().getString("units.name.Grade"));
        names.put(NonSI.HECTARE, I18n.getInstance().getString("units.name.Hectare"));
        names.put(NonSI.HORSEPOWER, I18n.getInstance().getString("units.name.Horsepower"));
        names.put(Units.HOUR, I18n.getInstance().getString("units.name.Hour"));
        names.put(USCustomary.INCH, I18n.getInstance().getString("units.name.Inch"));
        names.put(NonSI.INCH_OF_MERCURY, I18n.getInstance().getString("units.name.InchOfMercury"));
        names.put(NonSI.KILOGRAM_FORCE, I18n.getInstance().getString("units.name.KilogramForce"));
        names.put(KILO(METRE).divide(HOUR), I18n.getInstance().getString("units.name.KilometersPerHour"));
        names.put(NonSI.KNOT, I18n.getInstance().getString("units.name.Knot"));
        names.put(NonSI.LAMBERT, I18n.getInstance().getString("units.name.Lambert"));
        names.put(NonSI.LIGHT_YEAR, I18n.getInstance().getString("units.name.LightYear"));
        names.put(USCustomary.LITER, I18n.getInstance().getString("units.name.Liter"));
        names.put(Units.LITRE, I18n.getInstance().getString("units.name.Litre"));
        names.put(NonSI.MAXWELL, I18n.getInstance().getString("units.name.Maxwell"));
        names.put(Imperial.METRIC_TON, I18n.getInstance().getString("units.name.MetricTon"));
        names.put(USCustomary.MILE, I18n.getInstance().getString("units.name.Mile"));
        names.put(USCustomary.MILE_PER_HOUR, I18n.getInstance().getString("units.name.MilesPerHour"));
        names.put(NonSI.MILLIMETRE_OF_MERCURY, I18n.getInstance().getString("units.name.MillimeterOfMercury"));
        names.put(Units.MINUTE, I18n.getInstance().getString("units.name.Minute"));
        names.put(NonSI.MINUTE_ANGLE, I18n.getInstance().getString("units.name.MinuteAngle"));
        names.put(Units.MONTH, I18n.getInstance().getString("units.name.Month"));
        names.put(NonSI.NAUTICAL_MILE, I18n.getInstance().getString("units.name.NauticalMile"));
        names.put(USCustomary.OUNCE, I18n.getInstance().getString("units.name.Ounce"));
        names.put(Imperial.OUNCE, I18n.getInstance().getString("units.name.OunceLiquidUK"));
        names.put(NonSI.PARSEC, I18n.getInstance().getString("units.name.Parsec"));
        names.put(Units.PERCENT, I18n.getInstance().getString("units.name.Percent"));
        names.put(NonSI.POISE, I18n.getInstance().getString("units.name.Poise"));
        names.put(USCustomary.POUND, I18n.getInstance().getString("units.name.Pound"));
        names.put(Imperial.POUND_FORCE, I18n.getInstance().getString("units.name.PoundForce"));
        names.put(NonSI.RAD, I18n.getInstance().getString("units.name.Rad"));
        names.put(NonSI.RANKINE, I18n.getInstance().getString("units.name.Rankine"));
        names.put(NonSI.REM, I18n.getInstance().getString("units.name.Rem"));
        names.put(NonSI.REVOLUTION, I18n.getInstance().getString("units.name.Revolution"));
        names.put(NonSI.ROENTGEN, I18n.getInstance().getString("units.name.Roentgen"));
        names.put(NonSI.SECOND_ANGLE, I18n.getInstance().getString("units.name.SecondAngle"));
        names.put(Imperial.TON_UK, I18n.getInstance().getString("units.name.TonUK"));
        names.put(USCustomary.TON, I18n.getInstance().getString("units.name.TonUS"));
        names.put(Units.WEEK, I18n.getInstance().getString("units.name.Week"));
        names.put(USCustomary.YARD, I18n.getInstance().getString("units.name.Yard"));
        names.put(Units.YEAR, I18n.getInstance().getString("units.name.Year"));
        names.put(NonSI.YEAR_CALENDAR, I18n.getInstance().getString("units.name.YearCalendar"));
        names.put(NonSI.YEAR_SIDEREAL, I18n.getInstance().getString("units.name.YearSidereal"));
        //Prefix

        //money does not work with the rest of the system. The API will store € but cannot parse it again....
        // we have to use Currency + alt symbol :(
        names.put(EUR, I18n.getInstance().getString("units.currency.name.Euro"));
        names.put(GBP, I18n.getInstance().getString("units.currency.name.Pound"));
        names.put(USD, I18n.getInstance().getString("units.currency.name.US-Dollar"));
        names.put(YEN, I18n.getInstance().getString("units.currency.name.Yen"));
        names.put(TWD, I18n.getInstance().getString("units.currency.name.Yuan"));
        names.put(CNY, I18n.getInstance().getString("units.currency.name.Renminbi"));
        names.put(INR, I18n.getInstance().getString("units.currency.name.Rupee"));
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
     * @return
     */
    public String getUnitName(Unit unit) {
        String name = getNameMap().get(unit);
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return unit.toString();
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


    public List<JEVisUnit> getSIJEVisUnits() {
        if (_siJunit != null) {
            return _siJunit;
        }
        _siJunit = new ArrayList<>();

        for (Unit unit : ServiceProvider.current().getSystemOfUnitsService().getSystemOfUnits("SI").getUnits()) {
            _siJunit.add(new JEVisUnitImp(unit));
        }

        return _siJunit;
    }

    public HashMap<Class, String> getNameMapQuantities() {
        if (dimNames != null) {
            return dimNames;
        }
        dimNames = new HashMap<>();

        dimNames.put(Dimensionless.class, I18n.getInstance().getString("units.quantities.dimensionless"));
        dimNames.put(ElectricCurrent.class, I18n.getInstance().getString("units.quantities.electriccurrent"));
        dimNames.put(LuminousIntensity.class, I18n.getInstance().getString("units.quantities.luminousintensity"));
        dimNames.put(Temperature.class, I18n.getInstance().getString("units.quantities.temperature"));
        dimNames.put(Mass.class, I18n.getInstance().getString("units.quantities.mass"));
        dimNames.put(Length.class, I18n.getInstance().getString("units.quantities.length"));
        dimNames.put(AmountOfSubstance.class, I18n.getInstance().getString("units.quantities.amountofsubstance"));
        dimNames.put(Time.class, I18n.getInstance().getString("units.quantities.time"));
        dimNames.put(Angle.class, I18n.getInstance().getString("units.quantities.angle"));
        dimNames.put(SolidAngle.class, I18n.getInstance().getString("units.quantities.solidangle"));
        dimNames.put(Frequency.class, I18n.getInstance().getString("units.quantities.frequency"));
        dimNames.put(Force.class, I18n.getInstance().getString("units.quantities.force"));
        dimNames.put(Pressure.class, I18n.getInstance().getString("units.quantities.pressure"));
        dimNames.put(Energy.class, I18n.getInstance().getString("units.quantities.energy"));
        dimNames.put(Power.class, I18n.getInstance().getString("units.quantities.power"));
        dimNames.put(ElectricCharge.class, I18n.getInstance().getString("units.quantities.electriccharge"));
        dimNames.put(ElectricPotential.class, I18n.getInstance().getString("units.quantities.electricpotential"));
        dimNames.put(ElectricCapacitance.class, I18n.getInstance().getString("units.quantities.electriccapacitance"));
        dimNames.put(ElectricResistance.class, I18n.getInstance().getString("units.quantities.electricresistance"));
        dimNames.put(ElectricConductance.class, I18n.getInstance().getString("units.quantities.electricconductance"));
        dimNames.put(MagneticFlux.class, I18n.getInstance().getString("units.quantities.magneticflux"));
        dimNames.put(MagneticFluxDensity.class, I18n.getInstance().getString("units.quantities.magneticfluxdensity"));
        dimNames.put(ElectricInductance.class, I18n.getInstance().getString("units.quantities.electricinductance"));
        dimNames.put(LuminousFlux.class, I18n.getInstance().getString("units.quantities.luminousflux"));
        dimNames.put(Illuminance.class, I18n.getInstance().getString("units.quantities.illuminance"));
        dimNames.put(Radioactivity.class, I18n.getInstance().getString("units.quantities.radioactiveactivity"));
        dimNames.put(RadiationDoseAbsorbed.class, I18n.getInstance().getString("units.quantities.radiationdoseabsorbed"));
        dimNames.put(RadiationDoseEffective.class, I18n.getInstance().getString("units.quantities.radiationdoseeffective"));
        dimNames.put(CatalyticActivity.class, I18n.getInstance().getString("units.quantities.catalyticactivity"));
        dimNames.put(Speed.class, I18n.getInstance().getString("units.quantities.speed"));
        dimNames.put(Acceleration.class, I18n.getInstance().getString("units.quantities.acceleration"));
        dimNames.put(Area.class, I18n.getInstance().getString("units.quantities.area"));
        dimNames.put(Volume.class, I18n.getInstance().getString("units.quantities.volume"));
        dimNames.put(Currency.class, I18n.getInstance().getString("units.quantities.currency"));

//        dimNames.put(AngularVelocity.class, I18n.getInstance().getString("units.quantities.angularvelocity"));
//        dimNames.put(AngularAcceleration.class, I18n.getInstance().getString("units.quantities.angularacceleration"));
//        dimNames.put(DataAmount.class, I18n.getInstance().getString("units.quantities.dataamount"));
//        dimNames.put(DataRate.class, I18n.getInstance().getString("units.quantities.datarate"));
//        dimNames.put(Duration.class, I18n.getInstance().getString("units.quantities.duration"));
//        dimNames.put(DynamicViscosity.class, I18n.getInstance().getString("units.quantities.dynamicviscosity"));
//        dimNames.put(KinematicViscosity.class, I18n.getInstance().getString("units.quantities.kinematicviscosity"));
//        dimNames.put(MassFlowRate.class, I18n.getInstance().getString("units.quantities.massflowrate"));
//        dimNames.put(Torque.class, I18n.getInstance().getString("units.quantities.torque"));
//        dimNames.put(Velocity.class, I18n.getInstance().getString("units.quantities.velocity"));
//        dimNames.put(VolumetricDensity.class, I18n.getInstance().getString("units.quantities.volumetricdensity"));
//        dimNames.put(VolumetricFlowRate.class, I18n.getInstance().getString("units.quantities.volumetricflowrate"));

        return dimNames;
    }

    public List<JEVisUnit> getFavoriteJUnits() {
        if (favUnits != null) {
            return favUnits;
        }

        favUnits = new ArrayList<>();

        Unit _l = Units.LITRE;
        Unit _W = Units.WATT;
        Unit _kW = KILO(Units.WATT);
        Unit _MW = MEGA(Units.WATT);
        Unit _GW = GIGA(Units.WATT);
        Unit _Wh = Units.WATT.multiply(HOUR);
        Unit _kWh = KILO(Units.WATT).multiply(HOUR);
        Unit _MWh = MEGA(Units.WATT).multiply(HOUR);
        Unit _GWh = GIGA(Units.WATT).multiply(HOUR);
        Unit _m3 = Units.CUBIC_METRE;
        Unit _m2 = Units.SQUARE_METRE;
        Unit _t = KILO(Units.KILOGRAM.alternate("t"));
        Unit _C = Units.CELSIUS;
        Unit _cubicMeterPerHour = Units.CUBIC_METRE.divide(HOUR);
        Unit _kgh = Units.KILOGRAM.divide(HOUR);
        Unit _per100 = Units.PERCENT;


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


    public Prefix getPrefixFromShort(String sub) {
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
                return DECA;
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
            case "\u00b5":
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
            case "Ki":
                return KIBI;
            case ("Mi"):
                return MEBI;
            case "Gi":
                return GIBI;
            case "Ti":
                return TEBI;
            case "Pi":
                return PEBI;
            case "Ei":
                return EXBI;
            case "Zi":
                return ZEBI;
            case "Yi":
                return YOBI;
            default:
                return null;
        }
    }
}
