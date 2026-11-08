package com.example.home_api.domain.common;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class WhereItem {
    String columnName;
    Object value;

    private static final Logger log = LoggerFactory.getLogger(WhereItem.class);

    public WhereItem(String columnName, Object value) {
        this.columnName = columnName;
        this.value = value;
    }

    public static WhereItem from(String columnName, Object value) {
        log.debug("execute WhereItem.from");
        if (columnName == null) {
            log.error("columnName is null");
            throw new NullPointerException("columnName is null");
        }
        if (columnName.isEmpty()) {
            log.error("columnName is empty");
            throw new NullPointerException("columnName is empty");
        }
        return new WhereItem(columnName, value);
    }

    public static String create(List<WhereItem> andItems, List<WhereItem> orItems) {

        log.debug("execute WhereItem.create");
        boolean noAnd = andItems == null || andItems.isEmpty();
        boolean noOr = orItems == null || orItems.isEmpty();

        if (noAnd && noOr) {
            return "";
        }

        String and = "";
        if (!noAnd) {
            and = andItems.stream()
                    .map(w -> buildCondition(w))
                    .collect(Collectors.joining(" AND "));
        }

        String or = "";
        if (!noOr) {
            or = orItems.stream()
                    .map(w -> buildCondition(w))
                    .collect(Collectors.joining(" OR "));
        }

        StringBuilder cond = new StringBuilder();

        if (!and.isEmpty()) {
            cond.append(and);
        }

        if (!or.isEmpty()) {
            if (!and.isEmpty()) {
                cond.append(" AND ");
            }
            if (orItems.size() == 1) {
                cond.append(or);
            } else {
                cond.append("(").append(or).append(")");
            }
        }

        return " WHERE " + trimParentheses(cond.toString());
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

    private static String buildCondition(WhereItem w) {
        Object value = w.getValue();

        if (value == null) {
            return w.getColumnName() + " IS NULL";
        }

        if (value instanceof String str) {
            if(value.equals("NULL")){
                return w.getColumnName() + " = NULL";
            }
            // 文字列が "LIKE,%hoge%"のように設定されている場合はLIKE条件にする
            if(str.chars().filter(c -> c == ',').count() == 1){
                String[] strs = str.split(",", -1);
                if(strs[0].equals("LIKE") && strs[1].matches("^%%.*%%$")){
                    return w.getColumnName() + " LIKE '" + escape(strs[1]) + "'";
                }
            }
            return w.getColumnName() + " = '" + escape(str) + "'";
        }

        if (value instanceof Number) {
            return w.getColumnName() + " = " + value;
        }

        if (value instanceof Boolean b) {
            return w.getColumnName() + " = " + (b ? "TRUE" : "FALSE");
        }

        // 想定外
        log.error("Unsupported type: " + value.getClass());
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    private static String escape(String value) {
        return value.replace("'", "''");
    }
}
