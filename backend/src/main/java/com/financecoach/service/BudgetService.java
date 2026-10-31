package com.financecoach.service;

import com.financecoach.dto.BudgetDto;
import com.financecoach.dto.BudgetRequest;
import com.financecoach.entity.Budget;
import com.financecoach.entity.TransactionType;
import com.financecoach.entity.User;
import com.financecoach.exception.ConflictException;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.BudgetRepository;
import com.financecoach.repository.TransactionRepository;
import com.financecoach.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository repo;
    private final UserRepository users;
    private final TransactionRepository transactions;

    public BudgetService(BudgetRepository repo, UserRepository users, TransactionRepository transactions) {
        this.repo = repo;
        this.users = users;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> list(Long uid, Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int m = month == null ? now.getMonthValue() : month;
        int y = year == null ? now.getYear() : year;
        return repo.findByUserIdAndMonthAndYear(uid, m, y).stream().map(this::dto).toList();
    }

    @Transactional
    public BudgetDto create(Long uid, BudgetRequest request) {
        User user = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ensureUnique(uid, request, null);
        return dto(repo.save(new Budget(user, request.getCategory(), request.getMonthlyLimit(), request.getMonth(), request.getYear())));
    }

    @Transactional
    public BudgetDto update(Long uid, Long id, BudgetRequest request) {
        Budget budget = owned(uid, id);
        ensureUnique(uid, request, id);
        budget.setCategory(request.getCategory());
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return dto(repo.save(budget));
    }

    @Transactional
    public void delete(Long uid, Long id) {
        repo.delete(owned(uid, id));
    }

    private void ensureUnique(Long uid, BudgetRequest request, Long ignoredId) {
        repo.findByUserIdAndCategoryAndMonthAndYear(uid, request.getCategory(), request.getMonth(), request.getYear())
            .filter(existing -> ignoredId == null || !existing.getId().equals(ignoredId))
            .ifPresent(existing -> { throw new ConflictException("Budget already exists for this category and period"); });
    }

    private Budget owned(Long uid, Long id) {
        return repo.findById(id)
            .filter(budget -> budget.getUser().getId().equals(uid))
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private BudgetDto dto(Budget budget) {
        LocalDate start = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal spent = transactions.sumAmountByUserIdAndTypeAndCategoryAndDateBetween(
            budget.getUser().getId(), TransactionType.EXPENSE, budget.getCategory(), start, end
        );
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal limit = budget.getMonthlyLimit();
        BigDecimal utilization = BigDecimal.ZERO;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            utilization = spent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remaining = limit.subtract(spent);
        boolean exceeded = spent.compareTo(limit) > 0;
        BigDecimal overrunAmount = exceeded ? spent.subtract(limit) : BigDecimal.ZERO;

        return new BudgetDto(
            budget.getId(),
            budget.getCategory(),
            limit.setScale(2, RoundingMode.HALF_UP),
            budget.getMonth(),
            budget.getYear(),
            spent.setScale(2, RoundingMode.HALF_UP),
            utilization,
            remaining.setScale(2, RoundingMode.HALF_UP),
            exceeded,
            overrunAmount.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
