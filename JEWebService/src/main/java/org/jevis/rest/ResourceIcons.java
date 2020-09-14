/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import org.apache.logging.log4j.LogManager;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author fs
 */
@Path("/JEWebService/v1/classicons")
public class ResourceIcons {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceIcons.class);

    public final static String TEMPDIR = "JEWebService";
    public final static String TEMPFILE = "allIcons.zip";
    private SQLDataSource ds = null;

    @GET
    @Logged
    @Produces({"application/zip"})
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            File tmpZipFile = new File(FileCache.CLASS_ICON_FILE);
            if (tmpZipFile.exists()) {
                return Response.ok(new FileInputStream(tmpZipFile), MediaType.valueOf("application/zip")).build();
            } else {
                tmpZipFile.getParentFile().mkdirs();
            }

            //else, create new file
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpZipFile));
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);//because we cache it

            byte[] buffer = new byte[1024];

            FileFilter ff = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".png");
                }
            };
            for (File imageFile : Config.getClassDir().listFiles(ff)) {
                ZipEntry ze = new ZipEntry(imageFile.getName());
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(imageFile);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();

            }
            zos.closeEntry();
            zos.close();

            Response re = Response.ok(new FileInputStream(tmpZipFile), MediaType.valueOf("application/zip")).build();

            return re;

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @PostConstruct
    public void postConstruct() {
        if (ds != null) {
            ds.clear();
            ds = null;
        }

    }
}
