package dev.gfxv.blps.payload.request;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.entity.CommentStatus;
import dev.gfxv.blps.entity.Video;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentRequest {

    @NotNull
    private Long id;

    @NotBlank(message = "Text can't be empty")
    @Min(value = 1, message = "Comment must be at least 1 character long")
    private String text;

    @NotBlank
    private Long userId;

    @NotBlank
    private CommentStatus status;

    @NotBlank
    private LocalDateTime createdAt = LocalDateTime.now();

    public static CommentRequest from(Comment comment) {
        CommentRequest dto = new CommentRequest();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUserId());
        dto.setStatus(comment.getStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

}
