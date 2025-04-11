package dev.gfxv.blps.model;
import javax.xml.bind.annotation.XmlAttribute;

public class XmlUser {
    private String username;
    private String password;
    private String roles;

    @XmlAttribute
    public String getUsername() { return username; }

    @XmlAttribute
    public String getPassword() { return password; }

    @XmlAttribute
    public String getRoles() { return roles; }


    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRoles(String roles) { this.roles = roles; }
}