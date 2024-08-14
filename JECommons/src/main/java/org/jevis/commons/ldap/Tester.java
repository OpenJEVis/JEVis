package org.jevis.commons.ldap;

public class Tester {

    public static void main(String[] args) {
        login();
    }

    public static void login() {
        try {
            //LdapClient ldapClient = LdapClient.builder().build();
            //ldapClient.authenticate().query(query().where("uid").is("john.doe")).password("secret").execute();

            System.out.println("Login: " + SimpleDirectory.connect("cn=jsc", "secret", "system", "localhost"));
            //System.out.println("Login: " + SimpleDirectory.connect("uid=admin", "secret", "system", "localhost"));

            //LdapContext ctx = ActiveDirectory.getConnection("flo", "test", "system", "127.0.0.1");
            //ctx.close();
        } catch (Exception e) {
            //Failed to authenticate user!
            e.printStackTrace();
        }
    }

}
