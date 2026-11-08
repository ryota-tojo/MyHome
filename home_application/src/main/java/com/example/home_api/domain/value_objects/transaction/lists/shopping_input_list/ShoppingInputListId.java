package com.example.home_api.domain.value_objects.transaction.lists.shopping_input_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingInputListId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingInputListId.class);

    private ShoppingInputListId(String value) {
        this.value = value;
    }

    public static ShoppingInputListId of(String value) {
        return new ShoppingInputListId(value);
    }
}
