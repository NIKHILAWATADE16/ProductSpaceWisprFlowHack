package com.financecoach.repository;

import com.financecoach.entity.Subscription;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId, Sort sort);
    List<Subscription> findByUserIdAndActive(Long userId, boolean active, Sort sort);
}

