/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import com.google.gson.Gson;
import org.apache.commons.net.util.Base64;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.joda.time.DateTime;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author fs
 */
@Logged
@Provider
public class RLF implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(RLF.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty("StartTime", new DateTime());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (logger.getLevel().equals(Level.TRACE)) {
            try {
                String username = getUserName(requestContext.getHeaderString("authorization"));
                DateTime now = new DateTime();
                DateTime start = (DateTime) requestContext.getProperty("StartTime");
                logger.debug("[{}][{}][{}][{} ms] {}",
                        username,
                        requestContext.getRequest().getMethod(),
                        responseContext.getStatus(),
                        now.getMillis() - start.getMillis(),
                        requestContext.getUriInfo().getRequestUri()
                );

                if (requestContext.getMediaType() != null && requestContext.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    logger.trace("Payload: \n{}",
                            entityToString(requestContext.getEntityStream())
                    );
                }
                if (responseContext.getMediaType() != null && responseContext.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
                    logger.trace("Payload: \n{}",
                            (new Gson()).toJson(responseContext.getEntity())
                    );

                }

            } catch (Exception ex) {
                logger.trace("Logger error: ", ex);
            }
        }
    }

    private String entityToString(InputStream input) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final StringBuilder b = new StringBuilder();
            try {
                ReaderWriter.writeTo(input, out);

                byte[] requestEntity = out.toByteArray();
                if (requestEntity.length == 0) {
                    b.append("\n");
                } else {
                    b.append(new String(requestEntity));//charset?
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                out.close();
            }
            //trim
            if (b.length() > 200) {
                return b.substring(0, 200) + " ... " + (b.length() - 200) + " more chars";
            } else {
                return b.toString();
            }
        } catch (Exception ex) {
            logger.error(ex);
            return "- can not convert stream to string";
        }


    }

    private static String getUserName(String auth) {
        if (auth == null || auth.isEmpty()) {
            return "No User";
        }

        try {
            auth = auth.replaceFirst("[Bb]asic ", "");
            byte[] decoded = Base64.decodeBase64(auth);

            String decodeS = (new String(decoded, StandardCharsets.UTF_8));
            String[] dauth = decodeS.split(":");

            return dauth[0];
//            String password = dauth[1];

        } catch (Exception ex) {
            return "Error User";
        }
    }

}
