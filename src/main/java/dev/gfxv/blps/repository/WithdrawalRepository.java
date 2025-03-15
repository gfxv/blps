package dev.gfxv.blps.repository;

import dev.gfxv.blps.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    List<Withdrawal> findByUserIdOrderByWithdrawalDateDesc(Long userId);
}