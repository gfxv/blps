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
import dev.gfxv.blps.service.tx.TwoPhaseCommitService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
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
    TransactionTemplate transactionTemplate;
    TwoPhaseCommitService twoPhaseCommitService;

    @Autowired
    public VideoService(
            VideoRepository videoRepository,
            UserRepository userRepository,
            AdminAssignmentRepository adminAssignmentRepository,
            StorageService storageService,
            TransactionTemplate transactionTemplate,
            TwoPhaseCommitService twoPhaseCommitService
    ) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.adminAssignmentRepository = adminAssignmentRepository;
        this.storageService = storageService;
        this.transactionTemplate = transactionTemplate;
        this.twoPhaseCommitService = twoPhaseCommitService;
    }

    public VideoResponse getVideoById(Long id, String username) {
        Video video = videoRepository
                .findById(id)
                .orElseThrow(() -> new VideoNotFoundException("Video not found"));

        VideoResponse videoResponse = new VideoResponse(video);
        videoResponse.setStreamUrl("/videos/" + video.getId() + "/stream");
        if (video.isVisibility()) {
            updateVideoStatistics(video);
            return videoResponse;
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));
        if (canManageVideo(user.getId(), video)) {
            updateVideoStatistics(video);
            return videoResponse;
        }
        throw new VideoNotFoundException("No such video");
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
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));

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

    public VideoResponse createVideo(MultipartFile file, CreateVideoRequest request, String username) throws Exception {
        Video video = twoPhaseCommitService.saveFileAndMetadata(username, file, request);
        return new VideoResponse(video);
    }

    public VideoResponse updateVideo(Long videoId, UpdateVideoRequest updateDto, String username) {
        return transactionTemplate.execute(status -> {
            try {
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
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Failed to update video with ID " + videoId + ": " + e.getMessage(), e);
            }
        });
    }

    public void deleteVideo(Long videoId, String username) throws Exception {
        twoPhaseCommitService.deleteFileAndMetadata(videoId, username);
    }


    /* SUBSCRIPTION LOGIC */

    public void subscribeToChannel(Long channelId, String username) {
        User subscriber = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Subscriber not found"));

        if (channelId.equals(subscriber.getId())) {
            throw new IllegalArgumentException("Users cannot subscribe to themselves");
        }

        User channel = userRepository
                .findById(channelId)
                .orElseThrow(() -> new UserNotFoundException("Channel not found"));
        channel.setSubscribers(channel.getSubscribers() + 1);
        userRepository.save(subscriber);
    }

    public Long getSubscribers(Long channelId) {
        User channel = userRepository
                .findById(channelId)
                .orElseThrow(() -> new UserNotFoundException("Channel not found"));
        return channel.getSubscribers();
    }

    /* ADMIN ASSIGNMENT LOGIC */

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

    /* HELPER FUNCTIONS */

    private void updateVideoStatistics(Video video) {
        System.out.println("trying to update stats");

        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);

        User owner = video.getOwner();
        owner.setTotalViews(owner.getTotalViews() + 1);
        userRepository.save(owner);

        System.out.println("stats updated");
    }

    private boolean canManageVideo(Long userId, Video video) {
        if (userId == null) return false; // No authenticated user

        // Owner can view their own video
        if (video.getOwner().getId().equals(userId)) {
            return true;
        }

        // Admins of the channel can view
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
