package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.AdminAssignment;
import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.exception.UserNotFoundException;
import dev.gfxv.blps.exception.VideoNotFoundException;
import dev.gfxv.blps.payload.request.CreateVideoRequest;
import dev.gfxv.blps.payload.request.UpdateVideoRequest;
import dev.gfxv.blps.payload.response.VideoResponse;
import dev.gfxv.blps.repository.AdminAssignmentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {

    VideoRepository videoRepository;
    UserRepository userRepository;
    AdminAssignmentRepository adminAssignmentRepository;
    StorageService storageService;

    @Autowired
    public VideoService (
            VideoRepository videoRepository,
            UserRepository userRepository,
            AdminAssignmentRepository adminAssignmentRepository,
            StorageService storageService) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.adminAssignmentRepository = adminAssignmentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public VideoResponse createVideo(MultipartFile file, CreateVideoRequest request, String username) {
        try {
            String objectName = storageService.uploadVideo(
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getContentType()
            );

            System.out.println("Object Name: " + objectName);

            Video video = new Video();
            video.setTitle(request.getTitle());
            video.setDescription(request.getDescription());
            video.setOwner(userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"))
            );
            video.setMinioKey(objectName);
            video.setVisibility(request.isVisibility());
            videoRepository.save(video);

            System.out.println("Vide saved to database");

            return new VideoResponse(video);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create video: " + e.getMessage());
        }
    }



}
