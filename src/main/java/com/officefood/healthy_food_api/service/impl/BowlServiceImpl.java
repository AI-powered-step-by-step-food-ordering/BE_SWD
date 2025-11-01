package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.service.BowlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BowlServiceImpl extends CrudServiceImpl<Bowl> implements BowlService {
    private final BowlRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Bowl, String> repo() {
        return repository;
    }

    @Override
    public void markReady(String bowlId) {
        repository.findById(bowlId).orElseThrow(); /* TODO */
    }

    @Override
    public List<Bowl> findAllWithTemplateAndSteps() {
        log.info("Finding all bowls with template and steps");
        List<Bowl> bowls = repository.findAllWithTemplateAndSteps();

        // Fetch template steps separately to avoid LazyInitializationException
        if (!bowls.isEmpty()) {
            List<String> templateIds = bowls.stream()
                    .map(b -> b.getTemplate().getId())
                    .distinct()
                    .toList();

            if (!templateIds.isEmpty()) {
                repository.fetchTemplateSteps(templateIds);
            }
        }

        return bowls;
    }

    @Override
    public Optional<Bowl> findByIdWithTemplateAndSteps(String id) {
        log.info("Finding bowl {} with template and steps", id);
        return repository.findByIdWithTemplateAndSteps(id);
    }

    @Override
    public Optional<Bowl> findByIdWithTemplateAndItems(String id) {
        log.info("Finding bowl {} with template and items", id);
        return repository.findByIdWithTemplateAndItems(id);
    }
}
