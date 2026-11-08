
package com.example.home_api.domain.value_objects.transaction.shopping_label;

import lombok.Value;

@Value
public class ShoppingLabelName {
    String value;

    public static ShoppingLabelName of(String value) {
        return new ShoppingLabelName(value);
    }
}
