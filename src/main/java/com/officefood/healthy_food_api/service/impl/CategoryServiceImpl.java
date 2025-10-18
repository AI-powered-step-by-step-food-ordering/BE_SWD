package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.repository.CategoryRepository;
import com.officefood.healthy_food_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl extends CrudServiceImpl<Category> implements CategoryService {
    private final CategoryRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Category, java.util.UUID> repo() {
        return repository;
    }
}