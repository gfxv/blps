package dev.gfxv.blps.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import dev.gfxv.blps.entity.User;
import dev.gfxv.blps.entity.Video;
import dev.gfxv.blps.entity.Withdrawal;
import dev.gfxv.blps.exception.UserNotFoundException;
import dev.gfxv.blps.jca.StripeConnection;
import dev.gfxv.blps.jca.StripeConnectionFactory;
import dev.gfxv.blps.payload.response.MonetizationStatsResponse;
import dev.gfxv.blps.repository.UserRepository;
import dev.gfxv.blps.repository.VideoRepository;
import dev.gfxv.blps.repository.WithdrawalRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    TransactionTemplate transactionTemplate;

    @Autowired
    private NotificationService notificationService;


    @Autowired
    private StripeConnectionFactory stripeConnectionFactory;


    @Autowired
    public MonetizationService(
            VideoRepository videoRepository,
            UserRepository userRepository,
            WithdrawalRepository withdrawalRepository,
            TransactionTemplate transactionTemplate
    ) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public boolean requestMonetization(String username) {
        return transactionTemplate.execute(status -> {
            try {
                User user = userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                return user.isMonetized();
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Failed to request monetization for user " + username + ": " + e.getMessage(), e);
            }
        });
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

    public void withdrawEarnings(Long userId, Double amount) {
        transactionTemplate.execute(status -> {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Failed to process withdrawal for user ID " + userId + ": " + e.getMessage(), e);
            }
        });
    }

    public boolean isEligibleForMonetization(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getSubscribers() < minSubs) {
            return false;
        }

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(testPeriod);
        List<Video> publicVideosLast90Days = videoRepository
                .findByOwnerIdAndVisibilityTrueAndCreatedAtAfter(userId, ninetyDaysAgo);
        return publicVideosLast90Days.size() >= 3;
    }

    @Scheduled(fixedRate = 14 * 24 * 60 * 60 * 1000) // каждые 2 недели
    @Transactional
    public void checkMonetiizationEligibility() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                if (!isEligibleForMonetization(user.getId())) {
                    user.setMonetized(false);
                    userRepository.save(user);
                    notificationService.notifyUser(
                            user.getId(),
                            "Монетизация отключена: вы больше не соответствуете требованиям"
                    );
                } else {
                    user.setMonetized(true);
                    userRepository.save(user);
                    notificationService.notifyUser(
                            user.getId(),
                            "Монетизация подключена: вы соответствуете требованиям"
                    );
                }
            } catch (Exception e) {
                System.err.println("Error checking monetization for user " + user.getId() + ": " + e.getMessage());
            }
        }
    }

    public void withdrawEarningsWithStripe(String userName, Double amount) {
        transactionTemplate.execute(status -> {
            try {
                User user = userRepository.findByUsername(userName)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

                if (!user.isMonetized()) {
                    throw new IllegalStateException("User is not monetized");
                }

                long amountCents = (long) (amount * 100);

                try (StripeConnection conn = stripeConnectionFactory.getConnection()) {
                    String payoutId = conn.createPayout(
                            "usd",
                            amountCents,
                            user.getStripeAccountId()
                    );

                    Withdrawal withdrawal = new Withdrawal();
                    withdrawal.setUser(user);
                    withdrawal.setAmount(amount);
                    withdrawal.setStripePayoutId(payoutId);
                    withdrawalRepository.save(withdrawal);
                }

                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Withdrawal failed: " + e.getMessage(), e);
            }
        });
    }

    @Scheduled(cron = "0 0/30 * * * ?") // Каждые 30 минут
    public void syncPayoutStatuses() {
        List<Withdrawal> pendingWithdrawals = withdrawalRepository.findByStatus("pending");

        try (StripeConnection conn = stripeConnectionFactory.getConnection()) {
            pendingWithdrawals.forEach(withdrawal -> {
                Payout payout;
                try {
                    payout = Payout.retrieve(withdrawal.getStripePayoutId());
                } catch (StripeException e) {
                    throw new RuntimeException(e);
                }
                withdrawal.setStatus(payout.getStatus());
                withdrawalRepository.save(withdrawal);
            });
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

}
