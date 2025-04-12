package dev.gfxv.blps.auth;

import dev.gfxv.blps.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.jaas.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.authentication.jaas.JaasAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JaasAuthenticationCallbackHandler jaasNameCallbackHandler() {
        return new JaasNameCallbackHandler();
    }

    @Bean
    public JaasAuthenticationCallbackHandler jaasPasswordCallbackHandler() {
        return new JaasPasswordCallbackHandler();
    }

    @Bean
    public AuthorityGranter authorityGranter() {
        return principal -> {
            Set<String> roles = new HashSet<>();
            if (principal instanceof RolePrincipal) {
                roles.add(principal.getName());
            }
            return roles;
        };
    }
    @Bean
    public JaasAuthenticationProvider jaasAuthenticationProvider(
            AuthorityGranter authorityGranter,
            JaasAuthenticationCallbackHandler jaasNameCallbackHandler,
            JaasAuthenticationCallbackHandler jaasPasswordCallbackHandler
    ) {
        JaasAuthenticationProvider provider = new JaasAuthenticationProvider();
        provider.setAuthorityGranters(new AuthorityGranter[] { authorityGranter });
        provider.setCallbackHandlers(
                new JaasAuthenticationCallbackHandler[] {
                        jaasNameCallbackHandler,
                        jaasPasswordCallbackHandler,
                }
        );
        provider.setLoginConfig(new ClassPathResource("jaas.config"));
        provider.setLoginContextName("MyJaasAuth");


        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            JaasAuthenticationProvider jaasAuthenticationProvider
    ) {
        return new ProviderManager(
                Collections.singletonList(jaasAuthenticationProvider)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/videos/{}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/channels/{}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/{}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/comments/approve/**").hasAnyRole("ROLE_MODERATOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/comments/reject/**").hasAnyRole("ROLE_MODERATOR", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }







}