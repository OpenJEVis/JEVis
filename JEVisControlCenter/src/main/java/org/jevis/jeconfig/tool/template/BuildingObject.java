package org.jevis.jeconfig.tool.template;

import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingObject extends Template {
    private final SimpleBooleanProperty withNoEntryPointGroups = new SimpleBooleanProperty(this, "withNoEntryPointGroups", false);

    private final SimpleObjectProperty<DateTimeZone> timeZone = new SimpleObjectProperty<>(this, "timeZone", DateTimeZone.UTC);

    @Override
    public String getName() {
        return I18n.getInstance().getString("alarms.table.captions.building");
    }

    @Override
    public boolean create(JEVisClass jclass, JEVisObject parent, String name) throws JEVisException {
        JEVisDataSource ds = parent.getDataSource();
        JEVisClass buildingClass = ds.getJEVisClass("Building");
        String jscName = "JSC@" + name;
        DateTime firstDate = new DateTime(1990, 1, 1, 0, 0, 0, 0);
        DateTime creationDate = DateTime.now();

        JEVisClass administrationDirectoryClass = ds.getJEVisClass("Administration Directory");
        JEVisClass userDirectoryClass = ds.getJEVisClass("User Directory");
        JEVisClass userClass = ds.getJEVisClass("User");
        JEVisClass groupDirectoryClass = ds.getJEVisClass("Group Directory");
        JEVisClass group = ds.getJEVisClass("Group");
        JEVisClass userRoleDirectoryClass = ds.getJEVisClass("User Role Directory");

        JEVisClass accountingPluginClass = ds.getJEVisClass("Accounting Plugin");
        JEVisObject accountingPlugin = ds.getObjects(accountingPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass alarmPluginClass = ds.getJEVisClass("Alarm Plugin");
        JEVisObject alarmPlugin = ds.getObjects(alarmPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass baseDataPluginClass = ds.getJEVisClass("Base Data Plugin");
        JEVisObject baseDataPlugin = ds.getObjects(baseDataPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass chartPluginClass = ds.getJEVisClass("Graph Plugin");
        JEVisObject chartPlugin = ds.getObjects(chartPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass classPluginClass = ds.getJEVisClass("Class Plugin");
        JEVisObject classPlugin = ds.getObjects(classPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass objectPluginClass = ds.getJEVisClass("Configuration Plugin");
        JEVisObject objectPlugin = ds.getObjects(objectPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass dashboardPluginClass = ds.getJEVisClass("Dashboard Plugin");
        JEVisObject dashboardPlugin = ds.getObjects(dashboardPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass equipmentPluginClass = ds.getJEVisClass("Equipment Plugin");
        JEVisObject equipmentPlugin = ds.getObjects(equipmentPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass iso50001PluginClass = ds.getJEVisClass("ISO 50001 Plugin");
        JEVisObject iso50001Plugin = ds.getObjects(iso50001PluginClass, true).stream().findFirst().orElse(null);
        JEVisClass loytecPluginClass = ds.getJEVisClass("Loytec Plugin");
        JEVisObject loytecPlugin = ds.getObjects(loytecPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass mapPluginClass = ds.getJEVisClass("Map Plugin");
        JEVisObject mapPlugin = ds.getObjects(mapPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass meterPluginClass = ds.getJEVisClass("Meter Plugin");
        JEVisObject meterPlugin = ds.getObjects(meterPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass notesPluginClass = ds.getJEVisClass("Notes Plugin");
        JEVisObject notesPlugin = ds.getObjects(notesPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass reportPluginClass = ds.getJEVisClass("Report Plugin");
        JEVisObject reportPlugin = ds.getObjects(reportPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass trcPluginClass = ds.getJEVisClass("Template Result Calculation Plugin");
        JEVisObject trcPlugin = ds.getObjects(trcPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass unitPluginClass = ds.getJEVisClass("Unit Plugin");
        JEVisObject unitPlugin = ds.getObjects(unitPluginClass, true).stream().findFirst().orElse(null);

        JEVisObject buildingObject = parent.buildObject("Building", buildingClass);
        buildingObject.setLocalName(I18n.getInstance().getLocale().getLanguage(), name);
        buildingObject.commit();

        JEVisAttribute timezoneAttribute = buildingObject.getAttribute(JC.MonitoredObject.Building.a_Timezone);
        JEVisSample timeZoneSample = timezoneAttribute.buildSample(new DateTime(), this.timeZone.get().getID());
        timeZoneSample.commit();

        JEVisObject administrationDirectory = buildTranslatedObject(buildingObject, "Administration", administrationDirectoryClass,
                "Administration", "Адміністрація", "Администрация", "การบริหาร", "إدارة");

        JEVisObject userDirectory = buildTranslatedObject(administrationDirectory, "Users", userDirectoryClass,
                "Benutzer", "користувача", "пользователь", "ผู้ใช้", "المستعمل");

        JEVisObject jscUser = buildTranslatedObject(userDirectory, jscName, userClass, jscName, jscName, jscName, jscName, jscName);

        JEVisObject groupDirectory = buildTranslatedObject(administrationDirectory, "Groups", groupDirectoryClass,
                "Gruppen", "групи", "группы", "กลุ่ม", "مجموعات");

        JEVisObject userRoleDirectory = buildTranslatedObject(administrationDirectory, "Roles", userRoleDirectoryClass,
                "Benutzerrollen", "ролі користувачів", "роли пользователей", "บทบาทของผู้ใช้", "أدوار المستخدمين");

        JEVisObject buildingGroup = buildTranslatedObject(groupDirectory, name, group, "", "", "", "", "");
        ds.buildRelationship(jscUser.getID(), buildingGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);

        buildGroupsWithEntryPoints(ds, groupDirectoryClass, group, groupDirectory, buildingObject, jscUser, name);

        if (withNoEntryPointGroups.get()) {
            buildGroupsWithoutEntryPoints(ds, groupDirectoryClass, group, groupDirectory, buildingObject, jscUser, name);
        }

        JEVisObject pluginsDirectory = buildTranslatedObject(groupDirectory, "Plugins", groupDirectoryClass,
                "Plugins", "плагіни", "плагины", "ปลั๊กอิน", "الإضافات");
        String[] dashboardNames = new String[]{"Plugin", "Plugin", "підключати", "плагин", "เสียบเข้าไป", "توصيل في"};
        JEVisObject dashboardPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Dashboard " + dashboardNames[0], group,
                name + " Dashboard " + dashboardNames[1], name + " Панель приладів " + dashboardNames[2],
                name + " Приборная панель " + dashboardNames[3], name + " แผงควบคุม " + dashboardNames[4], dashboardNames[5] + " لوحة القيادة " + name);
        ds.buildRelationship(jscUser.getID(), dashboardPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(dashboardPlugin.getID(), dashboardPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject chartPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Analyses " + dashboardNames[0], group,
                name + " Analysen " + dashboardNames[1], name + " аналізи " + dashboardNames[2],
                name + " анализы " + dashboardNames[3], name + " การวิเคราะห์ " + dashboardNames[4], dashboardNames[5] + " التحليلات " + name);
        ds.buildRelationship(jscUser.getID(), chartPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(chartPlugin.getID(), chartPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject objectPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Objects " + dashboardNames[0], group,
                name + " Objekte " + dashboardNames[1], name + " об'єктів " + dashboardNames[2],
                name + " объекты " + dashboardNames[3], name + " วัตถุ " + dashboardNames[4], dashboardNames[5] + " أشياء " + name);
        ds.buildRelationship(jscUser.getID(), objectPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(objectPlugin.getID(), objectPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject alarmPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Alarms " + dashboardNames[0], group,
                name + " Alarme " + dashboardNames[1], name + " сигналізація " + dashboardNames[2],
                name + " Аварийная сигнализация " + dashboardNames[3], name + " เตือน " + dashboardNames[4], dashboardNames[5] + " إنذار " + name);
        ds.buildRelationship(jscUser.getID(), alarmPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(alarmPlugin.getID(), alarmPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject reportsPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Reports " + dashboardNames[0], group,
                name + " Berichte " + dashboardNames[1], name + " звіти " + dashboardNames[2],
                name + " отчеты " + dashboardNames[3], name + " รายงาน " + dashboardNames[4], dashboardNames[5] + " التقارير " + name);
        ds.buildRelationship(jscUser.getID(), reportsPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(reportPlugin.getID(), reportsPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject notesPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Notes " + dashboardNames[0], group,
                name + " Notizen " + dashboardNames[1], name + " примітки " + dashboardNames[2],
                name + " примечания " + dashboardNames[3], name + " หมายเหตุ " + dashboardNames[4], dashboardNames[5] + " ملحوظات " + name);
        ds.buildRelationship(jscUser.getID(), notesPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(notesPlugin.getID(), notesPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject measurementPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Measurement " + dashboardNames[0], group,
                name + " Messstellen " + dashboardNames[1], name + " точки вимірювання " + dashboardNames[2],
                name + " точки измерения " + dashboardNames[3], name + " จุดวัด " + dashboardNames[4], dashboardNames[5] + " نقاط القياس " + name);
        ds.buildRelationship(jscUser.getID(), measurementPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(meterPlugin.getID(), measurementPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        JEVisObject baseDataPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Base Data " + dashboardNames[0], group,
                name + " Stammdaten " + dashboardNames[1], name + " базові дані " + dashboardNames[2],
                name + " базовые данные " + dashboardNames[3], name + " ข้อมูลพื้นฐาน " + dashboardNames[4], dashboardNames[5] + " البيانات الأساسية " + name);
        ds.buildRelationship(jscUser.getID(), baseDataPluginGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
        ds.buildRelationship(baseDataPlugin.getID(), baseDataPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);

        return true;
    }

    private void buildGroupsWithEntryPoints(JEVisDataSource ds, JEVisClass groupDirectoryClass, JEVisClass groupClass, JEVisObject groupDirectory, JEVisObject buildingObject, JEVisObject jscUser, String buildingName) throws JEVisException {
        JEVisObject groupWithEntryPointsDirectory = buildTranslatedObject(groupDirectory, "Groups With Entry Points", groupDirectoryClass,
                "Gruppen mit Einstiegspunkt", "Групи з точкою входу", "Группы с точкой входа", "กลุ่มที่มีจุดเริ่มต้น", "مجموعات مع نقطة دخول");
        buildGroups(ds, groupClass, buildingObject, jscUser, groupWithEntryPointsDirectory, buildingName, true);
    }

    private void buildGroupsWithoutEntryPoints(JEVisDataSource ds, JEVisClass groupDirectoryClass, JEVisClass groupClass, JEVisObject groupDirectory, JEVisObject buildingObject, JEVisObject jscUser, String buildingName) throws JEVisException {
        JEVisObject groupWithoutEntryPointsDirectory = buildTranslatedObject(groupDirectory, "Groups Without Entry Points", groupDirectoryClass,
                "Gruppen ohne Einstiegspunkt", "Групи без точки входу", "Группы без точки входа", "กลุ่มที่ไม่มีจุดเริ่มต้น", "مجموعات بدون نقطة دخول");

        buildGroups(ds, groupClass, buildingObject, jscUser, groupWithoutEntryPointsDirectory, buildingName, false);
    }

    private void buildOwnerRelationship(JEVisDataSource ds, JEVisObject directory, JEVisObject group, boolean withEntryPoint) throws JEVisException {
        ds.buildRelationship(directory.getID(), group.getID(), JEVisConstants.ObjectRelationship.OWNER);
        if (withEntryPoint) {
            group.buildRelationship(directory, JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD);
        }
    }

    private void buildGroups(JEVisDataSource ds, JEVisClass groupClass, JEVisObject buildingObject, JEVisObject jscUser, JEVisObject groupDirectory, String buildingName, boolean withEntryPoints) throws JEVisException {

        JEVisClass alarmDirectoryClass = ds.getJEVisClass("Alarm Directory");
        JEVisClass analysesDirectoryClass = ds.getJEVisClass("Analyses Directory");
        JEVisClass calculationDirectoryClass = ds.getJEVisClass("Calculation Directory");
        JEVisClass reportDirectoryClass = ds.getJEVisClass("Report Directory");
        JEVisClass dataDirectoryClass = ds.getJEVisClass("Data Directory");
        JEVisClass dataSourceDirectoryClass = ds.getJEVisClass("Data Source Directory");
        JEVisClass documentsDirectoryClass = ds.getJEVisClass("Documents Directory");
        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
        JEVisClass meterDirectoryClass = ds.getJEVisClass("Measurement Directory");
        JEVisClass baseDataDirectoryClass = ds.getJEVisClass("Base Data Directory");

        JEVisObject alarmGroup = buildTranslatedObject(groupDirectory, buildingName + " Alarms", groupClass,
                buildingName + " Alarme", buildingName + " сигналізація", buildingName + " Аварийная сигнализация", buildingName + " เตือน", "إنذار " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), alarmGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), alarmGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), alarmGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), alarmGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), alarmGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject alarmDirectory = buildTranslatedObject(buildingObject, "Alarms", alarmDirectoryClass,
                "Alarme", "сигналізація", "Аварийная сигнализация", "เตือน", "إنذار");
        buildOwnerRelationship(ds, alarmDirectory, alarmGroup, withEntryPoints);

        JEVisObject analysesGroup = buildTranslatedObject(groupDirectory, buildingName + " Analyses", groupClass,
                buildingName + " Analysen", buildingName + " аналізи", buildingName + " анализы", buildingName + " การวิเคราะห์", "التحليلات " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), analysesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), analysesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), analysesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), analysesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), analysesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject analysesDirectory = buildTranslatedObject(buildingObject, "Analyses", analysesDirectoryClass,
                "Analysen", "аналізи", "анализы", "การวิเคราะห์", "التحليلات");
        buildOwnerRelationship(ds, analysesDirectory, analysesGroup, withEntryPoints);

        JEVisObject calculationsGroup = buildTranslatedObject(groupDirectory, buildingName + " Calculations", groupClass,
                buildingName + " Berechnungen", buildingName + " розрахунки", buildingName + " расчеты", buildingName + " การคำนวณ", "العمليات الحسابية " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), calculationsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), calculationsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), calculationsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), calculationsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), calculationsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject calculationDirectory = buildTranslatedObject(buildingObject, "Calculations", calculationDirectoryClass,
                "Berechnungen", "розрахунки", "расчеты", "การคำนวณ", "العمليات الحسابية");
        buildOwnerRelationship(ds, calculationDirectory, calculationsGroup, withEntryPoints);

        JEVisObject reportsGroup = buildTranslatedObject(groupDirectory, buildingName + " Reports", groupClass,
                buildingName + " Berichte", buildingName + " звіти", buildingName + " отчеты", buildingName + " รายงาน", "التقارير " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), reportsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), reportsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), reportsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), reportsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), reportsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject reportsDirectory = buildTranslatedObject(buildingObject, "Reports", reportDirectoryClass,
                "Berichte", "звіти", "отчеты", "รายงาน", "التقارير");
        buildOwnerRelationship(ds, reportsDirectory, reportsGroup, withEntryPoints);

        JEVisObject dataGroup = buildTranslatedObject(groupDirectory, buildingName + " Data", groupClass,
                buildingName + " Daten", buildingName + " Дані", buildingName + " Данные", buildingName + " ข้อมูล", "بيانات " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), dataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), dataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), dataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), dataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), dataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject dataDirectory = buildTranslatedObject(buildingObject, "Data", dataDirectoryClass,
                "Daten", "Дані", "Данные", "ข้อมูล", "بيانات");
        buildOwnerRelationship(ds, dataDirectory, dataGroup, withEntryPoints);

        JEVisObject dataSourcesGroup = buildTranslatedObject(groupDirectory, buildingName + " Data Sources", groupClass,
                buildingName + " Datenerfassung", buildingName + " збір даних", buildingName + " сбор информации", buildingName + " การเก็บรวบรวมข้อมูล", "جمع البيانات " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), dataSourcesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), dataSourcesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), dataSourcesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), dataSourcesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), dataSourcesGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject dataSourceDirectory = buildTranslatedObject(buildingObject, "Data Sources", dataSourceDirectoryClass,
                "Datenerfassung", "збір даних", "сбор информации", "การเก็บรวบรวมข้อมูล", "جمع البيانات");
        buildOwnerRelationship(ds, dataSourceDirectory, dataSourcesGroup, withEntryPoints);

        JEVisObject documentsGroup = buildTranslatedObject(groupDirectory, buildingName + " Documents", groupClass,
                buildingName + " Dokumente", buildingName + " Документи", buildingName + " Документы", buildingName + " เอกสาร", "وثائق " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), documentsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), documentsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), documentsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), documentsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), documentsGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject documentDirectory = buildTranslatedObject(buildingObject, "Documents", documentsDirectoryClass,
                "Dokumente", "Документи", "Документы", "เอกสาร", "وثائق");
        buildOwnerRelationship(ds, documentDirectory, documentsGroup, withEntryPoints);

        JEVisObject calendarGroup = buildTranslatedObject(groupDirectory, buildingName + " Calendar", groupClass,
                buildingName + " Kalender", buildingName + " календар", buildingName + " календарь", buildingName + " ปฏิทิน", "التقويم " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), calendarGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), calendarGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), calendarGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), calendarGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), calendarGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject calendarDirectory = buildTranslatedObject(buildingObject, "Calendar", calendarDirectoryClass,
                "Kalender", "календар", "календарь", "ปฏิทิน", "التقويم");
        buildOwnerRelationship(ds, calendarDirectory, calendarGroup, withEntryPoints);

        JEVisObject meterGroup = buildTranslatedObject(groupDirectory, buildingName + " Measurement", groupClass,
                buildingName + " Messstellen", buildingName + " точки вимірювання", buildingName + " точки измерения", buildingName + " จุดวัด", "نقاط القياس " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), meterGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), meterGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), meterGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), meterGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), meterGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject meterDirectory = buildTranslatedObject(buildingObject, "Measurement", meterDirectoryClass,
                "Messstellen", "точки вимірювання", "точки измерения", "จุดวัด", "نقاط القياس");
        buildOwnerRelationship(ds, meterDirectory, meterGroup, withEntryPoints);

        JEVisObject baseDataGroup = buildTranslatedObject(groupDirectory, buildingName + " Base Data", groupClass,
                buildingName + " Stammdaten", buildingName + " базові дані", buildingName + " базовые данные", buildingName + " ข้อมูลพื้นฐาน", "البيانات الأساسية " + buildingName);
        if (withEntryPoints) {
            ds.buildRelationship(jscUser.getID(), baseDataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_READ);
            ds.buildRelationship(jscUser.getID(), baseDataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_WRITE);
            ds.buildRelationship(jscUser.getID(), baseDataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
            ds.buildRelationship(jscUser.getID(), baseDataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_DELETE);
            ds.buildRelationship(jscUser.getID(), baseDataGroup.getID(), JEVisConstants.ObjectRelationship.MEMBER_CREATE);
        }
        JEVisObject baseDataDirectory = buildTranslatedObject(buildingObject, "Base Data", baseDataDirectoryClass,
                "Stammdaten", "базові дані", "базовые данные", "ข้อมูลพื้นฐาน", "البيانات الأساسية");
        buildOwnerRelationship(ds, baseDataDirectory, baseDataGroup, withEntryPoints);
    }

    private JEVisObject buildTranslatedObject(JEVisObject parent, String englishName, JEVisClass objectClass, String germanName, String ukrainianName, String russianName, String thaiName, String arabicName) throws JEVisException {

        List<JEVisObject> children = parent.getChildren(objectClass, true);
        JEVisObject child = null;

        if (!children.isEmpty()) {
            for (JEVisObject object : children) {
                if (object.getLocalName("en").equals(englishName)) {
                    child = object;
                    break;
                }
            }
        }

        if (child == null) {
            child = parent.buildObject(englishName, objectClass);
            Map<String, String> languageMap = new HashMap<>();
            languageMap.put("de", germanName);
            languageMap.put("en", englishName);
            languageMap.put("uk", ukrainianName);
            languageMap.put("ru", russianName);
            languageMap.put("th", thaiName);
            languageMap.put("ar", arabicName);
            child.setLocalNames(languageMap);
            child.commit();
        }

        return child;
    }

    @Override
    public boolean supportsClass(JEVisClass jClass) throws JEVisException {
        return jClass.getName().equals("Building");
    }

    @Override
    public Map<String, Node> getOptions() {
        Map<String, Node> optionMap = new HashMap<>();

        String withNoEntryPointGroupsName = "Create No Entry Points Groups";
        JFXCheckBox withNoEntryPointGroupsBox = new JFXCheckBox(withNoEntryPointGroupsName);
        withNoEntryPointGroupsBox.selectedProperty().bindBidirectional(withNoEntryPointGroups);

        String timeZone = "Timezone";
        TimeZoneBox timeZoneBox = new TimeZoneBox();
        timeZoneBox.valueProperty().bindBidirectional(this.timeZone);

        optionMap.put(withNoEntryPointGroupsName, withNoEntryPointGroupsBox);
        optionMap.put(timeZone, timeZoneBox);

        return optionMap;
    }
}
