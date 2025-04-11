package dev.gfxv.blps.auth;

import dev.gfxv.blps.model.XmlUser;
import dev.gfxv.blps.model.XmlUsers;
import dev.gfxv.blps.payload.request.LoginRequest;
import dev.gfxv.blps.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return jwtUtils.generateToken(request.getUsername(), authorities);
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRegistrationRequest request) throws Exception {

        JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
        File xmlFile = new ClassPathResource("users.xml").getFile();
        XmlUsers xmlUsers = (XmlUsers) context.createUnmarshaller().unmarshal(xmlFile);


        UserRegistrationRequest finalRequest = request;
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
            request = new UserRegistrationRequest(
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

        System.out.println(xmlUsers.getUsers());

        return "User registered successfully";
    }

    public record UserRegistrationRequest(
            String username,
            String password,
            List<String> roles
    ) {}
}