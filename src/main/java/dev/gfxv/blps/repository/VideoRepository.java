package dev.gfxv.blps.repository;

import dev.gfxv.blps.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    // get all public videos
    List<Video> findByVisibilityTrue();

    // get all videos by owner (for the owner to see both public and private videos)
    List<Video> findByOwnerId(Long ownerId);

    // get public videos for a specific owner
    List<Video> findByOwnerIdAndVisibilityTrue(Long ownerId);

    @Query("SELECT v FROM Video v WHERE v.owner.id = :ownerId AND v.visibility = true AND v.createdAt > :date")
    List<Video> findByOwnerIdAndVisibilityTrueAndCreatedAtAfter(Long ownerId, LocalDateTime date);

    Video findVideoById(Long id);
}