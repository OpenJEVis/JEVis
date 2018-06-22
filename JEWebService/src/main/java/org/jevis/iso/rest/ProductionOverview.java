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
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.Produce;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/productionoverview")
public class ProductionOverview {

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

            Double totalProduction = 0.0;
            List<Double> productionValues = new ArrayList<>();

            List<Produce> listProduction = iso.getOrganisation().getSite(site).getenergyplanning().getProduction(year);
            List<Double> shareOfTotalProduction = new ArrayList<>();

            for (Produce p : listProduction) {
                productionValues.add(p.getSum());
                totalProduction += p.getSum();
            }

            for (Double d : productionValues) {
                shareOfTotalProduction.add(divZeroFix(d, totalProduction) * 100);
            }

            Table tablecaptionsForHeader;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "Year")),
                        new TableColumn(t.getTranslatedKey(lang, "Name")),
                        new TableColumn(t.getTranslatedKey(lang, "Production")),
                        new TableColumn(t.getTranslatedKey(lang, "Share of total Production<br>%"))});
            } else {
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn("Year"),
                        new TableColumn("Name"),
                        new TableColumn("Production"),
                        new TableColumn("Share of total Production<br>%")});

            }

            root.put("tablecaptionsForHeader", tablecaptionsForHeader);
            root.put("listProduction", listProduction);
            root.put("totalProduction", totalProduction);
            root.put("productionValues", productionValues);
            root.put("shareOfTotalProduction", shareOfTotalProduction);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("production", t.getTranslatedKey(lang, "Production"));
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
                root.put("production", "Production");
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

            TemplateChooser tc = new TemplateChooser(root, "productionoverview");

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
