package dev.gfxv.blps.payload.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
public class UpdateVideoRequest {

    private String title;
    private String description;
    private Boolean visibility; // nullable to allow partial updates
}
