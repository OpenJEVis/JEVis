package org.jevis.commons.ldap;

public class LDAPClientSpring {
/*
    private final LdapTemplate ldapTemplate;


    public LDAPClientSpring() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl("ldap://localhost:10389");
        ldapContextSource.setUserDn("cn=JEVis js. System, OU=Applications, DC=ad,DC=jevis,DC=org");
        ldapContextSource.setPassword("Ldap!ldap");
        ldapContextSource.afterPropertiesSet();
        this.ldapTemplate = new LdapTemplate(ldapContextSource);


    }

    public static void main(String[] args) {
        LDAPClientSpring ldapAuth = new LDAPClientSpring();


        String username = "jsc";
        String password = "Ldap!ldap";
        String groupName = "JEVis Admin";

        if (ldapAuth.authenticate(username, password)) {
            System.out.println("User authenticated successfully.");
            if (ldapAuth.isUserInGroup(username, groupName)) {
                System.out.println("User is in the group.");
            } else {
                System.out.println("User is not in the group.");
            }
        } else {
            System.out.println("Authentication failed.");
        }

    }

    public boolean authenticate(String username, String password) {
        Filter filter = new EqualsFilter("uid", username);
        return ldapTemplate.authenticate("", filter.encode(), password);
    }


    public boolean isUserInGroup(String username, String groupName) {
        String groupDn = "cn=" + groupName + ",ou=groups";
        Filter filter = new EqualsFilter("member", "uid=" + username + ",ou=users");
        List<String> result = ldapTemplate.search(groupDn, filter.encode(), (DirContext ctx) -> ctx.getNameInNamespace());
        return !result.isEmpty();
    }

     */

}
