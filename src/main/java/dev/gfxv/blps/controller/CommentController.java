package dev.gfxv.blps.controller;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/add/{video_id}")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment, @RequestHeader("Authorization") String token, Long video_id) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok(commentService.addComment(token,comment,video_id));
    }


    @GetMapping("/pending")
    public ResponseEntity<List<Comment>> getPendingComments() {

        return ResponseEntity.ok(commentService.getPendingComments());
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Comment> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.approveComment(id));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<Comment> rejectComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.rejectComment(id));
    }
}


