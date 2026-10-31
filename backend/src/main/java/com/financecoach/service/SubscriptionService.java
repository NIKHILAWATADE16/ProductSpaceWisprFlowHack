package com.financecoach.service;

import com.financecoach.dto.DetectedSubscriptionDto;
import com.financecoach.dto.SubscriptionDto;
import com.financecoach.dto.SubscriptionRequest;
import com.financecoach.entity.BillingFrequency;
import com.financecoach.entity.Subscription;
import com.financecoach.entity.Transaction;
import com.financecoach.entity.TransactionType;
import com.financecoach.entity.User;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.TransactionRepository;
import com.financecoach.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository repo;
    private final UserRepository users;
    private final TransactionRepository txRepo;

    public SubscriptionService(SubscriptionRepository repo, UserRepository users, TransactionRepository txRepo) {
        this.repo = repo;
        this.users = users;
        this.txRepo = txRepo;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> list(Long uid, boolean activeOnly) {
        List<Subscription> subscriptions = activeOnly
                ? repo.findByUserIdAndActive(uid, true, Sort.by("nextPaymentDate"))
                : repo.findByUserId(uid, Sort.by("nextPaymentDate"));
        return subscriptions.stream().map(this::dto).toList();
    }

    @Transactional
    public SubscriptionDto create(Long uid, SubscriptionRequest request) {
        User user = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Subscription subscription = new Subscription(user, request.getName(), request.getAmount(),
                request.getBillingFrequency(), request.getNextPaymentDate(), request.getCategory(), true);
        return dto(repo.save(subscription));
    }

    @Transactional
    public SubscriptionDto setActive(Long uid, Long id, boolean active) {
        Subscription subscription = repo.findById(id)
                .filter(item -> item.getUser().getId().equals(uid))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        subscription.setActive(active);
        return dto(repo.save(subscription));
    }

    @Transactional
    public void delete(Long uid, Long id) {
        Subscription subscription = repo.findById(id)
                .filter(item -> item.getUser().getId().equals(uid))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        repo.delete(subscription);
    }

    /**
     * Scans transactions for recurring payments across months.
     * Groups by normalized merchant name, detects intervals (weekly, monthly, quarterly, yearly),
     * and verifies that amounts are within a 5% tolerance.
     */
    @Transactional(readOnly = true)
    public List<DetectedSubscriptionDto> detectSubscriptions(Long uid) {
        LocalDate since = LocalDate.now().minusDays(120);
        List<Transaction> transactions = txRepo.findByUserIdAndTransactionDateBetween(
                uid, since, LocalDate.now(), Sort.by("transactionDate").ascending());

        // Group expense transactions by normalized merchant
        Map<String, List<Transaction>> merchantGroups = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() != TransactionType.EXPENSE) continue;
            String normalized = normalizeMerchant(t.getDescription());
            merchantGroups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(t);
        }

        List<DetectedSubscriptionDto> detected = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : merchantGroups.entrySet()) {
            String merchant = entry.getKey();
            List<Transaction> group = entry.getValue();

            // Need at least 2 occurrences to establish a recurring pattern
            if (group.size() < 2) continue;

            // Sort chronologically
            group.sort(Comparator.comparing(Transaction::getTransactionDate));

            // Check intervals between consecutive transactions
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < group.size(); i++) {
                long days = ChronoUnit.DAYS.between(group.get(i - 1).getTransactionDate(), group.get(i).getTransactionDate());
                intervals.add(days);
            }

            // If any interval is less than 5 days, likely one-off purchases on same week
            boolean tooClose = intervals.stream().anyMatch(d -> d < 5);
            if (tooClose) continue;

            double avgInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
            BillingFrequency frequency;
            int nextIntervalDays;

            if (avgInterval >= 5 && avgInterval <= 9) {
                frequency = BillingFrequency.WEEKLY;
                nextIntervalDays = 7;
            } else if (avgInterval >= 24 && avgInterval <= 36) {
                frequency = BillingFrequency.MONTHLY;
                nextIntervalDays = 30;
            } else if (avgInterval >= 80 && avgInterval <= 100) {
                frequency = BillingFrequency.QUARTERLY;
                nextIntervalDays = 90;
            } else if (avgInterval >= 340 && avgInterval <= 380) {
                frequency = BillingFrequency.YEARLY;
                nextIntervalDays = 365;
            } else if (avgInterval >= 20 && avgInterval <= 45) {
                // Approximate monthly with slight timing variance
                frequency = BillingFrequency.MONTHLY;
                nextIntervalDays = 30;
            } else {
                // Irregular spacing, not a recognized subscription interval
                continue;
            }

            // Check amount consistency (within 5% tolerance)
            BigDecimal minAmount = group.stream().map(Transaction::getAmount).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxAmount = group.stream().map(Transaction::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            if (minAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal diff = maxAmount.subtract(minAmount);
            BigDecimal variance = diff.divide(minAmount, 4, RoundingMode.HALF_UP);

            if (variance.compareTo(BigDecimal.valueOf(0.05)) > 0) {
                // Amount variance exceeds 5%
                continue;
            }

            // Candidate identified
            Transaction latestTx = group.get(group.size() - 1);
            BigDecimal representativeAmount = latestTx.getAmount().setScale(2, RoundingMode.HALF_UP);
            String category = (latestTx.getCategory() != null && !latestTx.getCategory().isBlank())
                    ? latestTx.getCategory() : "Subscriptions";

            LocalDate nextPaymentDate = latestTx.getTransactionDate().plusDays(nextIntervalDays);
            while (nextPaymentDate.isBefore(LocalDate.now())) {
                nextPaymentDate = nextPaymentDate.plusDays(nextIntervalDays);
            }

            boolean alreadyTracked = repo.existsByUserIdAndNameIgnoreCase(uid, merchant);

            String confidence = (group.size() >= 3 && variance.compareTo(BigDecimal.valueOf(0.02)) <= 0)
                    ? "HIGH" : "MEDIUM";

            detected.add(new DetectedSubscriptionDto(
                    merchant,
                    representativeAmount,
                    frequency,
                    nextPaymentDate,
                    category,
                    group.size(),
                    confidence,
                    alreadyTracked
            ));
        }

        detected.sort(Comparator.comparing(DetectedSubscriptionDto::getName));
        return detected;
    }

    /**
     * Runs subscription detection and automatically registers any newly detected
     * subscriptions that aren't already tracked in the user's registry.
     */
    @Transactional
    public List<SubscriptionDto> autoSyncDetectedSubscriptions(Long uid) {
        User user = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<DetectedSubscriptionDto> detectedList = detectSubscriptions(uid);

        for (DetectedSubscriptionDto d : detectedList) {
            if (!d.isAlreadyTracked()) {
                Subscription sub = new Subscription(
                        user,
                        d.getName(),
                        d.getAmount(),
                        d.getBillingFrequency(),
                        d.getNextPaymentDate(),
                        d.getCategory(),
                        true
                );
                repo.save(sub);
                log.info("Auto-registered detected subscription: {} ({}) for user {}", d.getName(), d.getAmount(), uid);
            }
        }

        return list(uid, false);
    }

    public String normalizeMerchant(String description) {
        if (description == null || description.isBlank()) return "Unknown";
        String s = description.toUpperCase().trim();

        if (s.contains("NETFLIX")) return "Netflix";
        if (s.contains("SPOTIFY")) return "Spotify";
        if (s.contains("AMAZON PRIME") || s.contains("AMZN PRIME")) return "Amazon Prime";
        if (s.contains("APPLE.COM") || s.contains("APPLE MUSIC") || s.contains("APPLE ONE") || s.contains("ICLOUD")) return "Apple Services";
        if (s.contains("OPENAI") || s.contains("CHATGPT")) return "ChatGPT Plus";
        if (s.contains("PLANET FITNESS")) return "Planet Fitness";
        if (s.contains("DISNEY")) return "Disney+";
        if (s.contains("HULU")) return "Hulu";
        if (s.contains("HBO") || s.contains("MAX.COM")) return "Max";
        if (s.contains("YOUTUBE")) return "YouTube Premium";
        if (s.contains("GITHUB")) return "GitHub";
        if (s.contains("DROPBOX")) return "Dropbox";
        if (s.contains("AWS") || s.contains("AMAZON WEB")) return "AWS";
        if (s.contains("NY TIMES") || s.contains("NEW YORK TIMES")) return "NY Times";
        if (s.contains("FITNESS") || s.contains("GYM")) return "Gym Membership";

        // General cleaning: strip transaction noise prefixes & suffixes
        String cleaned = description.replaceAll("(?i)^(paypal\\s*\\*|pos\\s*debit|purchase\\s*at|autopay\\s*|recurring\\s*)", "");
        cleaned = cleaned.replaceAll("\\*.*$", "");
        cleaned = cleaned.replaceAll("#\\s*\\d+", "");
        cleaned = cleaned.replaceAll("\\d{4,}", ""); // Remove card numbers or terminal codes
        cleaned = cleaned.replaceAll("(?i)\\.com.*$", "");
        cleaned = cleaned.replaceAll("[_\\-]", " ");
        cleaned = cleaned.trim();

        if (cleaned.length() < 3) {
            cleaned = description.trim();
        }

        return capitalizeWords(cleaned);
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) {
                sb.append(w.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private SubscriptionDto dto(Subscription subscription) {
        return new SubscriptionDto(subscription.getId(), subscription.getName(), subscription.getAmount(),
                subscription.getBillingFrequency(), subscription.getNextPaymentDate(), subscription.getCategory(),
                subscription.isActive());
    }
}
