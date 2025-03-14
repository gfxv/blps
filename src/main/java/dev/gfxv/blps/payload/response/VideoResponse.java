package dev.gfxv.blps.payload.response;

import dev.gfxv.blps.entity.Video;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoResponse {

    Long id;
    String title;
    String description;
    Long ownerId;
    boolean visibility;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public VideoResponse(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.ownerId = video.getOwner().getId();
        this.visibility = video.isVisibility();
        this.createdAt = video.getCreatedAt();
        this.updatedAt = video.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
