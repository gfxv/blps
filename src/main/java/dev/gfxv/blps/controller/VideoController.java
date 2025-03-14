package dev.gfxv.blps.controller;

import dev.gfxv.blps.payload.request.CreateVideoRequest;
import dev.gfxv.blps.payload.request.UpdateVideoRequest;
import dev.gfxv.blps.payload.response.VideoResponse;
import dev.gfxv.blps.service.VideoService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoController {

    VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/public")
    public ResponseEntity<List<VideoResponse>> getPublicVideos() {
        List<VideoResponse> videos = videoService.getPublicVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/channels/{channelId}")
    public ResponseEntity<List<VideoResponse>> getChannelVideos(
            @PathVariable Long channelId,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        List<VideoResponse> videos = videoService.getChannelVideos(channelId, username);
        return ResponseEntity.ok(videos);
    }

    @PostMapping
    public ResponseEntity<?> createVideo(
            @RequestPart MultipartFile file,
            @RequestPart CreateVideoRequest details,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        VideoResponse response = videoService.createVideo(file, details, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResponse> updateVideo(
            @PathVariable Long id,
            @RequestBody UpdateVideoRequest request,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        VideoResponse response = videoService.updateVideo(id, request, username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        videoService.deleteVideo(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/channels/{channelId}/assign-admin")
    public ResponseEntity<Void> assignAdminToChannel(
            @PathVariable Long channelId,
            @RequestParam Long adminId,
            Authentication authentication
    ) {
        String currentUsername = getUsernameFromAuthentication(authentication);
        videoService.assignAdminToChannel(channelId, adminId, currentUsername);
        return ResponseEntity.ok().build();
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }
}
