package dev.gfxv.blps.service.tx;

import dev.gfxv.blps.service.StorageService;

import java.io.InputStream;

public class MinioSaveParticipant implements TwoPhaseCommitParticipant {

    private final StorageService storageService;
    private final String filename;
    private final InputStream inputStream;
    private final String contentType;
    private boolean uploaded = false;

    public MinioSaveParticipant(
            StorageService storageService,
            String filename,
            InputStream inputStream,
            String contentType
    ) {
        this.storageService = storageService;
        this.filename = filename;
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    @Override
    public boolean prepare() {
        try {
            storageService.uploadVideo(
                    filename, inputStream, contentType
            );
            uploaded = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void commit() {
        // file is already uploaded, no action needed
    }

    @Override
    public void rollback() {
        if (uploaded) {
            try {
                storageService.deleteVideo(filename);
            } catch (Exception e) {
                System.out.println("Failed to clean up video: " + filename);
            }
        }
    }
}
