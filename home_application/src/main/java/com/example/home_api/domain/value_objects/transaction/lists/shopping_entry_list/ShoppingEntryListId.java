package com.example.home_api.domain.value_objects.transaction.lists.shopping_entry_list;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ShoppingEntryListId {
    String value;

    private static final Logger log = LoggerFactory.getLogger(ShoppingEntryListId.class);

    private ShoppingEntryListId(String value) {
        this.value = value;
    }

    public static ShoppingEntryListId of(String value) {
        return new ShoppingEntryListId(value);
    }
}
