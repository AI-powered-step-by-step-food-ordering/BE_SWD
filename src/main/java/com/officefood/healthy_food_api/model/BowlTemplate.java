package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="bowls_template")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BowlTemplate {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=150)
    private String name;

    @Column(length=1000)
    private String description;

    @Column(name="is_active", nullable=false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "template")
    private Set<TemplateStep> steps = new HashSet<>();

    @OneToMany(mappedBy = "template")
    private Set<Bowl> bowls = new HashSet<>();
}
