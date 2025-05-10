package dev.gfxv.blps.auth;

import dev.gfxv.blps.model.XmlUser;
import dev.gfxv.blps.model.XmlUsers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.File;
import java.security.Principal;
import java.util.*;

import javax.xml.bind.JAXBContext;

import java.util.Map;

public class JaasLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> options;

    private String usersXmlPath;

    private String authenticatedUsername;
    private final List<Principal> principalsToAdd = new ArrayList<>();
    private boolean succeeded = false;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options
    ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
        this.usersXmlPath = (String) options.get("usersXmlPath");
    }

    @Override
    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback[2];
            callbacks[0] = new NameCallback("Username:");
            callbacks[1] = new PasswordCallback("Password:", false);
            callbackHandler.handle(callbacks);

            String inputUsername = ((NameCallback) callbacks[0]).getName();
            char[] inputPassword = ((PasswordCallback) callbacks[1]).getPassword();

            JAXBContext context = JAXBContext.newInstance(XmlUsers.class);
            File xmlFile = new File(usersXmlPath);
            if (!xmlFile.exists()) {
                throw new LoginException("File not found: " + usersXmlPath);
            }
            XmlUsers xmlUsers = (XmlUsers) context.createUnmarshaller().unmarshal(xmlFile);
            XmlUser user = xmlUsers.getUsers().stream()
                    .filter(u -> u.getUsername().equals(inputUsername))
                    .findFirst()
                    .orElseThrow(() -> new LoginException("User not found"));

            if (!passwordEncoder.matches(new String(inputPassword), user.getPassword())) {
                throw new LoginException("Invalid password");
            }

            this.succeeded = true;
            System.out.println("Password is valid");

            this.authenticatedUsername = inputUsername;
            if (user.getRoles() == null) {
                throw new LoginException("User has no roles assigned");
            }


            String[] roles = user.getRoles().split(",");
            for (String role : roles) {
                principalsToAdd.add(new RolePrincipal(role.trim()));
            }
            return true;


        } catch (Exception e) {
            e.printStackTrace();
            throw new LoginException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!succeeded) {
            return false;
        }

        if (subject.isReadOnly()) {
            throw new LoginException("Subject is Readonly, failed to add Principals");
        }
        for (Principal p : principalsToAdd) {
            if (!subject.getPrincipals().contains(p)) {
                subject.getPrincipals().add(p);
            }
        }

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }


}