/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class TemplateChooser {

    private String Output = new String();

    public TemplateChooser(Map<String, Object> inputData, String choice) {

        try {
            TemplateManager tm = new TemplateManager();
            Writer writer = new StringWriter();
            Template myTemplate;

            File path = new File("./templates/");
            tm.setPath(path);

            String productionoverview = "productionoverview.ftlh";
            String esoverview = "esoverview.ftlh";
            String assetregister = "assetregister.ftlh";
            String nav = "nav.ftlh";
            String managementreviewprint = "managementreviewprint.ftlh";
            String login = "login.ftlh";
            String managementmanual = "managementmanual.ftlh";
            String proceduraldocuments = "proceduraldocuments.ftlh";
            String supmeetings = "supmeetings.ftlh";
            String navbar = "navbar.ftlh";
            String equipmentventilation = "equipmentventilation.ftlh";
            String equipmentproduction = "equipmentproduction.ftlh";
            String equipmentpantry = "equipmentpantry.ftlh";
            String equipmentoffice = "equipmentoffice.ftlh";
            String equipmentlighting = "equipmentlighting.ftlh";
            String equipmentheater = "equipmentheater.ftlh";
            String equipmentcooler = "equipmentcooler.ftlh";
            String equipmentcompressor = "equipmentcompressor.ftlh";
            String equipmentairconditioning = "equipmentairconditioning.ftlh";
            String equipmentregister = "equipmentregister.ftlh";
            String stations = "stations.ftlh";
            String meters = "meters.ftlh";
            String measuringpoints = "measuringpoints.ftlh";
            String monitoringregister = "monitoringregister.ftlh";
            String trainings = "trainings.ftlh";
            String trainingcourses = "trainingcourses.ftlh";
            String managementreviews = "managementreviews.ftlh";
            String legalregulations = "legalregulations.ftlh";
            String energyteammembers = "energyteammembers.ftlh";
            String announcements = "announcements.ftlh";
            String actionplanprint = "actionplanprint.ftlh";
            String actionplans = "actionplans.ftlh";
            String dashboard = "dashboard.ftlh";
            String produce = "produce.ftlh";
            String production = "production.ftlh";
            String energyflowcharts = "energyflowcharts.ftlh";
            String energysourcedualview = "energysourcedualview.ftlh";
            String energysource = "energysource.ftlh";
            String energysources = "energysources.ftlh";
            String energyplanning = "energyplanning.ftlh";
            String auditquestion = "auditquestion.ftlh";
            String internalaudits = "internalaudits.ftlh";
            String audits = "audits.ftlh";
            String documents = "documents.ftlh";
            String meetings = "meetings.ftlh";
            String site = "site.ftlh";
            String main = "index.ftlh";
            String overview = "overview.ftlh";
            String form = "form.ftlh";
            String table = "table.ftlh";
            String chartLine = "chart_line.ftlh";
            String testObjects = "test_objects.ftlh";
            switch (choice) {
                case "testObjects":
                    myTemplate = tm.getTemplate(testObjects);
                    myTemplate.process(inputData, writer);
                    break;
                case "chartLine":
                    myTemplate = tm.getTemplate(chartLine);
                    myTemplate.process(inputData, writer);
                    break;
                case "table":
                    myTemplate = tm.getTemplate(table);
                    myTemplate.process(inputData, writer);
                    break;
                case "form":
                    myTemplate = tm.getTemplate(form);
                    myTemplate.process(inputData, writer);
                    break;
                case "overview":
                    myTemplate = tm.getTemplate(overview);
                    myTemplate.process(inputData, writer);
                    break;
                case "main":
                    myTemplate = tm.getTemplate(main);
                    myTemplate.process(inputData, writer);
                    break;
                case "site":
                    myTemplate = tm.getTemplate(site);
                    myTemplate.process(inputData, writer);
                    break;
                case "meetings":
                    myTemplate = tm.getTemplate(meetings);
                    myTemplate.process(inputData, writer);
                    break;
                case "documents":
                    myTemplate = tm.getTemplate(documents);
                    myTemplate.process(inputData, writer);
                    break;
                case "audits":
                    myTemplate = tm.getTemplate(audits);
                    myTemplate.process(inputData, writer);
                    break;
                case "internalaudits":
                    myTemplate = tm.getTemplate(internalaudits);
                    myTemplate.process(inputData, writer);
                    break;
                case "auditquestion":
                    myTemplate = tm.getTemplate(auditquestion);
                    myTemplate.process(inputData, writer);
                    break;
                case "energyplanning":
                    myTemplate = tm.getTemplate(energyplanning);
                    myTemplate.process(inputData, writer);
                    break;
                case "energysources":
                    myTemplate = tm.getTemplate(energysources);
                    myTemplate.process(inputData, writer);
                    break;
                case "energysource":
                    myTemplate = tm.getTemplate(energysource);
                    myTemplate.process(inputData, writer);
                    break;
                case "energysourcedualview":
                    myTemplate = tm.getTemplate(energysourcedualview);
                    myTemplate.process(inputData, writer);
                    break;
                case "energyflowcharts":
                    myTemplate = tm.getTemplate(energyflowcharts);
                    myTemplate.process(inputData, writer);
                    break;
                case "production":
                    myTemplate = tm.getTemplate(production);
                    myTemplate.process(inputData, writer);
                    break;
                case "produce":
                    myTemplate = tm.getTemplate(produce);
                    myTemplate.process(inputData, writer);
                    break;
                case "dashboard":
                    myTemplate = tm.getTemplate(dashboard);
                    myTemplate.process(inputData, writer);
                    break;
                case "actionplans":
                    myTemplate = tm.getTemplate(actionplans);
                    myTemplate.process(inputData, writer);
                    break;
                case "actionplanprint":
                    myTemplate = tm.getTemplate(actionplanprint);
                    myTemplate.process(inputData, writer);
                    break;
                case "announcements":
                    myTemplate = tm.getTemplate(announcements);
                    myTemplate.process(inputData, writer);
                    break;
                case "energyteammembers":
                    myTemplate = tm.getTemplate(energyteammembers);
                    myTemplate.process(inputData, writer);
                    break;
                case "legalregulations":
                    myTemplate = tm.getTemplate(legalregulations);
                    myTemplate.process(inputData, writer);
                    break;
                case "managementreviews":
                    myTemplate = tm.getTemplate(managementreviews);
                    myTemplate.process(inputData, writer);
                    break;
                case "trainingcourses":
                    myTemplate = tm.getTemplate(trainingcourses);
                    myTemplate.process(inputData, writer);
                    break;
                case "trainings":
                    myTemplate = tm.getTemplate(trainings);
                    myTemplate.process(inputData, writer);
                    break;
                case "monitoringregister":
                    myTemplate = tm.getTemplate(monitoringregister);
                    myTemplate.process(inputData, writer);
                    break;
                case "measuringpoints":
                    myTemplate = tm.getTemplate(measuringpoints);
                    myTemplate.process(inputData, writer);
                    break;
                case "meters":
                    myTemplate = tm.getTemplate(meters);
                    myTemplate.process(inputData, writer);
                    break;
                case "stations":
                    myTemplate = tm.getTemplate(stations);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentregister":
                    myTemplate = tm.getTemplate(equipmentregister);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentairconditioning":
                    myTemplate = tm.getTemplate(equipmentairconditioning);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentcompressor":
                    myTemplate = tm.getTemplate(equipmentcompressor);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentcooler":
                    myTemplate = tm.getTemplate(equipmentcooler);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentheater":
                    myTemplate = tm.getTemplate(equipmentheater);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentlighting":
                    myTemplate = tm.getTemplate(equipmentlighting);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentoffice":
                    myTemplate = tm.getTemplate(equipmentoffice);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentpantry":
                    myTemplate = tm.getTemplate(equipmentpantry);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentproduction":
                    myTemplate = tm.getTemplate(equipmentproduction);
                    myTemplate.process(inputData, writer);
                    break;
                case "equipmentventilation":
                    myTemplate = tm.getTemplate(equipmentventilation);
                    myTemplate.process(inputData, writer);
                    break;
                case "navbar":
                    myTemplate = tm.getTemplate(navbar);
                    myTemplate.process(inputData, writer);
                    break;
                case "supmeetings":
                    myTemplate = tm.getTemplate(supmeetings);
                    myTemplate.process(inputData, writer);
                    break;
                case "proceduraldocuments":
                    myTemplate = tm.getTemplate(proceduraldocuments);
                    myTemplate.process(inputData, writer);
                    break;
                case "managementmanual":
                    myTemplate = tm.getTemplate(managementmanual);
                    myTemplate.process(inputData, writer);
                    break;
                case "login":
                    myTemplate = tm.getTemplate(login);
                    myTemplate.process(inputData, writer);
                    break;
                case "managementreviewprint":
                    myTemplate = tm.getTemplate(managementreviewprint);
                    myTemplate.process(inputData, writer);
                    break;
                case "nav":
                    myTemplate = tm.getTemplate(nav);
                    myTemplate.process(inputData, writer);
                    break;
                case "assetregister":
                    myTemplate = tm.getTemplate(assetregister);
                    myTemplate.process(inputData, writer);
                    break;
                case "esoverview":
                    myTemplate = tm.getTemplate(esoverview);
                    myTemplate.process(inputData, writer);
                    break;
                case "productionoverview":
                    myTemplate = tm.getTemplate(productionoverview);
                    myTemplate.process(inputData, writer);
                    break;
                default:
                    break;
            }

            Output = writer.toString();

        } catch (IOException | TemplateException ex) {
            Logger.getLogger(TemplateChooser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getOutput() {
        return Output;
    }
}
