package org.jevis.commons.classes;

/**
 * References for all JEVisClass and Type names.
 * Created with the JEVisClassPrinter.
 */
public interface JC {


    public interface AccountingConfiguration {
        public static String name = "Accounting Configuration";

        public static String a_TemplateFile = "Template File";
    }

    public interface Alarm {
        public static String name = "Alarm";

        public interface DynamicLimitAlarm {
            public static String name = "Dynamic Limit Alarm";

            public static String a_AlarmLog = "Alarm Log";
            public static String a_Enable = "Enable";
            public static String a_LimitData = "Limit Data";
            public static String a_Operator = "Operator";
            public static String a_Status = "Status";
            public static String a_Tolerance = "Tolerance";
        }

        public interface StaticLimitAlarm {
            public static String name = "Static Limit Alarm";

            public static String a_AlarmLog = "Alarm Log";
            public static String a_Enable = "Enable";
            public static String a_Limit = "Limit";
            public static String a_Operator = "Operator";
            public static String a_Status = "Status";
        }
    }

    public interface AlarmConfiguration {
        public static String name = "Alarm Configuration";

        public static String a_Enabled = "Enabled";
        public static String a_DisableLink = "Disable Link";
        public static String a_AlarmScope = "Alarm Scope";
        public static String a_AlarmObjects = "Alarm Objects";
        public static String a_TimeStamp = "Time Stamp";
        public static String a_AlarmPeriod = "Alarm Period";
        public static String a_CustomScheduleObject = "Custom Schedule Object";
        public static String a_Log = "Log";
        public static String a_LogFile = "Log File";
        public static String a_AlarmChecked = "Alarm Checked";
    }

    public interface AlarmLink {
        public static String name = "Alarm Link";

        public static String a_AlarmLink = "Alarm Link";
        public static String a_TemplateName = "Template Name";
    }

    public interface Analysis {
        public static String name = "Analysis";

        public static String a_DataModel = "Data Model";
        public static String a_Charts = "Charts";
        public static String a_NumberOfChartsPerScreen = "Number of Charts per Screen";
        public static String a_NumberOfHorizontalPies = "Number of Horizontal Pies";
        public static String a_NumberOfHorizontalTables = "Number of Horizontal Tables";
        public static String a_AnalysisFile = "Analysis File";
    }

    public interface BuildingEquipment {
        public static String name = "Building Equipment";

        public static String a_Manufacturer = "Manufacturer";
        public static String a_Type = "Type";
        public static String a_YearOfConstruction = "Year of Construction";
        public static String a_Number = "Number";
        public static String a_NominalPower = "Nominal Power";
        public static String a_StandbyConsumption = "Standby Consumption";
        public static String a_EnergySource = "Energy Source";
        public static String a_Productivity = "Productivity";
        public static String a_MeasuringPoint = "Measuring Point";
        public static String a_WeightingFactor = "Weighting Factor";
        public static String a_DailyOperatingHours = "Daily Operating Hours";
        public static String a_WorkingDays = "Working Days";
        public static String a_WorkWeeks = "Work Weeks";

        public interface AirConditioning {
            public static String name = "Air Conditioning";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Compressor {
            public static String name = "Compressor";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Flow = "Flow";
            public static String a_NormalOperatingPressure = "Normal Operating Pressure";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Cooler {
            public static String name = "Cooler";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_CoolingCapacity = "Cooling Capacity";
            public static String a_PerformanceNumberCompressor = "Performance Number Compressor";
            public static String a_Refrigerant = "Refrigerant";
            public static String a_ServedArea = "Served Area";
            public static String a_Remark = "Remark";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Engine {
            public static String name = "Engine";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_EfficiencyClass = "Efficiency Class";
            public static String a_PowerFactor = "Power Factor";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Facade {
            public static String name = "Facade";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_UValueGround = "U Value Ground";
            public static String a_UValueOuterwall = "U Value Outerwall";
            public static String a_UValueRoof = "U Value Roof";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Floor {
            public static String name = "Floor";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Floor = "Floor";
            public static String a_Usage = "Usage";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface HeatGeneration {
            public static String name = "Heat Generation";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Domain = "Domain";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface HVACSystemSimplified {
            public static String name = "HVAC System Simplified";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Lighting {
            public static String name = "Lighting";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Domain = "Domain";
            public static String a_BallastPower = "Ballast Power";
            public static String a_BulbsPerLamp = "Bulbs per Lamp";
            public static String a_LampPower = "Lamp Power";
            public static String a_NumberOfLamps = "Number of Lamps";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Occupancy {
            public static String name = "Occupancy";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Occupancy = "Occupancy";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface OfficeEquipment {
            public static String name = "Office Equipment";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Sector = "Sector";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface PantryEquipment {
            public static String name = "Pantry Equipment";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Remark = "Remark";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface PlugLoad {
            public static String name = "Plug Load";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_PlugLoad = "Plug Load";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface ProductionEquipment {
            public static String name = "Production Equipment";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Sector = "Sector";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Pump {
            public static String name = "Pump";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_Domain = "Domain";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface RecoolerOrCondenser {
            public static String name = "Recooler or Condenser";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_RecoolingCapacity = "Recooling Capacity";
            public static String a_Remark = "Remark";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }

        public interface Ventilation {
            public static String name = "Ventilation";

            public static String a_Manufacturer = "Manufacturer";
            public static String a_Type = "Type";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_Number = "Number";
            public static String a_NominalPower = "Nominal Power";
            public static String a_StandbyConsumption = "Standby Consumption";
            public static String a_EnergySource = "Energy Source";
            public static String a_Productivity = "Productivity";
            public static String a_MeasuringPoint = "Measuring Point";
            public static String a_WeightingFactor = "Weighting Factor";
            public static String a_SupplyArea = "Supply Area";
            public static String a_AirExchangeRate = "Air Exchange Rate";
            public static String a_EnginePowerFROM = "Engine Power FROM";
            public static String a_EnginePowerTO = "Engine Power TO";
            public static String a_Remark = "Remark";
            public static String a_DailyOperatingHours = "Daily Operating Hours";
            public static String a_WorkingDays = "Working Days";
            public static String a_WorkWeeks = "Work Weeks";
        }
    }

    public interface Calculation {
        public static String name = "Calculation";

        public static String a_Enabled = "Enabled";
        public static String a_Expression = "Expression";
        public static String a_DIV0Handling = "DIV0 Handling";
        public static String a_StaticValue = "Static Value";
        public static String a_AllZeroValue = "All Zero Value";
    }

    public interface Channel {
        public static String name = "Channel";

        public static String a_LastReadout = "Last Readout";

        public interface EMailChannel {
            public static String name = "EMail Channel";

            public static String a_Sender = "Sender";
            public static String a_Subject = "Subject";
            public static String a_Filename = "Filename";
            public static String a_DataInBody = "Data in body";
            public static String a_LastReadout = "Last Readout";
        }

        public interface FTPChannel {
            public static String name = "FTP Channel";

            public static String a_Path = "Path";
            public static String a_LastReadout = "Last Readout";
        }

        public interface HTTPChannel {
            public static String name = "HTTP Channel";

            public static String a_Path = "Path";
            public static String a_LastReadout = "Last Readout";
        }

        public interface LoytecXMLDLChannel {
            public static String name = "Loytec XML-DL Channel";

            public static String a_TargetID = "Target ID";
            public static String a_TrendID = "Trend ID";
            public static String a_StatusLog = "Status Log";
            public static String a_LastReadout = "Last Readout";
        }

        public interface SFTPChannel {
            public static String name = "sFTP Channel";

            public static String a_Path = "Path";
            public static String a_LastReadout = "Last Readout";
        }

        public interface SOAPChannel {
            public static String name = "SOAP Channel";

            public static String a_Path = "Path";
            public static String a_Template = "Template";
            public static String a_LastReadout = "Last Readout";
        }

        public interface SQLChannel {
            public static String name = "SQL Channel";

            public static String a_Query = "Query";
            public static String a_LastReadout = "Last Readout";
        }

        public interface VIDA350Channel {
            public static String name = "VIDA350 Channel";

            public static String a_Index = "Index";
            public static String a_Target = "Target";
            public static String a_LastReadoutTry = "Last Readout Try";
            public static String a_LastReadout = "Last Readout";
        }

        public interface LoytecXMLDLOutputChannel {
            public static String name = "Loytec XML-DL Output Channel";

            public static String a_TargetID = "Target ID";
            public static String a_OPCID = "OPC ID";
            public static String a_StatusLog = "Status Log";
            public static String a_LastReadout = "Last Readout";
        }

        public interface JEVisChannel {
            public static String name = "JEVis Channel";

            public static String a_SourceId = "Source Id";
            public static String a_SourceAttribute = "Source Attribute";
            public static String a_TargetId = "Target Id";
            public static String a_LastReadout = "Last Readout";
        }

        public interface OPCUAChannel {
            public static String name = "OPC UA Channel";

            public static String a_TargetID = "Target ID";
            public static String a_NodeID = "Node ID";
            public static String a_FunctionNodeID = "Function Node ID";
            public static String a_FunctionInterval = "Function Interval";
            public static String a_StatusLog = "Status Log";
            public static String a_LastReadout = "Last Readout";
        }
    }

    public interface Constants {
        public static String name = "Constants";

        public static String a_Attribute = "Attribute";
        public static String a_Editable = "Editable";
        public static String a_Entries = "Entries";
    }

    public interface ControlCenter {
        public static String name = "Control Center";

    }

    public interface ControlCenterPlugin {
        public static String name = "Control Center Plugin";

        public static String a_Enable = "Enable";

        public interface UnitPlugin {
            public static String name = "Unit Plugin";

            public static String a_Enable = "Enable";
        }

        public interface EquipmentPlugin {
            public static String name = "Equipment Plugin";

            public static String a_Enable = "Enable";
        }

        public interface ISO5001BrowserPlugin {
            public static String name = "ISO5001 Browser Plugin";

            public static String a_Enable = "Enable";
        }

        public interface TemplateResultCalculationPlugin {
            public static String name = "Template Result Calculation Plugin";

            public static String a_Enable = "Enable";
        }

        public interface MapPlugin {
            public static String name = "Map Plugin";

            public static String a_Enable = "Enable";
        }

        public interface AccountingPlugin {
            public static String name = "Accounting Plugin";

            public static String a_Enable = "Enable";
        }

        public interface GraphPlugin {
            public static String name = "Graph Plugin";

            public static String a_NumberOfChartsPerAnalysis = "Number of Charts per Analysis";
            public static String a_NumberOfChartsPerScreen = "Number of Charts per Screen";
            public static String a_NumberOfHorizontalPies = "Number of Horizontal Pies";
            public static String a_NumberOfHorizontalTables = "Number of Horizontal Tables";
            public static String a_Enable = "Enable";
        }

        public interface AlarmPlugin {
            public static String name = "Alarm Plugin";

            public static String a_Enable = "Enable";
        }

        public interface NotesPlugin {
            public static String name = "Notes Plugin";

            public static String a_Enable = "Enable";
        }

        public interface ClassPlugin {
            public static String name = "Class Plugin";

            public static String a_Enable = "Enable";
        }

        public interface DashboardPlugin {
            public static String name = "Dashboard Plugin";

            public static String a_Enable = "Enable";
        }

        public interface MeterPlugin {
            public static String name = "Meter Plugin";

            public static String a_Enable = "Enable";
        }

        public interface ConfigurationPlugin {
            public static String name = "Configuration Plugin";

            public static String a_Enable = "Enable";
        }

        public interface BaseDataPlugin {
            public static String name = "Base Data Plugin";

            public static String a_Enable = "Enable";
        }

        public interface LoytecPlugin {
            public static String name = "Loytec Plugin";

            public static String a_Enable = "Enable";
        }

        public interface ReportPlugin {
            public static String name = "Report Plugin";

            public static String a_Enable = "Enable";
        }
    }

    public interface Converter {
        public static String name = "Converter";

        public interface ZIPConverter {
            public static String name = "ZIP Converter";

            public static String a_Path = "Path";
        }
    }

    public interface DashboardAnalysis {
        public static String name = "Dashboard Analysis";

        public static String a_DataModelFile = "Data Model File";
        public static String a_Background = "Background";
    }

    public interface Data {
        public static String name = "Data";

        public static String a_Value = "Value";
        public static String a_Period = "Period";

        public interface CleanData {
            public static String name = "Clean Data";

            public static String a_Value = "Value";
            public static String a_Period = "Period";
            public static String a_Enabled = "Enabled";
            public static String a_ValueMultiplier = "Value Multiplier";
            public static String a_ValueOffset = "Value Offset";
            public static String a_ValueIsAQuantity = "Value is a Quantity";
            public static String a_CounterOverflow = "Counter Overflow";
            public static String a_ConversionToDifferential = "Conversion to Differential";
            public static String a_PeriodAlignment = "Period Alignment";
            public static String a_PeriodOffset = "Period Offset";
            public static String a_LimitsEnabled = "Limits Enabled";
            public static String a_LimitsConfiguration = "Limits Configuration";
            public static String a_GapFillingEnabled = "GapFilling Enabled";
            public static String a_GapFillingConfig = "Gap Filling Config";
            public static String a_DeltaEnabled = "Delta Enabled";
            public static String a_DeltaConfig = "Delta Config";
            public static String a_AlarmEnabled = "Alarm Enabled";
            public static String a_AlarmConfig = "Alarm Config";
            public static String a_AlarmLog = "Alarm Log";
        }

        public interface BaseData {
            public static String name = "Base Data";

            public static String a_Value = "Value";
            public static String a_Period = "Period";
        }

        public interface ForecastData {
            public static String name = "Forecast Data";

            public static String a_Enabled = "Enabled";
            public static String a_Type = "Type";
            public static String a_ReferencePeriod = "Reference Period";
            public static String a_ReferencePeriodCount = "Reference Period Count";
            public static String a_BindToSpecific = "Bind To Specific";
            public static String a_ForecastDuration = "Forecast Duration";
            public static String a_ForecastDurationCount = "Forecast Duration Count";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Timezone = "Timezone";
            public static String a_Value = "Value";
            public static String a_Period = "Period";
        }

        public interface MathData {
            public static String name = "Math Data";

            public static String a_Enabled = "Enabled";
            public static String a_Manipulation = "Manipulation";
            public static String a_Formula = "Formula";
            public static String a_ReferencePeriod = "Reference Period";
            public static String a_ReferencePeriodCount = "Reference Period Count";
            public static String a_Beginning = "Beginning";
            public static String a_Ending = "Ending";
            public static String a_LastRun = "Last Run";
            public static String a_PeriodOffset = "Period Offset";
            public static String a_FillPeriod = "Fill Period";
            public static String a_Timezone = "Timezone";
            public static String a_Value = "Value";
            public static String a_Period = "Period";
        }
    }

    public interface DataNotes {
        public static String name = "Data Notes";

        public static String a_UserNotes = "User Notes";
        public static String a_Tag = "Tag";
        public static String a_User = "User";
    }

    public interface DataPoint {
        public static String name = "Data Point";

        public interface CSVDataPoint {
            public static String name = "CSV Data Point";

            public static String a_MappingIdentifier = "Mapping Identifier";
            public static String a_Target = "Target";
            public static String a_ValueIndex = "Value Index";
        }

        public interface DWDDataPoint {
            public static String name = "DWD Data Point";

            public static String a_City = "City";
            public static String a_HeightTarget = "Height Target";
            public static String a_AtmosphericPressureTarget = "Atmospheric Pressure Target";
            public static String a_TemperatureTarget = "Temperature Target";
            public static String a_TemperatureMinTarget = "Temperature Min Target";
            public static String a_TemperatureMaxTarget = "Temperature Max Target";
            public static String a_HumidityTarget = "Humidity Target";
            public static String a_PrecipitationTarget = "Precipitation Target";
            public static String a_Precipitation12Target = "Precipitation12 Target";
            public static String a_SnowHeightTarget = "Snow Height Target";
            public static String a_WindSpeedTarget = "Wind Speed Target";
            public static String a_WindPeaksTarget = "Wind Peaks Target";
            public static String a_WindDirectionTarget = "Wind Direction Target";
            public static String a_ClimateAndCloudsTarget = "Climate and Clouds Target";
            public static String a_SquallTarget = "Squall Target";
            public static String a_Station = "Station";
            public static String a_HeatingDegreeDaysTarget = "Heating Degree Days Target";
        }

        public interface SQLDataPoint {
            public static String name = "SQL Data Point";

            public static String a_TargetAttribute = "Target Attribute";
            public static String a_TargetID = "Target ID";
            public static String a_TimestampColumn = "Timestamp Column";
            public static String a_TimestampType = "Timestamp Type";
            public static String a_ValueColumn = "Value Column";
            public static String a_ValueType = "Value Type";
        }

        public interface XMLDataPoint {
            public static String name = "XML Data Point";

            public static String a_MappingIdentifier = "Mapping Identifier";
            public static String a_Target = "Target";
            public static String a_ValueIdentifier = "Value Identifier";
        }

        public interface DWDHDDDataPoint {
            public static String name = "DWD HDD Data Point";

            public static String a_Station = "Station";
            public static String a_HeatingDegreeDaysTarget = "Heating Degree Days Target";
        }
    }

    public interface DataSource {
        public static String name = "Data Source";

        public static String a_Enabled = "Enabled";
        public static String a_Timezone = "Timezone";
        public static String a_LastRun = "Last Run";
        public static String a_CycleTime = "Cycle Time";
        public static String a_LatestReported = "Latest reported";

        public interface DataServer {
            public static String name = "Data Server";

            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";

            public interface EMailServer {
                public static String name = "EMail Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";

                public interface IMAPEMailServer {
                    public static String name = "IMAP EMail Server";

                    public static String a_Folder = "Folder";
                    public static String a_Password = "Password";
                    public static String a_SSL = "SSL";
                    public static String a_User = "User";
                    public static String a_Host = "Host";
                    public static String a_Port = "Port";
                    public static String a_ReadTimeout = "Read Timeout";
                    public static String a_ManualTrigger = "Manual Trigger";
                    public static String a_ConnectionTimeout = "Connection Timeout";
                    public static String a_MaxThreadTime = "Max thread time";
                    public static String a_Overwrite = "Overwrite";
                    public static String a_Enabled = "Enabled";
                    public static String a_Timezone = "Timezone";
                    public static String a_LastRun = "Last Run";
                    public static String a_CycleTime = "Cycle Time";
                    public static String a_LatestReported = "Latest reported";
                }

                public interface POP3EMailServer {
                    public static String name = "POP3 EMail Server";

                    public static String a_Password = "Password";
                    public static String a_SSL = "SSL";
                    public static String a_User = "User";
                    public static String a_Host = "Host";
                    public static String a_Port = "Port";
                    public static String a_ReadTimeout = "Read Timeout";
                    public static String a_ManualTrigger = "Manual Trigger";
                    public static String a_ConnectionTimeout = "Connection Timeout";
                    public static String a_MaxThreadTime = "Max thread time";
                    public static String a_Overwrite = "Overwrite";
                    public static String a_Enabled = "Enabled";
                    public static String a_Timezone = "Timezone";
                    public static String a_LastRun = "Last Run";
                    public static String a_CycleTime = "Cycle Time";
                    public static String a_LatestReported = "Latest reported";
                }
            }

            public interface IMAPEMailServer {
                public static String name = "IMAP EMail Server";

                public static String a_Folder = "Folder";
                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface POP3EMailServer {
                public static String name = "POP3 EMail Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface FTPServer {
                public static String name = "FTP Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String i_ConnectionTimeout = "Connection Timeout";
                public static String a_DeleteFileOnSuccessfulParsing = "Delete File on successful parsing";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface HTTPServer {
                public static String name = "HTTP Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_Authentication = "Authentication";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface LoytecXMLDLServer {
                public static String name = "Loytec XML-DL Server";

                public static String a_User = "User";
                public static String a_Password = "Password";
                public static String a_LogHandleBasePath = "LogHandle Base Path";
                public static String a_SSL = "SSL";
                public static String i_ConnectionTimeout = "Connection Timeout";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface SFTPServer {
                public static String name = "sFTP Server";

                public static String a_Password = "Password";
                public static String a_User = "User";
                public static String i_ConnectionTimeout = "Connection Timeout";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface SOAPServer {
                public static String name = "SOAP Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String i_ConnectionTimeout = "Connection Timeout";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface VIDA350 {
                public static String name = "VIDA350";

                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface JEVisServer {
                public static String name = "JEVis Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String i_ConnectionTimeout = "Connection Timeout";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface OPCDAServer {
                public static String name = "OPC DA Server";

                public static String a_User = "User";
                public static String a_Password = "Password";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface ProfiNET {
                public static String name = "ProfiNET";

                public static String a_User = "User";
                public static String a_Password = "Password";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface OPCUAServer {
                public static String name = "OPC UA Server";

                public static String a_User = "User";
                public static String a_Password = "Password";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }
        }

        public interface EMailServer {
            public static String name = "EMail Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";

            public interface IMAPEMailServer {
                public static String name = "IMAP EMail Server";

                public static String a_Folder = "Folder";
                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }

            public interface POP3EMailServer {
                public static String name = "POP3 EMail Server";

                public static String a_Password = "Password";
                public static String a_SSL = "SSL";
                public static String a_User = "User";
                public static String a_Host = "Host";
                public static String a_Port = "Port";
                public static String a_ReadTimeout = "Read Timeout";
                public static String a_ManualTrigger = "Manual Trigger";
                public static String a_ConnectionTimeout = "Connection Timeout";
                public static String a_MaxThreadTime = "Max thread time";
                public static String a_Overwrite = "Overwrite";
                public static String a_Enabled = "Enabled";
                public static String a_Timezone = "Timezone";
                public static String a_LastRun = "Last Run";
                public static String a_CycleTime = "Cycle Time";
                public static String a_LatestReported = "Latest reported";
            }
        }

        public interface IMAPEMailServer {
            public static String name = "IMAP EMail Server";

            public static String a_Folder = "Folder";
            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface POP3EMailServer {
            public static String name = "POP3 EMail Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface FTPServer {
            public static String name = "FTP Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String i_ConnectionTimeout = "Connection Timeout";
            public static String a_DeleteFileOnSuccessfulParsing = "Delete File on successful parsing";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface HTTPServer {
            public static String name = "HTTP Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_Authentication = "Authentication";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface LoytecXMLDLServer {
            public static String name = "Loytec XML-DL Server";

            public static String a_User = "User";
            public static String a_Password = "Password";
            public static String a_LogHandleBasePath = "LogHandle Base Path";
            public static String a_SSL = "SSL";
            public static String i_ConnectionTimeout = "Connection Timeout";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface SFTPServer {
            public static String name = "sFTP Server";

            public static String a_Password = "Password";
            public static String a_User = "User";
            public static String i_ConnectionTimeout = "Connection Timeout";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface SOAPServer {
            public static String name = "SOAP Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String i_ConnectionTimeout = "Connection Timeout";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface VIDA350 {
            public static String name = "VIDA350";

            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface JEVisServer {
            public static String name = "JEVis Server";

            public static String a_Password = "Password";
            public static String a_SSL = "SSL";
            public static String a_User = "User";
            public static String i_ConnectionTimeout = "Connection Timeout";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface OPCDAServer {
            public static String name = "OPC DA Server";

            public static String a_User = "User";
            public static String a_Password = "Password";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface ProfiNET {
            public static String name = "ProfiNET";

            public static String a_User = "User";
            public static String a_Password = "Password";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }

        public interface OPCUAServer {
            public static String name = "OPC UA Server";

            public static String a_User = "User";
            public static String a_Password = "Password";
            public static String a_Host = "Host";
            public static String a_Port = "Port";
            public static String a_ReadTimeout = "Read Timeout";
            public static String a_ManualTrigger = "Manual Trigger";
            public static String a_ConnectionTimeout = "Connection Timeout";
            public static String a_MaxThreadTime = "Max thread time";
            public static String a_Overwrite = "Overwrite";
            public static String a_Enabled = "Enabled";
            public static String a_Timezone = "Timezone";
            public static String a_LastRun = "Last Run";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LatestReported = "Latest reported";
        }
    }

    public interface Directory {
        public static String name = "Directory";

        public interface AdministrationDirectory {
            public static String name = "Administration Directory";

        }

        public interface AlarmDirectory {
            public static String name = "Alarm Directory";

        }

        public interface ChannelDirectory {
            public static String name = "Channel Directory";

            public interface EMailChannelDirectory {
                public static String name = "EMail Channel Directory";

            }

            public interface FTPChannelDirectory {
                public static String name = "FTP Channel Directory";

            }

            public interface HTTPChannelDirectory {
                public static String name = "HTTP Channel Directory";

            }

            public interface LoytecXMLDLChannelDirectory {
                public static String name = "Loytec XML-DL Channel Directory";

                public interface LoytecXMLDLCEA709ChannelDirectory {
                    public static String name = "Loytec XML-DL CEA709 Channel Directory";

                }

                public interface LoytecXMLDLGenericChannelDirectory {
                    public static String name = "Loytec XML-DL Generic Channel Directory";

                }

                public interface LoytecXMLDLBacnetChannelDirectory {
                    public static String name = "Loytec XML-DL Bacnet Channel Directory";

                }
            }

            public interface LoytecXMLDLCEA709ChannelDirectory {
                public static String name = "Loytec XML-DL CEA709 Channel Directory";

            }

            public interface LoytecXMLDLGenericChannelDirectory {
                public static String name = "Loytec XML-DL Generic Channel Directory";

            }

            public interface LoytecXMLDLBacnetChannelDirectory {
                public static String name = "Loytec XML-DL Bacnet Channel Directory";

            }

            public interface SFTPChannelDirectory {
                public static String name = "sFTP Channel Directory";

            }

            public interface SOAPChannelDirectory {
                public static String name = "SOAP Channel Directory";

            }

            public interface VIDA350ChannelDirectory {
                public static String name = "VIDA350 Channel Directory";

            }

            public interface OPCUAChannelDirectory {
                public static String name = "OPC UA Channel Directory";

            }

            public interface JEVisChannelDirectory {
                public static String name = "JEVis Channel Directory";

            }
        }

        public interface EMailChannelDirectory {
            public static String name = "EMail Channel Directory";

        }

        public interface FTPChannelDirectory {
            public static String name = "FTP Channel Directory";

        }

        public interface HTTPChannelDirectory {
            public static String name = "HTTP Channel Directory";

        }

        public interface LoytecXMLDLChannelDirectory {
            public static String name = "Loytec XML-DL Channel Directory";

            public interface LoytecXMLDLCEA709ChannelDirectory {
                public static String name = "Loytec XML-DL CEA709 Channel Directory";

            }

            public interface LoytecXMLDLGenericChannelDirectory {
                public static String name = "Loytec XML-DL Generic Channel Directory";

            }

            public interface LoytecXMLDLBacnetChannelDirectory {
                public static String name = "Loytec XML-DL Bacnet Channel Directory";

            }
        }

        public interface LoytecXMLDLCEA709ChannelDirectory {
            public static String name = "Loytec XML-DL CEA709 Channel Directory";

        }

        public interface LoytecXMLDLGenericChannelDirectory {
            public static String name = "Loytec XML-DL Generic Channel Directory";

        }

        public interface LoytecXMLDLBacnetChannelDirectory {
            public static String name = "Loytec XML-DL Bacnet Channel Directory";

        }

        public interface SFTPChannelDirectory {
            public static String name = "sFTP Channel Directory";

        }

        public interface SOAPChannelDirectory {
            public static String name = "SOAP Channel Directory";

        }

        public interface VIDA350ChannelDirectory {
            public static String name = "VIDA350 Channel Directory";

        }

        public interface OPCUAChannelDirectory {
            public static String name = "OPC UA Channel Directory";

        }

        public interface JEVisChannelDirectory {
            public static String name = "JEVis Channel Directory";

        }

        public interface DashboardDirectory {
            public static String name = "Dashboard Directory";

        }

        public interface DataDirectory {
            public static String name = "Data Directory";

        }

        public interface DataPointDirectory {
            public static String name = "Data Point Directory";

            public interface CSVDataPointDirectory {
                public static String name = "CSV Data Point Directory";

            }

            public interface DWDDataPointDirectory {
                public static String name = "DWD Data Point Directory";

            }

            public interface XMLDataPointDirectory {
                public static String name = "XML Data Point Directory";

            }
        }

        public interface CSVDataPointDirectory {
            public static String name = "CSV Data Point Directory";

        }

        public interface DWDDataPointDirectory {
            public static String name = "DWD Data Point Directory";

        }

        public interface XMLDataPointDirectory {
            public static String name = "XML Data Point Directory";

        }

        public interface DataSourceDirectory {
            public static String name = "Data Source Directory";

        }

        public interface DriverDirectory {
            public static String name = "Driver Directory";

            public interface ConverterDriverDirectory {
                public static String name = "Converter Driver Directory";

            }

            public interface DataSourceDriverDirectory {
                public static String name = "Data Source Driver Directory";

            }

            public interface ImporterDriverDirectory {
                public static String name = "Importer Driver Directory";

            }

            public interface ParserDriverDirectory {
                public static String name = "Parser Driver Directory";

            }
        }

        public interface ConverterDriverDirectory {
            public static String name = "Converter Driver Directory";

        }

        public interface DataSourceDriverDirectory {
            public static String name = "Data Source Driver Directory";

        }

        public interface ImporterDriverDirectory {
            public static String name = "Importer Driver Directory";

        }

        public interface ParserDriverDirectory {
            public static String name = "Parser Driver Directory";

        }

        public interface EnumDirectory {
            public static String name = "Enum Directory";

        }

        public interface EquipmentDirectory {
            public static String name = "Equipment Directory";

            public interface AirConditionEquipmentDirectory {
                public static String name = "Air Condition Equipment Directory";

            }

            public interface CompressorEquipmentDirectory {
                public static String name = "Compressor Equipment Directory";

            }

            public interface CoolingEquipmentDirectory {
                public static String name = "Cooling Equipment Directory";

            }

            public interface HeatingEquipmentDirectory {
                public static String name = "Heating Equipment Directory";

            }

            public interface LightingEquipmentDirectory {
                public static String name = "Lighting Equipment Directory";

            }

            public interface OfficeEquipmentDirectory {
                public static String name = "Office Equipment Directory";

            }

            public interface PantryEquipmentDirectory {
                public static String name = "Pantry Equipment Directory";

            }

            public interface ProductionEquipmentDirectory {
                public static String name = "Production Equipment Directory";

            }

            public interface VentilationEquipmentDirectory {
                public static String name = "Ventilation Equipment Directory";

            }
        }

        public interface AirConditionEquipmentDirectory {
            public static String name = "Air Condition Equipment Directory";

        }

        public interface CompressorEquipmentDirectory {
            public static String name = "Compressor Equipment Directory";

        }

        public interface CoolingEquipmentDirectory {
            public static String name = "Cooling Equipment Directory";

        }

        public interface HeatingEquipmentDirectory {
            public static String name = "Heating Equipment Directory";

        }

        public interface LightingEquipmentDirectory {
            public static String name = "Lighting Equipment Directory";

        }

        public interface OfficeEquipmentDirectory {
            public static String name = "Office Equipment Directory";

        }

        public interface PantryEquipmentDirectory {
            public static String name = "Pantry Equipment Directory";

        }

        public interface ProductionEquipmentDirectory {
            public static String name = "Production Equipment Directory";

        }

        public interface VentilationEquipmentDirectory {
            public static String name = "Ventilation Equipment Directory";

        }

        public interface FileDirectory {
            public static String name = "File Directory";

            public interface DocumentDirectory {
                public static String name = "Document Directory";

            }
        }

        public interface DocumentDirectory {
            public static String name = "Document Directory";

        }

        public interface GroupDirectory {
            public static String name = "Group Directory";

        }

        public interface MonitoredObjectDirectory {
            public static String name = "Monitored Object Directory";

        }

        public interface OrganizationDirectory {
            public static String name = "Organization Directory";

        }

        public interface ReportDirectory {
            public static String name = "Report Directory";

        }

        public interface ReportLinkDirectory {
            public static String name = "Report Link Directory";

        }

        public interface ServiceDirectory {
            public static String name = "Service Directory";

        }

        public interface UserDirectory {
            public static String name = "User Directory";

        }

        public interface ViewDirectory {
            public static String name = "View Directory";

        }

        public interface TemplateCalculationDirectory {
            public static String name = "Template Calculation Directory";

        }

        public interface MeasurementDirectory {
            public static String name = "Measurement Directory";

            public interface WaterMeasurementDirectory {
                public static String name = "Water Measurement Directory";

            }

            public interface HeatMeasurementDirectory {
                public static String name = "Heat Measurement Directory";

            }

            public interface GasMeasurementDirectory {
                public static String name = "Gas Measurement Directory";

            }

            public interface CompressedAirMeasurementDirectory {
                public static String name = "Compressed-Air Measurement Directory";

            }

            public interface ElectricityMeasurementDirectory {
                public static String name = "Electricity Measurement Directory";

            }

            public interface AirMeasurementDirectory {
                public static String name = "Air Measurement Directory";

            }

            public interface NitrogenMeasurementDirectory {
                public static String name = "Nitrogen Measurement Directory";

            }
        }

        public interface WaterMeasurementDirectory {
            public static String name = "Water Measurement Directory";

        }

        public interface HeatMeasurementDirectory {
            public static String name = "Heat Measurement Directory";

        }

        public interface GasMeasurementDirectory {
            public static String name = "Gas Measurement Directory";

        }

        public interface CompressedAirMeasurementDirectory {
            public static String name = "Compressed-Air Measurement Directory";

        }

        public interface ElectricityMeasurementDirectory {
            public static String name = "Electricity Measurement Directory";

        }

        public interface AirMeasurementDirectory {
            public static String name = "Air Measurement Directory";

        }

        public interface NitrogenMeasurementDirectory {
            public static String name = "Nitrogen Measurement Directory";

        }

        public interface BaseDataDirectory {
            public static String name = "Base Data Directory";

        }

        public interface CalendarDirectory {
            public static String name = "Calendar Directory";

            public interface CustomPeriod {
                public static String name = "Custom Period";

                public static String a_Visible = "Visible";
                public static String a_StartReferencePoint = "Start Reference Point";
                public static String a_StartYears = "Start Years";
                public static String a_StartMonths = "Start Months";
                public static String a_StartWeeks = "Start Weeks";
                public static String a_StartDays = "Start Days";
                public static String a_StartHours = "Start Hours";
                public static String a_StartMinutes = "Start Minutes";
                public static String a_StartReferenceObject = "Start Reference Object";
                public static String a_StartInterval = "Start Interval";
                public static String a_EndReferencePoint = "End Reference Point";
                public static String a_EndYears = "End Years";
                public static String a_EndMonths = "End Months";
                public static String a_EndWeeks = "End Weeks";
                public static String a_EndDays = "End Days";
                public static String a_EndHours = "End Hours";
                public static String a_EndMinutes = "End Minutes";
                public static String a_EndReferenceObject = "End Reference Object";
                public static String a_EndInterval = "End Interval";
            }
        }

        public interface CustomPeriod {
            public static String name = "Custom Period";

            public static String a_Visible = "Visible";
            public static String a_StartReferencePoint = "Start Reference Point";
            public static String a_StartYears = "Start Years";
            public static String a_StartMonths = "Start Months";
            public static String a_StartWeeks = "Start Weeks";
            public static String a_StartDays = "Start Days";
            public static String a_StartHours = "Start Hours";
            public static String a_StartMinutes = "Start Minutes";
            public static String a_StartReferenceObject = "Start Reference Object";
            public static String a_StartInterval = "Start Interval";
            public static String a_EndReferencePoint = "End Reference Point";
            public static String a_EndYears = "End Years";
            public static String a_EndMonths = "End Months";
            public static String a_EndWeeks = "End Weeks";
            public static String a_EndDays = "End Days";
            public static String a_EndHours = "End Hours";
            public static String a_EndMinutes = "End Minutes";
            public static String a_EndReferenceObject = "End Reference Object";
            public static String a_EndInterval = "End Interval";
        }

        public interface UserRoleDirectory {
            public static String name = "User Role Directory";

        }

        public interface EnergyInvoicesDirectory {
            public static String name = "Energy Invoices Directory";

        }

        public interface AnalysesDirectory {
            public static String name = "Analyses Directory";

        }

        public interface CalculationDirectory {
            public static String name = "Calculation Directory";

        }

        public interface EnergyContractingDirectory {
            public static String name = "Energy Contracting Directory";

            public interface AccountingConfigurationDirectory {
                public static String name = "Accounting Configuration Directory";

            }

            public interface EnergyContractorDirectory {
                public static String name = "Energy Contractor Directory";

            }

            public interface EnergyGridOperationDirectory {
                public static String name = "Energy Grid Operation Directory";

            }

            public interface EnergyMeteringPointOperationDirectory {
                public static String name = "Energy Metering Point Operation Directory";

            }

            public interface EnergySupplyDirectory {
                public static String name = "Energy Supply Directory";

            }

            public interface EnergyGovernmentalDuesDirectory {
                public static String name = "Energy Governmental Dues Directory";

            }
        }

        public interface AccountingConfigurationDirectory {
            public static String name = "Accounting Configuration Directory";

        }

        public interface EnergyContractorDirectory {
            public static String name = "Energy Contractor Directory";

        }

        public interface EnergyGridOperationDirectory {
            public static String name = "Energy Grid Operation Directory";

        }

        public interface EnergyMeteringPointOperationDirectory {
            public static String name = "Energy Metering Point Operation Directory";

        }

        public interface EnergySupplyDirectory {
            public static String name = "Energy Supply Directory";

        }

        public interface EnergyGovernmentalDuesDirectory {
            public static String name = "Energy Governmental Dues Directory";

        }
    }

    public interface Driver {
        public static String name = "Driver";

        public static String a_Enabled = "Enabled";
        public static String a_JEVisClass = "JEVis Class";
        public static String a_MainClass = "Main Class";
        public static String a_SourceFile = "Source File";

        public interface ConverterDriver {
            public static String name = "Converter Driver";

            public static String a_Enabled = "Enabled";
            public static String a_JEVisClass = "JEVis Class";
            public static String a_MainClass = "Main Class";
            public static String a_SourceFile = "Source File";
        }

        public interface DataSourceDriver {
            public static String name = "Data Source Driver";

            public static String a_Enabled = "Enabled";
            public static String a_JEVisClass = "JEVis Class";
            public static String a_MainClass = "Main Class";
            public static String a_SourceFile = "Source File";
        }

        public interface ImporterDriver {
            public static String name = "Importer Driver";

            public static String a_Enabled = "Enabled";
            public static String a_JEVisClass = "JEVis Class";
            public static String a_MainClass = "Main Class";
            public static String a_SourceFile = "Source File";
        }

        public interface ParserDriver {
            public static String name = "Parser Driver";

            public static String a_Enabled = "Enabled";
            public static String a_JEVisClass = "JEVis Class";
            public static String a_MainClass = "Main Class";
            public static String a_SourceFile = "Source File";
        }
    }

    public interface DynamicChannelPath {
        public static String name = "Dynamic Channel Path";

        public static String a_Match = "Match";
        public static String a_Element = "Element";
    }

    public interface EnergyContractor {
        public static String name = "Energy Contractor";

        public static String a_VendorNumber = "Vendor Number";
        public static String a_EnergyContractorNumber = "Energy Contractor Number";
        public static String a_CustomerNumber = "Customer Number";
        public static String a_Company = "Company";
        public static String a_Address = "Address";
        public static String a_ZipCode = "Zip Code";
        public static String a_City = "City";
        public static String a_ContactName = "Contact Name";
        public static String a_ContactPhone = "Contact phone";
        public static String a_ContactMobile = "Contact mobile";
        public static String a_ContactMail = "Contact mail";

        public interface EnergyMeteringPointOperationContractor {
            public static String name = "Energy Metering Point Operation Contractor";

            public static String a_VendorNumber = "Vendor Number";
            public static String a_EnergyContractorNumber = "Energy Contractor Number";
            public static String a_CustomerNumber = "Customer Number";
            public static String a_Company = "Company";
            public static String a_Address = "Address";
            public static String a_ZipCode = "Zip Code";
            public static String a_City = "City";
            public static String a_ContactName = "Contact Name";
            public static String a_ContactPhone = "Contact phone";
            public static String a_ContactMobile = "Contact mobile";
            public static String a_ContactMail = "Contact mail";
        }

        public interface EnergyGridOperationContractor {
            public static String name = "Energy Grid Operation Contractor";

            public static String a_VendorNumber = "Vendor Number";
            public static String a_EnergyContractorNumber = "Energy Contractor Number";
            public static String a_CustomerNumber = "Customer Number";
            public static String a_Company = "Company";
            public static String a_Address = "Address";
            public static String a_ZipCode = "Zip Code";
            public static String a_City = "City";
            public static String a_ContactName = "Contact Name";
            public static String a_ContactPhone = "Contact phone";
            public static String a_ContactMobile = "Contact mobile";
            public static String a_ContactMail = "Contact mail";
        }

        public interface EnergySupplyContractor {
            public static String name = "Energy Supply Contractor";

            public static String a_VendorNumber = "Vendor Number";
            public static String a_EnergyContractorNumber = "Energy Contractor Number";
            public static String a_CustomerNumber = "Customer Number";
            public static String a_Company = "Company";
            public static String a_Address = "Address";
            public static String a_ZipCode = "Zip Code";
            public static String a_City = "City";
            public static String a_ContactName = "Contact Name";
            public static String a_ContactPhone = "Contact phone";
            public static String a_ContactMobile = "Contact mobile";
            public static String a_ContactMail = "Contact mail";
        }
    }

    public interface EnergyGridOperator {
        public static String name = "Energy Grid Operator";

        public static String a_Contractor = "Contractor";
        public static String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
        public static String a_FranchiseTax = "Franchise Tax";
        public static String a_NetCost = "Net Cost";
        public static String a_EnergyPriceGrid = "Energy Price Grid";
        public static String a_DemandCharge = "Demand Charge";

        public interface CommunityHeatingGridOperator {
            public static String name = "Community Heating Grid Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            public static String a_FranchiseTax = "Franchise Tax";
            public static String a_NetCost = "Net Cost";
            public static String a_EnergyPriceGrid = "Energy Price Grid";
            public static String a_DemandCharge = "Demand Charge";
        }

        public interface ElectricityGridOperator {
            public static String name = "Electricity Grid Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            public static String a_FranchiseTax = "Franchise Tax";
            public static String a_NetCost = "Net Cost";
            public static String a_EnergyPriceGrid = "Energy Price Grid";
            public static String a_DemandCharge = "Demand Charge";
        }

        public interface GasGridOperator {
            public static String name = "Gas Grid Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            public static String a_FranchiseTax = "Franchise Tax";
            public static String a_NetCost = "Net Cost";
            public static String a_EnergyPriceGrid = "Energy Price Grid";
            public static String a_DemandCharge = "Demand Charge";
        }
    }

    public interface EnergyMeteringPointOperator {
        public static String name = "Energy Metering Point Operator";

        public static String a_Contractor = "Contractor";
        public static String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
        public static String a_NetCost = "Net Cost";
        public static String a_AdditionalRelativeCost = "Additional Relative Cost";
        public static String a_AdditionalFixCost = "Additional Fix Cost";

        public interface GasMeteringPointOperator {
            public static String name = "Gas Metering Point Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            public static String a_NetCost = "Net Cost";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }

        public interface CommunityHeatingMeteringPointOperator {
            public static String name = "Community Heating Metering Point Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            public static String a_NetCost = "Net Cost";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }

        public interface ElectricityMeteringPointOperator {
            public static String name = "Electricity Metering Point Operator";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            public static String a_NetCost = "Net Cost";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }
    }

    public interface EnergySupplier {
        public static String name = "Energy Supplier";

        public static String a_Contractor = "Contractor";
        public static String a_AdditionalRelativeCost = "Additional Relative Cost";
        public static String a_AdditionalFixCost = "Additional Fix Cost";

        public interface ElectricitySupplyContractor {
            public static String name = "Electricity Supply Contractor";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyPriceHT = "Energy Price HT";
            public static String a_EnergyPriceNT = "Energy Price NT";
            public static String a_DemandCharge = "Demand Charge";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }

        public interface GasSupplyContractor {
            public static String name = "Gas Supply Contractor";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyPrice = "Energy Price";
            public static String a_DemandCharge = "Demand Charge";
            public static String a_HeatingValue = "Heating Value";
            public static String a_StateNumber = "State Number";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }

        public interface CommunityHeatingSupplyContractor {
            public static String name = "Community Heating Supply Contractor";

            public static String a_Contractor = "Contractor";
            public static String a_EnergyPrice = "Energy Price";
            public static String a_DemandCharge = "Demand Charge";
            public static String a_AdditionalRelativeCost = "Additional Relative Cost";
            public static String a_AdditionalFixCost = "Additional Fix Cost";
        }
    }

    public interface Enum {
        public static String name = "Enum";

        public static String a_JEVisClass = "JEVisClass";
    }

    public interface Export {
        public static String name = "Export";

        public static String a_Enabled = "Enabled";
        public static String a_TimeZone = "Time Zone";

        public interface CSVExport {
            public static String name = "CSV Export";

            public static String a_Separator = "Separator";
            public static String a_Enclosed = "Enclosed";
            public static String a_FileName = "File Name";
            public static String a_Header = "Header";
            public static String a_TimestampFormat = "Timestamp Format";
            public static String a_ExportDate = "Export Date";
            public static String a_StartPeriodOffset = "Start Period Offset";
            public static String a_ExportStatus = "Export Status";
            public static String a_Enabled = "Enabled";
            public static String a_TimeZone = "Time Zone";
        }
    }

    public interface ExportDirectory {
        public static String name = "Export Directory";

    }

    public interface ExportEvent {
        public static String name = "Export Event";

        public interface ExportDataEvent {
            public static String name = "Export Data Event";

            public static String a_Operator = "Operator";
            public static String a_JEVisID = "JEVis ID";
            public static String a_Limit = "Limit";
        }
    }

    public interface ExportLink {
        public static String name = "Export Link";

        public static String a_JEVisID = "JEVis ID";
        public static String a_Optional = "Optional";
        public static String a_TemplateVariableName = "Template Variable Name";

        public interface CSVExportLink {
            public static String name = "CSV Export Link";

            public static String a_ColumnID = "Column ID";
            public static String a_ValueFormat = "Value Format";
            public static String a_JEVisID = "JEVis ID";
            public static String a_Optional = "Optional";
            public static String a_TemplateVariableName = "Template Variable Name";
        }
    }

    public interface File {
        public static String name = "File";

        public static String a_File = "File";

        public interface Document {
            public static String name = "Document";

            public static String a_File = "File";
        }
    }

    public interface GovernmentalDues {
        public static String name = "Governmental Dues";

        public static String a_EnergySource = "Energy Source";
        public static String a_VAT = "VAT";
        public static String a_EnergyTaxGas = "Energy Tax Gas";
        public static String a_EnergyTaxElectricity = "Energy Tax Electricity";
        public static String a_Allocation01 = "Allocation 01";
        public static String a_Allocation02 = "Allocation 02";
        public static String a_Allocation03 = "Allocation 03";
        public static String a_Allocation04 = "Allocation 04";
        public static String a_Allocation05 = "Allocation 05";
        public static String a_Allocation06 = "Allocation 06";
        public static String a_Allocation07 = "Allocation 07";
        public static String a_Allocation08 = "Allocation 08";
        public static String a_Allocation09 = "Allocation 09";
        public static String a_Allocation10 = "Allocation 10";
    }

    public interface Group {
        public static String name = "Group";

    }

    public interface ISO50001 {
        public static String name = "ISO 50001";

        public interface ActionPlanDirectory {
            public static String name = "Action Plan Directory";

        }

        public interface AnnouncementDirectory {
            public static String name = "Announcement Directory";

        }

        public interface Audit {
            public static String name = "Audit";

            public static String a_AuditDate = "Audit Date";
            public static String a_Auditor = "Auditor";

            public interface ExternalAudit {
                public static String name = "External Audit";

                public static String a_Certifier = "Certifier";
                public static String a_ReportFile = "Report File";
                public static String a_AuditDate = "Audit Date";
                public static String a_Auditor = "Auditor";
            }

            public interface InternalAudit {
                public static String name = "Internal Audit";

                public static String a_AuditDate = "Audit Date";
                public static String a_Auditor = "Auditor";
            }
        }

        public interface ExternalAudit {
            public static String name = "External Audit";

            public static String a_Certifier = "Certifier";
            public static String a_ReportFile = "Report File";
            public static String a_AuditDate = "Audit Date";
            public static String a_Auditor = "Auditor";
        }

        public interface InternalAudit {
            public static String name = "Internal Audit";

            public static String a_AuditDate = "Audit Date";
            public static String a_Auditor = "Auditor";
        }

        public interface AuditDirectory {
            public static String name = "Audit Directory";

        }

        public interface AuditQuestion {
            public static String name = "Audit Question";

            public static String a_AuditObservation = "Audit Observation";
            public static String a_Evaluation000Points = "Evaluation 000 points";
            public static String a_Evaluation025Points = "Evaluation 025 points";
            public static String a_Evaluation050Points = "Evaluation 050 points";
            public static String a_Evaluation075Points = "Evaluation 075 points";
            public static String a_Evaluation100Points = "Evaluation 100 points";
            public static String a_NormChapter = "Norm Chapter";
            public static String a_ProposedMeasures = "Proposed Measures";
            public static String a_Question = "Question";
        }

        public interface Documents {
            public static String name = "Documents";

            public static String a_Content = "Content";
            public static String a_CreatedBy = "Created by";
            public static String a_DateOfCreation = "Date of Creation";
            public static String a_DocumentNumber = "Document Number";
            public static String a_ReleaseDate = "Release Date";
            public static String a_ReleasedBy = "Released by";
            public static String a_Title = "Title";
            public static String a_Version = "Version";

            public interface ActionPlan {
                public static String name = "Action Plan";

                public static String a_ActionPlanFile = "Action Plan File";
                public static String a_Participants = "Participants";
                public static String a_Content = "Content";
                public static String a_CreatedBy = "Created by";
                public static String a_DateOfCreation = "Date of Creation";
                public static String a_DocumentNumber = "Document Number";
                public static String a_ReleaseDate = "Release Date";
                public static String a_ReleasedBy = "Released by";
                public static String a_Title = "Title";
                public static String a_Version = "Version";
            }

            public interface Announcement {
                public static String name = "Announcement";

                public static String a_AnnouncementFile = "Announcement File";
                public static String a_Content = "Content";
                public static String a_CreatedBy = "Created by";
                public static String a_DateOfCreation = "Date of Creation";
                public static String a_DocumentNumber = "Document Number";
                public static String a_ReleaseDate = "Release Date";
                public static String a_ReleasedBy = "Released by";
                public static String a_Title = "Title";
                public static String a_Version = "Version";
            }

            public interface ProceduralDocument {
                public static String name = "Procedural Document";

                public static String a_Content = "Content";
                public static String a_CreatedBy = "Created by";
                public static String a_DateOfCreation = "Date of Creation";
                public static String a_DocumentNumber = "Document Number";
                public static String a_ReleaseDate = "Release Date";
                public static String a_ReleasedBy = "Released by";
                public static String a_Title = "Title";
                public static String a_Version = "Version";
            }

            public interface TrainingCourse {
                public static String name = "Training Course";

                public static String a_PresentationFile = "Presentation File";
                public static String a_Content = "Content";
                public static String a_CreatedBy = "Created by";
                public static String a_DateOfCreation = "Date of Creation";
                public static String a_DocumentNumber = "Document Number";
                public static String a_ReleaseDate = "Release Date";
                public static String a_ReleasedBy = "Released by";
                public static String a_Title = "Title";
                public static String a_Version = "Version";
            }
        }

        public interface ActionPlan {
            public static String name = "Action Plan";

            public static String a_ActionPlanFile = "Action Plan File";
            public static String a_Participants = "Participants";
            public static String a_Content = "Content";
            public static String a_CreatedBy = "Created by";
            public static String a_DateOfCreation = "Date of Creation";
            public static String a_DocumentNumber = "Document Number";
            public static String a_ReleaseDate = "Release Date";
            public static String a_ReleasedBy = "Released by";
            public static String a_Title = "Title";
            public static String a_Version = "Version";
        }

        public interface Announcement {
            public static String name = "Announcement";

            public static String a_AnnouncementFile = "Announcement File";
            public static String a_Content = "Content";
            public static String a_CreatedBy = "Created by";
            public static String a_DateOfCreation = "Date of Creation";
            public static String a_DocumentNumber = "Document Number";
            public static String a_ReleaseDate = "Release Date";
            public static String a_ReleasedBy = "Released by";
            public static String a_Title = "Title";
            public static String a_Version = "Version";
        }

        public interface ProceduralDocument {
            public static String name = "Procedural Document";

            public static String a_Content = "Content";
            public static String a_CreatedBy = "Created by";
            public static String a_DateOfCreation = "Date of Creation";
            public static String a_DocumentNumber = "Document Number";
            public static String a_ReleaseDate = "Release Date";
            public static String a_ReleasedBy = "Released by";
            public static String a_Title = "Title";
            public static String a_Version = "Version";
        }

        public interface TrainingCourse {
            public static String name = "Training Course";

            public static String a_PresentationFile = "Presentation File";
            public static String a_Content = "Content";
            public static String a_CreatedBy = "Created by";
            public static String a_DateOfCreation = "Date of Creation";
            public static String a_DocumentNumber = "Document Number";
            public static String a_ReleaseDate = "Release Date";
            public static String a_ReleasedBy = "Released by";
            public static String a_Title = "Title";
            public static String a_Version = "Version";
        }

        public interface DocumentsDirectory {
            public static String name = "Documents Directory";

        }

        public interface EnergyFlowChartDirectory {
            public static String name = "Energy Flow Chart Directory";

            public interface EnergyFlowChart {
                public static String name = "Energy Flow Chart";

                public static String a_CreatedOn = "Created On";
                public static String a_ImageFile = "Image File";
                public static String a_OriginalFile = "Original File";
            }
        }

        public interface EnergyFlowChart {
            public static String name = "Energy Flow Chart";

            public static String a_CreatedOn = "Created On";
            public static String a_ImageFile = "Image File";
            public static String a_OriginalFile = "Original File";
        }

        public interface EnergyPlanning {
            public static String name = "Energy Planning";

        }

        public interface EnergySavingAction {
            public static String name = "Energy Saving Action";

            public static String a_InvestmentCosts = "Investment Costs";
            public static String a_Measure = "Measure";
            public static String a_PaybackTime = "Payback Time";
            public static String a_ReponsiblePerson = "Reponsible Person";
            public static String a_SavingsPotentialCapital = "Savings Potential Capital";
            public static String a_SavingsPotentialCO2 = "Savings Potential CO2";
            public static String a_SavingsPotentialEnergy = "Savings Potential Energy";
        }

        public interface EnergySource {
            public static String name = "Energy Source";

            public static String a_CO2EmissionFactor = "CO2 Emission Factor";
        }

        public interface EnergySourcesDirectory {
            public static String name = "Energy Sources Directory";

        }

        public interface EnergyTeamDirectory {
            public static String name = "Energy Team Directory";

        }

        public interface EvaluatedOutput {
            public static String name = "Evaluated Output";

            public static String a_01January = "01 January";
            public static String a_02February = "02 February";
            public static String a_03March = "03 March";
            public static String a_04April = "04 April";
            public static String a_05May = "05 May";
            public static String a_06June = "06 June";
            public static String a_07July = "07 July";
            public static String a_08August = "08 August";
            public static String a_09September = "09 September";
            public static String a_10October = "10 October";
            public static String a_11November = "11 November";
            public static String a_12December = "12 December";
            public static String a_Year = "Year";
        }

        public interface ImplementedActionsDirectory {
            public static String name = "Implemented Actions Directory";

        }

        public interface InitialContact {
            public static String name = "Initial Contact";

            public static String a_Comment = "Comment";
            public static String a_ContactDate = "Contact Date";
            public static String a_ContactFile = "Contact File";
        }

        public interface ISO50001Directory {
            public static String name = "ISO 50001 Directory";

        }

        public interface ISO50001MeetingsDirectory {
            public static String name = "ISO 50001 Meetings Directory";

        }

        public interface LegalRegulation {
            public static String name = "Legal Regulation";

            public static String a_ContentSummary = "Content Summary";
            public static String a_DateOfReview = "Date of Review";
            public static String a_IssueDate = "Issue Date";
            public static String a_LastAmended = "Last Amended";
            public static String a_RegulationDesignation = "Regulation Designation";
            public static String a_RelevanceToISO50001 = "Relevance to ISO 50001";
            public static String a_SignificanceToTheCompany = "Significance to the Company";
        }

        public interface LegalRegulationDirectory {
            public static String name = "Legal Regulation Directory";

        }

        public interface ManagementManualDirectory {
            public static String name = "Management Manual Directory";

        }

        public interface ManagementReview {
            public static String name = "Management Review";

            public static String a_Content = "Content";
            public static String a_ManagementReviewFile = "Management Review File";
            public static String a_ManagementReviewPDF = "Management Review PDF";
            public static String a_Participants = "Participants";
            public static String a_ReviewDate = "Review Date";
        }

        public interface ManagementReviewDirectory {
            public static String name = "Management Review Directory";

        }

        public interface Meeting {
            public static String name = "Meeting";

            public static String a_ContentOfMeetingAndResults = "Content of Meeting and Results";
            public static String a_MeetingDate = "Meeting Date";
            public static String a_MeetingParticipants = "Meeting Participants";
            public static String a_MeetingTime = "Meeting Time";
            public static String a_MinutesOfMeeting = "Minutes of Meeting";
        }

        public interface MonitoringRegister {
            public static String name = "Monitoring Register";

            public interface MeasuringPointDirectory {
                public static String name = "Measuring Point Directory";

                public interface MeasuringPoint {
                    public static String name = "Measuring Point";

                    public static String a_Comment = "Comment";
                    public static String a_DataPointAssignment = "Data Point Assignment";
                    public static String a_InstallationLocation = "Installation Location";
                    public static String a_Meter = "Meter";
                    public static String a_MonitoringID = "Monitoring ID";
                    public static String a_Name = "Name";
                    public static String a_Photo = "Photo";
                    public static String a_PhysicalProperty = "Physical Property";
                    public static String a_Station = "Station";
                    public static String a_Unit = "Unit";
                }
            }

            public interface MeasuringPoint {
                public static String name = "Measuring Point";

                public static String a_Comment = "Comment";
                public static String a_DataPointAssignment = "Data Point Assignment";
                public static String a_InstallationLocation = "Installation Location";
                public static String a_Meter = "Meter";
                public static String a_MonitoringID = "Monitoring ID";
                public static String a_Name = "Name";
                public static String a_Photo = "Photo";
                public static String a_PhysicalProperty = "Physical Property";
                public static String a_Station = "Station";
                public static String a_Unit = "Unit";
            }

            public interface MeterDirectory {
                public static String name = "Meter Directory";

                public interface Meter {
                    public static String name = "Meter";

                    public static String a_ConversionFactor = "Conversion Factor";
                    public static String a_CurrentTransformer = "Current Transformer";
                    public static String a_InstallationDate = "Installation Date";
                    public static String a_Interface = "Interface";
                    public static String a_Type = "Type";
                    public static String a_VoltageTransformer = "Voltage Transformer";
                }
            }

            public interface Meter {
                public static String name = "Meter";

                public static String a_ConversionFactor = "Conversion Factor";
                public static String a_CurrentTransformer = "Current Transformer";
                public static String a_InstallationDate = "Installation Date";
                public static String a_Interface = "Interface";
                public static String a_Type = "Type";
                public static String a_VoltageTransformer = "Voltage Transformer";
            }

            public interface StationDirectory {
                public static String name = "Station Directory";

                public interface Station {
                    public static String name = "Station";

                    public static String a_DeviceID = "Device ID";
                    public static String a_IPAddress = "IP Address";
                    public static String a_SubNetMask = "SubNet Mask";
                    public static String a_Type = "Type";
                }
            }

            public interface Station {
                public static String name = "Station";

                public static String a_DeviceID = "Device ID";
                public static String a_IPAddress = "IP Address";
                public static String a_SubNetMask = "SubNet Mask";
                public static String a_Type = "Type";
            }
        }

        public interface MeasuringPointDirectory {
            public static String name = "Measuring Point Directory";

            public interface MeasuringPoint {
                public static String name = "Measuring Point";

                public static String a_Comment = "Comment";
                public static String a_DataPointAssignment = "Data Point Assignment";
                public static String a_InstallationLocation = "Installation Location";
                public static String a_Meter = "Meter";
                public static String a_MonitoringID = "Monitoring ID";
                public static String a_Name = "Name";
                public static String a_Photo = "Photo";
                public static String a_PhysicalProperty = "Physical Property";
                public static String a_Station = "Station";
                public static String a_Unit = "Unit";
            }
        }

        public interface MeasuringPoint {
            public static String name = "Measuring Point";

            public static String a_Comment = "Comment";
            public static String a_DataPointAssignment = "Data Point Assignment";
            public static String a_InstallationLocation = "Installation Location";
            public static String a_Meter = "Meter";
            public static String a_MonitoringID = "Monitoring ID";
            public static String a_Name = "Name";
            public static String a_Photo = "Photo";
            public static String a_PhysicalProperty = "Physical Property";
            public static String a_Station = "Station";
            public static String a_Unit = "Unit";
        }

        public interface MeterDirectory {
            public static String name = "Meter Directory";

            public interface Meter {
                public static String name = "Meter";

                public static String a_ConversionFactor = "Conversion Factor";
                public static String a_CurrentTransformer = "Current Transformer";
                public static String a_InstallationDate = "Installation Date";
                public static String a_Interface = "Interface";
                public static String a_Type = "Type";
                public static String a_VoltageTransformer = "Voltage Transformer";
            }
        }

        public interface Meter {
            public static String name = "Meter";

            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_CurrentTransformer = "Current Transformer";
            public static String a_InstallationDate = "Installation Date";
            public static String a_Interface = "Interface";
            public static String a_Type = "Type";
            public static String a_VoltageTransformer = "Voltage Transformer";
        }

        public interface StationDirectory {
            public static String name = "Station Directory";

            public interface Station {
                public static String name = "Station";

                public static String a_DeviceID = "Device ID";
                public static String a_IPAddress = "IP Address";
                public static String a_SubNetMask = "SubNet Mask";
                public static String a_Type = "Type";
            }
        }

        public interface Station {
            public static String name = "Station";

            public static String a_DeviceID = "Device ID";
            public static String a_IPAddress = "IP Address";
            public static String a_SubNetMask = "SubNet Mask";
            public static String a_Type = "Type";
        }

        public interface MonthlyValues {
            public static String name = "Monthly Values";

            public static String a_01January = "01 January";
            public static String a_02February = "02 February";
            public static String a_03March = "03 March";
            public static String a_04April = "04 April";
            public static String a_05May = "05 May";
            public static String a_06June = "06 June";
            public static String a_07July = "07 July";
            public static String a_08August = "08 August";
            public static String a_09September = "09 September";
            public static String a_10October = "10 October";
            public static String a_11November = "11 November";
            public static String a_12December = "12 December";
            public static String a_EnergySupplier = "Energy Supplier";
            public static String a_Year = "Year";

            public interface EnergyBills {
                public static String name = "Energy Bills";

                public static String a_01January = "01 January";
                public static String a_02February = "02 February";
                public static String a_03March = "03 March";
                public static String a_04April = "04 April";
                public static String a_05May = "05 May";
                public static String a_06June = "06 June";
                public static String a_07July = "07 July";
                public static String a_08August = "08 August";
                public static String a_09September = "09 September";
                public static String a_10October = "10 October";
                public static String a_11November = "11 November";
                public static String a_12December = "12 December";
                public static String a_EnergySupplier = "Energy Supplier";
                public static String a_Year = "Year";
            }

            public interface EnergyConsumption {
                public static String name = "Energy Consumption";

                public static String a_01January = "01 January";
                public static String a_02February = "02 February";
                public static String a_03March = "03 March";
                public static String a_04April = "04 April";
                public static String a_05May = "05 May";
                public static String a_06June = "06 June";
                public static String a_07July = "07 July";
                public static String a_08August = "08 August";
                public static String a_09September = "09 September";
                public static String a_10October = "10 October";
                public static String a_11November = "11 November";
                public static String a_12December = "12 December";
                public static String a_EnergySupplier = "Energy Supplier";
                public static String a_Year = "Year";
            }
        }

        public interface EnergyBills {
            public static String name = "Energy Bills";

            public static String a_01January = "01 January";
            public static String a_02February = "02 February";
            public static String a_03March = "03 March";
            public static String a_04April = "04 April";
            public static String a_05May = "05 May";
            public static String a_06June = "06 June";
            public static String a_07July = "07 July";
            public static String a_08August = "08 August";
            public static String a_09September = "09 September";
            public static String a_10October = "10 October";
            public static String a_11November = "11 November";
            public static String a_12December = "12 December";
            public static String a_EnergySupplier = "Energy Supplier";
            public static String a_Year = "Year";
        }

        public interface EnergyConsumption {
            public static String name = "Energy Consumption";

            public static String a_01January = "01 January";
            public static String a_02February = "02 February";
            public static String a_03March = "03 March";
            public static String a_04April = "04 April";
            public static String a_05May = "05 May";
            public static String a_06June = "06 June";
            public static String a_07July = "07 July";
            public static String a_08August = "08 August";
            public static String a_09September = "09 September";
            public static String a_10October = "10 October";
            public static String a_11November = "11 November";
            public static String a_12December = "12 December";
            public static String a_EnergySupplier = "Energy Supplier";
            public static String a_Year = "Year";
        }

        public interface PerformanceDirectory {
            public static String name = "Performance Directory";

        }

        public interface PlannedActionsDirectory {
            public static String name = "Planned Actions Directory";

        }

        public interface ProceduralDocumentsDirectory {
            public static String name = "Procedural Documents Directory";

        }

        public interface Responsibilities {
            public static String name = "Responsibilities";

            public interface EnergyTeam {
                public static String name = "Energy Team";

                public static String a_EMail = "EMail";
                public static String a_Function = "Function";
                public static String a_Name = "Name";
                public static String a_Phone = "Phone";
                public static String a_Surname = "Surname";

                public interface EnergyManager {
                    public static String name = "Energy Manager";

                    public static String a_AppointmentLetter = "Appointment Letter";
                    public static String a_EMail = "EMail";
                    public static String a_Function = "Function";
                    public static String a_Name = "Name";
                    public static String a_Phone = "Phone";
                    public static String a_Surname = "Surname";
                }

                public interface EnergyTeamMember {
                    public static String name = "Energy Team Member";

                    public static String a_EMail = "EMail";
                    public static String a_Function = "Function";
                    public static String a_Name = "Name";
                    public static String a_Phone = "Phone";
                    public static String a_Surname = "Surname";
                }
            }

            public interface EnergyManager {
                public static String name = "Energy Manager";

                public static String a_AppointmentLetter = "Appointment Letter";
                public static String a_EMail = "EMail";
                public static String a_Function = "Function";
                public static String a_Name = "Name";
                public static String a_Phone = "Phone";
                public static String a_Surname = "Surname";
            }

            public interface EnergyTeamMember {
                public static String name = "Energy Team Member";

                public static String a_EMail = "EMail";
                public static String a_Function = "Function";
                public static String a_Name = "Name";
                public static String a_Phone = "Phone";
                public static String a_Surname = "Surname";
            }
        }

        public interface EnergyTeam {
            public static String name = "Energy Team";

            public static String a_EMail = "EMail";
            public static String a_Function = "Function";
            public static String a_Name = "Name";
            public static String a_Phone = "Phone";
            public static String a_Surname = "Surname";

            public interface EnergyManager {
                public static String name = "Energy Manager";

                public static String a_AppointmentLetter = "Appointment Letter";
                public static String a_EMail = "EMail";
                public static String a_Function = "Function";
                public static String a_Name = "Name";
                public static String a_Phone = "Phone";
                public static String a_Surname = "Surname";
            }

            public interface EnergyTeamMember {
                public static String name = "Energy Team Member";

                public static String a_EMail = "EMail";
                public static String a_Function = "Function";
                public static String a_Name = "Name";
                public static String a_Phone = "Phone";
                public static String a_Surname = "Surname";
            }
        }

        public interface EnergyManager {
            public static String name = "Energy Manager";

            public static String a_AppointmentLetter = "Appointment Letter";
            public static String a_EMail = "EMail";
            public static String a_Function = "Function";
            public static String a_Name = "Name";
            public static String a_Phone = "Phone";
            public static String a_Surname = "Surname";
        }

        public interface EnergyTeamMember {
            public static String name = "Energy Team Member";

            public static String a_EMail = "EMail";
            public static String a_Function = "Function";
            public static String a_Name = "Name";
            public static String a_Phone = "Phone";
            public static String a_Surname = "Surname";
        }

        public interface Site {
            public static String name = "Site";

        }

        public interface SuperiorLevelMeetingsDirectory {
            public static String name = "Superior Level Meetings Directory";

        }

        public interface TrainingCourseDirectory {
            public static String name = "Training Course Directory";

        }

        public interface TrainingDirectory {
            public static String name = "Training Directory";

            public interface Training {
                public static String name = "Training";

                public static String a_Participants = "Participants";
                public static String a_Trainer = "Trainer";
                public static String a_TrainingCourse = "Training Course";
                public static String a_TrainingDate = "Training Date";
                public static String a_TrainingTime = "Training Time";
            }
        }

        public interface Training {
            public static String name = "Training";

            public static String a_Participants = "Participants";
            public static String a_Trainer = "Trainer";
            public static String a_TrainingCourse = "Training Course";
            public static String a_TrainingDate = "Training Date";
            public static String a_TrainingTime = "Training Time";
        }
    }

    public interface Input {
        public static String name = "Input";

        public static String a_Identifier = "Identifier";
        public static String a_InputData = "Input Data";
        public static String a_InputDataType = "Input Data Type";
    }

    public interface JENotifierPlugin {
        public static String name = "JENotifier Plugin";

        public interface EMailPlugin {
            public static String name = "EMail Plugin";

            public static String a_Authenticator = "Authenticator";
            public static String a_Password = "Password";
            public static String a_Port = "Port";
            public static String a_SMTPServer = "SMTP Server";
            public static String a_ServerUserName = "Server User Name";
            public static String a_TransportSecurity = "Transport Security";
            public static String a_Default = "Default";
        }
    }

    public interface Link {
        public static String name = "Link";

    }

    public interface MeasurementInstrument {
        public static String name = "Measurement Instrument";

        public static String a_Location = "Location";
        public static String a_Company = "Company";
        public static String a_CostCenter = "Cost Center";
        public static String a_MeterPoint = "Meter Point";
        public static String a_Picture = "Picture";
        public static String a_MeasuringPointID = "Measuring Point ID";
        public static String a_MeasuringPointName = "Measuring Point Name";
        public static String a_SerialNumber = "Serial Number";
        public static String a_Type = "Type";
        public static String a_Datasheet = "Datasheet";
        public static String a_Accuracy = "Accuracy";
        public static String a_ConversionFactor = "Conversion Factor";
        public static String a_InstallationDate = "Installation Date";
        public static String a_VerifiedDate = "Verified Date";
        public static String a_VerificationDate = "Verification Date";
        public static String a_OnlineID = "Online ID";
        public static String a_DeviceIP = "Device IP";
        public static String a_DeviceNumber = "Device Number";
        public static String a_Connection = "Connection";
        public static String a_Remarks = "Remarks";

        public interface HeatMeasurementInstrument {
            public static String name = "Heat Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
            public static String a_SensorFl = "Sensor Fl";
            public static String a_SensorRe = "Sensor Re";
        }

        public interface CompressedAirMeasurementInstrument {
            public static String name = "Compressed-Air Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
        }

        public interface NitrogenMeasurementInstrument {
            public static String name = "Nitrogen Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
        }

        public interface ElectricityMeasurementInstrument {
            public static String name = "Electricity Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_CurrentTransformer = "Current Transformer";
            public static String a_VoltageTransformer = "Voltage Transformer";
        }

        public interface GasMeasurementInstrument {
            public static String name = "Gas Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
        }

        public interface AirMeasurementInstrument {
            public static String name = "Air Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
        }

        public interface WaterMeasurementInstrument {
            public static String name = "Water Measurement Instrument";

            public static String a_Location = "Location";
            public static String a_Company = "Company";
            public static String a_CostCenter = "Cost Center";
            public static String a_MeterPoint = "Meter Point";
            public static String a_Picture = "Picture";
            public static String a_MeasuringPointID = "Measuring Point ID";
            public static String a_MeasuringPointName = "Measuring Point Name";
            public static String a_SerialNumber = "Serial Number";
            public static String a_Type = "Type";
            public static String a_Datasheet = "Datasheet";
            public static String a_Accuracy = "Accuracy";
            public static String a_ConversionFactor = "Conversion Factor";
            public static String a_InstallationDate = "Installation Date";
            public static String a_VerifiedDate = "Verified Date";
            public static String a_VerificationDate = "Verification Date";
            public static String a_OnlineID = "Online ID";
            public static String a_DeviceIP = "Device IP";
            public static String a_DeviceNumber = "Device Number";
            public static String a_Connection = "Connection";
            public static String a_Remarks = "Remarks";
            public static String a_Diameter = "Diameter";
        }
    }

    public interface MonitoredObject {
        public static String name = "Monitored Object";

        public interface Building {
            public static String name = "Building";

            public static String a_Address = "Address";
            public static String a_BuildingArea = "Building Area";
            public static String a_Location = "Location";
            public static String a_YearOfConstruction = "Year of Construction";
            public static String a_WorkdayBeginning = "Workday Beginning";
            public static String a_WorkdayEnd = "Workday End";
            public static String a_Timezone = "Timezone";
            public static String a_Holidays = "Holidays";
            public static String a_CustomHolidays = "Custom Holidays";
        }
    }

    public interface Notification {
        public static String name = "Notification";

        public static String a_SentTime = "Sent Time";

        public interface EMailNotification {
            public static String name = "E-Mail Notification";

            public static String a_Attachments = "Attachments";
            public static String a_BlindCarbonCopys = "Blind Carbon Copys";
            public static String a_CarbonCopys = "Carbon Copys";
            public static String a_Enabled = "Enabled";
            public static String a_HTMLEMail = "HTML E-Mail";
            public static String a_Message = "Message";
            public static String a_Recipients = "Recipients";
            public static String a_Subject = "Subject";
            public static String a_SentTime = "Sent Time";
        }
    }

    public interface Organization {
        public static String name = "Organization";

        public static String a_Address = "Address";
        public static String a_Branche = "Branche";
        public static String a_CompanyLogo = "Company Logo";
        public static String a_CompanyName = "Company Name";
        public static String a_Location = "Location";
        public static String a_Mail = "Mail";
        public static String a_Members = "Members";
        public static String a_Phone = "Phone";
        public static String a_WorkingOrOpeningTime = "Working or Opening Time";
    }

    public interface Output {
        public static String name = "Output";

        public static String a_Output = "Output";
    }

    public interface Parser {
        public static String name = "Parser";

        public interface CSVParser {
            public static String name = "CSV Parser";

            public static String a_Charset = "Charset";
            public static String a_DatapointAlignment = "Datapoint Alignment";
            public static String a_DatapointIndex = "Datapoint Index";
            public static String a_DateFormat = "Date Format";
            public static String a_DateIndex = "Date Index";
            public static String a_DecimalSeparator = "Decimal Separator";
            public static String a_Delimiter = "Delimiter";
            public static String a_NumberOfHeadlines = "Number Of Headlines";
            public static String a_Quote = "Quote";
            public static String a_ThousandSeparator = "Thousand Separator";
            public static String a_TimeFormat = "Time Format";
            public static String a_TimeIndex = "Time Index";

            public interface XLSParser {
                public static String name = "XLS Parser";

                public static String a_Charset = "Charset";
                public static String a_DatapointAlignment = "Datapoint Alignment";
                public static String a_DatapointIndex = "Datapoint Index";
                public static String a_DateFormat = "Date Format";
                public static String a_DateIndex = "Date Index";
                public static String a_DecimalSeparator = "Decimal Separator";
                public static String a_Delimiter = "Delimiter";
                public static String a_NumberOfHeadlines = "Number Of Headlines";
                public static String a_Quote = "Quote";
                public static String a_ThousandSeparator = "Thousand Separator";
                public static String a_TimeFormat = "Time Format";
                public static String a_TimeIndex = "Time Index";
            }
        }

        public interface XLSParser {
            public static String name = "XLS Parser";

            public static String a_Charset = "Charset";
            public static String a_DatapointAlignment = "Datapoint Alignment";
            public static String a_DatapointIndex = "Datapoint Index";
            public static String a_DateFormat = "Date Format";
            public static String a_DateIndex = "Date Index";
            public static String a_DecimalSeparator = "Decimal Separator";
            public static String a_Delimiter = "Delimiter";
            public static String a_NumberOfHeadlines = "Number Of Headlines";
            public static String a_Quote = "Quote";
            public static String a_ThousandSeparator = "Thousand Separator";
            public static String a_TimeFormat = "Time Format";
            public static String a_TimeIndex = "Time Index";
        }

        public interface DWDParser {
            public static String name = "DWD Parser";

            public static String a_Charset = "Charset";
        }

        public interface DWDHDDParser {
            public static String name = "DWD HDD Parser";

            public static String a_Charset = "Charset";
        }

        public interface SQLParser {
            public static String name = "SQL Parser";

        }

        public interface XMLParser {
            public static String name = "XML Parser";

            public static String a_DateAttribute = "Date Attribute";
            public static String a_DateElement = "Date Element";
            public static String a_DateFormat = "Date Format";
            public static String a_DateInElement = "Date in Element";
            public static String a_DecimalSeparator = "Decimal Separator";
            public static String a_MainAttribute = "Main Attribute";
            public static String a_MainElement = "Main Element";
            public static String a_ThousandSeparator = "Thousand Separator";
            public static String a_TimeAttribute = "Time Attribute";
            public static String a_TimeElement = "Time Element";
            public static String a_TimeFormat = "Time Format";
            public static String a_TimeInElement = "Time in Element";
            public static String a_ValueAttribute = "Value Attribute";
            public static String a_ValueElement = "Value Element";
            public static String a_ValueInElement = "Value in Element";
        }
    }

    public interface RecycleBin {
        public static String name = "Recycle Bin";

    }

    public interface Register {
        public static String name = "Register";

        public interface EquipmentRegister {
            public static String name = "Equipment Register";

        }
    }

    public interface Report {
        public static String name = "Report";

        public static String a_Enabled = "Enabled";
        public static String a_LastReport = "Last Report";
        public static String a_LastReportPDF = "Last Report PDF";
        public static String a_PDF = "PDF";
        public static String a_PDFPages = "PDF Pages";
        public static String a_Template = "Template";
        public static String a_TimeZone = "Time Zone";

        public interface PeriodicReport {
            public static String name = "Periodic Report";

            public static String a_AttributeName = "Attribute Name";
            public static String a_ConditionEnabled = "Condition Enabled";
            public static String a_JEVisID = "JEVis ID";
            public static String a_Limit = "Limit";
            public static String a_Operator = "Operator";
            public static String a_Schedule = "Schedule";
            public static String a_CustomScheduleObject = "Custom Schedule Object";
            public static String a_StartRecord = "Start Record";
            public static String a_Enabled = "Enabled";
            public static String a_LastReport = "Last Report";
            public static String a_LastReportPDF = "Last Report PDF";
            public static String a_PDF = "PDF";
            public static String a_PDFPages = "PDF Pages";
            public static String a_Template = "Template";
            public static String a_TimeZone = "Time Zone";

            public interface AutomatedWorkingSheet {
                public static String name = "Automated Working Sheet";

                public static String a_AttributeName = "Attribute Name";
                public static String a_ConditionEnabled = "Condition Enabled";
                public static String a_JEVisID = "JEVis ID";
                public static String a_Limit = "Limit";
                public static String a_Operator = "Operator";
                public static String a_Schedule = "Schedule";
                public static String a_CustomScheduleObject = "Custom Schedule Object";
                public static String a_StartRecord = "Start Record";
                public static String a_Enabled = "Enabled";
                public static String a_LastReport = "Last Report";
                public static String a_LastReportPDF = "Last Report PDF";
                public static String a_PDF = "PDF";
                public static String a_PDFPages = "PDF Pages";
                public static String a_Template = "Template";
                public static String a_TimeZone = "Time Zone";
            }
        }

        public interface AutomatedWorkingSheet {
            public static String name = "Automated Working Sheet";

            public static String a_AttributeName = "Attribute Name";
            public static String a_ConditionEnabled = "Condition Enabled";
            public static String a_JEVisID = "JEVis ID";
            public static String a_Limit = "Limit";
            public static String a_Operator = "Operator";
            public static String a_Schedule = "Schedule";
            public static String a_CustomScheduleObject = "Custom Schedule Object";
            public static String a_StartRecord = "Start Record";
            public static String a_Enabled = "Enabled";
            public static String a_LastReport = "Last Report";
            public static String a_LastReportPDF = "Last Report PDF";
            public static String a_PDF = "PDF";
            public static String a_PDFPages = "PDF Pages";
            public static String a_Template = "Template";
            public static String a_TimeZone = "Time Zone";
        }
    }

    public interface ReportAttribute {
        public static String name = "Report Attribute";

        public static String a_AttributeName = "Attribute Name";
    }

    public interface ReportConfiguration {
        public static String name = "Report Configuration";

        public interface ReportPeriodConfiguration {
            public static String name = "Report Period Configuration";

            public static String a_Aggregation = "Aggregation";
            public static String a_Manipulation = "Manipulation";
            public static String a_Period = "Period";
            public static String a_FixedPeriod = "Fixed Period";
        }
    }

    public interface ReportLink {
        public static String name = "Report Link";

        public static String a_JEVisID = "JEVis ID";
        public static String a_Optional = "Optional";
        public static String a_Calculation = "Calculation";
        public static String a_TemplateVariableName = "Template Variable Name";
    }

    public interface ResultCalculationTemplate {
        public static String name = "Result Calculation Template";

        public static String a_TemplateFile = "Template File";
    }

    public interface Service {
        public static String name = "Service";

        public interface JEAlarm {
            public static String name = "JEAlarm";

            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
        }

        public interface JECalc {
            public static String name = "JECalc";

            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
        }

        public interface JEDataCollector {
            public static String name = "JEDataCollector";

            public static String a_DataSourceTimeout = "Data Source Timeout";
            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
        }

        public interface JEReport {
            public static String name = "JEReport";

            public static String a_NotificationFile = "Notification File";
            public static String a_NotificationID = "Notification ID";
            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
            public static String a_Template = "Template";
        }

        public interface JENotifier {
            public static String name = "JENotifier";

            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
        }

        public interface JEOPCUAWriter {
            public static String name = "JEOPCUAWriter";

            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
        }

        public interface JEDataProcessor {
            public static String name = "JEDataProcessor";

            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_CycleTime = "Cycle Time";
            public static String a_Status = "Status";
            public static String a_ProcessingSize = "Processing Size";
        }

        public interface JEStatus {
            public static String name = "JEStatus";

            public static String a_NotificationFile = "Notification File";
            public static String a_NotificationID = "Notification ID";
            public static String a_Enable = "Enable";
            public static String a_MaxNumberThreads = "Max Number Threads";
            public static String a_User = "User";
            public static String a_Password = "Password";
            public static String a_Tariffs = "Tariffs";
            public static String a_CycleTime = "Cycle Time";
            public static String a_LastRun = "Last Run";
            public static String a_StatusEMail = "Status E-Mail";
            public static String a_StatusLog = "Status Log";
            public static String a_StatusFileLog = "Status File Log";
            public static String a_Status = "Status";
            public static String a_LatestReported = "Latest reported";
        }
    }

    public interface StringData {
        public static String name = "String Data";

        public static String a_Value = "Value";
        public static String a_Period = "Period";
    }

    public interface System {
        public static String name = "System";

        public static String a_DomainName = "Domain Name";
        public static String a_Hostname = "Hostname";
        public static String a_Language = "Language";
        public static String a_LocalIP = "Local IP";
        public static String a_PublicIP = "Public IP";
        public static String a_TimeZone = "TimeZone";
    }

    public interface User {
        public static String name = "User";

        public static String a_Enabled = "Enabled";
        public static String a_SysAdmin = "Sys Admin";
        public static String a_Password = "Password";
        public static String a_EMail = "E-Mail";
        public static String a_LastName = "Last Name";
        public static String a_FirstName = "First Name";
        public static String a_Title = "Title";
        public static String a_Position = "Position";
        public static String a_Phone = "Phone";
        public static String a_Timezone = "Timezone";
        public static String a_StartDashboard = "Start Dashboard";
        public static String a_AnalysisFile = "Analysis File";
        public static String a_Activities = "Activities";
    }

    public interface UserData {
        public static String name = "User Data";

        public static String i_Value = "Value";
        public static String a_Period = "Period";
    }

    public interface UserRole {
        public static String name = "User Role";

        public static String a_Description = "Description";
        public static String a_Enabled = "Enabled";
        public static String a_SysAdmin = "Sys Admin";
        public static String a_EMail = "E-Mail";
        public static String a_LastName = "Last Name";
        public static String a_FirstName = "First Name";
        public static String a_Title = "Title";
        public static String a_Position = "Position";
        public static String a_Phone = "Phone";
        public static String a_Timezone = "Timezone";
        public static String a_StartDashboard = "Start Dashboard";
    }

    public interface Nonconformities {
        public static String name = "Nonconformities";

        public static String a_CustomStatus = "Custom Status";
        public static String a_CustomFields = "Custom Fields";
        public static String a_CustomMedium = "Custom Medium";
        public static String a_EnPI = "EnPI";

        public interface NonconformitiesDirectory {
            public static String name = "Nonconformities Directory";

            public interface Nonconformity {
                public static String name = "Nonconformity";

                public static String a_Data = "Data";


            }
        }
    }
}
