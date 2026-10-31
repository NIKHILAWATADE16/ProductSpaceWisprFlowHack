package com.financecoach.service;

import com.financecoach.dto.GoalDto;
import com.financecoach.dto.GoalRequest;
import com.financecoach.entity.FinancialGoal;
import com.financecoach.entity.GoalStatus;
import com.financecoach.entity.User;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalService {

    private final FinancialGoalRepository repo;
    private final UserRepository users;

    public GoalService(FinancialGoalRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<GoalDto> list(Long uid) {
        return repo.findByUserId(uid, Sort.by("targetDate")).stream().map(this::dto).toList();
    }

    @Transactional
    public GoalDto create(Long uid, GoalRequest r) {
        User u = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GoalStatus status = r.getStatus() != null ? r.getStatus() : GoalStatus.IN_PROGRESS;
        if (r.getCurrentAmount() != null && r.getTargetAmount() != null &&
            r.getCurrentAmount().compareTo(r.getTargetAmount()) >= 0) {
            status = GoalStatus.COMPLETED;
        }

        FinancialGoal goal = new FinancialGoal(
            u,
            r.getName(),
            r.getTargetAmount(),
            r.getCurrentAmount(),
            r.getTargetDate(),
            r.getPriority(),
            status
        );
        return dto(repo.save(goal));
    }

    @Transactional
    public GoalDto update(Long uid, Long id, GoalRequest r) {
        FinancialGoal g = owned(uid, id);
        g.setName(r.getName());
        g.setTargetAmount(r.getTargetAmount());
        g.setCurrentAmount(r.getCurrentAmount());
        g.setTargetDate(r.getTargetDate());
        g.setPriority(r.getPriority());

        if (g.getCurrentAmount() != null && g.getTargetAmount() != null &&
            g.getCurrentAmount().compareTo(g.getTargetAmount()) >= 0) {
            g.setStatus(GoalStatus.COMPLETED);
        } else if (r.getStatus() != null) {
            g.setStatus(r.getStatus());
        }

        return dto(repo.save(g));
    }

    @Transactional
    public void delete(Long uid, Long id) {
        repo.delete(owned(uid, id));
    }

    private FinancialGoal owned(Long uid, Long id) {
        return repo.findById(id)
            .filter(g -> g.getUser().getId().equals(uid))
            .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }

    public GoalDto dto(FinancialGoal g) {
        BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
        BigDecimal current = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
        BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);

        boolean isCompleted = g.getStatus() == GoalStatus.COMPLETED || remaining.compareTo(BigDecimal.ZERO) == 0;
        LocalDate now = LocalDate.now();
        boolean isOverdue = !isCompleted && g.getTargetDate().isBefore(now);

        double progress = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            progress = current.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        BigDecimal monthlySavingsRequired;
        if (isCompleted) {
            monthlySavingsRequired = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (isOverdue) {
            // Target date is in the past: full remaining amount is needed immediately
            monthlySavingsRequired = remaining.setScale(2, RoundingMode.HALF_UP);
        } else {
            // Target date in future: calculate remaining months
            long days = ChronoUnit.DAYS.between(now, g.getTargetDate());
            long months = Math.max(1, (days + 29) / 30); // At least 1 month
            monthlySavingsRequired = remaining.divide(BigDecimal.valueOf(months), 2, RoundingMode.CEILING);
        }

        return new GoalDto(
            g.getId(),
            g.getName(),
            target.setScale(2, RoundingMode.HALF_UP),
            current.setScale(2, RoundingMode.HALF_UP),
            remaining.setScale(2, RoundingMode.HALF_UP),
            monthlySavingsRequired,
            g.getTargetDate(),
            g.getPriority(),
            g.getStatus(),
            progress,
            isOverdue,
            isCompleted
        );
    }
}
