package dev.gfxv.blps.service.tx;

import dev.gfxv.blps.service.StorageService;

public class MinioDeleteParticipant implements TwoPhaseCommitParticipant {

    private boolean exists;
    private final String filename;
    private final StorageService storageService;

    public MinioDeleteParticipant(
            String filename,
            StorageService storageService
    ) {
        this.filename = filename;
        this.storageService = storageService;
    }

    @Override
    public boolean prepare() {
        try {
            exists = storageService.objectExists(filename);
        } catch (Exception e) {
            System.out.println("Failed to prepare minio delete action: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void commit() {
        if (!exists) {
            return;
        }
        try {
            storageService.deleteVideo(filename);
        } catch (Exception e) {
            System.out.println("Failed to delete delete video: " + e.getMessage());
        }
    }

    @Override
    public void rollback() {
        System.out.println("MinioDeleteParticipant Rollback called on file " + filename);
        // ???
    }
}
