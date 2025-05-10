package dev.gfxv.blps.service.tx;

import dev.gfxv.blps.entity.AdminAssignment;
import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.exception.UserNotFoundException;
import dev.gfxv.blps.exception.VideoNotFoundException;
import dev.gfxv.blps.repository.AdminAssignmentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatabaseDeleteParticipant implements TwoPhaseCommitParticipant {

    TransactionStatus txStatus;
    Long videoId;
    String username;
    String storageKey;

    final PlatformTransactionManager transactionManager;
    final VideoRepository videoRepository;
    final UserRepository userRepository;
    final AdminAssignmentRepository adminAssignmentRepository;

    public DatabaseDeleteParticipant(
            Long videoId,
            String username,
            PlatformTransactionManager transactionManager,
            VideoRepository videoRepository,
            UserRepository userRepository,
            AdminAssignmentRepository adminAssignmentRepository
    ) {
        this.videoId = videoId;
        this.username = username;
        this.transactionManager = transactionManager;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.adminAssignmentRepository = adminAssignmentRepository;
    }

    @Override
    public boolean prepare() {
        try {
            txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

            Video video = videoRepository
                    .findById(videoId)
                    .orElseThrow(() -> new VideoNotFoundException("Video not found"));

            User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));

            if (!canManageVideo(user.getId(), video)) {
                throw new AccessDeniedException("You do not have permission to manage this video");
            }

            storageKey = video.getMinioKey();
            videoRepository.delete(video);
            return true;
        } catch (Exception e) {
            if (txStatus != null) {
                transactionManager.rollback(txStatus);
            }
            return false;
        }
    }

    @Override
    public void commit() {
        if (txStatus != null) {
            transactionManager.commit(txStatus);
        }
    }

    @Override
    public void rollback() {
        if (txStatus != null) {
            transactionManager.rollback(txStatus);
        }
    }

    public String getStorageKey() {
        return storageKey;
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
}
