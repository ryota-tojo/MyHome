package com.example.home_api.domain.value_objects.transaction.lists.shopping_search_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingSearchListId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingSearchListId.class);

    private ShoppingSearchListId(String value) {
        this.value = value;
    }

    public static ShoppingSearchListId of(String value) {
        return new ShoppingSearchListId(value);
    }
}
