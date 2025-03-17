package dev.gfxv.blps.service;

import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.entity.Withdrawal;
import dev.gfxv.blps.exception.UserNotFoundException;
import dev.gfxv.blps.payload.response.MonetizationStatsResponse;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import dev.gfxv.blps.repository.WithdrawalRepository;
import jakarta.transaction.Transactional;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonetizationService {


    @NonFinal
    @Value("${app.internal.monetization.min-subs}")
    Integer minSubs;

    @NonFinal
    @Value("${app.internal.monetization.test-period-days}")
    Integer testPeriod;

    @NonFinal
    @Value("${app.internal.monetization.monetization-ratio}")
    Double monetizationRatio;

    VideoRepository videoRepository;
    UserRepository userRepository;
    WithdrawalRepository withdrawalRepository;

    @Autowired
    public MonetizationService(
            VideoRepository videoRepository,
            UserRepository userRepository,
            WithdrawalRepository withdrawalRepository
    ) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.withdrawalRepository = withdrawalRepository;
    }

    @Transactional
    public boolean requestMonetization(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!isEligibleForMonetization(user.getId())) {
            return false;
        }

        user.setMonetized(true);
        userRepository.save(user);
        return true;
    }

    public MonetizationStatsResponse getMonetizationStats(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double totalEarnings = user.getTotalViews() * monetizationRatio;

        double earningsSinceLastWithdrawal;
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserIdOrderByWithdrawalDateDesc(user.getId());
        if (!withdrawals.isEmpty()) {
            Withdrawal lastWithdrawal = withdrawals.getFirst();
            earningsSinceLastWithdrawal = totalEarnings - lastWithdrawal.getAmount();
        } else {
            earningsSinceLastWithdrawal = totalEarnings;
        }

        return new MonetizationStatsResponse(totalEarnings, earningsSinceLastWithdrawal);
    }

    @Transactional
    public void withdrawEarnings(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMonetized()) {
            throw new IllegalStateException("User is not monetized");
        }

        double totalEarnings = user.getTotalViews() * monetizationRatio;
        if (amount > totalEarnings - user.getLastWithdrawalAmount()) {
            throw new IllegalArgumentException("Insufficient earnings for withdrawal");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setWithdrawalDate(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        user.setLastWithdrawalAmount(user.getLastWithdrawalAmount() + amount);
        userRepository.save(user);
    }

    public boolean isEligibleForMonetization(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // see: https://www.youtube.com/intl/en_us/creators/how-things-work/video-monetization/

        // check if the user has at least 500 subscribers
        if (user.getSubscribers() < minSubs) {
            return false;
        }

        // check if the user has at least 3 public uploads in the last 90 days
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(testPeriod);
        List<Video> publicVideosLast90Days = videoRepository
                .findByOwnerIdAndVisibilityTrueAndCreatedAtAfter(userId, ninetyDaysAgo);
        return publicVideosLast90Days.size() >= 3;
    }
}
