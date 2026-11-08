
package com.example.home_api.domain.value_objects.transaction.shopping_label;

import lombok.Value;

@Value
public class ShoppingLabelId {
    String value;

    public static ShoppingLabelId of(String value) {
        return new ShoppingLabelId(value);
    }
}
