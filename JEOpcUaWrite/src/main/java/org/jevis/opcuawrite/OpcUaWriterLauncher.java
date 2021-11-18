/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.opcuawrite;

import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;

import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author broder
 */
public class OpcUaWriterLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(OpcUaWriterLauncher.class);
    private static Injector injector;
    private static final String APP_INFO = "JEOpcUaWriter";
    private final Command commands = new Command();
    private boolean firstRun = true;
    private OPCClient opcClient;

    public OpcUaWriterLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEOpcUaWriter-------");
        OpcUaWriterLauncher app = new OpcUaWriterLauncher(args, APP_INFO);
        app.execute();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEReport";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        System.out.println("Start Single Mode");

        for (Long id : ids) {
            JEVisObject loytecDataServer = null;

            try {
                logger.info("Try adding Single Mode for ID {}", id);
                loytecDataServer = ds.getObject(id);
                connectToOpc(loytecDataServer);
                System.out.println(loytecDataServer.getJEVisClass().getName());
            } catch (Exception ex) {
                logger.error("Could not find Object with id: {}", id);
            }

        }

        logger.info("Start Single Mode");


    }

    @Override
    protected void runServiceHelp() {

    }

    @Override
    protected void runComplete() {

    }

    public void connectToOpc(JEVisObject opcServerObj) throws InterruptedException, JEVisException, UaException, ExecutionException {


        OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);

        System.out.println(opcuaServer.getURL());

        OPCClient opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
        EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
        UsernameProvider usernameProvider = new UsernameProvider(opcServerObj.getAttribute("User").getLatestSample().getValue().toString(), opcServerObj.getAttribute("Password").getLatestSample().getValue().toString());
        opcClient.setEndpoints(endpointDescription);

        if (!opcServerObj.getAttribute("User").getLatestSample().getValue().toString().isEmpty() && !opcServerObj.getAttribute("Password").getLatestSample().getValue().toString().isEmpty()) {opcClient.setIdentification(usernameProvider);
            opcClient.connect();
        }










    }


}