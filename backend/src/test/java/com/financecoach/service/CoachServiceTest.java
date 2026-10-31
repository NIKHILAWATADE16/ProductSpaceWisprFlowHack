package com.financecoach.service;

import com.financecoach.ai.DeterministicCoachFallback;
import com.financecoach.ai.FinancialContextBuilder;
import com.financecoach.ai.FinancialContextSummary;
import com.financecoach.ai.GeminiClient;
import com.financecoach.dto.CoachResponse;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.FinancialProfileRepository;
import com.financecoach.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoachServiceTest {

    @Mock
    private TransactionRepository tx;

    @Mock
    private FinancialGoalRepository goalRepo;

    @Mock
    private FinancialProfileRepository profileRepo;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private FinancialContextBuilder contextBuilder;

    private DeterministicCoachFallback fallback;
    private CoachService coachService;
    private FinancialContextSummary dummySummary;

    @BeforeEach
    void setUp() {
        fallback = new DeterministicCoachFallback();
        coachService = new CoachService(tx, goalRepo, profileRepo, geminiClient, contextBuilder, fallback);

        dummySummary = new FinancialContextSummary();
        dummySummary.setMonthlyIncome(new BigDecimal("5000.00"));
        dummySummary.setMonthlyExpenses(new BigDecimal("3200.00"));
        dummySummary.setNetSavings(new BigDecimal("1800.00"));
        dummySummary.setSavingsRate(new BigDecimal("36.00"));
        dummySummary.setPromptContextText("=== CONTEXT ===");
    }

    @Test
    void systemInstructionUsesIndianRupees() {
        when(contextBuilder.buildContext(1L)).thenReturn(dummySummary);
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    assertTrue(invocation.getArgument(0, String.class).contains("₹800"));
                    assertFalse(invocation.getArgument(0, String.class).contains("$800"));
                    return Optional.of("₹800");
                });

        CoachResponse response = coachService.chat(1L, "Where is my spending highest?");

        assertEquals("₹800", response.getResponse());
    }

    @Test
    void chatReturnsGeminiWhenAvailable() {
        when(contextBuilder.buildContext(1L)).thenReturn(dummySummary);
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenReturn(Optional.of("Grounded AI advice from Gemini"));

        CoachResponse res = coachService.chat(1L, "How can I improve my savings?");
        assertNotNull(res);
        assertEquals("gemini", res.getSource());
        assertEquals("Grounded AI advice from Gemini", res.getResponse());
    }

    @Test
    void chatFallsBackToDeterministicWhenGeminiFails() {
        when(contextBuilder.buildContext(1L)).thenReturn(dummySummary);
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenReturn(Optional.empty());

        CoachResponse res = coachService.chat(1L, "Where is my spending highest?");
        assertNotNull(res);
        assertEquals("deterministic", res.getSource());
        assertTrue(res.getResponse().contains("monthly expenses total"));
    }

    @Test
    void chatReturnsHelpfulPromptOnBlankQuestion() {
        when(contextBuilder.buildContext(1L)).thenReturn(dummySummary);

        CoachResponse res = coachService.chat(1L, "   ");
        assertNotNull(res);
        assertEquals("deterministic", res.getSource());
        assertTrue(res.getResponse().contains("I am your AI Financial Coach"));
    }
}

