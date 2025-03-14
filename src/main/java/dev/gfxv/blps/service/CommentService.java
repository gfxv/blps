package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.entity.CommentStatus;
import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.repository.CommentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Comment addComment(String token, Comment comment, Long videoId) {
        String username = jwtUtil.getUsernameFromJwtToken(token);
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

    public Comment approveComment(Long id, String token) {
        List<String> roles = jwtUtil.getRolesFromJwtToken(token);
        if (!roles.contains("ROLE_MODERATOR")){
            System.out.println(roles.get(0));
            throw new SecurityException("У пользователя нет прав для выполнения этой операции");
        }
        return updateCommentStatus(id, CommentStatus.APPROVED);
    }

    public void rejectComment(Long id, String token) {
        List<String> roles = jwtUtil.getRolesFromJwtToken(token);
        if (!roles.contains("ROLE_MODERATOR")){
            throw new SecurityException("У пользователя нет прав для выполнения этой операции");
        }
        //return updateCommentStatus(id, CommentStatus.REJECTED);
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
}
