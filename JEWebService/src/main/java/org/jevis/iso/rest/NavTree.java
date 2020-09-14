/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.JEVisClassTree;
import org.jevis.iso.add.JEVisObjectTree;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/tree")
public class NavTree {

    /**
     * @param httpHeaders
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url
    ) throws Exception {
        SQLDataSource ds = null;
        try {

            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            final String objects = "Objects";
            final String classes = "Classes";

            String output = "<div class=\"tabnav\">\n" +
                    "    <button class=\"tabnavlinks\" onclick=\"openNavTab(event, 'Objects')\" id=\"tabObjects\">" +
                    objects +
                    "</button>\n" +
                    "    <button class=\"tabnavlinks\" onclick=\"openNavTab(event, 'Classes')\" id=\"tabClasses\">" +
                    classes +
                    "</button>\n" +
                    "</div>";

            output += "<div id=\"Objects\" class=\"tabnavcontent\">";

            JEVisObjectTree tt = new JEVisObjectTree(ds, httpHeaders.getRequestHeader("authorization").get(0));
            output += tt.buildTree();

            output += "</div>\n" +
                    "<div id=\"Classes\" class=\"tabnavcontent\">";

            JEVisClassTree ct = new JEVisClassTree(ds, httpHeaders.getRequestHeader("authorization").get(0));

            output += ct.buildTree();
            output += "</div>";

            return Response.ok(output).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
