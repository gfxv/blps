package dev.gfxv.blps.service;


import dev.gfxv.blps.model.XmlUser;
import dev.gfxv.blps.repository.XmlUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class XmlUserService {

    @Autowired
    private XmlUserRepository xmlUserRepository;

    public List<String> getRolesByUsername(String username) {
        XmlUser user = xmlUserRepository.findXmlUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден: " + username);
        }

        return Arrays.stream(user.getRoles().split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }

    public XmlUser getUserByUsername(String username) {
        return xmlUserRepository.findXmlUserByUsername(username);
    }
}
