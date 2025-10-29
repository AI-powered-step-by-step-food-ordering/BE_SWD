package com.officefood.healthy_food_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name = "stores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Store extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=255)
    private String name;

    @Column(length=500)
    private String address;

    @Column(length=50)
    private String phone;

    @Column(name="image_url", length=500)
    private String imageUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private Set<Inventory> inventories = new HashSet<>();
}
