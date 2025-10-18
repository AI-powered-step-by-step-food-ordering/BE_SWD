package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.exception.BusinessException;
import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.repository.PromotionRedemptionRepository;
import com.officefood.healthy_food_api.repository.PromotionRepository;
import com.officefood.healthy_food_api.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl extends CrudServiceImpl<Promotion> implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final PromotionRepository repository;

    @Override public Optional<Promotion> findActiveByCode(String code) {
        return promotionRepository.findActiveByCode(code);
    }

    @Override public void validateAndApply(Promotion promo, Order order) {
        if (promo.getMaxRedemptions() != null) {
            int used = redemptionRepository.countByPromotionId(promo.getId());
            if (used >= promo.getMaxRedemptions()) throw new BusinessException("Promotion has reached max redemptions");
        }
        if (promo.getMinOrderValue() != null && order.getSubtotalAmount() != null && order.getSubtotalAmount() < promo.getMinOrderValue()) {
            throw new BusinessException("Order does not meet minimum value for this promotion");
        }
        PromotionRedemption red = new PromotionRedemption();
        red.setPromotion(promo); red.setOrder(order);
        redemptionRepository.save(red);
    }

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Promotion, java.util.UUID> repo() {
        return repository;
    }
}
