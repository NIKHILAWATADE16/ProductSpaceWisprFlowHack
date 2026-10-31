package com.financecoach.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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
    void detectsYearlyRecurringSubscriptionWithLongLookback() {
        LocalDate now = LocalDate.now();
        Transaction t1 = new Transaction(testUser, "ANNUAL CLOUD PLAN", new BigDecimal("120.00"),
                now.minusDays(365), TransactionType.EXPENSE, "Subscriptions", "Cloud Plan", "Card", false);
        Transaction t2 = new Transaction(testUser, "ANNUAL CLOUD PLAN", new BigDecimal("120.00"),
                now.minusDays(10), TransactionType.EXPENSE, "Subscriptions", "Cloud Plan", "Card", false);

        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(t1, t2));
        when(repo.existsByUserIdAndNameIgnoreCase(1L, "Cloud Plan")).thenReturn(false);

        List<DetectedSubscriptionDto> detected = service.detectSubscriptions(1L);

        assertEquals(1, detected.size());
        assertEquals(BillingFrequency.YEARLY, detected.get(0).getBillingFrequency());
    }

    @Test
    void matchesSubscriptionUsingTransactionMerchantField() {
        Subscription sub = new Subscription(testUser, "Cloud Plan", new BigDecimal("10.00"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), "Subscriptions", true);
        sub.setId(3L);
        Transaction transaction = new Transaction(testUser, "AUTOPAY REF 123", new BigDecimal("12.00"),
                LocalDate.now().minusDays(3), TransactionType.EXPENSE, "Subscriptions", "Cloud Plan", "Card", false);

        when(repo.findByUserIdAndActive(eq(1L), eq(true), any(Sort.class))).thenReturn(List.of(sub));
        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(transaction));

        SubscriptionDto result = service.list(1L, true).get(0);

        assertTrue(result.isPriceChangeAlert());
        assertEquals(new BigDecimal("12.00"), result.getNewAmount());
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

    @Test
    void detectsPriceChangeAlertWhenAmountChanges() {
        Subscription sub = new Subscription(testUser, "Netflix", new BigDecimal("10.99"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), "Entertainment", true);
        sub.setId(1L);

        Transaction recentTx = new Transaction(testUser, "Netflix Monthly", new BigDecimal("15.49"),
                LocalDate.now().minusDays(5), TransactionType.EXPENSE, "Entertainment", "Netflix", "Card", false);

        when(repo.findByUserIdAndActive(eq(1L), eq(true), any(Sort.class))).thenReturn(List.of(sub));
        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(recentTx));

        List<SubscriptionDto> list = service.list(1L, true);
        assertEquals(1, list.size());
        SubscriptionDto dto = list.get(0);
        assertTrue(dto.isPriceChangeAlert());
        assertEquals(new BigDecimal("10.99"), dto.getPreviousAmount());
        assertEquals(new BigDecimal("15.49"), dto.getNewAmount());
        assertNotNull(dto.getPriceChangeMessage());
        assertTrue(dto.getPriceChangeMessage().contains("Price increased"));
        assertFalse(dto.isUnusedAlert());
    }

    @Test
    void detectsUnusedAlertWhenNoChargeFor45PlusDays() {
        Subscription sub = new Subscription(testUser, "Spotify", new BigDecimal("9.99"),
                BillingFrequency.MONTHLY, LocalDate.now().plusDays(10), "Entertainment", true);
        sub.setId(2L);

        Transaction oldTx = new Transaction(testUser, "Spotify Premium", new BigDecimal("9.99"),
                LocalDate.now().minusDays(52), TransactionType.EXPENSE, "Entertainment", "Spotify", "Card", false);

        when(repo.findByUserIdAndActive(eq(1L), eq(true), any(Sort.class))).thenReturn(List.of(sub));
        when(txRepo.findByUserIdAndTransactionDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class), any(Sort.class)))
                .thenReturn(List.of(oldTx));

        List<SubscriptionDto> list = service.list(1L, true);
        assertEquals(1, list.size());
        SubscriptionDto dto = list.get(0);
        assertFalse(dto.isPriceChangeAlert());
        assertTrue(dto.isUnusedAlert());
        assertEquals(52, dto.getDaysInactive());
        assertNotNull(dto.getUnusedMessage());
        assertTrue(dto.getUnusedMessage().contains("No charge detected"));

        List<SubscriptionDto> alerts = service.getActiveAlerts(1L);
        assertEquals(1, alerts.size());
    }
}
