package dev.gfxv.blps.service.tx;

public interface TwoPhaseCommitParticipant {
    boolean prepare();
    void commit();
    void rollback();
}
