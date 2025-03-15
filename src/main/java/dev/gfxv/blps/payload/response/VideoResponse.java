package dev.gfxv.blps.payload.response;

import dev.gfxv.blps.entity.Video;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.time.LocalDateTime;

@Data
public class VideoResponse {

    private Long id;
    private String title;
    private String description;
    private Long ownerId;
    private boolean visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private String streamUrl;

    public VideoResponse(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.ownerId = video.getOwner().getId();
        this.visibility = video.isVisibility();
        this.createdAt = video.getCreatedAt();
        this.updatedAt = video.getUpdatedAt();
        this.views = video.getViewCount();
    }
}
