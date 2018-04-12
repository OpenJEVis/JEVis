/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fs
 */
public class RequestLog {
    
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(RequestLog.class);
    
    public static void log(String user , Request request,long time, Response resonse,UriInfo url){
        logger.error("[{}][{}][{}][{} ms][{} byte] {}", user , request.getMethod(), resonse.getStatus(),time, resonse.getLength(),url.getRequestUri());

    }
    
}
