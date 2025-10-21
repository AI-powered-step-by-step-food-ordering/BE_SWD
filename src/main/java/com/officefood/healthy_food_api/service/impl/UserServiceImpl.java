package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.UserRepository;
import com.officefood.healthy_food_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl extends CrudServiceImpl<User> implements UserService {
    private final UserRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<User, UUID> repo() {
        return repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String authority = switch (u.getRole()) {
            case ADMIN -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authority)
                .accountLocked(false)
                .disabled(false)
                .build();
    }

    @Override public void changePassword(UUID userId, String rawPassword) { repository.findById(userId).orElseThrow(); /* TODO */ }
    @Override public void suspend(UUID userId) { repository.findById(userId).orElseThrow(); /* TODO */ }
    @Override public void activate(UUID userId) { repository.findById(userId).orElseThrow(); /* TODO */ }

}
