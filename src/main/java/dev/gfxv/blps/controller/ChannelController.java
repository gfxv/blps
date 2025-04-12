package dev.gfxv.blps.controller;

import dev.gfxv.blps.payload.response.UserInfoResponse;
import dev.gfxv.blps.payload.response.VideoResponse;
import dev.gfxv.blps.service.VideoService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelController {

    VideoService videoService;


    @Autowired
    public ChannelController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<List<VideoResponse>> getChannelVideos(
            @PathVariable Long channelId,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        List<VideoResponse> videos = videoService.getChannelVideos(channelId, username);
        return ResponseEntity.ok(videos);
    }

    @PostMapping("/{channelId}/subscribe")
    public ResponseEntity<Void> subscribeToChannel(
            @PathVariable Long channelId,
            Authentication authentication
    ) {
        String username = getUsernameFromAuthentication(authentication);
        videoService.subscribeToChannel(channelId, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{channelId}/subscribers")
    public ResponseEntity<?> getSubscribers(
            @PathVariable Long channelId
    ) {
        Long subscribers = videoService.getSubscribers(channelId);
        return ResponseEntity.ok(subscribers);
    }

    @PostMapping("/{channelId}/assign-admin")
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
