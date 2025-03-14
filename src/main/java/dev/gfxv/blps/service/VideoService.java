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
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoService {

    VideoRepository videoRepository;
    UserRepository userRepository;
    AdminAssignmentRepository adminAssignmentRepository;
    StorageService storageService;

    @Autowired
    public VideoService(
            VideoRepository videoRepository,
            UserRepository userRepository,
            AdminAssignmentRepository adminAssignmentRepository,
            StorageService storageService) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.adminAssignmentRepository = adminAssignmentRepository;
        this.storageService = storageService;
    }

    public List<VideoResponse> getPublicVideos() {
        List<Video> publicVideos = videoRepository.findByVisibilityTrue();
        return publicVideos.stream()
                .map(VideoResponse::new)
                .collect(Collectors.toList());
    }

    public List<VideoResponse> getChannelVideos(Long channelId, String username) {
        User channelOwner = userRepository.findById(channelId)
                .orElseThrow(() -> new UserNotFoundException("Channel not found"));

        if (username.isEmpty()) {
            return getChannelPublicVideos(channelId);
        }

        User currentUser = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // check if owner or channel admin
        if (canManageVideo(currentUser.getId(), channelId, channelOwner.getId())) {
            // return all videos (public and hidden) for the channel
            List<Video> allChannelVideos = videoRepository.findByOwnerId(channelId);
            return allChannelVideos.stream()
                    .map(VideoResponse::new)
                    .collect(Collectors.toList());
        }

        return getChannelPublicVideos(channelId);
    }

    private List<VideoResponse> getChannelPublicVideos(Long channelId) {
        List<Video> publicChannelVideos = videoRepository.findByOwnerIdAndVisibilityTrue(channelId);
        return publicChannelVideos.stream()
                .map(VideoResponse::new)
                .collect(Collectors.toList());
    }

    public Video getVideoById(Long videoId){
        return videoRepository.findVideoById(videoId);
    }


    @Transactional
    public VideoResponse createVideo(MultipartFile file, CreateVideoRequest request, String username) {
        try {
            String objectName = storageService.uploadVideo(
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getContentType()
            );
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
            return new VideoResponse(video);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create video: " + e.getMessage());
        }
    }

    @Transactional
    public VideoResponse updateVideo(Long videoId, UpdateVideoRequest updateDto, String username) {
        Video video = videoRepository
                .findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("Video not found"));

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));

        if (!canManageVideo(user.getId(), video)) {
            throw new AccessDeniedException("You do not have permission to manage this video");
        }

        if (updateDto.getTitle() != null) {
            video.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            video.setDescription(updateDto.getDescription());
        }
        if (updateDto.getVisibility() != null) {
            video.setVisibility(updateDto.getVisibility());
        }

        videoRepository.save(video);
        return new VideoResponse(video);
    }

    @Transactional
    public void deleteVideo(Long videoId, String username) {
        Video video = videoRepository
                .findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("Video not found"));

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));

        if (!canManageVideo(user.getId(), video)) {
            throw new AccessDeniedException("You do not have permission to manage this video");
        }

        try {
            storageService.deleteVideo(video.getMinioKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        videoRepository.delete(video);
    }

    public void assignAdminToChannel(Long channelId, Long adminId, String currentUsername) {
        User channel = userRepository
                .findById(channelId)
                .orElseThrow(() -> new UserNotFoundException("Channel not found"));
        User admin = userRepository
                .findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));
        User currentUser = userRepository
                .findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User " + currentUsername + " not found"));

        // only the channel owner can assign admins
        if (!channel.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the channel owner can assign admins");
        }

        AdminAssignment assignment = new AdminAssignment();
        assignment.setAdmin(admin);
        assignment.setChannel(channel);
        adminAssignmentRepository.save(assignment);
    }

    private boolean canManageVideo(Long userId, Video video) {
        // check if the user is the video owner
        if (video.getOwner().getId().equals(userId)) {
            return true;
        }

        // check if the user is an admin for the video owner's channel
        List<AdminAssignment> assignments = adminAssignmentRepository.findByAdminId(userId);
        for (AdminAssignment assignment : assignments) {
            if (assignment.getChannel().getId().equals(video.getOwner().getId())) {
                return true;
            }
        }

        return false;
    }

    private boolean canManageVideo(Long userId, Long channelId, Long channelOwnerId) {
        return channelOwnerId.equals(userId) ||
                adminAssignmentRepository.findByAdminId(userId)
                        .stream()
                        .anyMatch(assignment -> assignment.getChannel().getId().equals(channelId));
    }


}
