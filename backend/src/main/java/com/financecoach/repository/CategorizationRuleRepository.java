package com.financecoach.repository;

import com.financecoach.entity.CategorizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorizationRuleRepository extends JpaRepository<CategorizationRule, Long> {

    @Query("SELECT r FROM CategorizationRule r WHERE r.user.id = :userId OR r.user IS NULL ORDER BY r.isUserOverride DESC, r.id DESC")
    List<CategorizationRule> findAllRulesForUser(@Param("userId") Long userId);

    Optional<CategorizationRule> findByUserIdAndKeyword(Long userId, String keyword);
}

