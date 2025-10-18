package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.model.*;
public final class MapperHelpers {
    private MapperHelpers() { }
    public static User user(java.util.UUID id) { User x = new User(); x.setId(id); return x; }
    public static Store store(java.util.UUID id) { Store x = new Store(); x.setId(id); return x; }
    public static Category category(java.util.UUID id) { Category x = new Category(); x.setId(id); return x; }
    public static Ingredient ingredient(java.util.UUID id) { Ingredient x = new Ingredient(); x.setId(id); return x; }
    public static BowlTemplate bowlTemplate(java.util.UUID id) { BowlTemplate x = new BowlTemplate(); x.setId(id); return x; }
    public static Bowl bowl(java.util.UUID id) { Bowl x = new Bowl(); x.setId(id); return x; }
    public static Order order(java.util.UUID id) { Order x = new Order(); x.setId(id); return x; }
}
