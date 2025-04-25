package dev.gfxv.blps.auth;

import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.model.XmlUser;
import dev.gfxv.blps.model.XmlUsers;
import dev.gfxv.blps.payload.request.LoginRequest;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.security.JwtUtils;
import dev.gfxv.blps.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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


    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    private final AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        System.out.println("authentication: " + authentication);

        return jwtUtils.generateToken(request.getUsername(), authorities);
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRegistrationRequest request) throws Exception {
        authService.register(request);
        return "User registered successfully";
    }

    public record UserRegistrationRequest(
            String username,
            String password,
            List<String> roles
    ) {}
}