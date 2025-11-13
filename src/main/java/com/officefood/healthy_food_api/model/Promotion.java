package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "redemptions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    @EqualsAndHashCode.Include
    private String id;

    /** Mã khuyến mãi – chuẩn hoá UPPERCASE, duy nhất */
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    /** % giảm giá (0–100) */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "discountPercent must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "discountPercent must be <= 100")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    /** Thời điểm hiệu lực (UTC). Có thể null nghĩa là không giới hạn đầu/cuối. */
    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;


    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "promotion")
    private Set<PromotionRedemption> redemptions = new HashSet<>();
}
