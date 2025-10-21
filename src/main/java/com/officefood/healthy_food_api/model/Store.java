package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name = "stores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Store {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=255)
    private String name;

    @Column(length=500)
    private String address;

    @Column(length=50)
    private String phone;

    @OneToMany(mappedBy = "store")
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "store")
    private Set<Inventory> inventories = new HashSet<>();
}
