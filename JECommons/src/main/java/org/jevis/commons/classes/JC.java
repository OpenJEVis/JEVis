package org.jevis.commons.classes;

/**
 * References for all JEVisClass and Type names.
 * Created with the JEVisClassPrinter.
 */
public interface JC {


    interface AccountingConfiguration {
        String name = "Accounting Configuration";

        String a_TemplateFile = "Template File";
    }

    interface Alarm {
        String name = "Alarm";

        interface DynamicLimitAlarm {
            String name = "Dynamic Limit Alarm";

            String a_AlarmLog = "Alarm Log";
            String a_Enable = "Enable";
            String a_LimitData = "Limit Data";
            String a_Operator = "Operator";
            String a_Status = "Status";
            String a_Tolerance = "Tolerance";
        }

        interface StaticLimitAlarm {
            String name = "Static Limit Alarm";

            String a_AlarmLog = "Alarm Log";
            String a_Enable = "Enable";
            String a_Limit = "Limit";
            String a_Operator = "Operator";
            String a_Status = "Status";
        }
    }

    interface AlarmConfiguration {
        String name = "Alarm Configuration";

        String a_Enabled = "Enabled";
        String a_DisableLink = "Disable Link";
        String a_AlarmScope = "Alarm Scope";
        String a_AlarmObjects = "Alarm Objects";
        String a_TimeStamp = "Time Stamp";
        String a_AlarmPeriod = "Alarm Period";
        String a_CustomScheduleObject = "Custom Schedule Object";
        String a_Log = "Log";
        String a_LogFile = "Log File";
        String a_AlarmChecked = "Alarm Checked";
    }

    interface AlarmLink {
        String name = "Alarm Link";

        String a_AlarmLink = "Alarm Link";
        String a_TemplateName = "Template Name";
    }

    interface Analysis {
        String name = "Analysis";

        String a_DataModel = "Data Model";
        String a_Charts = "Charts";
        String a_NumberOfChartsPerScreen = "Number of Charts per Screen";
        String a_NumberOfHorizontalPies = "Number of Horizontal Pies";
        String a_NumberOfHorizontalTables = "Number of Horizontal Tables";
        String a_AnalysisFile = "Analysis File";
    }

    interface BuildingEquipment {
        String name = "Building Equipment";

        String a_Manufacturer = "Manufacturer";
        String a_Type = "Type";
        String a_YearOfConstruction = "Year of Construction";
        String a_Number = "Number";
        String a_NominalPower = "Nominal Power";
        String a_StandbyConsumption = "Standby Consumption";
        String a_EnergySource = "Energy Source";
        String a_Productivity = "Productivity";
        String a_MeasuringPoint = "Measuring Point";
        String a_WeightingFactor = "Weighting Factor";
        String a_DailyOperatingHours = "Daily Operating Hours";
        String a_WorkingDays = "Working Days";
        String a_WorkWeeks = "Work Weeks";

        interface AirConditioning {
            String name = "Air Conditioning";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Compressor {
            String name = "Compressor";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Flow = "Flow";
            String a_NormalOperatingPressure = "Normal Operating Pressure";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Cooler {
            String name = "Cooler";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_CoolingCapacity = "Cooling Capacity";
            String a_PerformanceNumberCompressor = "Performance Number Compressor";
            String a_Refrigerant = "Refrigerant";
            String a_ServedArea = "Served Area";
            String a_Remark = "Remark";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Engine {
            String name = "Engine";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_EfficiencyClass = "Efficiency Class";
            String a_PowerFactor = "Power Factor";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Facade {
            String name = "Facade";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_UValueGround = "U Value Ground";
            String a_UValueOuterwall = "U Value Outerwall";
            String a_UValueRoof = "U Value Roof";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Floor {
            String name = "Floor";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Floor = "Floor";
            String a_Usage = "Usage";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface HeatGeneration {
            String name = "Heat Generation";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Domain = "Domain";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface HVACSystemSimplified {
            String name = "HVAC System Simplified";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Lighting {
            String name = "Lighting";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Domain = "Domain";
            String a_BallastPower = "Ballast Power";
            String a_BulbsPerLamp = "Bulbs per Lamp";
            String a_LampPower = "Lamp Power";
            String a_NumberOfLamps = "Number of Lamps";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Occupancy {
            String name = "Occupancy";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Occupancy = "Occupancy";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface OfficeEquipment {
            String name = "Office Equipment";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Sector = "Sector";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface PantryEquipment {
            String name = "Pantry Equipment";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Remark = "Remark";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface PlugLoad {
            String name = "Plug Load";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_PlugLoad = "Plug Load";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface ProductionEquipment {
            String name = "Production Equipment";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Sector = "Sector";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Pump {
            String name = "Pump";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_Domain = "Domain";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface RecoolerOrCondenser {
            String name = "Recooler or Condenser";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_RecoolingCapacity = "Recooling Capacity";
            String a_Remark = "Remark";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }

        interface Ventilation {
            String name = "Ventilation";

            String a_Manufacturer = "Manufacturer";
            String a_Type = "Type";
            String a_YearOfConstruction = "Year of Construction";
            String a_Number = "Number";
            String a_NominalPower = "Nominal Power";
            String a_StandbyConsumption = "Standby Consumption";
            String a_EnergySource = "Energy Source";
            String a_Productivity = "Productivity";
            String a_MeasuringPoint = "Measuring Point";
            String a_WeightingFactor = "Weighting Factor";
            String a_SupplyArea = "Supply Area";
            String a_AirExchangeRate = "Air Exchange Rate";
            String a_EnginePowerFROM = "Engine Power FROM";
            String a_EnginePowerTO = "Engine Power TO";
            String a_Remark = "Remark";
            String a_DailyOperatingHours = "Daily Operating Hours";
            String a_WorkingDays = "Working Days";
            String a_WorkWeeks = "Work Weeks";
        }
    }

    interface Calculation {
        String name = "Calculation";

        String a_Enabled = "Enabled";
        String a_Expression = "Expression";
        String a_DIV0Handling = "DIV0 Handling";
        String a_StaticValue = "Static Value";
        String a_AllZeroValue = "All Zero Value";
    }

    interface Channel {
        String name = "Channel";

        String a_LastReadout = "Last Readout";

        interface EMailChannel {
            String name = "EMail Channel";

            String a_Sender = "Sender";
            String a_Subject = "Subject";
            String a_Filename = "Filename";
            String a_DataInBody = "Data in body";
            String a_LastReadout = "Last Readout";
        }

        interface FTPChannel {
            String name = "FTP Channel";

            String a_Path = "Path";
            String a_LastReadout = "Last Readout";
        }

        interface HTTPChannel {
            String name = "HTTP Channel";

            String a_Path = "Path";
            String a_LastReadout = "Last Readout";

            String a_ParameterConfig = "Parameter Config";
        }

        interface LoytecXMLDLChannel {
            String name = "Loytec XML-DL Channel";

            String a_TargetID = "Target ID";
            String a_TrendID = "Trend ID";
            String a_StatusLog = "Status Log";
            String a_LastReadout = "Last Readout";
        }

        interface SFTPChannel {
            String name = "sFTP Channel";

            String a_Path = "Path";
            String a_LastReadout = "Last Readout";
        }

        interface SOAPChannel {
            String name = "SOAP Channel";

            String a_Path = "Path";
            String a_Template = "Template";
            String a_LastReadout = "Last Readout";
        }

        interface SQLChannel {
            String name = "SQL Channel";

            String a_Query = "Query";
            String a_LastReadout = "Last Readout";
        }

        interface VIDA350Channel {
            String name = "VIDA350 Channel";

            String a_Index = "Index";
            String a_Target = "Target";
            String a_LastReadoutTry = "Last Readout Try";
            String a_LastReadout = "Last Readout";
        }

        interface LoytecXMLDLOutputChannel {
            String name = "Loytec XML-DL Output Channel";

            String a_TargetID = "Target ID";
            String a_OPCID = "OPC ID";
            String a_StatusLog = "Status Log";
            String a_LastReadout = "Last Readout";
        }

        interface JEVisChannel {
            String name = "JEVis Channel";

            String a_SourceId = "Source Id";
            String a_SourceAttribute = "Source Attribute";
            String a_TargetId = "Target Id";
            String a_LastReadout = "Last Readout";
        }

        interface OPCUAChannel {
            String name = "OPC UA Channel";

            String a_TargetID = "Target ID";
            String a_NodeID = "Node ID";
            String a_FunctionNodeID = "Function Node ID";
            String a_FunctionInterval = "Function Interval";
            String a_StatusLog = "Status Log";
            String a_LastReadout = "Last Readout";
        }
    }

    interface Constants {
        String name = "Constants";

        String a_Attribute = "Attribute";
        String a_Editable = "Editable";
        String a_Entries = "Entries";
    }

    interface ControlCenter {
        String name = "Control Center";

    }

    interface ControlCenterPlugin {
        String name = "Control Center Plugin";

        String a_Enable = "Enable";

        interface UnitPlugin {
            String name = "Unit Plugin";

            String a_Enable = "Enable";
        }

        interface EquipmentPlugin {
            String name = "Equipment Plugin";

            String a_Enable = "Enable";
        }

        interface ISO5001BrowserPlugin {
            String name = "ISO5001 Browser Plugin";

            String a_Enable = "Enable";
        }

        interface TemplateResultCalculationPlugin {
            String name = "Template Result Calculation Plugin";

            String a_Enable = "Enable";
        }

        interface MapPlugin {
            String name = "Map Plugin";

            String a_Enable = "Enable";
        }

        interface AccountingPlugin {
            String name = "Accounting Plugin";

            String a_Enable = "Enable";
        }

        interface GraphPlugin {
            String name = "Graph Plugin";

            String a_NumberOfChartsPerAnalysis = "Number of Charts per Analysis";
            String a_NumberOfChartsPerScreen = "Number of Charts per Screen";
            String a_NumberOfHorizontalPies = "Number of Horizontal Pies";
            String a_NumberOfHorizontalTables = "Number of Horizontal Tables";
            String a_Enable = "Enable";
        }

        interface AlarmPlugin {
            String name = "Alarm Plugin";

            String a_Enable = "Enable";
        }

        interface NotesPlugin {
            String name = "Notes Plugin";

            String a_Enable = "Enable";
        }

        interface ClassPlugin {
            String name = "Class Plugin";

            String a_Enable = "Enable";
        }

        interface DashboardPlugin {
            String name = "Dashboard Plugin";

            String a_Enable = "Enable";
        }

        interface MeterPlugin {
            String name = "Meter Plugin";

            String a_Enable = "Enable";
        }

        interface ConfigurationPlugin {
            String name = "Configuration Plugin";

            String a_Enable = "Enable";
        }

        interface BaseDataPlugin {
            String name = "Base Data Plugin";

            String a_Enable = "Enable";
        }

        interface LoytecPlugin {
            String name = "Loytec Plugin";

            String a_Enable = "Enable";
        }

        interface ReportPlugin {
            String name = "Report Plugin";

            String a_Enable = "Enable";
        }
    }

    interface Converter {
        String name = "Converter";

        interface ZIPConverter {
            String name = "ZIP Converter";

            String a_Path = "Path";
        }
    }

    interface DashboardAnalysis {
        String name = "Dashboard Analysis";

        String a_DataModelFile = "Data Model File";
        String a_Background = "Background";
    }

    interface Data {
        String name = "Data";

        String a_Value = "Value";
        String a_Period = "Period";

        interface CleanData {
            String name = "Clean Data";

            String a_Value = "Value";
            String a_Period = "Period";
            String a_Enabled = "Enabled";
            String a_ValueMultiplier = "Value Multiplier";
            String a_ValueOffset = "Value Offset";
            String a_ValueIsAQuantity = "Value is a Quantity";
            String a_CounterOverflow = "Counter Overflow";
            String a_ConversionToDifferential = "Conversion to Differential";
            String a_PeriodAlignment = "Period Alignment";
            String a_PeriodOffset = "Period Offset";
            String a_LimitsEnabled = "Limits Enabled";
            String a_LimitsConfiguration = "Limits Configuration";
            String a_GapFillingEnabled = "GapFilling Enabled";
            String a_GapFillingConfig = "Gap Filling Config";
            String a_DeltaEnabled = "Delta Enabled";
            String a_DeltaConfig = "Delta Config";
            String a_AlarmEnabled = "Alarm Enabled";
            String a_AlarmConfig = "Alarm Config";
            String a_AlarmLog = "Alarm Log";
        }

        interface BaseData {
            String name = "Base Data";

            String a_Value = "Value";
            String a_Period = "Period";
        }

        interface ForecastData {
            String name = "Forecast Data";

            String a_Enabled = "Enabled";
            String a_Type = "Type";
            String a_ReferencePeriod = "Reference Period";
            String a_ReferencePeriodCount = "Reference Period Count";
            String a_BindToSpecific = "Bind To Specific";
            String a_ForecastDuration = "Forecast Duration";
            String a_ForecastDurationCount = "Forecast Duration Count";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_Timezone = "Timezone";
            String a_Value = "Value";
            String a_Period = "Period";
        }

        interface MathData {
            String name = "Math Data";

            String a_Enabled = "Enabled";
            String a_Manipulation = "Manipulation";
            String a_Formula = "Formula";
            String a_ReferencePeriod = "Reference Period";
            String a_ReferencePeriodCount = "Reference Period Count";
            String a_Beginning = "Beginning";
            String a_Ending = "Ending";
            String a_LastRun = "Last Run";
            String a_PeriodOffset = "Period Offset";
            String a_FillPeriod = "Fill Period";
            String a_Timezone = "Timezone";
            String a_Value = "Value";
            String a_Period = "Period";
        }
    }

    interface DataNotes {
        String name = "Data Notes";

        String a_UserNotes = "Value";
        String a_Tag = "Tag";
        String a_User = "User";
    }

    interface DataPoint {
        String name = "Data Point";

        interface CSVDataPoint {
            String name = "CSV Data Point";

            String a_MappingIdentifier = "Mapping Identifier";
            String a_Target = "Target";
            String a_ValueIndex = "Value Index";
        }

        interface DWDDataPoint {
            String name = "DWD Data Point";

            String a_City = "City";
            String a_HeightTarget = "Height Target";
            String a_AtmosphericPressureTarget = "Atmospheric Pressure Target";
            String a_TemperatureTarget = "Temperature Target";
            String a_TemperatureMinTarget = "Temperature Min Target";
            String a_TemperatureMaxTarget = "Temperature Max Target";
            String a_HumidityTarget = "Humidity Target";
            String a_PrecipitationTarget = "Precipitation Target";
            String a_Precipitation12Target = "Precipitation12 Target";
            String a_SnowHeightTarget = "Snow Height Target";
            String a_WindSpeedTarget = "Wind Speed Target";
            String a_WindPeaksTarget = "Wind Peaks Target";
            String a_WindDirectionTarget = "Wind Direction Target";
            String a_ClimateAndCloudsTarget = "Climate and Clouds Target";
            String a_SquallTarget = "Squall Target";
            String a_Station = "Station";
            String a_HeatingDegreeDaysTarget = "Heating Degree Days Target";
        }

        interface SQLDataPoint {
            String name = "SQL Data Point";

            String a_TargetAttribute = "Target Attribute";
            String a_TargetID = "Target ID";
            String a_TimestampColumn = "Timestamp Column";
            String a_TimestampType = "Timestamp Type";
            String a_ValueColumn = "Value Column";
            String a_ValueType = "Value Type";
        }

        interface XMLDataPoint {
            String name = "XML Data Point";

            String a_MappingIdentifier = "Mapping Identifier";
            String a_Target = "Target";
            String a_ValueIdentifier = "Value Identifier";
        }

        interface DWDHDDDataPoint {
            String name = "DWD HDD Data Point";

            String a_Station = "Station";
            String a_HeatingDegreeDaysTarget = "Heating Degree Days Target";
        }
    }

    interface DataSource {
        String name = "Data Source";

        String a_Enabled = "Enabled";
        String a_Timezone = "Timezone";
        String a_LastRun = "Last Run";
        String a_CycleTime = "Cycle Time";
        String a_LatestReported = "Latest reported";

        interface DataServer {
            String name = "Data Server";

            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";

            interface EMailServer {
                String name = "EMail Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";

                interface IMAPEMailServer {
                    String name = "IMAP EMail Server";

                    String a_Folder = "Folder";
                    String a_Password = "Password";
                    String a_SSL = "SSL";
                    String a_User = "User";
                    String a_Host = "Host";
                    String a_Port = "Port";
                    String a_ReadTimeout = "Read Timeout";
                    String a_ManualTrigger = "Manual Trigger";
                    String a_ConnectionTimeout = "Connection Timeout";
                    String a_MaxThreadTime = "Max thread time";
                    String a_Overwrite = "Overwrite";
                    String a_Enabled = "Enabled";
                    String a_Timezone = "Timezone";
                    String a_LastRun = "Last Run";
                    String a_CycleTime = "Cycle Time";
                    String a_LatestReported = "Latest reported";
                }

                interface POP3EMailServer {
                    String name = "POP3 EMail Server";

                    String a_Password = "Password";
                    String a_SSL = "SSL";
                    String a_User = "User";
                    String a_Host = "Host";
                    String a_Port = "Port";
                    String a_ReadTimeout = "Read Timeout";
                    String a_ManualTrigger = "Manual Trigger";
                    String a_ConnectionTimeout = "Connection Timeout";
                    String a_MaxThreadTime = "Max thread time";
                    String a_Overwrite = "Overwrite";
                    String a_Enabled = "Enabled";
                    String a_Timezone = "Timezone";
                    String a_LastRun = "Last Run";
                    String a_CycleTime = "Cycle Time";
                    String a_LatestReported = "Latest reported";
                }
            }

            interface IMAPEMailServer {
                String name = "IMAP EMail Server";

                String a_Folder = "Folder";
                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface POP3EMailServer {
                String name = "POP3 EMail Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface FTPServer {
                String name = "FTP Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String i_ConnectionTimeout = "Connection Timeout";
                String a_DeleteFileOnSuccessfulParsing = "Delete File on successful parsing";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface HTTPServer {
                String name = "HTTP Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_Authentication = "Authentication";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface LoytecXMLDLServer {
                String name = "Loytec XML-DL Server";

                String a_User = "User";
                String a_Password = "Password";
                String a_LogHandleBasePath = "LogHandle Base Path";
                String a_SSL = "SSL";
                String i_ConnectionTimeout = "Connection Timeout";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface SFTPServer {
                String name = "sFTP Server";

                String a_Password = "Password";
                String a_User = "User";
                String i_ConnectionTimeout = "Connection Timeout";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface SOAPServer {
                String name = "SOAP Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String i_ConnectionTimeout = "Connection Timeout";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface VIDA350 {
                String name = "VIDA350";

                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface JEVisServer {
                String name = "JEVis Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String i_ConnectionTimeout = "Connection Timeout";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface OPCDAServer {
                String name = "OPC DA Server";

                String a_User = "User";
                String a_Password = "Password";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface ProfiNET {
                String name = "ProfiNET";

                String a_User = "User";
                String a_Password = "Password";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface OPCUAServer {
                String name = "OPC UA Server";

                String a_User = "User";
                String a_Password = "Password";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }
        }

        interface EMailServer {
            String name = "EMail Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";

            interface IMAPEMailServer {
                String name = "IMAP EMail Server";

                String a_Folder = "Folder";
                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }

            interface POP3EMailServer {
                String name = "POP3 EMail Server";

                String a_Password = "Password";
                String a_SSL = "SSL";
                String a_User = "User";
                String a_Host = "Host";
                String a_Port = "Port";
                String a_ReadTimeout = "Read Timeout";
                String a_ManualTrigger = "Manual Trigger";
                String a_ConnectionTimeout = "Connection Timeout";
                String a_MaxThreadTime = "Max thread time";
                String a_Overwrite = "Overwrite";
                String a_Enabled = "Enabled";
                String a_Timezone = "Timezone";
                String a_LastRun = "Last Run";
                String a_CycleTime = "Cycle Time";
                String a_LatestReported = "Latest reported";
            }
        }

        interface IMAPEMailServer {
            String name = "IMAP EMail Server";

            String a_Folder = "Folder";
            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface POP3EMailServer {
            String name = "POP3 EMail Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface FTPServer {
            String name = "FTP Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String i_ConnectionTimeout = "Connection Timeout";
            String a_DeleteFileOnSuccessfulParsing = "Delete File on successful parsing";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface HTTPServer {
            String name = "HTTP Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_Authentication = "Authentication";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface LoytecXMLDLServer {
            String name = "Loytec XML-DL Server";

            String a_User = "User";
            String a_Password = "Password";
            String a_LogHandleBasePath = "LogHandle Base Path";
            String a_SSL = "SSL";
            String i_ConnectionTimeout = "Connection Timeout";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface SFTPServer {
            String name = "sFTP Server";

            String a_Password = "Password";
            String a_User = "User";
            String i_ConnectionTimeout = "Connection Timeout";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface SOAPServer {
            String name = "SOAP Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String i_ConnectionTimeout = "Connection Timeout";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface VIDA350 {
            String name = "VIDA350";

            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface JEVisServer {
            String name = "JEVis Server";

            String a_Password = "Password";
            String a_SSL = "SSL";
            String a_User = "User";
            String i_ConnectionTimeout = "Connection Timeout";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface OPCDAServer {
            String name = "OPC DA Server";

            String a_User = "User";
            String a_Password = "Password";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface ProfiNET {
            String name = "ProfiNET";

            String a_User = "User";
            String a_Password = "Password";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }

        interface OPCUAServer {
            String name = "OPC UA Server";

            String a_User = "User";
            String a_Password = "Password";
            String a_Host = "Host";
            String a_Port = "Port";
            String a_ReadTimeout = "Read Timeout";
            String a_ManualTrigger = "Manual Trigger";
            String a_ConnectionTimeout = "Connection Timeout";
            String a_MaxThreadTime = "Max thread time";
            String a_Overwrite = "Overwrite";
            String a_Enabled = "Enabled";
            String a_Timezone = "Timezone";
            String a_LastRun = "Last Run";
            String a_CycleTime = "Cycle Time";
            String a_LatestReported = "Latest reported";
        }
    }

    interface Directory {
        String name = "Directory";

        interface AdministrationDirectory {
            String name = "Administration Directory";

        }

        interface AlarmDirectory {
            String name = "Alarm Directory";

        }

        interface ChannelDirectory {
            String name = "Channel Directory";

            interface EMailChannelDirectory {
                String name = "EMail Channel Directory";

            }

            interface FTPChannelDirectory {
                String name = "FTP Channel Directory";

            }

            interface HTTPChannelDirectory {
                String name = "HTTP Channel Directory";

            }

            interface LoytecXMLDLChannelDirectory {
                String name = "Loytec XML-DL Channel Directory";

                interface LoytecXMLDLCEA709ChannelDirectory {
                    String name = "Loytec XML-DL CEA709 Channel Directory";

                }

                interface LoytecXMLDLGenericChannelDirectory {
                    String name = "Loytec XML-DL Generic Channel Directory";

                }

                interface LoytecXMLDLBacnetChannelDirectory {
                    String name = "Loytec XML-DL Bacnet Channel Directory";

                }
            }

            interface LoytecXMLDLCEA709ChannelDirectory {
                String name = "Loytec XML-DL CEA709 Channel Directory";

            }

            interface LoytecXMLDLGenericChannelDirectory {
                String name = "Loytec XML-DL Generic Channel Directory";

            }

            interface LoytecXMLDLBacnetChannelDirectory {
                String name = "Loytec XML-DL Bacnet Channel Directory";

            }

            interface SFTPChannelDirectory {
                String name = "sFTP Channel Directory";

            }

            interface SOAPChannelDirectory {
                String name = "SOAP Channel Directory";

            }

            interface VIDA350ChannelDirectory {
                String name = "VIDA350 Channel Directory";

            }

            interface OPCUAChannelDirectory {
                String name = "OPC UA Channel Directory";

            }

            interface JEVisChannelDirectory {
                String name = "JEVis Channel Directory";

            }
        }

        interface EMailChannelDirectory {
            String name = "EMail Channel Directory";

        }

        interface FTPChannelDirectory {
            String name = "FTP Channel Directory";

        }

        interface HTTPChannelDirectory {
            String name = "HTTP Channel Directory";

        }

        interface LoytecXMLDLChannelDirectory {
            String name = "Loytec XML-DL Channel Directory";

            interface LoytecXMLDLCEA709ChannelDirectory {
                String name = "Loytec XML-DL CEA709 Channel Directory";

            }

            interface LoytecXMLDLGenericChannelDirectory {
                String name = "Loytec XML-DL Generic Channel Directory";

            }

            interface LoytecXMLDLBacnetChannelDirectory {
                String name = "Loytec XML-DL Bacnet Channel Directory";

            }
        }

        interface LoytecXMLDLCEA709ChannelDirectory {
            String name = "Loytec XML-DL CEA709 Channel Directory";

        }

        interface LoytecXMLDLGenericChannelDirectory {
            String name = "Loytec XML-DL Generic Channel Directory";

        }

        interface LoytecXMLDLBacnetChannelDirectory {
            String name = "Loytec XML-DL Bacnet Channel Directory";

        }

        interface SFTPChannelDirectory {
            String name = "sFTP Channel Directory";

        }

        interface SOAPChannelDirectory {
            String name = "SOAP Channel Directory";

        }

        interface VIDA350ChannelDirectory {
            String name = "VIDA350 Channel Directory";

        }

        interface OPCUAChannelDirectory {
            String name = "OPC UA Channel Directory";

        }

        interface JEVisChannelDirectory {
            String name = "JEVis Channel Directory";

        }

        interface DashboardDirectory {
            String name = "Dashboard Directory";

        }

        interface DataDirectory {
            String name = "Data Directory";

        }

        interface DataPointDirectory {
            String name = "Data Point Directory";

            interface CSVDataPointDirectory {
                String name = "CSV Data Point Directory";

            }

            interface DWDDataPointDirectory {
                String name = "DWD Data Point Directory";

            }

            interface XMLDataPointDirectory {
                String name = "XML Data Point Directory";

            }
        }

        interface CSVDataPointDirectory {
            String name = "CSV Data Point Directory";

        }

        interface DWDDataPointDirectory {
            String name = "DWD Data Point Directory";

        }

        interface XMLDataPointDirectory {
            String name = "XML Data Point Directory";

        }

        interface DataSourceDirectory {
            String name = "Data Source Directory";

        }

        interface DriverDirectory {
            String name = "Driver Directory";

            interface ConverterDriverDirectory {
                String name = "Converter Driver Directory";

            }

            interface DataSourceDriverDirectory {
                String name = "Data Source Driver Directory";

            }

            interface ImporterDriverDirectory {
                String name = "Importer Driver Directory";

            }

            interface ParserDriverDirectory {
                String name = "Parser Driver Directory";

            }
        }

        interface ConverterDriverDirectory {
            String name = "Converter Driver Directory";

        }

        interface DataSourceDriverDirectory {
            String name = "Data Source Driver Directory";

        }

        interface ImporterDriverDirectory {
            String name = "Importer Driver Directory";

        }

        interface ParserDriverDirectory {
            String name = "Parser Driver Directory";

        }

        interface EnumDirectory {
            String name = "Enum Directory";

        }

        interface EquipmentDirectory {
            String name = "Equipment Directory";

            interface AirConditionEquipmentDirectory {
                String name = "Air Condition Equipment Directory";

            }

            interface CompressorEquipmentDirectory {
                String name = "Compressor Equipment Directory";

            }

            interface CoolingEquipmentDirectory {
                String name = "Cooling Equipment Directory";

            }

            interface HeatingEquipmentDirectory {
                String name = "Heating Equipment Directory";

            }

            interface LightingEquipmentDirectory {
                String name = "Lighting Equipment Directory";

            }

            interface OfficeEquipmentDirectory {
                String name = "Office Equipment Directory";

            }

            interface PantryEquipmentDirectory {
                String name = "Pantry Equipment Directory";

            }

            interface ProductionEquipmentDirectory {
                String name = "Production Equipment Directory";

            }

            interface VentilationEquipmentDirectory {
                String name = "Ventilation Equipment Directory";

            }
        }

        interface AirConditionEquipmentDirectory {
            String name = "Air Condition Equipment Directory";

        }

        interface CompressorEquipmentDirectory {
            String name = "Compressor Equipment Directory";

        }

        interface CoolingEquipmentDirectory {
            String name = "Cooling Equipment Directory";

        }

        interface HeatingEquipmentDirectory {
            String name = "Heating Equipment Directory";

        }

        interface LightingEquipmentDirectory {
            String name = "Lighting Equipment Directory";

        }

        interface OfficeEquipmentDirectory {
            String name = "Office Equipment Directory";

        }

        interface PantryEquipmentDirectory {
            String name = "Pantry Equipment Directory";

        }

        interface ProductionEquipmentDirectory {
            String name = "Production Equipment Directory";

        }

        interface VentilationEquipmentDirectory {
            String name = "Ventilation Equipment Directory";

        }

        interface FileDirectory {
            String name = "File Directory";

            interface DocumentDirectory {
                String name = "Document Directory";

            }
        }

        interface DocumentDirectory {
            String name = "Document Directory";

        }

        interface GroupDirectory {
            String name = "Group Directory";

        }

        interface MonitoredObjectDirectory {
            String name = "Monitored Object Directory";

        }

        interface OrganizationDirectory {
            String name = "Organization Directory";

        }

        interface ReportDirectory {
            String name = "Report Directory";

        }

        interface ReportLinkDirectory {
            String name = "Report Link Directory";

        }

        interface ServiceDirectory {
            String name = "Service Directory";

        }

        interface UserDirectory {
            String name = "User Directory";

        }

        interface ViewDirectory {
            String name = "View Directory";

        }

        interface TemplateCalculationDirectory {
            String name = "Template Calculation Directory";

        }

        interface MeasurementDirectory {
            String name = "Measurement Directory";

            interface WaterMeasurementDirectory {
                String name = "Water Measurement Directory";

            }

            interface HeatMeasurementDirectory {
                String name = "Heat Measurement Directory";

            }

            interface GasMeasurementDirectory {
                String name = "Gas Measurement Directory";

            }

            interface CompressedAirMeasurementDirectory {
                String name = "Compressed-Air Measurement Directory";

            }

            interface ElectricityMeasurementDirectory {
                String name = "Electricity Measurement Directory";

            }

            interface AirMeasurementDirectory {
                String name = "Air Measurement Directory";

            }

            interface NitrogenMeasurementDirectory {
                String name = "Nitrogen Measurement Directory";

            }
        }

        interface WaterMeasurementDirectory {
            String name = "Water Measurement Directory";

        }

        interface HeatMeasurementDirectory {
            String name = "Heat Measurement Directory";

        }

        interface GasMeasurementDirectory {
            String name = "Gas Measurement Directory";

        }

        interface CompressedAirMeasurementDirectory {
            String name = "Compressed-Air Measurement Directory";

        }

        interface ElectricityMeasurementDirectory {
            String name = "Electricity Measurement Directory";

        }

        interface AirMeasurementDirectory {
            String name = "Air Measurement Directory";

        }

        interface NitrogenMeasurementDirectory {
            String name = "Nitrogen Measurement Directory";

        }

        interface BaseDataDirectory {
            String name = "Base Data Directory";

        }

        interface CalendarDirectory {
            String name = "Calendar Directory";

            interface CustomPeriod {
                String name = "Custom Period";

                String a_Visible = "Visible";
                String a_StartReferencePoint = "Start Reference Point";
                String a_StartYears = "Start Years";
                String a_StartMonths = "Start Months";
                String a_StartWeeks = "Start Weeks";
                String a_StartDays = "Start Days";
                String a_StartHours = "Start Hours";
                String a_StartMinutes = "Start Minutes";
                String a_StartReferenceObject = "Start Reference Object";
                String a_StartInterval = "Start Interval";
                String a_EndReferencePoint = "End Reference Point";
                String a_EndYears = "End Years";
                String a_EndMonths = "End Months";
                String a_EndWeeks = "End Weeks";
                String a_EndDays = "End Days";
                String a_EndHours = "End Hours";
                String a_EndMinutes = "End Minutes";
                String a_EndReferenceObject = "End Reference Object";
                String a_EndInterval = "End Interval";
            }
        }

        interface CustomPeriod {
            String name = "Custom Period";

            String a_Visible = "Visible";
            String a_StartReferencePoint = "Start Reference Point";
            String a_StartYears = "Start Years";
            String a_StartMonths = "Start Months";
            String a_StartWeeks = "Start Weeks";
            String a_StartDays = "Start Days";
            String a_StartHours = "Start Hours";
            String a_StartMinutes = "Start Minutes";
            String a_StartReferenceObject = "Start Reference Object";
            String a_StartInterval = "Start Interval";
            String a_EndReferencePoint = "End Reference Point";
            String a_EndYears = "End Years";
            String a_EndMonths = "End Months";
            String a_EndWeeks = "End Weeks";
            String a_EndDays = "End Days";
            String a_EndHours = "End Hours";
            String a_EndMinutes = "End Minutes";
            String a_EndReferenceObject = "End Reference Object";
            String a_EndInterval = "End Interval";
        }

        interface UserRoleDirectory {
            String name = "User Role Directory";

        }

        interface EnergyInvoicesDirectory {
            String name = "Energy Invoices Directory";

        }

        interface AnalysesDirectory {
            String name = "Analyses Directory";

        }

        interface CalculationDirectory {
            String name = "Calculation Directory";

        }

        interface EnergyContractingDirectory {
            String name = "Energy Contracting Directory";

            interface AccountingConfigurationDirectory {
                String name = "Accounting Configuration Directory";

            }

            interface EnergyContractorDirectory {
                String name = "Energy Contractor Directory";

            }

            interface EnergyGridOperationDirectory {
                String name = "Energy Grid Operation Directory";

            }

            interface EnergyMeteringPointOperationDirectory {
                String name = "Energy Metering Point Operation Directory";

            }

            interface EnergySupplyDirectory {
                String name = "Energy Supply Directory";

            }

            interface EnergyGovernmentalDuesDirectory {
                String name = "Energy Governmental Dues Directory";

            }
        }

        interface AccountingConfigurationDirectory {
            String name = "Accounting Configuration Directory";

        }

        interface EnergyContractorDirectory {
            String name = "Energy Contractor Directory";

        }

        interface EnergyGridOperationDirectory {
            String name = "Energy Grid Operation Directory";

        }

        interface EnergyMeteringPointOperationDirectory {
            String name = "Energy Metering Point Operation Directory";

        }

        interface EnergySupplyDirectory {
            String name = "Energy Supply Directory";

        }

        interface EnergyGovernmentalDuesDirectory {
            String name = "Energy Governmental Dues Directory";

        }
    }

    interface Driver {
        String name = "Driver";

        String a_Enabled = "Enabled";
        String a_JEVisClass = "JEVis Class";
        String a_MainClass = "Main Class";
        String a_SourceFile = "Source File";

        interface ConverterDriver {
            String name = "Converter Driver";

            String a_Enabled = "Enabled";
            String a_JEVisClass = "JEVis Class";
            String a_MainClass = "Main Class";
            String a_SourceFile = "Source File";
        }

        interface DataSourceDriver {
            String name = "Data Source Driver";

            String a_Enabled = "Enabled";
            String a_JEVisClass = "JEVis Class";
            String a_MainClass = "Main Class";
            String a_SourceFile = "Source File";
        }

        interface ImporterDriver {
            String name = "Importer Driver";

            String a_Enabled = "Enabled";
            String a_JEVisClass = "JEVis Class";
            String a_MainClass = "Main Class";
            String a_SourceFile = "Source File";
        }

        interface ParserDriver {
            String name = "Parser Driver";

            String a_Enabled = "Enabled";
            String a_JEVisClass = "JEVis Class";
            String a_MainClass = "Main Class";
            String a_SourceFile = "Source File";
        }
    }

    interface DynamicChannelPath {
        String name = "Dynamic Channel Path";

        String a_Match = "Match";
        String a_Element = "Element";
    }

    interface EnergyContractor {
        String name = "Energy Contractor";

        String a_VendorNumber = "Vendor Number";
        String a_EnergyContractorNumber = "Energy Contractor Number";
        String a_CustomerNumber = "Customer Number";
        String a_Company = "Company";
        String a_Address = "Address";
        String a_ZipCode = "Zip Code";
        String a_City = "City";
        String a_ContactName = "Contact Name";
        String a_ContactPhone = "Contact phone";
        String a_ContactMobile = "Contact mobile";
        String a_ContactMail = "Contact mail";

        interface EnergyMeteringPointOperationContractor {
            String name = "Energy Metering Point Operation Contractor";

            String a_VendorNumber = "Vendor Number";
            String a_EnergyContractorNumber = "Energy Contractor Number";
            String a_CustomerNumber = "Customer Number";
            String a_Company = "Company";
            String a_Address = "Address";
            String a_ZipCode = "Zip Code";
            String a_City = "City";
            String a_ContactName = "Contact Name";
            String a_ContactPhone = "Contact phone";
            String a_ContactMobile = "Contact mobile";
            String a_ContactMail = "Contact mail";
        }

        interface EnergyGridOperationContractor {
            String name = "Energy Grid Operation Contractor";

            String a_VendorNumber = "Vendor Number";
            String a_EnergyContractorNumber = "Energy Contractor Number";
            String a_CustomerNumber = "Customer Number";
            String a_Company = "Company";
            String a_Address = "Address";
            String a_ZipCode = "Zip Code";
            String a_City = "City";
            String a_ContactName = "Contact Name";
            String a_ContactPhone = "Contact phone";
            String a_ContactMobile = "Contact mobile";
            String a_ContactMail = "Contact mail";
        }

        interface EnergySupplyContractor {
            String name = "Energy Supply Contractor";

            String a_VendorNumber = "Vendor Number";
            String a_EnergyContractorNumber = "Energy Contractor Number";
            String a_CustomerNumber = "Customer Number";
            String a_Company = "Company";
            String a_Address = "Address";
            String a_ZipCode = "Zip Code";
            String a_City = "City";
            String a_ContactName = "Contact Name";
            String a_ContactPhone = "Contact phone";
            String a_ContactMobile = "Contact mobile";
            String a_ContactMail = "Contact mail";
        }
    }

    interface EnergyGridOperator {
        String name = "Energy Grid Operator";

        String a_Contractor = "Contractor";
        String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
        String a_FranchiseTax = "Franchise Tax";
        String a_NetCost = "Net Cost";
        String a_EnergyPriceGrid = "Energy Price Grid";
        String a_DemandCharge = "Demand Charge";

        interface CommunityHeatingGridOperator {
            String name = "Community Heating Grid Operator";

            String a_Contractor = "Contractor";
            String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            String a_FranchiseTax = "Franchise Tax";
            String a_NetCost = "Net Cost";
            String a_EnergyPriceGrid = "Energy Price Grid";
            String a_DemandCharge = "Demand Charge";
        }

        interface ElectricityGridOperator {
            String name = "Electricity Grid Operator";

            String a_Contractor = "Contractor";
            String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            String a_FranchiseTax = "Franchise Tax";
            String a_NetCost = "Net Cost";
            String a_EnergyPriceGrid = "Energy Price Grid";
            String a_DemandCharge = "Demand Charge";
        }

        interface GasGridOperator {
            String name = "Gas Grid Operator";

            String a_Contractor = "Contractor";
            String a_EnergyGridOperatorCodeNumber = "Energy Grid Operator Code Number";
            String a_FranchiseTax = "Franchise Tax";
            String a_NetCost = "Net Cost";
            String a_EnergyPriceGrid = "Energy Price Grid";
            String a_DemandCharge = "Demand Charge";
        }
    }

    interface EnergyMeteringPointOperator {
        String name = "Energy Metering Point Operator";

        String a_Contractor = "Contractor";
        String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
        String a_NetCost = "Net Cost";
        String a_AdditionalRelativeCost = "Additional Relative Cost";
        String a_AdditionalFixCost = "Additional Fix Cost";

        interface GasMeteringPointOperator {
            String name = "Gas Metering Point Operator";

            String a_Contractor = "Contractor";
            String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            String a_NetCost = "Net Cost";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }

        interface CommunityHeatingMeteringPointOperator {
            String name = "Community Heating Metering Point Operator";

            String a_Contractor = "Contractor";
            String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            String a_NetCost = "Net Cost";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }

        interface ElectricityMeteringPointOperator {
            String name = "Electricity Metering Point Operator";

            String a_Contractor = "Contractor";
            String a_EnergyMeteringPointOperatorCodeNumber = "Energy Metering Point Operator Code Number";
            String a_NetCost = "Net Cost";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }
    }

    interface EnergySupplier {
        String name = "Energy Supplier";

        String a_Contractor = "Contractor";
        String a_AdditionalRelativeCost = "Additional Relative Cost";
        String a_AdditionalFixCost = "Additional Fix Cost";

        interface ElectricitySupplyContractor {
            String name = "Electricity Supply Contractor";

            String a_Contractor = "Contractor";
            String a_EnergyPriceHT = "Energy Price HT";
            String a_EnergyPriceNT = "Energy Price NT";
            String a_DemandCharge = "Demand Charge";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }

        interface GasSupplyContractor {
            String name = "Gas Supply Contractor";

            String a_Contractor = "Contractor";
            String a_EnergyPrice = "Energy Price";
            String a_DemandCharge = "Demand Charge";
            String a_HeatingValue = "Heating Value";
            String a_StateNumber = "State Number";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }

        interface CommunityHeatingSupplyContractor {
            String name = "Community Heating Supply Contractor";

            String a_Contractor = "Contractor";
            String a_EnergyPrice = "Energy Price";
            String a_DemandCharge = "Demand Charge";
            String a_AdditionalRelativeCost = "Additional Relative Cost";
            String a_AdditionalFixCost = "Additional Fix Cost";
        }
    }

    interface Enum {
        String name = "Enum";

        String a_JEVisClass = "JEVisClass";
    }

    interface Export {
        String name = "Export";

        String a_Enabled = "Enabled";
        String a_TimeZone = "Time Zone";

        interface CSVExport {
            String name = "CSV Export";

            String a_Separator = "Separator";
            String a_Enclosed = "Enclosed";
            String a_FileName = "File Name";
            String a_Header = "Header";
            String a_TimestampFormat = "Timestamp Format";
            String a_ExportDate = "Export Date";
            String a_StartPeriodOffset = "Start Period Offset";
            String a_ExportStatus = "Export Status";
            String a_Enabled = "Enabled";
            String a_TimeZone = "Time Zone";
        }
    }

    interface ExportDirectory {
        String name = "Export Directory";

    }

    interface ExportEvent {
        String name = "Export Event";

        interface ExportDataEvent {
            String name = "Export Data Event";

            String a_Operator = "Operator";
            String a_JEVisID = "JEVis ID";
            String a_Limit = "Limit";
        }
    }

    interface ExportLink {
        String name = "Export Link";

        String a_JEVisID = "JEVis ID";
        String a_Optional = "Optional";
        String a_TemplateVariableName = "Template Variable Name";

        interface CSVExportLink {
            String name = "CSV Export Link";

            String a_ColumnID = "Column ID";
            String a_ValueFormat = "Value Format";
            String a_JEVisID = "JEVis ID";
            String a_Optional = "Optional";
            String a_TemplateVariableName = "Template Variable Name";
        }
    }

    interface File {
        String name = "File";

        String a_File = "File";

        interface Document {
            String name = "Document";

            String a_File = "File";
        }
    }

    interface GovernmentalDues {
        String name = "Governmental Dues";

        String a_EnergySource = "Energy Source";
        String a_VAT = "VAT";
        String a_EnergyTaxGas = "Energy Tax Gas";
        String a_EnergyTaxElectricity = "Energy Tax Electricity";
        String a_Allocation01 = "Allocation 01";
        String a_Allocation02 = "Allocation 02";
        String a_Allocation03 = "Allocation 03";
        String a_Allocation04 = "Allocation 04";
        String a_Allocation05 = "Allocation 05";
        String a_Allocation06 = "Allocation 06";
        String a_Allocation07 = "Allocation 07";
        String a_Allocation08 = "Allocation 08";
        String a_Allocation09 = "Allocation 09";
        String a_Allocation10 = "Allocation 10";
    }

    interface Group {
        String name = "Group";

    }

    interface ISO50001 {
        String name = "ISO 50001";

        interface ActionPlanDirectory {
            String name = "Action Plan Directory";

        }

        interface AnnouncementDirectory {
            String name = "Announcement Directory";

        }

        interface Audit {
            String name = "Audit";

            String a_AuditDate = "Audit Date";
            String a_Auditor = "Auditor";

            interface ExternalAudit {
                String name = "External Audit";

                String a_Certifier = "Certifier";
                String a_ReportFile = "Report File";
                String a_AuditDate = "Audit Date";
                String a_Auditor = "Auditor";
            }

            interface InternalAudit {
                String name = "Internal Audit";

                String a_AuditDate = "Audit Date";
                String a_Auditor = "Auditor";
            }
        }

        interface ExternalAudit {
            String name = "External Audit";

            String a_Certifier = "Certifier";
            String a_ReportFile = "Report File";
            String a_AuditDate = "Audit Date";
            String a_Auditor = "Auditor";
        }

        interface InternalAudit {
            String name = "Internal Audit";

            String a_AuditDate = "Audit Date";
            String a_Auditor = "Auditor";
        }

        interface AuditDirectory {
            String name = "Audit Directory";

        }

        interface AuditQuestion {
            String name = "Audit Question";

            String a_AuditObservation = "Audit Observation";
            String a_Evaluation000Points = "Evaluation 000 points";
            String a_Evaluation025Points = "Evaluation 025 points";
            String a_Evaluation050Points = "Evaluation 050 points";
            String a_Evaluation075Points = "Evaluation 075 points";
            String a_Evaluation100Points = "Evaluation 100 points";
            String a_NormChapter = "Norm Chapter";
            String a_ProposedMeasures = "Proposed Measures";
            String a_Question = "Question";
        }

        interface Documents {
            String name = "Documents";

            String a_Content = "Content";
            String a_CreatedBy = "Created by";
            String a_DateOfCreation = "Date of Creation";
            String a_DocumentNumber = "Document Number";
            String a_ReleaseDate = "Release Date";
            String a_ReleasedBy = "Released by";
            String a_Title = "Title";
            String a_Version = "Version";

            interface ActionPlan {
                String name = "Action Plan";

                String a_ActionPlanFile = "Action Plan File";
                String a_Participants = "Participants";
                String a_Content = "Content";
                String a_CreatedBy = "Created by";
                String a_DateOfCreation = "Date of Creation";
                String a_DocumentNumber = "Document Number";
                String a_ReleaseDate = "Release Date";
                String a_ReleasedBy = "Released by";
                String a_Title = "Title";
                String a_Version = "Version";
            }

            interface Announcement {
                String name = "Announcement";

                String a_AnnouncementFile = "Announcement File";
                String a_Content = "Content";
                String a_CreatedBy = "Created by";
                String a_DateOfCreation = "Date of Creation";
                String a_DocumentNumber = "Document Number";
                String a_ReleaseDate = "Release Date";
                String a_ReleasedBy = "Released by";
                String a_Title = "Title";
                String a_Version = "Version";
            }

            interface ProceduralDocument {
                String name = "Procedural Document";

                String a_Content = "Content";
                String a_CreatedBy = "Created by";
                String a_DateOfCreation = "Date of Creation";
                String a_DocumentNumber = "Document Number";
                String a_ReleaseDate = "Release Date";
                String a_ReleasedBy = "Released by";
                String a_Title = "Title";
                String a_Version = "Version";
            }

            interface TrainingCourse {
                String name = "Training Course";

                String a_PresentationFile = "Presentation File";
                String a_Content = "Content";
                String a_CreatedBy = "Created by";
                String a_DateOfCreation = "Date of Creation";
                String a_DocumentNumber = "Document Number";
                String a_ReleaseDate = "Release Date";
                String a_ReleasedBy = "Released by";
                String a_Title = "Title";
                String a_Version = "Version";
            }
        }

        interface ActionPlan {
            String name = "Action Plan";

            String a_ActionPlanFile = "Action Plan File";
            String a_Participants = "Participants";
            String a_Content = "Content";
            String a_CreatedBy = "Created by";
            String a_DateOfCreation = "Date of Creation";
            String a_DocumentNumber = "Document Number";
            String a_ReleaseDate = "Release Date";
            String a_ReleasedBy = "Released by";
            String a_Title = "Title";
            String a_Version = "Version";
        }

        interface Announcement {
            String name = "Announcement";

            String a_AnnouncementFile = "Announcement File";
            String a_Content = "Content";
            String a_CreatedBy = "Created by";
            String a_DateOfCreation = "Date of Creation";
            String a_DocumentNumber = "Document Number";
            String a_ReleaseDate = "Release Date";
            String a_ReleasedBy = "Released by";
            String a_Title = "Title";
            String a_Version = "Version";
        }

        interface ProceduralDocument {
            String name = "Procedural Document";

            String a_Content = "Content";
            String a_CreatedBy = "Created by";
            String a_DateOfCreation = "Date of Creation";
            String a_DocumentNumber = "Document Number";
            String a_ReleaseDate = "Release Date";
            String a_ReleasedBy = "Released by";
            String a_Title = "Title";
            String a_Version = "Version";
        }

        interface TrainingCourse {
            String name = "Training Course";

            String a_PresentationFile = "Presentation File";
            String a_Content = "Content";
            String a_CreatedBy = "Created by";
            String a_DateOfCreation = "Date of Creation";
            String a_DocumentNumber = "Document Number";
            String a_ReleaseDate = "Release Date";
            String a_ReleasedBy = "Released by";
            String a_Title = "Title";
            String a_Version = "Version";
        }

        interface DocumentsDirectory {
            String name = "Documents Directory";

        }

        interface EnergyFlowChartDirectory {
            String name = "Energy Flow Chart Directory";

            interface EnergyFlowChart {
                String name = "Energy Flow Chart";

                String a_CreatedOn = "Created On";
                String a_ImageFile = "Image File";
                String a_OriginalFile = "Original File";
            }
        }

        interface EnergyFlowChart {
            String name = "Energy Flow Chart";

            String a_CreatedOn = "Created On";
            String a_ImageFile = "Image File";
            String a_OriginalFile = "Original File";
        }

        interface EnergyPlanning {
            String name = "Energy Planning";

        }

        interface EnergySavingAction {
            String name = "Energy Saving Action";

            String a_InvestmentCosts = "Investment Costs";
            String a_Measure = "Measure";
            String a_PaybackTime = "Payback Time";
            String a_ReponsiblePerson = "Reponsible Person";
            String a_SavingsPotentialCapital = "Savings Potential Capital";
            String a_SavingsPotentialCO2 = "Savings Potential CO2";
            String a_SavingsPotentialEnergy = "Savings Potential Energy";
        }

        interface EnergySource {
            String name = "Energy Source";

            String a_CO2EmissionFactor = "CO2 Emission Factor";
        }

        interface EnergySourcesDirectory {
            String name = "Energy Sources Directory";

        }

        interface EnergyTeamDirectory {
            String name = "Energy Team Directory";

        }

        interface EvaluatedOutput {
            String name = "Evaluated Output";

            String a_01January = "01 January";
            String a_02February = "02 February";
            String a_03March = "03 March";
            String a_04April = "04 April";
            String a_05May = "05 May";
            String a_06June = "06 June";
            String a_07July = "07 July";
            String a_08August = "08 August";
            String a_09September = "09 September";
            String a_10October = "10 October";
            String a_11November = "11 November";
            String a_12December = "12 December";
            String a_Year = "Year";
        }

        interface ImplementedActionsDirectory {
            String name = "Implemented Actions Directory";

        }

        interface InitialContact {
            String name = "Initial Contact";

            String a_Comment = "Comment";
            String a_ContactDate = "Contact Date";
            String a_ContactFile = "Contact File";
        }

        interface ISO50001Directory {
            String name = "ISO 50001 Directory";

        }

        interface ISO50001MeetingsDirectory {
            String name = "ISO 50001 Meetings Directory";

        }

        interface LegalRegulation {
            String name = "Legal Regulation";

            String a_ContentSummary = "Content Summary";
            String a_DateOfReview = "Date of Review";
            String a_IssueDate = "Issue Date";
            String a_LastAmended = "Last Amended";
            String a_RegulationDesignation = "Regulation Designation";
            String a_RelevanceToISO50001 = "Relevance to ISO 50001";
            String a_SignificanceToTheCompany = "Significance to the Company";
        }

        interface LegalRegulationDirectory {
            String name = "Legal Regulation Directory";

        }

        interface ManagementManualDirectory {
            String name = "Management Manual Directory";

        }

        interface ManagementReview {
            String name = "Management Review";

            String a_Content = "Content";
            String a_ManagementReviewFile = "Management Review File";
            String a_ManagementReviewPDF = "Management Review PDF";
            String a_Participants = "Participants";
            String a_ReviewDate = "Review Date";
        }

        interface ManagementReviewDirectory {
            String name = "Management Review Directory";

        }

        interface Meeting {
            String name = "Meeting";

            String a_ContentOfMeetingAndResults = "Content of Meeting and Results";
            String a_MeetingDate = "Meeting Date";
            String a_MeetingParticipants = "Meeting Participants";
            String a_MeetingTime = "Meeting Time";
            String a_MinutesOfMeeting = "Minutes of Meeting";
        }

        interface MonitoringRegister {
            String name = "Monitoring Register";

            interface MeasuringPointDirectory {
                String name = "Measuring Point Directory";

                interface MeasuringPoint {
                    String name = "Measuring Point";

                    String a_Comment = "Comment";
                    String a_DataPointAssignment = "Data Point Assignment";
                    String a_InstallationLocation = "Installation Location";
                    String a_Meter = "Meter";
                    String a_MonitoringID = "Monitoring ID";
                    String a_Name = "Name";
                    String a_Photo = "Photo";
                    String a_PhysicalProperty = "Physical Property";
                    String a_Station = "Station";
                    String a_Unit = "Unit";
                }
            }

            interface MeasuringPoint {
                String name = "Measuring Point";

                String a_Comment = "Comment";
                String a_DataPointAssignment = "Data Point Assignment";
                String a_InstallationLocation = "Installation Location";
                String a_Meter = "Meter";
                String a_MonitoringID = "Monitoring ID";
                String a_Name = "Name";
                String a_Photo = "Photo";
                String a_PhysicalProperty = "Physical Property";
                String a_Station = "Station";
                String a_Unit = "Unit";
            }

            interface MeterDirectory {
                String name = "Meter Directory";

                interface Meter {
                    String name = "Meter";

                    String a_ConversionFactor = "Conversion Factor";
                    String a_CurrentTransformer = "Current Transformer";
                    String a_InstallationDate = "Installation Date";
                    String a_Interface = "Interface";
                    String a_Type = "Type";
                    String a_VoltageTransformer = "Voltage Transformer";
                }
            }

            interface Meter {
                String name = "Meter";

                String a_ConversionFactor = "Conversion Factor";
                String a_CurrentTransformer = "Current Transformer";
                String a_InstallationDate = "Installation Date";
                String a_Interface = "Interface";
                String a_Type = "Type";
                String a_VoltageTransformer = "Voltage Transformer";
            }

            interface StationDirectory {
                String name = "Station Directory";

                interface Station {
                    String name = "Station";

                    String a_DeviceID = "Device ID";
                    String a_IPAddress = "IP Address";
                    String a_SubNetMask = "SubNet Mask";
                    String a_Type = "Type";
                }
            }

            interface Station {
                String name = "Station";

                String a_DeviceID = "Device ID";
                String a_IPAddress = "IP Address";
                String a_SubNetMask = "SubNet Mask";
                String a_Type = "Type";
            }
        }

        interface MeasuringPointDirectory {
            String name = "Measuring Point Directory";

            interface MeasuringPoint {
                String name = "Measuring Point";

                String a_Comment = "Comment";
                String a_DataPointAssignment = "Data Point Assignment";
                String a_InstallationLocation = "Installation Location";
                String a_Meter = "Meter";
                String a_MonitoringID = "Monitoring ID";
                String a_Name = "Name";
                String a_Photo = "Photo";
                String a_PhysicalProperty = "Physical Property";
                String a_Station = "Station";
                String a_Unit = "Unit";
            }
        }

        interface MeasuringPoint {
            String name = "Measuring Point";

            String a_Comment = "Comment";
            String a_DataPointAssignment = "Data Point Assignment";
            String a_InstallationLocation = "Installation Location";
            String a_Meter = "Meter";
            String a_MonitoringID = "Monitoring ID";
            String a_Name = "Name";
            String a_Photo = "Photo";
            String a_PhysicalProperty = "Physical Property";
            String a_Station = "Station";
            String a_Unit = "Unit";
        }

        interface MeterDirectory {
            String name = "Meter Directory";

            interface Meter {
                String name = "Meter";

                String a_ConversionFactor = "Conversion Factor";
                String a_CurrentTransformer = "Current Transformer";
                String a_InstallationDate = "Installation Date";
                String a_Interface = "Interface";
                String a_Type = "Type";
                String a_VoltageTransformer = "Voltage Transformer";
            }
        }

        interface Meter {
            String name = "Meter";

            String a_ConversionFactor = "Conversion Factor";
            String a_CurrentTransformer = "Current Transformer";
            String a_InstallationDate = "Installation Date";
            String a_Interface = "Interface";
            String a_Type = "Type";
            String a_VoltageTransformer = "Voltage Transformer";
        }

        interface StationDirectory {
            String name = "Station Directory";

            interface Station {
                String name = "Station";

                String a_DeviceID = "Device ID";
                String a_IPAddress = "IP Address";
                String a_SubNetMask = "SubNet Mask";
                String a_Type = "Type";
            }
        }

        interface Station {
            String name = "Station";

            String a_DeviceID = "Device ID";
            String a_IPAddress = "IP Address";
            String a_SubNetMask = "SubNet Mask";
            String a_Type = "Type";
        }

        interface MonthlyValues {
            String name = "Monthly Values";

            String a_01January = "01 January";
            String a_02February = "02 February";
            String a_03March = "03 March";
            String a_04April = "04 April";
            String a_05May = "05 May";
            String a_06June = "06 June";
            String a_07July = "07 July";
            String a_08August = "08 August";
            String a_09September = "09 September";
            String a_10October = "10 October";
            String a_11November = "11 November";
            String a_12December = "12 December";
            String a_EnergySupplier = "Energy Supplier";
            String a_Year = "Year";

            interface EnergyBills {
                String name = "Energy Bills";

                String a_01January = "01 January";
                String a_02February = "02 February";
                String a_03March = "03 March";
                String a_04April = "04 April";
                String a_05May = "05 May";
                String a_06June = "06 June";
                String a_07July = "07 July";
                String a_08August = "08 August";
                String a_09September = "09 September";
                String a_10October = "10 October";
                String a_11November = "11 November";
                String a_12December = "12 December";
                String a_EnergySupplier = "Energy Supplier";
                String a_Year = "Year";
            }

            interface EnergyConsumption {
                String name = "Energy Consumption";

                String a_01January = "01 January";
                String a_02February = "02 February";
                String a_03March = "03 March";
                String a_04April = "04 April";
                String a_05May = "05 May";
                String a_06June = "06 June";
                String a_07July = "07 July";
                String a_08August = "08 August";
                String a_09September = "09 September";
                String a_10October = "10 October";
                String a_11November = "11 November";
                String a_12December = "12 December";
                String a_EnergySupplier = "Energy Supplier";
                String a_Year = "Year";
            }
        }

        interface EnergyBills {
            String name = "Energy Bills";

            String a_01January = "01 January";
            String a_02February = "02 February";
            String a_03March = "03 March";
            String a_04April = "04 April";
            String a_05May = "05 May";
            String a_06June = "06 June";
            String a_07July = "07 July";
            String a_08August = "08 August";
            String a_09September = "09 September";
            String a_10October = "10 October";
            String a_11November = "11 November";
            String a_12December = "12 December";
            String a_EnergySupplier = "Energy Supplier";
            String a_Year = "Year";
        }

        interface EnergyConsumption {
            String name = "Energy Consumption";

            String a_01January = "01 January";
            String a_02February = "02 February";
            String a_03March = "03 March";
            String a_04April = "04 April";
            String a_05May = "05 May";
            String a_06June = "06 June";
            String a_07July = "07 July";
            String a_08August = "08 August";
            String a_09September = "09 September";
            String a_10October = "10 October";
            String a_11November = "11 November";
            String a_12December = "12 December";
            String a_EnergySupplier = "Energy Supplier";
            String a_Year = "Year";
        }

        interface PerformanceDirectory {
            String name = "Performance Directory";

        }

        interface PlannedActionsDirectory {
            String name = "Planned Actions Directory";

        }

        interface ProceduralDocumentsDirectory {
            String name = "Procedural Documents Directory";

        }

        interface Responsibilities {
            String name = "Responsibilities";

            interface EnergyTeam {
                String name = "Energy Team";

                String a_EMail = "EMail";
                String a_Function = "Function";
                String a_Name = "Name";
                String a_Phone = "Phone";
                String a_Surname = "Surname";

                interface EnergyManager {
                    String name = "Energy Manager";

                    String a_AppointmentLetter = "Appointment Letter";
                    String a_EMail = "EMail";
                    String a_Function = "Function";
                    String a_Name = "Name";
                    String a_Phone = "Phone";
                    String a_Surname = "Surname";
                }

                interface EnergyTeamMember {
                    String name = "Energy Team Member";

                    String a_EMail = "EMail";
                    String a_Function = "Function";
                    String a_Name = "Name";
                    String a_Phone = "Phone";
                    String a_Surname = "Surname";
                }
            }

            interface EnergyManager {
                String name = "Energy Manager";

                String a_AppointmentLetter = "Appointment Letter";
                String a_EMail = "EMail";
                String a_Function = "Function";
                String a_Name = "Name";
                String a_Phone = "Phone";
                String a_Surname = "Surname";
            }

            interface EnergyTeamMember {
                String name = "Energy Team Member";

                String a_EMail = "EMail";
                String a_Function = "Function";
                String a_Name = "Name";
                String a_Phone = "Phone";
                String a_Surname = "Surname";
            }
        }

        interface EnergyTeam {
            String name = "Energy Team";

            String a_EMail = "EMail";
            String a_Function = "Function";
            String a_Name = "Name";
            String a_Phone = "Phone";
            String a_Surname = "Surname";

            interface EnergyManager {
                String name = "Energy Manager";

                String a_AppointmentLetter = "Appointment Letter";
                String a_EMail = "EMail";
                String a_Function = "Function";
                String a_Name = "Name";
                String a_Phone = "Phone";
                String a_Surname = "Surname";
            }

            interface EnergyTeamMember {
                String name = "Energy Team Member";

                String a_EMail = "EMail";
                String a_Function = "Function";
                String a_Name = "Name";
                String a_Phone = "Phone";
                String a_Surname = "Surname";
            }
        }

        interface EnergyManager {
            String name = "Energy Manager";

            String a_AppointmentLetter = "Appointment Letter";
            String a_EMail = "EMail";
            String a_Function = "Function";
            String a_Name = "Name";
            String a_Phone = "Phone";
            String a_Surname = "Surname";
        }

        interface EnergyTeamMember {
            String name = "Energy Team Member";

            String a_EMail = "EMail";
            String a_Function = "Function";
            String a_Name = "Name";
            String a_Phone = "Phone";
            String a_Surname = "Surname";
        }

        interface Site {
            String name = "Site";

        }

        interface SuperiorLevelMeetingsDirectory {
            String name = "Superior Level Meetings Directory";

        }

        interface TrainingCourseDirectory {
            String name = "Training Course Directory";

        }

        interface TrainingDirectory {
            String name = "Training Directory";

            interface Training {
                String name = "Training";

                String a_Participants = "Participants";
                String a_Trainer = "Trainer";
                String a_TrainingCourse = "Training Course";
                String a_TrainingDate = "Training Date";
                String a_TrainingTime = "Training Time";
            }
        }

        interface Training {
            String name = "Training";

            String a_Participants = "Participants";
            String a_Trainer = "Trainer";
            String a_TrainingCourse = "Training Course";
            String a_TrainingDate = "Training Date";
            String a_TrainingTime = "Training Time";
        }
    }

    interface Input {
        String name = "Input";

        String a_Identifier = "Identifier";
        String a_InputData = "Input Data";
        String a_InputDataType = "Input Data Type";
    }

    interface JENotifierPlugin {
        String name = "JENotifier Plugin";

        interface EMailPlugin {
            String name = "EMail Plugin";

            String a_Authenticator = "Authenticator";
            String a_Password = "Password";
            String a_Port = "Port";
            String a_SMTPServer = "SMTP Server";
            String a_ServerUserName = "Server User Name";
            String a_TransportSecurity = "Transport Security";
            String a_Default = "Default";
        }
    }

    interface Link {
        String name = "Link";

    }

    interface MeasurementInstrument {
        String name = "Measurement Instrument";

        String a_Location = "Location";
        String a_Company = "Company";
        String a_CostCenter = "Cost Center";
        String a_MeterPoint = "Meter Point";
        String a_Picture = "Picture";
        String a_MeasuringPointID = "Measuring Point ID";
        String a_MeasuringPointName = "Measuring Point Name";
        String a_SerialNumber = "Serial Number";
        String a_Type = "Type";
        String a_Datasheet = "Datasheet";
        String a_Accuracy = "Accuracy";
        String a_ConversionFactor = "Conversion Factor";
        String a_InstallationDate = "Installation Date";
        String a_VerifiedDate = "Verified Date";
        String a_VerificationDate = "Verification Date";
        String a_OnlineID = "Online ID";
        String a_DeviceIP = "Device IP";
        String a_DeviceNumber = "Device Number";
        String a_Connection = "Connection";
        String a_Remarks = "Remarks";

        interface HeatMeasurementInstrument {
            String name = "Heat Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
            String a_SensorFl = "Sensor Fl";
            String a_SensorRe = "Sensor Re";
        }

        interface CompressedAirMeasurementInstrument {
            String name = "Compressed-Air Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
        }

        interface NitrogenMeasurementInstrument {
            String name = "Nitrogen Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
        }

        interface ElectricityMeasurementInstrument {
            String name = "Electricity Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_CurrentTransformer = "Current Transformer";
            String a_VoltageTransformer = "Voltage Transformer";
        }

        interface GasMeasurementInstrument {
            String name = "Gas Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
        }

        interface AirMeasurementInstrument {
            String name = "Air Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
        }

        interface WaterMeasurementInstrument {
            String name = "Water Measurement Instrument";

            String a_Location = "Location";
            String a_Company = "Company";
            String a_CostCenter = "Cost Center";
            String a_MeterPoint = "Meter Point";
            String a_Picture = "Picture";
            String a_MeasuringPointID = "Measuring Point ID";
            String a_MeasuringPointName = "Measuring Point Name";
            String a_SerialNumber = "Serial Number";
            String a_Type = "Type";
            String a_Datasheet = "Datasheet";
            String a_Accuracy = "Accuracy";
            String a_ConversionFactor = "Conversion Factor";
            String a_InstallationDate = "Installation Date";
            String a_VerifiedDate = "Verified Date";
            String a_VerificationDate = "Verification Date";
            String a_OnlineID = "Online ID";
            String a_DeviceIP = "Device IP";
            String a_DeviceNumber = "Device Number";
            String a_Connection = "Connection";
            String a_Remarks = "Remarks";
            String a_Diameter = "Diameter";
        }
    }

    interface MonitoredObject {
        String name = "Monitored Object";

        interface Building {
            String name = "Building";

            String a_Address = "Address";
            String a_BuildingArea = "Building Area";
            String a_Location = "Location";
            String a_YearOfConstruction = "Year of Construction";
            String a_WorkdayBeginning = "Workday Beginning";
            String a_WorkdayEnd = "Workday End";
            String a_Timezone = "Timezone";
            String a_Holidays = "Holidays";
            String a_CustomHolidays = "Custom Holidays";
        }
    }

    interface Notification {
        String name = "Notification";

        String a_SentTime = "Sent Time";

        interface EMailNotification {
            String name = "E-Mail Notification";

            String a_Attachments = "Attachments";
            String a_BlindCarbonCopys = "Blind Carbon Copys";
            String a_CarbonCopys = "Carbon Copys";
            String a_Enabled = "Enabled";
            String a_HTMLEMail = "HTML E-Mail";
            String a_Message = "Message";
            String a_Recipients = "Recipients";
            String a_Subject = "Subject";
            String a_SentTime = "Sent Time";
        }
    }

    interface Organization {
        String name = "Organization";

        String a_Address = "Address";
        String a_Branche = "Branche";
        String a_CompanyLogo = "Company Logo";
        String a_CompanyName = "Company Name";
        String a_Location = "Location";
        String a_Mail = "Mail";
        String a_Members = "Members";
        String a_Phone = "Phone";
        String a_WorkingOrOpeningTime = "Working or Opening Time";
    }

    interface Output {
        String name = "Output";

        String a_Output = "Output";
    }

    interface Parser {
        String name = "Parser";

        interface CSVParser {
            String name = "CSV Parser";

            String a_Charset = "Charset";
            String a_DatapointAlignment = "Datapoint Alignment";
            String a_DatapointIndex = "Datapoint Index";
            String a_DateFormat = "Date Format";
            String a_DateIndex = "Date Index";
            String a_DecimalSeparator = "Decimal Separator";
            String a_Delimiter = "Delimiter";
            String a_NumberOfHeadlines = "Number Of Headlines";
            String a_Quote = "Quote";
            String a_ThousandSeparator = "Thousand Separator";
            String a_TimeFormat = "Time Format";
            String a_TimeIndex = "Time Index";

            interface XLSParser {
                String name = "XLS Parser";

                String a_Charset = "Charset";
                String a_DatapointAlignment = "Datapoint Alignment";
                String a_DatapointIndex = "Datapoint Index";
                String a_DateFormat = "Date Format";
                String a_DateIndex = "Date Index";
                String a_DecimalSeparator = "Decimal Separator";
                String a_Delimiter = "Delimiter";
                String a_NumberOfHeadlines = "Number Of Headlines";
                String a_Quote = "Quote";
                String a_ThousandSeparator = "Thousand Separator";
                String a_TimeFormat = "Time Format";
                String a_TimeIndex = "Time Index";
            }
        }

        interface XLSParser {
            String name = "XLS Parser";

            String a_Charset = "Charset";
            String a_DatapointAlignment = "Datapoint Alignment";
            String a_DatapointIndex = "Datapoint Index";
            String a_DateFormat = "Date Format";
            String a_DateIndex = "Date Index";
            String a_DecimalSeparator = "Decimal Separator";
            String a_Delimiter = "Delimiter";
            String a_NumberOfHeadlines = "Number Of Headlines";
            String a_Quote = "Quote";
            String a_ThousandSeparator = "Thousand Separator";
            String a_TimeFormat = "Time Format";
            String a_TimeIndex = "Time Index";
        }

        interface DWDParser {
            String name = "DWD Parser";

            String a_Charset = "Charset";
        }

        interface DWDHDDParser {
            String name = "DWD HDD Parser";

            String a_Charset = "Charset";
        }

        interface SQLParser {
            String name = "SQL Parser";

        }

        interface JSONParser{
            String name = "JSON Parser";

            String a_dateTimeFormat = "Date Time Format";
            String a_dateTimePath = "Date Time Path";
        }

        interface XMLParser {
            String name = "XML Parser";

            String a_DateAttribute = "Date Attribute";
            String a_DateElement = "Date Element";
            String a_DateFormat = "Date Format";
            String a_DateInElement = "Date in Element";
            String a_DecimalSeparator = "Decimal Separator";
            String a_MainAttribute = "Main Attribute";
            String a_MainElement = "Main Element";
            String a_ThousandSeparator = "Thousand Separator";
            String a_TimeAttribute = "Time Attribute";
            String a_TimeElement = "Time Element";
            String a_TimeFormat = "Time Format";
            String a_TimeInElement = "Time in Element";
            String a_ValueAttribute = "Value Attribute";
            String a_ValueElement = "Value Element";
            String a_ValueInElement = "Value in Element";
        }
    }

    interface RecycleBin {
        String name = "Recycle Bin";

    }

    interface Register {
        String name = "Register";

        interface EquipmentRegister {
            String name = "Equipment Register";

        }
    }

    interface Report {
        String name = "Report";

        String a_Enabled = "Enabled";
        String a_LastReport = "Last Report";
        String a_LastReportPDF = "Last Report PDF";
        String a_PDF = "PDF";
        String a_PDFPages = "PDF Pages";
        String a_Template = "Template";
        String a_TimeZone = "Time Zone";

        interface PeriodicReport {
            String name = "Periodic Report";

            String a_AttributeName = "Attribute Name";
            String a_ConditionEnabled = "Condition Enabled";
            String a_JEVisID = "JEVis ID";
            String a_Limit = "Limit";
            String a_Operator = "Operator";
            String a_Schedule = "Schedule";
            String a_CustomScheduleObject = "Custom Schedule Object";
            String a_StartRecord = "Start Record";
            String a_Enabled = "Enabled";
            String a_LastReport = "Last Report";
            String a_LastReportPDF = "Last Report PDF";
            String a_PDF = "PDF";
            String a_PDFPages = "PDF Pages";
            String a_Template = "Template";
            String a_TimeZone = "Time Zone";

            interface AutomatedWorkingSheet {
                String name = "Automated Working Sheet";

                String a_AttributeName = "Attribute Name";
                String a_ConditionEnabled = "Condition Enabled";
                String a_JEVisID = "JEVis ID";
                String a_Limit = "Limit";
                String a_Operator = "Operator";
                String a_Schedule = "Schedule";
                String a_CustomScheduleObject = "Custom Schedule Object";
                String a_StartRecord = "Start Record";
                String a_Enabled = "Enabled";
                String a_LastReport = "Last Report";
                String a_LastReportPDF = "Last Report PDF";
                String a_PDF = "PDF";
                String a_PDFPages = "PDF Pages";
                String a_Template = "Template";
                String a_TimeZone = "Time Zone";
            }
        }

        interface AutomatedWorkingSheet {
            String name = "Automated Working Sheet";

            String a_AttributeName = "Attribute Name";
            String a_ConditionEnabled = "Condition Enabled";
            String a_JEVisID = "JEVis ID";
            String a_Limit = "Limit";
            String a_Operator = "Operator";
            String a_Schedule = "Schedule";
            String a_CustomScheduleObject = "Custom Schedule Object";
            String a_StartRecord = "Start Record";
            String a_Enabled = "Enabled";
            String a_LastReport = "Last Report";
            String a_LastReportPDF = "Last Report PDF";
            String a_PDF = "PDF";
            String a_PDFPages = "PDF Pages";
            String a_Template = "Template";
            String a_TimeZone = "Time Zone";
        }
    }

    interface ReportAttribute {
        String name = "Report Attribute";

        String a_AttributeName = "Attribute Name";
    }

    interface ReportConfiguration {
        String name = "Report Configuration";

        interface ReportPeriodConfiguration {
            String name = "Report Period Configuration";

            String a_Aggregation = "Aggregation";
            String a_Manipulation = "Manipulation";
            String a_Period = "Period";
            String a_FixedPeriod = "Fixed Period";
        }
    }

    interface ReportLink {
        String name = "Report Link";

        String a_JEVisID = "JEVis ID";
        String a_Optional = "Optional";
        String a_Calculation = "Calculation";
        String a_TemplateVariableName = "Template Variable Name";
    }

    interface ResultCalculationTemplate {
        String name = "Result Calculation Template";

        String a_TemplateFile = "Template File";
    }

    interface Service {
        String name = "Service";

        interface JEAlarm {
            String name = "JEAlarm";

            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
        }

        interface JECalc {
            String name = "JECalc";

            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
        }

        interface JEDataCollector {
            String name = "JEDataCollector";

            String a_DataSourceTimeout = "Data Source Timeout";
            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
        }

        interface JEReport {
            String name = "JEReport";

            String a_NotificationFile = "Notification File";
            String a_NotificationID = "Notification ID";
            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
            String a_Template = "Template";
        }

        interface JENotifier {
            String name = "JENotifier";

            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
        }

        interface JEOPCUAWriter {
            String name = "JEOPCUAWriter";

            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
        }

        interface JEDataProcessor {
            String name = "JEDataProcessor";

            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_CycleTime = "Cycle Time";
            String a_Status = "Status";
            String a_ProcessingSize = "Processing Size";
        }

        interface JEStatus {
            String name = "JEStatus";

            String a_NotificationFile = "Notification File";
            String a_NotificationID = "Notification ID";
            String a_Enable = "Enable";
            String a_MaxNumberThreads = "Max Number Threads";
            String a_User = "User";
            String a_Password = "Password";
            String a_Tariffs = "Tariffs";
            String a_CycleTime = "Cycle Time";
            String a_LastRun = "Last Run";
            String a_StatusEMail = "Status E-Mail";
            String a_StatusLog = "Status Log";
            String a_StatusFileLog = "Status File Log";
            String a_Status = "Status";
            String a_LatestReported = "Latest reported";
        }
    }

    interface StringData {
        String name = "String Data";

        String a_Value = "Value";
        String a_Period = "Period";
    }

    interface System {
        String name = "System";

        String a_DomainName = "Domain Name";
        String a_Hostname = "Hostname";
        String a_Language = "Language";
        String a_LocalIP = "Local IP";
        String a_PublicIP = "Public IP";
        String a_TimeZone = "TimeZone";
    }

    interface User {
        String name = "User";

        String a_Enabled = "Enabled";
        String a_SysAdmin = "Sys Admin";
        String a_Password = "Password";
        String a_EMail = "E-Mail";
        String a_LastName = "Last Name";
        String a_FirstName = "First Name";
        String a_Title = "Title";
        String a_Position = "Position";
        String a_Phone = "Phone";
        String a_Timezone = "Timezone";
        String a_StartDashboard = "Start Dashboard";
        String a_AnalysisFile = "Analysis File";
        String a_Activities = "Activities";
    }

    interface UserData {
        String name = "User Data";

        String i_Value = "Value";
        String a_Period = "Period";
    }

    interface UserRole {
        String name = "User Role";

        String a_Description = "Description";
        String a_Enabled = "Enabled";
        String a_SysAdmin = "Sys Admin";
        String a_EMail = "E-Mail";
        String a_LastName = "Last Name";
        String a_FirstName = "First Name";
        String a_Title = "Title";
        String a_Position = "Position";
        String a_Phone = "Phone";
        String a_Timezone = "Timezone";
        String a_StartDashboard = "Start Dashboard";
    }

    interface Nonconformities {
        String name = "Nonconformities";

        String a_CustomStatus = "Custom Status";
        String a_CustomFields = "Custom Fields";
        String a_CustomMedium = "Custom Medium";
        String a_EnPI = "EnPI";

        interface NonconformitiesDirectory {
            String name = "Nonconformities Directory";

            interface Nonconformity {
                String name = "Nonconformity";

                String a_Data = "Data";


            }
        }
    }
    interface IndexofLegalProvisions {
        String name = "Index of Legal Provisions";

        String a_CustomMedium = "Custom Medium";
        String a_category = "category";
        String a_scope = "scope";
        interface LegalCadastreDirectory {
            String name = "Index of Legal Provisions Directory";
            interface Obligation {
                String name = "Obligation";

                String a_Data = "Data";
            }

        }
    }
}
