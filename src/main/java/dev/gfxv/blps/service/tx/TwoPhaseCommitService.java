package dev.gfxv.blps.service.tx;

import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.exception.VideoNotFoundException;
import dev.gfxv.blps.payload.request.CreateVideoRequest;
import dev.gfxv.blps.repository.AdminAssignmentRepository;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import dev.gfxv.blps.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class TwoPhaseCommitService {

    private PlatformTransactionManager transactionManager;
    private StorageService storageService;
    private VideoRepository videoRepository;
    private UserRepository userRepository;
    private AdminAssignmentRepository adminAssignmentRepository;

    public Video saveFileAndMetadata(String username, MultipartFile file, CreateVideoRequest request) throws Exception {
        String storageKey = StorageService.generateFileKey(file.getOriginalFilename());

        Video video = new Video();
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setMinioKey(storageKey);
        video.setVisibility(request.isVisibility());

        DatabaseSaveParticipant dbParticipant = new DatabaseSaveParticipant(
                username, video, transactionManager, videoRepository, userRepository
        );
        MinioSaveParticipant minioParticipant = new MinioSaveParticipant(
                storageService, storageKey, file.getInputStream(), file.getContentType()
        );

        List<TwoPhaseCommitParticipant> participants = Arrays.asList(
                dbParticipant, minioParticipant
        );
        runTransaction(participants);
        return video;
    }

    public void deleteFileAndMetadata(Long videoId, String username) {
        Video video = videoRepository
                .findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("Video with id " + videoId + " not found"));

        DatabaseDeleteParticipant dbParticipant = new DatabaseDeleteParticipant(
                video.getId(), username, transactionManager, videoRepository, userRepository, adminAssignmentRepository
        );

        MinioDeleteParticipant minioParticipant = new MinioDeleteParticipant(
                video.getMinioKey(), storageService
        );
        List<TwoPhaseCommitParticipant> participants = Arrays.asList(dbParticipant, minioParticipant);
        runTransaction(participants);
    }

    private void runTransaction(List<TwoPhaseCommitParticipant> participants) {

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        boolean allPrepared = true;
        for (TwoPhaseCommitParticipant participant : participants) {
            if (!participant.prepare()) {
                allPrepared = false;
                break;
            }
        }

        if (!allPrepared) {
            rollbackPhase(participants);
            throw new RuntimeException("2PC failed during commit-request phase");
        }
        commitPhase(participants);
    }

    private void commitPhase(List<TwoPhaseCommitParticipant> participants) {
        for (TwoPhaseCommitParticipant participant : participants) {
            participant.commit();
        }
    }

    private void rollbackPhase(List<TwoPhaseCommitParticipant> participants) {
        for (TwoPhaseCommitParticipant participant : participants) {
            participant.rollback();
        }
    }

}
