package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling sort parameters
 * Example: ?sortBy=createdAt&sortDir=desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortRequest {
    private String sortBy;
    private String sortDir;

    /**
     * Get sort direction as enum
     */
    public SortDirection getSortDirection() {
        if (sortDir == null) {
            return SortDirection.DESC;
        }
        return SortDirection.fromString(sortDir);
    }

    public enum SortDirection {
        ASC("asc"),
        DESC("desc");

        private final String value;

        SortDirection(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SortDirection fromString(String value) {
            if (value == null) {
                return DESC;
            }
            for (SortDirection direction : SortDirection.values()) {
                if (direction.value.equalsIgnoreCase(value)) {
                    return direction;
                }
            }
            return DESC; // Default to DESC if invalid
        }
    }
}

