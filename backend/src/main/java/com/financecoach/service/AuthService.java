package com.financecoach.service;

import com.financecoach.dto.AuthResponse;
import com.financecoach.dto.LoginRequest;
import com.financecoach.dto.RegisterRequest;
import com.financecoach.dto.UserDto;
import com.financecoach.entity.FinancialProfile;
import com.financecoach.entity.User;
import com.financecoach.exception.ConflictException;
import com.financecoach.exception.ResourceNotFoundException;
import com.financecoach.repository.FinancialProfileRepository;
import com.financecoach.repository.UserRepository;
import com.financecoach.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FinancialProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       FinancialProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        User user = new User(
            request.getName().trim(),
            request.getEmail().toLowerCase().trim(),
            passwordEncoder.encode(request.getPassword())
        );
        user = userRepository.save(user);

        // Automatically initialize default living financial profile for new user
        FinancialProfile profile = new FinancialProfile(
            user,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        profileRepository.save(profile);

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}

