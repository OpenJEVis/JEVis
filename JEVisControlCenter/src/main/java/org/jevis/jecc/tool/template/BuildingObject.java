package org.jevis.jecc.tool.template;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.jevis.jecc.plugin.object.extension.role.RoleManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;

import static java.util.Locale.GERMANY;
import static org.jevis.api.JEVisConstants.Direction.FORWARD;
import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

public class BuildingObject extends Template {
    public static final int THREAD_WAIT = 500;
    private final SimpleBooleanProperty withNoEntryPointGroups = new SimpleBooleanProperty(this, "withNoEntryPointGroups", false);

    private final SimpleObjectProperty<DateTimeZone> timeZone = new SimpleObjectProperty<>(this, "timeZone", DateTimeZone.UTC);

    private final List<String> executeExceptions = new ArrayList<>(Arrays.asList("Alarms", "Data X", "Base Data"));

    @Override
    public String getName() {
        return I18n.getInstance().getString("alarms.table.captions.building");
    }

    @Override
    public boolean create(JEVisClass jclass, JEVisObject parent, String name) throws JEVisException, InterruptedException {
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
        JEVisClass userRoleClass = ds.getJEVisClass("User Role");

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
        JEVisClass actionPlanPluginClass = ds.getJEVisClass("Action Plan Plugin");
        JEVisObject actionPlanPlugin = ds.getObjects(actionPlanPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass nonConformityPluginClass = ds.getJEVisClass("Nonconformities Plugin");
        JEVisObject nonConformityPlugin = ds.getObjects(nonConformityPluginClass, true).stream().findFirst().orElse(null);
        JEVisClass indexOfLegalProvisionsPluginClass = ds.getJEVisClass("Index of Legal Provisions Plugin");
        JEVisObject indexOfLegalProvisionsPlugin = ds.getObjects(indexOfLegalProvisionsPluginClass, true).stream().findFirst().orElse(null);

        JEVisObject buildingObject = parent.buildObject("Building", buildingClass);
        buildingObject.setLocalName(I18n.getInstance().getLocale().getLanguage(), name);
        buildingObject.commit();

        JEVisAttribute timezoneAttribute = buildingObject.getAttribute(JC.MonitoredObject.Building.a_Timezone);
        JEVisSample timeZoneSample = timezoneAttribute.buildSample(new DateTime(), this.timeZone.get().getID());
        timeZoneSample.commit();
        Thread.sleep(THREAD_WAIT);

        JEVisObject administrationDirectory = buildTranslatedObject(buildingObject, "Administration", administrationDirectoryClass,
                "Administration", "Адміністрація", "Администрация", "การบริหาร", "إدارة");

        JEVisObject userDirectory = buildTranslatedObject(administrationDirectory, "Users", userDirectoryClass,
                "Benutzer", "користувача", "пользователь", "ผู้ใช้", "المستعمل");

        JEVisObject jscUser = buildTranslatedObject(userDirectory, jscName, userClass, jscName, jscName, jscName, jscName, jscName);

        JEVisObject groupDirectory = buildTranslatedObject(administrationDirectory, "Groups", groupDirectoryClass,
                "Gruppen", "групи", "группы", "กลุ่ม", "مجموعات");

        JEVisObject userRoleDirectory = buildTranslatedObject(administrationDirectory, "Roles", userRoleDirectoryClass,
                "Benutzerrollen", "ролі користувачів", "роли пользователей", "บทบาทของผู้ใช้", "أدوار المستخدمين");

        JEVisObject standardRole = buildTranslatedObject(userRoleDirectory, "User Role", userRoleClass,
                "Benutzer Rolle", "Роль користувача", "Роль пользователя", "บทบาทของผู้ใช้", "دور المستخدم");

        JEVisObject adminRole = buildTranslatedObject(userRoleDirectory, "Admin Role", userRoleClass,
                "Administrator Rolle", "Роль адміністратора", "Роль администратора", "บทบาทผู้ดูแลระบบ", "دور المسؤول");

        JEVisObject buildingGroup = null;
        JEVisObject buildingGroupX = null;
        Locale locale = I18n.getInstance().getLocale();

        if (locale.equals(GERMANY)) {
            buildingGroup = buildTranslatedObject(groupDirectory, "", group, name, "", "", "", "");
            buildingGroupX = buildTranslatedObject(groupDirectory, "", group, name + " X", "", "", "", "");
        } else if (locale.getLanguage().equals("ru")) {
            buildingGroup = buildTranslatedObject(groupDirectory, "", group, "", "", name, "", "");
            buildingGroupX = buildTranslatedObject(groupDirectory, "", group, "", "", name + " X", "", "");
        } else if (locale.getLanguage().equals("th")) {
            buildingGroup = buildTranslatedObject(groupDirectory, "", group, "", "", "", name, "");
            buildingGroupX = buildTranslatedObject(groupDirectory, "", group, "", "", "", name + " X", "");
        } else if (locale.getLanguage().equals("uk")) {
            buildingGroup = buildTranslatedObject(groupDirectory, "", group, "", name, "", "", "");
            buildingGroupX = buildTranslatedObject(groupDirectory, "", group, "", name + " X", "", "", "");
        } else if (locale.getLanguage().equals("ar")) {
            buildingGroup = buildTranslatedObject(groupDirectory, "", group, "", "", "", "", name);
            buildingGroupX = buildTranslatedObject(groupDirectory, "", group, "", "", "", "", name + " X");
        } else {
            buildingGroup = buildTranslatedObject(groupDirectory, name, group, "", "", "", "", "");
            buildingGroupX = buildTranslatedObject(groupDirectory, name + " X", group, "", "", "", "", "");
        }

        buildOwnerRelationship(ds, buildingObject, buildingGroup, false);
        buildOwnerRelationship(ds, buildingObject, buildingGroupX, true);

        JEVisObject groupDirectoryWithEntryPoints = buildGroupsWithEntryPoints(ds, groupDirectoryClass, group, groupDirectory, buildingObject, jscUser, name);
        JEVisObject groupDirectoryWithoutEntryPoints = null;

        if (withNoEntryPointGroups.get()) {
            groupDirectoryWithoutEntryPoints = buildGroupsWithoutEntryPoints(ds, groupDirectoryClass, group, groupDirectory, buildingObject, jscUser, name);
        }

        JEVisObject pluginsDirectory = buildTranslatedObject(groupDirectory, "Plugins", groupDirectoryClass,
                "Module", "плагіни", "плагины", "ปลั๊กอิน", "الإضافات");
        String[] dashboardNames = new String[]{"Plugin", "Modul", "підключати", "плагин", "เสียบเข้าไป", "توصيل في"};

        JEVisObject dashboardPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Dashboard " + dashboardNames[0], group,
                name + " Dashboard " + dashboardNames[1], name + " Панель приладів " + dashboardNames[2],
                name + " Приборная панель " + dashboardNames[3], name + " แผงควบคุม " + dashboardNames[4], dashboardNames[5] + " لوحة القيادة " + name);
        ds.buildRelationship(dashboardPlugin.getID(), dashboardPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject chartPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Analyses " + dashboardNames[0], group,
                name + " Analysen " + dashboardNames[1], name + " аналізи " + dashboardNames[2],
                name + " анализы " + dashboardNames[3], name + " การวิเคราะห์ " + dashboardNames[4], dashboardNames[5] + " التحليلات " + name);
        ds.buildRelationship(chartPlugin.getID(), chartPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject objectPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Objects " + dashboardNames[0], group,
                name + " Objekte " + dashboardNames[1], name + " об'єктів " + dashboardNames[2],
                name + " объекты " + dashboardNames[3], name + " วัตถุ " + dashboardNames[4], dashboardNames[5] + " أشياء " + name);
        ds.buildRelationship(objectPlugin.getID(), objectPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject alarmPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Alarms " + dashboardNames[0], group,
                name + " Alarme " + dashboardNames[1], name + " сигналізація " + dashboardNames[2],
                name + " Аварийная сигнализация " + dashboardNames[3], name + " เตือน " + dashboardNames[4], dashboardNames[5] + " إنذار " + name);
        ds.buildRelationship(alarmPlugin.getID(), alarmPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject reportsPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Reports " + dashboardNames[0], group,
                name + " Berichte " + dashboardNames[1], name + " звіти " + dashboardNames[2],
                name + " отчеты " + dashboardNames[3], name + " รายงาน " + dashboardNames[4], dashboardNames[5] + " التقارير " + name);
        ds.buildRelationship(reportPlugin.getID(), reportsPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject notesPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Notes " + dashboardNames[0], group,
                name + " Notizen " + dashboardNames[1], name + " примітки " + dashboardNames[2],
                name + " примечания " + dashboardNames[3], name + " หมายเหตุ " + dashboardNames[4], dashboardNames[5] + " ملحوظات " + name);
        ds.buildRelationship(notesPlugin.getID(), notesPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject measurementPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Measurement " + dashboardNames[0], group,
                name + " Messstellen " + dashboardNames[1], name + " точки вимірювання " + dashboardNames[2],
                name + " точки измерения " + dashboardNames[3], name + " จุดวัด " + dashboardNames[4], dashboardNames[5] + " نقاط القياس " + name);
        ds.buildRelationship(meterPlugin.getID(), measurementPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject baseDataPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Base Data " + dashboardNames[0], group,
                name + " Stammdaten " + dashboardNames[1], name + " базові дані " + dashboardNames[2],
                name + " базовые данные " + dashboardNames[3], name + " ข้อมูลพื้นฐาน " + dashboardNames[4], dashboardNames[5] + " البيانات الأساسية " + name);
        ds.buildRelationship(baseDataPlugin.getID(), baseDataPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject actionPlanPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Action Plan " + dashboardNames[0], group,
                name + " Aktionsplan " + dashboardNames[1], name + " План дій " + dashboardNames[2],
                name + " План Действий " + dashboardNames[3], name + " แผนปฏิบัติการ " + dashboardNames[4], dashboardNames[5] + " خطة عمل " + name);
        ds.buildRelationship(actionPlanPlugin.getID(), actionPlanPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject nonConformityPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Non Conformity " + dashboardNames[0], group,
                name + " Abweichungen " + dashboardNames[1], name + " Відхилення " + dashboardNames[2],
                name + " Отклонения " + dashboardNames[3], name + " การเบี่ยงเบน " + dashboardNames[4], dashboardNames[5] + " الانحرافات " + name);
        ds.buildRelationship(nonConformityPlugin.getID(), nonConformityPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        JEVisObject indexOfLegalProvisionsPluginGroup = buildTranslatedObject(pluginsDirectory, name + " Legal " + dashboardNames[0], group,
                name + " Rechtskataster " + dashboardNames[1], name + " Юридичний реєстр " + dashboardNames[2],
                name + " Юридический реестр " + dashboardNames[3], name + " ทะเบียนทางกฎหมาย " + dashboardNames[4], dashboardNames[5] + " السجل القانوني " + name);
        ds.buildRelationship(indexOfLegalProvisionsPlugin.getID(), indexOfLegalProvisionsPluginGroup.getID(), JEVisConstants.ObjectRelationship.OWNER);
        Thread.sleep(THREAD_WAIT);

        createRoleRelationships(groupDirectoryWithEntryPoints, buildingGroup, pluginsDirectory, standardRole, adminRole);

        adminRole.buildRelationship(jscUser, ROLE_MEMBER, FORWARD);

        RoleManager roleManager = new RoleManager(adminRole);
        roleManager.commit();

        return true;
    }

    private void createRoleRelationships(JEVisObject groupDirectoryWithEntryPoints, JEVisObject buildingGroup, JEVisObject pluginsDirectory, JEVisObject standardRole, JEVisObject adminRole) throws JEVisException, InterruptedException {

        for (JEVisObject group : groupDirectoryWithEntryPoints.getChildren()) {
            standardRole.buildRelationship(group, ROLE_READ, FORWARD);
            Thread.sleep(THREAD_WAIT);

            adminRole.buildRelationship(group, ROLE_READ, FORWARD);
            Thread.sleep(THREAD_WAIT);
            adminRole.buildRelationship(group, ROLE_WRITE, FORWARD);
            Thread.sleep(THREAD_WAIT);

            if (executeExceptions.stream().anyMatch(group.getLocalName("en")::contains)) {
                standardRole.buildRelationship(group, ROLE_EXECUTE, FORWARD);
                Thread.sleep(THREAD_WAIT);
            }
            adminRole.buildRelationship(group, ROLE_EXECUTE, FORWARD);
            Thread.sleep(THREAD_WAIT);

            adminRole.buildRelationship(group, ROLE_CREATE, FORWARD);
            Thread.sleep(THREAD_WAIT);
            adminRole.buildRelationship(group, ROLE_DELETE, FORWARD);
            Thread.sleep(THREAD_WAIT);
        }

        standardRole.buildRelationship(buildingGroup, ROLE_READ, FORWARD);
        adminRole.buildRelationship(buildingGroup, ROLE_READ, FORWARD);

        for (JEVisObject group : pluginsDirectory.getChildren()) {
            if (!group.getLocalName("en").contains("Objects")) {
                standardRole.buildRelationship(group, ROLE_READ, FORWARD);
                Thread.sleep(THREAD_WAIT);
            }
            adminRole.buildRelationship(group, ROLE_READ, FORWARD);
            Thread.sleep(THREAD_WAIT);
        }
    }


    private JEVisObject buildGroupsWithEntryPoints(JEVisDataSource ds, JEVisClass groupDirectoryClass, JEVisClass groupClass, JEVisObject groupDirectory, JEVisObject buildingObject, JEVisObject jscUser, String buildingName) throws JEVisException, InterruptedException {
        JEVisObject groupWithEntryPointsDirectory = buildTranslatedObject(groupDirectory, "Groups With Entry Points", groupDirectoryClass,
                "Gruppen mit Einstiegspunkt", "Групи з точкою входу", "Группы с точкой входа", "กลุ่มที่มีจุดเริ่มต้น", "مجموعات مع نقطة دخول");
        buildGroups(ds, groupClass, buildingObject, jscUser, groupWithEntryPointsDirectory, buildingName, true);

        return groupWithEntryPointsDirectory;
    }

    private JEVisObject buildGroupsWithoutEntryPoints(JEVisDataSource ds, JEVisClass groupDirectoryClass, JEVisClass groupClass, JEVisObject groupDirectory, JEVisObject buildingObject, JEVisObject jscUser, String buildingName) throws JEVisException, InterruptedException {
        JEVisObject groupWithoutEntryPointsDirectory = buildTranslatedObject(groupDirectory, "Groups Without Entry Points", groupDirectoryClass,
                "Gruppen ohne Einstiegspunkt", "Групи без точки входу", "Группы без точки входа", "กลุ่มที่ไม่มีจุดเริ่มต้น", "مجموعات بدون نقطة دخول");

        buildGroups(ds, groupClass, buildingObject, jscUser, groupWithoutEntryPointsDirectory, buildingName, false);
        return groupWithoutEntryPointsDirectory;
    }

    private void buildOwnerRelationship(JEVisDataSource ds, JEVisObject directory, JEVisObject group, boolean withEntryPoint) throws JEVisException, InterruptedException {
        ds.buildRelationship(directory.getID(), group.getID(), JEVisConstants.ObjectRelationship.OWNER);
        if (withEntryPoint) {
            group.buildRelationship(directory, JEVisConstants.ObjectRelationship.ROOT, FORWARD);
            Thread.sleep(THREAD_WAIT);
        }
    }

    private void buildGroups(JEVisDataSource ds, JEVisClass groupClass, JEVisObject buildingObject, JEVisObject jscUser, JEVisObject groupDirectory, String buildingName, boolean withEntryPoints) throws JEVisException, InterruptedException {

        JEVisClass alarmDirectoryClass = ds.getJEVisClass("Alarm Directory");
        JEVisClass actionPlanDirectoryClass = ds.getJEVisClass("Action Plan Directory v2");
        JEVisClass nonConformityDirectoryClass = ds.getJEVisClass("NonconformityPlan Directory");
        JEVisClass indexOfLegalProvisionsDirectoryClass = ds.getJEVisClass("Index of Legal Provisions Directory");
        JEVisClass analysesDirectoryClass = ds.getJEVisClass("Analyses Directory");
        JEVisClass dashboardDirectoryClass = ds.getJEVisClass("Dashboard Directory");
        JEVisClass calculationDirectoryClass = ds.getJEVisClass("Calculation Directory");
        JEVisClass reportDirectoryClass = ds.getJEVisClass("Report Directory");
        JEVisClass dataDirectoryClass = ds.getJEVisClass("Data Directory");
        JEVisClass dataSourceDirectoryClass = ds.getJEVisClass("Data Source Directory");
        JEVisClass documentsDirectoryClass = ds.getJEVisClass("Document Directory");
        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
        JEVisClass meterDirectoryClass = ds.getJEVisClass("Measurement Directory");
        JEVisClass baseDataDirectoryClass = ds.getJEVisClass("Base Data Directory");

        JEVisObject alarmGroup;
        if (withEntryPoints)
            alarmGroup = buildTranslatedObject(groupDirectory, buildingName + " Alarms X", groupClass,
                    buildingName + " Alarme X", buildingName + " сигналізація X", buildingName + " Аварийная сигнализация X", buildingName + " เตือน X", "إنذار X " + buildingName);
        else alarmGroup = buildTranslatedObject(groupDirectory, buildingName + " Alarms", groupClass,
                buildingName + " Alarme", buildingName + " сигналізація", buildingName + " Аварийная сигнализация", buildingName + " เตือน", "إنذار " + buildingName);
        JEVisObject alarmDirectory = buildTranslatedObject(buildingObject, "Alarms", alarmDirectoryClass,
                "Alarme", "сигналізація", "Аварийная сигнализация", "เตือน", "إنذار");
        buildOwnerRelationship(ds, alarmDirectory, alarmGroup, withEntryPoints);

        JEVisObject actionPlanGroup;
        if (withEntryPoints)
            actionPlanGroup = buildTranslatedObject(groupDirectory, buildingName + " Action Plan X", groupClass,
                    buildingName + " Aktionsplan X", buildingName + " План дій X", buildingName + " План Действий X", buildingName + " แผนปฏิบัติการ X", "خطة عمل X " + buildingName);
        else actionPlanGroup = buildTranslatedObject(groupDirectory, buildingName + " Action Plan", groupClass,
                buildingName + " Aktionsplan", buildingName + " План дій", buildingName + " План Действий", buildingName + " แผนปฏิบัติการ", "خطة عمل " + buildingName);
        JEVisObject actionPlanDirectory = buildTranslatedObject(buildingObject, "Action Plan", actionPlanDirectoryClass,
                "Aktionsplan", "План дій", "План Действий", "แผนปฏิบัติการ", "خطة عمل");
        buildOwnerRelationship(ds, actionPlanDirectory, actionPlanGroup, withEntryPoints);

        JEVisObject nonConformityGroup;
        if (withEntryPoints)
            nonConformityGroup = buildTranslatedObject(groupDirectory, buildingName + " Non Conformity X", groupClass,
                    buildingName + " Abweichungen X", buildingName + " Відхилення X", buildingName + " Отклонения X", buildingName + " การเบี่ยงเบน X", "الانحرافات X " + buildingName);
        else nonConformityGroup = buildTranslatedObject(groupDirectory, buildingName + " Non Conformity", groupClass,
                buildingName + " Abweichungen", buildingName + " Відхилення", buildingName + " Отклонения", buildingName + " การเบี่ยงเบน", "الانحرافات " + buildingName);
        JEVisObject nonConformityDirectory = buildTranslatedObject(buildingObject, "Non Conformity", nonConformityDirectoryClass,
                "Abweichungen", "Відхилення", "Отклонения", "การเบี่ยงเบน", "الانحرافات");
        buildOwnerRelationship(ds, nonConformityDirectory, nonConformityGroup, withEntryPoints);

        JEVisObject indexOfLegalProvisionsGroup;
        if (withEntryPoints)
            indexOfLegalProvisionsGroup = buildTranslatedObject(groupDirectory, buildingName + " Legal X", groupClass,
                    buildingName + " Rechtskataster X", buildingName + " Юридичний реєстр X", buildingName + " Юридический реестр X", buildingName + " ทะเบียนทางกฎหมาย X", "السجل القانوني X " + buildingName);
        else indexOfLegalProvisionsGroup = buildTranslatedObject(groupDirectory, buildingName + " Legal", groupClass,
                buildingName + " Rechtskataster", buildingName + " Юридичний реєстр", buildingName + " Юридический реестр", buildingName + " ทะเบียนทางกฎหมาย", "السجل القانوني " + buildingName);
        JEVisObject indexOfLegalProvisionsDirectory = buildTranslatedObject(buildingObject, "Legal", indexOfLegalProvisionsDirectoryClass,
                "Rechtskataster", "Юридичний реєстр", "Юридический реестр", "ทะเบียนทางกฎหมาย", "السجل القانوني");
        buildOwnerRelationship(ds, indexOfLegalProvisionsDirectory, indexOfLegalProvisionsGroup, withEntryPoints);

        JEVisObject analysesGroup;
        if (withEntryPoints)
            analysesGroup = buildTranslatedObject(groupDirectory, buildingName + " Analyses X", groupClass,
                    buildingName + " Analysen X", buildingName + " аналізи X", buildingName + " анализы X", buildingName + " การวิเคราะห์ X", "التحليلات X " + buildingName);
        else analysesGroup = buildTranslatedObject(groupDirectory, buildingName + " Analyses", groupClass,
                buildingName + " Analysen", buildingName + " аналізи", buildingName + " анализы", buildingName + " การวิเคราะห์", "التحليلات " + buildingName);
        JEVisObject analysesDirectory = buildTranslatedObject(buildingObject, "Analyses", analysesDirectoryClass,
                "Analysen", "аналізи", "анализы", "การวิเคราะห์", "التحليلات");
        buildOwnerRelationship(ds, analysesDirectory, analysesGroup, withEntryPoints);

        JEVisObject dashboardGroup;
        if (withEntryPoints)
            dashboardGroup = buildTranslatedObject(groupDirectory, buildingName + " Dashboards X", groupClass,
                    buildingName + " Dashboards X", buildingName + " Приладові панелі X", buildingName + " Панели мониторинга X", buildingName + " แดชบอร์ด X", "لوحات المعلومات X " + buildingName);
        else dashboardGroup = buildTranslatedObject(groupDirectory, buildingName + " Dashboards", groupClass,
                buildingName + " Dashboards", buildingName + " Приладові панелі", buildingName + " Панели мониторинга", buildingName + " แดชบอร์ด", "لوحات المعلومات " + buildingName);
        JEVisObject dashboardsDirectory = buildTranslatedObject(buildingObject, "Dashboards", dashboardDirectoryClass,
                "Dashboards", "Приладові панелі", "Панели мониторинга", "แดชบอร์ด", "لوحات المعلومات");
        buildOwnerRelationship(ds, dashboardsDirectory, dashboardGroup, withEntryPoints);

        JEVisObject calculationsGroup;
        if (withEntryPoints)
            calculationsGroup = buildTranslatedObject(groupDirectory, buildingName + " Calculations X", groupClass,
                    buildingName + " Berechnungen X", buildingName + " розрахунки X", buildingName + " расчеты X", buildingName + " การคำนวณ X", "العمليات الحسابية X " + buildingName);
        else calculationsGroup = buildTranslatedObject(groupDirectory, buildingName + " Calculations", groupClass,
                buildingName + " Berechnungen", buildingName + " розрахунки", buildingName + " расчеты", buildingName + " การคำนวณ", "العمليات الحسابية " + buildingName);
        JEVisObject calculationDirectory = buildTranslatedObject(buildingObject, "Calculations", calculationDirectoryClass,
                "Berechnungen", "розрахунки", "расчеты", "การคำนวณ", "العمليات الحسابية");
        buildOwnerRelationship(ds, calculationDirectory, calculationsGroup, withEntryPoints);

        JEVisObject reportsGroup;
        if (withEntryPoints)
            reportsGroup = buildTranslatedObject(groupDirectory, buildingName + " Reports X", groupClass,
                    buildingName + " Berichte X", buildingName + " звіти X", buildingName + " отчеты X", buildingName + " รายงาน X", "التقارير X " + buildingName);
        else reportsGroup = buildTranslatedObject(groupDirectory, buildingName + " Reports", groupClass,
                buildingName + " Berichte", buildingName + " звіти", buildingName + " отчеты", buildingName + " รายงาน", "التقارير " + buildingName);
        JEVisObject reportsDirectory = buildTranslatedObject(buildingObject, "Reports", reportDirectoryClass,
                "Berichte", "звіти", "отчеты", "รายงาน", "التقارير");
        buildOwnerRelationship(ds, reportsDirectory, reportsGroup, withEntryPoints);

        JEVisObject dataGroup;
        if (withEntryPoints)
            dataGroup = buildTranslatedObject(groupDirectory, buildingName + " Data X", groupClass,
                    buildingName + " Daten X", buildingName + " Дані X", buildingName + " Данные X", buildingName + " ข้อมูล X", "بيانات X " + buildingName);
        else dataGroup = buildTranslatedObject(groupDirectory, buildingName + " Data", groupClass,
                buildingName + " Daten", buildingName + " Дані", buildingName + " Данные", buildingName + " ข้อมูล", "بيانات " + buildingName);
        JEVisObject dataDirectory = buildTranslatedObject(buildingObject, "Data", dataDirectoryClass,
                "Daten", "Дані", "Данные", "ข้อมูล", "بيانات");
        buildOwnerRelationship(ds, dataDirectory, dataGroup, withEntryPoints);

        JEVisObject dataSourcesGroup;
        if (withEntryPoints)
            dataSourcesGroup = buildTranslatedObject(groupDirectory, buildingName + " Data Sources X", groupClass,
                    buildingName + " Datenerfassung X", buildingName + " збір даних X", buildingName + " сбор информации X", buildingName + " การเก็บรวบรวมข้อมูล X", "جمع البيانات X " + buildingName);
        else dataSourcesGroup = buildTranslatedObject(groupDirectory, buildingName + " Data Sources", groupClass,
                buildingName + " Datenerfassung", buildingName + " збір даних", buildingName + " сбор информации", buildingName + " การเก็บรวบรวมข้อมูล", "جمع البيانات " + buildingName);
        JEVisObject dataSourceDirectory = buildTranslatedObject(buildingObject, "Data Sources", dataSourceDirectoryClass,
                "Datenerfassung", "збір даних", "сбор информации", "การเก็บรวบรวมข้อมูล", "جمع البيانات");
        buildOwnerRelationship(ds, dataSourceDirectory, dataSourcesGroup, withEntryPoints);

        JEVisObject documentsGroup;
        if (withEntryPoints)
            documentsGroup = buildTranslatedObject(groupDirectory, buildingName + " Documents X", groupClass,
                    buildingName + " Dokumente X", buildingName + " Документи X", buildingName + " Документы X", buildingName + " เอกสาร X", "وثائق X " + buildingName);
        else documentsGroup = buildTranslatedObject(groupDirectory, buildingName + " Documents", groupClass,
                buildingName + " Dokumente", buildingName + " Документи", buildingName + " Документы", buildingName + " เอกสาร", "وثائق " + buildingName);
        JEVisObject documentDirectory = buildTranslatedObject(buildingObject, "Documents", documentsDirectoryClass,
                "Dokumente", "Документи", "Документы", "เอกสาร", "وثائق");
        buildOwnerRelationship(ds, documentDirectory, documentsGroup, withEntryPoints);

        JEVisObject calendarGroup;
        if (withEntryPoints)
            calendarGroup = buildTranslatedObject(groupDirectory, buildingName + " Calendar X", groupClass,
                    buildingName + " Kalender X", buildingName + " календар X", buildingName + " календарь X", buildingName + " ปฏิทิน X", "التقويم X " + buildingName);
        else calendarGroup = buildTranslatedObject(groupDirectory, buildingName + " Calendar", groupClass,
                buildingName + " Kalender", buildingName + " календар", buildingName + " календарь", buildingName + " ปฏิทิน", "التقويم " + buildingName);
        JEVisObject calendarDirectory = buildTranslatedObject(buildingObject, "Calendar", calendarDirectoryClass,
                "Kalender", "календар", "календарь", "ปฏิทิน", "التقويم");
        buildOwnerRelationship(ds, calendarDirectory, calendarGroup, withEntryPoints);

        JEVisObject meterGroup;
        if (withEntryPoints)
            meterGroup = buildTranslatedObject(groupDirectory, buildingName + " Measurement X", groupClass,
                    buildingName + " Messstellen X", buildingName + " точки вимірювання X", buildingName + " точки измерения X", buildingName + " จุดวัด X", "نقاط القياس X " + buildingName);
        else meterGroup = buildTranslatedObject(groupDirectory, buildingName + " Measurement", groupClass,
                buildingName + " Messstellen", buildingName + " точки вимірювання", buildingName + " точки измерения", buildingName + " จุดวัด", "نقاط القياس " + buildingName);
        JEVisObject meterDirectory = buildTranslatedObject(buildingObject, "Measurement", meterDirectoryClass,
                "Messstellen", "точки вимірювання", "точки измерения", "จุดวัด", "نقاط القياس");
        buildOwnerRelationship(ds, meterDirectory, meterGroup, withEntryPoints);

        JEVisObject baseDataGroup;
        if (withEntryPoints)
            baseDataGroup = buildTranslatedObject(groupDirectory, buildingName + " Base Data X", groupClass,
                    buildingName + " Stammdaten X", buildingName + " базові дані X", buildingName + " базовые данные X", buildingName + " ข้อมูลพื้นฐาน X", "البيانات الأساسية X " + buildingName);
        else baseDataGroup = buildTranslatedObject(groupDirectory, buildingName + " Base Data", groupClass,
                buildingName + " Stammdaten", buildingName + " базові дані", buildingName + " базовые данные", buildingName + " ข้อมูลพื้นฐาน", "البيانات الأساسية " + buildingName);
        JEVisObject baseDataDirectory = buildTranslatedObject(buildingObject, "Base Data", baseDataDirectoryClass,
                "Stammdaten", "базові дані", "базовые данные", "ข้อมูลพื้นฐาน", "البيانات الأساسية");
        buildOwnerRelationship(ds, baseDataDirectory, baseDataGroup, withEntryPoints);
    }

    private JEVisObject buildTranslatedObject(JEVisObject parent, String englishName, JEVisClass objectClass, String germanName, String ukrainianName, String russianName, String thaiName, String arabicName) throws JEVisException, InterruptedException {

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
            Thread.sleep(THREAD_WAIT);
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
        CheckBox withNoEntryPointGroupsBox = new CheckBox(withNoEntryPointGroupsName);
        withNoEntryPointGroupsBox.selectedProperty().bindBidirectional(withNoEntryPointGroups);

        String timeZone = "Timezone";
        TimeZoneBox timeZoneBox = new TimeZoneBox();
        timeZoneBox.valueProperty().bindBidirectional(this.timeZone);

        optionMap.put(withNoEntryPointGroupsName, withNoEntryPointGroupsBox);
        optionMap.put(timeZone, timeZoneBox);

        return optionMap;
    }
}
