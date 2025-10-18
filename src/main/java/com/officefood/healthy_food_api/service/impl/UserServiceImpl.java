package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.UserRepository;
import com.officefood.healthy_food_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl extends CrudServiceImpl<User> implements UserService {
    private final UserRepository repository;
@Override public Optional<User> findByEmail(String email) { return repository.findByEmail(email); }

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<User, java.util.UUID> repo() {
        return repository;
    }
}