package org.jevis.commons.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LDAPClient {

    private static final Logger log = LogManager.getLogger(LDAPClient.class);
    private final LDAPConnection connection;

    public LDAPClient() throws LDAPException {
        connection = new LDAPConnection();
        bindServer();

    }

    public boolean bindServer() throws LDAPException {
        connection.connect("192.168.178.46", 389);
        connection.bind("cn=JEVis js. System, OU=Applications, DC=ad,DC=jevis,DC=org", "Ldap!ldap");
        return true;
    }

    public void authUser(String username, String password) {
        SimpleBindRequest userBindRequest = new SimpleBindRequest(username, password);
        if (userBindRequest.getBindDN() == null) {
            log.warn("We got a null for the userBindRequest UserDN and therefore the bind is anonymous !");
        }
        if (userBindRequest.getPassword() == null) {
            log.warn("We got a null for the userBindRequest Password and therefore the bind is anonymous !");
        }
        System.out.println("User exists?");
        /*
        try {
            LDAPConnection userConnection = new LDAPConnection();
            //userConnection.bind(userDN, userPassword);
            //log.debug("Successful userConnection Bind as:" + userDN);
        } catch (LDAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

         */
    }

}
