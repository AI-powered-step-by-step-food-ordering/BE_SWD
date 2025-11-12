package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.request.CategorySearchRequest;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.repository.CategoryRepository;
import com.officefood.healthy_food_api.service.CategoryService;
import com.officefood.healthy_food_api.specification.CategorySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl extends CrudServiceImpl<Category> implements CategoryService {
    private final CategoryRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Category, String> repo() {
        return repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> search(CategorySearchRequest searchRequest) {
        // Build specification from search request
        Specification<Category> spec = CategorySpecifications.withSearchCriteria(searchRequest);

        // Execute search
        List<Category> categories = repository.findAll(spec);

        return categories;
    }

}
