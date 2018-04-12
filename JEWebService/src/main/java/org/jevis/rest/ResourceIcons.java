/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.ws.sql.SQLDataSource;

/**
 *
 * @author fs
 */
@Path("/JEWebService/v1/classicons")
public class ResourceIcons {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceClasses.class);

    public final static String TEMPDIR = "JEWebService";
    public final static String TEMPFILE = "allIcons.zip";

    //    /tmpAdministration
    @GET
    @Logged
    @Produces({"application/zip"})
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ResourceIcons", "get");

            File tmpZipFile = new File(FileCache.CLASS_ICON_FILE);
            if (tmpZipFile.exists()) {
                ds.getProfiler().addEvent("ResourceIcons", "done cache");
                return Response.ok(new FileInputStream(tmpZipFile), MediaType.valueOf("application/zip")).build();
            } else {
                System.out.println("icon cache does not exists: " + tmpZipFile);
            }

            //else, create new file
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpZipFile));
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);//because we cache it

            byte[] buffer = new byte[1024];

            for (JsonJEVisClass jc : ds.getJEVisClasses()) {
                try {
                    BufferedImage icon = ds.getJEVisClassIcon(jc.getName());
                    if (icon != null) {
                        File outputfile = new File(FileCache.CACH_PATH + "/" + jc.getName() + ".png");
                        ImageIO.write(icon, "png", outputfile);
                        ZipEntry ze = new ZipEntry(outputfile.getName());
                        zos.putNextEntry(ze);
                        FileInputStream in = new FileInputStream(outputfile);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        in.close();
                        ds.getProfiler().addEvent("ResourceIcons", "done putting " + outputfile);
                        outputfile.delete();

                    }
                } catch (Exception ex) {
                    System.out.println("Class ison error for: "+jc.getName());
                }

            }
            zos.closeEntry();
            zos.close();

            Response re = Response.ok(new FileInputStream(tmpZipFile), MediaType.valueOf("application/zip")).build();
//            tmpfolder.delete();
            ds.getProfiler().addEvent("ResourceIcons", "done");
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
}
