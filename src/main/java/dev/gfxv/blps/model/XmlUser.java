package dev.gfxv.blps.model;
import javax.xml.bind.annotation.XmlAttribute;

public class XmlUser {
    private String username;
    private String password;
    private String roles = "ROLE_USER";

    private String email;

    @XmlAttribute
    public String getUsername() { return username; }

    @XmlAttribute
    public String getPassword() { return password; }

    @XmlAttribute
    public String getEmail(){
        return email;
    }

    @XmlAttribute
    public String getRoles() {
        return roles != null ? roles : "";
    }

    public void setRoles(String roles) {
        this.roles = roles != null ? roles : "";
    }

    public void setEmail(String email){
        this.email = email;
    }


    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}