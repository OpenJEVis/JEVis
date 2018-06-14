/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.InitialContact;
import org.jevis.iso.classes.Meeting;
import org.jevis.iso.classes.Organisation;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/superiormeetings")
public class SuperiorMeetings {

    /**
     * @param httpHeaders
     * @param lang
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("lang") String lang
    ) throws Exception {
        SQLDataSource ds = null;
        try {

            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            ISO50001 iso = new ISO50001(ds);

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            Organisation org = iso.getOrganisation();
            long SupMeetingDirectoryID = org.getSuperiorMeetingsDirID();
            String SupMeetingDirectoryName = org.getSuperiorMeetingsDirName();
            InitialContact ic = org.getFirstContact();
            List<Meeting> listMeetings = org.getSuperiorLevelMeetings();
            List<Long> listYears = org.getListYearsSupMeetings();
            if (!listYears.contains(ic.getYear())) {
                listYears.add(ic.getYear());
            }
            Collections.sort(listYears);

            root.put("SupMeetingDirectoryID", SupMeetingDirectoryID);
            root.put("SupMeetingDirectoryName", SupMeetingDirectoryName);
            root.put("meetings", listMeetings);
            root.put("InitialContact", ic);
            root.put("listYears", listYears);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addMeeting", t.getTranslatedKey(lang, "Add Meeting"));
            } else {
                root.put("addMeeting", "Add Meeting");
            }

            TemplateChooser tc = new TemplateChooser(root, "supmeetings");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
