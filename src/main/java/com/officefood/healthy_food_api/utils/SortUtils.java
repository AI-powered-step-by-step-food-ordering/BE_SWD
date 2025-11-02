package com.officefood.healthy_food_api.utils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for sorting entities
 */
public class SortUtils {

    /**
     * Sort a list of entities by a specific field and direction
     * @param entities List of entities to sort
     * @param sortBy Field name to sort by
     * @param sortDir Direction: "asc" or "desc"
     * @return Sorted list
     */
    public static <T> List<T> sortEntities(List<T> entities, String sortBy, String sortDir) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        try {
            Comparator<T> comparator = (entity1, entity2) -> {
                try {
                    Object value1 = getFieldValue(entity1, sortBy);
                    Object value2 = getFieldValue(entity2, sortBy);

                    // Handle null values - nulls last
                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return 1;
                    if (value2 == null) return -1;

                    int result = compareValues(value1, value2);
                    return ascending ? result : -result;
                } catch (Exception e) {
                    return 0;
                }
            };

            return entities.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return entities;
        }
    }

    /**
     * Get field value from entity using reflection
     */
    private static Object getFieldValue(Object entity, String fieldName) throws Exception {
        Field field = findField(entity.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(entity);
        }
        return null;
    }

    /**
     * Find field in class hierarchy (including parent classes)
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Compare two values based on their type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareValues(Object value1, Object value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            if (value1.getClass().equals(value2.getClass())) {
                return ((Comparable) value1).compareTo(value2);
            }
        }
        // Fallback to string comparison
        return value1.toString().compareTo(value2.toString());
    }
}

