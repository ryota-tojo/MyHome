package com.example.home_api.web;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupDefaultsTest {
    @Test void newGroupsUseTheConfiguredInitialMasters() {
        assertEquals(List.of("カテゴリー１", "カテゴリー２", "カテゴリー３"), GroupDefaults.CATEGORIES);
        assertEquals(List.of("現金", "クレジットカード", "電子決済（PayPay・Suicaなど）", "銀行決済（振込・口座振替）", "ポイント・ギフト券", "その他"), GroupDefaults.PAYMENT_METHODS);
    }
}
