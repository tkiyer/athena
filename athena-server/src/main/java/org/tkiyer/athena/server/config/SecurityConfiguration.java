package org.tkiyer.athena.server.config;

public class SecurityConfiguration {

    private String kerberosPrincipal;

    private String kerberosKeytab;

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

    public String getKerberosKeytab() {
        return kerberosKeytab;
    }

    public void setKerberosKeytab(String kerberosKeytab) {
        this.kerberosKeytab = kerberosKeytab;
    }
}
