package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String authority = switch (u.getRole()) {
            case ADMIN -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities(authority)
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}


