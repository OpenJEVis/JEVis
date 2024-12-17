package org.jevis.rest;

import org.glassfish.grizzly.http.util.MimeType;
import org.jevis.commons.ws.sql.Config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.File;
import java.nio.file.Files;

@Path("/JEWebService/v1/installer/")
public class ResourceInstaller {

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{s:.*}")
    @Context
    public Response getFile(@Context HttpHeaders httpHeaders,
                            @Context Request request,
                            @Context UriInfo uri) {
        try {

            // String idStr = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
            String idStr = uri.getPath().split("/JEWebService/v1/installer/")[1];
            File file = new File(Config.getInstallerDir() + "/" + idStr);
            File basePath = new File(Config.getInstallerDir());
            if (!file.getCanonicalPath().startsWith(basePath.getCanonicalPath())) {
                /* just a additional check that we don't allow some kind of escape*/
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("File not found").build();
            }
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String range = httpHeaders.getHeaderString("Range");
            String mimeType = MimeType.get(getFileExtension(file.getName()), "text/html");

            if (range != null && range.startsWith("bytes=")) {
                String[] parts = range.substring(6).split("-");
                int start = Integer.parseInt(parts[0]);
                int end = parts.length > 1 ? Integer.parseInt(parts[1]) : fileBytes.length - 1;
                byte[] rangeData = new byte[end - start + 1];
                System.arraycopy(fileBytes, start, rangeData, 0, rangeData.length);
                return Response.status(Response.Status.PARTIAL_CONTENT)
                        .entity(rangeData)
                        .header("Content-Range", "bytes " + start + "-" + end + "/" + fileBytes.length)
                        .header("Content-Type", mimeType)
                        .build();
            } else {
                if (mimeType.equals("text/html")) {
                    return Response.ok(file).header("Content-Type", mimeType).header("Accept-Ranges", "bytes").build();
                }

                return Response.ok(file)//, MediaType.APPLICATION_OCTET_STREAM
                        .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"") //optional
                        .header("Accept-Ranges", "bytes")
                        .header("Content-Type", mimeType)
                        .build();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("File not found").build();
        }

    }

    String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
