package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.BowlSearchRequest;
import com.officefood.healthy_food_api.model.Bowl;

import java.util.List;
import java.util.Optional;

public interface BowlService extends CrudService<Bowl> {
    void markReady(String bowlId);
    List<Bowl> findAllWithTemplateAndSteps();
    Optional<Bowl> findByIdWithTemplateAndSteps(String id);
    Optional<Bowl> findByIdWithTemplateAndItems(String id);

    /**
     * Tạo Bowl từ template với default ingredients
     * Sử dụng default quantities từ template (isDefault=true)
     * @param orderId ID của order
     * @param templateId ID của template
     * @return Bowl đã được tạo với các BowlItems theo default ingredients
     */
    Bowl createFromTemplate(String orderId, String templateId);

    // Search functionality
    List<Bowl> search(BowlSearchRequest searchRequest);
}
