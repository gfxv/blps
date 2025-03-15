package dev.gfxv.blps.payload.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
public class LoginRequest {
    private String username;
    private String password;
}