/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.Table;
import org.jevis.iso.add.TableColumn;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.EnergyPlanningDirectory;
import org.jevis.iso.classes.EnergySource;
import org.jevis.iso.classes.ISO50001;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/esoverview")
public class ESOverview {

    /**
     * @param httpHeaders
     * @param site
     * @param lang
     * @param year
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site,
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("year") Integer year
    ) throws Exception {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            ISO50001 iso = new ISO50001(ds);
            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            if (Objects.isNull(year) || year.equals("")) {
                year = DateTime.now().getYear() - 1;
            }
            root.put("lastyear", year - 1);
            root.put("year", year);
            root.put("nextyear", year + 1);
            root.put("siteName", site);

            List<String> listConsumptionsNames = new ArrayList<>();
            List<Double> listConsumptions = new ArrayList<>();
            List<Double> listShareOfTotal = new ArrayList<>();
            List<Double> listCostRelated = new ArrayList<>();
            List<Double> listCO2ShareOfTotal = new ArrayList<>();
            Double totalConsumption = 0.0;
            List<Double> listCosts = new ArrayList<>();
            Double totalCost = 0.0;
            List<Double> listCO2Emissions = new ArrayList<>();
            Double totalCO2 = 0.0;

            EnergyPlanningDirectory energyPlanning = iso.getOrganisation().getSite(site).getenergyplanning();
            List<EnergySource> listES = iso.getOrganisation().getSite(site).getenergyplanning().getEnergySources();

            for (EnergySource es : listES) {
                listConsumptions.add(0.0);
                listCosts.add(0.0);
                listCO2Emissions.add(0.0);
                listConsumptionsNames.add("");

                listConsumptions.set(listES.indexOf(es), es.getEnergyConsumption(year).getSum());
                listCosts.set(listES.indexOf(es), es.getEnergyBill(year).getSum());
                listCO2Emissions.set(listES.indexOf(es), es.getEnergyConsumption(year).getSum() * es.getCO2EmissionFactor());
                listConsumptionsNames.set(listES.indexOf(es), es.getName());
            }

            for (Double d : listConsumptions) {
                totalConsumption += d;
                totalCost += listCosts.get(listConsumptions.indexOf(d));
                totalCO2 += listCO2Emissions.get(listConsumptions.indexOf(d));
            }

            for (Double d : listConsumptions) {
                listShareOfTotal.add(0.0);
                listCostRelated.add(0.0);
                listCO2ShareOfTotal.add(0.0);

                listShareOfTotal.set(listConsumptions.indexOf(d), (divZeroFix(d, totalConsumption) * 100));
                listCostRelated.set(listConsumptions.indexOf(d), (divZeroFix(listCosts.get(listConsumptions.indexOf(d)), totalCost) * 100));
                listCO2ShareOfTotal.set(listConsumptions.indexOf(d), (divZeroFix(listCO2Emissions.get(listConsumptions.indexOf(d)), totalCO2) * 100));
            }


            Table tablecaptionsForHeader;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "Year")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Source")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Consumption<br>kWh")),
                        new TableColumn(t.getTranslatedKey(lang, "Share of total consumption<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "Costs<br>€")),
                        new TableColumn(t.getTranslatedKey(lang, "Cost-share of total<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-emissions<br>kg")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-share of total<br>%"))});
            } else {
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn("Year"),
                        new TableColumn("Energy Source"),
                        new TableColumn("Energy Consumption<br>kWh"),
                        new TableColumn("Share of total consumption<br>%"),
                        new TableColumn("Costs<br>€"),
                        new TableColumn("Cost-share of total<br>%"),
                        new TableColumn("CO2-emissions<br>kg"),
                        new TableColumn("CO2-share of total<br>%")});

            }

            root.put("tablecaptionsForHeader", tablecaptionsForHeader);
            root.put("energysources", listES);
            root.put("listConsumptions", listConsumptions);
            root.put("totalConsumption", totalConsumption);
            root.put("listCosts", listCosts);
            root.put("totalCost", totalCost);
            root.put("listCO2Emissions", listCO2Emissions);
            root.put("totalCO2", totalCO2);
            root.put("listCO2ShareOfTotal", listCO2ShareOfTotal);
            root.put("listConsumptionsNames", listConsumptionsNames);
            root.put("listCostRelated", listCostRelated);
            root.put("listShareOfTotal", listShareOfTotal);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("consumption", t.getTranslatedKey(lang, "Consumption"));
                root.put("total", t.getTranslatedKey(lang, "Total"));
                root.put("janStr", t.getTranslatedKey(lang, "January"));
                root.put("febStr", t.getTranslatedKey(lang, "February"));
                root.put("marStr", t.getTranslatedKey(lang, "March"));
                root.put("aprStr", t.getTranslatedKey(lang, "April"));
                root.put("mayStr", t.getTranslatedKey(lang, "May"));
                root.put("junStr", t.getTranslatedKey(lang, "June"));
                root.put("julStr", t.getTranslatedKey(lang, "July"));
                root.put("augStr", t.getTranslatedKey(lang, "August"));
                root.put("sepStr", t.getTranslatedKey(lang, "September"));
                root.put("octStr", t.getTranslatedKey(lang, "October"));
                root.put("novStr", t.getTranslatedKey(lang, "November"));
                root.put("decStr", t.getTranslatedKey(lang, "December"));
            } else {
                root.put("consumption", "Consumption");
                root.put("total", "Total");
                root.put("janStr", "January");
                root.put("febStr", "February");
                root.put("marStr", "March");
                root.put("aprStr", "April");
                root.put("mayStr", "May");
                root.put("junStr", "June");
                root.put("julStr", "July");
                root.put("augStr", "August");
                root.put("sepStr", "September");
                root.put("octStr", "October");
                root.put("novStr", "November");
                root.put("decStr", "December");
            }

            TemplateChooser tc = new TemplateChooser(root, "esoverview");

            return Response.ok(tc.getOutput()).build();

        } finally {
            Config.CloseDS(ds);
        }
    }

    private Double divZeroFix(Double dividend, Double divisor) {
        Double d = 0.0;

        d = dividend / divisor;

        if (d.isNaN()) {
            return 0.0;
        } else {
            return d;
        }
    }

}
