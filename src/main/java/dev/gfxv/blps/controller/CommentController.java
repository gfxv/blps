package dev.gfxv.blps.controller;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/add/{videoId}")
    public ResponseEntity<String> addComment(@RequestBody Comment comment, @RequestHeader("Authorization") String token, @PathVariable Long videoId) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            commentService.addComment(token, comment, videoId);
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
    public ResponseEntity<String> approveComment(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try{
            commentService.approveComment(id, token);
            return ResponseEntity.ok("Комментарий успешно одобрен");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectComment(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try{
            commentService.rejectComment(id, token);
            return ResponseEntity.ok("Комментарий успешно удален");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}


