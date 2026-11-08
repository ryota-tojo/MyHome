package com.example.home_api.domain.value_objects.transaction.lists.shopping_search_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingSearchListName {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingSearchListName.class);

    private ShoppingSearchListName(String value) {
        this.value = value;
    }

    public static ShoppingSearchListName of(String value) {
        return new ShoppingSearchListName(value);
    }
}
