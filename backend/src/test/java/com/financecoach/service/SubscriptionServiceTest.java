package com.financecoach.service;

import com.financecoach.dto.DetectedSubscriptionDto;
import com.financecoach.dto.SubscriptionDto;
import com.financecoach.dto.SubscriptionRequest;
import com.financecoach.entity.BillingFrequency;
import com.financecoach.entity.Subscription;
import com.financecoach.entity.Transaction;
import com.financecoach.entity.TransactionType;
import com.financecoach.entity.User;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.TransactionRepository;
import com.financecoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository repo;

    @Mock
    private UserRepository users;

    @Mock
    private TransactionRepository txRepo;

    private SubscriptionService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(repo, users, txRepo);
        testUser = new User("test@example.com", "hash", "Test User");
        testUser.setId(1L);
    }

    @Test
    void normalizesKnownMerchantsCorrectly() {
        assertEquals("Netflix", service.normalizeMerchant("NETFLIX.COM *1234 CA"));
        assertEquals("Spotify", service.normalizeMerchant("SPOTIFY USA 877-778-1161"));
        assertEquals("Amazon Prime", service.normalizeMerchant("AMZN PRIME MEMBERSHIP"));
        assertEquals("Planet Fitness", service.normalizeMerchant("PLANET FITNESS CLUB #1234"));
        assertEquals("ChatGPT Plus", service.normalizeMerchant("CHATGPT SUBSCRIPTION OPENAI"));
    }

    @Test
    void detectsMonthlyRecurringSubscription() {
        LocalDate now = LocalDate.now();
        Transaction t1 = new Transaction(testUser, "NETFLIX.COM *1234", new BigDecimal("15.99"),
                now.minusDays(60), TransactionType.EXPENSE, "Entertainment", "Netflix", "Card", false);
        Transaction t2 = new Transaction(testUser, "NETFLIX.COM *1234", new BigDecimal("15.99"),
                now.minusDays(30), TransactionType.EXPENSE, "Entertainment", "Netflix", "Card", false);

        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(t1, t2));
        when(repo.existsByUserIdAndNameIgnoreCase(1L, "Netflix")).thenReturn(false);

        List<DetectedSubscriptionDto> detected = service.detectSubscriptions(1L);

        assertEquals(1, detected.size());
        DetectedSubscriptionDto sub = detected.get(0);
        assertEquals("Netflix", sub.getName());
        assertEquals(new BigDecimal("15.99"), sub.getAmount());
        assertEquals(BillingFrequency.MONTHLY, sub.getBillingFrequency());
        assertFalse(sub.isAlreadyTracked());
        assertEquals(2, sub.getOccurrenceCount());
    }

    @Test
    void rejectsTransactionsWithHighAmountVariance() {
        LocalDate now = LocalDate.now();
        // $50 and $100 -> 100% variance, should NOT be detected as a fixed subscription
        Transaction t1 = new Transaction(testUser, "TARGET STORE", new BigDecimal("50.00"),
                now.minusDays(60), TransactionType.EXPENSE, "Shopping", "Target", "Card", false);
        Transaction t2 = new Transaction(testUser, "TARGET STORE", new BigDecimal("100.00"),
                now.minusDays(30), TransactionType.EXPENSE, "Shopping", "Target", "Card", false);

        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(t1, t2));

        List<DetectedSubscriptionDto> detected = service.detectSubscriptions(1L);
        assertTrue(detected.isEmpty());
    }

    @Test
    void createsSubscriptionSuccessfully() {
        when(users.findById(1L)).thenReturn(Optional.of(testUser));
        Subscription saved = new Subscription(testUser, "Gym", new BigDecimal("45.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), "Fitness", true);
        saved.setId(10L);

        when(repo.save(any(Subscription.class))).thenReturn(saved);

        SubscriptionRequest req = new SubscriptionRequest();
        req.setName("Gym");
        req.setAmount(new BigDecimal("45.00"));
        req.setBillingFrequency(BillingFrequency.MONTHLY);
        req.setNextPaymentDate(LocalDate.now().plusDays(10));
        req.setCategory("Fitness");

        SubscriptionDto res = service.create(1L, req);
        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Gym", res.getName());
        assertTrue(res.isActive());
    }

    @Test
    void togglesSubscriptionActiveStatus() {
        Subscription existing = new Subscription(testUser, "Gym", new BigDecimal("45.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), "Fitness", true);
        existing.setId(10L);

        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        SubscriptionDto updated = service.setActive(1L, 10L, false);
        assertNotNull(updated);
        assertFalse(updated.isActive());
    }
}
