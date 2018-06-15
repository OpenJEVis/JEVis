/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.iso.add.Form;
import org.jevis.iso.add.*;
import org.jevis.iso.classes.Equipment;
import org.jevis.iso.classes.MeasuringPoint;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/object")
public class ISOForm {

    /**
     * @param httpHeaders
     * @param ID
     * @param lang
     * @param classname
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("ID") Long ID,
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("classname") String classname
    ) throws Exception {

        SQLDataSource ds = null;
        try {
            Form f = new Form();
            ds = new SQLDataSource(httpHeaders, request, url);

            if (classname.equals("")) {
                ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
                ds.preload(SQLDataSource.PRELOAD.ALL_REL);
                JsonObject obj = ds.getUserManager().filterObject(ds.getObject(ID));

                if (Objects.nonNull(obj)) {
                    List<FormAttribute> listFormAttributes = new ArrayList<>();

                    f.setID(obj.getId());
                    f.setName(obj.getName());
                    f.setBauth(httpHeaders.getRequestHeader("authorization").get(0));

                    List<JsonAttribute> listAttributes = ds.getAttributes(obj.getId());

                    for (JsonAttribute att : listAttributes) {
                        if (att.getSampleCount() > 0) {
                            String guiDisplayType = "";
                            for (JsonType jt : ds.getTypes(ds.getJEVisClass(obj.getJevisClass()))) {
                                if (jt.getName().equals(att.getType())) {
                                    guiDisplayType = jt.getGuiType();
                                }
                            }

                            FormAttributeDisplayType fadt = new FormAttributeDisplayType(att.getPrimitiveType(), guiDisplayType);
                            if (!fadt.getOutput().equals(FormAttribute.FormAttributeType.ObjectTarget)) {
                                if (!fadt.getOutput().equals(FormAttribute.FormAttributeType.File)) {
                                    FormAttribute fa = new FormAttribute(ds, obj, att.getType(), fadt.getOutput(), att, att.getLatestValue());

                                    if (fadt.getOutput() == FormAttribute.FormAttributeType.Double) {
                                        fa.setUnithelp(getUnits(ds));
                                    }

                                    listFormAttributes.add(fa);
                                } else {
                                    FormAttribute fa = new FormAttribute(ds, obj, att.getType(), fadt.getOutput(), att);

                                    listFormAttributes.add(fa);
                                }
                            } else {

                                FormAttribute fa = new FormAttribute(ds, obj, att.getType(), fadt.getOutput(), att, att.getLatestValue());

                                TargetHelper th = new TargetHelper(ds, att);

                                if (th.hasObject() && th.isValid()) {
                                    fa.setValue(th.getObject().getName());
                                    fa.setLongValue(th.getObject().getId());
                                }

                                fa.setOthelp(getObjectTargetHelper(ds, att));

                                listFormAttributes.add(fa);
                            }
                        } else {
                            String guiDisplayType = "";
                            for (JsonType jt : ds.getTypes(ds.getJEVisClass(obj.getJevisClass()))) {
                                if (jt.getName().equals(att.getType())) {
                                    guiDisplayType = jt.getGuiType();
                                }
                            }

                            FormAttributeDisplayType fadt = new FormAttributeDisplayType(att.getPrimitiveType(), guiDisplayType);
                            if (!fadt.getOutput().equals(FormAttribute.FormAttributeType.ObjectTarget)) {
                                FormAttribute fa = new FormAttribute(att.getType(), fadt.getOutput());
                                if (fadt.getOutput() == FormAttribute.FormAttributeType.Double) {
                                    fa.setUnithelp(getUnits(ds));
                                }
                                listFormAttributes.add(fa);
                            } else {
                                FormAttribute fa = new FormAttribute(att.getType(), fadt.getOutput());

                                fa.setOthelp(getObjectTargetHelper(ds, att));

                                listFormAttributes.add(fa);
                            }
                        }
                    }

                    if (!lang.equals("")) {
                        f.setTranslated(true);
                        Translations t = new Translations();
                        listFormAttributes = t.translate(listFormAttributes, lang);
                        f.setObjectname(t.getTranslatedKey(lang, "Object Name"));
                        f.setDeleteobject(t.getTranslatedKey(lang, "Delete Object"));
                        f.setUploaded(t.getTranslatedKey(lang, "Uploaded"));
                        f.setTabAttributes(t.getTranslatedKey(lang, "Attributes"));
                        f.setTabPermissions(t.getTranslatedKey(lang, "Permissions"));
                        f.setButtonDelete(t.getTranslatedKey(lang, "Delete Object"));
                        f.setButtonDownload(t.getTranslatedKey(lang, "Download"));
                        f.setButtonUpload(t.getTranslatedKey(lang, "Upload"));
                        f.setAddPermission(t.getTranslatedKey(lang, "Add Permission"));
                        f.setDelete(t.getTranslatedKey(lang, "Delete"));
                    }

                    f.setAttributes(listFormAttributes);

                    if (!obj.getJevisClass().equals("Group")) {
                        List<String> listAccessors = new ArrayList<>();
                        for (JsonRelationship rel : ds.getRelationships(ID)) {
                            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
                                listAccessors.add(ds.getObject(rel.getTo()).getName());
                            }
                        }

                        List<JsonObject> userGroups = ds.getObjects("Group", true);
                        List<JsonObject> foundGroups = new ArrayList<>();
                        List<JsonRelationshipHelper> relHelp = new ArrayList<>();
                        for (JsonRelationship rel : ds.getRelationships(obj.getId())) {
                            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
                                JsonObject g = ds.getObject(rel.getTo());
                                foundGroups.add(g);
                                relHelp.add(new JsonRelationshipHelper(g.getName(), rel.getFrom(), rel.getTo(), rel.getType()));
                            }
                        }
                        f.setPermissions(relHelp);
                        userGroups.removeAll(foundGroups);
                        f.setUserGroups(userGroups);
                    } else {
                        //TODO add User rights to groups
                    }

                    return Response.ok(f.getOutput()).build();
                } else return Response.ok("not enough rights to Object #" + ID).build();
            } else {
                ds.preload(SQLDataSource.PRELOAD.ALL_CLASSES);

                List<String> attributeTypes = Arrays.asList(FormAttribute.FormAttributeType.values().toString());

                f.setAttributeTypes(attributeTypes);

                if (!lang.equals("")) {
                    f.setTranslated(true);
                    Translations t = new Translations();
                    f.setObjectname(t.getTranslatedKey(lang, "Object Name"));
                    f.setDeleteobject(t.getTranslatedKey(lang, "Delete Object"));
                    f.setUploaded(t.getTranslatedKey(lang, "Uploaded"));
                    f.setTabAttributes(t.getTranslatedKey(lang, "Attributes"));
                    f.setTabPermissions(t.getTranslatedKey(lang, "Permissions"));
                    f.setButtonDelete(t.getTranslatedKey(lang, "Delete Object"));
                    f.setButtonDownload(t.getTranslatedKey(lang, "Download"));
                    f.setButtonUpload(t.getTranslatedKey(lang, "Upload"));
                    f.setAddPermission(t.getTranslatedKey(lang, "Add Permission"));
                    f.setDelete(t.getTranslatedKey(lang, "Delete"));
                }

                return Response.ok(f.getOutput()).build();
            }

        } finally {
            Config.CloseDS(ds);
        }
    }

    private List<String> getUnits(SQLDataSource ds) {
        List<String> listS = new ArrayList<>();
        for (JEVisUnit u : UnitManager.getInstance().getSIJEVisUnits()) {
            listS.add(u.toString());
        }

        for (JEVisUnit u : UnitManager.getInstance().getNonSIJEVisUnits()) {
            listS.add(u.toString());
        }
        return listS;
    }

    private List<ObjectTargetHelper> getObjectTargetHelper(SQLDataSource ds, JsonAttribute att) throws JEVisException {

        ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
        List<ObjectTargetHelper> oth = new ArrayList<>();
        JEVisClasses jc = new JEVisClasses(ds);

        switch (att.getType()) {
            case (Equipment.AttMeasuringPoint):
                for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects(jc.getMeasuringPoint().getName(), true))) {
                    ObjectTargetHelper o = new ObjectTargetHelper(obj.getId(), obj.getName(), obj.getJevisClass());
                    oth.add(o);
                }
                break;
            case (Equipment.AttEnergySource):
                for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects(jc.getEnergySource().getName(), false))) {
                    ObjectTargetHelper o = new ObjectTargetHelper(obj.getId(), obj.getName(), obj.getJevisClass());
                    oth.add(o);
                }
                break;
            case (MeasuringPoint.AttMeter):
                for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects(jc.getMeter().getName(), false))) {
                    ObjectTargetHelper o = new ObjectTargetHelper(obj.getId(), obj.getName(), obj.getJevisClass());
                    oth.add(o);
                }
                break;
            case (MeasuringPoint.AttMonitoringID):

                for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects("Data Directory", false))) {
                    ObjectTargetHelper o = new ObjectTargetHelper(obj.getId(), obj.getName(), obj.getJevisClass());
                    oth.add(o);
                    Map<String, JsonObject> children = new HashMap<>();

                    getAllChildren(ds, children, obj, "");

                    Map<String, JsonObject> sortedt = new TreeMap<>(children);

                    for (Map.Entry<String, JsonObject> entry : sortedt.entrySet()) {
                        String key = entry.getKey();
                        JsonObject value = entry.getValue();
                        ObjectTargetHelper t = new ObjectTargetHelper(value.getId(), key, value.getJevisClass());
                        oth.add(t);
                    }
//                        for (JEVisObject two : children) {
//
//                        }
                }
                break;
            case (MeasuringPoint.AttStation):

                for (JsonObject one : ds.getUserManager().filterList(ds.getObjects(jc.getStation().getName(), true))) {
                    ObjectTargetHelper o = new ObjectTargetHelper(one.getId(), one.getName(), one.getJevisClass());
                    oth.add(o);
                }
                break;

            default:
                break;
        }
        return oth;
    }

    private void getAllChildren(SQLDataSource ds, Map<String, JsonObject> all, JsonObject parentobject, String pname) throws JEVisException {
        List<JsonObject> children = Snippets.getAllChildren(ds, parentobject);

        for (JsonObject c : children) {
            String mewName;
            if (!pname.equals("")) {
                mewName = pname + " / " + c.getName();
            } else {
                mewName = c.getName();
            }
            all.put(mewName, c);
            getAllChildren(ds, all, c, mewName);
        }
    }
}
