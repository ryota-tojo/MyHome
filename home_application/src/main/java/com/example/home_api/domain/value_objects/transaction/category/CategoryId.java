
package com.example.home_api.domain.value_objects.transaction.category;

import lombok.Value;

@Value
public class CategoryId {
    String value;

    public static CategoryId of(String value) {
        return new CategoryId(value);
    }
}
