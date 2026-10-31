package com.financecoach.service;

import com.financecoach.dto.SubscriptionDto;
import com.financecoach.dto.SubscriptionRequest;
import com.financecoach.entity.Subscription;
import com.financecoach.entity.User;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository repo;
    private final UserRepository users;

    public SubscriptionService(SubscriptionRepository repo, UserRepository users) {
        this.repo = repo;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> list(Long uid, boolean activeOnly) {
        List<Subscription> subscriptions = activeOnly
            ? repo.findByUserIdAndActive(uid, true, Sort.by("nextPaymentDate"))
            : repo.findByUserId(uid, Sort.by("nextPaymentDate"));
        return subscriptions.stream().map(this::dto).toList();
    }

    @Transactional
    public SubscriptionDto create(Long uid, SubscriptionRequest request) {
        User user = users.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Subscription subscription = new Subscription(user, request.getName(), request.getAmount(),
            request.getBillingFrequency(), request.getNextPaymentDate(), request.getCategory(), true);
        return dto(repo.save(subscription));
    }

    @Transactional
    public SubscriptionDto setActive(Long uid, Long id, boolean active) {
        Subscription subscription = repo.findById(id)
            .filter(item -> item.getUser().getId().equals(uid))
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        subscription.setActive(active);
        return dto(repo.save(subscription));
    }

    private SubscriptionDto dto(Subscription subscription) {
        return new SubscriptionDto(subscription.getId(), subscription.getName(), subscription.getAmount(),
            subscription.getBillingFrequency(), subscription.getNextPaymentDate(), subscription.getCategory(),
            subscription.isActive());
    }
}
