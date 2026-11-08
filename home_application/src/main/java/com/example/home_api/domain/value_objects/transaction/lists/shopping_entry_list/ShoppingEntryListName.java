package com.example.home_api.domain.value_objects.transaction.lists.shopping_entry_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingEntryListName {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingEntryListName.class);

    private ShoppingEntryListName(String value) {
        this.value = value;
    }

    public static ShoppingEntryListName of(String value) {
        return new ShoppingEntryListName(value);
    }
}
