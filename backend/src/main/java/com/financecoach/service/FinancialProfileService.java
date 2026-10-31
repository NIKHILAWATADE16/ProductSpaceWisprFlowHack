package com.financecoach.service;

import com.financecoach.dto.FinancialProfileDto;
import com.financecoach.dto.ProfileUpdateRequest;
import com.financecoach.entity.FinancialProfile;
import com.financecoach.entity.User;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.FinancialProfileRepository;
import com.financecoach.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class FinancialProfileService {

    private final FinancialProfileRepository profileRepository;
    private final UserRepository userRepository;

    public FinancialProfileService(FinancialProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FinancialProfileDto getProfileByUserId(Long userId) {
        FinancialProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                FinancialProfile newProfile = new FinancialProfile(
                    user, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                );
                return profileRepository.save(newProfile);
            });

        return toDto(profile);
    }

    @Transactional
    public FinancialProfileDto updateProfile(Long userId, ProfileUpdateRequest request) {
        FinancialProfile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                return new FinancialProfile(
                    user, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                );
            });

        profile.setMonthlyIncome(request.getMonthlyIncome());
        profile.setCurrentSavings(request.getCurrentSavings());
        profile.setEmergencyFund(request.getEmergencyFund());
        profile.setMonthlyFixedExpenses(request.getMonthlyFixedExpenses());
        profile.setSavingsTarget(request.getSavingsTarget());

        FinancialProfile saved = profileRepository.save(profile);
        return toDto(saved);
    }

    private FinancialProfileDto toDto(FinancialProfile entity) {
        return new FinancialProfileDto(
            entity.getId(),
            entity.getUser().getId(),
            entity.getMonthlyIncome(),
            entity.getCurrentSavings(),
            entity.getEmergencyFund(),
            entity.getMonthlyFixedExpenses(),
            entity.getSavingsTarget(),
            entity.getUpdatedAt()
        );
    }
}

