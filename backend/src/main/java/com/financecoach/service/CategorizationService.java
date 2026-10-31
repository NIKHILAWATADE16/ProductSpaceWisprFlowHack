package com.financecoach.service;

import com.financecoach.dto.CategorizationRuleDto;
import com.financecoach.entity.CategorizationRule;
import com.financecoach.entity.User;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.CategorizationRuleRepository;
import com.financecoach.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CategorizationService {

    public static final List<String> STANDARD_CATEGORIES = Arrays.asList(
        "Housing",
        "Food & Dining",
        "Transport",
        "Shopping",
        "Entertainment",
        "Utilities",
        "Healthcare",
        "Salary",
        "Income",
        "Investments",
        "Subscriptions",
        "Personal Care",
        "Education",
        "Travel",
        "Other"
    );

    private static final List<CategoryKeyword> BUILT_IN_KEYWORDS = List.of(
        new CategoryKeyword("Food & Dining", "swiggy", "zomato", "zepto", "blinkit", "instamart", "dominos", "pizza hut", "mcdonald", "kfc", "starbucks", "restaurant", "cafe", "bakery", "food", "dining", "grocery", "groceries", "supermarket", "whole foods", "trader joe", "safeway"),
        new CategoryKeyword("Transport", "uber", "ola", "rapido", "lyft", "metro", "transit", "fuel", "petrol", "diesel", "shell", "bpcl", "hpcl", "indian oil", "parking", "toll", "fastag", "irctc", "airline", "flight", "cab", "taxi"),
        new CategoryKeyword("Shopping", "amazon", "flipkart", "myntra", "meesho", "ajio", "target", "walmart", "costco"),
        new CategoryKeyword("Entertainment", "netflix", "spotify", "prime video", "disney", "hotstar", "youtube", "cinema", "movie", "theatre", "hulu", "apple music", "playstation", "xbox"),
        new CategoryKeyword("Housing", "rent", "housing", "apartment", "maintenance", "society", "property", "mortgage", "home loan"),
        new CategoryKeyword("Utilities", "electricity", "water bill", "gas bill", "wifi", "broadband", "internet", "comcast", "jio", "airtel", "vodafone", "utility", "recharge", "mobile bill"),
        new CategoryKeyword("Healthcare", "pharmacy", "medicine", "medicines", "hospital", "doctor", "clinic", "apollo", "healthcare", "medical", "dental", "insurance"),
        new CategoryKeyword("Education", "school", "college", "tuition", "course", "udemy", "coursera", "education", "books"),
        new CategoryKeyword("Travel", "hotel", "airbnb", "booking.com", "makemytrip", "goibibo", "travel", "airways", "airport"),
        new CategoryKeyword("Personal Care", "salon", "spa", "barber", "grooming", "cosmetics"),
        new CategoryKeyword("Subscriptions", "subscription", "membership"),
        new CategoryKeyword("Salary", "salary", "payroll", "pay credit", "wages"),
        new CategoryKeyword("Income", "freelance", "income", "received", "deposit", "refund", "cashback"),
        new CategoryKeyword("Investments", "investment", "mutual fund", "sip", "dividend", "broker", "zerodha", "groww")
    );

    private static final Pattern NARRATION_NOISE = Pattern.compile(
        "(?i)\\b(?:upi|imps|neft|rtgs|nach|pos|atm|trf|transfer|txn|ref|utr)\\b|#|\\b\\d{4,}\\b"
    );

    private record CategoryKeyword(String category, List<String> keywords) {
        private CategoryKeyword(String category, String... keywords) {
            this(category, Arrays.stream(keywords)
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .toList());
        }
    }

    private final CategorizationRuleRepository ruleRepository;
    private final UserRepository userRepository;

    public CategorizationService(CategorizationRuleRepository ruleRepository, UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Determines the category for a transaction using deterministic rule matching.
     * User-created overrides are checked first, followed by system generic rules and
     * the built-in merchant catalog. If no rule matches, returns "Other".
     */
    @Transactional(readOnly = true)
    public String categorize(Long userId, String description, String merchant) {
        return categorize(userId, description, merchant, null);
    }

    public String categorize(Long userId, String description, String merchant, com.financecoach.entity.TransactionType type) {
        String rawText = ((description != null ? description : "") + " "
                + (merchant != null ? merchant : "")).toLowerCase(Locale.ROOT);
        String normalizedDescription = normalizeForMatching(description);
        String normalizedMerchant = normalizeForMatching(merchant);
        String normalizedText = normalizeForMatching(rawText);

        List<CategorizationRule> rules = ruleRepository.findAllRulesForUser(userId);

        // Explicit user corrections always win over generic rules and built-ins.
        for (CategorizationRule rule : rules) {
            if (rule.isUserOverride() && matchesRule(rule, rawText, normalizedText)) {
                return rule.getCategory();
            }
        }
        for (CategorizationRule rule : rules) {
            if (!rule.isUserOverride() && matchesRule(rule, rawText, normalizedText)) {
                return rule.getCategory();
            }
        }

        // Prefer the explicit merchant field when the description contains bank noise.
        String merchantCategory = matchBuiltInCategory(normalizedMerchant);
        if (merchantCategory != null) {
            return merchantCategory;
        }

        String builtInCategory = matchBuiltInCategory(normalizedDescription.isBlank()
                ? normalizedText : normalizedDescription + " " + normalizedMerchant);
        if (builtInCategory != null) {
            return builtInCategory;
        }

        // Incoming bank credits should not be mistaken for an expense category.
        if (type == com.financecoach.entity.TransactionType.INCOME) {
            return matchIncomeCategory(normalizedDescription + " " + normalizedMerchant);
        }
        return "Other";
    }

    private boolean matchesRule(CategorizationRule rule, String rawText, String normalizedText) {
        String keyword = rule.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT).trim();
        String normalizedKeyword = normalizeForMatching(lowerKeyword);
        return rawText.contains(lowerKeyword)
                || (!normalizedKeyword.isBlank() && normalizedText.contains(normalizedKeyword));
    }

    private String matchIncomeCategory(String normalizedText) {
        String paddedText = " " + (normalizedText == null ? "" : normalizedText) + " ";
        if (paddedText.contains(" salary ") || paddedText.contains(" payroll ")
                || paddedText.contains(" wages ") || paddedText.contains(" pay credit ")) {
            return "Salary";
        }
        if (paddedText.contains(" freelance ") || paddedText.contains(" dividend ")
                || paddedText.contains(" refund ") || paddedText.contains(" cashback ")
                || paddedText.contains(" deposit ") || paddedText.contains(" received ")) {
            return paddedText.contains(" dividend ") ? "Investments" : "Income";
        }
        return "Income";
    }

    private String matchBuiltInCategory(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return null;
        }
        String paddedText = " " + normalizedText + " ";
        for (CategoryKeyword group : BUILT_IN_KEYWORDS) {
            for (String keyword : group.keywords()) {
                String normalizedKeyword = normalizeForMatching(keyword);
                if (!normalizedKeyword.isBlank()
                        && paddedText.contains(" " + normalizedKeyword + " ")) {
                    return group.category();
                }
            }
        }
        return null;
    }

    private static String normalizeForMatching(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = NARRATION_NOISE.matcher(value.toLowerCase(Locale.ROOT)).replaceAll(" ");
        return normalized.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }

    /**
     * Learns a user's category correction as a rule with user override priority.
     */
    @Transactional
    public CategorizationRule learnUserCorrection(Long userId, String keyword, String category) {
        if (keyword == null || keyword.isBlank() || category == null || category.isBlank()) {
            return null;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        if (normalizedKeyword.length() > 100) {
            normalizedKeyword = normalizedKeyword.substring(0, 100).trim();
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Optional<CategorizationRule> existing = ruleRepository.findByUserIdAndKeyword(userId, normalizedKeyword);
        CategorizationRule rule;
        if (existing.isPresent()) {
            rule = existing.get();
            rule.setCategory(category);
        } else {
            rule = new CategorizationRule(user, normalizedKeyword, category, true);
        }

        return ruleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<CategorizationRuleDto> getUserRules(Long userId) {
        return ruleRepository.findAllRulesForUser(userId).stream()
            .map(CategorizationRuleDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRule(Long userId, Long ruleId) {
        CategorizationRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + ruleId));

        if (rule.getUser() != null && rule.getUser().getId().equals(userId)) {
            ruleRepository.delete(rule);
        } else {
            throw new IllegalArgumentException("Cannot delete default system rules or rules belonging to another user");
        }
    }

    public List<String> getAllCategories() {
        return STANDARD_CATEGORIES;
    }
}

