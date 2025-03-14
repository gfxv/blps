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

    @PostMapping
    public ResponseEntity<?> createVideo(
            @RequestPart MultipartFile file,
            @RequestPart CreateVideoRequest details,
            Authentication authentication
    ) {
        System.out.printf("Received: %s of %s \n", file.getOriginalFilename(), file.getContentType());
        String username = getUsernameFromAuthentication(authentication);
        System.out.println("Username: " + username);
        VideoResponse response = videoService.createVideo(file, details, username);
        return ResponseEntity.ok(response);
    }



    private String getUsernameFromAuthentication(Authentication authentication) {
        return authentication.getName();
    }
}
