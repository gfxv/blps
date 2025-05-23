package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.*;
import dev.gfxv.blps.repository.AdminAssignmentRepository;
import dev.gfxv.blps.repository.CommentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CommentService {
    private static final Set<String> STOP_WORDS = Set.of("spam", "offensive", "banned");

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    AdminAssignmentRepository adminAssignmentRepository;

    public Comment addComment(String username, Comment comment, Long videoId) {
        Video video = videoService.getVideoById(videoId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        comment.setUserId(user.getId());
        comment.setVideo(video);
        comment.setStatus(CommentStatus.PENDING);
        comment = commentRepository.save(comment);

        if (containsStopWords(comment.getText())) {
            commentRepository.delete(comment);
            notificationService.notifyUser(user.getId(), "Ваш комментарий был удален из-за нарушения правил.");
            throw new IllegalArgumentException("Комментарий содержит запрещенные слова и был удален.");
        }

        notificationService.notifyUser(user.getId(), "Ваш комментарий успешно сохранен.");
        return comment;
    }


    public List<Comment> getPendingComments() {
        return commentRepository.findByStatus(CommentStatus.PENDING);
    }

    public List<Comment> getVideoComments(Long videoId){
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
        if (userId == null) return false; // No authenticated user

        // Owner can view their own video
        if (video.getOwner().getId().equals(userId)) {
            return true;
        }

        // Admins of the channel can view
        List<AdminAssignment> assignments = adminAssignmentRepository.findByAdminId(userId);
        for (AdminAssignment assignment : assignments) {
            if (assignment.getChannel().getId().equals(video.getOwner().getId())) {
                return true;
            }
        }
        return false;
    }
}
