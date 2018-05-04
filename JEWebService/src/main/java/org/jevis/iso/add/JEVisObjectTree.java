package org.jevis.iso.add;

import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

public class JEVisObjectTree {

    private final String end = "</ul></li></ul></div>";

    private final String newList = "<ul>";
    private final String closeList = "</ul>";

    private final String closeElement = "</li>";
    private String _bauth = "";
    private SQLDataSource _ds;
    private Long id = 0L;
    private JsonObject parent;
    private String output = "";
    private List<JsonObject> list = new ArrayList<>();
    private List<JsonClassRelationship> listCRel = new ArrayList<>();

    public JEVisObjectTree(SQLDataSource ds, String bauth) throws JEVisException {
        ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
        ds.preload(SQLDataSource.PRELOAD.ALL_REL);

        parent = ds.getObject(1L);

        _ds = ds;
        _bauth = bauth;
        listCRel = ds.getClassRelationships();
    }


    public String buildTree() throws JEVisException {

        final String start = "<nav class='vertical-navbar resticons'><div class='css-treeview row'><ul><li style='padding:  4px;'><input type='checkbox' id='item-0' checked/><label for='item-0'>" +
                parent.getName() +
                "</label>";

        output += start;

        createTree(_ds, parent);

        output += end;

        return output;
    }

    private void createTree(SQLDataSource ds, JsonObject parent) throws JEVisException {
        list = Snippets.getAllChildren(ds, parent);
        JsonObject _parent = parent;
        for (JsonObject obj : list) {
            id = id + 1;
            output += newList;
            final String element = "<li style='padding:  4px;'><input type='checkbox' id='item-0-"
                    + id
                    + "'/><label class='toggle' for='item-0-"
                    + id
                    + "'><i class='fa fa-angle-right'></i>"
                    + "<a href='#' onclick=\"connect('./object?ID=" + obj.getId() + "', '" + _bauth + "', 'content-form');return false;\">"
                    + obj.getName()
                    + "</a>"
                    + "</label>";
            output += element;

            createTree(ds, obj);

            List<JsonObject> children = Snippets.getAllChildren(ds, obj);

            if (children.isEmpty()) {
                for (JsonClassRelationship crel : ds.getClassRelationships(obj.getJevisClass())) {
                    if (crel.getType() == JEVisConstants.ClassRelationship.OK_PARENT && crel.getEnd().equals(obj.getJevisClass())) {
                        output += "<ul><li><a class=\"add-item\" href=\"\"\n"
                                + "onclick=\"createObject('"
                                + crel.getStart()
                                + "', '"
                                + obj.getId()
                                + "', '"
                                + "tree"
                                + "', '', '"
                                + _bauth
                                + "'); return false;\"><i\n"
                                + "class=\"fa fa-plus-square\"></i> "
                                + " "
                                + crel.getStart()
                                + "</a></li></ul>";
                    }
                }
            }

            output += closeElement;

            List<JsonObject> checkList = Snippets.getAllChildren(ds, _parent);

            if (obj.getName().equals(checkList.get(checkList.size() - 1).getName())) {
                List<String> classList = new ArrayList<>();
                for (JsonObject jobj : checkList) {
                    if (ds.getJEVisClass(jobj.getJevisClass()).getUnique()) {
                        classList.add(jobj.getJevisClass());
                    }
                }
                String className = ds.getObject(obj.getParent()).getJevisClass();
                for (JsonClassRelationship crel : ds.getClassRelationships(className)) {
                    if (crel.getType() == JEVisConstants.ClassRelationship.OK_PARENT && !crel.getStart().equals(className)) {
                        if (!classList.contains(crel.getStart())) {
                            output += "<li><a class=\"add-item\" href=\"\"\n"
                                    + "onclick=\"createObject('"
                                    + crel.getStart()
                                    + "', '"
                                    + _parent.getId()
                                    + "', '"
                                    + "tree"
                                    + "', '', '"
                                    + _bauth
                                    + "'); return false;\"><i\n"
                                    + "class=\"fa fa-plus-square\"></i> "
                                    + " "
                                    + crel.getStart()
                                    + "</a></li>";
                        }
                    }
                }
            }


            output += closeList;
            parent = obj;
        }
    }
}

