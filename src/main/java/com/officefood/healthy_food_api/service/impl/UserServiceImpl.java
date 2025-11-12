package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.request.UserSearchRequest;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.UserRepository;
import com.officefood.healthy_food_api.service.UserService;
import com.officefood.healthy_food_api.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl extends CrudServiceImpl<User> implements UserService {
    private final UserRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<User, String> repo() {
        return repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if account is active using the new method
        boolean isAccountEnabled = u.isAccountActive();

        String authority = switch (u.getRole()) {
            case ADMIN -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authority)
                .accountLocked(false)
                .disabled(!isAccountEnabled) // Disabled if account is not active
                .build();
    }

    @Override
    public void changePassword(String userId, String rawPassword) {
        repository.findById(userId).orElseThrow();
        /* TODO */
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<User> search(UserSearchRequest searchRequest) {
        log.info("Searching users with criteria: {}", searchRequest);

        // Build specification from search request
        Specification<User> spec = UserSpecifications.withSearchCriteria(searchRequest);

        // Execute search
        java.util.List<User> users = repository.findAll(spec);

        log.info("Found {} users matching search criteria", users.size());
        return users;
    }
}
