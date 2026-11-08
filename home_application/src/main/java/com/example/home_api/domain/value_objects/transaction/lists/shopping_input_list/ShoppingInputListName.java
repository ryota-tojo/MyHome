package com.example.home_api.domain.value_objects.transaction.lists.shopping_input_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingInputListName {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingInputListName.class);

    private ShoppingInputListName(String value) {
        this.value = value;
    }

    public static ShoppingInputListName of(String value) {
        return new ShoppingInputListName(value);
    }
}
