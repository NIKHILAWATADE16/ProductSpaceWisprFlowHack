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
import java.util.Optional;
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

    private final CategorizationRuleRepository ruleRepository;
    private final UserRepository userRepository;

    public CategorizationService(CategorizationRuleRepository ruleRepository, UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Determines the category for a transaction using deterministic rule matching.
     * User-created overrides are checked first, followed by system generic rules.
     * If no rule matches, returns "Other".
     */
    @Transactional(readOnly = true)
    public String categorize(Long userId, String description, String merchant) {
        String combined = ((description != null ? description : "") + " " + (merchant != null ? merchant : "")).toLowerCase();

        List<CategorizationRule> rules = ruleRepository.findAllRulesForUser(userId);

        for (CategorizationRule rule : rules) {
            String keyword = rule.getKeyword();
            if (keyword != null && !keyword.isBlank() && combined.contains(keyword.toLowerCase())) {
                return rule.getCategory();
            }
        }

        return "Other";
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

