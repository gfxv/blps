package dev.gfxv.blps.service;

import dev.gfxv.blps.auth.AuthController;
import dev.gfxv.blps.entity.Role;
import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.model.XmlUser;
import dev.gfxv.blps.model.XmlUsers;
import dev.gfxv.blps.repository.RoleRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Value("${app.users-xml-path}")
    private String usersXmlPath;

    AuthenticationManager authenticationManager;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;

    @Autowired
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(AuthController.UserRegistrationRequest request) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
        //File xmlFile = new ClassPathResource("users.xml").getFile();

        File xmlFile = new File(usersXmlPath);
        System.out.println("Real file path: " + xmlFile.getAbsolutePath());
        XmlUsers xmlUsers = (XmlUsers) context.createUnmarshaller().unmarshal(xmlFile);


        AuthController.UserRegistrationRequest finalRequest = request;
        Optional<XmlUser> existingUser = xmlUsers.getUsers().stream()
                .filter(u -> u.getUsername().equals(finalRequest.username()))
                .findFirst();

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }


        XmlUser newUser = new XmlUser();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        if (request.roles() == null || request.roles().isEmpty()) {
            request = new AuthController.UserRegistrationRequest(
                    request.username(),
                    request.password(),
                    List.of("ROLE_USER")
            );
        } else {
            newUser.setRoles(String.join(",", request.roles()));
        }

        xmlUsers.getUsers().add(newUser);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(xmlUsers, xmlFile);

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(newUser.getPassword());
        user.setEmail(request.username());
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        System.out.println("File saved to: " + xmlFile.getAbsolutePath());
        System.out.println("File size: " + xmlFile.length());

        System.out.println(xmlUsers.getUsers());
    }

}
