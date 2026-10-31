package com.financecoach.service;

import com.financecoach.entity.CategorizationRule;
import com.financecoach.entity.User;
import com.financecoach.repository.CategorizationRuleRepository;
import com.financecoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategorizationServiceTest {

    @Mock
    private CategorizationRuleRepository ruleRepository;

    @Mock
    private UserRepository userRepository;

    private CategorizationService service;

    @BeforeEach
    void setUp() {
        service = new CategorizationService(ruleRepository, userRepository);
    }

    @Test
    void testGenericCategorization() {
        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of(
            new CategorizationRule(null, "uber", "Transport", false),
            new CategorizationRule(null, "netflix", "Entertainment", false),
            new CategorizationRule(null, "swiggy", "Food & Dining", false)
        ));

        assertEquals("Transport", service.categorize(1L, "Uber trip to airport", "Uber"));
        assertEquals("Entertainment", service.categorize(1L, "Monthly Netflix Premium", "Netflix"));
        assertEquals("Food & Dining", service.categorize(1L, "SWIGGY-BANGALORE", "Swiggy"));
        assertEquals("Other", service.categorize(1L, "Unknown corner store", "CornerStore"));
    }

    @Test
    void testImportedNarrationsUseBuiltInMerchantCatalog() {
        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of());

        assertEquals("Food & Dining", service.categorize(1L,
                "UPI/SWIGGY/ORDER/987654321", null));
        assertEquals("Transport", service.categorize(1L,
                "POS/UBER TRIP/TERM 123456", null));
        assertEquals("Shopping", service.categorize(1L,
                "AMAZON MARKETPLACE ORDER 12345", null));
        assertEquals("Entertainment", service.categorize(1L,
                "NETFLIX.COM *1234", null));
        assertEquals("Utilities", service.categorize(1L,
                "JIO RECHARGE REF 123456", null));
        assertEquals("Healthcare", service.categorize(1L,
                "APOLLO PHARMACY POS 123456", null));
    }

    @Test
    void testIncomeRowsAreClassifiedWithoutGenericCreditRule() {
        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of());

        assertEquals("Income", service.categorize(1L,
                "NEFT IN22627532707614 RANGAM INFOTECH", "RANGAM INFOTECH",
                com.financecoach.entity.TransactionType.INCOME));
        assertEquals("Income", service.categorize(1L,
                "NACH-APB-CR-CREDIT THROUGH PFMS", null,
                com.financecoach.entity.TransactionType.INCOME));
    }

    @Test
    void testExplicitMerchantIsUsedWhenDescriptionIsNoisy() {
        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of());

        assertEquals("Food & Dining", service.categorize(1L,
                "UPI/REF/987654321", "Zomato"));
    }

    @Test
    void testUnknownMerchantRemainsOther() {
        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of());

        assertEquals("Other", service.categorize(1L, "UPI/REF/987654321", "Local Merchant"));
    }

    @Test
    void testUserOverrideTakesPrecedence() {
        // User created an override for swiggy -> "Office Expense"
        CategorizationRule userOverride = new CategorizationRule(new User("Alex", "alex@test.com", "pass"), "swiggy", "Office Expense", true);
        CategorizationRule genericRule = new CategorizationRule(null, "swiggy", "Food & Dining", false);

        when(ruleRepository.findAllRulesForUser(1L)).thenReturn(List.of(userOverride, genericRule));

        String category = service.categorize(1L, "Order Swiggy lunch", "Swiggy");
        assertEquals("Office Expense", category);
    }

    @Test
    void testLearnUserCorrection() {
        User user = new User("Alex", "alex@test.com", "pass");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ruleRepository.findByUserIdAndKeyword(1L, "starbucks")).thenReturn(Optional.empty());
        when(ruleRepository.save(any(CategorizationRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategorizationRule saved = service.learnUserCorrection(1L, "Starbucks", "Meetings & Coffee");
        assertNotNull(saved);
        assertEquals("starbucks", saved.getKeyword());
        assertEquals("Meetings & Coffee", saved.getCategory());
        assertTrue(saved.isUserOverride());
    }
}

