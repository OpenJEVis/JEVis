package org.jevis.jecc.plugin.accounting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;

public class AccountingDirectories {
    private static final Logger logger = LogManager.getLogger(AccountingDirectories.class);

    private final String ENERGY_CONTRACTING_DIRECTORY = "Energy Contracting Directory";
    private final String ENERGY_SUPPLY_DIRECTORY = "Energy Supply Directory";
    private final String ENERGY_METERING_POINT_OPERATION_DIRECTORY = "Energy Metering Point Operation Directory";
    private final String ENERGY_GRID_OPERATION_DIRECTORY = "Energy Grid Operation Directory";
    private final String ENERGY_CONTRACTOR_DIRECTORY = "Energy Contractor Directory";
    private final String ENERGY_GOVERNMENTAL_DUES_DIRECTORY = "Energy Governmental Dues Directory";

    private final String ELECTRICITY_SUPPLY_CONTRACTOR = "Electricity Supply Contractor";
    private final String GAS_SUPPLY_CONTRACTOR = "Gas Supply Contractor";
    private final String COMMUNITY_HEATING_SUPPLY_CONTRACTOR = "Community Heating Supply Contractor";

    private final String ELECTRICITY_METERING_POINT_OPERATOR = "Electricity Metering Point Operator";
    private final String GAS_METERING_POINT_OPERATOR = "Gas Metering Point Operator";
    private final String COMMUNITY_HEATING_METERING_POINT_OPERATOR = "Community Heating Metering Point Operator";

    private final String ELECTRICITY_GRID_OPERATOR = "Electricity Grid Operator";
    private final String GAS_GRID_OPERATOR = "Gas Grid Operator";
    private final String COMMUNITY_HEATING_GRID_OPERATOR = "Community Heating Grid Operator";

    private final String ENERGY_CONTRACTOR = "Energy Contractor";

    private final String GOVERNMENTAL_DUES = "Governmental Dues";

    private final String ENERGY_SUPPLY_CONTRACTOR = "Energy Supply Contractor";
    private final String ENERGY_METERING_POINT_OPERATION_CONTRACTOR = "Energy Metering Point Operation Contractor";
    private final String ENERGY_GRID_OPERATION_CONTRACTOR = "Energy Grid Operation Contractor";

    private final String ENERGY_SUPPLIER = "Energy Supplier";
    private final String ENERGY_METERING_POINT_OPERATOR = "Energy Metering Point Operator";
    private final String ENERGY_GRID_OPERATOR = "Energy Grid Operator";

    private JEVisObject energyContractingDir = null;
    private JEVisObject energySupplyDir = null;
    private JEVisObject energyMeteringPointOperationDir = null;
    private JEVisObject energyGridOperationDir = null;
    private JEVisObject energyContractorDir = null;
    private JEVisObject energyGovernmentalDuesDir = null;
    private JEVisClass electricitySupplyContractorClass = null;
    private JEVisClass gasSupplyContractorClass = null;
    private JEVisClass communityHeatingSupplyContractorClass = null;
    private JEVisClass electricityMeteringPointOperatorClass = null;
    private JEVisClass gasMeteringPointOperatorClass = null;
    private JEVisClass communityHeatingMeteringPointOperatorClass = null;
    private JEVisClass electricityGridOperatorClass = null;
    private JEVisClass gasGridOperatorClass = null;
    private JEVisClass communityHeatingGridOperatorClass = null;
    private JEVisClass energyContractorClass = null;
    private JEVisClass governmentalDuesClass = null;
    private JEVisClass energySupplierClass = null;
    private JEVisClass energyMeteringOperatorClass = null;
    private JEVisClass energyGridOperatorClass = null;
    private JEVisClass energySupplyContractorClass = null;
    private JEVisClass energyMeteringPointOperationContractorClass = null;
    private JEVisClass energyGridOperationContractorClass = null;

    public AccountingDirectories(JEVisDataSource ds) {
        try {
            JEVisClass energyContractingDirectoryClass = ds.getJEVisClass(ENERGY_CONTRACTING_DIRECTORY);
            JEVisClass energySupplyDirectoryClass = ds.getJEVisClass(ENERGY_SUPPLY_DIRECTORY);
            JEVisClass energyMeteringPointOperationDirectoryClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATION_DIRECTORY);
            JEVisClass energyGridOperationDirectoryClass = ds.getJEVisClass(ENERGY_GRID_OPERATION_DIRECTORY);
            JEVisClass energyContractorDirectory = ds.getJEVisClass(ENERGY_CONTRACTOR_DIRECTORY);
            JEVisClass energyGovernmentalDuesDirectory = ds.getJEVisClass(ENERGY_GOVERNMENTAL_DUES_DIRECTORY);

            energySupplierClass = ds.getJEVisClass(ENERGY_SUPPLIER);
            energyMeteringOperatorClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATOR);
            energyGridOperatorClass = ds.getJEVisClass(ENERGY_GRID_OPERATOR);
            energySupplyContractorClass = ds.getJEVisClass(ENERGY_SUPPLY_CONTRACTOR);
            energyMeteringPointOperationContractorClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATION_CONTRACTOR);
            energyGridOperationContractorClass = ds.getJEVisClass(ENERGY_GRID_OPERATION_CONTRACTOR);

            electricitySupplyContractorClass = ds.getJEVisClass(ELECTRICITY_SUPPLY_CONTRACTOR);
            gasSupplyContractorClass = ds.getJEVisClass(GAS_SUPPLY_CONTRACTOR);
            communityHeatingSupplyContractorClass = ds.getJEVisClass(COMMUNITY_HEATING_SUPPLY_CONTRACTOR);

            electricityMeteringPointOperatorClass = ds.getJEVisClass(ELECTRICITY_METERING_POINT_OPERATOR);
            gasMeteringPointOperatorClass = ds.getJEVisClass(GAS_METERING_POINT_OPERATOR);
            communityHeatingMeteringPointOperatorClass = ds.getJEVisClass(COMMUNITY_HEATING_METERING_POINT_OPERATOR);

            electricityGridOperatorClass = ds.getJEVisClass(ELECTRICITY_GRID_OPERATOR);
            gasGridOperatorClass = ds.getJEVisClass(GAS_GRID_OPERATOR);
            communityHeatingGridOperatorClass = ds.getJEVisClass(COMMUNITY_HEATING_GRID_OPERATOR);

            energyContractorClass = ds.getJEVisClass(ENERGY_CONTRACTOR);

            governmentalDuesClass = ds.getJEVisClass(GOVERNMENTAL_DUES);

            energyContractingDir = ds.getObjects(energyContractingDirectoryClass, false).stream().findFirst().orElse(null);
            energySupplyDir = ds.getObjects(energySupplyDirectoryClass, false).stream().findFirst().orElse(null);
            energyMeteringPointOperationDir = ds.getObjects(energyMeteringPointOperationDirectoryClass, false).stream().findFirst().orElse(null);
            energyGridOperationDir = ds.getObjects(energyGridOperationDirectoryClass, false).stream().findFirst().orElse(null);
            energyContractorDir = ds.getObjects(energyContractorDirectory, false).stream().findFirst().orElse(null);
            energyGovernmentalDuesDir = ds.getObjects(energyGovernmentalDuesDirectory, false).stream().findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("Could not get jevisClasses", e);
        }
    }

    public String getENERGY_CONTRACTING_DIRECTORY() {
        return ENERGY_CONTRACTING_DIRECTORY;
    }

    public String getENERGY_SUPPLY_DIRECTORY() {
        return ENERGY_SUPPLY_DIRECTORY;
    }

    public String getENERGY_METERING_POINT_OPERATION_DIRECTORY() {
        return ENERGY_METERING_POINT_OPERATION_DIRECTORY;
    }

    public String getENERGY_GRID_OPERATION_DIRECTORY() {
        return ENERGY_GRID_OPERATION_DIRECTORY;
    }

    public String getENERGY_CONTRACTOR_DIRECTORY() {
        return ENERGY_CONTRACTOR_DIRECTORY;
    }

    public JEVisObject getEnergyContractingDir() {
        return energyContractingDir;
    }

    public JEVisObject getEnergySupplyDir() {
        return energySupplyDir;
    }

    public JEVisObject getEnergyMeteringPointOperationDir() {
        return energyMeteringPointOperationDir;
    }

    public JEVisObject getEnergyGridOperationDir() {
        return energyGridOperationDir;
    }

    public JEVisObject getEnergyContractorDir() {
        return energyContractorDir;
    }

    public String getENERGY_GOVERNMENTAL_DUES_DIRECTORY() {
        return ENERGY_GOVERNMENTAL_DUES_DIRECTORY;
    }

    public JEVisObject getEnergyGovernmentalDuesDir() {
        return energyGovernmentalDuesDir;
    }

    public String getELECTRICITY_SUPPLY_CONTRACTOR() {
        return ELECTRICITY_SUPPLY_CONTRACTOR;
    }

    public String getGAS_SUPPLY_CONTRACTOR() {
        return GAS_SUPPLY_CONTRACTOR;
    }

    public String getCOMMUNITY_HEATING_SUPPLY_CONTRACTOR() {
        return COMMUNITY_HEATING_SUPPLY_CONTRACTOR;
    }

    public String getELECTRICITY_METERING_POINT_OPERATOR() {
        return ELECTRICITY_METERING_POINT_OPERATOR;
    }

    public String getGAS_METERING_POINT_OPERATOR() {
        return GAS_METERING_POINT_OPERATOR;
    }

    public String getCOMMUNITY_HEATING_METERING_POINT_OPERATOR() {
        return COMMUNITY_HEATING_METERING_POINT_OPERATOR;
    }

    public String getELECTRICITY_GRID_OPERATOR() {
        return ELECTRICITY_GRID_OPERATOR;
    }

    public String getGAS_GRID_OPERATOR() {
        return GAS_GRID_OPERATOR;
    }

    public String getCOMMUNITY_HEATING_GRID_OPERATOR() {
        return COMMUNITY_HEATING_GRID_OPERATOR;
    }

    public String getENERGY_CONTRACTOR() {
        return ENERGY_CONTRACTOR;
    }

    public String getGOVERNMENTAL_DUES() {
        return GOVERNMENTAL_DUES;
    }

    public JEVisClass getElectricitySupplyContractorClass() {
        return electricitySupplyContractorClass;
    }

    public JEVisClass getGasSupplyContractorClass() {
        return gasSupplyContractorClass;
    }

    public JEVisClass getCommunityHeatingSupplyContractorClass() {
        return communityHeatingSupplyContractorClass;
    }

    public JEVisClass getElectricityMeteringPointOperatorClass() {
        return electricityMeteringPointOperatorClass;
    }

    public JEVisClass getGasMeteringPointOperatorClass() {
        return gasMeteringPointOperatorClass;
    }

    public JEVisClass getCommunityHeatingMeteringPointOperatorClass() {
        return communityHeatingMeteringPointOperatorClass;
    }

    public JEVisClass getElectricityGridOperatorClass() {
        return electricityGridOperatorClass;
    }

    public JEVisClass getGasGridOperatorClass() {
        return gasGridOperatorClass;
    }

    public JEVisClass getCommunityHeatingGridOperatorClass() {
        return communityHeatingGridOperatorClass;
    }

    public JEVisClass getEnergyContractorClass() {
        return energyContractorClass;
    }

    public JEVisClass getGovernmentalDuesClass() {
        return governmentalDuesClass;
    }

    public JEVisClass getEnergySupplierClass() {
        return energySupplierClass;
    }

    public JEVisClass getEnergyMeteringOperatorClass() {
        return energyMeteringOperatorClass;
    }

    public JEVisClass getEnergyGridOperatorClass() {
        return energyGridOperatorClass;
    }

    public JEVisClass getEnergySupplyContractorClass() {
        return energySupplyContractorClass;
    }

    public JEVisClass getEnergyMeteringPointOperationContractorClass() {
        return energyMeteringPointOperationContractorClass;
    }

    public JEVisClass getEnergyGridOperationContractorClass() {
        return energyGridOperationContractorClass;
    }
}
