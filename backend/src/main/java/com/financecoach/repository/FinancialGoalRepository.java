package com.financecoach.repository;

import com.financecoach.entity.FinancialGoal;
import com.financecoach.entity.GoalStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserId(Long userId, Sort sort);
    List<FinancialGoal> findByUserIdAndStatus(Long userId, GoalStatus status, Sort sort);
}

