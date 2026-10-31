package com.financecoach.ai;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@Component
public class DeterministicCoachFallback {

    private static String money(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        formatter.setCurrency(Currency.getInstance("INR"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value != null ? value : BigDecimal.ZERO);
    }

    public String generateFallback(FinancialContextSummary context, String question) {
        String q = question != null ? question.toLowerCase() : "";

        if (q.contains("spend") || q.contains("highest") || q.contains("expense") || q.contains("cost")) {
            return generateSpendingAdvice(context);
        }

        if (q.contains("save") || q.contains("savings") || q.contains("rate") || q.contains("improve")) {
            return generateSavingsAdvice(context);
        }

        if (q.contains("goal") || q.contains("target") || q.contains("focus") || q.contains("plan")) {
            return generateGoalsAdvice(context);
        }

        if (q.contains("sub") || q.contains("recurring") || q.contains("netflix") || q.contains("membership")) {
            return generateSubscriptionAdvice(context);
        }

        return generateGeneralSnapshot(context);
    }

    private String generateSpendingAdvice(FinancialContextSummary ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Based on your recorded numbers, your monthly expenses total **%s** against an income of **%s**.\n\n",
                money(ctx.getMonthlyExpenses()), money(ctx.getMonthlyIncome())));

        List<FinancialContextSummary.CategoryDetail> cats = ctx.getTopCategories();
        if (!cats.isEmpty()) {
            FinancialContextSummary.CategoryDetail top = cats.get(0);
            sb.append(String.format("Your largest spending category is **%s** at **%s** (accounting for %.1f%% of your total expenses).\n\n",
                    top.category(), money(top.amount()), top.percentOfExpenses()));

            if (cats.size() > 1) {
                sb.append("Other notable categories:\n");
                for (int i = 1; i < Math.min(cats.size(), 4); i++) {
                    FinancialContextSummary.CategoryDetail c = cats.get(i);
                    sb.append(String.format("- **%s**: %s (%.1f%%)\n", c.category(), money(c.amount()), c.percentOfExpenses()));
                }
                sb.append("\n");
            }
            sb.append(String.format("**Practical Step:** Review discretionary transactions inside **%s**. Even a 10%% reduction in this category would free up approximately **%s/month** for your savings goals.",
                    top.category(), money(top.amount().multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP))));
        } else {
            sb.append("You currently have no recorded categorized expenses for this month. Upload a bank statement or record transactions to see your detailed spending breakdown.");
        }
        return sb.toString();
    }

    private String generateSavingsAdvice(FinancialContextSummary ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Your current calculated savings rate is **%s%%** (monthly net savings of **%s** on **%s** income).\n\n",
                ctx.getSavingsRate(), money(ctx.getNetSavings()), money(ctx.getMonthlyIncome())));

        BigDecimal targetRate = ctx.getTargetSavingsRate();
        BigDecimal currentRate = ctx.getSavingsRate();

        if (currentRate.compareTo(targetRate) >= 0) {
            sb.append(String.format("Great work! You are currently meeting or exceeding your target savings rate of **%s%%** (benchmark is typically 20%%).\n\n", targetRate));
            sb.append("Consider channeling excess monthly surplus toward your highest priority goals or an emergency cushion.");
        } else {
            BigDecimal targetNet = ctx.getMonthlyIncome().multiply(targetRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal gap = targetNet.subtract(ctx.getNetSavings()).max(BigDecimal.ZERO);

            sb.append(String.format("To reach your target savings rate of **%s%%**, your goal is a net monthly surplus of **%s** (a gap of **%s/month** from where you are today).\n\n",
                    targetRate, money(targetNet), money(gap)));

            if (!ctx.getTopCategories().isEmpty()) {
                FinancialContextSummary.CategoryDetail top = ctx.getTopCategories().get(0);
                sb.append(String.format("**Opportunity:** Your highest expense area is **%s** (%s). Trimming this category or using the What-If Simulator can show how adjustments directly accelerate your goals.",
                        top.category(), money(top.amount())));
            }
        }
        return sb.toString();
    }

    private String generateGoalsAdvice(FinancialContextSummary ctx) {
        List<FinancialContextSummary.GoalDetail> goals = ctx.getActiveGoals();
        if (goals.isEmpty()) {
            return "You have no active goals registered yet. Head to the **Goals** section to define a target (like an Emergency Fund or Debt Payoff), and I'll calculate the exact monthly savings required to stay on track.";
        }

        FinancialContextSummary.GoalDetail nearest = goals.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You have **%d active goal(s)**. Your nearest priority is **%s**.\n\n", goals.size(), nearest.name()));
        sb.append(String.format("- **Target Amount:** %s\n", money(nearest.targetAmount())));
        sb.append(String.format("- **Currently Saved:** %s (Remaining: %s)\n", money(nearest.currentAmount()), money(nearest.remainingAmount())));
        sb.append(String.format("- **Target Date:** %s (%d month(s) remaining)\n", nearest.targetDate(), nearest.monthsRemaining()));
        sb.append(String.format("- **Required Monthly Allocation:** **%s/month**\n\n", money(nearest.requiredMonthlySavings())));

        BigDecimal surplus = ctx.getNetSavings();
        if (surplus.compareTo(nearest.requiredMonthlySavings()) >= 0) {
            sb.append(String.format("Your current monthly net surplus of **%s** is sufficient to cover this goal's required **%s/month** allocation.",
                    money(surplus), money(nearest.requiredMonthlySavings())));
        } else {
            BigDecimal shortfall = nearest.requiredMonthlySavings().subtract(surplus);
            sb.append(String.format("Your current monthly net surplus is **%s**, leaving a **%s/month shortfall** for this goal. Consider using the What-If simulator to test expense reductions.",
                    money(surplus), money(shortfall)));
        }
        return sb.toString();
    }

    private String generateSubscriptionAdvice(FinancialContextSummary ctx) {
        List<FinancialContextSummary.SubscriptionDetail> subs = ctx.getActiveSubscriptions();
        if (subs.isEmpty()) {
            return "You have no active recurring subscriptions recorded. The system can scan your transactions to detect recurring charges automatically under the Subscriptions tab.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You have **%d active recurring subscription(s)** totaling approximately **%s/month**:\n\n",
                subs.size(), money(ctx.getTotalMonthlySubscriptions())));

        for (FinancialContextSummary.SubscriptionDetail s : subs) {
            sb.append(String.format("- **%s**: %s (%s)\n", s.name(), money(s.amount()), s.frequency()));
        }

        sb.append("\n**Tip:** Recurring charges often accumulate quietly. Auditing and cancelling just 1 or 2 unused services can instantly boost your monthly savings rate without affecting your daily lifestyle.");
        return sb.toString();
    }

    private String generateGeneralSnapshot(FinancialContextSummary ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here is your Living Financial Model snapshot:\n\n");
        sb.append(String.format("- **Monthly Income:** %s\n", money(ctx.getMonthlyIncome())));
        sb.append(String.format("- **Monthly Expenses:** %s\n", money(ctx.getMonthlyExpenses())));
        sb.append(String.format("- **Monthly Net Savings:** %s (Savings Rate: **%s%%**)\n",
                money(ctx.getNetSavings()), ctx.getSavingsRate()));

        if (!ctx.getTopCategories().isEmpty()) {
            FinancialContextSummary.CategoryDetail top = ctx.getTopCategories().get(0);
            sb.append(String.format("- **Top Spending Area:** %s (%s)\n", top.category(), money(top.amount())));
        }

        if (!ctx.getActiveGoals().isEmpty()) {
            FinancialContextSummary.GoalDetail g = ctx.getActiveGoals().get(0);
            sb.append(String.format("- **Nearest Goal:** %s (%s / %s, Target: %s)\n",
                    g.name(), money(g.currentAmount()), money(g.targetAmount()), g.targetDate()));
        }

        sb.append("\nYou can ask me specific questions like: *\"Where is my spending highest?\"*, *\"How can I improve my savings rate?\"*, or *\"What should I focus on this month?\"*.");
        return sb.toString();
    }
}

