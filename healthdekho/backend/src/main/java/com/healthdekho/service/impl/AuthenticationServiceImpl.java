package com.healthdekho.service.impl;

import com.healthdekho.dto.LoginRequest;
import com.healthdekho.dto.LoginResponse;
import com.healthdekho.dto.RegisterRequest;
import com.healthdekho.dto.RegisterResponse;
import com.healthdekho.entity.Customer;
import com.healthdekho.exception.AuthenticationFailedException;
import com.healthdekho.exception.CustomerRegistrationConflictException;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.security.CustomUserDetailsService;
import com.healthdekho.security.CustomerPrincipal;
import com.healthdekho.security.JwtService;
import com.healthdekho.service.AuthenticationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            CustomerRepository customerRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = CustomUserDetailsService.normalizeEmail(request.email());
        CustomerPrincipal principal;
        try {
            principal = (CustomerPrincipal) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())).getPrincipal();
        } catch (AuthenticationException | IllegalArgumentException exception) {
            throw new AuthenticationFailedException();
        }

        Customer customer = customerRepository.findById(principal.customerId())
                .filter(Customer::isActive)
                .orElseThrow(AuthenticationFailedException::new);
        String accessToken = jwtService.issueAccessToken(customer);
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenLifetimeSeconds());
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = CustomUserDetailsService.normalizeEmail(request.email());
        String normalizedMobile = normalizeMobileNumber(request.mobileNumber());
        if (customerRepository.existsByNormalizedEmail(normalizedEmail)
                || customerRepository.existsByMobileNumber(normalizedMobile)) {
            throw new CustomerRegistrationConflictException();
        }

        Customer customer = new Customer();
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setEmail(normalizedEmail);
        customer.setNormalizedEmail(normalizedEmail);
        customer.setMobileNumber(normalizedMobile);
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setActive(true);

        try {
            Customer saved = customerRepository.saveAndFlush(customer);
            return new RegisterResponse(saved.getId(), "Customer registered successfully");
        } catch (DataIntegrityViolationException exception) {
            throw new CustomerRegistrationConflictException();
        }
    }

    public static String normalizeMobileNumber(String mobileNumber) {
        return mobileNumber.trim().replaceAll("[\\s-]+", "");
    }
}
