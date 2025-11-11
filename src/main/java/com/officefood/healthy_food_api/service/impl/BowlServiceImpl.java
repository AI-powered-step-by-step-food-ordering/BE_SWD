package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.repository.BowlTemplateRepository;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.service.BowlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BowlServiceImpl extends CrudServiceImpl<Bowl> implements BowlService {
    private final BowlRepository repository;
    private final BowlTemplateRepository templateRepository;
    private final OrderRepository orderRepository;
    private final IngredientRepository ingredientRepository;

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
        log.info("Finding all bowls with template, items and steps");

        // Fetch bowls with items first
        List<Bowl> bowls = repository.findAllWithItems();

        // Fetch template steps separately to avoid Cartesian product
        if (!bowls.isEmpty()) {
            List<String> templateIds = bowls.stream()
                    .filter(b -> b.getTemplate() != null)
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
    @Transactional // Ensure this runs in a transaction
    public Bowl createFromTemplate(String orderId, String templateId) {
        log.info("Creating bowl from template {} for order {}", templateId, orderId);

        // 1. Load order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // 2. Load template với steps
        BowlTemplate template = templateRepository.findByIdWithSteps(templateId)
                .orElseThrow(() -> new RuntimeException("Bowl template not found: " + templateId));

        // Force initialize steps collection to prevent LazyInitializationException
        org.hibernate.Hibernate.initialize(template.getSteps());

        // 3. Tạo Bowl mới
        Bowl bowl = new Bowl();
        bowl.setOrder(order);
        bowl.setTemplate(template);
        bowl.setName(template.getName());
        bowl.setIsActive(true);

        // 4. Thu thập tất cả ingredientIds từ các default ingredients
        Set<String> allIngredientIds = template.getSteps().stream()
                .filter(step -> step.getDefaultIngredients() != null)
                .flatMap(step -> step.getDefaultIngredients().stream())
                .filter(item -> item.getIsDefault() != null && item.getIsDefault()) // Chỉ lấy isDefault=true
                .map(TemplateStep.DefaultIngredientItem::getIngredientId)
                .collect(Collectors.toSet());

        // 5. Load tất cả ingredients trong 1 query
        Map<String, Ingredient> ingredientMap = ingredientRepository.findAllById(allIngredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, ing -> ing));

        // 6. Tạo BowlItems từ default ingredients với default quantities
        Set<BowlItem> bowlItems = new HashSet<>();
        double totalPrice = 0.0;

        for (TemplateStep step : template.getSteps()) {
            if (step.getDefaultIngredients() == null) continue;

            for (TemplateStep.DefaultIngredientItem defaultItem : step.getDefaultIngredients()) {
                // Chỉ tạo BowlItem cho ingredients có isDefault=true
                if (defaultItem.getIsDefault() == null || !defaultItem.getIsDefault()) {
                    continue;
                }

                Ingredient ingredient = ingredientMap.get(defaultItem.getIngredientId());
                if (ingredient == null) {
                    log.warn("Ingredient {} not found, skipping", defaultItem.getIngredientId());
                    continue;
                }

                // Sử dụng default quantity từ template
                Double quantity = defaultItem.getQuantity();

                // Tính giá: (quantity / standardQuantity) * unitPrice
                Double itemPrice = 0.0;
                if (ingredient.getStandardQuantity() != null && ingredient.getStandardQuantity() > 0) {
                    itemPrice = (quantity / ingredient.getStandardQuantity()) * ingredient.getUnitPrice();
                }

                BowlItem bowlItem = new BowlItem();
                bowlItem.setBowl(bowl);
                bowlItem.setIngredient(ingredient);
                bowlItem.setQuantity(quantity);
                bowlItem.setUnitPrice(itemPrice);
                bowlItem.setIsActive(true);

                bowlItems.add(bowlItem);
                totalPrice += itemPrice;
            }
        }

        bowl.setItems(bowlItems);
        bowl.setLinePrice(totalPrice);

        // 7. Save và return
        Bowl savedBowl = repository.save(bowl);
        log.info("Created bowl {} with {} items, total price: {}", savedBowl.getId(), bowlItems.size(), totalPrice);

        return savedBowl;
    }

    @Override
    public Optional<Bowl> findByIdWithTemplateAndItems(String id) {
        log.info("Finding bowl {} with template and items", id);
        Optional<Bowl> bowlOpt = repository.findByIdWithTemplateAndItems(id);

        // Fetch template steps separately to avoid Cartesian product
        if (bowlOpt.isPresent() && bowlOpt.get().getTemplate() != null) {
            Bowl bowl = bowlOpt.get();
            String templateId = bowl.getTemplate().getId();
            repository.fetchTemplateSteps(List.of(templateId));
        }

        return bowlOpt;
    }
}
