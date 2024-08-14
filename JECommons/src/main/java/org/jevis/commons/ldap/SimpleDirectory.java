package org.jevis.commons.ldap;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

public class SimpleDirectory {


    public static boolean connect(String uid, String password, String ou, String server) {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_PRINCIPAL, uid + ", ou=" + ou);
        env.put(Context.SECURITY_CREDENTIALS, password);
        System.out.println("Env: " + env);
        try {
            DirContext connection = new InitialDirContext(env);
            System.out.println("connection: " + connection);
            return true;
        } catch (AuthenticationException authenticationException) {
            System.out.println("AuthenticationException: " + authenticationException.getMessage());
        } catch (NamingException namingException) {
            System.out.println("namingException: " + namingException.getMessage());
        }
        return false;
    }
}
