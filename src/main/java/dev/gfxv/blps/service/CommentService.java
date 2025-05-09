package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.*;
import dev.gfxv.blps.payload.request.CommentRequest;
import dev.gfxv.blps.repository.AdminAssignmentRepository;
import dev.gfxv.blps.repository.CommentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.service.jms.CommentSender;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class CommentService {
    private static final Set<String> STOP_WORDS = Set.of("spam", "offensive", "banned");

    private CommentSender commentSender;

    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private VideoService videoService;
    private AdminAssignmentRepository adminAssignmentRepository;

    public Comment addComment(String username, Comment comment, Long videoId) {
        Video video = videoService.getVideoById(videoId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        comment.setUserId(user.getId());
        comment.setVideo(video);
        comment.setStatus(CommentStatus.PENDING);
        comment = commentRepository.save(comment);

        notificationService.notifyUser(user.getId(), "Ваш комментарий успешно сохранен и ожидает модерации.");
        return comment;
    }

    @Scheduled(fixedRate = 60 * 1000) // 1 minute
    public void autoModerateComments() {
        // TODO: add pagination
        List<Comment> pendingComments = commentRepository.findByStatus(CommentStatus.PENDING);
        for (Comment comment : pendingComments) {
            var dto = CommentRequest.from(comment);
            commentSender.sendComment(dto);
        }
    }

    public List<Comment> getPendingComments() {
        return commentRepository.findByStatus(CommentStatus.PENDING);
    }

    public List<Comment> getVideoComments(Long videoId) {
        Video video = videoService.getVideoById(videoId);
        return commentRepository.findByVideo(video);
    }

    public Comment approveComment(Long id, String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        Comment comment = commentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with username: " + username));

        Video video = comment.getVideo();

        if (!canManageVideo(user.getId(), video) && !user.getRoles().contains("ROLE_GLOBAL_MODERATOR")) {
            throw new SecurityException("У пользователя нет прав для выполнения этой операции");
        }

        return updateCommentStatus(id, CommentStatus.APPROVED);
    }

    public void rejectComment(Long id, String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        Comment comment = commentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with username: " + username));

        Video video = comment.getVideo();

        if (!canManageVideo(user.getId(), video) && !user.getRoles().contains("ROLE_GLOBAL_MODERATOR")) {
            throw new SecurityException("У пользователя нет прав для выполнения этой операции");
        }

        commentRepository.deleteById(id);
    }

    private Comment updateCommentStatus(Long id, CommentStatus status) {
        return commentRepository.findById(id).map(comment -> {
            comment.setStatus(status);
            return commentRepository.save(comment);
        }).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    private boolean containsStopWords(String text) {
        return STOP_WORDS.stream().anyMatch(text.toLowerCase()::contains);
    }

    private boolean canManageVideo(Long userId, Video video) {
        if (userId == null) return false;
        if (video.getOwner().getId().equals(userId)) {
            return true;
        }
        List<AdminAssignment> assignments = adminAssignmentRepository.findByAdminId(userId);
        for (AdminAssignment assignment : assignments) {
            if (assignment.getChannel().getId().equals(video.getOwner().getId())) {
                return true;
            }
        }
        return false;
    }
}
