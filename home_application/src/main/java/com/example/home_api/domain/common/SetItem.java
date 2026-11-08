package com.example.home_api.domain.common;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class SetItem {
    String columnName;
    Object value;

    private static final Logger log = LoggerFactory.getLogger(SetItem.class);

    public SetItem(String columnName, Object value) {
        this.columnName = columnName;
        this.value = value;
    }

    public static SetItem from(String columnName, Object value) {
        if (columnName == null) {
            log.error("columnName is null");
            throw new NullPointerException("columnName is null");
        }
        if (columnName.isEmpty()) {
            log.error("columnName is empty");
            throw new NullPointerException("columnName is empty");
        }
        return new SetItem(columnName, value);
    }

    public static String create(List<SetItem> setItems) {

        boolean noSet = setItems == null || setItems.isEmpty();

        if (noSet) {
            log.error("setItems is empty");
            throw new NullPointerException("setItem is empty");
        }

        String set = setItems.stream()
                .map(w -> buildCondition(w))
                .collect(Collectors.joining(", "));
        return " SET " + set;
    }

    private static String trimParentheses(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("(") && str.endsWith(")")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static String buildCondition(SetItem w) {
        Object value = w.getValue();

        if (value.toString() == null) {
            return w.getColumnName() + "=NULL";
        }
        if (value.toString().equals("null")) {
            return w.getColumnName() + "=NULL";
        }

        if (value instanceof String str) {
            if(str.equals("NULL")){
                return w.getColumnName() + "=NULL";
            }
            if(str.equals("NOW()")){
                return w.getColumnName() + "=CURRENT_TIMESTAMP";
            }
            if(str.equals("CURRENT_TIMESTAMP")){
                return w.getColumnName() + "=CURRENT_TIMESTAMP";
            }
            return w.getColumnName() + "='" + escape(str) + "'";
        }

        if (value instanceof Number) {
            return w.getColumnName() + "=" + value;
        }

        if (value instanceof Boolean b) {
            return w.getColumnName() + "=" + (b ? "TRUE" : "FALSE");
        }

        // 想定外
        log.error("Unsupported type: " + value.getClass());
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    private static String escape(String value) {
        return value.replace("'", "''");
    }
}
