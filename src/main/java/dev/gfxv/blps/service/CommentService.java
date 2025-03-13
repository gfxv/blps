package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.entity.CommentStatus;
import dev.gfxv.blps.repository.CommentRepository;
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
    private NotificationService notificationService;
    public Comment addComment(Comment comment) {
        comment.setStatus(CommentStatus.PENDING);
        comment = commentRepository.save(comment);

        if (containsStopWords(comment.getText())) {
            commentRepository.delete(comment);
            notificationService.notifyUser(comment.getUserId(), "Ваш комментарий был удален из-за нарушения правил.");
            throw new IllegalArgumentException("Комментарий содержит запрещенные слова и был удален.");
        }

        notificationService.notifyUser(comment.getUserId(), "Ваш комментарий успешно сохранен и ожидает модерации.");
        return comment;
    }



    public List<Comment> getPendingComments() {
        return commentRepository.findByStatus(CommentStatus.PENDING);
    }

    public Comment approveComment(Long id) {
        return updateCommentStatus(id, CommentStatus.APPROVED);
    }

    public Comment rejectComment(Long id) {
        return updateCommentStatus(id, CommentStatus.REJECTED);
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
