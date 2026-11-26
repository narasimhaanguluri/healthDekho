package com.healthdekho.security;

import com.healthdekho.entity.Customer;
import com.healthdekho.repository.CustomerRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = normalizeEmail(email);
        Customer customer = customerRepository.findByNormalizedEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        return toPrincipal(customer);
    }

    public CustomerPrincipal loadPrincipalById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        return toPrincipal(customer);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerPrincipal toPrincipal(Customer customer) {
        return new CustomerPrincipal(
                customer.getId(),
                customer.getNormalizedEmail(),
                customer.getPasswordHash(),
                customer.isActive());
    }
}
