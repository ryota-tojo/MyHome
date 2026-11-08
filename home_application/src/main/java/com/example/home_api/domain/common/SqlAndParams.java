package com.example.home_api.domain.common;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class SqlAndParams {
    String sql;
    List<Object> params;

    private static final Logger log = LoggerFactory.getLogger(SqlAndParams.class);

    public static SqlAndParams from(String sql, List<Object> params) {
        SqlAndParams sqlAndParams = new SqlAndParams(sql, params);
        echo(sqlAndParams);
        return sqlAndParams;
    }

    public static String toSql(SqlAndParams sqlAndParams) {
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();

        for (Object param : params) {
            String value = formatParam(param);
            sql = sql.replaceFirst("\\?", value);
        }
        return sql.replaceAll("\\s+", " ").trim().replace(", ", ",");
    }

    public static void echo(SqlAndParams sqlAndParams) {
        log.info("sqlBase: " + sqlAndParams.getSql());
        log.info("sqlParam: " + sqlAndParams.getParams());
        log.info("sql: " + toSql(sqlAndParams));
    }

    private static String formatParam(Object param) {
        if (param == null) {
            return "NULL";
        }
        if (param instanceof Number) {
            return param.toString();
        }
        return "'" + param.toString() + "'";
    }

    public static SqlAndParams toSqlAndParamsForSelect(
            String tableName,
            List<String> columns,
            List<WhereItem> whereItemsForAnd,
            List<WhereItem> whereItemsForOr,
            Offset offsetValue,
            Limit limitValue
    ) {
        String base = "SELECT " + String.join(",", columns) + " FROM " + tableName + WhereItem.create(whereItemsForAnd, whereItemsForOr);
        String offset = offsetValue != null ? " OFFSET ?" : "";
        String limit = limitValue != null ? " LIMIT ?" : "";

        String sql = base.replaceAll("=\\s*(?:'[^']*'|\\d+)", "= ?") + offset + limit + ";";

        List<Object> params = getParams(base);
        if (offsetValue != null) {
            params.add(offsetValue.getValue());
        }
        if (limitValue != null) {
            params.add(limitValue.getValue());
        }

        if (countChar(sql, '?') != params.size()) {
            log.error("sqlの生成に失敗しました: sql[?]={}, param={}", countChar(sql, '?'), params.size());
            throw new IllegalArgumentException("sqlの生成に失敗しました");
        }
        return SqlAndParams.from(sql, params);

    }

    public static SqlAndParams toSqlAndParamsForInsert(
            String tableName,
            List<String> columns,
            List<Object> values
    ) {

        List<String> placeholders = values.stream().map(v -> {
            if ("NULL".equals(v)) {
                return "NULL";
            }
            if ("NOW()".equals(v) || "CURRENT_TIMESTAMP".equals(v)) {
                return "CURRENT_TIMESTAMP";
            }
            return "?";
        }).toList();

        String sql = "INSERT INTO " + tableName + "(" + String.join(", ", columns) + ") VALUES(" + String.join(", ", placeholders) + ");";
        return SqlAndParams.from(sql, values.stream().filter(v -> !"NULL".equals(v) && !"NOW()".equals(v) && !"CURRENT_TIMESTAMP".equals(v)).toList());

    }

    public static SqlAndParams toSqlAndParamsForUpdate(
            String tableName,
            List<SetItem> setItem,
            List<WhereItem> whereItemsForAnd,
            List<WhereItem> whereItemsForOr
    ) {
        System.out.println(setItem);
        System.out.println(whereItemsForAnd);
        System.out.println(whereItemsForOr);

        String base = "UPDATE " + tableName + SetItem.create(setItem) + WhereItem.create(whereItemsForAnd, whereItemsForOr) + ";";
        String sql = base.replaceAll("=\\s*(?:'[^']*'|\\d+)", "=?");
        List<Object> params = getParams(base);
        return SqlAndParams.from(sql, params);
    }

    public static SqlAndParams toSqlAndParamsForDelete(
            String tableName,
            List<WhereItem> whereItemsForAnd,
            List<WhereItem> whereItemsForOr
    ) {
        String base = "DELETE FROM " + tableName + WhereItem.create(whereItemsForAnd, whereItemsForOr) + ";";
        String sql = base.replaceAll("=\\s*(?:'[^']*'|\\d+)", "=?");
        List<Object> params = getParams(base);
        return SqlAndParams.from(sql, params);
    }

    private static List<Object> getParams(String sql) {
        List<Object> values = new ArrayList<>();

        Pattern pattern = Pattern.compile("=\\s*(?:'([^']*)'|(\\d+))");
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                values.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                values.add(Integer.parseInt(matcher.group(2)));
            }
        }
        return values;
    }

    public static int countChar(String str, char target) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == target) {
                count++;
            }
        }
        return count;
    }


}
