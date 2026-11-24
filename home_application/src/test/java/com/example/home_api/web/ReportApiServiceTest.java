package com.example.home_api.web;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportApiServiceTest {
    @Test
    void categoryWithoutBudgetHasNoUsageRate() {
        var result = ReportApiService.toCategoryExpense(Map.of(
                "category_name", "食費", "budget", 0L, "expense", 3000L, "income", 1000L));

        assertThat(result.maximum()).isEqualTo(1000L);
        assertThat(result.displayAmount()).isEqualTo(4000L);
        assertThat(result.usageRate()).isNull();
        assertThat(result.status()).isEqualTo("NO_BUDGET");
    }

    @Test
    void categoryUsesBudgetPlusIncomeAsMaximum() {
        var result = ReportApiService.toCategoryExpense(Map.of(
                "category_name", "食費", "budget", 8000L, "expense", 5000L, "income", 2000L));

        assertThat(result.maximum()).isEqualTo(10000L);
        assertThat(result.usageRate()).isEqualByComparingTo(new BigDecimal("50.0"));
        assertThat(result.status()).isEqualTo("NORMAL");
    }

    @Test
    void categoryOverNinetyPercentIsWarning() {
        var result = ReportApiService.toCategoryExpense(Map.of(
                "category_name", "食費", "budget", 10000L, "expense", 9001L, "income", 0L));

        assertThat(result.status()).isEqualTo("WARNING");
    }

    @Test
    void categoryOverMaximumIsOver() {
        var result = ReportApiService.toCategoryExpense(Map.of(
                "category_name", "食費", "budget", 10000L, "expense", 10001L, "income", 0L));

        assertThat(result.status()).isEqualTo("OVER");
    }
}
