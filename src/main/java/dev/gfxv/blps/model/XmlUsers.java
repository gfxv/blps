package dev.gfxv.blps.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "users")
public class XmlUsers {
    private List<XmlUser> users;

    @XmlElement(name = "user")
    public List<XmlUser> getUsers() { return users; }
    public void setUsers(List<XmlUser> users) { this.users = users; }
}