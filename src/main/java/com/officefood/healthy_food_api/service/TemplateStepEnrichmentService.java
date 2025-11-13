package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service để enrich TemplateStepResponse với thông tin ingredient đầy đủ
 */
@Service
@RequiredArgsConstructor
public class TemplateStepEnrichmentService {
    private final IngredientRepository ingredientRepository;

    /**
     * Enrich defaultIngredients trong response với thông tin ingredient từ database
     */
    @Transactional(readOnly = true)
    public void enrichDefaultIngredients(TemplateStepResponse response, TemplateStep entity) {
        if (entity.getDefaultIngredients() == null || entity.getDefaultIngredients().isEmpty()) {
            response.setDefaultIngredients(new ArrayList<>());
            return;
        }

        // Lấy tất cả ingredientIds
        List<String> ingredientIds = entity.getDefaultIngredients().stream()
                .map(TemplateStep.DefaultIngredientItem::getIngredientId)
                .distinct()
                .collect(Collectors.toList());

        // Load tất cả ingredients trong 1 query
        Map<String, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, ing -> ing));

        // Map sang DTO với thông tin đầy đủ
        List<TemplateStepResponse.DefaultIngredientItemDto> enrichedItems = entity.getDefaultIngredients().stream()
                .map(item -> {
                    Ingredient ingredient = ingredientMap.get(item.getIngredientId());
                    if (ingredient == null) return null; // Skip nếu ingredient không tồn tại

                    TemplateStepResponse.DefaultIngredientItemDto dto = new TemplateStepResponse.DefaultIngredientItemDto();
                    dto.setIngredientId(item.getIngredientId());
                    dto.setQuantity(item.getQuantity());
                    dto.setIsDefault(item.getIsDefault());
                    dto.setIngredientName(ingredient.getName());
                    dto.setUnitPrice(ingredient.getUnitPrice());
                    dto.setUnit(ingredient.getUnit());
                    dto.setStandardQuantity(ingredient.getStandardQuantity());
                    return dto;
                })
                .filter(dto -> dto != null) // Loại bỏ null
                .collect(Collectors.toList());

        response.setDefaultIngredients(enrichedItems);
    }

    /**
     * Enrich multiple responses
     */
    @Transactional(readOnly = true)
    public void enrichDefaultIngredients(List<TemplateStepResponse> responses, List<TemplateStep> entities) {
        if (responses == null || entities == null || responses.size() != entities.size()) {
            return;
        }

        for (int i = 0; i < responses.size(); i++) {
            enrichDefaultIngredients(responses.get(i), entities.get(i));
        }
    }

    /**
     * Tính tổng giá tiền của tất cả default ingredients trong bowl template
     * @param steps Danh sách các steps đã được enriched
     * @return Tổng giá tiền
     */
    public Double calculateDefaultPrice(List<TemplateStepResponse> steps) {
        if (steps == null || steps.isEmpty()) {
            return 0.0;
        }

        return steps.stream()
                .filter(step -> step.getDefaultIngredients() != null)
                .flatMap(step -> step.getDefaultIngredients().stream())
                .mapToDouble(item -> {
                    if (item.getUnitPrice() == null || item.getQuantity() == null) {
                        return 0.0;
                    }
                    // Tính giá theo tỷ lệ: (quantity / standardQuantity) * unitPrice
                    double standardQty = item.getStandardQuantity() != null ? item.getStandardQuantity() : 100.0;
                    return (item.getQuantity() / standardQty) * item.getUnitPrice();
                })
                .sum();
    }
}

