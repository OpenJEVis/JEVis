/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.cli;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.config.CommonOptions;

/**
 * Temporary solution to mask the DataSource authentication.
 *
 * This function can be removed if the JEAPI-WS is implemented.
 *
 * @author fs
 */
public class ConnectionEncoder {

    private static final String seperator = "|";

    public static String encode(String host, String port, String schema, String user, String pw) {
        String toEncode = "" + host + seperator + port + seperator + schema + seperator + user + seperator + pw;
        return Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
    }

    public static JEVisOption decode(String value) {
        byte[] decodedValue = Base64.getDecoder().decode(value);
        String asSting = new String(decodedValue, StandardCharsets.UTF_8);
//        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));

        String[] segs = asSting.split(Pattern.quote(seperator));
        JEVisOption datasource = new BasicOption();
        datasource.setKey(CommonOptions.DataSource.DataSource.getKey());

        JEVisOption host = new BasicOption();
        host.setKey(CommonOptions.DataSource.HOST.getKey());
        host.setValue(segs[0]);

        JEVisOption port = new BasicOption();
        port.setKey(CommonOptions.DataSource.PORT.getKey());
        port.setValue(segs[1]);

        JEVisOption schema = new BasicOption();
        schema.setKey(CommonOptions.DataSource.SCHEMA.getKey());
        schema.setValue(segs[2]);

        JEVisOption user = new BasicOption();
        user.setKey(CommonOptions.DataSource.USERNAME.getKey());
        user.setValue(segs[3]);

        JEVisOption pass = new BasicOption();
        pass.setKey(CommonOptions.DataSource.PASSWORD.getKey());
        pass.setValue(segs[4]);

        JEVisOption dsclass = new BasicOption();
        dsclass.setKey(CommonOptions.DataSource.CLASS.getKey());
        dsclass.setValue("org.jevis.api.sql.JEVisDataSourceSQL");

        datasource.addOption(host, true);
        datasource.addOption(port, true);
        datasource.addOption(schema, true);
        datasource.addOption(user, true);
        datasource.addOption(pass, true);
        datasource.addOption(dsclass, true);

//        for (JEVisOption jo : datasource.getOptions()) {
//            System.out.println("KEY: " + jo.getKey() + "   Value: " + jo.getValue());
//        }
        return datasource;
    }
}
