package dev.gfxv.blps.payload.request;

import lombok.Data;

@Data
public class CreateVideoRequest {
    private String title;
    private String description;
    private boolean visibility;
}
