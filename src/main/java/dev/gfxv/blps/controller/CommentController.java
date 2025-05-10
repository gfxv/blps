package dev.gfxv.blps.controller;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add/{videoId}")
    public ResponseEntity<String> addComment(
            @RequestBody Comment comment,
            Authentication authentication,
            @PathVariable Long videoId
    ) {
        try {
            String username = getUsernameFromAuthentication(authentication);
            commentService.addComment(username, comment, videoId);
            return ResponseEntity.ok("Комментарий успешно добавлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Comment>> getPendingComments() {
        return ResponseEntity.ok(commentService.getPendingComments());
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<List<Comment>> getVideoComments(@PathVariable Long videoId) {
        return ResponseEntity.ok(commentService.getVideoComments(videoId));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String username = getUsernameFromAuthentication(authentication);

            commentService.approveComment(id, username);
            return ResponseEntity.ok("Комментарий успешно одобрен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String username = getUsernameFromAuthentication(authentication);
            commentService.rejectComment(id, username);
            return ResponseEntity.ok("Комментарий успешно удален");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }
}