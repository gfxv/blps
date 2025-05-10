package dev.gfxv.blps.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name="video_id")
    private Video video;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

}

