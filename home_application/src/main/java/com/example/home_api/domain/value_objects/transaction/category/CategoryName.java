
package com.example.home_api.domain.value_objects.transaction.category;

import lombok.Value;

@Value
public class CategoryName {
    String value;

    public static CategoryName of(String value) {
        return new CategoryName(value);
    }
}
