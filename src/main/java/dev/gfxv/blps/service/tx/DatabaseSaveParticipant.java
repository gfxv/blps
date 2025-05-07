package dev.gfxv.blps.service.tx;

import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.exception.UserNotFoundException;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatabaseSaveParticipant implements TwoPhaseCommitParticipant {

    String username;
    Video video;
    TransactionStatus txStatus;

    final PlatformTransactionManager transactionManager;
    final VideoRepository videoRepository;
    final UserRepository userRepository;

    public DatabaseSaveParticipant(
            String username,
            Video video,
            PlatformTransactionManager transactionManager,
            VideoRepository videoRepository,
            UserRepository userRepository
    ) {
        this.username = username;
        this.video = video;
        this.transactionManager = transactionManager;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean prepare() {
        try {
            txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

            video.setOwner(userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username))
            );

            videoRepository.saveAndFlush(video);
            return true;
        } catch (Exception e) {
            if (txStatus != null) {
                return false;
                // transactionManager.rollback(txStatus);
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
}
