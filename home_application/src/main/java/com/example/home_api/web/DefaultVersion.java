package com.example.home_api.web;

import java.time.LocalDateTime;

/**
 * 【バージョン管理】
 * デプロイ時に未登録なら自動投入するバージョン情報。
 * 新しいレコードはEnum定数を追加するだけで登録対象になる。
 * 同じversionがDBに存在する場合、既存レコードは上書きしない。
 */
public enum DefaultVersion {
    v1_0_3(
            "1.0.3",
            LocalDateTime.of(2026, 11, 24, 0, 0),
            "レポートAPIを実装",
            true
    ),
    v1_0_2(
            "1.0.2",
            LocalDateTime.of(2026, 11, 18, 0, 0),
            "【所属グループ】（購入データ入力画面）入力日時をシステム設定のタイムゾーンに依存するように修正",
            true
    ),
    v1_0_1(
            "1.0.1",
            LocalDateTime.of(2026, 11, 18, 0, 0),
            "【所属グループ】（ホーム画面）カテゴリ別支出グラフ修正対応 ",
            true
    ),
    INITIAL_RELEASE(
            "1.0.0",
            LocalDateTime.of(2026, 11, 9, 0, 0),
            "家計簿アプリ 初回リリース",
            true
    ),
    ;

    private final String version;
    private final LocalDateTime publishedAt;
    private final String content;
    private final boolean enabled;

    DefaultVersion(String version, LocalDateTime publishedAt, String content, boolean enabled) {
        this.version = version;
        this.publishedAt = publishedAt;
        this.content = content;
        this.enabled = enabled;
    }

    public String version() { return version; }
    public LocalDateTime publishedAt() { return publishedAt; }
    public String content() { return content; }
    public boolean enabled() { return enabled; }
}
