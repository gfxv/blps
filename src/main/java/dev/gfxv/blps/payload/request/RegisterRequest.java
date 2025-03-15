package dev.gfxv.blps.payload.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
