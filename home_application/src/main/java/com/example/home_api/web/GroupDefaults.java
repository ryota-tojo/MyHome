package com.example.home_api.web;

import java.util.List;

final class GroupDefaults {
    static final List<String> CATEGORIES = List.of("カテゴリー１", "カテゴリー２", "カテゴリー３");
    static final List<String> PAYMENT_METHODS = List.of(
            "現金",
            "クレジットカード",
            "電子決済（PayPay・Suicaなど）",
            "銀行決済（振込・口座振替）",
            "ポイント・ギフト券",
            "その他"
    );
    private GroupDefaults() {}
}
