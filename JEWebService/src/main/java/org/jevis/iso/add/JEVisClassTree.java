package org.jevis.iso.add;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JEVisClassTree {

    private final String end = "</ul></li></ul></div>";

    private final String newList = "<ul>";
    private final String closeList = "</ul>";

    private final String closeElement = "</li>";
    private String _bauth = "";
    private SQLDataSource _ds;
    private Long id = 0L;
    private JsonJEVisClass parent;
    private String output = "";
    private List<JsonJEVisClass> list = new ArrayList<>();


    public JEVisClassTree(SQLDataSource ds, String bauth) {
        ds.preload(SQLDataSource.PRELOAD.ALL_CLASSES);


        parent = ds.getJEVisClass("System");

        _ds = ds;
        _bauth = bauth;
    }

    public String buildTree() throws JEVisException {

        final String start = "<nav class='vertical-navbar resticons'><div class='css-treeview row'><ul><li style='padding:  4px;'><input type='checkbox' id='item-0' checked/><label for='item-0'>" +
                parent.getName() +
                "</label>";

        output += start;

        List<JsonJEVisClass> listAllClasses = _ds.getJEVisClasses();
        List<JsonClassRelationship> listClassRelationships = _ds.getClassRelationships();

        for (JsonJEVisClass cl : listAllClasses) {
            for (JsonClassRelationship crel : listClassRelationships) {
                if (crel.getStart().equals(cl.getName())) {
                    List<JsonClassRelationship> temp = new ArrayList<>();
                    if (Objects.nonNull(cl.getRelationships())) temp.addAll(cl.getRelationships());
                    temp.add(crel);
                    cl.setRelationships(temp);
                }
            }
        }

        output += newList;

        for (JsonJEVisClass cl : listAllClasses) {
            if (Objects.isNull(cl.getRelationships())) {
                id = id + 1;
                final String element = "<li style='padding:  4px;'><input type='checkbox' id='item-0-"
                        + id
                        + "'/><label class='toggle' for='item-0-"
                        + id
                        + "'><i class='fa fa-angle-right'></i>"
                        + "<a href='#' onclick=\"connect('./object?classname=" + cl.getName() + "', '" + _bauth + "', 'content-form');return false;\">"
                        + cl.getName()
                        + "</a>"
                        + "</label>";
                output += element;

                createTree(_ds, cl);

                output += closeElement;
            }
        }

        output += closeList;

        output += end;

        return output;
    }

    private void createTree(SQLDataSource ds, JsonJEVisClass parent) {
        list = Snippets.getAllChildren(ds, parent);
        output += newList;

        for (JsonJEVisClass obj : list) {
            id = id + 1;
            final String element = "<li style='padding:  4px;'><input type='checkbox' id='item-0-"
                    + id
                    + "'/><label class='toggle' for='item-0-"
                    + id
                    + "'><i class='fa fa-angle-right'></i>"
                    + "<a href='#' onclick=\"connect('./object?classname=" + obj.getName() + "', '" + _bauth + "', 'content-form');return false;\">"
                    + obj.getName()
                    + "</a>"
                    + "</label>";
            output += element;

            createTree(ds, obj);

            output += closeElement;

        }

        output += closeList;
    }
}

