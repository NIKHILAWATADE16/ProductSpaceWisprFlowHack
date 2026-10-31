package com.financecoach.service;

import com.financecoach.entity.*;
import com.financecoach.repository.*;
import com.financecoach.util.StatementImportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepo;
    private final FinancialProfileRepository profileRepo;
    private final FinancialGoalRepository goalRepo;
    private final BudgetRepository budgetRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final TransactionRepository txRepo;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepo,
            FinancialProfileRepository profileRepo,
            FinancialGoalRepository goalRepo,
            BudgetRepository budgetRepo,
            SubscriptionRepository subscriptionRepo,
            TransactionRepository txRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.budgetRepo = budgetRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.txRepo = txRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            String demoEmail = "demo@financecoach.com";
            Optional<User> existing = userRepo.findByEmail(demoEmail);
            if (existing.isEmpty()) {
                log.info("No demo user found. Seeding initial demo data...");
                seedDemoData(demoEmail, "Password123!", "Alex Morgan", false);
            } else {
                long txCount = txRepo.count();
                if (txCount == 0) {
                    log.info("Demo user exists with 0 transactions. Seeding demo transactions...");
                    seedDemoData(demoEmail, "Password123!", "Alex Morgan", true);
                }
            }
        } catch (Exception e) {
            log.warn("Could not auto-seed demo data on startup: {}", e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> seedDemoData(String email, String rawPassword, String fullName, boolean resetExisting) {
        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User(fullName, email, passwordEncoder.encode(rawPassword));
            user = userRepo.save(user);
        } else if (resetExisting) {
            // Delete existing records for fresh demo reset
            txRepo.deleteAll(txRepo.findByUserId(user.getId(), Sort.by("id")));
            goalRepo.deleteAll(goalRepo.findByUserId(user.getId(), Sort.by("id")));
            budgetRepo.deleteAll(budgetRepo.findByUserId(user.getId()));
            subscriptionRepo.deleteAll(subscriptionRepo.findByUserId(user.getId(), Sort.by("id")));
        }

        Long uid = user.getId();
        LocalDate now = LocalDate.now();
        int curMonth = now.getMonthValue();
        int curYear = now.getYear();

        // 1. Financial Profile
        FinancialProfile profile = profileRepo.findByUserId(uid).orElse(new FinancialProfile());
        profile.setUser(user);
        profile.setMonthlyIncome(new BigDecimal("6500.00"));
        profile.setMonthlyFixedExpenses(new BigDecimal("3200.00"));
        profile.setCurrentSavings(new BigDecimal("12500.00"));
        profile.setEmergencyFund(new BigDecimal("18000.00"));
        profile.setSavingsTarget(new BigDecimal("25.00"));
        profileRepo.save(profile);

        // 2. Financial Goals
        if (goalRepo.findByUserId(uid, Sort.by("id")).isEmpty()) {
            FinancialGoal g1 = new FinancialGoal(user, "Emergency Safety Net", new BigDecimal("18000.00"),
                    new BigDecimal("12500.00"), now.plusMonths(8), GoalPriority.HIGH, GoalStatus.IN_PROGRESS);
            FinancialGoal g2 = new FinancialGoal(user, "Japan Travel Trip", new BigDecimal("4500.00"),
                    new BigDecimal("2100.00"), now.plusMonths(5), GoalPriority.MEDIUM, GoalStatus.IN_PROGRESS);
            FinancialGoal g3 = new FinancialGoal(user, "Car Down Payment", new BigDecimal("8000.00"),
                    new BigDecimal("3000.00"), now.plusMonths(10), GoalPriority.LOW, GoalStatus.IN_PROGRESS);
            goalRepo.saveAll(List.of(g1, g2, g3));
        }

        // 3. Budgets
        if (budgetRepo.findByUserId(uid).isEmpty()) {
            Budget b1 = new Budget(user, "Dining Out", new BigDecimal("450.00"), curMonth, curYear);
            Budget b2 = new Budget(user, "Groceries", new BigDecimal("600.00"), curMonth, curYear);
            Budget b3 = new Budget(user, "Shopping", new BigDecimal("300.00"), curMonth, curYear);
            Budget b4 = new Budget(user, "Utilities", new BigDecimal("250.00"), curMonth, curYear);
            budgetRepo.saveAll(List.of(b1, b2, b3, b4));
        }

        // 4. Subscriptions
        if (subscriptionRepo.findByUserId(uid, Sort.by("id")).isEmpty()) {
            Subscription s1 = new Subscription(user, "Netflix", new BigDecimal("15.49"), BillingFrequency.MONTHLY,
                    now.plusDays(12), "Entertainment", true);
            Subscription s2 = new Subscription(user, "Spotify", new BigDecimal("10.99"), BillingFrequency.MONTHLY,
                    now.plusDays(18), "Entertainment", true);
            Subscription s3 = new Subscription(user, "Gym Membership", new BigDecimal("45.00"), BillingFrequency.MONTHLY,
                    now.plusDays(5), "Health", true);
            Subscription s4 = new Subscription(user, "AWS Cloud", new BigDecimal("28.50"), BillingFrequency.MONTHLY,
                    now.plusDays(22), "Utilities", true);
            subscriptionRepo.saveAll(List.of(s1, s2, s3, s4));
        }

        // 5. 3 Months of Transactions (Current month, month -1, month -2)
        List<Transaction> demoTxs = new ArrayList<>();

        for (int monthOffset = 2; monthOffset >= 0; monthOffset--) {
            LocalDate monthBase = now.minusMonths(monthOffset);
            LocalDate firstDay = monthBase.with(TemporalAdjusters.firstDayOfMonth());

            // Monthly Salary (1st)
            demoTxs.add(createTx(user, firstDay, new BigDecimal("6500.00"), TransactionType.INCOME,
                    "Salary", "Tech Corp Salary Direct Deposit", "Tech Corp", "Direct Deposit"));

            // Rent (2nd)
            demoTxs.add(createTx(user, firstDay.plusDays(1), new BigDecimal("1800.00"), TransactionType.EXPENSE,
                    "Housing", "Maple Apartments Monthly Rent", "Maple Apartments", "ACH"));

            // Utilities (5th & 10th)
            demoTxs.add(createTx(user, firstDay.plusDays(4), new BigDecimal("124.50"), TransactionType.EXPENSE,
                    "Utilities", "Pacific Gas & Electric Utility Bill", "PG&E", "AutoPay"));
            demoTxs.add(createTx(user, firstDay.plusDays(9), new BigDecimal("79.99"), TransactionType.EXPENSE,
                    "Utilities", "Comcast Xfinity High-Speed Internet", "Comcast", "Card"));

            // Groceries across month
            demoTxs.add(createTx(user, firstDay.plusDays(3), new BigDecimal("175.40"), TransactionType.EXPENSE,
                    "Groceries", "Whole Foods Market Grocery", "Whole Foods", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(11), new BigDecimal("138.60"), TransactionType.EXPENSE,
                    "Groceries", "Trader Joe's Supermarket", "Trader Joe's", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(18), new BigDecimal("152.10"), TransactionType.EXPENSE,
                    "Groceries", "Safeway Store #1842", "Safeway", "Card"));

            // Dining Out
            demoTxs.add(createTx(user, firstDay.plusDays(6), new BigDecimal("24.50"), TransactionType.EXPENSE,
                    "Dining Out", "Chipotle Mexican Grill", "Chipotle", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(8), new BigDecimal("16.75"), TransactionType.EXPENSE,
                    "Dining Out", "Starbucks Coffee #421", "Starbucks", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(14), new BigDecimal("115.00"), TransactionType.EXPENSE,
                    "Dining Out", "Italian Osteria Dinner", "Italian Osteria", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(21), new BigDecimal("34.80"), TransactionType.EXPENSE,
                    "Dining Out", "Sweetgreen Salads Downtown", "Sweetgreen", "Card"));

            // Subscriptions (Recurring)
            demoTxs.add(createTx(user, firstDay.plusDays(7), new BigDecimal("15.49"), TransactionType.EXPENSE,
                    "Entertainment", "NETFLIX.COM *1234 CA", "Netflix", "AutoPay", true));
            demoTxs.add(createTx(user, firstDay.plusDays(13), new BigDecimal("10.99"), TransactionType.EXPENSE,
                    "Entertainment", "SPOTIFY USA 877-778-1161 NY", "Spotify", "AutoPay", true));
            demoTxs.add(createTx(user, firstDay.plusDays(17), new BigDecimal("45.00"), TransactionType.EXPENSE,
                    "Health", "PLANET FITNESS CLUB #4912", "Planet Fitness", "AutoPay", true));
            demoTxs.add(createTx(user, firstDay.plusDays(23), new BigDecimal("28.50"), TransactionType.EXPENSE,
                    "Utilities", "AMAZON WEB SERVICES AWS.AMAZON", "AWS", "Card", true));

            // Shopping
            demoTxs.add(createTx(user, firstDay.plusDays(15), new BigDecimal("89.90"), TransactionType.EXPENSE,
                    "Shopping", "Amazon.com*Order #112-9843", "Amazon", "Card"));
            demoTxs.add(createTx(user, firstDay.plusDays(24), new BigDecimal("64.20"), TransactionType.EXPENSE,
                    "Shopping", "Target Store #2819", "Target", "Card"));
        }

        // Save transactions with duplicate check
        for (Transaction t : demoTxs) {
            if (!txRepo.existsByUserIdAndImportHash(uid, t.getImportHash())) {
                txRepo.save(t);
            }
        }

        log.info("Demo data successfully populated for user: {}", email);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "SUCCESS");
        result.put("message", "Demo financial profile, goals, budgets, subscriptions, and 3-month transactions populated.");
        result.put("email", email);
        result.put("password", rawPassword);
        result.put("monthlyIncome", "6500.00");
        result.put("goalsCount", 3);
        result.put("budgetsCount", 4);
        result.put("subscriptionsCount", 4);
        result.put("transactionsCount", demoTxs.size());

        return result;
    }

    private Transaction createTx(User user, LocalDate date, BigDecimal amount, TransactionType type,
                                 String category, String description, String merchant, String method) {
        return createTx(user, date, amount, type, category, description, merchant, method, false);
    }

    private Transaction createTx(User user, LocalDate date, BigDecimal amount, TransactionType type,
                                 String category, String description, String merchant, String method, boolean recurring) {
        Transaction t = new Transaction(user, description, amount, date, type, category, merchant, method, recurring);
        String hash = StatementImportHelper.computeImportHash(user.getId(), date, amount, description);
        t.setImportHash(hash);
        return t;
    }
}
