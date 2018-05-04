package org.jevis.iso.add;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

public class JEVisClassTree {

    private final String end = "</ul></li></ul></div>";

    private final String newList = "<ul>";
    private final String closeList = "</ul>";

    private final String closeElement = "</li>";
    List<JsonJEVisClass> check = new ArrayList<>();
    private String _bauth = "";
    private SQLDataSource _ds;
    private Long id = 0L;
    private JsonJEVisClass parent;
    private String output = "";
    private List<JsonJEVisClass> list = new ArrayList<>();


    public JEVisClassTree(SQLDataSource ds, String bauth) throws JEVisException {
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

        createTree(_ds, parent);

        output += end;

        return output;
    }

    private void createTree(SQLDataSource ds, JsonJEVisClass parent) throws JEVisException {
        list = Snippets.getAllChildren(ds, parent);
        for (JsonJEVisClass obj : list) {
            id = id + 1;
            output += newList;
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
            check.add(obj);

            //createTree(ds, obj);

            output += closeElement;

            output += closeList;
        }
    }
}

