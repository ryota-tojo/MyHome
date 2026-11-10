package com.example.home_api.web;

import java.time.LocalDateTime;

/**
 * 【バージョン管理】
 * デプロイ時に未登録なら自動投入するバージョン情報。
 * 新しいレコードはEnum定数を追加するだけで登録対象になる。
 * 同じversionがDBに存在する場合、既存レコードは上書きしない。
 */
public enum DefaultVersion {
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
