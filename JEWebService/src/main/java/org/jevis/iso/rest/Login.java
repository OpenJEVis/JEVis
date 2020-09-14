/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.ws.sql.Config;
import org.jevis.iso.add.LangHelper;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.ThemeHelper;
import org.jevis.iso.add.Translations;
import sun.misc.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/login")
public class Login {

    private final String dsUserName = "Sys Admin";
    private final String dsPassword = "nordhorn2.0";

    public String getBasicAuth() throws UnsupportedEncodingException {
        String encoded = "Basic ";
        String b = dsUserName + ":" + dsPassword;
        encoded += Base64.getEncoder().encodeToString(b.getBytes(StandardCharsets.UTF_8));
        return encoded;
    }

    public String getServer() {
        String server = "localhost";
        return server;
    }

    public String getPort() {
        String port = "3306";
        return port;
    }

    public String getSchema() {
        String schema = "jevis";
        return schema;
    }

    public String getUser() {
        String user = "root";
        return user;
    }

    public String getPassword() {
        String password = "jevis";
        return password;
    }

    public String getDsUserName() {
        return dsUserName;
    }

    public String getDsPassword() {
        return dsPassword;
    }

    /**
     * @param theme
     * @param lang
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @DefaultValue("") @QueryParam("theme") String theme,
            @DefaultValue("") @QueryParam("lang") String lang
    ) throws Exception {
        JEVisDataSource ds = null;
        try {
            String nh = "";
            String nh2 = "";
            List<ThemeHelper> listThemes = new ArrayList<>();
            List<LangHelper> listLangs = new ArrayList<>();

            List<String> resourceFiles = getResourceFiles("themes/", "css");

            List<String> listLang = getResourceFiles("lang/", "json");

            for (String s : resourceFiles) {
                if (!s.equals(theme) && !("white.css".equals(s) && "".equals(theme))) {
                    ThemeHelper th = new ThemeHelper();
                    th.setFileName(s);
                    nh = s;
                    nh = nh.replace(nh.substring(nh.length() - 4), "");
                    nh = nh.substring(0, 1).toUpperCase() + nh.substring(1);
                    th.setName(nh);
                    listThemes.add(th);
                }
            }
            for (String s : listLang) {
                LangHelper lh = new LangHelper();
                lh.setFileName(s);
                nh2 = s;
                nh2 = nh2.replace(nh2.substring(nh2.length() - 5), "");
                lh.setFileName(nh2);
                nh2 = nh2.substring(0, 1).toUpperCase() + nh2.substring(1);
                lh.setName(nh2);
                listLangs.add(lh);
            }

            Map<String, Object> root = new HashMap<>();
            root.put("themes", listThemes);
            root.put("lang", listLangs);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("submit", t.getTranslatedKey(lang, "Login"));
                root.put("username", t.getTranslatedKey(lang, "Username"));
                root.put("password", t.getTranslatedKey(lang, "Password"));
            } else {
                root.put("submit", "Login");
                root.put("username", "Username");
                root.put("password", "Password");
            }

            if (theme.equals("")) {
                root.put("themeName", "White");
                root.put("themeValue", "white.css");
            } else {
                String themeName = theme;
                themeName = themeName.replace(themeName.substring(themeName.length() - 1), "");
                themeName = themeName.replace(themeName.substring(themeName.length() - 1), "");
                themeName = themeName.replace(themeName.substring(themeName.length() - 1), "");
                themeName = themeName.substring(0, 1).toUpperCase() + themeName.substring(1);
                root.put("themeName", themeName);
                root.put("themeValue", theme);
            }

            TemplateChooser tc = new TemplateChooser(root, "login");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    private List<String> getResourceFiles(String path, String extension) throws Exception {
        List<String> filenames = new ArrayList<>();

        //            InputStream is = getClass().getResourceAsStream(path);
//            logger.info("Inputstream: " + is.toString() + " from: " + path);
//            InputStreamReader isr = new InputStreamReader(is);
//            logger.info("InputstreamREader: " + isr.toString());
//            BufferedReader br = new BufferedReader(isr);
//            logger.info("Buffered Reader: " + br.toString());
//            String resource;
//
//            while ((resource = br.readLine()) != null) {
//                logger.info("Filename: " + resource);
//                filenames.add(resource);
//            }
//
//            br.close();
//            isr.close();
//            is.close();

        File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        if (jarFile.isFile()) {  // Run with JAR file
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && name.endsWith(extension)) { //filter according to the path
                    filenames.add(name.replace(path, ""));
                }
            }
            jar.close();
        } else { // Run with IDE
            URL url = Launcher.class.getResource("/" + path);
            if (url != null) {
                try {
                    File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        filenames.add(app.getName());
                    }
                } catch (URISyntaxException ex) {
                    // never happens
                }
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        //        final InputStream in
        //                = getContextClassLoader().getResourceAsStream(resource);

//        final InputStream in
//                = Login. getContextClassLoader().getResourceAsStream(resource);
        return Login.class.getResourceAsStream(resource);
//        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
