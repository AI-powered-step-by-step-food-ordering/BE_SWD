package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.model.*;
public final class MapperHelpers {
    private MapperHelpers() { }
    public static User user(String id) { User x = new User(); x.setId(id); return x; }
    public static Store store(String id) { Store x = new Store(); x.setId(id); return x; }
    public static Category category(String id) { Category x = new Category(); x.setId(id); return x; }
    public static Ingredient ingredient(String id) { Ingredient x = new Ingredient(); x.setId(id); return x; }
    public static BowlTemplate bowlTemplate(String id) { BowlTemplate x = new BowlTemplate(); x.setId(id); return x; }
    public static Bowl bowl(String id) { Bowl x = new Bowl(); x.setId(id); return x; }
    public static Order order(String id) { Order x = new Order(); x.setId(id); return x; }
}
