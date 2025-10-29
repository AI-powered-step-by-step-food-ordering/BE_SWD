package com.officefood.healthy_food_api.utils;

/**
 * Utility class for ingredient calculations
 */
public class IngredientCalculator {

    /**
     * Tính giá tiền dựa trên số lượng khách hàng chọn
     *
     * @param requestedQuantity Số lượng khách hàng muốn (VD: 150g)
     * @param standardQuantity Số lượng chuẩn của ingredient (VD: 100g)
     * @param unitPrice Giá tiền cho standardQuantity (VD: 10000 VNĐ cho 100g)
     * @return Giá tiền cần trả
     *
     * Example:
     * - Ingredient: Cơm gạo lứt
     * - standardQuantity = 100 (gram)
     * - unitPrice = 10000 (VNĐ cho 100g)
     * - Khách muốn 150g
     * - Giá = (150 / 100) * 10000 = 15000 VNĐ
     */
    public static Double calculatePrice(Double requestedQuantity, Double standardQuantity, Double unitPrice) {
        if (requestedQuantity == null || standardQuantity == null || unitPrice == null) {
            return 0.0;
        }
        if (standardQuantity == 0) {
            throw new IllegalArgumentException("Standard quantity cannot be zero");
        }
        return (requestedQuantity / standardQuantity) * unitPrice;
    }

    /**
     * Tính số lượng inventory cần trừ dựa trên số lượng khách hàng đặt
     *
     * @param requestedQuantity Số lượng khách hàng đặt
     * @param standardQuantity Số lượng chuẩn của ingredient
     * @return Số lượng cần trừ trong inventory (tính theo standardQuantity units)
     *
     * Example:
     * - Khách đặt 250g
     * - standardQuantity = 100g
     * - Inventory cần trừ = 250 / 100 = 2.5 units
     */
    public static Double calculateInventoryDeduction(Double requestedQuantity, Double standardQuantity) {
        if (requestedQuantity == null || standardQuantity == null) {
            return 0.0;
        }
        if (standardQuantity == 0) {
            throw new IllegalArgumentException("Standard quantity cannot be zero");
        }
        return requestedQuantity / standardQuantity;
    }

    /**
     * Tính tổng giá cho BowlItem
     * Sử dụng unitPrice từ BowlItem (snapshot) thay vì Ingredient.unitPrice hiện tại
     *
     * @param bowlItemQuantity Số lượng trong BowlItem (VD: 150g)
     * @param ingredientStandardQuantity Số lượng chuẩn của Ingredient (VD: 100g)
     * @param bowlItemUnitPrice Giá snapshot trong BowlItem (VD: 10000)
     * @return Tổng giá
     *
     * Example:
     * - BowlItem.quantity = 150g
     * - Ingredient.standardQuantity = 100g
     * - BowlItem.unitPrice = 10000 (snapshot)
     * - Total = (150 / 100) * 10000 = 15000 VNĐ
     */
    public static Double calculateBowlItemPrice(Double bowlItemQuantity,
                                                 Double ingredientStandardQuantity,
                                                 Double bowlItemUnitPrice) {
        return calculatePrice(bowlItemQuantity, ingredientStandardQuantity, bowlItemUnitPrice);
    }

    /**
     * Format hiển thị đơn vị với số lượng
     *
     * @param quantity Số lượng
     * @param unit Đơn vị (g, ml, piece, etc.)
     * @return Chuỗi hiển thị (VD: "100g", "50ml", "1 piece")
     */
    public static String formatQuantityUnit(Double quantity, String unit) {
        if (quantity == null || unit == null) {
            return "";
        }
        if (unit.equalsIgnoreCase("piece") || unit.equalsIgnoreCase("pieces")) {
            return String.format("%.0f %s", quantity, quantity > 1 ? "pieces" : "piece");
        }
        return String.format("%.0f%s", quantity, unit);
    }
}

