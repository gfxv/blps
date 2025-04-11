package dev.gfxv.blps.auth;

import dev.gfxv.blps.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password(),
                            Collections.emptyList()
                    )
            );

            String token = jwtUtils.generateToken(authentication);
            return ResponseEntity.ok().body(Collections.singletonMap("token", token));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Authentication failed"));
        }
    }

    public record LoginRequest(String username, String password) {}
}