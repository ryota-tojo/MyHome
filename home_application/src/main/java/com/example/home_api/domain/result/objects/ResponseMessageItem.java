package com.example.home_api.domain.result.objects;

public enum ResponseMessageItem {
    成功_参照("%sを参照しました。"),
    成功_作成("%sを作成しました。"),
    成功_更新("%sを更新しました。"),
    成功_削除("%sを削除しました。"),

    失敗_参照("%sの参照に失敗しました。"),
    失敗_作成("%sの作成に失敗しました。"),
    失敗_更新("%sの更新に失敗しました。"),
    失敗_削除("%sの削除に失敗しました。");

    public final String value;

    ResponseMessageItem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
