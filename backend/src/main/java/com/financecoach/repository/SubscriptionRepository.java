package com.financecoach.repository;

import com.financecoach.entity.Subscription;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId, Sort sort);
    List<Subscription> findByUserIdAndActive(Long userId, boolean active, Sort sort);
    Optional<Subscription> findByUserIdAndNameIgnoreCase(Long userId, String name);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}

