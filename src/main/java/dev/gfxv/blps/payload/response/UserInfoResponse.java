package dev.gfxv.blps.payload.response;

import dev.gfxv.blps.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;

    public UserInfoResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}

