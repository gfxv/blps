package dev.gfxv.blps.repository;

import dev.gfxv.blps.entity.Comment;
import dev.gfxv.blps.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByStatus(CommentStatus status);

}