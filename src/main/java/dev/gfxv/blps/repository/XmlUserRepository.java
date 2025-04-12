package dev.gfxv.blps.repository;

import dev.gfxv.blps.model.XmlUser;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface XmlUserRepository {
   XmlUser findXmlUserByUsername(String username);
}
