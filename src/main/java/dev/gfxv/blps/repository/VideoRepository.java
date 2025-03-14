package dev.gfxv.blps.repository;

import dev.gfxv.blps.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}