package com.example.home_api.web;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportApiService {
    private final JdbcTemplate jdbc;
    private final FinanceService financeService;

    public ReportApiService(JdbcTemplate jdbc, FinanceService financeService) {
        this.jdbc = jdbc;
        this.financeService = financeService;
    }

    public List<GroupTarget> groups() {
        return jdbc.query("""
                SELECT g.group_id, g.group_name,
                       COALESCE(gs.value, 'false') AS line_enabled
                  FROM "group" g
                  LEFT JOIN group_setting gs
                    ON gs.group_id = g.group_id AND gs.key = 'line_enabled'
                 WHERE g.status = 'ACTIVE'
                 ORDER BY g.group_name, g.group_id
                """, (rs, rowNum) -> new GroupTarget(
                rs.getString("group_id"), rs.getString("group_name"),
                Boolean.parseBoolean(rs.getString("line_enabled"))));
    }

    public LineConnection lineConnection(String groupId) {
        Map<String, String> settings = jdbc.query("""
                SELECT g.group_id,
                       COALESCE(MAX(CASE WHEN gs.key='line_enabled' THEN gs.value END), 'false') line_enabled,
                       COALESCE(MAX(CASE WHEN gs.key='line_url' THEN gs.value END), '') line_url,
                       COALESCE(MAX(CASE WHEN gs.key='line_token' THEN gs.value END), '') line_token,
                       COALESCE(MAX(CASE WHEN gs.key='line_destination_id' THEN gs.value END), '') line_destination_id
                  FROM "group" g
                  LEFT JOIN group_setting gs ON gs.group_id=g.group_id
                 WHERE g.group_id=? AND g.status='ACTIVE'
                 GROUP BY g.group_id
                """, rs -> {
            if (!rs.next()) return null;
            return Map.of(
                    "enabled", rs.getString("line_enabled"),
                    "url", rs.getString("line_url"),
                    "token", rs.getString("line_token"),
                    "destination", rs.getString("line_destination_id"));
        }, groupId);
        if (settings == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "所属グループが見つかりません");
        boolean enabled = Boolean.parseBoolean(settings.get("enabled"));
        if (!enabled)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LINE通知が無効です");
        if (settings.get("url").isBlank() || settings.get("token").isBlank() || settings.get("destination").isBlank())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LINE接続情報が未設定です");
        return new LineConnection(groupId, true, settings.get("url"), settings.get("token"), settings.get("destination"));
    }

    public MonthlyCategoryReport monthlyCategoryReport(String groupId, int year, int month) {
        String groupName = jdbc.query(
                "SELECT group_name FROM \"group\" WHERE group_id=? AND status='ACTIVE'",
                rs -> rs.next() ? rs.getString(1) : null, groupId);
        if (groupName == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "所属グループが見つかりません");
        Map<String, Object> summary = financeService.summary(groupId, year, month);
        List<CategoryExpense> categories = financeService.categorySummary(groupId, year, month).stream()
                .map(ReportApiService::toCategoryExpense).toList();
        return new MonthlyCategoryReport(groupId, groupName, year, month,
                number(summary.get("income")), number(summary.get("expense")),
                number(summary.get("balance")), categories);
    }

    static CategoryExpense toCategoryExpense(Map<String, Object> row) {
        long budget = number(row.get("budget"));
        long expense = number(row.get("expense"));
        long income = number(row.get("income"));
        long maximum = budget + income;
        BigDecimal usageRate = budget == 0 || maximum == 0 ? null
                : BigDecimal.valueOf(expense).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(maximum), 1, RoundingMode.HALF_UP);
        String status = budget == 0 ? "NO_BUDGET"
                : expense > maximum ? "OVER"
                : expense * 10 > maximum * 9 ? "WARNING" : "NORMAL";
        return new CategoryExpense(String.valueOf(row.get("category_name")), budget, expense, income,
                maximum, budget == 0 ? expense + income : expense, usageRate, status);
    }

    private static long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public record GroupTarget(String groupId, String groupName, boolean lineEnabled) {}
    public record LineConnection(String groupId, boolean lineEnabled, String apiUrl, String token,
                                 String destinationId) {}
    public record MonthlyCategoryReport(String groupId, String groupName, int year, int month,
                                        long income, long expense, long balance,
                                        List<CategoryExpense> categories) {}
    public record CategoryExpense(String categoryName, long budget, long expense, long income,
                                  long maximum, long displayAmount, BigDecimal usageRate, String status) {}
}
