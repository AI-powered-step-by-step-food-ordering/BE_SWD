package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.PromotionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "redemptions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Promotion {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    /** Mã khuyến mãi – chuẩn hoá UPPERCASE, duy nhất */
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionType type;

    /** % giảm (0–100). Dùng cho type = PERCENT_OFF */
    @DecimalMin(value = "0.0", inclusive = true, message = "percentOff must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "percentOff must be <= 100")
    @Digits(integer = 5, fraction = 2)
    @Column(precision = 7, scale = 2) // ví dụ: 100.00
    private BigDecimal percentOff;

    /** Số tiền giảm cố định. Dùng cho type = AMOUNT_OFF/... */
    @DecimalMin(value = "0.00", inclusive = true, message = "amountOff must be >= 0")
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal amountOff;

    /** Ngưỡng tối thiểu đơn hàng để áp khuyến mãi */
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    /** Thời điểm hiệu lực (UTC). Có thể null nghĩa là không giới hạn đầu/cuối. */
    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Min(0)
    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    @Min(0)
    @Column(name = "per_order_limit")
    private Integer perOrderLimit;

    /** Đổi từ isActive(Boolean) -> active(boolean) để derived query nhận diện. Default: true */
    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "promotion")
    @Builder.Default
    private Set<PromotionRedemption> redemptions = new HashSet<>();
}
